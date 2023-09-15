package org.xiaoxingqi.shengxi.modules.login

import android.content.Intent
import kotlinx.android.synthetic.main.activity_select_character.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import java.lang.Deprecated

/**
 * 用户设置初始化的性格类型
 */
@Deprecated
class SelectCharacterTypeActivity : BaseLoginAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_select_character
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initEvent() {
        tv_Introvert.setOnClickListener {
            try {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                SPUtils.setString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, IConstant.USER_INTROVERT)
            } catch (e: Exception) {
            }
            request(0)
        }
        tv_Extrovert.setOnClickListener {
            try {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                SPUtils.setString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, IConstant.USER_EXTROVERT)
            } catch (e: Exception) {
            }
            request(1)
        }
    }

    override fun request(flag: Int) {
        /**
         * 更新用户的ie类型
         */
        transLayout.showProgress()

        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val userId = (if (loginBean != null) {
            loginBean.user_id
        } else {
            try {
                val info = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                info.data.user_id
            } catch (e: Exception) {
                null
            }
        }) ?: return
        OkClientHelper.patch(this, "users/${userId}", FormBody.Builder().add("interfaceType", if (flag == 0) IConstant.USER_INTROVERT else IConstant.USER_EXTROVERT).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SPUtils.setBoolean(this@SelectCharacterTypeActivity, IConstant.WORLD_SHOW_TAB + userId, true)
                    SPUtils.setBoolean(this@SelectCharacterTypeActivity, IConstant.HOME_WORLD_TITLE_RED_POINT + userId, true)
                    if (!intent.getBooleanExtra("illegalRecord", false))
                        startActivity(Intent(this@SelectCharacterTypeActivity, SignRecordActivity::class.java)
                                .putExtra("isCanBack", intent.getBooleanExtra("isCanBack", false))
                                .putExtra("name", intent.getStringExtra("name"))
                                .putExtra("uid", intent.getStringExtra("uid")))
                    finish()
                } else {
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun onBackPressed() {

    }
}