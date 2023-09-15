package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.frag_alarm_recycler.view.*
import kotlinx.android.synthetic.main.layout_alarm_head.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.alarmService.configuration.EditedAlarm
import org.xiaoxingqi.alarmService.configuration.Prefs
import org.xiaoxingqi.alarmService.model.AlarmData
import org.xiaoxingqi.alarmService.model.AlarmValue
import org.xiaoxingqi.alarmService.model.Alarmtone
import org.xiaoxingqi.alarmService.model.DaysOfWeek
import org.xiaoxingqi.alarmService.ui.Comparators
import org.xiaoxingqi.alarmService.ui.UiStore
import org.xiaoxingqi.alarmService.utils.Optional
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App.Companion.container
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.modules.startNotify
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.GroupToggleView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.ViewToggleAlarm
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress

class AlarmFragment : BaseFragment(), ITabAlarmClickCall {

    override fun tabSelected(position: Int) {
        isCurrent = position == 3
    }

    companion object {
        private const val EDIT_TIME = 0x01
        private const val EDIT_ALARM = 0x02
    }

    //初始化打开首页
    private var isCurrent = true

    override fun tabClick(isVisible: Boolean) {
        try {
            if (mView!!.swipeRefresh.isEnabled != isVisible) {
                mView!!.swipeRefresh.isEnabled = isVisible
                mView!!.swipeRefresh.isRefreshing = false
            }
        } catch (e: Exception) {

        }
    }

    override fun doubleClickRefresh() {
    }

    private val editAlarm = EditedAlarm()
    private val alarms = container().alarms()
    private val prefs: Prefs = container().prefs()
    private var alarmsSub: Disposable = Disposables.disposed()
    private lateinit var uiStore: UiStore
    private lateinit var alarmData: AlarmValue
    private lateinit var sp: SharedPreferences
    private val store = container().store()

    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private lateinit var headView: View
    private var lastId: String = ""
    private lateinit var userInfo: UserInfoData
    private var playBean: BaseAlarmBean? = null
    private val audioPlayer: AudioPlayer by lazy {
        AudioPlayer(activity)
    }
    private var currentType = 0//0 下载 1 我的
    private var currentTagId = ""
    private lateinit var loadMoreView: View
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorEvent(event: SensorChangeMoodEvent) {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        if (headsetOn || a2dpOn || scoOn) {
            return
        }
        if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) {
            return
        }
        if (event.type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else if (event.type == 2) {
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
        return R.layout.frag_alarm_recycler
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(context)
        headView = LayoutInflater.from(context).inflate(R.layout.layout_alarm_head, view.recyclerView, false)
        view.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators), ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        sp = PreferenceManager.getDefaultSharedPreferences(activity)
        sp.edit().putString("prealarm_duration", "-1").putString("auto_silence", "5").putString("fade_in_time_sec", "1").apply()
        val onBackPressed = PublishSubject.create<String>()
        val editing: BehaviorSubject<EditedAlarm> = BehaviorSubject.createDefault(editAlarm)
        val transitioningToNewAlarmDetails: Subject<Boolean> = BehaviorSubject.createDefault(false)
        uiStore = object : UiStore {
            override fun editing(): BehaviorSubject<EditedAlarm> {//编辑数据
                return editing
            }

            override fun onBackPressed(): PublishSubject<String> {
                return onBackPressed
            }

            override fun createNewAlarm() {
                /**
                 * 创建新的闹钟
                 */
                transitioningToNewAlarmDetails.onNext(true)
                val newAlarm = alarms.createNewAlarm()
                editing.onNext(EditedAlarm(
                        isNew = true,
                        value = Optional.of(AlarmData.from(newAlarm.edit())),
                        id = newAlarm.id))
            }

            override fun transitioningToNewAlarmDetails(): Subject<Boolean> {
                return transitioningToNewAlarmDetails
            }

            override fun edit(id: Int) {
                alarms.getAlarm(id)?.let { alarm ->
                    editing.onNext(EditedAlarm(
                            isNew = false,
                            value = Optional.of(AlarmData.from(alarm.edit())),
                            id = id))
                }
            }

            override fun edit(id: Int, holder: Any?) {
            }

            override fun hideDetails() {
            }

            override fun hideDetails(holder: Any?) {
            }
        }
        alarmsSub = prefs.listRowLayout()
                .switchMap { uiStore.transitioningToNewAlarmDetails() }
                .switchMap { transitioning ->
                    if (transitioning)
                        Observable.never()
                    else
                        store.alarms()
                }
                .subscribe { alarms ->
                    //过滤时间一致的数据
                    val sorted = alarms
                            .sortedWith(Comparators.MinuteComparator())
                            .sortedWith(Comparators.HourComparator())
                            .sortedWith(Comparators.RepeatComparator())
                    if (sorted.isNotEmpty()) {
                        alarmData = sorted[0]
                        uiStore.edit(alarmData.id)
                        //展示数据
                        headView.tv_alarm_time.text = "${sorted[0].hour}:${if (sorted[0].minutes < 10) "0${sorted[0].minutes}" else "${sorted[0].minutes}"}"
                        headView.toggleAlarm.setAnimStatus(sorted[0].isEnabled)
                        if (!TextUtils.isEmpty(sorted[0].alarmtone.persistedString)) {
                            sorted[0].alarmtone.persistedString!!.split(",").filter {
                                !TextUtils.isEmpty(it) && it != ","
                            }.let {
                                headView.tvSetAlarm.text = if (it.size == 1 && it.contains(IConstant.LOCAL_DEFAULT_ALARM)) {
                                    resources.getString(R.string.string_alarm_7) + resources.getString(R.string.string_alarm_18)
                                } else {
                                    resources.getString(R.string.string_alarm_7) + "${resources.getString(R.string.string_diy_alarm_title)} ${it.size}"
                                }
                            }
                        }
                    }
                }
        EventBus.getDefault().register(this)
        userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<BaseAlarmBean>(context, R.layout.item_alarm_dub, mData, headView) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
//                helper!!.getView(R.id.tv_dubbing_count).visibility = View.GONE
                item!!.show(helper!!, activity!!, glideUtil) { exceptionShow(helper) }
                (helper.getView(R.id.voiceProgress) as VoiceProgress).apply {
                    data = item
                    val viewSeekProgress = findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
                    this.setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = audioPlayer.currentPosition.toInt()
                                audioPlayer.stop()
                                finish()
                                return@setOnClickListener
                            } else {
                                audioPlayer.stop()
                                progressHandler.stop()
                            }
                        }
                        playBean?.let {
                            if (item !== it) {
                                it.isPlaying = false
                                it.pasuePosition = 0
                            }
                        }
                        download(helper, item)
                    }
                    viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
                        override fun startTrack() {
                            if (!viewSeekProgress.isPressed) {
                                if (audioPlayer.isPlaying) {
                                    item.allDuration = audioPlayer.duration
                                    progressHandler.stop()
                                    audioPlayer.stop()
                                    item.isPlaying = false
                                    finish()
                                }
                            }
                        }

                        override fun endTrack(progress: Float) {
                            item.pasuePosition = (progress * item.allDuration).toInt()
                            download(helper, item)
                        }
                    })
                    findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                audioPlayer.stop()
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = 1
                                finish()
                            }
                        }
                        playBean?.let {
                            it.isPlaying = false
                            it.pasuePosition = 0
                        }
                        item.pasuePosition = 0
                        download(helper, item)
                    }
                }
                showOperatorTitle(helper.getView(R.id.linearOperate), item)
                helper.getView(R.id.iv_more).isSelected = item.isSelf
                helper.getView(R.id.linear_angel).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "1")
                        deleteVote(item, helper.getView(R.id.linearOperate), 1)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 1)
                }
                helper.getView(R.id.linear_monster).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "2")
                        deleteVote(item, helper.getView(R.id.linearOperate), 2)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 2)
                }
                helper.getView(R.id.linear_god).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "3")
                        deleteVote(item, helper.getView(R.id.linearOperate), 3)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 3)
                }
                helper.getView(R.id.tv_alarm_word).setOnClickListener {
                    startActivity(Intent(activity!!, WordingVoiceActivity::class.java)
                            .putExtra("id", item.line_id)
                            .putExtra("userInfo", item.to_user_info)
                            .putExtra("toUserId", item.to_user_id)
                            .putExtra("tagName", item.line.tag_name)
                            .putExtra("dubbingNum", item.line.dubbing_num)
                            .putExtra("lineContent", item.line.line_content))
                }
                helper.getView(R.id.relative_Report).setOnClickListener {
                    if (item.isSelf) {
                        //删除或者只为匿名
                        DialogAlarmEdit(activity!!).setProperty(item.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete ->
                                    DialogDeleteChatComment(activity!!).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                        deleteDub(item)
                                    }).show()
                                R.id.tv_Self -> editDub(item)
                            }
                        }).show()
                    } else {
                        DialogNormalReport(activity!!).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Attach -> reportNormalItem(item.id, "1", 8)
                                R.id.tv_Porn -> reportNormalItem(item.id, "2", 8)
                                R.id.tv_Junk -> reportNormalItem(item.id, "3", 8)
                                R.id.tv_illegal -> reportNormalItem(item.id, "4", 8)
                            }
                        }).show()
                    }
                }
                helper.getView(R.id.iv_Privacy).setOnClickListener {
                    //下载 判断本地是否有缓存
                    if (!item.checkExist().isDownload) {
                        activity!!.downFile(item, "${item.from_user_info.nick_name} ${AppTools.getSuffix(item.dubbing_url)}") {
                            //下载成功
                            helper.getView(R.id.iv_Privacy).visibility = View.GONE
                            helper.getView(R.id.tv_download).visibility = View.VISIBLE
                            EventBus.getDefault().post(AlarmUpdateEvent(5, 5).apply {
                                dubbingId = item.id
                            })
                        }
                    }
                }
                helper.getView(R.id.tv_download).setOnClickListener {
                    if (item.checkExist().isDownload)
                        DialogDeleteLocalAlarm(activity!!).setOnClickListener(View.OnClickListener {
                            item.deleteFile {
                                helper.getView(R.id.iv_Privacy).visibility = View.VISIBLE
                                helper.getView(R.id.tv_download).visibility = View.GONE
                                EventBus.getDefault().post(AlarmUpdateEvent(3, 3).apply {
                                    deletePath = "${item.from_user_info.nick_name} ${AppTools.getSuffix(item.dubbing_url)}"
                                })
                            }
                        }).show()
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: BaseAlarmBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
            }

            override fun changeStatue(isSelect: Boolean) {
                super.changeStatue(isSelect)
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun exceptionShow(helper: BaseAdapterHelper) {
                Glide.with(helper.getImageView(R.id.roundImg))
                        .load(R.mipmap.icon_user_default)
                        .into(helper.getImageView(R.id.roundImg))
                helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_alarm_anonymous)
                helper.getView(R.id.roundImg).setOnClickListener(null)
                helper.getView(R.id.iv_user_type).visibility = View.GONE
            }
        }
        mView!!.recyclerView.adapter = adapter
        loadMoreView = LayoutInflater.from(activity).inflate(R.layout.view_loadmore_alarm, mView!!.recyclerView, false)
        try {
            glideUtil.loadGlide(userInfo.data.avatar_url, loadMoreView.findViewById(R.id.ivUserLogo), 0, glideUtil.getLastModified(userInfo.data.avatar_url))
            loadMoreView.findViewById<TextView>(R.id.tvUserName).text = userInfo.data.nick_name
            loadMoreView.findViewById<View>(R.id.ivUserType).visibility = if (userInfo.data.identity_type == 0) View.GONE else View.VISIBLE
            loadMoreView.findViewById<View>(R.id.ivUserType).isSelected = userInfo.data.identity_type == 1
        } catch (e: Exception) {
        }
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMoreView)
        request(0)
    }

    @SuppressLint("SetTextI18n")
    private fun showOperatorTitle(view: View, bean: BaseAlarmBean) {
        try {
            view.findViewById<TextView>(R.id.tv_angel).text = "您是天使" + if (bean.vote_option_one > 0) " ${bean.vote_option_one}" else ""
            view.findViewById<TextView>(R.id.tv_monster).text = "您是恶魔" + if (bean.vote_option_two > 0) " ${bean.vote_option_two}" else ""
            view.findViewById<TextView>(R.id.tv_god).text = "您是神" + if (bean.vote_option_three > 0) " ${bean.vote_option_three}" else ""
        } catch (e: Exception) {
        }
    }

    private fun down(item: BaseAlarmBean, isScroll: Boolean = true) {
        if (TextUtils.isEmpty(item.dubbing_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        try {
            if (isScroll && mView!!.recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                mView!!.recyclerView.scroll(activity, mData.indexOf(item) + 1)
            }
            playBean = item
            activity!!.downPlay(item.dubbing_url) { it, _ ->
                audioPlayer.setDataSource(it)
                audioPlayer.bean = item
                audioPlayer.start(if (SPUtils.getBoolean(context, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            }
        } catch (e: Exception) {
        }
    }

    private fun nextAudio() {
        if (SPUtils.getBoolean(activity, IConstant.PLAY_MENU_AUTO, false)) {
            playBean?.let {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) mView!!.recyclerView.scrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    request(0)
                }
            }
        } else {
            mView!!.customPlayMenu.isSelected = false
        }
    }

    private var headLength = 100f
    private var allLength = 0f
    override fun initEvent() {
        loadMoreView.findViewById<View>(R.id.tvFindMore).setOnClickListener {
            (activity as AlarmListActivity).changeCurrentPage(0)
        }
        loadMoreView.findViewById<View>(R.id.tvMoreDubbing).setOnClickListener {
            (activity as AlarmListActivity).changeCurrentPage(2)
        }
        headView.headItemClickView.setOnItemClick(object : OnAlarmItemClickListener {
            override fun itemClick(type: Int) {
                currentTagId = if (type == 0) "" else type.toString()
                lastId = ""
                mData.clear()
                adapter.notifyDataSetChanged()
                request(currentType)
            }
        })
        headView.headButton.setOnChildClickListener(object : GroupToggleView.OnChildClickListener {
            override fun onClick(position: Int, childView: View) {
                currentType = position
                lastId = ""
                mData.clear()
                adapter.notifyDataSetChanged()
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                if (currentType == 0) {
                    loadMoreView.findViewById<View>(R.id.relativeDownload).visibility = View.VISIBLE
                    loadMoreView.findViewById<View>(R.id.relativeMy).visibility = View.GONE
                } else {
                    loadMoreView.findViewById<View>(R.id.relativeDownload).visibility = View.GONE
                    loadMoreView.findViewById<View>(R.id.relativeMy).visibility = View.VISIBLE
                }
                lastId = ""
                request(currentType)
            }
        })
        headView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                headView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                headLength = headView.height - AppTools.dp2pxFloat(activity, 20)
            }
        })
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                mView!!.customPlayMenu.isSelected = true
                playBean?.let {
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                }
                adapter.changeStatue(false)
                progressHandler.stop()
                //next
                try {
                    nextAudio()
                } catch (e: Exception) {
                }
            }

            override fun onInterrupt() {
                mView!!.customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                }
                adapter.changeStatue(false)
                progressHandler.stop()
            }
        }
        mView!!.customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        var index = mData.indexOf(it) + 1
                        /*  if (index >= mData.size) {
                              index = 0
                              mView!!.recyclerView.scrollToPosition(0)
                          }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(0)
                        }
                        down(mData[index], index != 0)
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
                        if (mData.size > 0) {
                            down(mData[0])
                        }
                    }
                }
            }

            override fun pre() {
                audioPlayer.stop()
                playBean?.let {
                    var index = mData.indexOf(it) - 1
                    if (index < 0) index = 0
                    down(mData[index])
                }
            }

            override fun top() {
                allLength = 0f
                mView!!.viewTopDividing.visibility = View.VISIBLE
                if (!audioPlayer.isPlaying) {
                    mView!!.recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        mView!!.recyclerView.scroll(activity, mData.indexOf(it) + 1, false)
                    }
            }
        })
        headView.toggleAlarm.setOnChangeListener(object : ViewToggleAlarm.OnToggleChangeListener {
            override fun change(isOpen: Boolean) {
                if (isOpen) {
                    if (!NotificationManagerCompat.from(activity!!).areNotificationsEnabled()) {
                        headView.toggleAlarm.setAnimStatus(false)
                        DialogAlarmPermissionDialog(activity!!).setOnClickListener(View.OnClickListener {
                            activity!!.startNotify()
                        }).show()
                    } else {//开启闹钟
                        alarms.getAlarm(alarmData.id)?.also { alarm ->
                            alarm.edit()
                                    .withIsEnabled(true)
                                    .commit()
                        }
                        if (SPUtils.getBoolean(activity, IConstant.IGNORE_ALARM_HINT, true))
                            DialogAlarmWarn(activity!!).show()
                    }
                } else {
                    //关闭闹钟
                    alarms.getAlarm(alarmData.id)?.also { alarm ->
                        alarm.edit()
                                .withIsEnabled(false)
                                .commit()
                    }
                }
            }
        })
        headView.tvSetAlarm.setOnClickListener {
            startActivityForResult(Intent(activity, SelectAlarmActivity::class.java)
                    .putExtra("alarmTone", alarmData.alarmtone.persistedString)
                    , EDIT_ALARM)
        }
        headView.iv_edit_time.setOnClickListener {
            startActivityForResult(Intent(activity, AlarmTimeGalleryActivity::class.java)
                    .putExtra("hour", alarmData.hour)
                    .putExtra("minute", alarmData.minutes)
                    .putExtra("daysOfWeek", alarmData.daysOfWeek.coded)
                    , EDIT_TIME)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
            lastId = ""
            request(currentType)
        }
        adapter.setOnLoadListener {
            request(currentType)
        }
    }

    //删除投票
    private fun deleteVote(item: BaseAlarmBean, view: View, voteType: Int) {
        activity!!.deleteVote(item, null, view, voteType) { childView, bean ->
            showOperatorTitle(childView, bean)
        }
    }

    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        activity!!.artVote(bean, null, view, type) { childView, item ->
            showOperatorTitle(childView, item)
        }
    }

    private fun deleteDub(bean: BaseAlarmBean) {
        activity!!.deleteDub(bean, null) {
            mData.remove(it)
            adapter.notifyDataSetChanged()
            EventBus.getDefault().post(AlarmUpdateEvent(4, 4).apply {
                dubbingId = it.id
                deletePath = it.line_id
            })
        }
    }

    /*编辑可见性*/
    private fun editDub(bean: BaseAlarmBean) {
        activity!!.editDub(bean, null) {
            adapter.notifyItemChanged(mData.indexOf(it))
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "dubbings/downloadlog?lastId=$lastId&tagId=$currentTagId", WordingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        mView!!.swipeRefresh.isRefreshing = false
                        result as WordingData
                        if (result.code == 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                allLength = 0f
                                mData.clear()
                                result.data?.forEach {
                                    mData.add(it.checkUser(userInfo).checkExist())
                                }
                                adapter.notifyDataSetChanged()
                            } else {
                                result.data?.forEach {
                                    mData.add(it.checkUser(userInfo).checkExist())
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                            if (result.data != null && result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            } else {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                            }
                            if (mData.size > 0)
                                lastId = mData[mData.size - 1].id
                        }
                        if (mData.size == 0) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }, "V4.3")
            }
            1 -> {
                //请求我的界面
                OkClientHelper.get(activity, "users/${userInfo.data.user_id}/dubbings?lastId=$lastId&tagId=$currentTagId", WordingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as WordingData
                        mView!!.swipeRefresh.isRefreshing = false
                        allLength = 0f
                        result.data?.let {
                            if (TextUtils.isEmpty(lastId)) {
                                allLength = 0f
                                mData.clear()
                                result.data?.forEach {
                                    mData.add(it.checkUser(userInfo).checkExist())
                                }
                                adapter.notifyDataSetChanged()
                            } else {
                                result.data?.forEach {
                                    mData.add(it.checkUser(userInfo).checkExist())
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                        }
                        if (result.data != null && result.data.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        } else {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        }
                        if (mData.size > 0)
                            lastId = mData[mData.size - 1].id
                        if (mData.size == 0) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EDIT_TIME) {
                data?.let {
                    val hour = it.getStringExtra("hour")
                    val minute = it.getStringExtra("minute")
                    headView.tv_alarm_time.text = "$hour:$minute"
                    val snoozeDuration = it.getStringExtra("snooze_duration")
                    val coded = it.getIntExtra("daysOfWeek", 0)
                    sp.edit().putString("snooze_duration", snoozeDuration).apply()
                    modify { prev ->
                        prev.copy(daysOfWeek = DaysOfWeek(coded))
                    }
                    uiStore.editing().firstOrError().subscribe { editorToSave ->
                        alarms.getAlarm(uiStore.editing().value!!.id)?.run {
                            edit().copy(alarmValue = editorToSave.value.get())
                                    .withIsEnabled(alarmData.isEnabled)
                                    .withHour(hour.toInt())
                                    .withMinutes(minute.toInt())
                                    .commit()
                        }
                    }
                }
            } else if (requestCode == EDIT_ALARM) {
                //获取闹钟的地址
                data?.let {
                    val tonePath = it.getStringExtra("path")
                    val vibrate = it.getBooleanExtra("vibrate", false)
                    sp.edit().putBoolean("vibrate", vibrate).apply()
                    modify { prev ->
                        prev.copy(alarmtone = Alarmtone.Sound(tonePath))
                    }
                    uiStore.editing().firstOrError().subscribe { editorToSave ->
                        alarms.getAlarm(uiStore.editing().value!!.id)?.run {
                            edit().copy(alarmValue = editorToSave.value.get())
                                    .withIsEnabled(alarmData.isEnabled)
                                    .commit()
                        }
                    }
                }
            }
        }
    }

    private fun modify(function: (AlarmData) -> AlarmData) {
        uiStore.editing().value?.let { editedAlarm ->
            val modified: Optional<AlarmData> = editedAlarm.value.of
                    ?.let(function)
                    .let { Optional.fromNullable(it) }
            uiStore.editing().onNext(editedAlarm.copy(value = modified))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlarmEvent(event: AlarmUpdateEvent) {
        if (event.type == 3 && event.updateResource == 3) {
            if (!isCurrent || !isResumed)
                if (!TextUtils.isEmpty(event.deletePath)) {
                    mData.loop {
                        event.deletePath == "${it.from_user_info.nick_name} ${AppTools.getSuffix(it.dubbing_url)}"
                    }?.let {
                        it.isDownload = false
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
                }
        } else if (event.type == 4 && event.updateResource == 4) {
            //主动删除配音
            if (!isCurrent || !isResumed)
                mData.loop {
                    it.id == event.dubbingId
                }?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
        } else if (event.type == 5) {
            if (!isCurrent || !isResumed) {
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.isDownload = true
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        } else if (event.type == 6) {
            if (!isCurrent || !isResumed) {
                if (!TextUtils.isEmpty(event.dubbingId))
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.is_anonymous = event.deletePath
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
            }
        } else if (event.updateResource == 7) {
            if (!isCurrent || !isResumed)
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        if (!TextUtils.isEmpty(it.vote_id) && !TextUtils.isEmpty(it.vote_option)) {
                            when (it.vote_option) {
                                "1" -> it.vote_option_one--
                                "2" -> it.vote_option_two--
                                "3" -> it.vote_option_three--
                            }
                        }
                        it.vote_id = event.deletePath
                        it.vote_option = event.type.toString()
                        when (event.type) {
                            1 -> it.vote_option_one++
                            2 -> it.vote_option_two++
                            3 -> it.vote_option_three++
                        }
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
                }
        } else if (event.updateResource == 8) {
            if (!isCurrent || !isResumed)
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.vote_id = ""
                        when (it.vote_option) {
                            "1" -> it.vote_option_one--
                            "2" -> it.vote_option_two--
                            "3" -> it.vote_option_three--
                        }
                        it.vote_option = ""
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
                }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isCurrent || !isResumed) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
        }
    }

    override fun onDestroy() {
        try {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
        } catch (e: Exception) {
        }
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        alarmsSub.dispose()
        progressHandler.removeCallbacks(progressHandler)
    }
}