package org.xiaoxingqi.shengxi.modules.listen.book

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_search_book_result.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.AddResourceEvent
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.SearchResData
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddBookItemActivity
import org.xiaoxingqi.zxing.activity.ScanActivity
import skin.support.SkinCompatManager

class BookSearchResultActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private var lastId: String? = ""
    private lateinit var loadMoreView: View
    private val mData by lazy {
        ArrayList<BaseSearchBean>()
    }
    private var searchTag: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_search_book_result
    }

    override fun initView() {
        swipeRefresh.isEnabled = false
    }

    override fun initData() {
        searchTag = intent.getStringExtra("tag")
        et_Content.text = searchTag
        adapter = object : QuickAdapter<BaseSearchBean>(this, R.layout.item_search_movie, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                Glide.with(this@BookSearchResultActivity)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                        .load(item?.book_cover)
                        .into(helper!!.getImageView(R.id.ivMovieCover))
                helper!!.getTextView(R.id.tvMovieName).text = item!!.book_name
                helper.getTextView(R.id.tv_MovieType).text = "${item.book_score / 10f}/${item.book_author}"
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadMoreView = LayoutInflater.from(this).inflate(R.layout.view_loadmore_search, recyclerView, false)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadMoreView)
        loadMoreView.findViewById<TextView>(R.id.tv_hint).text = resources.getString(R.string.string_manual_no_books)
        loadMoreView.findViewById<TextView>(R.id.tv_create_item).text = resources.getString(R.string.string_manual_book_add)
        request(0)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _: View?, position: Int ->
            startActivity(Intent(this, OneBookDetailsActivity::class.java).putExtra("id", mData[position].id))
        }
        linearBack.setOnClickListener {
            finish()
        }
        btn_Back.setOnClickListener { finish() }
        tvCancel.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(1)
        }
        transLayout.findViewById<View>(R.id.tv_custom_server).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).putExtra("uid", "1"))
        }
        transLayout.findViewById<View>(R.id.tv_CustomChat).setOnClickListener {
            startActivity(Intent(this, AddBookItemActivity::class.java).putExtra("name", searchTag))
        }
        transLayout.findViewById<View>(R.id.tvScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        loadMoreView.findViewById<View>(R.id.tv_create_item).setOnClickListener {
            startActivity(Intent(this, AddBookItemActivity::class.java).putExtra("name", searchTag))
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "resources/2/search/$searchTag?lastId=$lastId", SearchResData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchResData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result.data?.let {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemChanged(adapter.itemCount - 1)
                    }
                    lastId = mData[mData.size - 1].id
                    if (it.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                if (result.data == null || result.data.size < 10) {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showOffline()
            }
        }, "V3.2")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun addEvent(event: AddResourceEvent) {
        if (event.type == 2) {
            if (mData.size == 0) {
                transLayout.showContent()
            }
            mData.add(event.bean)
            adapter.notifyItemInserted(adapter.itemCount - 1)
        }
    }

}