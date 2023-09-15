package org.xiaoxingqi.shengxi.modules.listen.movies

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.frag_movie_board.view.*
import org.xiaoxingqi.shengxi.model.MovieData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.TimeUtils

/**
 * 电影总榜的榜单
 */
class MovieBoardFragment : BaseFragment() {
    private lateinit var adapter: QuickAdapter<MovieData.MovieBean>
    private lateinit var recyclerView: RecyclerView
    private var current = 1
    private val mData by lazy {
        ArrayList<MovieData.MovieBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_movie_board
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun initData() {
        adapter = object : QuickAdapter<MovieData.MovieBean>(activity, R.layout.item_movie_board, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: MovieData.MovieBean?) {
                helper!!.getTextView(R.id.tv_Score).text = "${item!!.movie_score / 10f}分"
                Glide.with(this@MovieBoardFragment)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop())
                        .load(item!!.movie_poster)
                        .into(helper?.getImageView(R.id.ivMovieCover))
                try {
                    val paserYyMm = TimeUtils.getInstance().paserYyMm(item.released_at.toInt())
                    if (paserYyMm.contains("-")) {
                        helper!!.getTextView(R.id.tv_Years).text = "（${paserYyMm.substring(0, paserYyMm.indexOf("-"))}）"
                    } else {
                        helper!!.getTextView(R.id.tv_Years).text = "（$paserYyMm）"
                    }
                } catch (e: Exception) {
                }
                helper!!.getTextView(R.id.tv_MovieName).text = item?.movie_title
                val starring = AppTools.array2String(item.movie_starring)
                helper.getTextView(R.id.tv_MovieType).text = if (!TextUtils.isEmpty(starring)) {
                    starring + "/" + AppTools.array2String(item.movie_type) + "/" + item.movie_len + "分"
                } else {
                    AppTools.array2String(item.movie_type) + "/" + item.movie_len + "分"
                }
                helper.getTextView(R.id.tv_actors).text = AppTools.array2String(item.movie_area)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, recyclerView, false))
        request(current)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { view, position ->
            OneMovieDetailsActivity.start(activity!!, mData[position].movie_poster, mData[position].movie_id, view!!.findViewById(R.id.ivMovieCover))
        }
        adapter.setOnLoadListener {
            current++
            request(current)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "leaderboard/movies?pageNo=$flag", MovieData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as MovieData
                if (result.data != null)
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                    }
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result.data?.let {
                    if (it.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }

            override fun onFailure(any: Any?) {


            }
        })
    }
}