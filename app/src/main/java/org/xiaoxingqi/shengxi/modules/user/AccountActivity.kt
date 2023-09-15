package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent
import kotlinx.android.synthetic.main.activity_account.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogHintCancle
import org.xiaoxingqi.shengxi.dialog.DialogSimpleContent
import org.xiaoxingqi.shengxi.impl.QQLoginUI
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.set.PwdActivity
import org.xiaoxingqi.shengxi.utils.*
import java.util.HashMap

/*
    1.必须绑定手机号之后再做检测, 是否开启密码
 */
class AccountActivity : BaseAct() {
    private lateinit var mTencent: Tencent
    private lateinit var mUiListener: QQLoginUI
    private var unbindType: String = ""
    private var phone: String? = null
    private var bindCount = 0
    private var userInfoData: LoginData.LoginBean? = null
    private var isOpenPwd = 1 //0 1 2  1表示开启

    companion object {
        private const val REQUEST_EDIT_PHONE = 0x01
        private const val REQUEST_PWD = 0x02

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_account
    }

    override fun initView() {

    }

    override fun initData() {
        mTencent = Tencent.createInstance(IConstant.QQ_ID, this)
        userInfoData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        request(0)
    }

    override fun initEvent() {
        relative_Phone.setOnClickListener {
            startActivityForResult(Intent(this, BindPhoneActivity::class.java).putExtra("phone", phone), REQUEST_EDIT_PHONE)
        }
        toggle_Wechat.setOnClickListener {
            if (!toggle_Wechat.isSelected) {
                if (!WXHelper.loginWechat(this)) {
                    showToast("未安装微信客户端")
                }
            } else {//解绑
                if (bindCount == 1 && TextUtils.isEmpty(phone))
                    DialogHintCancle(this).setHintTitle(resources.getString(R.string.string_unbind_last_Account)).setOnClickListener(View.OnClickListener {
                        unbindType = "1"
                        request(3)
                    }).show()
                else
                    DialogHintCancle(this).setHintTitle("是否解除微信绑定").setOnClickListener(View.OnClickListener {
                        unbindType = "1"
                        request(3)
                    }).show()
            }
        }
        toggle_QQ.setOnClickListener {
            if (!toggle_QQ.isSelected) {
                val qq = ShareSDK.getPlatform(QQ.NAME)
                if (!qq.isClientValid) {
                    showToast("未安装QQ客户端")
                } else
                    loginQQ()
            } else {
                if (bindCount == 1 && TextUtils.isEmpty(phone))
                    DialogHintCancle(this).setHintTitle(resources.getString(R.string.string_unbind_last_Account)).setOnClickListener(View.OnClickListener {
                        unbindType = "2"
                        request(3)
                    }).show()
                else
                    DialogHintCancle(this).setHintTitle("是否解除QQ绑定").setOnClickListener(View.OnClickListener {
                        unbindType = "2"
                        request(3)
                    }).show()
            }
        }
        toggle_Weibo.setOnClickListener {
            if (!toggle_Weibo.isSelected) {
                val sina = ShareSDK.getPlatform(SinaWeibo.NAME)
                if (!sina.isClientValid) {
                    showToast("未安装新浪微博")
                    return@setOnClickListener
                }
                val plat = ShareSDK.getPlatform(SinaWeibo.NAME)
                plat.platformActionListener = object : PlatformActionListener {

                    override fun onComplete(platform: Platform, i: Int, hashMap: HashMap<String, Any>) {
                        val formBody = FormBody.Builder()
                                .add("openId", platform.db.userId)
                                .add("nickName", platform.db.userName)
                                .add("avatarUrl", platform.db.userIcon)
                                .add("userGender", if (platform.db.userGender == "m") "1" else "2")
                                .add("authType", "3").build()
                        updateThird(formBody)

                    }

                    override fun onError(platform: Platform, i: Int, throwable: Throwable) {
                        showToast("取消授权")
                    }

                    override fun onCancel(platform: Platform, i: Int) {
                        showToast("取消授权")
                    }
                }
                plat.SSOSetting(false)
                //获取用户资料
                plat.showUser(null)
                plat.removeAccount(true)
            } else {
                if (bindCount == 1 && TextUtils.isEmpty(phone))
                    DialogHintCancle(this).setHintTitle(resources.getString(R.string.string_unbind_last_Account)).setOnClickListener(View.OnClickListener {
                        unbindType = "3"
                        request(3)
                    }).show()
                else
                    DialogHintCancle(this).setHintTitle("是否解除微博绑定").setOnClickListener(View.OnClickListener {
                        unbindType = "3"
                        request(3)
                    }).show()
            }
        }
        btn_Back.setOnClickListener { finish() }
        more_language.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }
        viewPwd.setOnClickListener {
            if (TextUtils.isEmpty(phone)) {
                DialogSimpleContent(this).setContent(resources.getString(R.string.string_single_pwd_3)).show()
            } else
                startActivityForResult(Intent(this, PwdActivity::class.java)
                        .putExtra("isOpen", isOpenPwd)
                        , REQUEST_PWD)
        }
    }

    private fun loginQQ() {
        mUiListener = object : QQLoginUI() {
            override fun paserObj(`object`: JSONObject) {
                try {
                    mTencent.setAccessToken(`object`.getString("access_token"), `object`.getInt("expires_in").toString())
                    mTencent.openId = `object`.getString("openid")
                    val userInfo = UserInfo(this@AccountActivity, mTencent.qqToken)
                    userInfo.getUserInfo(object : QQLoginUI() {
                        override fun paserObj(`object`: JSONObject) {
                            val formBody = FormBody.Builder()
                                    .add("openId", mTencent.openId)
                                    .add("nickName", `object`.getString("nickname"))
                                    .add("avatarUrl", `object`.getString("figureurl_qq_1"))
                                    .add("userGender", if ("男" == `object`.getString("gender")) "1" else "2")
                                    .add("authType", "2").build()
                            transLayout.showProgress()
                            updateThird(formBody)
                        }
                    })

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        mTencent.login(this, "all", mUiListener)
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "users/${userInfoData?.user_id}", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        tv_Phone.text = resources.getString(R.string.string_AccountAct_unbind)
                        (result as UserInfoData).data.mobile?.let {
                            if (!TextUtils.isEmpty(it)) {
                                tv_Phone.text = result.data.mobile
                                phone = it
                                viewPwd.setMsgCount(resources.getString(R.string.string_statue_off))
                                request(4)
                            } else {
                                viewPwd.setMsgCount(resources.getString(R.string.string_single_pwd_2))
                            }
                        }
                        request(2)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@AccountActivity)) {
                            showToast(resources.getString(R.string.string_net_error))
                        }
                    }
                })
            }
            2 -> {
                OkClientHelper.get(this, "users/${userInfoData?.user_id}/third", BindThirdData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        toggle_QQ.setText("QQ${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_AccountAct_unbind)}")
                        toggle_Wechat.setText("微信${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_AccountAct_unbind)}")
                        toggle_Weibo.setText("微博${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_AccountAct_unbind)}")
                        bindCount = 0
                        if ((result as BindThirdData).code == 0) {
                            if (result.data.bindQQ == 1) {
                                bindCount
                                toggle_QQ.setToggle()
                                toggle_QQ.setText("QQ${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_account_bind)}")
                                bindCount++
                            }
                            if (result.data.bindWechat == 1) {
                                toggle_Wechat.setText("微信${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_account_bind)}")
                                toggle_Wechat.setToggle()
                                bindCount++
                            }
                            if (result.data.bindWeibo == 1) {
                                toggle_Weibo.setToggle()
                                toggle_Weibo.setText("微博${resources.getString(R.string.string_account_1)}：${resources.getString(R.string.string_account_bind)}")
                                bindCount++
                            }
                        }
                        if (bindCount == 0 && TextUtils.isEmpty(phone)) {
                            /**无第三方绑定, 退出登录*/
                            request(4)
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                OkClientHelper.delete(this, "users/${userInfoData?.user_id}/third/$unbindType", FormBody.Builder()
                        .build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {

                            when (unbindType) {
                                "2" -> {
                                    toggle_QQ.isSelected = false
                                }
                                "1" -> {
                                    toggle_Wechat.isSelected = false
                                }
                                "3" -> {
                                    toggle_Weibo.isSelected = false
                                }
                            }
                            request(2)
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            4 -> {
                val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${login.user_id}/settings?settingTag=other&settingName=login_check_switch", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        transLayout.showContent()
                        result.data?.let {
                            if (it.setting_name == "login_check_switch") {//是否开启了验证
                                isOpenPwd = it.setting_value
                                if (it.setting_value != 1) {
                                    viewPwd.setMsgCount(resources.getString(R.string.string_statue_off))
                                } else {//查询密码并展示
                                    request(5)
                                }
                            }
                        }

                    }
                }, "V4.2")
            }
            5 -> {
                OkClientHelper.get(this, "loginCheckCode", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as IntegerRespData
                        if (result.data?.let {
                                    if (!TextUtils.isEmpty(it.check_code)) {
                                        viewPwd.setMsgCount(it.check_code)
                                    } else {
                                        viewPwd.setMsgCount(resources.getString(R.string.string_statue_off))
                                    }
                                    it
                                } == null) {
                            viewPwd.setMsgCount(resources.getString(R.string.string_statue_off))
                        }
                    }
                }, "V4.3")
            }
        }
    }

    private fun updateThird(formbody: FormBody) {
        OkClientHelper.post(this, "users/${userInfoData?.user_id}/third", formbody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    request(2)
                } else {
                    showToast(result.msg)
                }

                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, QQLoginUI())
        Tencent.handleResultData(data, QQLoginUI())
        if (requestCode == Constants.REQUEST_LOGIN)
            mTencent.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_EDIT_PHONE) {
                data?.let {
                    val result = data.getStringExtra("phone")
                    if (!TextUtils.isEmpty(result)) {
                        tv_Phone.text = result
                        phone = result
                        //查询是否开启过密码
                        request(4)
                    } else {
                        isOpenPwd = 2
                        phone = null
                        tv_Phone.text = resources.getString(R.string.string_single_pwd_2)
                        viewPwd.setMsgCount(resources.getString(R.string.string_single_pwd_2))
                    }
                }
            }
        }
        if (requestCode == REQUEST_PWD) {
            //展示code 为空则关闭
            SPUtils.getString(this, IConstant.USER_PWD, "").let { pwd ->
                if (TextUtils.isEmpty(pwd)) {
                    isOpenPwd = 2
                    viewPwd.setMsgCount(resources.getString(R.string.string_statue_off))
                } else {
                    isOpenPwd = 1
                    viewPwd.setMsgCount(pwd)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun wechatEvent(event: WeChatEvent) {
        if (event.resp != null) {
            val formBody = FormBody.Builder()
                    .add("openId", event.resp?.openid)
                    .add("unionId", event.resp!!.unionid)
                    .add("nickName", event.resp!!.nickname)
                    .add("avatarUrl", event.resp!!.headimgurl)
                    .add("userGender", (event.resp!!.sex + 1).toString())
                    .add("authType", "1").build()
            updateThird(formBody)
            transLayout.showProgress()
        }
    }

    class WeChatEvent {

        var resp: WeUserInf? = null

        constructor(resp: WeUserInf?) {
            this.resp = resp
        }
    }

}