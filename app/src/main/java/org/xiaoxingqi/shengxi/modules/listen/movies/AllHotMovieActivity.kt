package org.xiaoxingqi.shengxi.modules.listen.movies

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
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.SkinCompatManager

/**
 * 所有热门电影的列表
 */
class AllHotMovieActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private val mData by lazy {
        ArrayList<BaseSearchBean>()
    }
    private var current = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_hot_movies
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        tv_Title.text = resources.getString(R.string.string_all_hot_Movies)
        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseSearchBean>(this, R.layout.item_hot_movie, mData) {

            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                helper!!.getTextView(R.id.tv_Score).text = "${item!!.movie_score / 10f}分"
                Glide.with(this@AllHotMovieActivity)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                        .load(item?.movie_poster)
                        .into(helper?.getImageView(R.id.ivMovieCover))
                helper.getTextView(R.id.tv_MovieName).text = item?.movie_title
                helper.getTextView(R.id.tv_MovieType).text = AppTools.array2String(item.movie_area) + "/" + AppTools.array2String(item.movie_type) + "/${item.movie_len}分钟"
                helper.getTextView(R.id.tv_Actor).text = AppTools.array2String(item.movie_starring)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { view, position ->
            OneMovieDetailsActivity.start(this, mData[position].movie_poster, mData[position].id, view.findViewById(R.id.ivMovieCover))
        }
        swipeRefresh.setOnRefreshListener {
            current = 1
            request(0)
        }
        adapter.setOnLoadListener {
            current++
            request(0)
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "resources/1?sortType=2&pageNo=$current&recentDay=30", SearchResData::class.java, object : OkResponse {
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