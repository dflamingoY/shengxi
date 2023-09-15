package org.xiaoxingqi.shengxi.modules.nearby

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_search.*
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.AppTools

class SearchNearbyActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private var searchTag: String? = null
    private val mData by lazy {
        ArrayList<BaseUserBean>()
    }
    private var lastId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_search
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_search_user, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                helper!!.getTextView(R.id.tv_UserName).text = item!!.nick_name
//                GlideUtil.load(this@SearchNearbyActivity, item?.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default)
                glideUtil.loadGlide(item?.avatar_url, helper.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item?.avatar_url))
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { view, position ->
            UserDetailsActivity.start(this, mData[position].avatar_url, mData[position].user_id, view.findViewById(R.id.iv_img))
        }
        tvSearch.setOnClickListener {
            if (AppTools.isEmptyEt(etContent, 0)) {
                return@setOnClickListener
            }
            searchTag = etContent.text.toString().trim()
            if (!TextUtils.isEmpty(searchTag)) {
                linearNull.visibility = View.GONE
                transLayout.visibility = View.VISIBLE
                mData.clear()
                adapter.notifyDataSetChanged()
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(etContent.windowToken, 0)
                lastId = null
                request(0)
            }
        }
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    tv_Cancel.visibility = View.VISIBLE
                    if (linearNull.visibility == View.VISIBLE) {
                        linearNull.visibility = View.GONE
                    }
                } else {
                    tv_Cancel.visibility = View.GONE
                }
            }
        })
        tv_Cancel.setOnClickListener {
            linearNull.visibility = View.VISIBLE
            transLayout.visibility = View.GONE
            etContent.setText("")
            mData.clear()
            adapter.notifyDataSetChanged()
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener { request(0) }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "users/search?searchValue=$searchTag&lastId=$lastId", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemChanged(adapter.itemCount - 1)
                    }
                    lastId = mData[mData.size - 1].user_id
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showOffline()
            }
        })

    }
}