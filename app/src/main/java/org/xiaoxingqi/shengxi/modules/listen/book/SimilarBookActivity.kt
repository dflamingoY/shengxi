package org.xiaoxingqi.shengxi.modules.listen.book

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import kotlinx.android.synthetic.main.activity_similar_book.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.modules.listen.movies.SimilarCommonMoviesActivity

class SimilarBookActivity : BaseAct() {
    private val mData by lazy { ArrayList<BaseUserBean>() }
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    override fun getLayoutId(): Int {
        return R.layout.activity_similar_book
    }

    override fun initView() {
        tv_Title.text = resources.getString(R.string.string_book_6)
        tv_Hint.text = resources.getString(R.string.string_book_13)
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_similar_movies, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_Avatar), 0, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.nick_name
                helper.getTextView(R.id.tv_Common_num).text = Html.fromHtml(String.format(resources.getString(R.string.string_book_14), item.common_num))
                val position = helper.itemView.tag as Int
                helper.getView(R.id.viewLine).visibility = if (position == mData.size - 1) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(this, SimilarCommonMoviesActivity::class.java)
                    .putExtra("resourceType", 2)
                    .putExtra("uid", mData[position].id)
                    .putExtra("nickName", mData[position].nick_name))
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "hobby/common/2", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                if (result.data != null) {
                    for (item in result.data) {
                        mData.add(item)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else {
                    tv_Similar_hint.visibility = View.VISIBLE
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V3.2")
    }
}