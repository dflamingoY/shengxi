package org.xiaoxingqi.shengxi.modules.listen.movies

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_friends_list.view.*
import kotlinx.android.synthetic.main.vew_empty_subscribe_resource.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogGraffiti
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.ArgumentData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.SubscribeUserData
import org.xiaoxingqi.shengxi.model.UserAttentionData
import org.xiaoxingqi.shengxi.modules.againAttention
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import kotlin.math.ceil

//关注列表
class AttentionFragment : BaseFragment(), ITabClickCall {
    private var type = 0
    private lateinit var adapter: QuickAdapter<SubscribeUserData.SubscribeUserBean>
    private val mData by lazy { ArrayList<SubscribeUserData.SubscribeUserBean>() }
    private var lastId: String? = ""
    private var currentVisible = false

    override fun tabClick(isVisible: Boolean) {
        if (currentVisible != isVisible) {
            currentVisible = isVisible
            try {
                if (!currentVisible)
                    mView!!.recyclerView.smoothCloseMenu()
            } catch (e: Exception) {
            }
        }
    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_friends_list
    }

    override fun initView(view: View?) {

    }

    override fun initData() {
        arguments?.let {
            type = it.getInt("type")
            when (type) {
                1 -> {
                    mView!!.iv_empty_movies.visibility = View.VISIBLE
                    mView!!.iv_empty_movies.isSelected = AppTools.getLanguage(activity) == IConstant.HK || AppTools.getLanguage(activity) == IConstant.TW
                }
                2 -> {
                    mView!!.iv_empty_books.visibility = View.VISIBLE
                    mView!!.iv_empty_books.isSelected = AppTools.getLanguage(activity) == IConstant.HK || AppTools.getLanguage(activity) == IConstant.TW
                }
                3 -> {
                    mView!!.iv_empty_songs.visibility = View.VISIBLE
                    mView!!.iv_empty_songs.isSelected = AppTools.getLanguage(activity) == IConstant.HK || AppTools.getLanguage(activity) == IConstant.TW
                }
            }
        }
        adapter = object : QuickAdapter<SubscribeUserData.SubscribeUserBean>(activity, R.layout.item_attention, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: SubscribeUserData.SubscribeUserBean?) {
                glideUtil.loadGlide(item!!.subscribeUser.avatar_url, helper!!.getImageView(R.id.iv_img), 0, glideUtil.getLastModified(item.subscribeUser.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.subscribeUser.nick_name
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_attention), ceil((item.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_attention)
                helper.getView(R.id.linearAgain).isSelected = item.renewed_at != 0
                helper.getView(R.id.linearAgain).setOnClickListener {
                    attentionAgain(item, helper)
                }
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    mView!!.recyclerView.smoothCloseMenu()
                    DialogGraffiti(activity!!).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (type) {
                        1 -> resources.getString(R.string.string_follow_movies)
                        2 -> resources.getString(R.string.string_follow_book)
                        3 -> resources.getString(R.string.string_follow_song)
                        else -> resources.getString(R.string.string_follow_movies)
                    }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                        deleteSubScriber(item)
                        mView!!.recyclerView.smoothCloseMenu()
                    }).show()
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(1)
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", mData[position].subscribeUser.id))
        }
        mView!!.tv_Explain_Friends.setOnClickListener {
            query(0)
        }
    }

    private fun attentionAgain(item: SubscribeUserData.SubscribeUserBean, helper: BaseAdapterHelper) {
        mView!!.transLayout.showProgress()
        activity!!.againAttention(item.id.toString(), if (item.renewed_at == 0) (System.currentTimeMillis() / 1000).toInt() else 0) {
            it?.let {
                if (it.code == 0) {
                    //修改关注状态
                    if (item.renewed_at != 0) {//表示取消关注
                        item.renewed_at = 0
                        helper.getView(R.id.linearAgain).isSelected = false
                        helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_attention)
                    } else {
                        helper.getView(R.id.linearAgain).isSelected = true
                        item.renewed_at = (System.currentTimeMillis() / 1000).toInt()
                        helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_attention)
                    }
                } else
                    showToast(it.msg)
            }
            mView!!.transLayout.showContent()
        }
    }

    //删除关注
    private fun deleteSubScriber(item: SubscribeUserData.SubscribeUserBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "userSubscription/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.transLayout.showContent()
                if ((result as BaseRepData).code == 0) {
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    if (mData.size == 0) {
                        mView!!.transLayout.showEmpty()
                    }
                } else
                    showToast(result.msg)

            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V4.1")
    }

    private fun query(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "h5/html/declare/3", ArgumentData::class.java, object : OkResponse {
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
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "userSubscription?resourceType=$type&lastId=$lastId", UserAttentionData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.transLayout.showContent()
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as UserAttentionData
                        if (result.code == 0) {
                            result.data?.let { data ->
                                data?.list?.let {
                                    it.forEach { bean ->
                                        mData.add(bean)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                    }
                                    if (it.size >= 10) {
                                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                        lastId = it[it.size - 1].log_id
                                    }
                                    mView!!.tv_FriendCount.text = if (mData.size == 0) resources.getString(R.string.string_attention_empty) else String.format(resources.getString(R.string.string_attention_count), data.total)
                                }
                            }
                        }
                        if (mData.size == 0) {
                            mView!!.transLayout.showEmpty()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.transLayout.showContent()
                    }
                }, "V4.2")
            }
            1 -> {
                OkClientHelper.get(activity, "userSubscription?resourceType=$type&lastId=$lastId", SubscribeUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.transLayout.showContent()
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as SubscribeUserData
                        if (result.code == 0) {
                            result.data?.let {
                                it.forEach { bean ->
                                    mData.add(bean)
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                                if (it.size >= 10) {
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                    lastId = it[it.size - 1].log_id
                                }
                            }
                            if (mData.size == 0) {
                                mView!!.transLayout.showEmpty()
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.transLayout.showContent()
                    }
                }, "V4.2")
            }
        }
    }

}