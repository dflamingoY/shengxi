package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.ObjectAnimator
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_user_home.*
import kotlinx.android.synthetic.main.activity_user_home.btn_Back
import kotlinx.android.synthetic.main.activity_user_home.cardLayout
import kotlinx.android.synthetic.main.activity_user_home.transLayout
import kotlinx.android.synthetic.main.activity_user_home.tv_Title
import kotlinx.android.synthetic.main.head_userhome.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogCommitPwd
import org.xiaoxingqi.shengxi.dialog.DialogReport
import org.xiaoxingqi.shengxi.dialog.DialogReportContent
import org.xiaoxingqi.shengxi.dialog.DialogUserHomeDetails
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.frag.*
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.AddAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.frag.talkAlbum.TalkCreateAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.userResource.HomeUserBookActivity
import org.xiaoxingqi.shengxi.modules.user.userResource.HomeUserMoviesActivity
import org.xiaoxingqi.shengxi.modules.user.userResource.HomeUserSongActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.download.DownVoiceProxy
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.calendar.CalendarMonthView
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

/**
 * 总时长从上一级页面返回, 避免每次进入页面都必须加载语音接口,
 */
class UserHomeActivity : BaseNormalActivity(), ItemOperator {
    companion object {
        private const val REQUEST_THEME = 0x00
        private const val REQUEST_CROP = 0x01
    }

    private var uid: String? = null
    private var userInfo: UserInfoData? = null
    private var friendCount = 0
    private var voiceTotalLength = 0
    private lateinit var adapter: QuickAdapter<UserDateListData>
    private val mData by lazy { ArrayList<UserDateListData>() }
    private lateinit var headView: View
    private lateinit var otherAdapter: QuickAdapter<BaseBean>
    private val otherData by lazy { ArrayList<BaseBean>() }//自己的界面时, 用来装需要播放的数据
    private var lastId: String? = ""
    private val audioPlayer by lazy { AudioPlayer(this) }
    private var playBean: BaseBean? = null
    private var isSelf = true
    private var topicData: PrivacyTopicData? = null
    private lateinit var loadMoreView: View
    private var userVisitMovies = 1
    private var userVisitBooks = 1
    private var userVisitSongs = 1
    private var userShareVisibility = 2
    private var relation = 0 //用户关系，0=陌生人，1=待验证，2=已是好友，3：被对方拉黑，4：自己拉黑对方,5=自己
    private var years: Int = 2018
    private var months: Int = 4
    private var currentDate: String? = null//201909
    private val random by lazy { Random() }
    private var signYear = 2018//最低截止的年 最新-->最旧
    private var signMonth = 4//最低截止的月
    private var clickDay: String? = null//20190909
    private var currentClickDate: UserDateListData? = null
    private var needUpdateAlbum = false
    private var visitorUser: BaseUserBean? = null
    private var isRequestShareVoice = true//是否请求共享心情
    private var currentYear: Int = 2020
    private var showAchievement = 0//未初始化
    private var needUpdateTalkAlbum = false

    //记录和播放的天数
    private var recordDays = 0

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            otherAdapter.changeStatue(false)
        }
    }

    override fun writeHeadSet(): Boolean {
        return try {
            val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
            val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
            val scoOn = audioPlayer.audioManager.isBluetoothScoOn
            headsetOn || a2dpOn || scoOn
        } catch (e: Exception) {
            super.writeHeadSet()
        }
    }

    override fun changSpeakModel(type: Int) {
        if (type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_home
    }

    override fun initView() {
        val params = view_status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                view_status_bar.setBackgroundColor(Color.WHITE)
            } else {//夜间模式
                view_status_bar.setBackgroundColor(Color.parseColor("#181828"))
            }
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                view_status_bar.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {//夜间模式
                view_status_bar.setBackgroundColor(Color.parseColor("#181828"))
            }
        }
        setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
        swipeRefresh.isEnabled = true
        ObjectAnimator.ofFloat(relativeUserBottomPlayer, "translationY", AppTools.dp2px(this, 60).toFloat()).setDuration(0).start()
        recyclerView.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.head_userhome, recyclerView, false)
    }

    override fun onRestart() {
        super.onRestart()
        if (isSelf && needUpdateAlbum) {
            needUpdateAlbum = false
            request(4)
        }
        if (isSelf && needUpdateTalkAlbum) {
            needUpdateTalkAlbum = false
            request(18)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setItemOperator(this)
        /**
         * 判断是否是自己主页
         */
        uid = intent.getStringExtra("id")
        relation = intent.getIntExtra("relation", 0)
        friendCount = intent.getIntExtra("friendCount", 0)
        voiceTotalLength = intent.getIntExtra("totalLength", 0)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (uid == loginBean.user_id) {
            headView.relativeTalkAlbum.visibility = View.VISIBLE
            headView.tvAlarmName.text = resources.getString(R.string.string_user_build_alarm_title)
            customPlayMenu.visibility = View.GONE
            val params = swipeRefresh.layoutParams as FrameLayout.LayoutParams
            swipeRefresh.isEnabled = false
            params.marginStart = AppTools.dp2px(this, 15)
            params.marginEnd = AppTools.dp2px(this, 15)
            tv_Title.text = resources.getString(R.string.string_user_history)
            ivRecord.visibility = View.VISIBLE
            val obj = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
            val parseYearMonth = TimeUtils.getInstance().parseYearMonth(obj.data.created_at)
            val split = parseYearMonth.split("-")
            signYear = split[0].toInt()
            signMonth = split[1].toInt()
        } else {
            headView.tvAlarmName.text = resources.getString(R.string.string_user_build_alarm_title).replace("我", "ta")
            customPlayMenu.visibility = View.VISIBLE
            swipeRefresh.isSelected = true
            headView.linearVisitor.visibility = View.GONE
            isSelf = false
            tv_Title.text = "ta的心情簿"
            headView.tvOtherVoices.visibility = View.VISIBLE
            headView.viewTopRadius.visibility = View.GONE
            request(2)
            headView.tvAddAlbum.visibility = View.GONE
        }
        adapter = object : QuickAdapter<UserDateListData>(this, R.layout.item_home_user_calendar, mData, headView) {
            private var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: UserDateListData?) {
//                val position = helper!!.itemView.tag as Int
                val monthView = helper!!.getView(R.id.calendarView) as CalendarMonthView
                monthView.setOptionalDate(item!!.contains)
                monthView.setDate(item.year, item.month)
                (helper.getView(R.id.coverWave) as CoverWaveView).attachData(item)
                monthView.onBindView(helper.getView(R.id.coverWave) as CalendarMonthView.OnTouchPosition)
                helper.getImageView(R.id.iv_album).visibility = if (!TextUtils.isEmpty(item.coverUrl)) {
                    Glide.with(this@UserHomeActivity)
                            .load(item.coverUrl)
                            .apply(RequestOptions().centerCrop())
                            .into(helper.getImageView(R.id.iv_album))
                    helper.getView(R.id.viewLayer).visibility = View.VISIBLE
                    View.VISIBLE
                } else {
                    helper.getView(R.id.viewLayer).visibility = View.GONE
                    View.GONE
                }
                helper.getTextView(R.id.tvMonth).text = if (currentYear == item.year) "${item.month}月" else "${item.year}年${item.month}月"
                monthView.setOnClickDate { _, _, day, clickDate ->
                    if (relativeUserBottomPlayer.top != 0)
                        anim(0f)
                    if (audioPlayer.isPlaying) {
                        if (clickDate == clickDay) {
                            audioPlayer.stop()
                            anim(AppTools.dp2px(this@UserHomeActivity, 60).toFloat())
                            return@setOnClickDate
                        } else {
                            audioPlayer.stop()
                            getDataByDate(clickDate)
                        }
                    } else {
                        if (clickDate == clickDay) {
                            if (otherData.size > 0) {
                                down(otherData[random.nextInt(otherData.size)])
                            } else {
                                getDataByDate(clickDate)
                            }
                        } else {
                            getDataByDate(clickDate)
                        }
                    }
                    showInfo(item, day)
                    currentClickDate = item
                    (helper.getView(R.id.coverWave) as CoverWaveView).startAnim()
                }
                helper.getView(R.id.cardLayout).setOnClickListener {
                    currentDate = item.date
                    startActivityForResult(Intent(this@UserHomeActivity, AlbumActivity::class.java)
                            .putExtra("count", 1)
                            , REQUEST_THEME)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun changeStatue(isAttach: Boolean) {
                cache.forEach {
                    val waveView = it.getView(R.id.coverWave) as CoverWaveView
                    waveView.update()
                }
            }
        }
        otherAdapter = object : QuickAdapter<BaseBean>(this, R.layout.item_home_user_voices, otherData, headView) {
            val cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                val statusText = helper!!.getView(R.id.linearStatusText) as LinearStatusText
                statusText.visibility = View.VISIBLE
                item!!.show(helper, this@UserHomeActivity, glideUtil) {
                    true
                }
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                helper.getView(R.id.itemDynamic).isSelected = true
                val position = helper.itemView.tag as Int
                helper.getView(R.id.viewLine).visibility = if (position != otherData.size - 1) View.VISIBLE else View.GONE
                helper.getTextView(R.id.tvTime)?.text = resources.getString(R.string.string_home_hint_2) + TimeUtils.getInstance().paserWorl(this@UserHomeActivity, item.shared_at)
                if (!TextUtils.isEmpty(item.is_shared)) {//是自己
                    statusText.visibility = View.GONE
                    helper.getView(R.id.heartView).visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                } else {
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    statusText.isSelected = item.friend_status == 0
                    when (item.friend_status) {
                        2 -> //好友
                            statusText.visibility = View.GONE
                        1 -> {
                            statusText.visibility = View.VISIBLE
                            statusText.isSelected = false
                        }
                        0 -> {
                            statusText.visibility = View.VISIBLE
                            statusText.isSelected = true
                        }
                    }
                }
                (helper.getView(R.id.imageGroup) as ImageGroupView).setOnClickViewListener {
                    startActivity(Intent(this@UserHomeActivity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
                helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(this@UserHomeActivity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id)
                                .putExtra("uid", item.user.id)
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num == 0) {
                            queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        } else {
                            startActivity(Intent(this@UserHomeActivity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id)
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user_id.toString()))
                        }
                    }
                }
                statusText.setOnClickListener {
                    if (statusText.isSelected)
                        requestFriends(item, it)
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb), transLayout)
                    } else {                //共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
//                            transLayout.showProgress()
                            item.isNetStatus = true
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            transLayout.showProgress()
                            LightUtils.contains(item.voice_id)
                            thumb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        share(item) {
                            if (it == 1) {
                                delete(item) { delBean ->
                                    showToast("删除成功")
                                    otherData.remove(delBean)
                                    adapter.notifyDataSetChanged()
                                    transLayout.showContent()
                                    if (mData.size == 0) {
                                        transLayout.showEmpty()
                                    }
                                    about()
                                }
                            } else if (it == 2) {
                                transLayout.showProgress()
                                setVoicePrivacy(item)
                            }
                        }
                    else
                        DialogReport(this@UserHomeActivity).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@UserHomeActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogReportContent(this@UserHomeActivity)
                                            .setOnResultListener(OnReportItemListener { report ->
                                                transLayout.showProgress()
                                                report(item.voice_id, report, item.user.id)
                                            }).show()
                                }
                                R.id.tv_report_normal -> {
                                    reportNormal { type ->
                                        reportNormalItem(item.voice_id, type)
                                    }
                                }
                                R.id.tv_Follow -> {
                                    if (item.subscription_id == 0) {
                                        addSubscriber(item)
                                    } else {
                                        deleteSubscriber(item) {
                                            deletedSubscriber(item)
                                        }
                                    }
                                }
                            }
                        }).show()
                }
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                voiceProgress./*findViewById<View>(R.id.viewSeekProgress).*/setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            voiceProgress.finish()
                            return@setOnClickListener
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    playBean?.let {
                        if (item !== it) {
                            it.isPlaying = false
                            it.isPause = false
                            it.pasuePosition = 0
                        }
                    }
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    down(item)
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                voiceProgress.finish()
                            }
                        }
                    }

                    override fun endTrack(progress: Float) {
                        /**
                         * 滑动停止
                         */
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                        down(item)
                    }
                })
                voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            voiceProgress.finish()
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    item.pasuePosition = 0
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    down(item)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun animStatus(isStart: Boolean) {
                if (isStart) {
                    if (anim == null) {
                        anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1500)
                        anim!!.interpolator = LinearInterpolator()
                        anim!!.repeatCount = ValueAnimator.INFINITE
                        anim!!.addUpdateListener {
                            val value = it.animatedValue as Float
                            if (audioPlayer.isPlaying) {
                                cache.forEach { helper ->
                                    (helper.getView(R.id.heartView) as HeartWaveView).waveShiftRatio = value
                                }
                            }
                        }
                    }
                    anim!!.start()
                } else {
                    cache.forEach { helper ->
                        (helper.getView(R.id.heartView) as HeartWaveView).end()
                    }
                    anim?.let {
                        it.cancel()
                        anim = null
                    }
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                        (helper.getView(R.id.heartView) as HeartWaveView).waterLevelRatio = currentPosition / audioPlayer.duration.toFloat()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun notifyHeart(bean: BaseBean?) {
                bean?.let { b ->
                    cache.loop { baseAda ->
                        (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                    }?.let { helper ->
                        if (TextUtils.isEmpty(b.is_shared)) {
                            helper.getView(R.id.iv_Thumb).isSelected = b.is_collected == 1
                            if (recyclerView.isOffsetScreen(otherData.indexOf(bean) + 1)) {
                                SmallBang.attach2Window(this@UserHomeActivity).bang(helper.getView(R.id.iv_Thumb), 60f, null)
                            }
                        }
                    }
                }
            }
        }
        if (uid == loginBean.user_id) {
            recyclerView.adapter = adapter
            adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.loadmore_padding_bottom, recyclerView, false).apply {
                progressHandler.postDelayed({
                    findViewById<View>(R.id.viewBottom).visibility = View.VISIBLE
                }, 500)
            })
            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            request(3)
            request(4)
            val yearMonth = TimeUtils.getInstance().parseYearMonth((System.currentTimeMillis() / 1000).toInt())
            val split = yearMonth.split("-")
            years = split[0].toInt()
            currentYear = years
            months = split[1].toInt()
            requestCalendar(years, months)
            request(18)
        } else {
            val params = headView.layoutParams as RecyclerView.LayoutParams
            params.marginStart = 0
            params.marginEnd = 0
            recyclerView.adapter = otherAdapter
            loadMoreView = LayoutInflater.from(this).inflate(R.layout.loadmore_user_voices, recyclerView, false)
            otherAdapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadMoreView)
            otherAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
            request(10)
            //非自己 非好友的情况 需要查询权限, 好友的话, 直接查询是否有内容
            request(15)
            //查询用户浏览权限设置
            if (relation != 2)
                request(16)
        }
        request(0)
    }

    override fun onResume() {
        super.onResume()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (uid != loginBean.user_id) {
            if (relation != 2) {
                request(8)
            } else {
                if (isRequestShareVoice) {
                    isRequestShareVoice = false
                    request(8)
                    request(9)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSelf) {
            audioPlayer.stop()
            anim(AppTools.dp2px(this, 60).toFloat())
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        cardLayout.setOnClickListener {
            userInfo?.let {
                if (topicData == null) {
                    when {
                        showAchievement != 0 -> {//请求过, 可能是不展示话题
                            request(6)
                        }
                        else -> {
                            request(12)
                        }
                    }
                } else {
                    DialogUserHomeDetails(this@UserHomeActivity).setAchieveInfo(showAchievement, recordDays).setData(it.data, glideUtil).setTopic(topicData).show()
                }
            }
        }
        headView.btnMovies.setOnClickListener {
            if (headView.btnMovies.getButtonStatus() == 2) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnMovies.getEmptyStatus()) {
                showToast("还没有电影心情")
                return@setOnClickListener
            }
            userInfo?.let {
                startActivity(Intent(this, HomeUserMoviesActivity::class.java)
                        .putExtra("title", it.data.nick_name)
                        .putExtra("permission", userVisitMovies)
                        .putExtra("relation", relation)
                        .putExtra("userId", uid))
            }
        }
        headView.btnBook.setOnClickListener {
            if (headView.btnBook.getButtonStatus() == 2) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnBook.getEmptyStatus()) {
                showToast("还没有书籍心情")
                return@setOnClickListener
            }
            userInfo?.let {
                startActivity(Intent(this, HomeUserBookActivity::class.java)
                        .putExtra("title", it.data.nick_name)
                        .putExtra("permission", userVisitBooks)
                        .putExtra("relation", relation)
                        .putExtra("userId", uid))
            }
        }
        headView.btnSong.setOnClickListener {
            if (headView.btnSong.getButtonStatus() == 2) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnSong.getEmptyStatus()) {
                showToast("还没有唱回忆")
                return@setOnClickListener
            }
            userInfo?.let {
                startActivity(Intent(this, HomeUserSongActivity::class.java)
                        .putExtra("title", it.data.nick_name)
                        .putExtra("permission", userVisitSongs)
                        .putExtra("relation", relation)
                        .putExtra("userId", uid))
            }
        }
        headView.btnVoices.setOnClickListener {
            if (headView.btnVoices.getButtonStatus() == 0) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnVoices.getEmptyStatus()) {
                showToast("还没有心情")
                return@setOnClickListener
            }
            userInfo?.let {
                startActivity(Intent(this, VoiceListActivity::class.java)
                        .putExtra("userId", uid)
                        .putExtra("voiceLength", voiceTotalLength)
                        .putExtra("createdAt", it.data.created_at))
            }
        }
        headView.btnPhoto.setOnClickListener {
            if (headView.btnPhoto.getButtonStatus() == 0) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnPhoto.getEmptyStatus()) {
                showToast("还没有照片")
                return@setOnClickListener
            }
            startActivity(Intent(this, UserPhotoListActivity::class.java)
                    .putExtra("voiceLength", voiceTotalLength)
                    .putExtra("userId", uid))
        }
        headView.btnMachine.setOnClickListener {
            if (headView.btnMachine.getButtonStatus() == 0) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnMachine.getEmptyStatus()) {
                showToast("还没有内容")
                return@setOnClickListener
            }
            startActivity(Intent(this, TimeMachineActivity::class.java).putExtra("userId", uid))
        }
        headView.tvAddAlbum.setOnClickListener {
            startActivity(Intent(this, AddAlbumActivity::class.java))
        }
        headView.tvCreateTalkAlbum.setOnClickListener {
            startActivity(Intent(this, TalkCreateAlbumActivity::class.java))
        }
        ivRecord.setOnClickListener {
            if (!RecordTransparentActivity.isOnCreate) {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("type", 1))
                overridePendingTransition(0, 0)
            }
        }
        tv_VoiceLength.setOnClickListener {
            userInfo?.let {
                if (topicData == null) {
                    when {
                        showAchievement != 0 -> {//请求过, 可能是不展示话题
                            request(6)
                        }
                        else -> {
                            request(12)
                        }
                    }
                } else {
                    DialogUserHomeDetails(this@UserHomeActivity).setAchieveInfo(showAchievement, recordDays).setData(it.data, glideUtil).setTopic(topicData).show()
                }
            }
        }
        otherAdapter.setOnLoadListener {
            request(9)
        }
        tvDetails.setOnClickListener {
            startActivity(Intent(this, UserMonthDayVoicesActivity::class.java)
                    .putExtra("date", clickDay))
        }
        tvStop.setOnClickListener {
            audioPlayer.stop()
            anim(AppTools.dp2px(this, 60).toFloat())
        }
        headView.tvVisitUser.setOnClickListener {
            visitorUser?.let {
                startActivity<UserDetailsActivity>("id" to it.user_id)
            }
        }
        customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = otherData.indexOf(it) + 1
                        /*if (index >= otherData.size) {
                            index = 0
                            recyclerView.smoothScrollToPosition(0)
                        }*/
                        if (index >= otherData.size) {
                            return@let
                        }
                        if (index == otherData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(9)
                        }
                        down(otherData[index], index != 0)
                    }
                } catch (e: Exception) {

                }
            }

            override fun play() {
                if (audioPlayer.isPlaying) {
                    playBean?.let {
                        it.pasuePosition = audioPlayer.currentPosition.toInt()
                    }
                    audioPlayer.stop()
                } else {
                    if (playBean?.let {
                                down(it)
                                it
                            } == null) {
                        //播放0角标
                        if (otherData.size > 0) {
                            down(otherData[0], true)
                        }
                    }
                }
            }

            override fun pre() {
                audioPlayer.stop()
                playBean?.let {
                    var index = otherData.indexOf(it) - 1
                    if (index < 0) index = 0
                    down(otherData[index], true)
                }
            }

            override fun top() {
                if (!audioPlayer.isPlaying) {
                    recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        recyclerView.scroll(this@UserHomeActivity, otherData.indexOf(it) + 1, false)
                    }
            }
        })
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            @SuppressLint("SetTextI18n")
            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                }
                if (isSelf) {
                    otherData.indexOf(playBean).let {
                        playBean = null
                        if (it == -1) {
                            down(otherData[random.nextInt(otherData.size)])
                        } else {
                            if (otherData.size > it + 1) {
                                down(otherData[it + 1])
                            } else {
                                down(otherData[0])
                            }
                        }
                    }
                } else {
                    progressHandler.stop()
                    otherAdapter.changeStatue(false)
                    nextAudio()
                }
            }

            override fun onInterrupt() {
                playBean?.let {
                    it.isPlaying = false
                }
                if (isSelf) {
                    //停止所有动画
                    currentClickDate?.let {
                        it.isPlaying = false
                        adapter.changeStatue(false)
                    }
                } else {
                    otherAdapter.animStatus(false)
                    customPlayMenu.isSelected = false
                    otherAdapter.changeStatue(false)
                }
                progressHandler.stop()
            }

            override fun onPrepared() {
                //开始播放, 添加播放次数
                playBean?.let {
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.isPlaying = true
                    it.pasuePosition = 0
                    addPlays(it, null) {}
                }
                if (!isSelf) {
                    playBean?.let {
                        if (LightUtils.contains(it.voice_id)) {
                            otherAdapter.animStatus(true)
                        }
                    }
                    customPlayMenu.isSelected = true
                    otherAdapter.changeStatue(false)
                    progressHandler.start()
                } else {
                    currentClickDate?.let {
                        it.isPlaying = true
                    }
                }
            }
        }
        headView.btnDubbing.setOnClickListener {
            if (headView.btnDubbing.getButtonStatus() == 2) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnDubbing.getButtonStatus() == 3) {
                showToast("仅自己可见")
                return@setOnClickListener
            }
            if (headView.btnDubbing.getEmptyStatus()) {
                showToast("还没有配音")
                return@setOnClickListener
            }
            startActivity<UserDubbingActivity>("uid" to uid)
        }
        headView.btnPaint.setOnClickListener {
            if (headView.btnPaint.getButtonStatus() == 2) {
                showToast("仅室友可见")
                return@setOnClickListener
            }
            if (headView.btnPaint.getButtonStatus() == 3) {
                showToast("仅自己可见")
                return@setOnClickListener
            }
            if (headView.btnPaint.getEmptyStatus()) {
                showToast("还没有作品")
                return@setOnClickListener
            }
            startActivity<UserPaintActivity>("uid" to uid)
        }
    }

    //下一首
    private fun nextAudio() {
        playBean?.let {
            if (it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                it.is_collected = 1
                thumb(it, null, 5)
            }
            otherAdapter.animStatus(false)
            //继续列表循环
            if (SPUtils.getBoolean(this@UserHomeActivity, IConstant.PLAY_MENU_AUTO, false)) {
                var index = otherData.indexOf(it) + 1
                if (index >= otherData.size) index = 0
                if (index == 0) recyclerView.scrollToPosition(0)
                down(otherData[index], index != 0)
                if (index == otherData.size - 2) {
                    request(9)
                }
            } else {
                customPlayMenu.isSelected = false
            }
        }
    }

    /**
     * @param isScroll 是否需要自动滚动
     */
    private fun down(bean: BaseBean, isScroll: Boolean = false) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(this, otherData.indexOf(bean) + 1)
        }
        if (TextUtils.isEmpty(bean.voice_url)) {
            showToast(resources.getString(R.string.string_error_file))
        }
        DownVoiceProxy.downProxy.clearFocus()
        playBean = bean
        val file = getDownFilePath(bean.voice_url)
        if (file.exists()) {
            audioPlayer.setDataSource(file.absolutePath)
            if (currentMode == MODE_EARPIECE)
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            else
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        } else {
            OkClientHelper.downProxy(this, bean, {
                audioPlayer.setDataSource(it)
                if (currentMode == MODE_EARPIECE)
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                else
                    audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            }, {
                if (!isSelf) {
                    nextAudio()
                }
                showToast(VolleyErrorHelper.getMessage(it))
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showInfo(bean: UserDateListData, day: Int) {
        clickDay = "${bean.date}${if (day < 10) "0$day" else day}"
        tvCalendar.text = TimeUtils.getCharMonth(bean.month.toString()) + ".${day} " + bean.year
    }

    private fun anim(dy: Float) {
        val animator = ObjectAnimator.ofFloat(relativeUserBottomPlayer, "translationY", dy).setDuration(200)
        animator.start()
    }

    @SuppressLint("SetTextI18n")
    private fun attachAlbum(data: List<VoiceAlbumData.AlbumDataBean>?) {
        headView.linearAlbum.removeAllViews()
        val width = ((AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 30) - AppTools.dp2px(this, 46)) / 3f + 0.5f).toInt()
        if (data == null || data.isEmpty()) {
            if (isSelf) {
                val view = View.inflate(this, R.layout.layout_album_text, null)
                view.findViewById<TextView>(R.id.tvTitle).text = resources.getString(R.string.string_create_album)
                val params = LinearLayout.LayoutParams(width, width)
                params.setMargins(AppTools.dp2px(this, 13), AppTools.dp2px(this, 15), 0, AppTools.dp2px(this, 21))
                view.findViewById<TextView>(R.id.tvTitle).layoutParams = params
                view.setOnClickListener {
                    startActivity(Intent(this, AddAlbumActivity::class.java))
                }
                headView.linearAlbum.addView(view)
            } else {
                val textView = View.inflate(this, R.layout.layout_empty_album, null)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, width)
                params.setMargins(0, 0, 0, AppTools.dp2px(this, 22))
                headView.linearAlbum.addView(textView, params)
            }
        } else {
            (0 until if (data.size >= 3) 2 else data.size).forEach {
                val view = AlbumCoverView(this)
                view.setTitle(data[it], if (isSelf) 5 else relation, width, if (it == 0) AppTools.dp2px(this, 13) else AppTools.dp2px(this, 10))
                view.setOnClickListener { _ ->
                    if ((data[it].album_type == 1 && data[it].voice_total_len > 0) || isSelf || (relation == 2 && data[it].album_type == 2))
                        startActivity(Intent(this, SeasonAlbumDetailsActivity::class.java).putExtra("data", data[it]))
                    else if (data[it].voice_total_len == 0 && ((relation == 2 && data[it].album_type == 2) || data[it].album_type == 1)) {
                        showToast("专辑为空")
                    }
                }
                headView.linearAlbum.addView(view)
            }
            if (data.size >= 3) {
                val view = View.inflate(this, R.layout.layout_album_text, null)
                val params = LinearLayout.LayoutParams(width, width)
                params.setMargins(AppTools.dp2px(this, 10), AppTools.dp2px(this, 15), 0, 0)
                view.findViewById<TextView>(R.id.tvTitle).layoutParams = params
                view.setOnClickListener {
                    startActivity(Intent(this, SeasonAlbumActivity::class.java).putExtra("userId", uid))
                }
                headView.linearAlbum.addView(view)
                if (data.size >= 10) {
                    request(11)
                } else {
                    view.findViewById<TextView>(R.id.tvTitle).text = "查看更多\n(${data.size})"
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun attachTalkAlbum(data: List<VoiceAlbumData.AlbumDataBean>?) {
        headView.linearTalkAlbum.removeAllViews()
        val width = ((AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 30) - AppTools.dp2px(this, 46)) / 3f + 0.5f).toInt()
        if (data == null || data.isEmpty()) {
            val view = View.inflate(this, R.layout.layout_album_text, null)
            view.findViewById<TextView>(R.id.tvTitle).text = resources.getString(R.string.string_create_album)
            val params = LinearLayout.LayoutParams(width, width)
            params.setMargins(AppTools.dp2px(this, 13), AppTools.dp2px(this, 15), 0, AppTools.dp2px(this, 21))
            view.findViewById<TextView>(R.id.tvTitle).layoutParams = params
            view.setOnClickListener {
                startActivity(Intent(this, TalkCreateAlbumActivity::class.java))
            }
            headView.linearTalkAlbum.addView(view)
        } else {
            (0 until if (data.size >= 3) 2 else data.size).forEach {
                val view = AlbumCoverView(this)
                view.setTitle(data[it], 5, width, if (it == 0) AppTools.dp2px(this, 13) else AppTools.dp2px(this, 10), true)
                view.setOnClickListener { _ ->
                    if ((data[it].album_type == 1 && data[it].voice_total_len > 0) || isSelf || (relation == 2 && data[it].album_type == 2))
                        startActivity(Intent(this, TalkAlbumDetailsActivity::class.java).putExtra("data", data[it]))
                    else if (data[it].voice_total_len == 0 && ((relation == 2 && data[it].album_type == 2) || data[it].album_type == 1)) {
                        showToast("专辑为空")
                    }
                }
                headView.linearTalkAlbum.addView(view)
            }
            if (data.size >= 3) {
                val view = View.inflate(this, R.layout.layout_album_text, null)
                val params = LinearLayout.LayoutParams(width, width)
                params.setMargins(AppTools.dp2px(this, 10), AppTools.dp2px(this, 15), 0, 0)
                view.findViewById<TextView>(R.id.tvTitle).layoutParams = params
                view.setOnClickListener {
                    startActivity(Intent(this, TalkAlbumActivity::class.java).putExtra("userId", uid))
                }
                headView.linearTalkAlbum.addView(view)
                if (data.size >= 10) {
                    request(19)
                } else
                    view.findViewById<TextView>(R.id.tvTitle).text = "查看更多\n(${data.size})"
            }
        }
    }


    /**
     * 一次性load完
     */
    private fun requestCalendar(year: Int, month: Int) {//获取日历
        OkClientHelper.get(this, "users/$uid/voices/calendar?year=$year&month=$month", ModelCalenderData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ModelCalenderData
                if (result.code == 0 && result.data != null) {
                    //有数据
                    result.data?.let {
                        mData.add(UserDateListData().apply {
                            date = "$year${if (month > 9) month else "0$month"}"
                            this.year = year
                            this.month = month
                            this.coverUrl = SPUtils.getString(this@UserHomeActivity, IConstant.USER_CALENDAR_COVER + uid + date, "")
                            this.contains = it.map {
                                it.voiceDay
                            }.toTypedArray()
                            getCoverByCalendar(date)
                        })
                    }
                    adapter.notifyDataSetChanged()
                }
                if (headView.viewTopRadius.visibility != View.VISIBLE) {
                    headView.viewTopRadius.visibility = View.VISIBLE
                }
                //继续请求结果, 年请一次
                months--
                if (months == 0) {//换年份_停止请求
                    years--
                    months = 12
                    //上一个年份没有数据继续下一个年份
                }
                //达到最低年份, 且月份比最低月份低,
                if (years < signYear || (years == signYear && months < signMonth)) {
                    if (mData.size == 0) {
                        //添加当前最新一个月的
                        val yearMonth = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
                        mData.add(UserDateListData().apply {
                            this.date = yearMonth.substring(0, 6)
                            this.year = yearMonth.substring(0, 4).toInt()
                            this.month = yearMonth.substring(4, 6).toInt()
                        })
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    requestCalendar(years, months)
                }
                progressHandler.postDelayed({
                    swipeRefresh.isSelected = false
                }, 100)
            }

            override fun onFailure(any: Any?) {
                //请求错误, 可能是无网络,为空时添加一条记录
                swipeRefresh.isSelected = false
                if (mData.size == 0) {
                    if (headView.viewTopRadius.visibility != View.VISIBLE) {
                        headView.viewTopRadius.visibility = View.VISIBLE
                    }
                    val yearMonth = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
                    mData.add(UserDateListData().apply {
                        this.date = yearMonth.substring(0, 6)
                        this.year = yearMonth.substring(0, 4).toInt()
                        this.month = yearMonth.substring(4, 6).toInt()
                    })
                    adapter.notifyDataSetChanged()
                }
            }
        }, "V4.2")
    }

    private fun getCoverByCalendar(date: String) {
        OkClientHelper.get(this, "users/${uid}/covers?coverName=calendartheme_cover&coverTag=$date", NewVersionCoverData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewVersionCoverData
                if (result.code == 0 && result.data != null && result.data.size > 0) {
                    mData.loop {
                        it.date == date
                    }?.let {
                        it.coverUrl = result.data[0].coverUrl
                        adapter.notifyItemChanged(mData.indexOf(it) + 1)
                        //保存用户的cover 到缓存
                        SPUtils.setString(this@UserHomeActivity, IConstant.USER_CALENDAR_COVER + uid + date, result.data[0].coverUrl)
                    }
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V4.2")
    }

    private fun updateCoverByCalendar(path: String) {
        transLayout.showProgress()
        OkClientHelper.post(this, "users/${uid}/covers", FormBody.Builder()
                .add("bucketId", AppTools.bucketId)
                .add("coverUri", path)
                .add("coverTag", currentDate!!)
                .add("coverName", "calendartheme_cover").build(), AddCoverData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as AddCoverData
                if (result.code == 0) {
                    mData.loop {
                        it.date == currentDate
                    }?.let {
                        it.coverUrl = result.data.coverUrl
                        adapter.notifyItemChanged(mData.indexOf(it) + 1)
                        SPUtils.setString(this@UserHomeActivity, IConstant.USER_CALENDAR_COVER + uid + it.date, result.data.coverUrl)
                    }
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
    }

    /**
     * 一定存在数据
     */
    private fun getDataByDate(date: String) {
        transLayout.showProgress()
        OkClientHelper.get(this, "users/$uid/voices/calendar/$date", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                otherData.clear()
                if (result.code == 0) {
                    result.data?.let {
                        otherData.addAll(result.data)
                        //随机playMenu
                        down(otherData[random.nextInt(otherData.size)])
                    }
                    if (otherData.size == 0) {
                        toast("还没有心情")
                        anim(AppTools.dp2px(this@UserHomeActivity, 60).toFloat())
                        //移除点击月份的按钮
                        mData.loop {
                            it.date == date.substring(0, 6)
                        }?.let {
                            it.contains = it.contains.filter { element ->
                                element != date
                            }.toTypedArray()
                            if (it.contains.isEmpty()) {
                                //如果当月只有一条数据, 移除数据之后, 不删除
                                if (it.year != currentYear) {
                                    mData.remove(it)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    adapter.notifyItemChanged(mData.indexOf(it) + 1)
                                }
                            } else {
                                adapter.notifyItemChanged(mData.indexOf(it) + 1)
                            }
                        }
                    }
                } else {
                    toast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//about
                OkClientHelper.get(this, "users/$uid/about", UserInfoData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            userInfo = result
                            voiceTotalLength = result.data.voice_total_len
                            glideUtil.loadGlide(result.data.avatar_url, ivAvatar, 0, glideUtil.getLastModified(result.data.avatar_url))
//                            tv_VoiceLength.text = "共" + TimeUtils.formatterS(this@UserHomeActivity, result.data.voice_total_len)
                            /**
                             * 更新数据到local
                             */
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            2 -> {//提交访客记录
                OkClientHelper.post(this, "users/$uid/visitors", FormBody.Builder().add("position", "1").build(), BaseRepData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                    }

                    override fun success(result: Any?) {
                    }
                }, "V4.2")
            }
            3 -> {//获取访客记录
                OkClientHelper.get(this, "users/$uid/visitors?position=1", SearchUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as SearchUserData).code == 0) {
                            result.data?.let {
                                visitorUser = it[0]
                                headView.tvVisitUser.text = it[0].nick_name
                                headView.tvVisitHint.text = resources.getString(R.string.string_user_home_visit_mattch)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            4 -> {//获取 专辑列表
                OkClientHelper.get(this, "user/$uid/voiceAlbum?orderByField=albumSort&orderByValue=", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0) {
                            attachAlbum(result.data)
                        } else {
                            attachAlbum(null)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        attachAlbum(null)
                    }
                }, "V3.8")
            }
            5 -> {//获取用户设置的话题列表
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid/favorite/topic", PrivacyTopicData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PrivacyTopicData
                        if (result.code == 0) {
                            topicData = result
                            if (isVisibleActivity) {
                                DialogUserHomeDetails(this@UserHomeActivity).setData(userInfo!!.data, glideUtil).setAchieveInfo(showAchievement, recordDays).setTopic(topicData).show()
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
            6 -> {
                //查詢是否設置話題展示
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid/setting/favoriteTopic", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.favorite_topic == 1 && !isSelf && relation != 2) {//非好友,仅室友可见
                                DialogUserHomeDetails(this@UserHomeActivity).setAchieveInfo(showAchievement, recordDays).setData(userInfo!!.data, glideUtil).setPermissionDenial(true).show()
                            } else {
                                request(5)
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            8 -> {//查询用户设置其他用户是否允许查看共享心情, 预先加载书影音可能用到的权限设置,无参数则默认开启
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid/settings?settingName=&settingTag=moodbook", NewVersionSetData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetData
                        result.data?.let {
                            it.forEach { set ->
                                when (set.setting_name) {
                                    "share_voice_visibility" -> userShareVisibility = set.setting_value
                                    "movie_voice_visibility" -> userVisitMovies = set.setting_value
                                    "book_voice_visibility" -> userVisitBooks = set.setting_value
                                    "song_voice_visibility" -> userVisitSongs = set.setting_value
                                }
                            }
                        }
                        if (userShareVisibility == 1) {
                            otherAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                            loadMoreView.findViewById<TextView>(R.id.tv_NullDesc).text = resources.getString(R.string.string_user_share_voice_enable)
                        } else {
                            loadMoreView.findViewById<TextView>(R.id.tv_NullDesc).text = resources.getString(R.string.string_user_share_voice_empty)
                            if (isRequestShareVoice) {
                                isRequestShareVoice = false
                                request(9)
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            9 -> {//查询用户的共享列表
                OkClientHelper.get(this, "users/$uid/voices?lastId=$lastId&moduleId=4&recognition=1", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceData
                        otherAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0) {
                            result.data?.let {
                                it.forEach { bean ->
                                    otherData.add(bean)
                                    otherAdapter.notifyItemInserted(otherAdapter.itemCount - 1)
                                }
                                if (!TextUtils.isEmpty(lastId))
                                    otherAdapter.notifyItemChanged(otherData.size - it.size)
                                if (it.size >= 10) {
                                    lastId = otherData[otherData.size - 1].share_id
                                    otherAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            }
                        }
                        if (otherData.size == 0) {
                            otherAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            10 -> {//其他人的专辑
                OkClientHelper.get(this, "user/$uid/voiceAlbum?orderByField=albumSort&orderByValue=", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0) {
                            attachAlbum(result.data)
                        } else {
                            attachAlbum(null)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        attachAlbum(null)
                    }
                }, "V4.2")
            }
            11 -> {//查询专辑数量
                OkClientHelper.get(this, "user/${uid}/voiceAlbum/Statistics", IntegerRespData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as IntegerRespData
                        result.data?.let {
                            if (headView.linearAlbum.childCount == 3) {
                                headView.linearAlbum.findViewById<TextView>(R.id.tvTitle).text = "查看更多\n(${it.total})"
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            12 -> {//查询用户是否显示成就系统
                transLayout.showProgress()
                OkClientHelper.get(this, "users/${uid}/settings?settingName=achievement_visibility&settingTag=other", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        result.data?.let { list ->
                            if (list.setting_name == "achievement_visibility") {
                                showAchievement = list.setting_value
                            }
                        }
                        if (showAchievement == 1) {
                            //计算
                            request(13)
                        } else {
                            request(6)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            13 -> {//查新用户的成就系数
                transLayout.showProgress()
                OkClientHelper.get(this, "$uid/achievement?achievementType=1", AchieveData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AchieveData
                        result.data?.let {
                            if (it.isNotEmpty()) {
                                if (it[0].achievement_type == 1) {
                                    val today = (System.currentTimeMillis() / 1000).toInt()
                                    if (it[0].achievement_type == 1) {
                                        if ((floor((today.toDouble() - TimeUtils.achieveS2Int(it[0].latest_at)) / (60 * 60 * 24)).toInt()) in (0..1)) {
                                            if (floor((today.toDouble() - TimeUtils.achieveS2Int(it[0].latest_at)) / (60 * 60 * 24)).toInt() == 0) {
                                                //已经完成了当天
                                                SPUtils.setString(this@UserHomeActivity, IConstant.FIRST_PUSH_VOICES + uid, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                                            }
                                            recordDays = ((TimeUtils.achieveS2Int(it[0].latest_at) - TimeUtils.achieveS2Int(it[0].started_at)) / (60 * 60 * 24)) + 1
                                        }
                                    }
                                }
                            }
                        }
                        if (isSelf) {
                            request(5)
                        } else
                            request(6)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.3")
            }
            15 -> {//查询各个模块是否有内容
                OkClientHelper.get(this, "users/${uid}/modules/statistics", UserVoiceLengthData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserVoiceLengthData
                        result.data?.let {
                            it.forEach { bean ->
                                when (bean.id) {
                                    "1" -> {
                                        if (bean.total == 0) {
                                            headView.btnVoices.setEmptyStatus()
                                        }
                                    }
                                    "2" -> {
                                        if (bean.total == 0) {
                                            headView.btnPhoto.setEmptyStatus()
                                        }
                                    }
                                    "3" -> {
                                        if (bean.total == 0) {
                                            headView.btnMachine.setEmptyStatus()
                                        }
                                    }
                                    "4" -> {
                                        if (bean.total == 0) {
                                            headView.btnMovies.setEmptyStatus()
                                        }
                                    }
                                    "5" -> {
                                        if (bean.total == 0) {
                                            headView.btnBook.setEmptyStatus()
                                        }
                                    }
                                    "6" -> {
                                        if (bean.total == 0) {
                                            headView.btnSong.setEmptyStatus()
                                        }
                                    }
                                    "8" -> {
                                        if (bean.total == 0) {
                                            headView.btnDubbing.setEmptyStatus()
                                        }
                                    }
                                    "7" -> {
                                        if (bean.total == 0)
                                            headView.btnPaint.setEmptyStatus()
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            16 -> {//查询用户的设置
                OkClientHelper.get(this, "users/$uid/settings?settingName=&settingTag=moodbook", NewVersionSetData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetData
                        result.data?.let { list ->
                            list.forEach {
                                when (it.setting_name) {
                                    "voice_visible_days" -> {//可见性
                                        if (it.setting_value == 0) {
                                            headView.btnVoices.setLockStatus(it.setting_value)
                                            headView.btnPhoto.setLockStatus(it.setting_value)
                                            headView.btnMachine.setLockStatus(it.setting_value)
                                        }
                                    }
                                    "movie_voice_visibility" -> {
                                        if (it.setting_value != 1)
                                            headView.btnMovies.setLockStatus()
                                    }
                                    "book_voice_visibility" -> {
                                        if (it.setting_value != 1)
                                            headView.btnBook.setLockStatus()
                                    }
                                    "song_voice_visibility" -> {
                                        if (it.setting_value != 1)
                                            headView.btnSong.setLockStatus()
                                    }
                                    "dubbing_visibility" -> {//1:所有人可见，2:仅限好友，默认所有人可见,3:仅自己可见，默认：1
                                        if (it.setting_value != 1) {
                                            if (it.setting_value == 3) {
                                                headView.btnDubbing.setLockStatus(it.setting_value)
                                            } else
                                                if (relation != 2)
                                                    headView.btnDubbing.setLockStatus(it.setting_value)
                                        }
                                    }
                                    "artwork_visibility" -> {
                                        if (it.setting_value != 1) {
                                            if (it.setting_value == 3) {
                                                headView.btnPaint.setLockStatus(it.setting_value)
                                            } else
                                                if (relation != 2)
                                                    headView.btnPaint.setLockStatus(it.setting_value)
                                        }
                                    }
                                }
                            }
                        }
//                        request(15)
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            17 -> {//单独查询好友的配音设置
                OkClientHelper.get(this, "users/$uid/settings?settingName=dubbing_visibility&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        result.data?.let { list ->
                            if (list.setting_name == "dubbing_visibility") {
                                //1:所有人可见，2:仅限好友，默认所有人可见,3:仅自己可见，默认：1
                                if (list.setting_value == 3) {
                                    headView.btnDubbing.setLockStatus(list.setting_value)
                                }
                            }
                        }
                        request(15)
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            18 -> {//查询用户自己的专辑
                OkClientHelper.get(this, "dialogAlbums?orderByField=albumSort&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        attachTalkAlbum(result.data)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showEmpty()
                    }
                }, "V4.3")
            }
            19 -> {
                OkClientHelper.get(this, "dialogAlbums/statistics", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as IntegerRespData
                        result.data?.let {
                            if (headView.linearTalkAlbum.childCount == 3) {
                                headView.linearTalkAlbum.findViewById<TextView>(R.id.tvTitle).text = "查看更多\n(${it.total})"
                            }
                        }
                    }
                }, "V4.3")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlbumEvent(event: IUpdateAlbumEvent) {
        request(4)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun albumEvent(event: IUpdateTalkAlbumEvent) {
        if (!needUpdateTalkAlbum)
            request(18)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_THEME) {
                data?.let {
                    val result = it.getSerializableExtra("result") as ArrayList<String>
                    if (result.size > 0) {
                        startActivityForResult(Intent(this, CropActivity::class.java)
                                .putExtra("resourceType", "23")
                                .putExtra("path", result[0]), REQUEST_CROP)
                    }
                }
            } else if (requestCode == REQUEST_CROP) {
                data?.let {
                    val path = it.getStringExtra("result")
                    updateCoverByCalendar(path)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        progressHandler.removeCallbacks(progressHandler)
    }

    override fun finish() {
        super.finish()
        OkClientHelper.cancelAllTask()
    }

    override fun onFriend() {
    }

    override fun onDelete(bean: BaseBean?) {
    }

    override fun onUnRecommend(bean: BaseBean?) {
    }

    override fun onReport(type: String?) {
    }

    override fun onRecommend(bean: BaseBean?) {
    }

    override fun onPrivacy(bean: BaseBean?) {
    }

    override fun onAdminFail() {
        dialogPwd?.setCallBack()
    }

    override fun onFailure(e: Any?) {
        transLayout.showContent()
    }

    override fun onComment(from_id: String?) {
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        try {
            val indexOf = otherData.indexOf(bean)
            otherData.remove(bean)
            otherAdapter.notifyItemRemoved(indexOf + 1)
            dialogPwd?.dismiss()
        } catch (e: Exception) {

        }
    }

    override fun onUnThumb(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onthumb(bean: BaseBean?) {
        otherAdapter.notifyHeart(bean)
        transLayout.showContent()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            commentBean?.let {
                if (it.voice_id == event.voiceId) {
                    /**
                     * 更新数据
                     */
                    it.chat_id = event.chatId
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (it.user_id.toString() == loginBean.user_id) {
                        it.chat_num++
                    } else {
                        it.dialog_num++
                    }
                    otherAdapter.notifyItemChanged(otherData.indexOf(it) + 1)
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    otherAdapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
            if (isSelf) {
                anim(AppTools.dp2px(this@UserHomeActivity, 60).toFloat())
            }
        }
    }

    /**
     * 发了新的心情, 更新日历
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewVoiceEvent(event: OperatorVoiceListEvent) {
        //当月
        if (event.type != 3) {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (uid == loginBean.user_id) {
                val yearMonth = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
                if (mData.size == 0) {
                    mData.add(0, UserDateListData().apply {
                        date = yearMonth.substring(0, 6)
                        year = yearMonth.substring(0, 4).toInt()
                        month = yearMonth.substring(4, 6).toInt()
                        contains = arrayOf(yearMonth)
                    })
                    adapter.notifyDataSetChanged()
                } else {
                    //查询是否存在值
                    if (mData.loop {
                                it.date == yearMonth.substring(0, 6)
                            }?.let {
                                it.contains = if (it.contains == null) arrayOf(yearMonth) else it.contains.plus(yearMonth)
                                adapter.notifyDataSetChanged()
                                it
                            } == null) {
                        //新增一条记录
                        mData.add(0, UserDateListData().apply {
                            date = yearMonth.substring(0, 6)
                            year = yearMonth.substring(0, 4).toInt()
                            month = yearMonth.substring(4, 6).toInt()
                            contains = arrayOf(yearMonth)
                        })
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    /**
     * 更新好友请求的状态,
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (uid == event.userId) {//是当前用户的状态变更s
            when (event.status) {
                2 -> {
                    relation = 2
                    //更新用户关系
                    otherData.forEach {
                        it.friend_status = 2
                    }
                    otherAdapter.notifyDataSetChanged()
                    request(10)
                }
                3 -> {//删除好友
                    relation = 0
                    otherData.forEach {
                        it.friend_status = 0
                    }
                    otherAdapter.notifyDataSetChanged()
                    request(10)
                }
                4 -> {
                    finish()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopicChangeEvent(event: ITopicChangeEvent) {
        if (event.userId == uid) {
            request(5)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlbumChangeEvent(event: IAlbumUpdate) {
        if (event.type == 0)
            needUpdateAlbum = true
        else {
            needUpdateTalkAlbum = true
        }
    }

}