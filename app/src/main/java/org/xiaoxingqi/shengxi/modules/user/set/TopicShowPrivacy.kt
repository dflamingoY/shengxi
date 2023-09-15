package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_select.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class TopicShowPrivacy : BaseAct() {
    private var value = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {
        btn_Back.setOnClickListener { finish() }
        tvTitle.text = resources.getString(R.string.string_privacy_topic_setting)
        tv_Content.text = resources.getString(R.string.string_privacy_topic_setting_3)
        viewAll.setTitle(resources.getString(R.string.string_show))
        viewFriend.setTitle(resources.getString(R.string.string_unshow))
        tv_hint_2.text = resources.getString(R.string.string_privacy_topic_setting_1)
        iv_hint_drawable.setImageResource(R.drawable.draw_privavcy_topic_show)
    }

    override fun initData() {
        viewAll.isSelected = true
        when (intent.getIntExtra("privacy", 0)) {
            1 -> {
                viewAll.changeStatus(true)
                viewFriend.changeStatus(false)
                value = 1
            }
            0 -> {
                viewAll.changeStatus(false)
                viewFriend.changeStatus(true)
                value = 0
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewAll.setOnClickListener {
            if (value == 1)
                return@setOnClickListener
            viewAll.changeStatus(true)
            viewFriend.changeStatus(false)
            value = 1
            request(1)
        }
        viewFriend.setOnClickListener {
            if (value == 0)
                return@setOnClickListener
            viewAll.changeStatus(false)
            viewFriend.changeStatus(true)
            value = 0
            request(0)
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val infoData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("displaySameTopic", flag.toString())
                .build()
        OkClientHelper.patch(this, "users/${infoData.user_id}/setting", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {

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