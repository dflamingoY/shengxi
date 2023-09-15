package org.xiaoxingqi.shengxi.modules.adminManager

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_admin_recycler.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.AdminCheckListData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

class CheckFragment : BaseFragment(), ITabClickCall {
    private lateinit var adapter: QuickAdapter<AdminCheckListData.AdminCheckBean>
    private val mData by lazy { ArrayList<AdminCheckListData.AdminCheckBean>() }
    private var lastId: String = ""
    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_admin_recycler
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<AdminCheckListData.AdminCheckBean>(activity, R.layout.item_check, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: AdminCheckListData.AdminCheckBean?) {
                glideUtil.loadGlide(item!!.user.avatar_url, helper!!.getImageView(R.id.img), 0, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tvName).text = item.user.nick_name
                helper.getTextView(R.id.tvId).text = "${item.id}"
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserLong(item.created_at.toLong())
                helper.getTextView(R.id.tvReportType).text = when (item.data_type) {
                    1 -> "用户信息"
                    2 -> "心情"
                    3 -> "对话"
                    4 -> "台词"
                    5 -> "配音"
                    6 -> "绘画"
                    7 -> "词条"
                    else -> "未知"
                }
                helper.getImageView(R.id.img).setOnClickListener {
                    startActivity<UserDetailsActivity>("id" to item.user.id)
                }
            }
        }
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
        request(0)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            when (mData[position].data_type) {//1:用户，2：声兮，3：对话，4：台词，5：配音，6：绘画
                1 -> {//1:用户
                    startActivity<CheckUserInfoActivity>("data" to mData[position])
                }
                2 -> {//2：声兮
                    startActivity<CheckVoiceActivity>("data" to mData[position])
                }
                3 -> {//3：对话，
                    startActivity<CheckChatActivity>("data" to mData[position])
                }
                4 -> {//4：台词

                }
                5 -> {//5：配音

                }
                6 -> {//6：绘画
                    startActivity<CheckOriginPaintActivity>("data" to mData[position])
                }
                7 -> {//词条
                    startActivity<CheckEntryActivity>("data" to mData[position])
                }
            }
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "admin/suspiciousDatas?token=${SPUtils.getString(activity, IConstant.ADMINTOKEN, "")}&lastId=$lastId", AdminCheckListData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
            }

            override fun success(result: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                mView!!.swipeRefresh.isRefreshing = false
                result as AdminCheckListData
                result.data?.let {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(it)
                        adapter.notifyDataSetChanged()
                    } else {
                        it.forEach { bean ->
                            mData.add(bean)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (it.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        lastId = it[it.size - 1].id.toString()
                    }
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operator(event: AdminCheckOperatorEvent) {
        mData.loop {
            it.id == event.id
        }?.let {
            mData.remove(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}