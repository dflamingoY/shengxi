package org.xiaoxingqi.shengxi.modules.adminManager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.activity_admin_manager.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.SensorChangeAdminEvent
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class ManagerAdminActivity : BaseAct() {
    private lateinit var titles: Array<String>
    private val fragments = arrayOf(CheckFragment(), UserEchoFragment(), UserChatFragment(), ManagerWorldFragment(),
            ReportDetailsFragment(), ReportDetailsFragment(), ReportDetailsFragment(), AVFragment(), SensitiveFragment())

    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeAdminEvent(type))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_admin_manager
    }

    override fun initView() {
//        val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//        if ("1" == login.user_id) {
//            tvText.visibility = View.VISIBLE
//        }
    }

    override fun initData() {
        fragments.forEach {
            if (it is ReportDetailsFragment) {
                val indexOf = fragments.indexOf(it)
                val bundle = Bundle()
                bundle.putInt("state", indexOf - 1)
                it.arguments = bundle
            }
        }
        titles = arrayOf("审核中心", resources.getString(R.string.string_admin_manager_1), resources.getString(R.string.string_admin_manager_2), "世界", "举报待处理", "举报待调查", "举报已处理", "书音影", "配音敏感词库")
        viewPager.adapter = ManagerPager(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
    }

    override fun initEvent() {
        tvText.setOnClickListener {
            startActivity<TestAudioStatusActivity>()
        }
        btn_Back.setOnClickListener { finish() }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                for (tab in fragments.indices) {
                    if (tab == position) {
                        (fragments[tab] as ITabClickCall).tabClick(true)
                    } else {
                        (fragments[tab] as ITabClickCall).tabClick(false)
                    }
                }
            }
        })
    }

    private inner class ManagerPager(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position] as Fragment
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}