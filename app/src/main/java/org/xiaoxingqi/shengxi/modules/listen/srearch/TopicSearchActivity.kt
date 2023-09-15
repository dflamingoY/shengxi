package org.xiaoxingqi.shengxi.modules.listen.srearch

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_topic_search.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.model.SearchTopicData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.SearchTopicEvent
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils

class TopicSearchActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<String>
    private val hotTagList by lazy {
        ArrayList<SearchTopicData.SearchTopicBean>()
    }

    private val mData by lazy {
        ArrayList<String>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_topic_search
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        val tagString = intent.getStringExtra("tag")
        if (!TextUtils.isEmpty(tagString)) {
            if (resources.getString(R.string.string_search_hot_topic) != tagString) {
                etContent.hint = tagString
            }
        }
        adapter = object : QuickAdapter<String>(this, R.layout.item_histroy, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper?.getTextView(R.id.tv_Tag)?.text = item
                helper?.getView(R.id.iv_Delete)?.setOnClickListener {
                    updateHistory(item!!, true)
                }
            }
        }
        recyclerView.adapter = adapter
        getHistroy(3)
        request(1)
    }

    override fun initEvent() {
        etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (AppTools.isEmptyEt(etContent, 0) && resources.getString(R.string.string_search_hot_topic) == etContent.hint.toString()) {
                    return@setOnEditorActionListener true
                }
                val searchTag = etContent.text.toString().trim()
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(etContent.windowToken, 0)
                searchTag.let {
                    updateHistory(if (it.length > 15) {
                        it.substring(0, 15)
                    } else it, false)
                    startActivity(Intent(this@TopicSearchActivity, TopicResultActivity::class.java)
                            .putExtra("tag", if (AppTools.isEmptyEt(etContent, 0) && resources.getString(R.string.string_search_hot_topic) != etContent.hint.toString()) etContent.hint.toString() else if (it.length > 15) {
                                it.substring(0, 15)
                            } else it)
                    )
                }
            }
            false
        }
        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ivClear.visibility = if (s?.length!! > 0) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        flow_tables.setOnItemClickListener { _, position ->
            startActivity(Intent(this, TopicResultActivity::class.java)
                    .putExtra("tagId", hotTagList[position].topic_id)
                    .putExtra("tag", hotTagList[position].topic_name)
            )
        }
        tv_More.setOnClickListener {
            if (tv_More.text.toString().trim() == resources.getString(R.string.string_clear_history)) {
                tv_More.text = resources.getString(R.string.string_more_history)
                SPUtils.setString(this, IConstant.TOPIC, "")
                getHistroy(3)
            } else {
                getHistroy(10)
                tv_More.text = resources.getString(R.string.string_clear_history)
            }
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(this, TopicResultActivity::class.java).putExtra("tag", mData[position]))
        }
        tv_Cancel.setOnClickListener { finish() }
        ivClear.setOnClickListener {
            etContent.setText("")
        }
    }

    /**
     * 获取历史记录
     */
    private fun getHistroy(limit: Int) {
        mData.clear()
        val localTag = SPUtils.getString(this, IConstant.TOPIC, "")
        val split = localTag.split("__".toRegex())
        for (index in split.indices) {
            if (TextUtils.isEmpty(split[index]))
                continue
            if (mData.size >= limit)
                break
            mData.add(split[index])
        }
        if (mData.size > 0) {
            adapter.notifyDataSetChanged()
            linearHistory.visibility = View.VISIBLE
        } else {
            linearHistory.visibility = View.GONE
        }
        /*if (mData.size < limit ) {
            tv_More.visibility = View.GONE
        } else {
            tv_More.visibility = View.VISIBLE
        }*/
    }

    private fun updateHistory(tag: String, isMove: Boolean) {
        var localTag = SPUtils.getString(this, IConstant.TOPIC, "")
        localTag = if (!isMove) {
            val split = localTag.split("__".toRegex())
            for (str in split) {
                if (tag == str)
                    return
            }
            tag + "__" + localTag
        } else {
            localTag.replace(tag, "")
        }
        SPUtils.setString(this, IConstant.TOPIC, localTag)
        getHistroy(3)
    }

    /**
     * 查询热门标签
     */
    override fun request(flag: Int) {
        OkClientHelper.get(this, "topics/hot/15", SearchTopicData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as SearchTopicData).data != null && result.data.size > 0) {
                    hotTagList.addAll(result.data)
                    for (bean in result.data) {
                        val view = View.inflate(this@TopicSearchActivity, R.layout.item_table_search, null)
                        view.findViewById<TextView>(R.id.text_inf).text = "#${bean.topic_name}#"
                        flow_tables.addView(view)
                    }
                }
            }

            override fun onFailure(any: Any?) {
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateHistory(event: SearchTopicEvent) {
        updateHistory(event.tag, false)
    }

}