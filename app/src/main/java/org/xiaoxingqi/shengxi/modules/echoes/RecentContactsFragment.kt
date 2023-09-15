package org.xiaoxingqi.shengxi.modules.echoes

import android.content.Intent
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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
import org.xiaoxingqi.shengxi.impl.OnRecentStatusEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.EchoeData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.swiprecycler.SwipeMenuRecyclerView

class RecentContactsFragment : BaseFragment() {
    private var lastId: String? = null
    private lateinit var recyclerView: SwipeMenuRecyclerView
    private lateinit var transLayout: TransLayout
    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var adapter: QuickAdapter<EchoeData.EchoesBean>
    private val mData by lazy { ArrayList<EchoeData.EchoesBean>() }
    override fun getLayoutId(): Int {
        return R.layout.frag_echoe_msg
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        transLayout = view.transLayout
        refreshLayout = view.swipeRefresh
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        adapter = object : QuickAdapter<EchoeData.EchoesBean>(activity, R.layout.item_echoes, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: EchoeData.EchoesBean?) {
                helper!!.getTextView(R.id.tvUserName).text = item!!.with_user_name
                glideUtil.loadGlide(item!!.with_user_avatar_url, helper.getImageView(R.id.roundCircle), R.mipmap.icon_user_default, glideUtil.getLastModified(item.with_user_avatar_url))
                helper.getTextView(R.id.tv_ChatType).text = if (item.last_resource_type == 1) "[语音]" else "[图片]"
                helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(activity, item.updated_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.with_user_identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.with_user_identity_type == 1
                helper.getView(R.id.ivOfficial).visibility = if (item.with_user_id == 1) View.VISIBLE else View.GONE
                try {
                    if (loginBean.user_id == "1")
                        helper.getView(R.id.tvTopicType).visibility = if (item.topic_type == 1) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                }
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    deleteComments(item!!)
                }
                helper.getView(R.id.tv_Report).setOnClickListener {
                    DialogAlbumHowOperator(activity!!).setTitle(resources.getString(R.string.string_report_hint_content)).show()
                }
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
                helper.getView(R.id.roundCircle).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.with_user_id.toString()))
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_little_height_grey, recyclerView, false))
//        request(0)//小二特殊处理
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, ChatActivity::class.java)
                    .putExtra("topicType", mData[position].topic_type)
                    .putExtra("uid", mData[position].with_user_id.toString())
                    .putExtra("userName", mData[position].with_user_name)
                    .putExtra("unreadCount", mData[position].un_read_num)
                    .putExtra("createAt", mData[position].created_at)
                    .putExtra("chatId", mData[position].chat_id.toString())
            )
            if (mData[position].un_read_num > 0) {
                mData[position].un_read_num = 0
                adapter.notifyItemChanged(position)
            }
        }
        adapter.setOnLoadListener {
            lastId = try {
                mData[mData.size - 1].chat_id.toString()
            } catch (e: Exception) {
                null
            }
            request(1)
        }
        refreshLayout.setOnRefreshListener {
            lastId = null
            request(0)
        }
        transLayout.findViewById<View>(R.id.tv_Custom).setOnClickListener {
            startActivity(Intent(activity, ChatActivity::class.java).putExtra("uid", "1"))
        }
    }

    override fun onResume() {
        super.onResume()
        lastId = null
        request(0)
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(activity, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            transLayout.findViewById<TextView>(R.id.tv_recent_hint_1).text = resources.getString(R.string.string_chat_e_1)
        } else {
            transLayout.findViewById<TextView>(R.id.tv_recent_hint_1).text = resources.getString(R.string.string_chat_i_1)
        }
    }

    private fun deleteComments(item: EchoeData.EchoesBean) {
        transLayout.showProgress()
        OkClientHelper.patch(activity, "chats/${item.chat_id}", FormBody.Builder().add("isVisible", "0")
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                recyclerView.smoothCloseMenu()
                if ((result as BaseRepData).code == 0) {
                    showToast("关闭成功")
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    if (mData.size == 0) {
                        transLayout.showEmpty()
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.1")
    }

    private var isQuestint = false
    override fun request(flag: Int) {
        if (isQuestint)
            return
        isQuestint = true
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "chats/users/${loginBean.user_id}/2?lastId=$lastId", EchoeData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as EchoeData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.code == 0) {
                    if (null == lastId) {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                    }
                    if (result.data != null && result.data.size > 0) {
                        result.data.forEach {
                            mData.add(it)
                            adapter.notifyItemChanged(adapter.itemCount - 1)
                        }
//                        mData.addAll(result.data)
//                        adapter.notifyDataSetChanged()
                        if (result.data.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    } else {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    }
                }
                recyclerView.smoothCloseMenu()
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                refreshLayout.isRefreshing = false
                isQuestint = false
            }

            override fun onFailure(any: Any?) {
                isQuestint = false
                transLayout.showContent()
                refreshLayout.isRefreshing = false
            }
        })
    }

    /**
     * 拉黑 或者清空历史记录要删除联系人在列表中, 清除掉阅读的数字
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecentEvent(event: OnRecentStatusEvent) {
        if (event.type == 1) {
            var temp: EchoeData.EchoesBean? = null
            for (bean in mData) {
                if (bean.with_user_id.toString() == event.userId) {
                    temp = bean
                    break
                }
            }
            if (null != temp) {
//                var position = mData.indexOf(temp)
                mData.remove(temp)
                adapter.notifyDataSetChanged()
//                adapter.notifyItemRemoved(position)
            }
            if (mData.size == 0) {
                transLayout.showEmpty()
            }
        } else if (event.type == 2) {
            lastId = null
            request(0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}