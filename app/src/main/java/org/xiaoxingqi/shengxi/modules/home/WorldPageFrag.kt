package org.xiaoxingqi.shengxi.modules.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.frag_world_page.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.impl.HomeTabClick
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class WorldPageFrag : BaseFragment(), HomeTabClick {
    private val worldFrags = arrayOf(WorldFragment(), ShakeFragment())
    private lateinit var titles: Array<String>
    private var isVisiblePage = false
    private var worldCount = 0
    private var personalCount = 0

    override fun currentPage(page: Int) {
        isVisiblePage = (page == 1)
        try {
            if (isVisiblePage) {
                worldFrags.forEach {
                    (it as HomeTabClick).currentPage(mView!!.worldViewPager.currentItem)
                }
            } else {
                worldFrags.forEach {
                    (it as HomeTabClick).currentPage(-1)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_world_page
    }

    override fun initView(view: View?) {
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        titles = arrayOf("全部", resources.getString(R.string.string_cike))
        mView!!.worldViewPager.adapter = WorldAdapter(childFragmentManager)
        mView!!.pagerSliding.setViewPager(mView!!.worldViewPager)
        try {
            val obj = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            updateSelected(!TextUtils.isEmpty(SPUtils.getString(activity, IConstant.THEMEKEY + obj.user_id, "")))
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        mView!!.worldViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                worldFrags.forEach {
                    (it as HomeTabClick).currentPage(position)
                }
                notifyView()
            }
        })
        mView!!.toggle_attention.setViewPager(mView!!.worldViewPager)
    }

    private inner class WorldAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return worldFrags[position] as Fragment
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    //当界面处于个性的时候更新此界面
    private fun notifyView() {
        try {
            mView!!.tv_share_count.text = if (mView!!.worldViewPager.currentItem == 0) {
                /* if (worldCount == 0) {
                     resources.getString(R.string.string_world_share_empty)
                 } else {
                     String.format(resources.getString(R.string.string_world_share_count), worldCount)
                 }*/
                resources.getString(R.string.string_share_voice_to_world)
            } else {
                resources.getString(R.string.string_personal_empty)
            }
        } catch (e: Exception) {
        }
    }

    fun setWorldCount(count: Int) {
        worldCount = count
        notifyView()
    }

    fun setPersonalCount(count: Int) {
        personalCount = count
        notifyView()
    }

    private fun updateSelected(isSelected: Boolean) {
        try {
            mView!!.toggle_attention.isSelected = isSelected
            mView!!.tv_share_count.isSelected = isSelected
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateThemeEvent(event: OnThemeUpdate) {
        updateSelected(event.state)
    }

    class OnThemeUpdate constructor(val state: Boolean)

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}