package org.xiaoxingqi.shengxi.modules.user.userResource

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_home_user_movies.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.SensorChangeMoodEvent
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class HomeUserBookActivity : BaseAct() {
    private val fragments = arrayListOf(MoodBooksFragment(), WishBookFragment(), WishBookFragment())
    private lateinit var titles: Array<String>

    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeMoodEvent(type))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_home_user_movies
    }

    override fun initView() {
        tvTitle.text = "我的书籍"
    }

    override fun initData() {
        val userId = intent.getStringExtra("userId")
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id != userId) {
            tvTitle.text = resources.getString(R.string.string_title_7)
        }
        fragments.forEach {
            it.arguments = Bundle().apply {
                putString("userId", userId)
                putString("title", intent.getStringExtra("title"))
                putInt("relation", intent.getIntExtra("relation", 0))
                putInt("permission", intent.getIntExtra("permission", 1))
                putInt("type", fragments.indexOf(it))
            }
        }
        titles = arrayOf("心情", resources.getString(R.string.string_book_wish), resources.getString(R.string.string_book_readed))
        viewPager.adapter = MoviesAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }

    private inner class MoviesAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(p0: Int): Fragment {
            return fragments[p0]
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}