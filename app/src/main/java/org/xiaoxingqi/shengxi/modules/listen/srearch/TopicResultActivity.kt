package org.xiaoxingqi.shengxi.modules.listen.srearch

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_topic_result.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.utils.AppTools

class TopicResultActivity : BaseAct() {
    private var searchTag: String? = null
    private var searchTagId: String? = ""
    private val fragments = arrayOf(TopicSearchFragment(), TopicSearchUserFragment())
    private val titles = arrayOf("话题", "用户")

    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeMoodEvent(type))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_topic_result
    }

    override fun initView() {

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            searchTag = intent.getStringExtra("tag")
        }
        intent?.let {
            val tag = it.getStringExtra("tagId")
            if (TextUtils.isEmpty(tag)) {
                searchTagId = "0"
            }
        }
    }

    override fun initData() {
        viewPager.adapter = SearchPager(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
        searchTag = intent.getStringExtra("tag")
        searchTagId = intent.getStringExtra("tagId")
        if (TextUtils.isEmpty(searchTagId)) {
            searchTagId = "0"
        }
        tv_Content.setText(searchTag)
        fragments.forEach {
            (it as ISearchTag).setTag(searchTag, false)
        }
    }

    override fun initEvent() {
        tv_Content.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (AppTools.isEmptyEt(tv_Content, 0)) {
                    return@setOnEditorActionListener true
                }
                val searchTag = tv_Content.text.toString().trim()
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(tv_Content.windowToken, 0)
                fragments.forEach {
                    (it as ISearchTag).setTag(if (searchTag.length > 15) {
                        searchTag.substring(0, 15)
                    } else searchTag, true)
                }
            }
            false
        }
        btn_Back.setOnClickListener { finish() }
        linearSearch.setOnClickListener {
            startActivity(Intent(this, TopicSearchActivity::class.java))
            overridePendingTransition(R.anim.search_act_enter, R.anim.search_act_out)
            finish()
        }
        ivRecord.setOnClickListener {
            if (!RecordTransparentActivity.isOnCreate) {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("type", 3)
                        .putExtra("topicName", searchTag)
                        .putExtra("topicId", if (searchTagId == "0") "" else searchTagId))
                overridePendingTransition(0, 0)
            }
        }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                ivRecord.visibility = if (position == 0) View.VISIBLE else View.GONE
                fragments.forEach {
                    (it as ISearchTag).visible(position)
                }
            }
        })
    }

    private inner class SearchPager(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

        override fun getItem(p0: Int): Fragment {
            return fragments[p0] as Fragment
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

}