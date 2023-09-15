package org.xiaoxingqi.shengxi.modules.home

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.frag_home.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogUserAchievement
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.AchieveData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.FindFriendsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.PagerSlidingTabStripExtends
import org.xiaoxingqi.shengxi.wedgit.videoPlayer.MediaPlayerManager
import org.xiaoxingqi.shengxi.wedgit.videoPlayer.SystemMediaPlayer
import skin.support.SkinCompatManager
import kotlin.math.abs
import kotlin.math.floor

const val REQUEST_RECORD = 0x1001

/**
 * 删除心情的同步 查询当天是会否还有心情
 */
class HomeFragment : BaseFragment(), ITabClickCall {
    private var isVisiblePage = false
    override fun tabClick(isVisible: Boolean) {
        isVisiblePage = isVisible
        if (isVisiblePage) {
            if (playUrl != -1 && MediaPlayerManager.instance().isPlaying) {
                mView!!.viewCoverVideo.visibility = View.GONE
            } else if (playUrl != -1 && !MediaPlayerManager.instance().isPlaying) {
                mView!!.video.dataSourceObject = resources.openRawResourceFd(playUrl)
                mView!!.video.start()
                mView!!.viewCoverVideo.visibility = View.VISIBLE
            }
            // 5.1 出现返回界面视频不播放问题  r9m r9 a59s a37m oppo 手机有此问题
            if ("5.1" == Build.VERSION.RELEASE || !OsUtil.isOppo()) {
                try {
                    mView!!.video.dataSourceObject = resources.openRawResourceFd(playUrl)
                    mView!!.video.start()
                    mView!!.viewCoverVideo.visibility = View.VISIBLE
                } catch (e: Exception) {
                }
            }
            if (today != null) {
                try {
                    updateAchieveStatus()
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun doubleClickRefresh() {

    }

    private val progressHandler = ProgressHelper()

    private lateinit var title: Array<String>
    lateinit var viewPager: ViewPager
    lateinit var pagerSliding: PagerSlidingTabStripExtends
    val fragList = arrayListOf(WorldFragment(), FriendFragment())
    private var playUrl: Int = -1

    override fun getLayoutId(): Int {
        return R.layout.frag_home
    }

    override fun initView(view: View?) {
        val params = view!!.view_status_bar_place.layoutParams
        params.height = AppTools.getStatusBarHeight(context)
        view.view_status_bar_place.layoutParams = params
        viewPager = view.home_pager
        pagerSliding = view.pagerSliding
        //屏幕基准尺寸比例为: 360:210
        //calc ImageView 的尺寸
        val imageParams = mView!!.video.layoutParams
        imageParams.height = (context!!.displayMetrics.widthPixels * 210f / 360 + 0.5f).toInt()
        imageParams.width = context!!.displayMetrics.widthPixels
        mView!!.video.layoutParams = imageParams
        MediaPlayerManager.instance().releasePlayerAndView(activity)
        MediaPlayerManager.instance().mediaPlayer = SystemMediaPlayer()
        userId = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id
    }

    private var userId: String? = null
    private var today: String? = null
    override fun onResume() {
        super.onResume()
        today = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
        if (playUrl != -1 && isVisiblePage) {
            if (MediaPlayerManager.instance().mediaPlayer.isPlaying) {
                mView!!.viewCoverVideo.visibility = View.GONE
            }
        }
        //检测成就的状态
        try {
            if (isVisiblePage) {
                updateAchieveStatus()
            }
        } catch (e: Exception) {
        }
        if (SPUtils.getBoolean(activity, IConstant.HOME_WORLD_TITLE_RED_POINT + userId, false)) {
            mView!!.viewPoint.visibility = View.VISIBLE
        }
    }

    private fun updateAchieveStatus() {
        val recordDate = SPUtils.getString(activity, IConstant.FIRST_PUSH_VOICES + userId, "")
        val travelDate = SPUtils.getString(activity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, "")
        if (recordDate == today && travelDate == today) {
            mView!!.ivAchieve.isSelected = true
        } else if (recordDate == today || travelDate == today) {
            mView!!.ivAchieve.isSelected = false
            mView!!.ivAchieve.isActivated = true
        } else {
            mView!!.ivAchieve.isActivated = false
            mView!!.ivAchieve.isSelected = false
        }
    }

    fun stop() {
        mView!!.viewCoverVideo.visibility = View.VISIBLE
    }

    override fun initData() {
        val obj = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        model = SPUtils.getString(activity, IConstant.THEMEKEY + obj.user_id, "")
        mView!!.viewCoverVideo.visibility = if (TextUtils.isEmpty(model)) View.VISIBLE else View.GONE
        EventBus.getDefault().register(this)
        title = arrayOf("操场", "寢室")
        if (SPUtils.getString(activity, IConstant.TAB_HOME_INDEX + obj.user_id, "0") == "1") {
            title.reverse()
            fragList.reverse()
        }
        viewPager.adapter = HomePager(childFragmentManager)
        pagerSliding.setViewPager(viewPager)
        request(0)
    }

    override fun initEvent() {
        MediaPlayerManager.instance().setOnPrepareListener {
            progressHandler.postDelayed({
                val anim = ObjectAnimator.ofFloat(mView!!.viewCoverVideo, "alpha", 1f, 0f).setDuration(320)
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        mView!!.viewCoverVideo.alpha = 1f
                        mView!!.viewCoverVideo.visibility = View.GONE
                    }
                })
                anim.start()
            }, 1000)
        }
        mView!!.tv_Nearby.setOnClickListener {
            startActivity(Intent(activity, FindFriendsActivity::class.java))
        }
        mView!!.ivAchieve.setOnClickListener {
            request(1)
        }
        mView!!.iv_Guide.setOnClickListener {
            EventBus.getDefault().post(ThemeSetMainEvent())
        }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1)
                    if (mView!!.viewPoint.visibility == View.VISIBLE) {
                        mView!!.viewPoint.visibility = View.GONE
                        SPUtils.setBoolean(activity, IConstant.HOME_WORLD_TITLE_RED_POINT + userId, false)
                    }
                /**
                 * 判断当前首页展示的什么的界面
                 */
                for (frag in fragList) {
                    (frag as HomeTabClick).currentPage(position)
                }
            }
        })
        mView!!.tvWhoAreYou.setOnClickListener {
            startActivity<CartoonListActivity>()
        }
        updateTheme()
    }

    /**
     * @param record -1 无任何记录  0 连续今天未达标  1 今天已达标
     */
    private fun showDialog(record: Int = -1, recordDays: Int = 0, travel: Int = -1, travelDays: Int = 0) {
        DialogUserAchievement(activity!!).setRecordInfo(record, recordDays).setTravelInfo(travel, travelDays).show()
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//成就系统初始化  判断今天是否完成成就
                OkClientHelper.get(activity, "$userId/achievement?achievementType=1", AchieveData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AchieveData
                        result.data?.let {
                            if (it.isNotEmpty()) {
                                if (it[0].achievement_type == 1) {
                                    val today = (System.currentTimeMillis() / 1000).toInt()
                                    if (floor((today.toDouble() - TimeUtils.achieveS2Int(it[0].latest_at)) / (60 * 60 * 24)).toInt() == 0) {//表示今天已达成,否则未达成
                                        SPUtils.setString(activity, IConstant.FIRST_PUSH_VOICES + userId, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            1 -> {
                mView!!.rootTransLayout.showProgress()
                OkClientHelper.get(activity, "$userId/achievement?achievementType=", AchieveData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AchieveData
                        if (result.data != null) {
                            //数据同步
                            var record = -1
                            var travel = -1
                            var recordDays = 0
                            var travelDays = 0
                            val today = (System.currentTimeMillis() / 1000).toInt()
                            this@HomeFragment.today = today.toString()
                            result.data.forEach {
                                if (it.achievement_type == 1) {//心情
                                    val dTimestamp = TimeUtils.achieveS2Int(it.latest_at) - TimeUtils.achieveS2Int(it.started_at)
                                    recordDays = (dTimestamp / (60 * 60 * 24)) + 1
                                    record = when (floor((today.toDouble() - TimeUtils.achieveS2Int(it.latest_at)) / (60 * 60 * 24)).toInt()) {
                                        1 -> {//到昨天
                                            0
                                        }
                                        0 -> {//今天
                                            1
                                        }
                                        else -> {//未连续
                                            -1
                                        }
                                    }
                                    if (record == 1) {//到今天
                                        SPUtils.setString(activity, IConstant.FIRST_PUSH_VOICES + userId, it.latest_at)
                                    }
                                } else if (it.achievement_type == 2) {//时光机
                                    travelDays = ((TimeUtils.achieveS2Int(it.latest_at) - TimeUtils.achieveS2Int(it.started_at)) / (60 * 60 * 24)) + 1
                                    travel = when (floor((today.toDouble() - TimeUtils.achieveS2Int(it.latest_at)) / (60 * 60 * 24)).toInt()) {
                                        1 -> {
                                            0
                                        }
                                        0 -> {
                                            1
                                        }
                                        else -> {
                                            -1
                                        }
                                    }
                                    if (travel == 1) {//到今天
                                        SPUtils.setString(activity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, it.latest_at)
                                    }
                                }
                            }
                            showDialog(record, recordDays, travel, travelDays)
                        } else {
                            showDialog()
                        }
                        mView!!.rootTransLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.rootTransLayout.showContent()
                        showDialog()
                    }
                }, "V4.3")
            }
        }
    }

    private var model: String = ""

    inner class HomePager(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        //是否需要反向
        private var isReverse = false
        override fun getItem(position: Int): Fragment {
            return fragList[position] as Fragment
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getItemId(position: Int): Long {
            return if (isReverse)
                super.getItemId(abs(position - 1))
            else
                super.getItemId(position)
        }

        override fun notifyDataSetChanged() {
            isReverse = !isReverse
            super.notifyDataSetChanged()
        }
    }

    fun setThemeChange() {
        val obj = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        model = SPUtils.getString(activity, IConstant.THEMEKEY + obj.user_id, "")
        updateTheme()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSkinEvent(event: FragSkinUpdateTheme) {
        updateTheme()
    }

    private fun updateTheme() {
        /**
         * 如果 <6.0   夜间模式View 的背景色为黑
         *              白天模式 View 的背景色为0xffcccccc
         *     >=6.0  夜间模式为黑色  白天模式为白色
         */
        if (mView != null) {
            if (TextUtils.isEmpty(model)) {
                mView!!.viewCoverVideo.setImageDrawable(null)
                mView!!.viewCoverVideo.visibility = View.VISIBLE
                if (MediaPlayerManager.instance().isPlaying) {
                    MediaPlayerManager.instance().pause()
                    MediaPlayerManager.instance().releaseMediaPlayer()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                        mView!!.view_status_bar_place.setBackgroundColor(Color.WHITE)
                    } else {//夜间模式
                        mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                    }
                } else {
                    if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                        mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#cccccc"))
                    } else {//夜间模式
                        mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                    }
                }
                playUrl = -1
            } else {
                mView!!.viewCoverVideo.isSelected = !TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)
                playUrl = when (model) {
                    IConstant.THEME_RAIN -> {
                        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                            R.raw.media_video_rain
                        else
                            R.raw.media_video_rain_night
                    }
                    IConstant.THEME_FLOWER -> {
                        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                            R.raw.media_video_flower
                        else
                            R.raw.media_video_flower_night
                    }
                    IConstant.THEME_LEAVES -> {
                        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                            R.raw.media_video_leaves
                        else
                            R.raw.media_video_leaves_night
                    }
                    IConstant.THEME_MAPLE -> {
                        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                            R.raw.media_video_mapple
                        else
                            R.raw.media_video_mapple_night
                    }
                    IConstant.THEME_SNOW -> {
                        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                            R.raw.media_video_snow
                        else
                            R.raw.media_video_snow_night
                    }
                    else -> -1
                }
                mView!!.view_status_bar_place.setBackgroundColor(Color.TRANSPARENT)
            }
            pagerSliding.isSelected = !TextUtils.isEmpty(model)
            if (playUrl != -1) {
                mView!!.video.dataSourceObject = resources.openRawResourceFd(playUrl)
                mView!!.video.start()
                mView!!.viewCoverVideo.visibility = View.VISIBLE
                mView!!.viewCoverVideo.setImageResource(when (model) {
                    IConstant.THEME_RAIN -> R.drawable.selector_rain_first_frame
                    IConstant.THEME_FLOWER -> R.drawable.selector_flower_first_frame
                    IConstant.THEME_LEAVES -> R.drawable.selector_leaves_first_frame
                    IConstant.THEME_MAPLE -> R.drawable.selector_mapple_first_frame
                    IConstant.THEME_SNOW -> R.drawable.selector_snow_model
                    else -> {
                        0
                    }
                })
                mView!!.ivTopCover.visibility = View.GONE
            } else {
                mView!!.ivTopCover.visibility = View.VISIBLE
            }
//            EventBus.getDefault().post(WorldPageFrag.OnThemeUpdate(!TextUtils.isEmpty(model)))
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pageChangeEvent(event: ImplPageChangeEvent) {
        try {
            if (event.page == 0) {
                //跳转到寝室
                viewPager.setCurrentItem(if (fragList[0] is WorldFragment) 1 else 0, false)
            } else if (event.page == 1) {
                if (viewPager.currentItem != 1) {
                    viewPager.setCurrentItem(1, false)
                }
            }
        } catch (e: Exception) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHomeTab(event: HomeTabChangeEvent) {
        fragList.reverse()
        title.reverse()
        viewPager.adapter?.notifyDataSetChanged()
        pagerSliding.notifyDataSetChanged()
    }

    //删除item之后, 强制走一波此状态
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteUpdate(event: IDeleteHomeStatus) {
        updateAchieveStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        MediaPlayerManager.instance().releasePlayerAndView(activity)
    }
}