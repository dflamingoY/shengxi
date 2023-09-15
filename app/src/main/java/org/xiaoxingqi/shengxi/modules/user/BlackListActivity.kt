package org.xiaoxingqi.shengxi.modules.user

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_black_list.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.modules.user.black.BlackFragment
import org.xiaoxingqi.shengxi.modules.user.black.WhiteListFragment

class BlackListActivity : BaseAct() {
    private val fragments = getFragment()
    private lateinit var titles: Array<String>
    private fun getFragment() = arrayListOf<Fragment>().apply {
        for (index in 1..2) {
            val frag = BlackFragment()
            val bundle = Bundle()
            bundle.putInt("key", index)
            frag.arguments = bundle
            add(frag)
        }
        add(WhiteListFragment())
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_black_list
    }

    override fun initView() {

    }

    override fun initData() {
        titles = arrayOf(resources.getString(R.string.string_BlackAct_2), "善意屏蔽", resources.getString(R.string.string_white_list))
        viewPager.adapter = BlackAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }

    private inner class BlackAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

}