package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_moive.btn_Back
import kotlinx.android.synthetic.main.activity_moive.swipeRefresh
import kotlinx.android.synthetic.main.activity_moive.transLayout
import kotlinx.android.synthetic.main.activity_user_topic_list.*
import kotlinx.android.synthetic.main.head_topic_manager.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.UserMoviesData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.*
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.File
import java.io.IOException

/**
 * 用户话题模块
 */
class UserTopicManagerActivity : BaseAct(), ItemOperator {
    private lateinit var headView: View
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy { ArrayList<BaseBean>() }
    private var lastId: String = ""
    private var searchTag: String? = null
    private var searchTagId = "0"
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: BaseBean? = null
    private var userName: String? = null
    private var userId: String? = null
    override fun writeHeadSet(): Boolean {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        return headsetOn || a2dpOn || scoOn
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

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_topic_list
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
        recyclerReview.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.head_topic_manager, recyclerReview, false)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setItemOperator(this)
        userId = intent.getStringExtra("uid")
        userName = intent.getStringExtra("title")
        audioPlayer = AudioPlayer(this)
        searchTag = intent.getStringExtra("tag")
        tv_Title.text = "#$searchTag#"
        searchTagId = intent.getStringExtra("tagId")
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            ivRecord.visibility = View.VISIBLE
        }
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_voice, mData, headView) {
            var cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {

                val statusText = helper!!.getView(R.id.linearStatusText) as LinearStatusText
                glideUtil.loadGlide(item!!.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(this@UserTopicManagerActivity, item.created_at)
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == 1) View.VISIBLE else View.GONE
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                (helper.getView(R.id.imageGroup) as ImageGroupView).setData(item.img_list)
                if (!TextUtils.isEmpty(item.topic_name)) {
                    helper.getTextView(R.id.tv_Action).text = "#${item.topic_name}#"
                } else {
                    helper.getTextView(R.id.tv_Action).text = ""
                }
                helper.getTextView(R.id.tv_Sub).text = if (item.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${item.played_num}"
                if (!TextUtils.isEmpty(item.is_shared)) {//是自己
                    helper.getView(R.id.iv_Thumb).isSelected = item.is_shared == "1"
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                    helper.getView(R.id.heartView).visibility = View.GONE
                    statusText.visibility = View.GONE
                    helper.getView(R.id.tv_Sub).visibility = if (item.is_private == 1) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_Privacy).visibility = if (item.is_private == 0) View.GONE else View.VISIBLE
                    if (item.is_shared == "1") {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_unshare_world)
                    } else {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_share_world)
                    }
                    helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing) + if (item.chat_num == 0) {
                        ""
                    } else {
                        " " + item.chat_num
                    }
                } else {//
                    helper.getView(R.id.iv_Privacy).visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_gongming)
                    if (item.dialog_num == 0) {
                        helper.getTextView(R.id.tv_Recommend).text = resources.getString(R.string.string_echoing)
                    } else {
                        helper.getTextView(R.id.tv_Recommend).text = "${resources.getString(R.string.string_Talks)} ${item.dialog_num}"
                    }
                    helper.getView(R.id.tv_Sub).visibility = View.GONE
                    when {
                        item.friend_status == 2 -> {
                            statusText.visibility = View.GONE
                            helper.getView(R.id.tv_Sub).visibility = View.GONE
                        }
                        item.friend_status == 1 -> {
                            statusText.visibility = View.VISIBLE
                            statusText.isSelected = false
                        }
                        item.friend_status == 0 -> {
                            statusText.isSelected = true
                            statusText.visibility = View.VISIBLE
                        }
                    }
                }
                (helper.getView(R.id.imageGroup) as ImageGroupView).setOnClickViewListener {
                    startActivity(Intent(this@UserTopicManagerActivity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    overridePendingTransition(0, 0)
                }
                helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(this@UserTopicManagerActivity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id.toString())
                                .putExtra("uid", item.user_id.toString())
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num > 0) {
                            startActivity(Intent(this@UserTopicManagerActivity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id.toString())
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user.id))
                        } else {
                            queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        }
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    item.topic_name?.let {
                        if (it != searchTag)
                            startActivity(Intent(this@UserTopicManagerActivity, UserTopicManagerActivity::class.java)
                                    .putExtra("tagId", item.topic_id.toString())
                                    .putExtra("tag", item.topic_name))
                    }
                }
                statusText.setOnClickListener {
                    if (statusText.isSelected) {
                        transLayout.showProgress()
                        requestFriends(item, it)
                    }
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb))
                    } else {//共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
//                            transLayout.showProgress()
                            item.isNetStatus = true
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            transLayout.showProgress()
                            LightUtils.addItem(item.voice_id)
                            thumb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    UserDetailsActivity.start(this@UserTopicManagerActivity, item.user.avatar_url, item.user.id, helper.getImageView(R.id.roundImg))
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        DialogMore(this@UserTopicManagerActivity).setEditable(item.resource_id == "0").setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_ShareWechat -> {
                                    DialogShare(this@UserTopicManagerActivity, true).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearWechat -> {
                                                ShareUtils.share(this@UserTopicManagerActivity, Wechat.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearMoment -> {
                                                ShareUtils.share(this@UserTopicManagerActivity, WechatMoments.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_ShareWeibo -> {
                                    ShareUtils.share(this@UserTopicManagerActivity, SinaWeibo.NAME, item.voice_url, item.share_url, "", null)
                                }
                                R.id.tv_ShareQQ -> {
                                    DialogShare(this@UserTopicManagerActivity, false).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearQQ -> {
                                                ShareUtils.share(this@UserTopicManagerActivity, QQ.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearQzone -> {
                                                ShareUtils.share(this@UserTopicManagerActivity, QZone.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@UserTopicManagerActivity).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(this@UserTopicManagerActivity).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                                R.id.tv_add_album -> {
                                    startActivity(Intent(this@UserTopicManagerActivity, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
                                }
                                R.id.tvReEditVoice -> {
                                    startActivity<EditSendAct>("data" to item)
                                }
                            }
                        }).show()
                    } else {
                        DialogReport(this@UserTopicManagerActivity).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@UserTopicManagerActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(this@UserTopicManagerActivity).setOnResultListener(OnReportItemListener {
                                        transLayout.showProgress()
                                        report(item.voice_id, it, item.user.id)
                                    }).show()
                                R.id.tv_report_normal -> {
                                    DialogNormalReport(this@UserTopicManagerActivity).setOnClickListener(View.OnClickListener { report ->
                                        when (report.id) {
                                            R.id.tv_Attach -> {
                                                reportNormalItem(item.voice_id, "1")
                                            }
                                            R.id.tv_Porn -> {
                                                reportNormalItem(item.voice_id, "2")
                                            }
                                            R.id.tv_Junk -> {
                                                reportNormalItem(item.voice_id, "3")
                                            }
                                            R.id.tv_illegal -> {
                                                reportNormalItem(item.voice_id, "4")
                                            }
                                        }
                                    }).show()
                                }
                            }
                        }).show()
                    }
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                voiceProgress.setOnClickListener {
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
                    download(helper, item)
                }
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
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(helper, item)
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
                    download(helper, item)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
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
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
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
                            helper.getView(R.id.iv_Thumb).let {
                                it.isSelected = b.is_collected == 1
                                if (recyclerReview.isOffsetScreen(mData.indexOf(bean) + 1)) {
                                    SmallBang.attach2Window(this@UserTopicManagerActivity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }
        }
        recyclerReview.adapter = adapter
        adapter.setLoadMoreEnable(recyclerReview, recyclerReview.layoutManager, LayoutInflater.from(this).inflate(R.layout.loadmore_padding_bottom, recyclerReview, false))
        searchContent()
        request(0)
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerReview.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerReview.scroll(this@UserTopicManagerActivity, mData.indexOf(item) + 1)
        }
        if (TextUtils.isEmpty(item.voice_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        try {
            playBean = item
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this@UserTopicManagerActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this@UserTopicManagerActivity, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this@UserTopicManagerActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { showToast(VolleyErrorHelper.getMessage(it)) })
            }
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                        it.is_collected = 1
                        thumb(it, null, 5)
                    }
                }
                progressHandler.stop()
                try {
                    playBean?.let {
                        if (SPUtils.getBoolean(this@UserTopicManagerActivity, IConstant.PLAY_MENU_AUTO, false)) {
                            var index = mData.indexOf(it) + 1
                            if (index >= mData.size) index = 0
                            if (index == 0) recyclerReview.smoothScrollToPosition(0)
                            down(mData[index], index != 0)
                            if (mData.indexOf(it) == mData.size - 2) {
                                request(1)
                            }
                        } else {
                            customPlayMenu.isSelected = false
                        }
                    }
                } catch (e: Exception) {
                }
                adapter.animStatus(false)
            }

            override fun onInterrupt() {
                customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                }
                adapter.animStatus(false)
                progressHandler.stop()
            }

            override fun onPrepared() {
                customPlayMenu.isSelected = true
                playBean?.let {
                    if (TextUtils.isEmpty(it.is_shared) && LightUtils.contains(it.voice_id)) {
                        adapter.animStatus(true)
                    }
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    addPlays(it, null) { bean ->
                        adapter.notifyHelperStatus(bean)
                    }
                    it.isPlaying = true
                }
                progressHandler.start()
            }
        }
        customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        var index = mData.indexOf(it) + 1
                        /*if (index >= mData.size) {
                            index = 0
                            recyclerReview.smoothScrollToPosition(0)
                        }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(1)
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
                if (!audioPlayer.isPlaying) {
                    recyclerReview.scrollToPosition(0)
                } else
                    playBean.let {
                        recyclerReview.scroll(this@UserTopicManagerActivity, mData.indexOf(it) + 1, false)
                    }
            }
        })
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(1)
        }
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
            searchContent()
        }
        transLayout.findViewById<View>(R.id.tv_CreateTopic).setOnClickListener {
            startActivity(Intent(this, SendAct::class.java)
                    .putExtra("type", 3)
                    .putExtra("topicName", searchTag)
                    .putExtra("topicId", if (searchTagId == "0") "" else searchTagId))
            overridePendingTransition(R.anim.operate_enter, 0)
        }
        ivRecord.setOnClickListener {
            startActivity(Intent(this, RecordTransparentActivity::class.java)
                    .putExtra("type", 3)
                    .putExtra("topicName", searchTag)
                    .putExtra("topicId", if (searchTagId == "0") "" else searchTagId))
            overridePendingTransition(0, 0)
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "users/$userId/favorite/topics/$searchTagId/voices?lastId=$lastId", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as VoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (item in result.data) {
                            mData.add(item)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    result.data?.let {
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    }
                    lastId = mData[mData.size - 1].voice_id
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                transLayout.showOffline()
            }
        }, "V3.2")
    }

    @SuppressLint("SetTextI18n")
    private fun searchContent() {
        OkClientHelper.get(this, "users/$userId/favorite/topics/$searchTagId", UserMoviesData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserMoviesData
                if (result.data != null) {
                    headView.tvTopicTitle.text = userName + "  " + String.format(resources.getString(R.string.string_topic_manager_10), result.data.total)
                } else {
                    headView.tvTopicTitle.text = userName + "  " + String.format(resources.getString(R.string.string_topic_manager_10), "0")
                }
            }

            override fun onFailure(any: Any?) {
                headView.tvTopicTitle.text = userName + "  " + String.format(resources.getString(R.string.string_topic_manager_10), "0")
            }
        }, "V3.2")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))

                }
            }
        }
    }

    override fun onDelete(bean: BaseBean?) {
        transLayout.showContent()
        showToast("删除成功")
        mData.remove(bean)
        adapter.notifyDataSetChanged()
    }

    override fun onRecommend(bean: BaseBean?) {

    }

    override fun onUnRecommend(bean: BaseBean?) {

    }

    override fun onthumb(bean: BaseBean?) {
        adapter.notifyHeart(bean)
        transLayout.showContent()
    }

    override fun onUnThumb(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onFailure(e: Any?) {
        showToast(e.toString())
        transLayout.showContent()
    }

    override fun onComment(from_id: String?) {
        commentBean?.let {
            it.dialog_num = 1
            it.chat_id = from_id
            adapter.notifyDataSetChanged()
        }
        transLayout.showContent()
    }

    override fun onReport(type: String) {
        transLayout.showContent()
        DialogPBBlack(this).setType(type).show()
    }

    override fun onFriend() {
        transLayout.showContent()
    }

    override fun onPrivacy(bean: BaseBean?) {
        adapter.notifyItemChanged(mData.indexOf(bean) + 1)
        transLayout.showContent()
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        try {
            val indexOf = mData.indexOf(bean)
            mData.remove(bean)
            adapter.notifyItemRemoved(indexOf + 1)
            dialogPwd?.dismiss()
        } catch (e: Exception) {

        }
    }

    override fun onAdminFail() {
        dialogPwd?.setCallBack()
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
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
        }
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
                    adapter.notifyItemChanged(mData.indexOf(it) + 1)
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun topicChangeEvent(event: ChangeTopicInfoEvent) {
        mData.loop {
            event.voiceId == it.voice_id
        }?.let {
            try {
                it.topic_id = event.topicId.toInt()
                it.topic_name = event.topicName
                adapter.notifyItemChanged(mData.indexOf(it) + 1)
            } catch (e: Exception) {
            }
        }
    }

    override fun finish() {
        super.finish()
        audioPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }


}