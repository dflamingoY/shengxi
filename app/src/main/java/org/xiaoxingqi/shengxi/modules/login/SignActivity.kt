package org.xiaoxingqi.shengxi.modules.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent
import kotlinx.android.synthetic.main.activity_sign.*
import kotlinx.android.synthetic.main.activity_sign.btn_QQ
import kotlinx.android.synthetic.main.activity_sign.btn_Sina
import kotlinx.android.synthetic.main.activity_sign.btn_Wechat
import kotlinx.android.synthetic.main.activity_sign.et_Phone
import kotlinx.android.synthetic.main.activity_sign.transLayout
import kotlinx.android.synthetic.main.activity_sign.tvPolicy
import kotlinx.android.synthetic.main.activity_sign.tv_National
import kotlinx.android.synthetic.main.activity_sign.tv_Trim
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.json.JSONException
import org.json.JSONObject
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.QQLoginUI
import org.xiaoxingqi.shengxi.impl.WechatLoginEvent
import org.xiaoxingqi.shengxi.model.ArgumentData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.ListenData
import org.xiaoxingqi.shengxi.model.login.JudeAccountData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PhoneUtil
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.WXHelper
import java.lang.Deprecated

const val NATIONALCODE = 0x00

@Deprecated
class SignActivity : BaseLoginAct() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: SignActivity? = null
    }

    private lateinit var mTencent: Tencent
    private var mUiListener: QQLoginUI? = null
    private var map = HashMap<String, String>()
    private var isSign = true
    private lateinit var national: NationalData.NationalEntity
    override fun getLayoutId(): Int {
        return R.layout.activity_sign
    }

    override fun initView() {

    }

    override fun initData() {
        national = NationalData.NationalEntity("86", "CN")
        intent.getStringExtra("mobile")?.let {
            et_Phone.setText(it)
            tvHint.text = resources.getString(R.string.string_new_sign_hint_1)
        }
        intent.getParcelableExtra<NationalData.NationalEntity>("national")?.let {
            national = it
            tv_National.text = "+${it.phone_code}"
        }
        instance = this
        mTencent = Tencent.createInstance(IConstant.QQ_ID, this)
    }

    override fun initEvent() {
        tv_Login.setOnClickListener {
            map["national"] = tv_National.text.toString().trim().replace("+", "")
            map["mobile"] = et_Phone.text.toString().trim()
            request(0)
        }
        tv_National.setOnClickListener { startActivityForResult(Intent(this, CountryActivity::class.java), NATIONALCODE) }
        btn_Back.setOnClickListener { finish() }
        btn_Sina.setOnClickListener {
            transLayout.showProgress()
            val plat = ShareSDK.getPlatform(SinaWeibo.NAME)
            plat.platformActionListener = object : PlatformActionListener {

                override fun onComplete(platform: Platform, i: Int, hashMap: java.util.HashMap<String, Any>) {
                    val formBody = FormBody.Builder()
                            .add("openId", platform.db.userId)
                            .add("nickName", platform.db.userName)
                            .add("avatarUrl", platform.db.userIcon)
                            .add("userGender", if (platform.db.userGender == "m") "1" else "2")
                            .add("authType", "3")
//                    thirdLogin(formBody)
                }

                override fun onError(platform: Platform, i: Int, throwable: Throwable) {
                    showToast("登录失败")
                    transLayout.showContent()
                }

                override fun onCancel(platform: Platform, i: Int) {
                    showToast("取消授权")
                    transLayout.showContent()
                }
            }
            plat.SSOSetting(false)
            //获取用户资料
            plat.showUser(null)
            plat.removeAccount(true)
        }
        btn_Wechat.setOnClickListener {
            if (!WXHelper.loginWechat(this)) {
                Toast.makeText(this, "未安装微信客户端", Toast.LENGTH_SHORT).show()
            }
        }
        btn_QQ.setOnClickListener {
            val qq = ShareSDK.getPlatform(QQ.NAME)
            if (!qq.isClientValid) {
                showToast("未安装QQ客户端")
            } else {
                loginQQ()
            }
        }
        tv_Trim.setOnClickListener {
            request(2)
        }
        tvPolicy.setOnClickListener {
            request(2)
        }
    }

    private fun loginQQ() {
        startActivity<ThirdLoginBindActivity>()
        mUiListener = object : QQLoginUI() {

            override fun paserObj(`object`: JSONObject) {
                try {
                    mTencent.setAccessToken(`object`.getString("access_token"), `object`.getInt("expires_in").toString())
                    mTencent.openId = `object`.getString("openid")
                    queryQQInfo()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        mTencent.login(this, "all", mUiListener)
    }

    private fun queryQQInfo() {
        val userInfo = UserInfo(application, mTencent.qqToken)
        userInfo.getUserInfo(object : QQLoginUI() {
            override fun paserObj(`object`: JSONObject) {
                val nickName = if (`object`.has("nickname") && TextUtils.isEmpty(`object`.getString("nickname"))) {
                    ""
                } else {
                    try {
                        `object`.getString("nickname")
                    } catch (e: Exception) {
                        ""
                    }
                }
                val formBody = FormBody.Builder()
                        .add("openId", mTencent.openId)
                        .add("nickName", nickName)
                        .add("avatarUrl", `object`.getString("figureurl_qq_1"))
                        .add("userGender", if ("男" == `object`.getString("gender")) "1" else "2")
                        .add("authType", "2")
//                thirdLogin(formBody)

            }
        })
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {//判断是否是新用户
                OkClientHelper.get(this, "users/${national.area_code}/${map["mobile"]}", JudeAccountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as JudeAccountData
                        isSign = result.data.is_exist == 0
                        request(1)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                OkClientHelper.get(this, "users/code/${national.area_code}/${map["mobile"]}/${if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else PhoneUtil.getInstance(this).imei}", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            startActivity(Intent(this@SignActivity, VerifyActivity::class.java)
                                    .putExtra("mobile", map["mobile"])
                                    .putExtra("isSign", isSign)
                                    .putExtra("national", national))
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
            2 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "protocols/user", ArgumentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ArgumentData
                        if (result.code == 0) {
                            if (result.data != null) {
                                startActivity(Intent(this@SignActivity, ArgumentActivity::class.java)
                                        .putExtra("title", result.data.html_title)
                                        .putExtra("url", result.data.html_content)
                                        .putExtra("isHtml", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "h5/type/1", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                startActivity(Intent(this@SignActivity, ArgumentActivity::class.java)
                                        .putExtra("title", result.data[0].html_title)
                                        .putExtra("url", result.data[0].html_content)
                                        .putExtra("isHtml", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_LOGIN)
            mTencent.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NATIONALCODE) {
                data?.let {
                    national = it.getParcelableExtra("national")
                    tv_National.text = "+${national.phone_code}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PhoneUtil.getInstance(this).onDestroy()
        instance = null
        mTencent.releaseResource()
        mUiListener = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun wechatEvent(event: WechatLoginEvent) {
        Log.d("Mozator", "Sign ")


    }

}