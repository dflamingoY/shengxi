package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_msg_notify.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.NoticeData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.DubbingDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.TodayBestWorkActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.WordingVoiceActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import skin.support.SkinCompatManager

class MsgNotifyActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<NoticeData.NoticeBean>
    private var lastId: String? = ""
    private val mData by lazy {
        ArrayList<NoticeData.NoticeBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_msg_notify
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        transLayout.findViewById<View>(R.id.tv_msg_hint_1).isSelected = AppTools.getLanguage(this) == IConstant.HK || AppTools.getLanguage(this) == IConstant.TW
    }

    override fun initData() {
        adapter = object : QuickAdapter<NoticeData.NoticeBean>(this, R.layout.item_notice, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: NoticeData.NoticeBean?) {
                helper!!.getView(R.id.tv_skinColor).visibility = View.GONE
                helper.getView(R.id.ivAlarmType).visibility = View.GONE
                helper.getTextView(R.id.tv_UserName).isSelected = false
                helper.getView(R.id.ivAlarmType).isSelected = !TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)
                when (item!!.message_type) {
                    5 -> {//好友
                        if (item.friend_from == 2) {
                            helper.getTextView(R.id.tv_Type)?.text = "  " + item.tips.replace(item.nick_name, "")  //"   ${resources.getString(R.string.string_approved_friend)}"
                        } else {
                            helper.getTextView(R.id.tv_Type)?.text = "   ${resources.getString(R.string.string_approved_friend)}"
                        }
                        helper.getTextView(R.id.tv_UserName).isSelected = false
                        helper.getTextView(R.id.tv_UserName)?.text = item.nick_name
                        helper.getView(R.id.iv_Arrow)?.visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                    }
                    3 -> {//共鸣
                        if (item.is_anonymous == 1) {//匿名
                            helper.getTextView(R.id.tv_UserName).visibility = View.VISIBLE
                            helper.getView(R.id.tv_Type).visibility = View.GONE
                            helper.getTextView(R.id.tv_UserName).text = "一位声昔舍友" + resources.getString(R.string.string_received_admire)
                            helper.getTextView(R.id.tv_UserName).isSelected = true
                        } else {
                            helper.getView(R.id.tv_Type).visibility = View.VISIBLE
                            helper.getTextView(R.id.tv_UserName).visibility = View.VISIBLE
                            helper.getTextView(R.id.tv_Type)?.text = "   ${resources.getString(R.string.string_received_admire)}"
                            helper.getTextView(R.id.tv_UserName).isSelected = false
                            helper.getTextView(R.id.tv_UserName)?.text = item.nick_name
                        }
                        helper.getView(R.id.iv_Arrow)?.visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                    }
                    4 -> {//匿名表白
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                        helper.getTextView(R.id.tv_UserName).isSelected = true
                        helper.getTextView(R.id.tv_UserName).text = when (item.open_at) {
                            item.created_at.toLong() -> resources.getString(R.string.string_received_anonymous_1)
                            else -> resources.getString(R.string.string_received_anonymous)
                        }
                        helper.getTextView(R.id.tv_Type).text = ""
                        helper.getView(R.id.iv_Arrow).visibility = View.GONE
                        if (item.open_at > 0) {
                            helper.getView(R.id.tv_skinColor).visibility = View.VISIBLE
                            if (System.currentTimeMillis() / 1000 >= item.open_at) {
                                helper.getTextView(R.id.tv_skinColor).text = resources.getString(R.string.string_msg_notice_show_nickname)
                                helper.getTextView(R.id.tv_skinColor).isSelected = true
                            } else {
                                helper.getTextView(R.id.tv_skinColor).isSelected = false
                                try {
                                    helper.getTextView(R.id.tv_skinColor).text = String.format(resources.getString(R.string.string_msg_notice_days_nickname), TimeUtils.getInstance().IsToday(item.open_at.toInt()).toString())
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                    8 -> {
                        helper.getTextView(R.id.tv_UserName).isSelected = false
                        helper.getTextView(R.id.tv_UserName).text = item.tips
                        helper.getTextView(R.id.tv_Type).text = ""
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                    }
                    14 -> {//心情关注
                        helper.getTextView(R.id.tv_Type).text = item.tips.replace(item.nick_name, "   ")
                        helper.getTextView(R.id.tv_follow_relieve).text = String.format(resources.getString(R.string.string_fololow_relieve), TimeUtils.getInstance().IsToday(item.release_at.toInt()).toString())
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_UserName).isSelected = false
                    }
                    16 -> {//配音
                        helper.getTextView(R.id.tv_UserName).isSelected = false
                        helper.getTextView(R.id.tv_Type).text = "   ${item.tips.replace(item.nick_name, "")}"
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                    }
                    17 -> {//配音下载
                        helper.getTextView(R.id.tv_UserName).isSelected = false
                        helper.getTextView(R.id.tv_Type).text = "   ${item.tips.replace(item.nick_name, "")}"
                        helper.getTextView(R.id.tv_follow_relieve).text = ""
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                    }
                    18 -> {//绘画作品 喜欢 匿名喜欢
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_UserName).isSelected = item.like_type == 1
                        if (item.like_type == 1) {
                            helper.getTextView(R.id.tv_Type).text = ""
                            helper.getTextView(R.id.tv_UserName).text = item.tips
                        } else {
                            helper.getTextView(R.id.tv_Type).text = "   ${item.tips}".replace(item.nick_name, "").replace("悄悄", "")
                        }
                    }
                    19 -> {//新的涂鸦作品
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type)?.text = "   ${item.tips}".replace(item.nick_name, "")
                    }
                    20 -> {//涂鸦作品点赞
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type).text = "    ${item.tips}".replace(item.nick_name, "").replace("神", "").replace("天使", "").replace("魔鬼", "")
                        helper.getView(R.id.ivAlarmType).visibility = View.VISIBLE
                        helper.getImageView(R.id.ivAlarmType).setImageResource(when (item.vote_option) {
                            1 -> R.drawable.selector_hint_angel
                            else -> R.drawable.selector_hint_monster
                        })
                    }
                    21 -> {//台词点赞
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type)?.text = "    ${item.tips}".replace(item.nick_name, "").replace("神", "").replace("天使", "").replace("魔鬼", "")
                        helper.getView(R.id.ivAlarmType).visibility = View.VISIBLE
                        helper.getImageView(R.id.ivAlarmType).setImageResource(when (item.vote_option) {
                            1 -> R.drawable.selector_hint_angel
                            2 -> R.drawable.selector_hint_monster
                            3 -> R.drawable.selector_hint_god
                            else -> R.drawable.selector_hint_god
                        })
                    }
                    22 -> {//配音点赞
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type)?.text = "    ${item.tips}".replace(item.nick_name, "").replace("神", "").replace("天使", "").replace("魔鬼", "")
                        helper.getView(R.id.ivAlarmType).visibility = View.VISIBLE
                        helper.getImageView(R.id.ivAlarmType).setImageResource(when (item.vote_option) {
                            1 -> R.drawable.selector_hint_angel
                            2 -> R.drawable.selector_hint_monster
                            3 -> R.drawable.selector_hint_god
                            else -> R.drawable.selector_hint_god
                        })
                    }
                    23 -> {
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_UserName).isSelected = true
                        helper.getTextView(R.id.tv_Type).text = ""
                        helper.getTextView(R.id.tv_UserName).text = item.tips
                    }
                    25 -> {//绘画收藏
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type).text = " " + item.tips.replace(item.nick_name, "")
                    }
                    26 -> {//管理员置顶画
                        helper.getTextView(R.id.tv_UserName).isSelected = true
                        helper.getTextView(R.id.tv_Type).text = ""
                        helper.getTextView(R.id.tv_UserName).text = item.tips.replace(item.nick_name, "")
                    }
                    27 -> {//xx送了画
                        helper.getTextView(R.id.tv_UserName).text = item.nick_name
                        helper.getTextView(R.id.tv_Type).text = " " + item.tips.replace(item.nick_name, "").replace("~", "")
                    }
                    else -> {
                        helper.getTextView(R.id.tv_UserName).text = "有新的版本发布,请更新到最新版本"
                    }
                }
                helper.getView(R.id.tv_UserName)?.setOnClickListener {
                    if (item.message_type == 3 || item.message_type == 5 || item.message_type == 14 || item.message_type == 16 || item.message_type == 17
                            || item.message_type == 18 || item.message_type == 19 || item.message_type == 20 || item.message_type == 21 || item.message_type == 22 || item.message_type == 27
                            || item.message_type == 25
                    ) {
                        if (!TextUtils.isEmpty(item.from_user_id) && "0" != item.from_user_id && item.is_anonymous != 1) {
                            startActivity(Intent(this@MsgNotifyActivity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                        }
                    }
                }
                helper.getTextView(R.id.tv_Time)?.text = TimeUtils.getInstance().paserFriends(this@MsgNotifyActivity, item.created_at)
                helper.getTextView(R.id.tv_skinColor).setOnClickListener {
                    if (it.isSelected) {
                        if (helper.getTextView(R.id.tv_skinColor).text == resources.getString(R.string.string_msg_notice_show_nickname))
                            helper.getTextView(R.id.tv_skinColor).text = item.nick_name
                        else
                            startActivity(Intent(this@MsgNotifyActivity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                    }
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        adapter.setOnLoadListener {
            try {
                lastId = mData[mData.size - 1].id
                request(0)
            } catch (e: Exception) {

            }
        }
        adapter.setOnItemClickListener { _, position ->
            when (mData[position].message_type) {
                5 -> {
                    startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", mData[position].from_user_id))
                }
                3 -> {
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    startActivity(Intent(this, DynamicDetailsActivity::class.java)
                            .putExtra("uid", loginBean.user_id)
                            .putExtra("id", mData[position].voice_id))
                }
                8 -> {
                    startActivity(Intent(this, ActionActivity::class.java)
                            .putExtra("isHtml", true)
                            .putExtra("isScroll", true)
                            .putExtra("url", mData[position].html_id))
                }
                14 -> {
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    startActivity(Intent(this, DynamicDetailsActivity::class.java)
                            .putExtra("uid", loginBean.user_id)
                            .putExtra("id", mData[position].voice_id))
                }
                16 -> {
                    startActivity(Intent(this, WordingVoiceActivity::class.java).putExtra("id", mData[position].line_id))
                }
                17 -> {//配音详情
                    startActivity(Intent(this, DubbingDetailsActivity::class.java).putExtra("id", mData[position].dubbing_id))
                }
                18 -> {//绘画作品点赞
                    startActivity<CanvasDetailsActivity>("id" to mData[position].artwork_id, "type" to "1")
                }
                19 -> {//新的涂鸦作品
                    startActivity<CanvasDetailsActivity>("id" to mData[position].artwork_id, "type" to "1")
                }
                20 -> {//涂鸦的点赞
                    startActivity<CanvasDetailsActivity>("id" to mData[position].artwork_id, "type" to "1")
                }
                21 -> {//台词
                    startActivity<AlarmResponseActivity>("id" to mData[position].line_id, "type" to "1")
                }
                22 -> {//配音点赞
                    startActivity<AlarmResponseActivity>("id" to mData[position].dubbing_id, "type" to "2")
                }
                23 -> {
                    startActivity<TodayBestWorkActivity>("id" to mData[position].dubbing_id, "type" to 2)
                }
                25 -> {//收藏
                    startActivity<CanvasDetailsActivity>("id" to mData[position].artwork_id, "type" to "3")
                }
                26 -> {//置顶画
                    startActivity<PaintTopActivity>("id" to mData[position].artwork_id)
                }
                27 -> {
                    startActivity<CanvasDetailsActivity>("id" to mData[position].artwork_id, "type" to "2")
                }
            }
        }
        transLayout.findViewById<View>(R.id.tv_Record).setOnClickListener {
            startActivity(Intent(this, SendAct::class.java).putExtra("type", 1))
            overridePendingTransition(R.anim.operate_enter, 0)
        }
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "messages/${loginBean?.user_id}/3?lastId=$lastId", NoticeData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NoticeData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (bean in (result).data) {
                            mData.add(bean)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    result.data?.let {
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                transLayout.showOffline()
            }
        })
    }
}