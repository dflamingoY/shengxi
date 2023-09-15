package org.xiaoxingqi.shengxi.modules.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent
import kotlinx.android.synthetic.main.activity_login.*
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
import org.xiaoxingqi.shengxi.dialog.DialogEnterSystemSet
import org.xiaoxingqi.shengxi.dialog.DialogPermission
import org.xiaoxingqi.shengxi.dialog.DialogWaring
import org.xiaoxingqi.shengxi.impl.QQLoginUI
import org.xiaoxingqi.shengxi.impl.WechatLoginEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.JudeAccountData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.service.SocketThread
import org.xiaoxingqi.shengxi.utils.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * 判斷是否點擊不在提醒  单个权限进行赋值
 * 必须2个都点击不在提示, 在弹出去设置的窗口
 */
class LoginActivity : BaseLoginAct() {
    private var mUiListener: QQLoginUI? = null
    private lateinit var mTencent: Tencent
    private val map = HashMap<String, String>()
    private lateinit var national: NationalData.NationalEntity
    private var dialogSys: DialogEnterSystemSet? = null
    private var dialogPermission: DialogPermission? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: LoginActivity? = null
        private const val REQUEST_NATIONAL = 0x99
        private const val REQUEST_PERMISSION_PHONE_STATE = 0x100
        private const val REQUEST_PERMISSION_EXTERNAL_STORAGE = 0x101
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun initView() {

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.P*/) {
            if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) && checkPermissionState(Manifest.permission.READ_PHONE_STATE)) {//都没有权限赋予
                if (SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false) && SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)) {//都是设置了 不在提示
                    dialogSys = DialogEnterSystemSet(this).setLoginPermission(checkPermissionState(Manifest.permission.READ_PHONE_STATE), checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    dialogSys!!.show()
                } else {//都没有设置 或者只设置了一个
                    dialogPermission = DialogPermission(this).setPermissionState(!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false), !SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)).setOnClickListener(View.OnClickListener {
                        if (!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false)) {//請求手機狀態
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_PHONE_STATE)
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_EXTERNAL_STORAGE)
                        }
                    })
                    dialogPermission!!.show()
                }
            } else if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) || checkPermissionState(Manifest.permission.READ_PHONE_STATE)) {//赋予了单个权限
                if ((!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false) && checkPermissionState(Manifest.permission.READ_PHONE_STATE))
                        || (!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false) && checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    dialogPermission = DialogPermission(this).setPermissionState(checkPermissionState(Manifest.permission.READ_PHONE_STATE), checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)).setOnClickListener(View.OnClickListener {
                        if (!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false)) {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_PHONE_STATE)
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_EXTERNAL_STORAGE)
                        }
                    })
                    dialogPermission!!.show()
                } else {
                    dialogSys = DialogEnterSystemSet(this).setLoginPermission(checkPermissionState(Manifest.permission.READ_PHONE_STATE), checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    dialogSys!!.show()
                }
            } else {
                dialogSys?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
                dialogPermission?.let {
                    if (it.isShowing)
                        it.dismiss()
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    qConfig()
                }
            }
        } /*else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            //android 10 .0以上只需要申请文件权限
            if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//没有文件权限
                if (SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)) {//都是设置了 不在提示
                    dialogSys = DialogEnterSystemSet(this).setLoginPermission(false, checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    dialogSys!!.show()
                } else {
                    dialogPermission = DialogPermission(this).setPermissionState(false, !SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)).setOnClickListener(View.OnClickListener {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_EXTERNAL_STORAGE)
                    })
                    dialogPermission!!.show()
                }
            } else {
                dialogSys?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
                dialogPermission?.let {
                    if (it.isShowing)
                        it.dismiss()
                }
                qConfig()
            }
        }*/
    }

    override fun onPause() {
        super.onPause()
        dialogPermission?.let {
            if (it.isShowing)
                it.dismiss()
        }
        dialogSys?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun initData() {
        SocketThread.instances(this.applicationContext).onDisConnect()
        LocalLogUtils.writeLog("login:init login page", System.currentTimeMillis())
        SplashActivity.instance?.finish()
        national = NationalData.NationalEntity("86", "CN")
        instance = this
        mTencent = Tencent.createInstance(IConstant.QQ_ID, this)
        SPUtils.setString(this, IConstant.ISREFRESHTOKEN, "")
        SPUtils.setInt(this, IConstant.HAS_CHECKED_CODE, 0)//重置需要验证标记
        SPUtils.setString(this, IConstant.PWD_DEFAULT_COVER, "")
    }

    /**
     * 判断是否是第一次进入APP  SP中无数据, 并且 本地文件无数据
     */
    private fun qConfig() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {//android 10.0 之后采用此方法保证唯一的标识符
            val spUUid = SPUtils.getString(this, IConstant.CACHEUUID, "")
            val readLine = try {
                val reader = BufferedReader(FileReader(File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.UUIDFILE)))
                reader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            //授权访问
            File(Environment.getExternalStorageDirectory().absolutePath, IConstant.DOCNAME).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            if (TextUtils.isEmpty(spUUid) && TextUtils.isEmpty(readLine)) {
                //都为空,表示第一次进入app
                val uniqueID = UUID.randomUUID().toString()
                SPUtils.setString(this, IConstant.CACHEUUID, uniqueID)
                try {
                    FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                        write(uniqueID)
                        close()
                    }
                } catch (e: Exception) {
                }
            } else if (!TextUtils.isEmpty(readLine) && TextUtils.isEmpty(spUUid)) {
                SPUtils.setString(this, IConstant.CACHEUUID, readLine)
            } else if (!TextUtils.isEmpty(spUUid) && TextUtils.isEmpty(readLine)) {
                try {//open failed: ENOENT (No such file or directory)
                    FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                        write(spUUid)
                        close()
                    }
                } catch (e: Exception) {
                }
            } else {//本地文件被篡改
                try {
                    if (readLine != spUUid) {
                        FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                            write(spUUid)
                            close()
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * @return true 表示需要申请权限, false otherWise
     */
    private fun checkPermissionState(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }

    override fun initEvent() {
        tv_Sign.setOnClickListener {
//            startActivity(Intent(this, SignActivity::class.java))
            startActivity<SignGuideActivity1>()
        }
        tvLogin.setOnClickListener {
            if (et_Phone.text.isEmpty()) {
                showToast(resources.getString(R.string.string_no_empty_phone))
                return@setOnClickListener
            }
            map["mobile"] = et_Phone.text.toString().trim()
            map["national"] = tv_National.text.toString().replace("+", "")
            request(0)
        }
        btn_Sina.setOnClickListener {
            /* val sina = ShareSDK.getPlatform(SinaWeibo.NAME)
             if (!sina.isClientValid) {
                 showToast("未安装新浪微博")
                 return@setOnClickListener
             }*/
            transLayout.showProgress()
            val plat = ShareSDK.getPlatform(SinaWeibo.NAME)
//            ShareSDK.setActivity(this)//存在内存泄漏
            plat.platformActionListener = object : PlatformActionListener {

                override fun onComplete(platform: Platform, i: Int, hashMap: HashMap<String, Any>) {
                    val formBody = FormBody.Builder()
                            .add("openId", platform.db.userId)
                            .add("nickName", platform.db.userName)
                            .add("avatarUrl", platform.db.userIcon)
                            .add("userGender", if (platform.db.userGender == "m") "1" else "2")
                            .add("authType", "3")
//                    thirdLogin(formBody)
                    thirdStatus(3, platform.db.userId, formBody, platform.db.userName)
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
        tv_National.setOnClickListener {
            startActivityForResult(Intent(this, CountryActivity::class.java), REQUEST_NATIONAL)
        }
        tvPolicy.setOnClickListener {
            request(3)
        }
    }

    private fun loginQQ() {
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
                thirdStatus(2, mTencent.openId, formBody, nickName)
//                thirdLogin(formBody)
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//检测手机是否是注册
                transLayout.showProgress()
                OkClientHelper.get(this, "users/${national.area_code}/${map["mobile"]}", JudeAccountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as JudeAccountData
                        if (result.code == 0) {
                            if (result.data.is_forbidden == 0) {
                                if (result.data.is_exist == 1) {
                                    request(1)
                                } else {
                                    transLayout.showContent()
                                    /* startActivity(Intent(this@LoginActivity, SignActivity::class.java)
                                             .putExtra("mobile", map["mobile"])
                                             .putExtra("national", national)
                                     )*/
                                    startActivity<SignGuideActivity1>("phone" to map["mobile"])
                                }
                            } else {
                                DialogWaring(this@LoginActivity).show()
                            }
                        } else {
                            transLayout.showContent()
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@LoginActivity)) {
                            showToast("网络异常,请稍后再试~")
                        }
                    }
                })
            }
            1 -> {//获取验证码
                LocalLogUtils.writeLog("login:手机号登录 ", System.currentTimeMillis())
                OkClientHelper.get(this, "users/code/${national.area_code}/${map["mobile"]}/${if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, map["mobile"]) else PhoneUtil.getInstance(this).imei}", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            startActivity(Intent(this@LoginActivity, VerifyActivity::class.java)
                                    .putExtra("mobile", map["mobile"])
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
                                startActivity(Intent(this@LoginActivity, ArgumentActivity::class.java)
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
                                startActivity(Intent(this@LoginActivity, ArgumentActivity::class.java)
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

    /**
     * 查询用户信息
     */
    private fun userInfo() {
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${obj.user_id}", UserInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                PreferenceTools.saveObj(this@LoginActivity, IConstant.USERCACHE, result as UserInfoData)
                SPUtils.setString(this@LoginActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, result.data.interface_type)
                /*     when (*//*TextUtils.isEmpty(result.data.nick_name) -> {
                                startActivity(Intent(this@LoginActivity, SignNameActivity::class.java)
                                        .putExtra("mobile", result.data.mobile)
                                        .putExtra("uid", result.data.user_id.toString())
                                        .putExtra("national", NationalData.NationalEntity("0", result.data.country_code))
                                )
                            }*//*
                    result.data.interface_type_setted) {
                    "0" -> {
                        startActivity(Intent(this@LoginActivity, SelectCharacterTypeActivity::class.java)
                                .putExtra("isCanBack", false)
                                .putExtra("uid", result.data.user_id.toString()))
                        *//* else {
                                    startActivity(Intent(this@LoginActivity, SignRecordActivity::class.java)
                                            .putExtra("isCanBack", false)
                                            .putExtra("uid", result.data.user_id.toString()))
                                }*//*
                        SPUtils.setBoolean(this@LoginActivity, IConstant.WORLD_SHOW_TAB + result.data.user_id, true)
                    }
                    else -> {
                    }
                }*/
                SPUtils.setString(this@LoginActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, result.data.interface_type)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun thirdStatus(type: Int, openId: String, formBody: FormBody.Builder, name: String, unionId: String? = null) {
        transLayout.showProgress()
        OkClientHelper.get(this, "thirds/checkRegister?authType=$type&openId=$openId" + if (TextUtils.isEmpty(unionId)) "" else
            "&unionId=$unionId", JudeAccountData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as JudeAccountData
                if (result.code == 0) {
                    if (result.data.forbidden == 1) {
                        DialogWaring(this@LoginActivity).show()
                    } else {
                        if (result.data.exists == 1) {
                            thirdLogin(formBody)
                        } else {
                            startActivity<SignGuideActivity1>(
                                    "openId" to openId,
                                    "unionId" to unionId,
                                    "authType" to type,
                                    "name" to name,
                                    "third" to true
                            )
                        }
                    }
                }

            }
        }, "V4.5")
    }

    /**
     * 第三方登录
     */
    private fun thirdLogin(formBody: FormBody.Builder) {
        LocalLogUtils.writeLog("login:第三方登录 ", System.currentTimeMillis())
        transLayout.showProgress()
        formBody.add("countryCode", "CN")
                .add("appVersion", AppTools.getVersion(this))
                .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                .add("platformId", "1")
                .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else {
                    if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei
                })
        OkClientHelper.post(this, "users/third/login", formBody.build(), LoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as LoginData).code == 0) {
                    SPUtils.setInt(this@LoginActivity, IConstant.HAS_CHECKED_CODE, if (result.data.need_login_check == 1) 1 else 0)
                    PreferenceTools.saveObj(this@LoginActivity, IConstant.LOCALTOKEN, result.data)
                    userInfo()
                    SPUtils.setString(this@LoginActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                if (null != any) {
                    if (any.toString() == "412") {
                        DialogWaring(this@LoginActivity).show()
                    } else {
                        showToast(any.toString())
                    }
                }
                transLayout.showContent()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        PhoneUtil.getInstance(this).onDestroy()
        map.clear()
        mTencent.releaseResource()
        instance = null
        mUiListener = null
    }

    /**
     * QQ 登录的回调
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, QQLoginUI())
        Tencent.handleResultData(data, QQLoginUI())
        if (requestCode == Constants.REQUEST_LOGIN)
            mTencent.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_NATIONAL) {
                data?.let {
                    national = it.getParcelableExtra("national")
                    tv_National.text = "+${national.phone_code}"
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SocketThread.instances(this.applicationContext).onDestroy()
//            stopService(Intent(this, SocketServer::class.java))
            Process.killProcess(Process.myPid())
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun wechatEvent(event: WechatLoginEvent) {
        if (SignActivity.instance != null)
            return
        transLayout.showContent()
        if (event.resp != null) {
            val formBody = FormBody.Builder()
                    .add("openId", event.resp?.openid)
                    .add("unionId", event.resp!!.unionid)
                    .add("nickName", event.resp!!.nickname)
                    .add("avatarUrl", event.resp!!.headimgurl)
                    .add("userGender", (event.resp!!.sex + 1).toString())
                    .add("authType", "1")
//            thirdLogin(formBody)
            thirdStatus(1, event.resp!!.openid, formBody, event.resp!!.nickname, event.resp!!.unionid)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if (checkPermissionState(Manifest.permission.READ_PHONE_STATE) && !SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false)) {
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_PHONE_STATE)
//                } else {
//                }
                dialogPermission?.let {
                    it.dismiss()
                }
            } else {
//                if (checkPermissionState(Manifest.permission.READ_PHONE_STATE)) {
//                    if (!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_PHONE, false)) {
//                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_PHONE_STATE)
//                    }
//                }
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    SPUtils.setBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, true)
                }
            }
        } else if (requestCode == REQUEST_PERMISSION_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_EXTERNAL_STORAGE)
                } else
                    dialogPermission?.let {
                        it.dismiss()
                    }
            } else {
                if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (!SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_STORAGE, false)) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_EXTERNAL_STORAGE)
                    }
                }
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                    SPUtils.setBoolean(this, IConstant.PERMISSION_DENIED_PHONE, true)
                }
            }
        }
    }

}