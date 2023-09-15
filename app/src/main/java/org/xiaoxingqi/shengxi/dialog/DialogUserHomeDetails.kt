package org.xiaoxingqi.shengxi.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_user_home_details.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.model.PrivacyTopicData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.UserTopicManagerActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.set.TopicManagerActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils

class DialogUserHomeDetails(context: Context) : BaseDialog(context) {
    private var mContext = context
    private var userBean: UserInfoData.UserBean? = null
    private var glideUtil: GlideJudeUtils? = null
    private var topic: PrivacyTopicData? = null
    private var permissionDenial = false
    private var achieveStatus: Int = 0// 0 未初始化 1 展示 2 不展示
    private var recordDays = 0

    override fun getLayoutId(): Int {
        return R.layout.dialog_user_home_details
    }

    override fun initView() {
        userBean?.let {
            iv_user_type.visibility = if (it.identity_type == 0) View.GONE else View.VISIBLE
            iv_user_type.isSelected = it.identity_type == 1
            var url = it.avatar_url
            if (it.avatar_url?.contains("?")!!) {
                url = it.avatar_url?.substring(0, it.avatar_url?.lastIndexOf("?")!!)
            }
            glideUtil?.loadGlide(url, iv_img, R.mipmap.icon_user_default, glideUtil?.getLastModified(url))
            tv_UserName.text = it.nick_name
            tv_frequency_id.text = context.resources.getString(R.string.string_frequency) + it.frequency_no
            if (it.friend_num == 0 && it.voice_total_len == 0) {
                tv_UserSummary.text = "${context.resources.getString(R.string.string_empty_friends)} ${context.resources.getString(R.string.string_empty_voices)}"
            } else if (it.friend_num > 0 && it.voice_total_len == 0) {
                tv_UserSummary.text = Html.fromHtml(String.format(context.resources.getString(R.string.string_only_friend_summary), it.friend_num))
            } else if (it.friend_num == 0 && it.voice_total_len > 0) {
                tv_UserSummary.text = Html.fromHtml(String.format(context.resources.getString(R.string.string_only_voice_summary), TimeUtils.formatterS(context, it.voice_total_len)))
            } else {
                tv_UserSummary.text = Html.fromHtml(String.format(context.resources.getString(R.string.string_user_summary), it.friend_num, TimeUtils.formatterS(context, it.voice_total_len)))
            }
            iv_img.setOnClickListener {
                mContext.startActivity(Intent(mContext, ShowPicActivity::class.java).putExtra("path", url))
                (mContext as Activity).overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
            ivOfficial.visibility = if (it.user_id == "1") View.VISIBLE else View.GONE
        }
        tvEmptyTopic.setOnClickListener {
            if (!it.isSelected) {
                context.startActivity<TopicManagerActivity>()
                dismiss()
            }
        }
        if (achieveStatus == 2) {
            linearAchievement.visibility = View.GONE
        }
        tvRecordDays.text = "连续记录${recordDays}天 "
        linearContainer.removeAllViews()
        topic?.let {
            if (it.data != null) {
                tvEmptyTopic.visibility = View.GONE
                it.data.forEach { bean ->
                    val view = View.inflate(context, R.layout.item_topic_text, null) as LinearLayout
                    view.gravity = Gravity.CENTER_HORIZONTAL
                    view.findViewById<TextView>(R.id.text_inf).text = "#${bean.topic_name}#"
                    linearContainer.addView(view)
                    view.setOnClickListener {
                        context.startActivity(Intent(context, UserTopicManagerActivity::class.java)
                                .putExtra("title", userBean?.nick_name)
                                .putExtra("tag", bean.topic_name)
                                .putExtra("uid", userBean?.user_id)
                                .putExtra("tagId", bean.topic_id))
                    }
                }
            } else {
                if (PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id != userBean?.user_id) {
                    tvEmptyTopic.text = context.resources.getString(R.string.string_about_4)
                    tvEmptyTopic.isSelected = true
                }
            }
        }
        if (topic == null) {
            if (permissionDenial) {
                tvEmptyTopic.text = context.resources.getString(R.string.string_about_3)
                tvEmptyTopic.isSelected = true
            }
        }
        fillWidth()
    }

    fun setData(userBean: UserInfoData.UserBean, glideUtil: GlideJudeUtils): DialogUserHomeDetails {
        this.userBean = userBean
        this.glideUtil = glideUtil
        return this
    }

    fun setTopic(topic: PrivacyTopicData?): DialogUserHomeDetails {
        this.topic = topic
        return this
    }

    fun setPermissionDenial(permissionDenial: Boolean): DialogUserHomeDetails {
        this.permissionDenial = permissionDenial
        return this
    }

    fun setAchieveInfo(showStatus: Int, recordDays: Int): DialogUserHomeDetails {
        this.achieveStatus = showStatus
        this.recordDays = recordDays
        return this
    }

}