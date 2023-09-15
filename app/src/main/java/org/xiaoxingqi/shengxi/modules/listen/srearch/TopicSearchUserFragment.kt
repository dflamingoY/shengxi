package org.xiaoxingqi.shengxi.modules.listen.srearch

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_search_user.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.ISearchTag
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity

class TopicSearchUserFragment : BaseFragment(), ISearchTag {

    override fun setTag(tag: String?, isSearch: Boolean) {
        searchTag = tag
        isRequest = false
        if (isSearch && isCurrentVisibility) {
            lastId = null
            mData.clear()
            adapter.notifyDataSetChanged()
            request(0)
        }
    }

    private var isCurrentVisibility = false
    override fun visible(page: Int) {
        isCurrentVisibility = page == 1
        if (!isRequest) {
            lastId = null
            request(0)
        }
    }

    private var isRequest = false
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private val mData by lazy {
        ArrayList<BaseUserBean>()
    }
    private var searchTag: String? = null
    private var lastId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.frag_search_user
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseUserBean>(activity, R.layout.item_search_user, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                helper!!.getTextView(R.id.tv_UserName).text = item!!.nick_name
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
            }
        }
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, mView!!.recyclerView, false))
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", mData[position].user_id))
        }
        adapter.setOnLoadListener {
            request(0)
        }
    }

    override fun request(flag: Int) {
        mView!!.transLayout.showProgress()
        OkClientHelper.get(activity, "users/search?searchValue=$searchTag&lastId=$lastId", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemChanged(adapter.itemCount - 1)
                    }
                    lastId = mData[mData.size - 1].user_id
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                mView!!.transLayout.showContent()
                if (mData.size == 0) {
                    mView!!.transLayout.showEmpty()
                }
                isRequest = true
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showOffline()
                isRequest = true
            }
        })
    }

}