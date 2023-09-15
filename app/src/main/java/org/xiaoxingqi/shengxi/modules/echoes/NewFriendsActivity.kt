package org.xiaoxingqi.shengxi.modules.echoes

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_new_friends.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.ShackActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*

class NewFriendsActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<NewFriendsData.NewFriendBean>
    private val mData = ArrayList<NewFriendsData.NewFriendBean>()
    override fun getLayoutId(): Int {
        return R.layout.activity_new_friends
    }

    private val userInfo by lazy {
        PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
    }

    private var lastId: String? = null

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        transLayout.findViewById<View>(R.id.tv_friends_request_hint_1).isSelected = AppTools.getLanguage(this) == IConstant.HK || AppTools.getLanguage(this) == IConstant.TW
        adapter = object : QuickAdapter<NewFriendsData.NewFriendBean>(this, R.layout.item_friends, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: NewFriendsData.NewFriendBean?) {
                helper?.getTextView(R.id.tvName)?.text = item?.nick_name
                if (item?.log_status == 0) {
                    helper?.getView(R.id.linear_Status)?.visibility = View.VISIBLE
                    helper?.getView(R.id.tv_ShowAgree)?.visibility = View.GONE
                } else {
                    helper?.getView(R.id.linear_Status)?.visibility = View.GONE
                    helper?.getView(R.id.tv_ShowAgree)?.visibility = View.VISIBLE
                }
                helper?.getTextView(R.id.tv_Time)?.text = TimeUtils.getInstance().paserFriends(this@NewFriendsActivity, item!!.created_at)
                helper!!.getView(R.id.view_line).visibility =
                        if (mData.size - 1 == helper!!.itemView.tag) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                helper?.getView(R.id.tvAgree)?.setOnClickListener {
                    //同意
                    feedbackFriend(item!!, helper, "1")
                }
                helper?.getView(R.id.tvIgnore)?.setOnClickListener {
                    //忽略
                    feedbackFriend(item!!, helper, "2")
                }
                helper?.getView(R.id.tvName)?.setOnClickListener {
                    startActivity(Intent(this@NewFriendsActivity, UserDetailsActivity::class.java).putExtra("id", item!!.from_user_id))
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(0)
        }
        swipeRefresh.setOnRefreshListener {
            lastId = null
            request(0)
        }
        transLayout.findViewById<View>(R.id.tv_FriendBang).setOnClickListener {
            /**
             * 去好友榜
             */
            startActivity(Intent(this, ShackActivity::class.java))
            finish()
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "messages/${userInfo?.data?.user_id}/2?lastId=$lastId", NewFriendsData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewFriendsData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null && result.data.size > 0) {
                    if (lastId == null) {
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
                    result.data.let {
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    }
                    lastId = mData[mData.size - 1].id.toString()
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                transLayout.showOffline()
                swipeRefresh.isRefreshing = false
            }
        })
    }

    private fun feedbackFriend(bean: NewFriendsData.NewFriendBean, helper: BaseAdapterHelper, type: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("operationType", type)
                .build()
        OkClientHelper.patch(this, "users/${PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java).data.user_id}/friendslog/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (type == "1") {
                        helper.getView(R.id.linear_Status).visibility = View.GONE
                        helper.getView(R.id.tv_ShowAgree).visibility = View.VISIBLE
                        bean.log_status = 1
                    } else {//忽略
                        mData.remove(bean)
                        adapter.notifyDataSetChanged()

                    }
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun newFriendsNotify(event: INotifyFriendStatus) {
        try {
            if (!TextUtils.isEmpty(event.userId)) {
                if (mData.size > 0) {
                    for (bean in mData) {
                        if (event.userId == bean.from_user_id) {//对方已经同意为添加好友 更改按钮状态
                            bean.log_status = 1
                            adapter.notifyDataSetChanged()
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

}