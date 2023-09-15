package org.xiaoxingqi.shengxi.modules.echoes

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.echoe_headview.view.*
import kotlinx.android.synthetic.main.frag_echoe_msg.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAlbumHowOperator
import org.xiaoxingqi.shengxi.impl.EchoesUpdateEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.EchoeData
import org.xiaoxingqi.shengxi.model.SystemNoticeData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.TalkGraffitiDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.swiprecycler.SwipeMenuRecyclerView

class EchoMsgFragment : BaseFragment() {
    private lateinit var adapter: QuickAdapter<EchoeData.EchoesBean>
    private var lastId: String = ""
    private var userinfo: UserInfoData? = null
    private lateinit var transLayout: TransLayout
    private val mData by lazy {
        ArrayList<EchoeData.EchoesBean>()
    }
    private lateinit var recyclerView: SwipeMenuRecyclerView
    private lateinit var headView: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadMoreView: View

    override fun getLayoutId(): Int {
        return R.layout.frag_echoe_msg
    }

    override fun onResume() {
        super.onResume()
        userinfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        lastId = ""
        request(0)
        getSystemInfo()
        try {
            loadMoreView.findViewById<View>(R.id.tv_echo_hint_1).isSelected = AppTools.getLanguage(activity) == IConstant.HK || AppTools.getLanguage(activity) == IConstant.TW
        } catch (e: Exception) {
        }
    }

    override fun initView(view: View?) {
        transLayout = view!!.transLayout
        recyclerView = view.recyclerView
        swipeRefreshLayout = view.swipeRefresh
        recyclerView.layoutManager = LinearLayoutManager(activity)
        headView = LayoutInflater.from(activity).inflate(R.layout.echoe_headview, recyclerView, false)
        recyclerView.itemAnimator?.changeDuration = 0
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators),
                ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    override fun initData() {
        adapter = object : QuickAdapter<EchoeData.EchoesBean>(activity, R.layout.item_echoes, mData, headView) {
            override fun convert(helper: BaseAdapterHelper?, item: EchoeData.EchoesBean?) {
                helper!!.getTextView(R.id.tvUserName)!!.text = item!!.with_user_name
                helper.getTextView(R.id.tv_Delete).text = resources.getString(R.string.string_statue_off)
                glideUtil.loadGlide(item.with_user_avatar_url, helper.getImageView(R.id.roundCircle), R.mipmap.icon_user_default, glideUtil.getLastModified(item.with_user_avatar_url))
                helper.getTextView(R.id.tv_Time)?.text = TimeUtils.getInstance().paserFriends(activity, item.updated_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.with_user_identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.with_user_identity_type == 1
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    deleteComments(item)
                }
                helper.getTextView(R.id.tv_ChatType).text = if (item.chat_type == 1) resources.getString(R.string.string_echoes_type_1) else "[涂鸦]"
                if (!TextUtils.isEmpty(item.un_read_num.toString()) && item.un_read_num != 0) {
                    if (item.notice_at == 0) {
                        helper.getView(R.id.tv_Count).visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_Count).text = item.un_read_num.toString()
                    } else {
                        helper.getView(R.id.tv_Count).visibility = View.GONE
                    }
                } else {
                    helper.getView(R.id.tv_Count).visibility = View.GONE
                }
                helper.getView(R.id.tv_Report).setOnClickListener {
                    DialogAlbumHowOperator(activity!!).setTitle(if (item.chat_type == 1)
                        resources.getString(R.string.string_report_hint_content) else resources.getString(R.string.string_27)).show()
                }
                helper.getView(R.id.roundCircle).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.with_user_id.toString()))
                }
            }
        }
        recyclerView.setHasFixedSize(true)
        loadMoreView = LayoutInflater.from(activity).inflate(R.layout.echoes_loadmore, recyclerView, false)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadMoreView)
        recyclerView.adapter = adapter
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            if (mData[position].chat_type == 1)
                startActivity(Intent(activity, TalkListActivity::class.java)
                        .putExtra("voice_id", mData[position].resource_id.toString())
                        .putExtra("chat_id", mData[position].chat_id.toString())
                        .putExtra("uid", mData[position].resource_user_id)
                        .putExtra("talkId", mData[position].with_user_id.toString())
                )
            else {
                TalkGraffitiDetailsActivity.open(activity!!, Intent(activity, TalkGraffitiDetailsActivity::class.java)
                        .putExtra("resourceId", mData[position].resource_id)
                        .putExtra("chatId", mData[position].chat_id.toString())
                        .putExtra("uid", mData[position].with_user_id.toString())
                )
//                startActivity<TalkGraffitiDetailsActivity>("resourceId" to mData[position].resource_id,
//                        "chatId" to mData[position].chat_id.toString(),
//                        "uid" to mData[position].with_user_id.toString()
//                )
            }
            recyclerView.smoothCloseMenu()
        }
        headView.echoSystem.setOnClickListener { startActivity(Intent(activity, SystemInfoActivity::class.java)) }
        headView.echoNewF.setOnClickListener { startActivity(Intent(activity, NewFriendsActivity::class.java)) }
        headView.echoHint.setOnClickListener { startActivity(Intent(activity, MsgNotifyActivity::class.java)) }
        swipeRefreshLayout.setOnRefreshListener {
            lastId = ""
            request(0)
            getSystemInfo()
        }
        adapter.setOnLoadListener {
            request(0)
        }
    }

    /**
     * 更新会话
     */
    private fun deleteComments(item: EchoeData.EchoesBean) {
        transLayout.showProgress()
        OkClientHelper.patch(activity, "chats/${item.chat_id}", FormBody.Builder().add("isVisible", "0")
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    if (item.un_read_num > 0) {
                        EventBus.getDefault().post(EchoesUpdateEvent(2))
                    }
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    recyclerView.smoothCloseMenu()
                    if (mData.size == 0) {
                        lastId = ""
                        request(0)
                    }
                    showToast("关闭成功")
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.1")
    }

    /**
     * 获取系统通知消息
     */
    private fun getSystemInfo() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (null != loginBean) {
            OkClientHelper.get(activity, "messages/${loginBean.user_id}", SystemNoticeData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    if ((result as SystemNoticeData).code == 0) {
                        headView.echoSystem.setData(result.data.sys)
                        headView.echoNewF.setData(result.data.frequest)
                        headView.echoHint.setData(result.data.other)
                    }
                }

                override fun onFailure(any: Any?) {

                }
            })
        }
    }

    /**
     * 是否在请求中
     */
    private var isQuestint = false

    override fun request(flag: Int) {
        if (isQuestint)
            return
        isQuestint = true
        OkClientHelper.get(activity, "chats/users/${userinfo?.data?.user_id}/1?lastDialogId=$lastId", EchoeData::class.java, object : OkResponse {

            @Synchronized
            override fun success(result: Any?) {
                synchronized(this@EchoMsgFragment) {
                    result as EchoeData
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
                            adapter.notifyItemChanged(adapter.itemCount - result.data.size - 2)
                        }
                        if (result.data != null && result.data.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                        lastId = mData[mData.size - 1].last_dialog_id
                    } else {
                        if (TextUtils.isEmpty(lastId)) {
                            mData.clear()
                            adapter.notifyDataSetChanged()
                        }
                    }
                    if (mData.size == 0) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                    }
                    swipeRefreshLayout.isRefreshing = false
                    isQuestint = false
                }
            }

            override fun onFailure(any: Any?) {
                swipeRefreshLayout.isRefreshing = false
                if (mData.size == 0) {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                }
                isQuestint = false
            }
        }, "V4.3")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

/*
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deleteEvent(event: IDeleteTypeEvent) {
        //撤销取消小红点  chatId
        mData.loop {
            it.chat_id == event.bean.data.chat_id
        }?.let {
            it.
        }
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun update(event: EchoesUpdateEvent) {
        if (event.type == 1) {
            lastId = ""
            request(0)
            getSystemInfo()
        } else if (event.type == 2) {
            getSystemInfo()
        }
    }
}