package org.xiaoxingqi.shengxi.modules.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign.*
import kotlinx.android.synthetic.main.activity_sign.et_Phone
import kotlinx.android.synthetic.main.activity_sign.transLayout
import kotlinx.android.synthetic.main.activity_sign.tvPolicy
import kotlinx.android.synthetic.main.activity_sign.tv_National
import kotlinx.android.synthetic.main.activity_sign.tv_Trim
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCheersDelete
import org.xiaoxingqi.shengxi.dialog.DialogWaring
import org.xiaoxingqi.shengxi.model.ArgumentData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.ListenData
import org.xiaoxingqi.shengxi.model.login.JudeAccountData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PhoneUtil
import org.xiaoxingqi.shengxi.utils.SPUtils

class ThirdLoginBindActivity : BaseLoginAct() {
    private lateinit var national: NationalData.NationalEntity
    override fun getLayoutId(): Int {
        return R.layout.activity_sign
    }

    override fun initView() {
        relativeThird.visibility = View.GONE
        tvSignHint.text = "请绑定手机号,完成注册"
        tvHint.text = "为防止忘记账号 请绑定手机号"
    }

    override fun initData() {
        national = NationalData.NationalEntity("86", "CN")
        val phone = intent.getStringExtra("phone")
        if (!TextUtils.isEmpty(phone)) {
            et_Phone.setText(phone)
            et_Phone.setSelection(phone.length)
        }
    }

    override fun initEvent() {
        tv_National.setOnClickListener { startActivityForResult(Intent(this, CountryActivity::class.java), NATIONALCODE) }
        tv_Login.setOnClickListener {
            if (!AppTools.isEmptyEt(et_Phone, 0)) {
                request(1)
            }
        }
        tv_Trim.setOnClickListener {
            request(2)
        }
        tvPolicy.setOnClickListener {
            request(3)
        }
        btn_Back.setOnClickListener {
            DialogCheersDelete(this).setTitle("确定取消注册吗，将失去已录制的内容").setOnClickListener(View.OnClickListener { finish() }).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogCheersDelete(this).setTitle("确定取消注册吗，将失去已录制的内容").setOnClickListener(View.OnClickListener { finish() }).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "users/code/${national.area_code}/${et_Phone.text.trim()}/${if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else PhoneUtil.getInstance(this).imei}", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            startActivity(Intent(this@ThirdLoginBindActivity, VerifyActivity::class.java)
                                    .putExtra("isThirdSign", true)
                                    .putExtra("mobile", et_Phone.text.toString().trim())
                                    .putExtra("national", national)
                                    .putExtra("openId", intent.getStringExtra("openId"))
                                    .putExtra("unionId", intent.getStringExtra("unionId"))
                                    .putExtra("authType", intent.getIntExtra("authType", 0))
                                    .putExtra("name", intent.getStringExtra("name"))
                                    .putExtra("answers", intent.getStringExtra("answers"))
                                    .putExtra("path", intent.getStringExtra("path"))
                                    .putExtra("third", intent.getBooleanExtra("third", false))
                                    .putExtra("length", intent.getIntExtra("length", 0))
                                    .putExtra("identity", intent.getStringExtra("identity"))
                            )
                            finish()
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
            1 -> {//检测手机号是否注册过
                OkClientHelper.get(this, "users/${national.area_code}/${et_Phone.text.trim()}", JudeAccountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as JudeAccountData
                        if (result.code == 0) {
                            if (result.data.is_forbidden == 0) {
                                if (result.data.is_exist == 1) {
                                    showToast("该手机号已被注册, 请去登录")
                                } else {
                                    request(0)
                                }
                                transLayout.showContent()
                            } else {
                                DialogWaring(this@ThirdLoginBindActivity).show()
                            }
                        } else {
                            transLayout.showContent()
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@ThirdLoginBindActivity)) {
                            showToast("网络异常,请稍后再试~")
                        }
                    }
                })
            }
            2 -> {
                OkClientHelper.get(this, "protocols/user", ArgumentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ArgumentData
                        if (result.code == 0) {
                            if (result.data != null) {
                                startActivity(Intent(this@ThirdLoginBindActivity, ArgumentActivity::class.java)
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
                OkClientHelper.get(this, "h5/type/1", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                startActivity(Intent(this@ThirdLoginBindActivity, ArgumentActivity::class.java)
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NATIONALCODE) {
                data?.let {
                    national = it.getParcelableExtra("national")
                    tv_National.text = "+${national.phone_code}"
                }
            }
        }
    }
}