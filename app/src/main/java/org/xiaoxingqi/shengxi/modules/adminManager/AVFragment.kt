package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.frag_admin_recycler.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelMsg
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.AdminReportData
import org.xiaoxingqi.shengxi.model.BaseAdminReportBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.TestEnjoyCountData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.book.OneBookDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.OneMovieDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.music.OneMusicDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*

/**
 * 书籍  影评 曲评
 */
class AVFragment : BaseFragment(), ITabClickCall {

    private var lastId: String? = null
    private lateinit var adapter: QuickAdapter<BaseAdminReportBean>
    private val mData by lazy { ArrayList<BaseAdminReportBean>() }
    private var preReadTag: String = ""
    private var tagBean: BaseAdminReportBean? = null

    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_admin_recycler
    }

    override fun initView(view: View?) {

    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseAdminReportBean>(activity, R.layout.item_admin_av, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseAdminReportBean?) {
                helper!!.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserTimeMachine(activity, item!!.created_at) + "  " + when (item.resource_type) {
                    1 -> "影片词条"
                    2 -> "书籍词条"
                    3 -> "歌曲词条"
                    else -> ""
                }
                if (item.resource_type == 3) {
                    helper.getView(R.id.ivVideo).visibility = View.GONE
                    helper.getView(R.id.ivAudio).visibility = View.VISIBLE
                    helper.getView(R.id.tvType).visibility = View.GONE
                } else {
                    helper.getView(R.id.ivVideo).visibility = View.VISIBLE
                    helper.getView(R.id.ivAudio).visibility = View.GONE
                    helper.getView(R.id.tvType).visibility = View.VISIBLE
                }
                helper.getView(R.id.linear_tag).visibility = if (item.isReadTag) View.VISIBLE else View.GONE
                Glide.with(this@AVFragment)
                        .applyDefaultRequestOptions(RequestOptions().error(R.drawable.drawable_default_tmpry).signature(ObjectKey(when (item.resource_type) {
                            1 -> item.resource_detail.movie_poster
                            2 -> item.resource_detail.book_cover
                            else -> item.resource_detail.song_cover
                        })))
                        .load(when (item.resource_type) {
                            1 -> item.resource_detail.movie_poster
                            2 -> item.resource_detail.book_cover
                            else -> item.resource_detail.song_cover
                        })
                        .into(if (item.resource_type == 3) helper.getImageView(R.id.ivAudio) else helper.getImageView(R.id.ivVideo))
                helper.getTextView(R.id.tvVideoName).text = when (item.resource_type) {
                    1 -> item.resource_detail.movie_title
                    2 -> item.resource_detail.book_name
                    else ->  item.resource_detail.song_name
                }
                helper.getTextView(R.id.tvType).text = when (item.resource_type) {
                    1 -> AppTools.array2String(item.resource_detail.movie_type)
                    2 -> item.resource_detail.book_name
                    else -> ""
                }
                helper.getTextView(R.id.tvTime).text = when (item.resource_type) {
                    1 -> item.resource_detail.released_date.replace("00:00:00", "")
                    2 -> ""
                    else -> item.resource_detail.song_singer
                }
                helper.getTextView(R.id.tvSource).text = when (item.resource_type) {
                    1 -> item.resource_detail.movie_intro
                    2 -> item.resource_detail.book_intro
                    else -> item.resource_detail.album_name
                }
                helper.getView(R.id.tvUser).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                }
                helper.getView(R.id.tvItem).setOnClickListener {
                    startActivity(Intent(activity, when (item.resource_type) {
                        1 -> OneMovieDetailsActivity::class.java
                        2 -> OneBookDetailsActivity::class.java
                        else -> OneMusicDetailsActivity::class.java
                    }).putExtra("id", item.resource_id)
                            .putExtra("token", true)
                    )
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = preReadTag
            request(1)
        }
        adapter.setOnLoadListener {
            request(1)
        }
        adapter.setOnItemLongClickListener { _, position ->
            DialogCancelMsg(activity!!).setTitle("设置审阅到此").setOnClickListener(View.OnClickListener {
                setReadTag(if (position > 0) mData[position - 1] else mData[position], position)
            }).show()
        }
    }

    /**
     * 设置审阅点
     */
    private fun setReadTag(bean: BaseAdminReportBean, position: Int) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "admin/${loginBean.user_id}/point", FormBody.Builder().add("pointKey", "entryPoint")
                .add("pointValue", bean.id).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tagBean?.let {
                        it.isReadTag = false
                    }
                    preReadTag = bean.id
                    val indexOf = mData.indexOf(bean)
                    tagBean = if (indexOf + 1 < mData.size - 1 && position != 0) {
                        mData[indexOf + 1].isReadTag = true
                        adapter.notifyDataSetChanged()
                        mData[indexOf + 1]
                    } else {
                        bean.isReadTag = true
                        adapter.notifyDataSetChanged()
                        bean
                    }
                    showToast("设置成功")
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }


    override fun request(flag: Int) {

        when (flag) {
            0 -> {
                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(activity, "admin/${loginBean.user_id}/point/entryPoint", TestEnjoyCountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as TestEnjoyCountData
                        if (result.data != null) {
                            if (!TextUtils.isEmpty(result.data.pointValue)) {
                                preReadTag = result.data.pointValue
                                lastId = preReadTag
                            }
                        }
                        request(1)
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            1 -> {
                OkClientHelper.get(activity, "admin/entries?lastId=$lastId&token=${SPUtils.getString(activity, IConstant.ADMINTOKEN, "")}", AdminReportData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as AdminReportData
                        if (result.code == 0 && result.data != null) {
                            if (preReadTag == lastId) {
                                mData.clear()
                                result.data[0].isReadTag = true
                                tagBean = result.data[0]
                            }
                            mData.addAll(result.data)
                            adapter.notifyDataSetChanged()
                            lastId = mData[mData.size - 1].id
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    }
                })
            }
        }

    }
}