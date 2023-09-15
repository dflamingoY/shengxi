package org.xiaoxingqi.shengxi.modules.listen.movies

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_recycler.view.*
import kotlinx.android.synthetic.main.movie_socre_user_list_head.view.*
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.wedgit.MovieUserScoreView

/**
 * 影评榜用户排行
 */
class MovieScoreListFragment : BaseFragment() {
    private lateinit var headView: View
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private lateinit var recyclerView: RecyclerView
    private lateinit var movieRanking1: MovieUserScoreView
    private lateinit var movieRanking2: MovieUserScoreView
    private lateinit var movieRanking3: MovieUserScoreView
    private var current = 1
    private val mData by lazy {
        ArrayList<BaseUserBean>()
    }
    private val tempData by lazy {
        ArrayList<BaseUserBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_recycler
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        headView = LayoutInflater.from(activity).inflate(R.layout.movie_socre_user_list_head, recyclerView, false)
        movieRanking1 = headView.movieRanking1
        movieRanking2 = headView.movieRanking2
        movieRanking3 = headView.movieRanking3
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseUserBean>(activity, R.layout.item_score_user_list, mData, headView) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                helper!!.getTextView(R.id.tv_No).text = ((helper.itemView.tag as Int) + 4).toString()
//                GlideUtil.load(activity, item!!.avatar_url, helper.getImageView(R.id.iv_img), R.mipmap.icon_user_default)
                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.nick_name
                helper.getTextView(R.id.tv_ScoreCount).text = String.format(resources.getString(R.string.string_movies_connversion_count), item.movie_voice)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, recyclerView, false))
        request(current)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            current++
            request(current)
        }

        adapter.setOnItemClickListener { view, position ->
            startActivity(Intent(activity, UserDetailsActivity::class.java)
                    .putExtra("id", mData[position].user_id)
                    .putExtra("url", mData[position].avatar_url))
        }
        movieRanking1.setOnClickListener {
            startActivity(Intent(activity, UserDetailsActivity::class.java)
                    .putExtra("id", tempData[0].user_id)
                    .putExtra("url", tempData[0].avatar_url))
        }
        movieRanking2.setOnClickListener {
            startActivity(Intent(activity, UserDetailsActivity::class.java)
                    .putExtra("id", tempData[1].user_id)
                    .putExtra("url", tempData[1].avatar_url))
        }
        movieRanking3.setOnClickListener {
            startActivity(Intent(activity, UserDetailsActivity::class.java)
                    .putExtra("id", tempData[2].user_id)
                    .putExtra("url", tempData[2].avatar_url))
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "leaderboard/ratings/movies?pageNo=$flag", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (flag == 1) {
                        if (result.data.size > 3) {
                            mData.addAll(result.data.subList(3, result.data.size))
                            tempData.addAll(result.data.subList(0, 3))
                        } else {
                            tempData.addAll(result.data)
                        }
                        for (bean in tempData.indices) {
                            if (bean == 0) {
                                movieRanking1.setData(glideUtil, tempData[0].avatar_url, tempData[0].nick_name, tempData[0].movie_voice)
                            } else if (bean == 1) {
                                movieRanking2.setData(glideUtil, tempData[1].avatar_url, tempData[1].nick_name, tempData[1].movie_voice)
                            } else {
                                movieRanking3.setData(glideUtil, tempData[2].avatar_url, tempData[2].nick_name, tempData[2].movie_voice)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        for (bean in result.data) {
                            mData.add(bean)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }

            override fun onFailure(any: Any?) {


            }
        })

    }
}