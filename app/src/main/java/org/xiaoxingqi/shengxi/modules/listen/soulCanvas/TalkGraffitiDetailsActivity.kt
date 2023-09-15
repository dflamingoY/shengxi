package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_talk_graffiti_details.*
import kotlinx.android.synthetic.main.activity_talk_graffiti_details.btn_Back
import kotlinx.android.synthetic.main.activity_talk_graffiti_details.swipeRefresh
import kotlinx.android.synthetic.main.activity_talk_graffiti_details.transLayout
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelMsg
import org.xiaoxingqi.shengxi.dialog.DialogNormalReport
import org.xiaoxingqi.shengxi.dialog.DialogReport
import org.xiaoxingqi.shengxi.dialog.DialogReportSuccess
import org.xiaoxingqi.shengxi.impl.OnArtNewMsgEvent
import org.xiaoxingqi.shengxi.impl.OnDeleteMsg
import org.xiaoxingqi.shengxi.impl.SendMsgEvent
import org.xiaoxingqi.shengxi.model.AdminReportGraffitiData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.TalkListData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.echoes.CanvasDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.utils.time.DateUtils

class TalkGraffitiDetailsActivity : BaseAct() {
    private var chatId: String? = null
    private var resourceId: Int = 0
    private lateinit var uid: String
    private var fromUserIdentity = 0
    private var lastId: String = ""
    private var url: String = ""
    private var deleteItem: TalkListData.TalkListBean? = null
    private var fromUid: String? = null

    companion object {
        fun open(activity: Context, intent: Intent) {
            instance?.finish()
            activity.startActivity(intent)
        }

        var instance: TalkGraffitiDetailsActivity? = null
    }

    private lateinit var adapter: QuickAdapter<TalkListData.TalkListBean>
    private val mData by lazy { ArrayList<TalkListData.TalkListBean>() }

    override fun getLayoutId(): Int {
        return R.layout.activity_talk_graffiti_details
    }

    override fun initView() {
        instance = this
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        manager.stackFromEnd = true//列表再底部开始展示，反转后由上面开始展示
        manager.reverseLayout = true//列表翻转
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.requestDisallowInterceptTouchEvent(true)
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.layoutManager = manager
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators), ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    override fun initData() {
        chatId = intent.getStringExtra("chatId")
        resourceId = intent.getIntExtra("resourceId", 0)
        uid = intent.getStringExtra("uid")
        fromUid = intent.getStringExtra("from")
        if (TextUtils.isEmpty(fromUid)) {
            fromUid = uid
        }
        url = if (TextUtils.isEmpty(chatId)) {
            "chats/3/$fromUid/$resourceId"
        } else {
            "chats/$chatId"
        }

        adapter = object : QuickAdapter<TalkListData.TalkListBean>(this, R.layout.item_talk_graffiti, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: TalkListData.TalkListBean?) {
                val position = helper!!.itemView.tag as Int
                helper.getView(R.id.ivOfficialRight).visibility = View.GONE
                helper.getView(R.id.ivOfficialLeft).visibility = View.GONE
                if (item!!.is_self == 1) {
                    helper.getView(R.id.cardLeft).visibility = View.GONE
                    helper.getView(R.id.cardRight).visibility = View.VISIBLE
                    helper.getView(R.id.iv_user_type_right).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_right).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_left).visibility = View.GONE
                    helper.getView(R.id.ivOfficialRight).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                } else {
                    helper.getView(R.id.cardLeft).visibility = View.VISIBLE
                    helper.getView(R.id.cardRight).visibility = View.GONE
                    helper.getView(R.id.iv_user_type_left).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_left).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_right).visibility = View.GONE
                    helper.getView(R.id.ivOfficialLeft).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                }
                try {
                    if (position != mData.size - 1) {
                        if (DateUtils.isCloseEnough(item.created_at * 1000L, mData[position + 1].created_at * 1000L)) {
                            helper.getTextView(R.id.tv_Time).visibility = View.GONE
                        } else {
                            helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                            helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkGraffitiDetailsActivity, item.created_at)
                        }
                    } else {
                        helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkGraffitiDetailsActivity, item.created_at)
                    }
                } catch (e: Exception) {
                    helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkGraffitiDetailsActivity, item.created_at)
                }
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_leftimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_rightimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                if (item.garbage_type > 0) {
                    helper.getView(R.id.relativeIllegal).visibility = View.VISIBLE
                    helper.getView(R.id.iv_showPic).visibility = View.GONE
                } else {
                    helper.getView(R.id.relativeIllegal).visibility = View.GONE
                    helper.getView(R.id.iv_showPic).visibility = View.VISIBLE
                }
                Glide.with(this@TalkGraffitiDetailsActivity)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item.resource_url))
                                .placeholder(R.drawable.drawable_default_tmpry)
                                .error(R.drawable.drawable_default_tmpry))
                        .asBitmap()
                        .load(item.resource_url)
                        .into(helper.getImageView(R.id.iv_showPic))
//                (helper.getView(R.id.iv_showPic) as MsgThumbImageView).loadAsPath(item.resource_url, getImageMaxEdge(), getImageMaxEdge(), R.drawable.message_item_round_bg, item.resource_url)
                helper.getView(R.id.relativeIllegal).setOnClickListener {
                    helper.getView(R.id.relativeIllegal).visibility = View.GONE
                    helper.getView(R.id.iv_showPic).visibility = View.VISIBLE
                }
                helper.getView(R.id.iv_showPic).setOnClickListener {
                    if (item.is_self == 1) {//self
                        startActivity<ShowPicActivity>("path" to item.resource_url)
                        overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        startActivity<CanvasLocalActivity>("artworkUrl" to item.resource_url,
                                "artId" to resourceId,
                                "uid" to fromUid
                        )
                    }
                }
                helper.getView(R.id.cardLeft).setOnClickListener {
                    startActivity(Intent(this@TalkGraffitiDetailsActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                helper.getView(R.id.cardRight).setOnClickListener {
                    startActivity(Intent(this@TalkGraffitiDetailsActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                helper.getView(R.id.iv_showPic).setOnLongClickListener {
                    if (item.is_self == 1)
                        DialogCancelMsg(this@TalkGraffitiDetailsActivity).setOnClickListener(View.OnClickListener {
                            /**
                             * 撤回消息,
                             */
                            transLayout.showProgress()
                            deleteItem = item
                            EventBus.getDefault().post(SendMsgEvent(AppTools.graffitiJson(fromUid, "-1", item.chat_id, item.dialog_id, resourceId)))
                        }).show()
                    else {
                        DialogReport(this@TalkGraffitiDetailsActivity).setIsReportNormal(true).setReportTitle(resources.getString(R.string.string_report_normal)).setOnClickListener(View.OnClickListener {
                            DialogNormalReport(this@TalkGraffitiDetailsActivity).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Attach -> {
                                        reportDialog(item.dialog_id.toString(), "1")
                                    }
                                    R.id.tv_Porn -> {
                                        reportDialog(item.dialog_id.toString(), "2")
                                    }
                                    R.id.tv_Junk -> {
                                        reportDialog(item.dialog_id.toString(), "3")
                                    }
                                    R.id.tv_illegal -> {
                                        reportDialog(item.dialog_id.toString(), "4")
                                    }
                                }
                            }).show()
                        }).show()
                    }
                    false
                }
            }
        }
        recyclerView.adapter = adapter
        request(0)
        artDetails()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_Res.setOnClickListener {
            startActivity<CanvasDetailsActivity>("id" to resourceId.toString(), "type" to "5")
        }
        swipeRefresh.setOnRefreshListener {
            request(0)
        }
    }

    private fun reportDialog(dialogId: String, type: String) {
        val formBody = FormBody.Builder()
                .add("resourceId", dialogId)
                .add("reportType", type)
                .add("resourceType", "3")
                .build()
        transLayout.showProgress()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    DialogReportSuccess(this@TalkGraffitiDetailsActivity).show()
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

    //查询作品详情
    private fun artDetails() {
        OkClientHelper.get(this, "artworks/$resourceId", AdminReportGraffitiData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as AdminReportGraffitiData
                if (result.code == 0) {
                    val name = if (result.data.user_info == null) {
                        val user = PreferenceTools.getObj(this@TalkGraffitiDetailsActivity, IConstant.USERCACHE, UserInfoData::class.java)
                        user.data.nick_name
                    } else {
                        result.data.user_info.nick_name
                    }
                    tv_Info.text = name + "在" + TimeUtils.getInstance().paserLong(result.data.created_at.toLong()) + "的作品"
                }
            }
        }, "V4.2")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "$url?lastId=$lastId", TalkListData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as TalkListData
                result.data?.let {
                    if (TextUtils.isEmpty(chatId)) {
                        chatId = it[0].chat_id
                    }
                    mData.addAll(it)
                    adapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(mData.size - result.data.size)
                    if (it.size < 10) {
                        swipeRefresh.isEnabled = false
                    } else {
                        lastId = it[it.size - 1].id
                    }
                }
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteEvent(event: OnDeleteMsg) {
        if (event.data.code == 0) {
            /**
             * 删除成功  移除当前的 isBusy
             *
             * 撤回判断是否是撤回的最新的消息,
             * 并且判断 是否处于繁忙状态, 需要更新UI
             */
            try {
                if (mData.size > 0 && mData.contains(deleteItem)) {
                    val indexOf = mData.indexOf(deleteItem)
                    if (indexOf >= 0) {
                        mData.remove(deleteItem)
                        adapter.notifyDataSetChanged()
                        deleteItem = null
                    }
                }
            } catch (e: Exception) {

            }
        } else {
            if (deleteItem != null)
                showToast("撤回失败")
        }
        transLayout.showContent()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgReceive(event: OnArtNewMsgEvent) {
        if (!TextUtils.isEmpty(event.bean.action)) {
            try {
                if ("delete" == event.bean.action /*&& resourceId == event.bean.data.resourceId*/) {
                    /**
                     * 当前需要删除列表中的一条会话
                     */
                    mData.loop {
                        it.dialog_id == event.bean.data.dialogId
                    }?.let {
                        mData.remove(it)
                        adapter.notifyDataSetChanged()
                    }
                    /*  if (mData.size > 0) {
                          var tempBean: TalkListData.TalkListBean? = null
                          for (item in mData) {
                              if (item.dialog_id == event.bean.data.dialogId) {
                                  tempBean = item
                                  break
                              }
                          }
                          if (null != tempBean) {
                              mData.remove(tempBean)
                              adapter.notifyDataSetChanged()
                          }
                      }*/
                }
            } catch (e: Exception) {
            }
        } else
            if (chatId == event.bean.data.chat_id.toString()) {
                if (!TextUtils.isEmpty(event.bean.data.flag) && "1" == event.bean.data.flag) {
                    return
                }
                val loginBean = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                val bean = TalkListData.TalkListBean()
                bean.is_self = if (loginBean.data.user_id == event.bean.data.from_user_id) {
                    bean.identity_type = loginBean.data.identity_type
                    1
                } else {
                    bean.identity_type = fromUserIdentity
                    0
                }
                bean.created_at = event.bean.data.created_at
                bean.voice_len = event.bean.data.dialog_content_len.toString()
                bean.avatar_url = event.bean.data.user_avatar_url
                bean.resource_type = event.bean.data.dialog_content_type.toString()
                bean.dialog_id = event.bean.data.dialog_id.toString()
                bean.resource_url = event.bean.data.dialog_content_url
                bean.from_user_id = event.bean.data.from_user_id
                bean.chat_id = event.bean.data.chat_id.toString()
                bean.id = event.bean.data.id
                bean.resource_id = event.bean.data.resource_id.toString()
                mData.add(0, bean)
                adapter.notifyDataSetChanged()
                recyclerView.smoothScrollToPosition(0)
                /*
                 * 请求清空角标
                 */
//                clearFlag()
            }
    }

    override fun finish() {
        super.finish()
        instance = null
    }
}