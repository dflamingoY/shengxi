package org.xiaoxingqi.shengxi.modules.listen.book

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_search_movies.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils

class BookSearchActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<String>
    private val mData by lazy {
        ArrayList<String>()
    }

    override fun onResume() {
        super.onResume()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etContent, InputMethodManager.SHOW_FORCED)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_movies
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        etContent.hint = resources.getString(R.string.string_search_books_or_author)
    }

    override fun initData() {
        adapter = object : QuickAdapter<String>(this, R.layout.item_histroy, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper?.getTextView(R.id.tv_Tag)?.text = item
                helper?.getView(R.id.iv_Delete)?.setOnClickListener {
                    updateHistory(item!!, true)
                }
            }
        }
        recyclerView.adapter = adapter
        getHistory(3)
    }

    override fun initEvent() {
        tv_Cancel.setOnClickListener { finish() }

        tv_More.setOnClickListener {
            getHistory(10)
        }
        etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (AppTools.isEmptyEt(etContent, 0)) {
                    return@setOnEditorActionListener true
                }
                val searchTag = etContent.text.toString().trim()
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(etContent.windowToken, 0)
                searchTag?.let {
                    updateHistory(searchTag, false)
                    startActivity(Intent(this@BookSearchActivity, BookSearchResultActivity::class.java).putExtra("tag", searchTag))
                }
            }
            false
        }
        adapter.setOnItemClickListener { view, position ->
            startActivity(Intent(this@BookSearchActivity, BookSearchResultActivity::class.java).putExtra("tag", mData[position]))
        }
    }

    /**
     * 获取历史记录
     */
    private fun getHistory(limit: Int) {
        mData.clear()
        val localTag = SPUtils.getString(this, IConstant.BOOK, "")
        val split = localTag.split("__".toRegex())
        for (index in split.indices) {
            if (TextUtils.isEmpty(split[index]))
                continue
            if (mData.size >= limit)
                break
            mData.add(split[index])
        }
        adapter.notifyDataSetChanged()
        if (mData.size < limit || limit == 10) {
            tv_More.visibility = View.GONE
        } else {
            tv_More.visibility = View.VISIBLE
        }
    }

    private fun updateHistory(tag: String, isMove: Boolean) {
        var localTag = SPUtils.getString(this, IConstant.BOOK, "")
        if (!isMove) {
            val split = localTag.split("__".toRegex())
            for (str in split) {
                if (tag == str)
                    return
            }
            localTag = tag + "__" + localTag
        } else {
            localTag = localTag.replace(tag, "")
        }
        SPUtils.setString(this, IConstant.BOOK, localTag)
        getHistory(3)
    }
}