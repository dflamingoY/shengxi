package org.xiaoxingqi.shengxi.modules.user.black

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_recycler.view.*
import kotlinx.android.synthetic.main.frag_recycler.view.recyclerView
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
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class BlackFragment : BaseFragment() {
    private var type = 1

    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private val mData by lazy { ArrayList<BaseUserBean>() }
    private lateinit var headView: View
    private var lastId = ""

    override fun getLayoutId(): Int {
        return R.layout.frag_recycler
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        headView = LayoutInflater.from(activity).inflate(R.layout.head_black_list, view.recyclerView, false)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        arguments?.let {
            type = it.getInt("key")
        }
        headView.findViewById<TextView>(R.id.tv_desc).text = when (type) {
            1 -> resources.getString(R.string.string_black_black_desc)
            2 -> resources.getString(R.string.string_black_shield_desc)
            3 -> resources.getString(R.string.string_black_white_desc)
            else -> resources.getString(R.string.string_black_black_desc)
        }
        adapter = object : QuickAdapter<BaseUserBean>(activity, R.layout.item_black, mData, headView) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tvName).text = item.nick_name
                if (type == 2) {
                    helper.getTextView(R.id.tv_RemoveBlack).text = "取消屏蔽"
                    helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                }
                helper.getView(R.id.tv_RemoveBlack).setOnClickListener {
                    removeBlack(item)
                }
            }
        }
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, UserDetailsActivity::class.java)
                    .putExtra("id", mData[position].user_id)
            )
        }
    }

    /**
     * 移除黑名单
     */
    private fun removeBlack(bean: BaseUserBean) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(activity, "users/${loginBean.user_id}/blacklist/${bean.user_id}", FormBody.Builder().add("blackType", type.toString())
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.transLayout.showContent()
                if ((result as BaseRepData).code == 0) {
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }

        })
    }

    override fun request(flag: Int) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "users/${loginBean.user_id}/blacklist?lastId=$lastId&blackType=$type", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemChanged(adapter.itemCount - 1)
                        lastId = bean.id
                    }
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                } else {
                    if (lastId.isEmpty()) {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showOffline()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userRelationChangeEvent(event: INotifyFriendStatus) {
        if (event.status == 5) {
            if (mData.size > 0) {
                var tempData: BaseUserBean? = null
                for (bean in mData) {
                    if (event.userId == bean.user_id) {
                        tempData = bean
                        break
                    }
                }
                tempData?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}