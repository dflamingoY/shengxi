package org.xiaoxingqi.shengxi.modules.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_sign_name.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.model.RandomNameData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.YunxinTokenData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import java.lang.Exception

class SignNameActivity : BaseLoginAct() {
    private lateinit var national: NationalData.NationalEntity
    private lateinit var mobile: String
    private var uid: String? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: SignNameActivity? = null
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_name
    }

    override fun initView() {
    }

    override fun initData() {
        instance = this
        national = intent.getParcelableExtra("national")
        mobile = intent.getStringExtra("mobile")
        uid = intent.getStringExtra("uid")
        request(5)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_Next.setOnClickListener {
            if (it.isSelected) {
                if (AppTools.isEmptyEt(et_Name, 0)) {
                    showToast("昵称不能为空")
                    return@setOnClickListener
                }
                if (it.isSelected) {
//                    request(1)
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (TextUtils.isEmpty(SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, ""))) {
                        startActivity(Intent(this, SelectCharacterTypeActivity::class.java)
                                .putExtra("name", et_Name.text.toString().trim())
                                .putExtra("isCanBack", true)
                                .putExtra("uid", uid)
                        )
                    } else {//已经选择之后直接跳转到录制声波
                        startActivity(Intent(this, SignRecordActivity::class.java)
                                .putExtra("name", et_Name.text.toString().trim())
                                .putExtra("isCanBack", true)
                                .putExtra("uid", uid)
                        )
                    }
                }
            }
        }

        tv_Random.setOnClickListener {
            request(0)
        }
        et_Name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tv_Next.isSelected = s!!.isNotEmpty()
                if (TextUtils.isEmpty(s.toString())) {
                    tv_Next.setTextColor(Color.parseColor("#999999"))
                } else {
                    tv_Next.setTextColor(Color.WHITE)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 ->
                OkClientHelper.get(this, "users/randomname/${national.area_code}", RandomNameData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as RandomNameData
                        if (result.code == 0) {
                            et_Name.setText(result.data.nick_name)
                        } else {
                            showToast(result.msg)
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            1 -> {//保存信息
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                formBody.add("nickName", et_Name.text.toString().trim())
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody.build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            val loginBean = PreferenceTools.getObj(this@SignNameActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                            if (TextUtils.isEmpty(SPUtils.getString(this@SignNameActivity, IConstant.USERCHARACTERTYPE + loginBean.user_id, ""))) {
                                startActivity(Intent(this@SignNameActivity, SelectCharacterTypeActivity::class.java))
                            } else {//已经选择之后直接跳转到录制声波
                                SPUtils.setBoolean(this@SignNameActivity, IConstant.WORLD_SHOW_TAB + loginBean.user_id, true)
                                LoginActivity.instance?.finish()
                                VerifyActivity.instance?.finish()
                                SignActivity.instance?.finish()
                                startActivity(Intent(this@SignNameActivity, SelectCharacterTypeActivity::class.java)
                                        .putExtra("isSign", true))
                            }
                            finish()
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (any is Exception) {

                        }
                    }
                })
            }
            5 -> {
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${obj.user_id}/yxtoken", YunxinTokenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {

                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}