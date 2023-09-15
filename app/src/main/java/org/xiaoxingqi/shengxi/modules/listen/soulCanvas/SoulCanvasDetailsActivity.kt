package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.content.Intent
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import kotlinx.android.synthetic.main.activity_soul_canvas.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.ImpUpdatePaint
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class SoulCanvasDetailsActivity : BaseAct() {
    private val fragments = arrayOf(RecommendFrag(), HotWorksFragment(), AllWorksFragment(), UserWorksFragment())
    private lateinit var titles: Array<String>

    override fun getLayoutId(): Int {
        return R.layout.activity_soul_canvas
    }

    override fun initView() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {// e 型
            iv_head_charType.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.drawable.draw_soul_canvas_e_2 else R.drawable.draw_soul_canvas_e_1)
        } else { //I 型
            iv_head_charType.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.drawable.draw_soul_canvas_i_2 else R.drawable.draw_soul_canvas_i_1)
        }
    }

    override fun initData() {
        titles = arrayOf("今日推荐", "热门", "实时", "我的")
        viewPager.adapter = CanvasAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        iv_canvas.setOnClickListener {
            startActivity(Intent(this, CanvasLocalActivity::class.java))
        }
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            fragments.forEach {
                (it as ITabClickCall).tabClick(verticalOffset >= 0)
            }
        })
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                /**
                 * 界面切换  是否需要注册eventBus
                 */
                fragments.forEach {
                    (it as ITabClickCall).doubleClickRefresh()
                }
            }
        })
    }

    fun selectPage(position: Int, animated: Boolean = true) {
        viewPager.setCurrentItem(position, animated)
    }

    private inner class CanvasAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position] as Fragment
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ArtUserRelationDelegate.getInstance().close()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewCanvas(event: ImpUpdatePaint) {
        if (event.type == 4) {
            selectPage(2, false)
        }
    }
}