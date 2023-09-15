package org.xiaoxingqi.shengxi.modules

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import java.io.File

/*
 * 展示播放条的数据信息
 */
/**
 * @ function 是否显示专有内容
 */
@SuppressLint("SetTextI18n")
fun BaseBean.show(helper: BaseAdapterHelper, context: Context, glideUtil: GlideJudeUtils, function: () -> Boolean) {
    glideUtil.loadGlide(user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(user.avatar_url))
    helper.getTextView(R.id.tv_UserName)?.text = user.nick_name
    helper.getView(R.id.iv_user_type).visibility = if (user.identity_type == 0) View.GONE else View.VISIBLE
    helper.getView(R.id.iv_user_type).isSelected = user.identity_type == 1
    helper.getTextView(R.id.tv_Sub).text = if (played_num == 0) context.resources.getString(R.string.string_Listener) else context.resources.getString(R.string.string_Listener) + " $played_num"
    helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(topic_name)) "" else "#${topic_name}#"
    helper.getTextView(R.id.tv_Action).visibility = if (TextUtils.isEmpty(topic_name)) View.GONE else View.VISIBLE
    try {
        helper.getView(R.id.ivOfficial).visibility = if (user_id == 1) View.VISIBLE else View.GONE
    } catch (e: Exception) {
    }
    val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
    voiceProgress.data = this
    if (function()) {
        if (resource == null) {
            helper.getView(R.id.itemDynamic).visibility = View.GONE
        }
        resource?.let {
            if (!TextUtils.isEmpty(it.id)) {
                helper.getView(R.id.itemDynamic).visibility = View.VISIBLE
                (helper.getView(R.id.itemDynamic) as ItemDynamicView).setData(it, resource_type, user_score)
            } else {
                helper.getView(R.id.itemDynamic).visibility = View.GONE
            }
        }
        (helper.getView(R.id.imageGroup) as ImageGroupView).setData(img_list)
    } else {
        helper.getView(R.id.itemDynamic).visibility = View.GONE
        helper.getView(R.id.imageGroup).visibility = View.GONE
    }
    if (!TextUtils.isEmpty(is_shared)) {//是自己
        helper.getImageView(R.id.iv_Thumb).isSelected = is_shared == "1"
        helper.getView(R.id.tv_Sub).visibility = View.VISIBLE
        if (is_shared == "1") {
            helper.getTextView(R.id.tv_Echo).text = context.resources.getString(R.string.string_unshare_world)
        } else {
            helper.getTextView(R.id.tv_Echo).text = context.resources.getString(R.string.string_share_world)
        }
        helper.getTextView(R.id.tv_Recommend).text = context.resources.getString(R.string.string_echoing) + if (chat_num <= 0) {
            ""
        } else {
            " $chat_num"
        }
    } else {
        helper.getView(R.id.tv_Sub).visibility = View.GONE
        helper.getTextView(R.id.tv_Echo)?.text = context.resources.getString(R.string.string_gongming)
        if (dialog_num <= 0) {
            helper.getTextView(R.id.tv_Recommend).text = context.resources.getString(R.string.string_echoing)
        } else {
            helper.getTextView(R.id.tv_Recommend).text = "${context.resources.getString(R.string.string_Talks)} " + dialog_num
        }
    }
    try {
        if (!intersect_tags.isNullOrEmpty()) {
            helper.getView(R.id.linearInterested).visibility = View.VISIBLE
            when (user_gender) {
                1 -> helper.getView(R.id.linearInterested).isSelected = true
                2 -> helper.getView(R.id.linearInterested).isSelected = false
                else -> {
                    helper.getView(R.id.linearInterested).visibility = View.GONE
                }
            }
            helper.getTextView(R.id.tvInterestedTitle).text = String.format(context.resources.getString(R.string.string_49), intersect_tags.size, intersect_tags.joinToString("、"))
        } else {
            helper.getView(R.id.linearInterested).visibility = View.GONE
        }
    } catch (e: Exception) {
    }

    helper.getTextView(R.id.tv_Action).setOnClickListener {
        context.startActivity(Intent(context, TopicResultActivity::class.java)
                .putExtra("tag", topic_name)
                .putExtra("tagId", topic_id.toString())
        )
    }
//    helper.getView(R.id.cardView).setOnClickListener {
//        context.startActivity(Intent(context, UserDetailsActivity::class.java).putExtra("url", user.avatar_url).putExtra("id", user.id))
//    }
}

fun Activity.showSubscriber(bean: BaseBean, function: () -> Unit) {
    DialogGraffiti(this).setTitle(String.format(resources.getString(R.string.string_follow_hint), when (bean.resource_type) {
        1 -> resources.getString(R.string.string_follow_movies)
        2 -> resources.getString(R.string.string_follow_book)
        3 -> resources.getString(R.string.string_follow_song)
        else -> resources.getString(R.string.string_follow_movies)
    }), resources.getString(R.string.string_follow_action)).setOnClickListener(View.OnClickListener {
        function()
    }).show()
}

fun Activity.deleteSubscriber(bean: BaseBean, function: () -> Unit) {
    DialogGraffiti(this).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (bean.resource_type) {
        1 -> resources.getString(R.string.string_follow_movies)
        2 -> resources.getString(R.string.string_follow_book)
        3 -> resources.getString(R.string.string_follow_song)
        else -> resources.getString(R.string.string_follow_movies)
    }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
        function()
    }).show()
}

/**
 * @param function Int 1为删除 2 设置为隐私操作
 */
fun Activity.share(item: BaseBean, function: (Int) -> Unit) {
    DialogMore(this).setEditable(item.resource_id == "0").setAdmin(item.user_id.toString(), item.isTop).setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
        when (it.id) {
            R.id.tv_ShareWechat -> {
                DialogShare(this, true).setOnClickListener(View.OnClickListener { share ->
                    when (share.id) {
                        R.id.linearWechat -> {
                            ShareUtils.share(this, Wechat.NAME, item.voice_url, item.share_url, "", null)
                        }
                        R.id.linearMoment -> {
                            ShareUtils.share(this, WechatMoments.NAME, item.voice_url, item.share_url, "", null)
                        }
                    }
                }).show()
            }
            R.id.tv_ShareWeibo -> {
                ShareUtils.share(this, SinaWeibo.NAME, item.voice_url, item.share_url, "", null)
            }
            R.id.tv_ShareQQ -> {
                DialogShare(this, false).setOnClickListener(View.OnClickListener { share ->
                    when (share.id) {
                        R.id.linearQQ -> {
                            ShareUtils.share(this, QQ.NAME, item.voice_url, item.share_url, "", null)
                        }
                        R.id.linearQzone -> {
                            ShareUtils.share(this, QZone.NAME, item.voice_url, item.share_url, "", null)
                        }
                    }
                }).show()
            }
            R.id.tv_Delete -> {
                DialogDeleteConment(this).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                    function(1)
                }).show()
            }
            R.id.tv_Self -> {//切换是否是自己可见
                DialogPrivacy(this).setOnClickListener(View.OnClickListener {
                    function(2)
                }).show()
            }
            R.id.tv_add_album -> {
                startActivity(Intent(this, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
            }
            R.id.tvOfficialTop -> {
                function(4)
            }
            R.id.tvReEditVoice -> {
                function(5)
            }
        }
    }).show()
}

/**
 * 删除
 */
fun Activity.delete(bean: BaseBean, transLayout: TransLayout? = null, function: (BaseBean) -> Unit) {
    OkClientHelper.delete(this, "users/${bean.user_id}/voices/${bean.voice_id}", FormBody.Builder()
            .build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            if ((result as BaseRepData).code == 0) {
                function(bean)
            } else {
                toast(result.msg)
            }
            transLayout?.showContent()
        }

        override fun onFailure(any: Any?) {
            transLayout?.showContent()
            toast("请稍候重试")
        }
    })
}

fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

/**
 *  通用举报
 */
fun Activity.reportNormal(function: (String) -> Unit) {
    DialogNormalReport(this).setOnClickListener(View.OnClickListener { report ->
        function(when (report.id) {
            R.id.tv_Attach -> "1"
            R.id.tv_Porn -> "2"
            R.id.tv_Junk -> "3"
            R.id.tv_illegal -> "4"
            else -> "1"
        })
    }).show()
}

fun Activity.about() {
    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
    OkClientHelper.get(this, "users/${loginBean.user_id}/about", UserInfoData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            result as UserInfoData
            if (result.code == 0) {
                SPUtils.setInt(this@about, IConstant.TOTALLENGTH + result.data.user_id, result.data.voice_total_len)
            }
        }

        override fun onFailure(any: Any?) {

        }
    })
}

fun getDownFilePath(path: String): File {
    return File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getSuffix(path)).let {
        if (it.exists())
            it
        else {//兼容4.2.2 之前的文件后缀未处理的情况
            File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getCompatibleSuffix(path)).let { compatible ->
                if (compatible.exists()) {
                    compatible
                } else
                    it
            }
        }
    }
}


/*
 *删除心愿单
 */
fun Activity.removeWish(subscribeId: String, block: (BaseRepData?) -> Unit) {
    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
    OkClientHelper.delete(this, "users/${loginBean.user_id}/resourceSubscription/$subscribeId", FormBody.Builder().build()
            , BaseRepData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            block(null)
        }

        override fun success(result: Any?) {
            block(result as BaseRepData)
        }
    }, "V4.2"
    )
}

/*
添加到心愿单
 */
fun Activity.addWish(resourceType: Int, resourceId: String, wishType: Int, block: (IntegerRespData?) -> Unit) {
    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
    OkClientHelper.post(this, "users/${loginBean.user_id}/resourceSubscription", FormBody.Builder()
            .add("resourceType", "$resourceType")
            .add("resourceId", resourceId)
            .add("subscriptionType", "$wishType")
            .build()
            , IntegerRespData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            block(null)
        }

        override fun success(result: Any?) {
            block(result as IntegerRespData)
        }
    }, "V4.2"
    )
}

fun Activity.saveSetting(userId: String, name: String, value: Int, settingTag: String = "moodbook", block: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, "users/$userId/settings", FormBody.Builder()
            .add("settingValue", "$value")
            .add("settingTag", settingTag)
            .add("settingName", name).build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            block(result as BaseRepData)
        }

        override fun onFailure(any: Any?) {
            block(null)
        }
    }, "V4.2")
}

fun Activity.getSubscribe(type: Int, lastId: String?, block: (SearchResData?) -> Unit) {
    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
    OkClientHelper.get(this, "users/${loginBean.user_id}/resourceSubscription?resourceType=$type&lastId=$lastId", SearchResData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            result as SearchResData
            block(result)
        }

        override fun onFailure(any: Any?) {

        }
    }, "V4.2")
}

fun Activity.againAttention(logId: String, timestamp: Int, block: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, "userSubscription/$logId", FormBody.Builder()
            .add("renewedAt", "$timestamp")
            .build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            block(result as BaseRepData)
        }

        override fun onFailure(any: Any?) {
            block(null)
        }
    }, "V4.2")
}

/**
 * 好友续期
 */
fun Activity.friendsRenew(toUserId: String, renew: Int, block: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, "friends/$toUserId", FormBody.Builder().add("renewedAt", "$renew").build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            block(result as BaseRepData)
        }

        override fun onFailure(any: Any?) {
            block(null)
        }
    }, "V4.2")
}

//滚动到指定位置
fun RecyclerView.scroll(context: Context?, position: Int, isAnimated: Boolean = true) {
    if (isAnimated) {
        smoothScrollToPosition(position)
    } else {
        val scroller = TopSmoothScroller(context)
        scroller.targetPosition = position
        this.layoutManager?.startSmoothScroll(scroller)
    }
}

/**
 * 资源分享到第三方平台
 */
fun Activity.shareDialog(item: BaseBean, function: (Int) -> Unit) {
    DialogMore(this).setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
        when (it.id) {
            R.id.tv_ShareWechat -> {
                DialogShare(this, true).setOnClickListener(View.OnClickListener { share ->
                    when (share.id) {
                        R.id.linearWechat -> {
                            ShareUtils.share(this, Wechat.NAME, item.voice_url, item.share_url, "", item.user)
                        }
                        R.id.linearMoment -> {
                            ShareUtils.share(this, WechatMoments.NAME, item.voice_url, item.share_url, "", item.user)
                        }
                    }
                }).show()
            }
            R.id.tv_ShareWeibo -> {
                ShareUtils.share(this, SinaWeibo.NAME, item.voice_url, item.share_url, "", item.user)
            }
            R.id.tv_ShareQQ -> {
                DialogShare(this, false).setOnClickListener(View.OnClickListener { share ->
                    when (share.id) {
                        R.id.linearQQ -> {
                            ShareUtils.share(this, QQ.NAME, item.voice_url, item.share_url, "", item.user)
                        }
                        R.id.linearQzone -> {
                            ShareUtils.share(this, QZone.NAME, item.voice_url, item.share_url, "", item.user)
                        }
                    }
                }).show()
            }
            R.id.tv_add_album -> {
                startActivity(Intent(this, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
            }
            else -> {
                function(it.id)
            }
        }
    }).show()
}

/**
 * 书影音列表item的数据绑定
 */
@SuppressLint("SetTextI18n")
fun BaseBean.resourceContent(helper: BaseAdapterHelper, context: Context, glideUtil: GlideJudeUtils, defaultType: Int) {
    try {
        helper.getView(R.id.imageGroup).visibility = View.GONE
        helper.getView(R.id.itemDynamic).visibility = View.VISIBLE
        resource?.let {
            (helper.getView(R.id.itemDynamic) as ItemDynamicView).setData(it, resource_type, user_score)
        }
        glideUtil.loadGlide(user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(user.avatar_url))
        helper.getTextView(R.id.tv_UserName).text = user?.nick_name
        helper.getTextView(R.id.tvTime)?.text = TimeUtils.getInstance().paserFriends(context, created_at)
        helper.getView(R.id.iv_user_type).visibility = if (user.identity_type == 0) View.GONE else View.VISIBLE
        helper.getView(R.id.iv_user_type).isSelected = user.identity_type == 1
        helper.getView(R.id.ivOfficial).visibility = if (user_id == 1) View.VISIBLE else View.GONE
        (helper.getView(R.id.heartView) as HeartWaveView).attachBean(this)
        try {
            if (!intersect_tags.isNullOrEmpty()) {
                helper.getView(R.id.linearInterested).visibility = View.VISIBLE
                when (user_gender) {
                    1 -> helper.getView(R.id.linearInterested).isSelected = true
                    2 -> helper.getView(R.id.linearInterested).isSelected = false
                    else -> {
                        helper.getView(R.id.linearInterested).visibility = View.GONE
                    }
                }
                helper.getTextView(R.id.tvInterestedTitle).text = String.format(context.resources.getString(R.string.string_49), intersect_tags.size, intersect_tags.joinToString("、"))
            } else {
                helper.getView(R.id.linearInterested).visibility = View.GONE
            }
        } catch (e: Exception) {
        }
/*
        if (may_interested == 1) {
            helper.getView(R.id.tv_Interested).visibility = View.VISIBLE
            when (user_gender) {
                1 -> helper.getView(R.id.tv_Interested).isSelected = true
                2 -> helper.getView(R.id.tv_Interested).isSelected = false
                else -> {
                    helper.getView(R.id.tv_Interested).visibility = View.GONE
                }
            }
        } else {
            helper.getView(R.id.tv_Interested).visibility = View.GONE
        }*/
        try {
            helper.getView(R.id.ivOfficial).visibility = if (user_id == 1) View.VISIBLE else View.GONE
        } catch (e: Exception) {
        }
        if (!TextUtils.isEmpty(is_shared)) {
            helper.getView(R.id.tv_push).visibility = View.GONE
            helper.getView(R.id.heartView).visibility = View.GONE
            helper.getView(R.id.iv_Thumb).visibility = View.VISIBLE
            helper.getImageView(R.id.iv_Thumb).isSelected = is_shared == "1"
            helper.getView(R.id.tv_Sub).visibility = if (is_private == 1) View.GONE else View.VISIBLE
            helper.getView(R.id.iv_Privacy).visibility = if (is_private == 1) View.VISIBLE else View.GONE
            val params = helper.getView(R.id.iv_Privacy).layoutParams
            params.width = AppTools.dp2px(context, 27)
            params.height = AppTools.dp2px(context, 27)
            helper.getView(R.id.iv_Privacy).layoutParams = params
            helper.getTextView(R.id.tv_Sub).text = if (played_num == 0) context.resources.getString(R.string.string_Listener) else "${context.resources.getString(R.string.string_Listener)} $played_num"
            if (is_shared == "1") {
                helper.getTextView(R.id.tv_Echo)?.text = context.resources.getString(R.string.string_unshare_world)
            } else {
                helper.getTextView(R.id.tv_Echo)?.text = context.resources.getString(R.string.string_share_world)
            }
            helper.getTextView(R.id.tv_Recommend)?.text = context.resources.getString(R.string.string_echoing) + if (chat_num == 0) {
                ""
            } else {
                " $chat_num"
            }
        } else {
            helper.getView(R.id.heartView).visibility = View.VISIBLE
            helper.getView(R.id.iv_Thumb).visibility = View.GONE
            helper.getView(R.id.tv_push).visibility = if (defaultType == 0) View.VISIBLE else View.GONE
            helper.getTextView(R.id.tv_push).text = if (subscription_id != 0) context.resources.getString(R.string.string_followed) else context.resources.getString(R.string.string_follow_action)
            helper.getTextView(R.id.tv_push).isSelected = subscription_id != 0
            helper.getView(R.id.iv_Privacy).visibility = View.GONE
            helper.getView(R.id.tv_Sub).visibility = View.GONE
            helper.getTextView(R.id.tv_Echo)?.text = context.resources.getString(R.string.string_gongming)
            if (dialog_num == 0) {
                helper.getTextView(R.id.tv_Recommend)?.text = context.resources.getString(R.string.string_echoing)
            } else {
                helper.getTextView(R.id.tv_Recommend)?.text = context.resources.getString(R.string.string_Talks) + dialog_num
            }
        }
    } catch (e: Exception) {
    }
}

fun BaseBean.echoClick(context: Context, helper: BaseAdapterHelper, function: () -> Unit) {
    helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
        if (!TextUtils.isEmpty(is_shared)) {
            context.startActivity(Intent(context, DynamicDetailsActivity::class.java)
                    .putExtra("id", voice_id.toString())
                    .putExtra("uid", user.id)
                    .putExtra("isExpend", chat_num > 0)
            )
        } else {
            if (dialog_num == 0) {
                function()
            } else {
                context.startActivity(Intent(context, TalkListActivity::class.java)
                        .putExtra("voice_id", voice_id)
                        .putExtra("chat_id", chat_id)
                        .putExtra("uid", user_id.toString()))
            }
        }
    }
}

/*
当前item 是否在屏幕中
 */
fun RecyclerView.isOffsetScreen(current: Int): Boolean {
    /*return if (layoutManager is LinearLayoutManager) {
        val first = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val last = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        current in first..last *//*&& scrollState == RecyclerView.SCROLL_STATE_IDLE*//*
    } else false*/
    return false
}

