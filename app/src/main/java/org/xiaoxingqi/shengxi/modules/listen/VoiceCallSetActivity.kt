package org.xiaoxingqi.shengxi.modules.listen

import android.app.Activity
import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.activity_voice_call_set.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.MatchSettingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class VoiceCallSetActivity : BaseAct() {
    companion object {
        private const val REQUEST_EDIT_FRIEND = 0x00
    }

    private var type = 1
    private var firstUserId: String = "0"

    override fun getLayoutId(): Int {
        return R.layout.activity_voice_call_set
    }

    override fun initView() {

    }

    override fun initData() {
        type = intent.getIntExtra("type", 1)
        tv_Title.text = if (type == 1) {
            resources.getString(R.string.string_voice_set_1)
        } else {
            resources.getString(R.string.string_voice_set_8)
        }
        request(1)
        toggle_Button.setEnableEffect(false)
    }

    override fun initEvent() {
        tv_choose_friend.setOnClickListener {
            startActivityForResult(Intent(this, SelectedFriendActivity::class.java).putExtra("id", firstUserId), REQUEST_EDIT_FRIEND)
        }
        btn_Back.setOnClickListener { finish() }
    }

    private fun delayLoading() {
        toggle_Button.setOnCheckedChangeListener { _, isChecked ->
            update(FormBody.Builder()
                    .add("firstUserId", firstUserId)
                    .add("matchType", type.toString())
                    .add("matchNew", if (isChecked) "1" else "0").build())
        }
        toggle_Button.setEnableEffect(true)
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/matchSetting?matchType=$type", MatchSettingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as MatchSettingData
                if (result.code == 0) {
                    toggle_Button.isChecked = result.data.match_new == 1
                    if ("0" != result.data.first_user_id) {
                        tv_Hint.visibility = View.GONE
                        linear_user.visibility = View.VISIBLE
                        glideUtil.loadGlide(result.data.first_avatar_url, iv_Avatar, 0, glideUtil.getLastModified(result.data.first_avatar_url))
                        tv_Name.text = result.data.first_nick_name
                    }
                    firstUserId = result.data.first_user_id
                }
                transLayout.showContent()
                delayLoading()
            }

            override fun onFailure(any: Any?) {
                delayLoading()
                transLayout.showContent()
            }
        }, "V3.3")
    }

    private fun update(formBody: FormBody) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/matchSetting", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.3")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_EDIT_FRIEND) {
                tv_Hint.visibility = View.GONE
                linear_user.visibility = View.VISIBLE
                val userBean = data?.getParcelableExtra<BaseUserBean>("data")
                glideUtil.loadGlide(userBean?.avatar_url, iv_Avatar, 0, glideUtil.getLastModified(userBean?.avatar_url))
                tv_Name.text = userBean?.nick_name
                firstUserId = userBean?.user_id!!
                update(FormBody.Builder()
                        .add("matchNew", if (toggle_Button.isChecked) "1" else "0")
                        .add("matchType", type.toString())
                        .add("firstUserId", userBean?.user_id).build())
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (null == data) {//取消选中
                if ("0" != firstUserId) {
                    update(FormBody.Builder()
                            .add("matchNew", if (toggle_Button.isChecked) "1" else "0")
                            .add("matchType", type.toString())
                            .add("firstUserId", "0").build())
                }
                tv_Hint.visibility = View.VISIBLE
                linear_user.visibility = View.GONE
                firstUserId = "0"
            }
        }
    }
}