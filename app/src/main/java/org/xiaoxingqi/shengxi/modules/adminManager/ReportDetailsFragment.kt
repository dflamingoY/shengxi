package org.xiaoxingqi.shengxi.modules.adminManager

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
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
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.event.AdminReportEvent
import org.xiaoxingqi.shengxi.model.AdminReportData
import org.xiaoxingqi.shengxi.model.BaseAdminReportBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

/**
 * 举报处理情况界面
 */
class ReportDetailsFragment : BaseFragment(), ITabClickCall {
    private var currentTag = 3//3 待调查 4 待处理  5 已处理
    private var lastId: String? = null
    private lateinit var adapter: QuickAdapter<BaseAdminReportBean>
    private val mData by lazy { ArrayList<BaseAdminReportBean>() }

    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_echoe_msg
    }

    override fun initView(view: View?) {

    }

    override fun initData() {
        EventBus.getDefault().register(this)
        currentTag = arguments?.getInt("state")!!
        adapter = object : QuickAdapter<BaseAdminReportBean>(activity, R.layout.item_admin_report, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseAdminReportBean?) {
                glideUtil.loadGlide(item!!.from_user_info.avatar_url, helper!!.getImageView(R.id.img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.from_user_info.avatar_url))
                helper.getTextView(R.id.tvName).text = item.from_user_info.nick_name
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserTimeMachine(activity, item.created_at)
                helper.getTextView(R.id.tvId).text = item.id
                helper.getTextView(R.id.tv_process).text = when (currentTag) {
                    3 -> "待调查"
                    else -> "待处理"
                }
                helper.getTextView(R.id.otherWay).text = when (currentTag) {
                    5 -> "待调查"
                    else -> "已处理"
                }
                helper.getTextView(R.id.tvReportType).text = when (item.type_id) {
                    1 -> "人身攻击"
                    2 -> "色情暴力"
                    3 -> "垃圾广告"
                    4 -> "违法信息"
                    else -> "人身攻击"
                }
                helper.getTextView(R.id.tvReportContent).text = when (item.resource_type) {
                    1 -> "心情"
                    2 -> "会话"
                    3 -> when (item.dialog.chat_type) {
                        1 -> "回声"
                        2 -> "私聊"
                        else -> "涂鸦"
                    }
                    4 -> "用户"
                    5 -> "灵魂画手"
                    6 -> "涂鸦"
                    7 -> "台词"
                    8 -> "配音"
                    else -> "声兮"
                }
                helper.getView(R.id.img).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.from_user_info.id))
                }
                helper.getView(R.id.tv_process).setOnClickListener {
                    when (currentTag) {
                        3 -> {//待调查
                            patch("0", item)
                        }
                        else -> {
                            patch("1", item)
                        }
                    }
                    mView!!.recyclerView.smoothCloseMenu()
                }
                helper.getView(R.id.otherWay).setOnClickListener {
                    mView!!.recyclerView.smoothCloseMenu()
                    when (currentTag) {
                        5 -> {
                            patch("0", item)
                        }
                        else -> {
                            /*DialogAdminReportOperator(activity!!).setOnClickListener(View.OnClickListener { id ->
                                when (id.id) {
                                    R.id.tv_AddBlack -> {
                                        patch("3", item)
                                    }
                                    R.id.tv_Report -> {
                                        patch("4", item)
                                    }
                                    R.id.tv_report_normal -> {

                                    }
                                }
                            }).show()*/
                            patch("2", item)
                        }
                    }
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemClickListener { _, position ->
            if (mData[position].resource_type == 2) {
                return@setOnItemClickListener
            }
            when {
                mData[position].resource_type == 4 -> startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", mData[position].to_user_info.id))
                mData[position].resource_type == 6 -> {
                    startActivity(Intent(activity, AdminReportGraffitiActivity::class.java)
                            .putExtra("id", mData[position].graffiti.id)
                    )
                }
                mData[position].resource_type == 7 || mData[position].resource_type == 8 -> {//举报台词
                    startActivity(Intent(activity, AlarmReportDetailsActivity::class.java)
                            .putExtra("type", mData[position].resource_type)
                            .putExtra("data", if (mData[position].resource_type == 7) mData[position].line else mData[position].dubbing.apply {
                                if (this.from_user_info == null) {
                                    from_user_info = mData[position].to_user_info
                                }
                            }))
                }
                else -> startActivity(Intent(activity, AdminReportDetailsActivity::class.java)
                        .putExtra("resource", mData[position].voice?.resource)
                        .putExtra("data", mData[position]))
            }
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(1)
        }
    }

    private fun patch(status: String, bean: BaseAdminReportBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.patch(activity, "admin/reports/${bean.id}", FormBody.Builder().add("reportStatus", status).add("token", SPUtils.getString(activity, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "admin/reports?token=${SPUtils.getString(activity, IConstant.ADMINTOKEN, "")}&reportStatus=${if (currentTag == 3) "1" else if (currentTag == 4) "0" else "2-3-4"}&lastId=$lastId", AdminReportData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as AdminReportData
                if (result.code == 0 && result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        result.data.forEach {
                            mData.add(it)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    lastId = mData[mData.size - 1].id
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    /**
     * 更新事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: AdminReportEvent) {
        try {
            if (mData.size > 0) {
                for (bean in mData) {
                    if (bean.id == event.id) {
                        if (bean.voice != null) {
                            bean.voice.voice_status = event.status
                            bean.voice.hide_at = event.hideAt
                        } else if (bean.artwork != null) {
                            bean.artwork.artwork_status = event.status
                            bean.artwork.hide_at = event.hideAt
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {

        }
    }
}