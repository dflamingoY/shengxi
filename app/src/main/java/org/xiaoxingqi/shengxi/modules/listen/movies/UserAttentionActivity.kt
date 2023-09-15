package org.xiaoxingqi.shengxi.modules.listen.movies

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.activity_home_user_movies.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity

/**
 * 我的关注列表
 */
class UserAttentionActivity : BaseAct() {
    private lateinit var title: Array<String>
    private val fragments = arrayOf(AttentionFragment(), AttentionFragment(), AttentionFragment())
    private var current = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_home_user_movies
    }

    override fun initView() {
        tvTitle.text = resources.getString(R.string.string_user_attention)
        relative_Custom.visibility = View.VISIBLE
    }

    override fun initData() {
        current = intent.getIntExtra("current", 0)
        title = arrayOf("电影", "书籍", "唱回忆")
        (fragments.indices).forEach {
            fragments[it].arguments = Bundle().apply {
                putInt("type", it + 1)
            }
        }
        viewPager.adapter = AttentionAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
        viewPager.setCurrentItem(current, false)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_Custom.setOnClickListener {
            startActivity<ChatActivity>("uid" to "1", "userName" to "声昔小二")
        }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                fragments.indices.forEach {
                    (fragments[it] as ITabClickCall).tabClick(it == position)
                }
            }
        })
    }

    private inner class AttentionAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
            return fragments[p0]
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }
    }

}