package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.frag_dubbing_parent.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.impl.IAlarmTabCall
import org.xiaoxingqi.shengxi.impl.ITabAlarmClickCall
import org.xiaoxingqi.shengxi.impl.OnAlarmItemClickListener

/*
 *配音父容器
 */
class DubbingParentFragment : BaseFragment(), ITabAlarmClickCall {
    private val frags = arrayListOf(DubFragment(), DubbingUserFragment())
    private var allDubbingCount = 0
    private var userDubbingCount = 0
    override fun tabClick(isVisible: Boolean) {
        //调全部 全部
        if (view!!.viewPagerDub.currentItem != 0) {
            view!!.viewPagerDub.setCurrentItem(0, false)
        } else {
            (frags[mView!!.viewPagerDub.currentItem] as IAlarmTabCall).itemTabSelected(mView!!.viewPagerDub.currentItem, true, 0)
        }
        if (mView!!.headItemClickView.getSelectedTabType() != "0") {
            mView!!.headItemClickView.setOnItemSelected(0)
        }
    }

    override fun doubleClickRefresh() {

    }

    override fun tabSelected(position: Int) {
        try {
            frags.forEach {
                (it as ITabAlarmClickCall).tabSelected(mView!!.viewPagerDub.currentItem)
            }
        } catch (e: Exception) {
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_dubbing_parent
    }

    override fun initView(view: View?) {
        view!!.viewPagerDub.adapter = DubAdapter(childFragmentManager)
        view.toggle_attention.setViewPager(view.viewPagerDub)
        view.viewPagerDub.clearFocus()
        view.viewPagerDub.isFocusableInTouchMode = false
        view.viewPagerDub.isFocusable = false
    }

    override fun initData() {

    }

    override fun initEvent() {
        mView!!.viewPagerDub.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                try {
                    mView!!.headItemClickView.setOnItemSelected(0)
                    //切换界面, 请求全部的数据, 默认为All时,不处理,否则切换tab时, 后台请求一次数据 界面不可见时请求
                    frags.forEach {
                        (it as IAlarmTabCall).itemTabSelected(position, false, 0)
                    }
                } catch (e: Exception) {
                }
            }
        })
        mView!!.headItemClickView.setOnItemClick(object : OnAlarmItemClickListener {
            override fun itemClick(type: Int) {
                //请求
                (frags[mView!!.viewPagerDub.currentItem] as IAlarmTabCall).itemTabSelected(mView!!.viewPagerDub.currentItem, true, type)
            }
        })
    }

    private inner class DubAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
            return frags[p0] as Fragment
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun setAllDubbing(count: Int) {
        allDubbingCount = count
    }

    fun setUserDubbing(count: Int) {
        userDubbingCount = count
    }

}