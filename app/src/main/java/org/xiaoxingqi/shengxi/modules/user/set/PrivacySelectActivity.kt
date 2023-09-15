package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_select.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

/**
 * 隐私设置
 */
class PrivacySelectActivity : BaseAct() {
    private lateinit var key: String
    private lateinit var value: String
    private var infoData: UserInfoData? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {

    }

    override fun initData() {
        infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        if (intent.getBooleanExtra("isComment", false)) {
            key = "chatWith"
            tv_Content.text = resources.getString(R.string.string_PrivacyAct_6)
            iv_hint_drawable.setImageResource(R.drawable.drawable_privacy_echo)
            tv_hint_2.text = resources.getString(R.string.string_privacy_11)
        } else {
            key = "chatPriWith"
            iv_hint_drawable.setImageResource(R.drawable.drawable_privacy_chat)
            tv_Content.text = resources.getString(R.string.string_PrivacyAct_7)
            tv_hint_2.text = resources.getString(R.string.string_privacy_12)
        }

        val privacy = intent.getIntExtra("privacy", 0)
        if (0 == privacy) {
            viewAll.isSelected = true
            value = "0"
        } else {
            viewFriend.changeStatus(true)
            viewAll.changeStatus(false)
            value = "1"
        }
    }

    override fun initEvent() {
        viewAll.setOnClickListener {
            if (value == "0") {
                return@setOnClickListener
            }
            viewFriend.changeStatus(false)
            viewAll.changeStatus(true)
            value = "0"
            request(0)
        }
        viewFriend.setOnClickListener {
            if (value == "1")
                return@setOnClickListener
            viewFriend.changeStatus(true)
            viewAll.changeStatus(false)
            value = "1"
            request(0)
        }
        btn_Back.setOnClickListener { finish() }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add(key, value)
                .build()
        OkClientHelper.patch(this, "users/${infoData?.data?.user_id}/setting", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {


                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

}