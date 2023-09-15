package org.xiaoxingqi.shengxi.modules.listen.book

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_hot_movies.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.SearchResData
import skin.support.SkinCompatManager

class HotBooksListActivity : BaseAct() {
    private val mData by lazy { ArrayList<BaseSearchBean>() }
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private var current = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_hot_movies
    }

    override fun initView() {
        tv_Title.text = resources.getString(R.string.string_book_15)
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseSearchBean>(this, R.layout.item_hot_movie, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                Glide.with(this@HotBooksListActivity)
                        .applyDefaultRequestOptions(RequestOptions()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .centerCrop())
                        .load(item!!.book_cover)
                        .into(helper!!.getImageView(R.id.ivMovieCover))
                helper.getTextView(R.id.tv_MovieName).text = item.book_name
//                helper.getTextView(R.id.tv_Years).text = item.published_date
                helper.getTextView(R.id.tv_Score).text = "${item.book_score / 10f}"
                helper.getTextView(R.id.tv_MovieType).text = item.book_author
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.loadmore_center, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            current++
            request(1)
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(this, OneBookDetailsActivity::class.java)
                    .putExtra("id", mData[position].id))
        }
        swipeRefresh.setOnRefreshListener {
            current = 1
            request(0)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "resources/2?sortType=2&pageNo=$current&recentDay=30", SearchResData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchResData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result.data?.let {
                    if (current == 1) {
                        mData.clear()
                        mData.addAll(it)
                        adapter.notifyDataSetChanged()
                    } else
                        for (bean in result.data) {
                            mData.add(bean)
                            adapter.notifyItemChanged(adapter.itemCount - 1)
                        }
                    if (it.size >= 10)
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                transLayout.showOffline()
            }
        }, "V3.2")
    }


}