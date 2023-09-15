package org.xiaoxingqi.shengxi.modules.listen.book

import android.content.Intent
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.frag_user_books.view.*
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
import org.xiaoxingqi.shengxi.impl.OnDeletedMovieEvent
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.UserMoviesData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.movies.UserMoviesListDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import skin.support.SkinCompatManager

class UserBookFragment : BaseFragment() {
    private var lastId: String? = ""
    private var score: String? = ""
    private lateinit var transLayout: TransLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private lateinit var allcount: TextView
    private var userId: String? = null
    private var title: String? = null
    private val mData by lazy {
        ArrayList<BaseSearchBean>()
    }

    //view_empty_similar_book
    override fun getLayoutId(): Int {
        return R.layout.frag_user_books
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        transLayout = view.transLayout
        recyclerView.layoutManager = LinearLayoutManager(activity)
        allcount = view.tv_AllCount
        swipeRefresh = view.swipeRefresh
        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    override fun initData() {
        EventBus.getDefault().register(this)

        score = arguments?.getString("score")
        userId = arguments?.getString("uid")
        title = arguments?.getString("title")
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        adapter = object : QuickAdapter<BaseSearchBean>(activity, R.layout.item_user_movies, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                Glide.with(this@UserBookFragment)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                        .load(item!!.book_cover)
                        .into(helper!!.getImageView(R.id.ivMovieCover))
                try {
                    if (TextUtils.isEmpty(item.published_date)) {
                        helper.getTextView(R.id.tv_Years).text = ""
                    } else {
                        val paserYyMm = if (item.published_date.contains("-")) item.published_date.split("-")[0] else item.published_date
                        helper.getTextView(R.id.tv_Years).text = "（$paserYyMm）"
                    }
                } catch (e: Exception) {

                }
                helper.getTextView(R.id.tv_UserComment).text = if (loginBean.user_id != userId) resources.getString(R.string.string_book_other_comment) else resources.getString(R.string.string_book_self_comment)
                helper.getTextView(R.id.tv_MovieName).text = item?.book_name
                helper.getTextView(R.id.tv_MovieType).text = item.book_author
                val position = helper.itemView.tag as Int
                helper.getView(R.id.viewLine).visibility = if (position != mData.size - 1) View.VISIBLE else View.GONE
                helper.getView(R.id.tv_UserComment).setOnClickListener {
                    startActivity(Intent(activity, UserMoviesListDetailsActivity::class.java)
                            .putExtra("resourceType", 2)
                            .putExtra("id", item.id)
                            .putExtra("uid", userId)
                            .putExtra("title", title))
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            lastId = mData[mData.size - 1].rate_id
            request(0)
        }
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
    }


    override fun request(flag: Int) {
        OkClientHelper.get(activity, "users/$userId/resources/2?lastId=$lastId${if (TextUtils.isEmpty(score)) "" else "&score=$score"}", UserMoviesData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserMoviesData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                transLayout.showContent()
                if (result.data.list != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data.list)
                        adapter.notifyDataSetChanged()
                    } else {
                        if (result.data.list.size > 0) {
                            adapter.notifyItemChanged(mData.size - 1)
                            for (item in result.data.list) {
                                mData.add(item)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                        }
                    }
                    if (result.data.list.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                allcount.text = "共${result.data.total}本"
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        }, "V3.2")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateDeleted(event: OnDeletedMovieEvent) {
        /**
         * 删除影评 在页面中清除数据
         */
        if (event.resourceType == 2) {
            if (event.deletedId == score) {
                lastId = ""
                request(0)
            }
            if (TextUtils.isEmpty(score)) {
                lastId = ""
                request(0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}