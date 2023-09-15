package org.xiaoxingqi.shengxi.modules.listen.movies

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_movie_list.*
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
import org.xiaoxingqi.shengxi.utils.TimeUtils
import skin.support.SkinCompatManager

/**
 * 电影榜单界面
 */
class MovieListActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private var current = 1
    private val mData by lazy {
        ArrayList<BaseSearchBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_movie_list
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseSearchBean>(this, R.layout.item_movie_board, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                helper!!.getTextView(R.id.tv_Score).text = "${item!!.movie_score / 10f}"
                Glide.with(this@MovieListActivity)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                        .load(item!!.movie_poster)
                        .into(helper?.getImageView(R.id.ivMovieCover))
                try {
                    if (item.released_at != 0 || TimeUtils.string2Long(item.released_date) == 0) {
                        val paserYyMm = TimeUtils.getInstance().paserYyMm(item.released_at)
                        if (paserYyMm.contains("-")) {
                            helper!!.getTextView(R.id.tv_Years).text = "（${paserYyMm.substring(0, paserYyMm.indexOf("-"))}）"
                        } else {
                            helper!!.getTextView(R.id.tv_Years).text = "（$paserYyMm）"
                        }
                    } else {
                        helper.getTextView(R.id.tv_Years).text = "（${item.released_date.substring(0, item.released_date.indexOf("-"))}）"
                    }
                } catch (e: Exception) {
                }
                helper!!.getTextView(R.id.tv_MovieName).text = item?.movie_title
                val starring = AppTools.array2String(item.movie_area)
                helper.getTextView(R.id.tv_MovieType).text = if (!TextUtils.isEmpty(starring)) {
                    starring + "/" + AppTools.array2String(item.movie_type) + "/" + item.movie_len + resources.getString(R.string.string_second)
                } else {
                    AppTools.array2String(item.movie_type) + "/" + item.movie_len + resources.getString(R.string.string_second)
                }
                helper.getTextView(R.id.tv_actors).text = AppTools.array2String(item.movie_starring)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(current)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { view, position ->
            OneMovieDetailsActivity.start(this, mData[position].movie_poster, mData[position].id, view!!.findViewById(R.id.ivMovieCover))
        }
        adapter.setOnLoadListener {
            current++
            request(current)
        }

    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "leaderboard/resources/1?pageNo=$flag", SearchResData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchResData
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
        }, "V3.2")
    }
}