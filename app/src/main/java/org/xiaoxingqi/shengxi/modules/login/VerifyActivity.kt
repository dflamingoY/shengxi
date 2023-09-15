package org.xiaoxingqi.shengxi.modules.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_verify.*
import kotlinx.android.synthetic.main.activity_verify.btn_Back
import kotlinx.android.synthetic.main.activity_verify.transLayout
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File

/**
 * 登录/注册 验证码
 */
class VerifyActivity : BaseLoginAct() {
    private var timer = TimeTask(60000, 1000)
    private val map = HashMap<String, String>()
    private var isSign = false
    private lateinit var national: NationalData.NationalEntity
    private var isThirdSign = false
    private var voicePath: String? = null
    private var voiceLength = 0
    private var isThird = false//是否是第三方登录

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: VerifyActivity? = null
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_verify
    }

    override fun initView() {
        reSend.isSelected = true
    }

    override fun initData() {
        instance = this
        isThird = intent.getBooleanExtra("third", false)
        isThirdSign = intent.getBooleanExtra("isThirdSign", false)
        national = intent.getParcelableExtra("national")
        map["mobile"] = intent.getStringExtra("mobile")
        tv_Phone.text = String.format(resources.getString(R.string.string_verifyAct_3), map["mobile"])
        isSign = intent.getBooleanExtra("isSign", false)
        voicePath = intent.getStringExtra("path")
        voiceLength = intent.getIntExtra("length", 0)
        timer.start()
        reSend.isSelected = false
        bordEdit.requestFocus()
    }

    override fun initEvent() {
        reSend.setOnClickListener {
            if (reSend.isSelected)
                request(0)
        }
        bordEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    map["verifyCode"] = bordEdit.text.toString().trim()
                    when {
                        isThirdSign -> {
                            request(4)
                        }
                        isSign -> {
                            request(2)
                        }
                        else -> request(1)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        btn_Back.setOnClickListener { finish() }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {//重新获取
                OkClientHelper.get(this, "users/code/${national.area_code}/${map["mobile"]}/${if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else PhoneUtil.getInstance(this).imei}", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            reSend.isSelected = false
                            timer.start()
                            bordEdit.setText("")
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
            1 -> {//登录
                val formBody = FormBody.Builder()
                        .add("mobile", map["mobile"]!!)
                        .add("countryCode", national.area_code)
                        .add("smsCode", map["verifyCode"]!!)
                        .add("appVersion", AppTools.getVersion(this))
                        .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                        .add("platformId", "1")
                        .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei)
                        .build()
                OkClientHelper.post(this, "users/login", formBody, LoginData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as LoginData).code == 0) {
                            PreferenceTools.saveObj(this@VerifyActivity, IConstant.LOCALTOKEN, result.data)
                            SPUtils.setInt(this@VerifyActivity, IConstant.HAS_CHECKED_CODE, if (result.data.need_login_check == 1) 1 else 0)
                            SPUtils.setString(this@VerifyActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                            map["userid"] = result.data.user_id
                            request(3)
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            2 -> {//注冊
                val formBody = FormBody.Builder()
                        .add("mobile", map["mobile"]!!)
                        .add("smsCode", map["verifyCode"]!!)
                        .add("countryCode", national.area_code)
                        .add("appVersion", AppTools.getVersion(this))
                        .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei)
                        .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                        .add("platformId", "1")
                        .build()
                OkClientHelper.post(this, "users/register", formBody, LoginData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as LoginData).code == 0) {
                            PreferenceTools.saveObj(this@VerifyActivity, IConstant.LOCALTOKEN, result.data)
                            SPUtils.setString(this@VerifyActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                            startActivity(Intent(this@VerifyActivity, SelectCharacterTypeActivity::class.java))
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
            3 -> {//查询用户信息
                OkClientHelper.get(this, "users/${map["userid"]}", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            /* if (result.data.interface_type_setted == "0") {
                                 startActivity(Intent(this@VerifyActivity, SelectCharacterTypeActivity::class.java)
                                         .putExtra("isCanBack", false)
                                         .putExtra("uid", result.data.user_id.toString()))
                             } else {*/
                            SPUtils.setString(this@VerifyActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, result.data.interface_type)
                            startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                            LoginActivity.instance?.finish()
                            finish()
                        } else
                            showToast(result.msg)
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            4 -> {//注册验证手机号手机号
                transLayout.showProgress()
                OkClientHelper.get(this, "code/${national.area_code}/${map["mobile"]}/check/${map["verifyCode"]}", BaseRepData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as BaseRepData
                        if (result.code == 0) {
                            //验证成功, 开始注册
                            if (!isThird) {
                                request(5)
                            } else {
                                request(6)
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }
                }, "V4.5")
            }
            5 -> {//手机号注册
                val builder = FormBody.Builder()
                builder.add("platformId", "1")
                        .add("areaCode", national.area_code)
                        .add("mobile", map["mobile"]!!)
                        .add("smsCode", map["verifyCode"]!!)
                        .add("appVersion", AppTools.getVersion(this))
                        .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                        .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei)
                        .add("answers", intent.getStringExtra("answers"))
                OkClientHelper.post(this, "users/register", builder.build(), LoginData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as LoginData
                        if (result.code == 0) {
                            PreferenceTools.saveObj(this@VerifyActivity, IConstant.LOCALTOKEN, result.data)
                            SPUtils.setString(this@VerifyActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                            SPUtils.setBoolean(this@VerifyActivity, IConstant.WORLD_SHOW_TAB + result.data.user_id, true)
                            SPUtils.setBoolean(this@VerifyActivity, IConstant.HOME_WORLD_TITLE_RED_POINT + result.data.user_id, true)
                            SPUtils.setString(this@VerifyActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, intent.getStringExtra("identity"))
                            if (!TextUtils.isEmpty(voicePath) && File(voicePath).exists()) {
                                request(7)
                            } else {
                                startActivity<MainActivity>()
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }
                }, "V4.5")
            }
            6 -> {//第三方注册
                val builder = FormBody.Builder()
                builder.add("authType", intent.getIntExtra("authType", 1).toString())
                        .add("openId", intent.getStringExtra("openId"))
                        .add("gender", "0")
                        .add("nickName", intent.getStringExtra("name"))
                        .add("platformId", "1")
                        .add("areaCode", national.area_code)
                        .add("mobile", map["mobile"]!!)
                        .add("smsCode", map["verifyCode"]!!)
                        .add("appVersion", AppTools.getVersion(this))
                        .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                        .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei)
                        .add("answers", intent.getStringExtra("answers"))
                val unionId = intent.getStringExtra("unionId")
                if (!TextUtils.isEmpty(unionId)) {
                    builder.add("unionId", unionId)
                }
                OkClientHelper.post(this, "thirds/register", builder.build(), LoginData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as LoginData
                        if (result.code == 0) {//注册成功,记录本地信息
                            PreferenceTools.saveObj(this@VerifyActivity, IConstant.LOCALTOKEN, result.data)
                            SPUtils.setString(this@VerifyActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                            SPUtils.setBoolean(this@VerifyActivity, IConstant.WORLD_SHOW_TAB + result.data.user_id, true)
                            SPUtils.setBoolean(this@VerifyActivity, IConstant.HOME_WORLD_TITLE_RED_POINT + result.data.user_id, true)
                            SPUtils.setString(this@VerifyActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, intent.getStringExtra("identity"))
                            if (!TextUtils.isEmpty(voicePath) && File(voicePath).exists()) {
                                request(7)
                            } else {
                                startActivity<MainActivity>()
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }
                }, "V4.5")
            }
            7 -> {
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                        .add("resourceType", "2")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(voicePath))}.aac")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            result.data.bucket_id?.let {
                                AppTools.bucketId = it
                            }
                            AliLoadFactory(this@VerifyActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    runOnUiThread {
                                        LocalLogUtils.writeLog("SignRecordActivity:上传成功,开始接口请求", System.currentTimeMillis())
                                        sendVoice(result.data.resource_content)
                                    }
                                }

                                override fun fail() {
                                    LocalLogUtils.writeLog("SignRecordActivity:upload fail by alibaba oss", System.currentTimeMillis())
                                    runOnUiThread { transLayout.showContent() }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {
                                }
                            }, UploadData(result.data.resource_content, voicePath))
                        } else {
                            LocalLogUtils.writeLog("SignRecordActivity:upload fail interface error code:${result.code} msg:${result.msg}", System.currentTimeMillis())
                            showToast(resources.getString(R.string.string_sign_record_hint))
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun sendVoice(uri: String) {
        val builder = FormBody.Builder()
                .add("voiceType", "2")
                .add("voiceUri", uri)
                .add("voiceLen", voiceLength.toString())
                .add("bucketId", AppTools.bucketId)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "voices", builder.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    LocalLogUtils.writeLog("SignRecordActivity:user wave setting success", System.currentTimeMillis())
                    SPUtils.setBoolean(this@VerifyActivity, IConstant.WORLD_SHOW_TAB + loginBean.user_id, true)
                    LoginActivity.instance?.finish()
                    startActivity(Intent(this@VerifyActivity, MainActivity::class.java)
                            .putExtra("isSign", true))
                    finish()
                } else {
                    LocalLogUtils.writeLog("SignRecordActivity:user wave setting fail ${result.msg}", System.currentTimeMillis())
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                LocalLogUtils.writeLog("SignRecordActivity: 接口错误 ${any.toString()}", System.currentTimeMillis())
                transLayout.showContent()
                if (any is Exception) {
                    showToast(any.message)
                } else {
                    showToast(any.toString())
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    inner class TimeTask(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            reSend.isSelected = true
            reSend.text = "重新发送"
        }

        override fun onTick(millisUntilFinished: Long) {
            reSend.text = "重新发送 (${millisUntilFinished / 1000})"
            reSend.isSelected = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PhoneUtil.getInstance(this).onDestroy()
        timer.cancel()
        instance = null
    }
}