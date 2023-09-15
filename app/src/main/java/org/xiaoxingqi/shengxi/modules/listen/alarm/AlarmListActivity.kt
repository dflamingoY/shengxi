package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm_plaza.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.impl.ITabAlarmClickCall
import org.xiaoxingqi.shengxi.impl.SensorChangeMoodEvent
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import skin.support.SkinCompatManager
import java.io.File

class AlarmListActivity : BaseNormalActivity() {
    private lateinit var titles: Array<String>
    private val fragments = arrayOf(HotAlarmFragment(), DubbingParentFragment(), WordingFragment(), AlarmFragment())

    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeMoodEvent(type))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_plaza
    }

    override fun initView() {
        val params = viewStatus.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        viewStatus.layoutParams = params
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.WHITE)
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        }
        setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (SPUtils.getBoolean(this, IConstant.FIRST_ALARM_GUIDE + loginBean.user_id, true)) {
            frameGuide.visibility = View.VISIBLE
        }
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {// e 型
            ivBanner.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.mipmap.icon_alarm_top_banner_e_2 else R.mipmap.icon_alarm_top_banner_e_1)
        } else { //I 型
            ivBanner.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.mipmap.icon_alarm_top_banner_i_2 else R.mipmap.icon_alarm_top_banner_i_1)
        }
    }

    override fun initData() {
        titles = arrayOf(resources.getString(R.string.string_hot_alarm_title), resources.getString(R.string.string_hot_alarm_real_time), resources.getString(R.string.string_alarm_1), resources.getString(R.string.string_alarm_3))
        viewPager.adapter = AlarmAdapter(supportFragmentManager)
        pagerSliding.setViewPager(viewPager)
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.DOWNAUDIO)
        if (!file.exists()) {
            file.mkdirs()
        }
        request(0)
    }

    override fun initEvent() {
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
               /* if (position == 2) {
                    relativeBottom.visibility = View.VISIBLE
                } else {
                    relativeBottom.visibility = View.GONE
                }*/
                fragments.forEach {
                    (it as ITabAlarmClickCall).tabSelected(position)
                }
            }
        })
        relativeBottom.setOnClickListener {
            startActivity(Intent(this, PushWordingActivity::class.java))
            overridePendingTransition(R.anim.operate_enter, 0)
        }
        btn_Back.setOnClickListener { finish() }
        cardTopView.setOnClickListener {
            startActivity<AlarmBangActivity>()
        }
        tvPushWord.setOnClickListener {
            startActivity(Intent(this, PushWordingActivity::class.java))
            overridePendingTransition(R.anim.operate_enter, 0)
        }
    }

    fun changeCurrentPage(position: Int, isRefresh: Boolean = false) {
        viewPager.currentItem = position
        if (position == 1) {
            if (isRefresh)
                (fragments[1] as ITabAlarmClickCall).tabClick(true)
        }
    }

    override fun request(flag: Int) {
        //后去今日最佳台词
        executeRequest("leaderboard/dubbings/optimal") {
            try {
                if (it != null) {
                    it as WordingData
                    it.data?.let { data ->
                        relativeMvp.visibility = View.VISIBLE
                        val userInfo = PreferenceTools.getObj(this@AlarmListActivity, IConstant.USERCACHE, UserInfoData::class.java)
                        val bean = data[0].checkUser(userInfo)
                        glideUtil.loadGlide(bean.from_user_info.avatar_url, ivMvp, 0, glideUtil.getLastModified(bean.from_user_info.avatar_url))
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private inner class AlarmAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
            return fragments[p0] as Fragment
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}