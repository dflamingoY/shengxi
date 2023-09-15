package org.xiaoxingqi.shengxi.modules.user.frag

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_friends_list.view.*
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
import org.xiaoxingqi.shengxi.impl.IDeleteFriends
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.listen.FriendsBangListActivity
import org.xiaoxingqi.shengxi.modules.user.EditUserNameActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserHomeActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.TransLayout

class FriendListFragment : BaseFragment(), ITabClickCall {
    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private val mData by lazy { ArrayList<BaseUserBean>() }
    val REQUEST_REMARK = 0x00
    private var userbean: BaseUserBean? = null
    private var lastId: String? = null
    private var userId: String? = null
    private lateinit var transLayout: TransLayout
    private var userInfo: UserInfoData? = null
    override fun getLayoutId(): Int {
        return R.layout.frag_friends_list
    }

    override fun initView(view: View?) {
        transLayout = view!!.transLayout
    }

    override fun initData() {
        EventBus.getDefault().register(this)
//        userId = (activity as UserHomeActivity).uid
//        userInfo = (activity as UserHomeActivity).userInfo
//        mView!!.tv_FriendCount.text = String.format(resources.getString(R.string.string_friends_count), (activity as UserHomeActivity).friendCount)
        adapter = object : QuickAdapter<BaseUserBean>(activity, R.layout.item_friend_list, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                glideUtil.loadGlide(item?.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item?.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item?.nick_name
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_friend), Math.ceil((item?.release_at!! - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getView(R.id.tv_Remark).setOnClickListener {
                    userbean = item
                    startActivityForResult(Intent(activity, EditUserNameActivity::class.java).putExtra("name", item.nick_name), REQUEST_REMARK)
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, mView!!.recyclerView, false))
        request(1)
    }

    override fun initEvent() {
        transLayout.findViewById<View>(R.id.tv_Nearby).setOnClickListener {
            /**
             * 查看好友帮
             */
//            query(1)
            startActivity(Intent(activity, FriendsBangListActivity::class.java))

        }
        adapter.setOnItemClickListener { view, position ->
            UserDetailsActivity.start(activity!!, mData[position].avatar_url, mData[position].user_id.toString(), view.findViewById(R.id.iv_img))
        }
        mView!!.tv_Explain_Friends.setOnClickListener {
            userInfo?.let {
                query(3)
            }
        }
        adapter.setOnLoadListener {
            request(2)
        }
    }

    private var friendBangUrl: String? = null
    private fun query(flag: Int) {
        when (flag) {
            1 -> {
                OkClientHelper.get(activity, "resources?resourceType=1&platformId=3", ResouceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ResouceData
                        if (result.code == 0 && result.data != null) {
                            friendBangUrl = result.data.resource_content
                            query(2)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            2 -> {
                OkClientHelper.get(activity, "leaderboard/users", ListenFriendData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenFriendData
//                        startActivity(Intent(activity, WebViewActivity::class.java).putExtra("title", "好友榜")
//                                .putExtra("url", "$friendBangUrl?data=${URLEncoder.encode(AppTools.encode64(result.data))}")
//                        )
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            3 -> {
                OkClientHelper.get(activity, "h5/html/declare/1", ArgumentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ArgumentData
                        if (result.code == 0 && result.data != null) {
                            startActivity(Intent(activity, ActionActivity::class.java)
                                    .putExtra("url", result.data.html_title)
                                    .putExtra("isHtml", true)
                                    .putExtra("url", result.data.html_id)
                            )
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "users/$userId/friends?lastId=$lastId", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                if (result.code == 0 && result.data != null) {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                        lastId = bean.id
                    }
                }
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result.data?.let {
                    if (it.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else {
                    mView!!.relativeFriendsInfo.visibility = View.VISIBLE
                }
            }

            override fun onFailure(any: Any?) {
                if (AppTools.isNetOk(activity)) {

                } else {
                    transLayout.showOffline()
                    if (mData.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }
        })
    }

    private fun updateRemark(remark: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("nickName", remark)
                .build()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(activity, "users/${loginBean.user_id}/friends/${userbean?.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    userbean?.let {
                        it.nick_name = remark
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_REMARK) {
                data?.let {
                    val name = data.getStringExtra("name")
                    updateRemark(name)
                }
            }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deleteEvent(event: IDeleteFriends) {
        if (mData.size > 0) {
            var tempBean: BaseUserBean? = null
            for (bean in mData) {
                if (bean.user_id == event.userid) {
                    tempBean = bean
                    break
                }
            }
            tempBean?.let {
                mData.remove(tempBean)
                adapter.notifyDataSetChanged()
//                (activity as UserHomeActivity).friendCount--
//                mView!!.tv_FriendCount.text = String.format(resources.getString(R.string.string_friends_count), (activity as UserHomeActivity).friendCount - 1)
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }
        }
    }


    /**
     * 更新好友请求的状态,
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 2) {
            lastId = null
            request(0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}