package org.xiaoxingqi.shengxi.modules.user.userResource

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.frag_user_music.view.*
import kotlinx.android.synthetic.main.view_empty_user_frag_music.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.SearchResData
import org.xiaoxingqi.shengxi.model.UserMoviesData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.music.MusicActivity
import org.xiaoxingqi.shengxi.modules.listen.music.OneMusicDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import skin.support.SkinCompatManager

class WishSongFragment : BaseFragment() {
    private val mData by lazy { ArrayList<BaseSearchBean>() }
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private var lastId: String? = ""
    private var userId: String? = null
    private var type = 2
    override fun getLayoutId(): Int {
        return R.layout.frag_user_music
    }

    override fun initView(view: View?) {
        view!!.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators),
                ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
        view.transLayout.findViewById<View>(R.id.frameHomeUserResource).visibility = View.VISIBLE

    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)

        adapter = object : QuickAdapter<BaseSearchBean>(activity, R.layout.item_search_music, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                Glide.with(this@WishSongFragment)
                        .applyDefaultRequestOptions(RequestOptions()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .centerCrop())
                        .load(item?.song_cover)
                        .into(helper!!.getImageView(R.id.ivMovieCover))
                helper.getTextView(R.id.tvMovieName).text = item!!.song_name
                helper.getTextView(R.id.tv_MovieType).text = "${item.song_singer}/${item.album_name}"
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        arguments?.let {
            userId = it.getString("userId")
            type = it.getInt("type")
            val permission = it.getInt("permission")
            mView!!.transLayout.tvDesc.text = if (loginBean.user_id != userId) {
                if (it.getInt("relation") != 2 && permission == 2) {//陌生人且设置了非好友限制
                    mView!!.transLayout.showEmpty()
                    resources.getString(R.string.string_resource_empty_12)
                } else {
                    request(0)
                    if (type == 1)
                        "ta${resources.getString(R.string.string_resource_empty_5)}"
                    else
                        "ta${resources.getString(R.string.string_resource_empty_6)}"
                }
            } else {
                request(0)
                if (type == 1) resources.getString(R.string.string_resource_empty_5)
                else resources.getString(R.string.string_resource_empty_6)
            }
            if (type == 1) {
                mView!!.transLayout.findViewById<TextView>(R.id.tv_UploadPhoto).text = "标记想听的歌"
            } else {
                mView!!.transLayout.findViewById<TextView>(R.id.tv_UploadPhoto).text = "标记听过的歌曲"
            }
        }
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            startActivity<OneMusicDetailsActivity>("id" to mData[position].song_id)
        }
        adapter.setOnLoadListener {
            request(0)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        mView!!.transLayout.findViewById<View>(R.id.tv_UploadPhoto).setOnClickListener {
            startActivity<MusicActivity>()
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "users/${userId}/resourceSubscription?resourceType=3&subscriptionType=$type&lastId=$lastId", UserMoviesData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as UserMoviesData
                        if (result.code == 0) {
                            result.data?.list?.let {
                                if (TextUtils.isEmpty(lastId)) {
                                    mView!!.tv_AllCount.text = "共${result.data.total}首"
                                    mData.clear()
                                }
                                mData.addAll(it)
                                adapter.notifyDataSetChanged()
                                if (it.size >= 10) {
                                    lastId = it[it.size - 1].id
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            }
                        }
                        if (mData.size == 0) {
                            mView!!.tv_AllCount.visibility = View.GONE
                            mView!!.transLayout.showEmpty()
                        } else {
                            mView!!.transLayout.showContent()
                            mView!!.tv_AllCount.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }
                }, "V4.2")
            }
            1 -> {
                OkClientHelper.get(activity, "users/${userId}/resourceSubscription?resourceType=3&subscriptionType=$type&lastId=$lastId", SearchResData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as SearchResData
                        if (result.code == 0) {
                            result.data?.let {
                                if (TextUtils.isEmpty(lastId)) {
                                    mData.clear()
                                }
                                mData.addAll(it)
                                adapter.notifyDataSetChanged()
                                if (it.size >= 10) {
                                    lastId = it[it.size - 1].id
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            }
                        }
                        if (mData.size == 0) {
                            mView!!.transLayout.showEmpty()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }
                }, "V4.2")
            }
        }


    }
}