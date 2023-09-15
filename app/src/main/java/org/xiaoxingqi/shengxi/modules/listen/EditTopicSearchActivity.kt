package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_eidt_topic_search.*
import org.xiaoxingqi.shengxi.model.SearchTopicData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.wedgit.TagTextView

/**
 * 用于 发布声兮时,搜索话题
 */
class EditTopicSearchActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<SearchTopicData.SearchTopicBean>
    private lateinit var historyAdapter: QuickAdapter<String>
    private val mData by lazy {
        ArrayList<SearchTopicData.SearchTopicBean>()
    }
    private val historyData = ArrayList<String>()
    private val hotTag by lazy {
        ArrayList<SearchTopicData.SearchTopicBean>()
    }
    private var topicType = 0
    private var searchTag: String? = null
    private var current = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_eidt_topic_search
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        topicType = intent.getIntExtra("topicType", 0)
        adapter = object : QuickAdapter<SearchTopicData.SearchTopicBean>(this, R.layout.item_matcher_topic, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: SearchTopicData.SearchTopicBean?) {
                val tagText = helper!!.getView(R.id.tv_Topic) as TagTextView
                tagText.setData(item!!.topic_name, searchTag)
                if (item.isExtra) {
                    helper.getTextView(R.id.tv_Count).text = resources.getString(R.string.string_create_new_topic)
                } else {
                    if (topicType == 0)
                        helper.getTextView(R.id.tv_Count).text = String.format(resources.getString(R.string.string_topic_count, item.voice_num))
                    else {
                        helper.getTextView(R.id.tv_Count).text = String.format(resources.getString(R.string.string_art_topic_count, item.artwork_num))
                    }
                }
                if (helper.itemView.tag as Int == (mData.size - 1)) {
                    helper.getView(R.id.viewLine).visibility = View.GONE
                } else {
                    helper.getView(R.id.viewLine).visibility = View.VISIBLE
                }
            }
        }
        historyAdapter = object : QuickAdapter<String>(this, R.layout.item_histroy, historyData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper?.getTextView(R.id.tv_Tag)?.text = "#$item#"
                helper?.getView(R.id.iv_Delete)?.setOnClickListener {
                    //移除数据
                    updateHistory(item!!, true)
                }
            }
        }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter
        recyclerView.adapter = adapter
        getTag()
        getHistroy(3)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK, Intent().putExtra("topicBean", mData[position]))
            finish()
        }
        etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (AppTools.isEmptyEt(etContent, 0)) {
                    return@setOnEditorActionListener true
                }
                searchTag = etContent.text.toString().trim()
                if (searchTag!!.length >= 15) {
                    searchTag = searchTag!!.substring(0, 15)
                }
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(etContent.windowToken, 0)
                current = 1
                searchTag?.let {
                    updateHistory(it, false)
                }
                request(current)
            }
            false
        }
        tv_Cancel.setOnClickListener {
            if (mData.size == 0 && transLayout.visibility == View.GONE) {
                finish()
            } else {
                mData.clear()
                transLayout.visibility = View.GONE
                nestedHistory.visibility = View.VISIBLE
            }
        }
        flow_tables.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK, Intent().putExtra("topicBean", hotTag[position]))
            finish()
        }
        tv_More.setOnClickListener {
            if (tv_More.text.toString().trim() == resources.getString(R.string.string_clear_history)) {
                tv_More.text = resources.getString(R.string.string_more_history)
                SPUtils.setString(this, IConstant.TOPICTAG, "")
                getHistroy(3)
            } else {
                getHistroy(10)
                tv_More.text = resources.getString(R.string.string_clear_history)
            }
        }
        historyAdapter.setOnItemClickListener { _, position ->
            searchTag = historyData[position]
            etContent.setText(searchTag)
            request(0)
        }
    }

    /**
     * 获取历史记录
     */
    private fun getHistroy(limit: Int) {
        historyData.clear()
        val localTag = SPUtils.getString(this, IConstant.TOPICTAG, "").trim()
        val split = localTag.split("\n")//__
        for (index in split.indices) {
            if (TextUtils.isEmpty(split[index]))
                continue
            if (historyData.size >= limit)
                break
            historyData.add(split[index])
        }
        if (historyData.size > 0) {
            historyAdapter.notifyDataSetChanged()
            linearHistory.visibility = View.VISIBLE
        } else {
            linearHistory.visibility = View.GONE
        }
//        if (historyData.size < limit || limit == 10) {
//            tv_More.visibility = View.GONE
//        } else {
//            tv_More.visibility = View.VISIBLE
//        }

    }


    private fun updateHistory(tag: String, isMove: Boolean) {
        var localTag = SPUtils.getString(this, IConstant.TOPICTAG, "")
        if (!isMove) {
            val split = localTag.split("\n")
            for (str in split) {
                if (tag == str)
                    return
            }
            localTag = tag + "\n" + localTag
        } else {
            localTag = localTag.replace(tag, "")
        }
        SPUtils.setString(this, IConstant.TOPICTAG, localTag)
        getHistroy(3)
    }

    /**
     * 获取热门标签
     */
    private fun getTag() {
        OkClientHelper.get(this, "topics/hot/15", SearchTopicData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                if ((result as SearchTopicData).data != null && result.data.size > 0) {
                    hotTag.addAll(result.data)
                    for (bean in result.data) {
                        val view = View.inflate(this@EditTopicSearchActivity, R.layout.item_table_search, null)
                        view.findViewById<TextView>(R.id.text_inf).text = "#${bean.topic_name}#"
                        flow_tables.addView(view)
                    }
                }
            }

            override fun onFailure(any: Any?) {
            }
        })
    }

    override fun request(flag: Int) {
        transLayout.visibility = View.VISIBLE
        nestedHistory.visibility = View.GONE
        mData.clear()
        adapter.notifyDataSetChanged()
        OkClientHelper.get(this, "topics?topicName=$searchTag&topicType=$topicType", SearchTopicData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                (result as SearchTopicData)
                if (result.data != null) {
                    var isExact = false
                    for (bean in result.data) {
                        if (bean.topic_name == searchTag) {
                            isExact = true
                        }
                        mData.add(bean)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                    }
                    if (!isExact) {
                        mData.add(0, SearchTopicData.SearchTopicBean(searchTag, true))
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    mData.add(0, SearchTopicData.SearchTopicBean(searchTag, true))
                    adapter.notifyDataSetChanged()
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else {
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }
}