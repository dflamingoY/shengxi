package org.xiaoxingqi.shengxi.modules.listen.music

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_user_movies.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.BaseFragment

class UserMusicListActivity : BaseAct() {
    private val moviesFrags = arrayOfNulls<BaseFragment>(4)
    private lateinit var titles: Array<String>
    private val scores = arrayOf("", "200", "150", "100")
    override fun getLayoutId(): Int {
        return R.layout.activity_user_movies
    }

    override fun initView() {

    }

    override fun initData() {
        titles = arrayOf("全部", resources.getString(R.string.string_music_10), resources.getString(R.string.string_music_11), resources.getString(R.string.string_music_12))
        val uid = intent.getStringExtra("uid")
        for (a in 0..3) {
            val fragment = UserMusicFragment()
            val bundle = Bundle()
            bundle.putString("score", scores[a])
            bundle.putString("uid", uid)
            fragment.arguments = bundle
            moviesFrags[a] = fragment
        }
        viewPager.adapter = MoviesAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
    }

    private inner class MoviesAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return moviesFrags[position]!!
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}