package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.activity_bind_phone.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelCommit
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.modules.login.CountryActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class BindPhoneActivity : BaseAct() {
    private val map = HashMap<String, String>()
    private lateinit var timer: TimeCount
    private var isIDEL = false
    private var isCoding = false
    private var national: NationalData.NationalEntity? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_bind_phone
    }

    override fun initView() {

    }

    override fun initData() {
        national = NationalData.NationalEntity("86", "CN")
        if (TextUtils.isEmpty(intent.getStringExtra("phone"))) {
            tv_Title.text = resources.getString(R.string.string_bindPhoneAct_1)
        } else {
            tv_Title.text = resources.getString(R.string.string_bindPhoneAct_2)
            tv_UnBind.visibility = View.VISIBLE
        }
        timer = TimeCount(60000, 1000)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_National.setOnClickListener { startActivityForResult(Intent(this, CountryActivity::class.java), 0) }
        et_Phone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isIDEL) {
                    tv_GetCode.isEnabled = !TextUtils.isEmpty(s?.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        et_Code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isCoding)
                    tv_Commit.isSelected = !TextUtils.isEmpty(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        tv_Commit.setOnClickListener {
            if (!tv_Commit.isSelected)
                return@setOnClickListener
            map["verifyCode"] = et_Code.text.toString().trim()
            request(1)
        }
        tv_GetCode.setOnClickListener {
            if (AppTools.isEmptyEt(et_Phone, 0)) {
                showToast(getString(R.string.string_loginAct_2))
                return@setOnClickListener
            }
            map["mobile"] = et_Phone.text.toString().trim()
            map["national"] = tv_National.text.toString().replace("+", "")
            request(0)
        }
        tv_UnBind.setOnClickListener {
            DialogCancelCommit(this).setTitle_Contnet(resources.getString(R.string.string_check_reset_phone), resources.getString(R.string.string_reset_phone_hint)).setOnClickListener(View.OnClickListener {
                request(2)
            }).show()
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "code/${national?.area_code}/${map["mobile"]}/1", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 倒计时60S
                         */
                        if ((result as BaseRepData).code == 0) {
                            timer.start()
                            isCoding = true
                        } else {
                            showToast(result.msg)
                        }
                        et_Code.requestFocus()
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                val formBody = FormBody.Builder()
                        .add("countryCode", national?.area_code)
                        .add("mobile", map["mobile"])
                        .add("smsCode", map["verifyCode"])
                        .build()
                OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            showToast(resources.getString(R.string.string_bindPhoneAct_5))
                            setResult(Activity.RESULT_OK, Intent().putExtra("phone", map["mobile"]))
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
            2 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                val formBody = FormBody.Builder()
                        .add("mobile", "")
                        .build()
                OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            setResult(Activity.RESULT_OK, Intent().putExtra("phone", ""))
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0)
                data?.getParcelableExtra<NationalData.NationalEntity>("national")?.let {
                    national = it
                    tv_National.text = "+${it.phone_code}"
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    private inner class TimeCount(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onFinish() {
            tv_GetCode.isEnabled = true
            tv_GetCode.text = resources.getString(R.string.string_bindPhoneAct_6)
            isIDEL = false
        }

        override fun onTick(millisUntilFinished: Long) {
            isIDEL = true
            tv_GetCode.text = "${resources.getString(R.string.string_bindPhoneAct_6)}(${millisUntilFinished / 1000})"
            tv_GetCode.isEnabled = false
        }
    }

}