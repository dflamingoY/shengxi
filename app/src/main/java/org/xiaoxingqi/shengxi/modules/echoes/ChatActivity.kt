package org.xiaoxingqi.shengxi.modules.echoes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.view_progress_speed.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddTalkAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.time.DateUtils
import org.xiaoxingqi.shengxi.wedgit.EchoesProgress
import org.xiaoxingqi.shengxi.wedgit.MsgThumbImageView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

/**
 * 1.出现私聊发送失败, 界面无消息更新
 *
 */
class ChatActivity : BaseAct(), IAudioRecordCallback {
    companion object {
        private const val REQUEST_PHOTO = 0x11
        private const val REQUEST_PRETREATMENT = 0x12
        var instances: ChatActivity? = null
    }

    private var audioMessageHelper: AudioRecorder? = null
    private var playBean: TalkListData.TalkListBean? = null
    private var lastId: String? = null
    private var chatId: String? = null
    private var uid: String? = null
    private lateinit var adapter: QuickAdapter<TalkListData.TalkListBean>
    private val mData by lazy { ArrayList<TalkListData.TalkListBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var deleteItem: TalkListData.TalkListBean? = null
    private var isScrolled = false
    private lateinit var loginBean: LoginData.LoginBean
    private var isHelpPage = false
    private var isTopicType = false
    private var isHasCache = 0
    private var isCactus = "0"//自己是否是仙人掌
    private var url: String? = null

    override fun writeHeadSet(): Boolean {
        return if (audioPlayer.isPlaying) {
            val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
            val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
            val scoOn = audioPlayer.audioManager.isBluetoothScoOn
            headsetOn || a2dpOn || scoOn
        } else {
            false
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
        return R.layout.activity_chat
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators), ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(50) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    override fun initData() {
        instances = this
        sendObserver()
        audioPlayer = AudioPlayer(this)
        audioMessageHelper = AudioRecorder(this, RecordType.AAC, 120, this)
        isTopicType = intent.getIntExtra("topicType", 0) == 1
        intent.getStringExtra("userName")?.let {
            tv_Title.text = it
        }
        uid = intent.getStringExtra("uid")
        isHelpPage = intent.getBooleanExtra("isHelp", false)//是否是密码帮助界面跳转 头像不可点击,不可跳转,
        if (isHelpPage) {
            tvChatXE.text = "密码忘记了嘛?需要帮助请给小二留言\n(休息时间若不能及时回复还请耐心等待)"
        }
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        isCactus = userInfo.data.flag
        val createAt = intent.getIntExtra("createAt", 0)
        when {
            "1" == uid -> {//与声兮小二聊天
                team_notify_bar_panel.visibility = View.VISIBLE
                relative_normal_custom.visibility = View.VISIBLE
                ivByeByeTextRuler.visibility = View.GONE
                tvCountDown.visibility = View.GONE
            }
            "1" == loginBean.user_id -> {//户主是声兮小二
                tv_pretreatment.visibility = View.VISIBLE
                team_notify_bar_panel.visibility = View.VISIBLE
                tv_customer_type.visibility = View.VISIBLE
                iv_Other.setImageResource(R.mipmap.icon_album_setting)
                ivByeByeTextRuler.visibility = View.GONE
                queryDevice()
                queryHobby(1)
                tvCountDown.visibility = View.GONE
            }
            else -> {
                team_notify_bar_panel.visibility = View.GONE
                queryHobby(1)
                tvCountDown.visibility = View.VISIBLE
//                tvCountDown.text = String.format(resources.getString(R.string.string_48), ceil((createAt + 60 * 60 * 24 * 30 - System.currentTimeMillis() / 1000f) / (24 * 60f * 60)).toInt().toString())
            }
        }
        chatId = intent.getStringExtra("chatId")
        url = if (TextUtils.isEmpty(chatId)) {
            "chats/2/$uid/0"
        } else {
            queryTalkInfo()
            "chats/$chatId"
        }
        /**
         * 请求权限  下载和录音
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
        adapter = object : QuickAdapter<TalkListData.TalkListBean>(this, R.layout.item_talk_list, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            var hasNextPlay = false//是否自动播放吓一条音频
            override fun convert(helper: BaseAdapterHelper?, item: TalkListData.TalkListBean?) {
                if (item!!.is_self == 1) {
                    helper!!.getView(R.id.cardLeft).visibility = View.GONE
                    helper.getView(R.id.cardRight).visibility = View.VISIBLE
                    helper.getView(R.id.viewReadStatus).visibility = View.GONE
                    helper.getView(R.id.iv_user_type_right).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_right).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_left).visibility = View.GONE
                    (helper.getView(R.id.linear_content) as LinearLayout).gravity = Gravity.RIGHT
                    helper.getView(R.id.ivOfficialRight).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                    helper.getView(R.id.ivOfficialLeft).visibility = View.GONE
                } else {
                    helper!!.getView(R.id.cardLeft).visibility = View.VISIBLE
                    (helper.getView(R.id.linear_content) as LinearLayout).gravity = Gravity.LEFT
                    helper.getView(R.id.cardRight).visibility = View.GONE
                    helper.getView(R.id.viewReadStatus).visibility = if (item.read_at == 0 && item.resource_type == "1") View.VISIBLE else View.GONE
                    helper.getView(R.id.iv_user_type_left).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_left).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_right).visibility = View.GONE
                    helper.getView(R.id.ivOfficialLeft).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                    helper.getView(R.id.ivOfficialRight).visibility = View.GONE
                }
                helper.getView(R.id.frameResend).visibility = if (item.isCache) View.VISIBLE else View.GONE
                val position = helper.itemView.tag as Int
                try {
                    if (position != mData.size - 1) {
                        if (DateUtils.isCloseEnough(item.created_at * 1000L, mData[position + 1].created_at * 1000L)) {
                            helper.getTextView(R.id.tv_Time).visibility = View.GONE
                        } else {
                            helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                            helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@ChatActivity, item.created_at)
                        }
                    } else {
                        helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@ChatActivity, item.created_at)
                    }
                } catch (e: Exception) {
                    helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@ChatActivity, item.created_at)
                }
                if (item.isBusy) {
                    helper.getView(R.id.tv_Customer_busy).visibility = View.VISIBLE
                    if (uid == "1") {
                        helper.getTextView(R.id.tv_Customer_busy).text = resources.getString(R.string.string_customer_busy)
                    } else {
                        helper.getTextView(R.id.tv_Customer_busy).text = resources.getString(R.string.string_echo_busy)
                    }
                    if (!isScrolled) {
                        messageListView.smoothScrollBy(0, 100)
                        isScrolled = true
                    }
                } else {
                    helper.getView(R.id.tv_Customer_busy).visibility = View.GONE
                }
                if (!TextUtils.isEmpty(item.offline_prompt)) {//小二是否处于离线
                    helper.getView(R.id.tv_custom_server_2).visibility = View.VISIBLE
                } else {
                    helper.getView(R.id.tv_custom_server_2).visibility = View.GONE
                }
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_leftimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_rightimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                if (item.resource_type == "1") {
                    if (loginBean.user_id == "1") {
                        helper.getView(R.id.linear_content).visibility = View.VISIBLE
                    } else {
                        helper.getView(R.id.linear_content).visibility = View.GONE
                    }
//                    helper.getView(R.id.tvContent).visibility = if (TextUtils.isEmpty(item.resource_content)) View.GONE else View.VISIBLE
                    helper.getView(R.id.echoesProgress).visibility = View.VISIBLE
                    helper.getView(R.id.frame_ImgContainer).visibility = View.GONE
                    helper.getTextView(R.id.tvContent).text = if (TextUtils.isEmpty(item.recognition_content)) "转文字失败" else item.recognition_content
                    val echoesProgress = helper.getView(R.id.echoesProgress) as EchoesProgress
                    echoesProgress.setData(item)
                    val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                    seekProgress.setOnClickListener {
                        hasNextPlay = false
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                item.pasuePosition = audioPlayer.currentPosition.toInt()
                                audioPlayer.stop()
                                handler.stop()
                                item.isPlaying = false
                                echoesProgress.finish()
                                playBean = null
                                return@setOnClickListener
                            }
                        }
                        playBean?.let {
                            it.isPlaying = false
                        }
                        download(item)
                    }
                    seekProgress.setOnTrackListener(object : ProgressTrackListener {
                        override fun startTrack() {
                            if (!seekProgress.isPressed) {
                                if (audioPlayer.isPlaying) {
                                    item.allDuration = audioPlayer.duration
                                    handler.stop()
                                    audioPlayer.stop()
                                    item.isPlaying = false
                                    echoesProgress.finish()
                                }
                            }
                        }

                        override fun endTrack(progress: Float) {
                            hasNextPlay = false
                            item.pasuePosition = (progress * item.allDuration).toInt()
                            download(item)
                        }
                    })
                    echoesProgress.setReOnClickListener {
                        //点击之后立即重播此条数据
                        hasNextPlay = false
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                audioPlayer.stop()
                                handler.stop()
                                item.isPlaying = false
                                item.pasuePosition = 1
                                echoesProgress.finish()
                                playBean = null
                            }
                        }
                        playBean?.let {
                            it.isPlaying = false
                            it.pasuePosition = 0
                        }
                        item.pasuePosition = 0
                        download(item)
                    }
                    seekProgress.setOnLongClickListener {
                        if (item.is_self == 1)
                            DialogCancelMsg(this@ChatActivity).setCollection(true).setOnClickListener(View.OnClickListener { dialog ->
                                when (dialog.id) {
                                    R.id.tvCollection -> {
                                        if (!TextUtils.isEmpty(item.dialog_id)) {
                                            startActivity<DialogAddTalkAlbumActivity>("talkId" to item.dialog_id)
                                        }
                                    }
                                    else -> {
                                        /**
                                         * 撤回消息,
                                         */
                                        transLayout.showProgress()
                                        deleteItem = item
                                        EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(uid, "0", item.chat_id, item.dialog_id), false))
                                    }
                                }
                            }).show()
                        else {
                            DialogReport(this@ChatActivity).setCollectionAble(true).setIsReportNormal(true).setReportTitle(resources.getString(R.string.string_report_normal)).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tvCollection -> {
                                        if (!TextUtils.isEmpty(item.dialog_id))
                                            startActivity<DialogAddTalkAlbumActivity>("talkId" to item.dialog_id)
                                    }
                                    else -> {
                                        DialogNormalReport(this@ChatActivity).setOnClickListener(View.OnClickListener {
                                            when (it.id) {
                                                R.id.tv_Attach -> {
                                                    reportDialog(item.dialog_id.toString(), "1")
                                                }
                                                R.id.tv_Porn -> {
                                                    reportDialog(item.dialog_id.toString(), "2")
                                                }
                                                R.id.tv_Junk -> {
                                                    reportDialog(item.dialog_id.toString(), "3")
                                                }
                                                R.id.tv_illegal -> {
                                                    reportDialog(item.dialog_id.toString(), "4")
                                                }
                                            }
                                        }).show()
                                    }
                                }
                            }).show()
                        }
                        false
                    }
                } else {
                    helper.getView(R.id.linear_content).visibility = View.GONE
                    helper.getView(R.id.echoesProgress).visibility = View.GONE
                    helper.getView(R.id.frame_ImgContainer).visibility = View.VISIBLE
                    if (item.garbage_type > 0) {
                        helper.getView(R.id.relativeIllegal).visibility = View.VISIBLE
                        helper.getView(R.id.iv_showPic).visibility = View.GONE
                    } else {
                        helper.getView(R.id.iv_showPic).visibility = View.VISIBLE
                        helper.getView(R.id.relativeIllegal).visibility = View.GONE
                    }
                    (helper.getView(R.id.iv_showPic) as MsgThumbImageView).loadAsPath(item.resource_url, getImageMaxEdge(), getImageMinEdge(), R.drawable.message_item_round_bg, item.resource_url)
                    val params = helper.getImageView(R.id.iv_showPic).layoutParams as FrameLayout.LayoutParams
                    if (item.is_self == 1) {
                        params.gravity = Gravity.RIGHT
                    } else {
                        params.gravity = Gravity.LEFT
                    }
                    helper.getView(R.id.iv_showPic).setOnClickListener {
                        startActivity(Intent(this@ChatActivity, ShowPicActivity::class.java)
                                .putExtra("path", item.resource_url)
                                .putExtra("isVoice", false)
                        )
                        overridePendingTransition(R.anim.act_enter_alpha, 0)
                    }
                    helper.getView(R.id.relativeIllegal).setOnClickListener {
                        it.visibility = View.GONE
                        if (helper.getView(R.id.iv_showPic).visibility != View.VISIBLE) {
                            helper.getView(R.id.iv_showPic).visibility = View.VISIBLE
                        }
                    }
                    helper.getView(R.id.iv_showPic).setOnLongClickListener {
                        if (item.is_self == 1)
                            DialogCancelMsg(this@ChatActivity).setOnClickListener(View.OnClickListener {
                                /**
                                 * 撤回消息,
                                 */
                                transLayout.showProgress()
                                deleteItem = item
                                EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(uid, "0", item.chat_id, item.dialog_id)))
                            }).show()
                        else {
                            DialogReport(this@ChatActivity).setIsReportNormal(true).setReportTitle(resources.getString(R.string.string_report_normal)).setOnClickListener(View.OnClickListener {
                                DialogNormalReport(this@ChatActivity).setOnClickListener(View.OnClickListener {
                                    when (it.id) {
                                        R.id.tv_Attach -> {
                                            reportDialog(item.dialog_id.toString(), "1")
                                        }
                                        R.id.tv_Porn -> {
                                            reportDialog(item.dialog_id.toString(), "2")
                                        }
                                        R.id.tv_Junk -> {
                                            reportDialog(item.dialog_id.toString(), "3")
                                        }
                                        R.id.tv_illegal -> {
                                            reportDialog(item.dialog_id.toString(), "4")
                                        }
                                    }
                                }).show()
                            }).show()
                        }
                        false
                    }
                }
                helper.getImageView(R.id.iv_leftimg).setOnClickListener {
                    if (isHelpPage)
                        return@setOnClickListener
                    startActivity(Intent(this@ChatActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                helper.getImageView(R.id.iv_rightimg).setOnClickListener {
                    if (isHelpPage)
                        return@setOnClickListener
                    startActivity(Intent(this@ChatActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                helper.getView(R.id.ivResend).setOnClickListener {
                    //重新发送
                    reSend(item)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun changeStatue(isSelect: Boolean) {
                super.changeStatue(isSelect)
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.echoesProgress) as EchoesProgress
                        voiceProgress.changeProgress(currentPosition)
//                            if (voiceProgress.bean.isPlaying) {
                        if (voiceProgress.bean.read_at != 0) {
                            helper.getView(R.id.viewReadStatus).visibility = View.GONE
//                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun download(item: TalkListData.TalkListBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    val file = if (URLUtil.isNetworkUrl(item.resource_url)) getDownFilePath(item.resource_url) else File(item.resource_url)
                    /**
                     * 重置已读的标记
                     */
                    if (item.read_at == 0 && item.is_self != 1) {
                        hasNextPlay = true
                        clearRead(item.dialog_id)
                        item.read_at = (System.currentTimeMillis() / 1000).toInt()
                        changeStatue(true)
                    }
                    if (file.exists() && file.length() > 0) {
                        audioPlayer.setDataSource(file.absolutePath)
                        if (currentMode == MODE_EARPIECE) {
                            audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                        } else
                            audioPlayer.start(if (SPUtils.getBoolean(this@ChatActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@ChatActivity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioPlayer.setDataSource(o.toString())
                                if (audioMessageHelper?.isRecording == true) {
                                    //正在录音,禁止播放
                                    return@downFile
                                }
                                if (currentMode == MODE_EARPIECE) {
                                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                                } else
                                    audioPlayer.start(if (SPUtils.getBoolean(this@ChatActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, {
                            showToast(VolleyErrorHelper.getMessage(it))
                        })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
                        override fun onCompletion() {
                            item.isPlaying = false
                            changeStatue(true)
                            handler.stop()
                            if (hasNextPlay) {
                                /**
                                 * 查找下一条音频
                                 */
                                val indexOf = mData.indexOf(item)
                                if (item.is_self != 1)
                                    for (index in indexOf downTo 0) {
                                        if (mData[index].read_at == 0 && mData[index].is_self != 1 && "1" == mData[index].resource_type) {
                                            /**
                                             * 下一条要播放的音频
                                             */
                                            download(mData[index])
                                            break
                                        }
                                    }
                            }
                        }

                        override fun onPrepared() {
                            playBean = item
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            item.isPlaying = true
                            handler.start()
                        }

                        override fun onInterrupt() {
                            item.allDuration = audioPlayer.duration
                            if (item.pasuePosition == 0) {
                                if (hasNextPlay)
                                    hasNextPlay = false
                            }
                            item.isPlaying = false
                            changeStatue(true)
                            handler.stop()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        manager.stackFromEnd = true//列表再底部开始展示，反转后由上面开始展示
        manager.reverseLayout = true//列表翻转
        messageListView.layoutManager = manager
        messageListView.setHasFixedSize(true)
        messageListView.adapter = adapter
        messageListView.requestDisallowInterceptTouchEvent(true)
        messageListView.overScrollMode = View.OVER_SCROLL_NEVER
        request(0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        try {
            intent?.let {
                isHasCache = 0
                val userName = it.getStringExtra("userName")
                uid = it.getStringExtra("uid")
                tv_Title.text = userName
                chatId = it.getStringExtra("chatId")
                lastId = null
                mData.clear()
                adapter.notifyDataSetChanged()
                chatId = null
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                transLayout.showProgress()
                loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                when {
                    "1" == uid -> {//与声兮小二聊天
                        team_notify_bar_panel.visibility = View.VISIBLE
                        relative_normal_custom.visibility = View.VISIBLE
                        ivByeByeTextRuler.visibility = View.GONE
                        tvCountDown.visibility = View.GONE
                    }
                    "1" == loginBean.user_id -> {//户主是声兮小二
                        team_notify_bar_panel.visibility = View.VISIBLE
                        tv_customer_type.visibility = View.VISIBLE
                        iv_Other.setImageResource(R.mipmap.icon_album_setting)
                        ivByeByeTextRuler.visibility = View.GONE
                        queryDevice()
                        queryHobby(1)
                        tvCountDown.visibility = View.GONE
                    }
                    else -> {
                        team_notify_bar_panel.visibility = View.GONE
                        queryHobby(1)
                        val createAt = intent.getIntExtra("createAt", 0)
                        tvCountDown.visibility = View.VISIBLE
//                        tvCountDown.text = String.format(resources.getString(R.string.string_48), ceil(createAt + 60 * 60 * 24 * 30 - System.currentTimeMillis() / 1000f).toInt().toString())
                    }
                }

                url = if (TextUtils.isEmpty(chatId)) {
                    "chats/2/$uid/0"
                } else {
                    queryTalkInfo()
                    "chats/$chatId"
                }
                request(0)
            }
        } catch (e: Exception) {
        }
        tv_AutoReplay.visibility = View.GONE
    }

    private fun getImageMaxEdge(): Int {
        return (165.0 / 320.0 * AppTools.getWindowsWidth(this)).toInt()
    }

    private fun getImageMinEdge(): Int {
        return (76.0 / 320.0 * AppTools.getWindowsHeight(this)).toInt()
    }

    override fun initEvent() {
        ivByeByeTextRuler.setOnClickListener {
            startActivity(Intent(this, ActionActivity::class.java)
                    .putExtra("title", "声昔宿舍的“不不不”")
                    .putExtra("url", "32")
                    .putExtra("isHtml", true)
                    .putExtra("isVersion", false)
            )
        }
        btn_Back.setOnClickListener {
            if (isHasCache > 0)
                CenterHintDialog(this).setTitle("离开对话后，发送失败的语音\n" +
                        "将不会保存", "确定", "在想想").setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
            else
                finish()
        }
        iv_Other.setOnClickListener {
            DialogChatOperator(this).isPwdTopic(isTopicType).setIsCustom(uid == "1").setOnClickListener(View.OnClickListener {
                when (it.id) {
                    R.id.tv_AddBlack -> {//加入黑名单
                        DialogHintCancle(this).setHintTitle(resources.getString(R.string.string_is_addBlock)).setOnClickListener(View.OnClickListener { addBlack() }).show()
                    }
                    R.id.tv_Report -> {//举报
                        DialogReportContent(this).setOnResultListener(OnReportItemListener { report ->
                            transLayout.showProgress()
                            report(report)
                        }).show()
                    }
                    R.id.tv_Delete -> {
                        /**
                         * 是否清除聊天记录
                         */
                        DialogHintCancle(this).setHintTitle(resources.getString(R.string.string_check_clear_chat_list)).setOnClickListener(View.OnClickListener {
                            deleteList()
                        }).show()
                    }
                    R.id.tv_report_normal -> {
                        DialogAlbumHowOperator(this).setTitle(resources.getString(R.string.string_report_hint_content)).show()
                    }
                    R.id.tv_admin_user_details -> {
                        startActivity(Intent(this, PretreatmentActivity::class.java))
                    }
                    R.id.tv_admin_delete -> {
                        DialogDeleteConment(this).setHintText("确定发送？").setClickBtnTitle("确定").setOnClickListener(View.OnClickListener {
                            sendPwdMsg(uid!!)
                        }).show()
                    }
                }
            }).show()
        }
        audioRecord.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touched = true
                initAudioRecord()
                onStartAudioRecord()
            }
            true
        }
        ivPic.setOnClickListener {
            startActivityForResult(Intent(this, AlbumActivity::class.java)
                    .putExtra("isChat", true)
                    .putExtra("count", 1), REQUEST_PHOTO)
        }
        swipeRefresh.setOnRefreshListener {
            lastId = try {
                mData[mData.size - 1].dialog_id
            } catch (e: Exception) {
                null
            }
            request(1)
        }
        tv_Cancel.setOnClickListener {
            onEndAudioRecord(true)
        }
        tv_Send.setOnClickListener {
            onEndAudioRecord(false)
        }
        tv_pretreatment.setOnClickListener {
            startActivityForResult(Intent(this, DialogShowPretreatmentActivity::class.java), REQUEST_PRETREATMENT)
        }
    }

    /**
     * 初始化AudioRecord
     */
    private fun initAudioRecord() {
        if (audioPlayer.isPlaying) {
            playBean?.isPlaying = false
            audioPlayer.stop()
        }
    }

    private var path: String? = null

    /**
     * 开始语音录制
     */
    private fun onStartAudioRecord() {
        path = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/audio/${UUID.randomUUID()}").absolutePath
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        audioMessageHelper?.startRecord()
        cancelled = false
    }

    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private fun onEndAudioRecord(cancel: Boolean) {
        try {
            started = false
            window.setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            /* if (!cancel) {
                 audioMessageHelper?.stop()
             } else {
                 audioMessageHelper?.cancel()
             }*/
            audioMessageHelper?.completeRecord(cancel)
            tv_RecordStatus.text = resources.getString(R.string.record_audio)
            audioRecord.isSelected = false
            stopAudioRecordAnim()
        } catch (e: Exception) {
        }
    }

    /**
     * 结束语音录制动画
     */
    private fun stopAudioRecordAnim() {
        tv_RecordStatus.text = resources.getString(R.string.record_audio)
        layoutPlayAudio.visibility = View.GONE
        timer.stop()
        timer.base = SystemClock.elapsedRealtime()
    }

    /**
     * 开始语音录制动画
     */
    private fun playAudioRecordAnim() {
        layoutPlayAudio.visibility = View.VISIBLE
        timer.base = SystemClock.elapsedRealtime()
        timer.start()
    }

    private var touched = false
    private var cancelled = false
    private var started = false

    private fun queryDevice() {
        OkClientHelper.get(this, "users/$uid/specialInfo", CustomerDeviceData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as CustomerDeviceData
                if (result.code == 0) {
                    tv_customer_type.text = resources.getString(R.string.string_device_type) + "  ${result.data.personality_no}${if ("1" == result.data.gender) "男性" else if ("2" == result.data.gender) "女性" else "未测试"}" + if ("1" == result.data.platform_id) "  android" else {
                        "  ios"
                    } + "/" + result.data.device_info + "/" + result.data.app_version
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.1")
    }


    /**
     * 查询用户的偏好设置
     */
    @SuppressLint("SetTextI18n")
    private fun queryHobby(flag: Int) {
        when (flag) {
            2 -> {
                OkClientHelper.get(this, "users/$uid/setting/chatHobby", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any) {
                        result as PatchData
                        if (result.code == 0) {
                            var preText = tv_user_hobby.text.toString()
                            if (result.data.chat_hobby != null) {
                                iv_single_line.visibility = View.VISIBLE
                                relative_hobby.visibility = View.VISIBLE
                                if (!TextUtils.isEmpty(preText)) {
                                    preText += "\n"
                                }
                                tv_user_hobby.text = preText + "ta喜欢:${result.data.chat_hobby.name}"
                            }
                        }
                    }

                    override fun onFailure(any: Any) {

                    }
                })
            }
            1 -> {
                OkClientHelper.get(this, "users/$uid/setting/chatTips", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.chat_tips != null && result.data.chat_tips.size > 0) {
                                relative_hobby.visibility = View.VISIBLE
                                var text = ""
                                for (indices in 0 until result.data.chat_tips.size) {
                                    text += result.data.chat_tips[indices].name
                                    if (indices < result.data.chat_tips.size - 1) {
                                        text += "·"
                                    }
                                }
                                iv_single_line.visibility = View.VISIBLE
                                tv_user_hobby.text = text
                            } else {
                                tv_user_hobby.text = ""
                            }
                        } else {
                            tv_user_hobby.text = ""
                        }
                        queryHobby(2)

                    }

                    override fun onFailure(any: Any) {

                    }
                })
            }
        }
    }

    private var isBusy = false

    private fun querySet() {
        OkClientHelper.get(this, "users/$uid/setting/autoReply", PatchData::class.java, object : OkResponse {
            override fun success(result: Any) {
                result as PatchData
                if (result.code == 0) {
                    if (result.data.auto_reply == 1) {
                        isBusy = true
                        if (mData.size > 0) {
                            mData[0].isBusy = true
                            adapter.notifyItemChanged(0)
                            scrollBottom()
                        } else {
                            tv_AutoReplay.visibility = View.VISIBLE
                        }
                    } else {
                        isBusy = false
                        tv_AutoReplay.visibility = View.GONE
                    }
                } else {
                    isBusy = false
                    tv_AutoReplay.visibility = View.GONE
                }
            }

            override fun onFailure(any: Any) {
                isBusy = false
                tv_AutoReplay.visibility = View.GONE
            }
        })
    }

    /**
     * 查询对话信息
     */
    private fun queryTalkInfo() {
        OkClientHelper.get(this, "chats/${chatId}", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as IntegerRespData
                result.data?.let {
                    if (tvCountDown.visibility == View.VISIBLE) {
                        tvCountDown.text = String.format(resources.getString(R.string.string_48), floor((it.created_at + 60 * 60 * 24 * 30 - System.currentTimeMillis() / 1000f) / (24 * 60f * 60)).toInt().toString())
                    }
                }
            }
        }, "V4.5")
    }

    private var fromUserIdentity = 0
    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询用户信息
                OkClientHelper.get(this, "users/$uid", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            result.data.relation_status
                            fromUserIdentity = result.data.identity_type
                        }
                        tv_Title.text = result.data.nick_name
                        request(1)
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            else -> {
                OkClientHelper.get(this, "$url?lastId=$lastId", TalkListData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as TalkListData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                if (null == lastId) {
                                    mData.clear()
                                    adapter.notifyDataSetChanged()
                                    for (bean in result.data) {
                                        if (null == chatId) {
                                            chatId = bean.chat_id
                                            queryTalkInfo()
                                        }
                                        mData.add(bean)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                        scrollBottom()
                                    }
                                    querySet()
//                                    handler.postDelayed({
//                                        messageListView.scrollToPosition(0)
//                                        scrollBottom()
//                                    }, 300)
                                } else {
                                    mData.addAll(result.data)
                                    adapter.notifyDataSetChanged()
                                }
                                if (result.data.size < 10) {
                                    swipeRefresh.isEnabled = false
                                }
                            } else {
                                if (null == lastId) {
                                    querySet()
                                }
                                swipeRefresh.isEnabled = false
                            }
                        }
                        swipeRefresh.isRefreshing = false
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                        transLayout.showContent()
                    }
                }, "V4.3")
            }
        }
    }

    private fun scrollBottom() {
        val scroller = TopSmoothScroller(this)
        scroller.targetPosition = 0
        messageListView.layoutManager?.startSmoothScroll(scroller)
//        messageListView.scrollToPosition(0)

    }

    private class TopSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

        override fun getHorizontalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return 5f / displayMetrics?.densityDpi!!
        }
    }

    /**
     * 更新
     */
    private fun clearRead(dialog: String) {
        if (TextUtils.isEmpty(chatId)) {
            return
        }
        OkClientHelper.patch(this, "chats/$chatId/$dialog", FormBody.Builder().add("readAt", (System.currentTimeMillis() / 1000).toString()).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 删除聊天记录
     */
    private fun deleteList() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "chats/$chatId", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    showToast("删除成功")
                    EventBus.getDefault().post(OnRecentStatusEvent(1, uid))
                    finish()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 加入黑名单
     */
    private fun addBlack() {
        val formBody = FormBody.Builder()
                .add("toUserId", uid!!.substring(uid!!.lastIndexOf("_") + 1, uid?.length!!))
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/blacklist", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    EventBus.getDefault().post(OnRecentStatusEvent(1, uid))
                    finish()
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun reportDialog(dialogId: String, type: String) {
        val formBody = FormBody.Builder()
                .add("resourceId", dialogId)
                .add("reportType", type)
                .add("resourceType", "3")
                .build()
        transLayout.showProgress()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    DialogReportSuccess(this@ChatActivity).show()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 举报
     */
    private fun report(type: String) {
        val formBody = FormBody.Builder()
                .add("resourceId", uid!!.substring(uid!!.lastIndexOf("_") + 1, uid?.length!!))
                .add("reasonType", type)
                .add("resourceType", "4")
                .add("toUserId", uid!!)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/shield", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    DialogPBBlack(this@ChatActivity).setType(type).setOnClickListener(View.OnClickListener {
                        EventBus.getDefault().post(OnRecentStatusEvent(1, uid))
                        finish()
                    }).show()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.4")
    }

    private fun reSend(item: TalkListData.TalkListBean) {
        if (AppTools.isNetOk(this)) {
            if (item.isCache) {
                isHasCache--
                mData.remove(item)
                adapter.notifyDataSetChanged()
                if (item.resource_type == "1") {//音频
                    updateVoice(item.resource_url, item.voice_len.toInt())
                } else {//
                    updatePic(item.resource_url)
                }
            }
        } else {
            showToast("网络异常")
        }
    }

    private var allLength = 0L
    private var startTime = 0L
    private var aliLoad: AliLoadFactory? = null

    /*
    上传音频, 如果失败, 加入本地缓存文件
     */
    private fun updateVoice(voicePath: String, voiceLength: Int) {
        transLayout.showProgress()
        startTime = System.currentTimeMillis()
        allLength = File(voicePath).length()
        val formBody = FormBody.Builder()
                .add("resourceType", "4")
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(voicePath))}.aac")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                val data = result as QiniuStringData
                LocalLogUtils.writeLog("私聊 =>获取token: $result", System.currentTimeMillis())
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        AppTools.bucketId = it
                    }
                    aliLoad = AliLoadFactory(this@ChatActivity, result.data.end_point, data.data.bucket, data.data.oss, object : LoadStateListener {
                        @SuppressLint("SetTextI18n")
                        override fun progress(current: Long) {
                            if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                LocalLogUtils.writeLog("私聊=>上传音频文件:more than 30s cancel all task", System.currentTimeMillis())
                                aliLoad?.cancel()
                            } else if (System.currentTimeMillis() - startTime >= 10000) {
                                if (tv_progress.visibility != View.VISIBLE) {
                                    tv_progress.post {
                                        tv_progress.visibility = View.VISIBLE
                                    }
                                }
                                tv_progress.post {
                                    tv_progress.text = "${((current * 1f / allLength) * 100).toInt()}%"
                                }
                            }
                        }

                        override fun success() {
                            //删除本地音频文件
                            try {
                                File(voicePath).let {
                                    if (it.exists()) {
                                        it.delete()
                                    }
                                }
                            } catch (e: java.lang.Exception) {

                            }
                            runOnUiThread {
                                tv_progress.visibility = View.GONE
                                LocalLogUtils.writeLog("私聊 => 上传成功,socket发送", System.currentTimeMillis())
                                EventBus.getDefault().post(SendMsgEvent(if (isHelpPage) AppTools.fastJson(uid, 1, "0", result.data.resource_content, voiceLength.toString(), "\"topicType\":1") else
                                    AppTools.fastJson(uid, 1, "0", result.data.resource_content, voiceLength.toString())
                                ))
                                transLayout.showContent()
                            }
                        }

                        override fun fail() {
                            LocalLogUtils.writeLog("私聊 => 音频上传失败", System.currentTimeMillis())
                            runOnUiThread {
                                //添加本地缓存数据

                                val userInfo = PreferenceTools.getObj(this@ChatActivity, IConstant.USERCACHE, UserInfoData::class.java)
                                mData.add(0, TalkListData.TalkListBean().apply {
                                    is_self = 1
                                    identity_type = userInfo.data.identity_type
                                    created_at = (System.currentTimeMillis() / 1000).toInt()
                                    isBusy = this@ChatActivity.isBusy
                                    avatar_url = userInfo.data.avatar_url
                                    resource_url = voicePath
                                    resource_type = "1"
                                    voice_len = voiceLength.toString()
                                    isCache = true
                                })
                                adapter.notifyDataSetChanged()
                                scrollBottom()
                                isHasCache++

                                tv_progress.visibility = View.GONE
                                showToast("发送失败,当前网络不太稳定")
                                transLayout.showContent()
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(data.data.resource_content, voicePath))
                }
            }

            override fun onFailure(any: Any?) {
                showToast("发送失败,当前网络不太稳定")
                //添加本地发送失败的文件
                val userInfo = PreferenceTools.getObj(this@ChatActivity, IConstant.USERCACHE, UserInfoData::class.java)
                mData.add(0, TalkListData.TalkListBean().apply {
                    is_self = 1
                    identity_type = userInfo.data.identity_type
                    created_at = (System.currentTimeMillis() / 1000).toInt()
                    isBusy = this@ChatActivity.isBusy
                    avatar_url = userInfo.data.avatar_url
                    resource_url = voicePath
                    resource_type = "1"
                    voice_len = voiceLength.toString()
                    isCache = true
                })
                isHasCache++
                adapter.notifyDataSetChanged()
                scrollBottom()
                LocalLogUtils.writeLog("私聊 =>Token 语音 :获取出错: ${any.toString()} ${if (AppTools.isNetOk(this@ChatActivity)) "网络正常" else "网络异常"}", System.currentTimeMillis())
                transLayout.showContent()
            }
        })
    }

    private fun updatePic(imgPath: String) {
        transLayout.showProgress()
        startTime = System.currentTimeMillis()
        allLength = File(imgPath).length()
        val formBody = FormBody.Builder()
                .add("resourceType", "11")
                .add("resourceContent", imgMd5(imgPath))
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                val data = result as QiniuStringData
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        AppTools.bucketId = it
                    }
                    aliLoad = AliLoadFactory(this@ChatActivity, result.data.end_point, data.data.bucket, data.data.oss, object : LoadStateListener {
                        @SuppressLint("SetTextI18n")
                        override fun progress(current: Long) {
                            if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                LocalLogUtils.writeLog("私聊=>上传图片:more than 30s cancel all task", System.currentTimeMillis())
                                aliLoad?.cancel()
                            } else if (System.currentTimeMillis() - startTime >= 10000) {
                                if (tv_progress.visibility != View.VISIBLE) {
                                    tv_progress.post {
                                        tv_progress.visibility = View.VISIBLE
                                    }
                                }
                                tv_progress.post {
                                    tv_progress.text = "${((current * 1f / allLength) * 100).toInt()}%"
                                }
                            }
                        }

                        override fun success() {
                            LocalLogUtils.writeLog("私聊 => 图片成功,socket发送", System.currentTimeMillis())
                            runOnUiThread {
                                tv_progress.visibility = View.GONE
                                EventBus.getDefault().post(SendMsgEvent(if (isHelpPage) AppTools.fastJson(uid, 2, "0", result.data.resource_content, "0", "\"topicType\":1")
                                else AppTools.fastJson(uid, 2, "0", result.data.resource_content, "0")))
                                transLayout.showContent()
                            }
                        }

                        override fun fail() {
                            LocalLogUtils.writeLog("私聊 => 图片上传失败", System.currentTimeMillis())
                            runOnUiThread {
                                showToast("发送失败,当前网络不太稳定")

                                val userInfo = PreferenceTools.getObj(this@ChatActivity, IConstant.USERCACHE, UserInfoData::class.java)
                                mData.add(0, TalkListData.TalkListBean().apply {
                                    is_self = 1
                                    identity_type = userInfo.data.identity_type
                                    created_at = (System.currentTimeMillis() / 1000).toInt()
                                    isBusy = this@ChatActivity.isBusy
                                    avatar_url = userInfo.data.avatar_url
                                    resource_url = imgPath
                                    resource_type = "2"
                                    isCache = true
                                })
                                adapter.notifyDataSetChanged()
                                scrollBottom()
                                isHasCache++
                                tv_progress.visibility = View.GONE
                                transLayout.showContent()
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(data.data.resource_content, imgPath))
                }
            }

            override fun onFailure(any: Any?) {

                val userInfo = PreferenceTools.getObj(this@ChatActivity, IConstant.USERCACHE, UserInfoData::class.java)
                mData.add(0, TalkListData.TalkListBean().apply {
                    is_self = 1
                    identity_type = userInfo.data.identity_type
                    created_at = (System.currentTimeMillis() / 1000).toInt()
                    isBusy = this@ChatActivity.isBusy
                    avatar_url = userInfo.data.avatar_url
                    resource_url = imgPath
                    resource_type = "2"
                    isCache = true
                })
                adapter.notifyDataSetChanged()
                scrollBottom()
                isHasCache++
                showToast("发送失败,当前网络不太稳定")
                LocalLogUtils.writeLog("私聊 => 图片 Token获取出错: ${any.toString()} ${if (AppTools.isNetOk(this@ChatActivity)) "网络正常" else "网络异常"}", System.currentTimeMillis())
                transLayout.showContent()
            }
        })
    }

    /**
     * 清除未读的标记
     */
    private fun clearUnreadTag() {
        OkClientHelper.get(this, "chats/$uid/0?lastId=null", TalkListData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun imgMd5(path: String): String {
        val suffix: String = TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())
        var endSuffix = ""
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val type = options.outMimeType
            endSuffix = if (type.contains("gif", true)) {
                ".gif"
            } else {
                try {
                    path.substring(path.lastIndexOf("."), path.length)
                } catch (e: Exception) {
                    ".png"
                }
            }
            /* val exif = ExifInterface(path)
             val time = exif.getAttribute(ExifInterface.TAG_DATETIME)
             if (!TextUtils.isEmpty(time)) {
                 suffix = time.substring(0, time.indexOf(" ")).replace(":", "").replace(" ", "")
             }*/
        } catch (e: Exception) {

        }
        return suffix + "_" + AppTools.getFileMD5(File(path)) + endSuffix
    }

    /* override fun onRecordSuccess(p0: File?, p1: Long, p2: RecordType?) {
         stopAudioRecordAnim()
         audioRecord.isSelected = false
         *//*
         * 发送消息
         *//*
        var voiceLength = (p1.toFloat() / 1000 + 0.5f).toInt()
        if (voiceLength < 1) {
            voiceLength = 1
        }
        *//*
         * 上传音频文件
         *//*
        LocalLogUtils.writeLog("私聊 => 音频文件录制结束，开始上传", System.currentTimeMillis())
        updateVoice(p0!!.absolutePath, voiceLength)
    }*/

    /* override fun onRecordReachedMaxTime(p0: Int) {
         stopAudioRecordAnim()
         audioMessageHelper?.handleEndRecord(true, p0)
         audioRecord.isSelected = false
     }*/

    override fun onRecordSuccess(var1: File?, var2: Long, p2: RecordType?) {
        stopAudioRecordAnim()
        audioRecord.isSelected = false
        /*
         * 发送消息
         */
        var voiceLength = (var2.toFloat() / 1000 + 0.5f).toInt()
        if (voiceLength < 1) {
            voiceLength = 1
        }
        /*
         * 上传音频文件
         */
        LocalLogUtils.writeLog("私聊 => 音频文件录制结束，开始上传", System.currentTimeMillis())
        calcMediaLength(var1!!.absolutePath, voiceLength)
    }

    /**
     * 计算音频的实际长度
     */
    private fun calcMediaLength(path: String, voiceLength: Int = 0) {
        /*val length = try {
            val media = MediaPlayer()
            media.setDataSource(path)
            media.prepare()
            val duration = (media.duration / 1000f + 0.5f).toInt()
            media.release()
            if (duration > 120) {
                120
            } else
                duration
        } catch (e: Exception) {
            voiceLength
        }*/
        if (isCactus != "1") {
            updateVoice(path, voiceLength)
        }
    }

    override fun onRecordCancel() {

    }


    override fun onRecordFail() {

    }

    override fun onRecordReachedMaxTime(p0: Int) {
        stopAudioRecordAnim()
        audioMessageHelper?.handleEndRecord(true, p0)
        audioRecord.isSelected = false
    }

    override fun onRecordReady() {

    }

    override fun onRecordStart(p0: File?, p1: RecordType?) {
        started = true
        if (!touched) {
            return
        }
        tv_RecordStatus.text = resources.getString(R.string.string_recordAct_7)
        audioRecord.isSelected = true
        playAudioRecordAnim()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateIdentity(event: UpdateIdentityEvent) {
        if (mData.size > 0) {
            mData.forEach {
                if (it.is_self == 1) {
                    it.identity_type = event.type
                    val indexOf = mData.indexOf(it)
                    adapter.notifyItemChanged(indexOf)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteEvent(event: OnDeleteMsg) {
        if (event.data.code == 0) {
            /**
             * 删除成功  移除当前的 isBusy
             *
             * 撤回判断是否是撤回的最新的消息,
             * 并且判断 是否处于繁忙状态, 需要更新UI
             */
            try {
                if (mData.size > 0 && mData.contains(deleteItem)) {
                    val indexOf = mData.indexOf(deleteItem)
                    if (indexOf >= 0) {
                        mData.remove(deleteItem)
                        if (isBusy) {
                            if (mData.size > 0)
                                if (!mData[0].isBusy) {
                                    mData[0].isBusy = true
                                }
                        }
                        adapter.notifyDataSetChanged()
                        deleteItem = null
                    }
                }
            } catch (e: Exception) {

            } finally {
                if (mData.size == 0) {
                    if (isBusy) {
                        tv_AutoReplay.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            if (deleteItem != null)
                showToast("撤回失败")
        }
        transLayout.showContent()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMsg(event: OnChatNewMsgEvent) {
        if (event.bean != null) {
            if (!TextUtils.isEmpty(event.bean.action)) {
                if ("delete" == event.bean.action) {
                    /**
                     * 当前需要删除列表中的一条会话
                     */
                    try {
                        if (mData.size > 0) {
                            var tempBean: TalkListData.TalkListBean? = null
                            for (item in mData) {
                                if (item.dialog_id == event.bean.data.dialogId) {
                                    tempBean = item
                                    break
                                }
                            }
                            if (null != tempBean) {
                                if (0 == mData.indexOf(tempBean)) {
                                    if (mData.size >= 2) {
                                        if (isBusy) {
                                            mData[1].isBusy = true
                                        }
                                    }
                                }
                                if (tempBean.isPlaying) {
                                    if (audioPlayer.isPlaying)
                                        audioPlayer.stop()
                                }
                                mData.remove(tempBean)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {

                    } finally {
                        if (mData.size == 0) {
                            if (isBusy) {
                                tv_AutoReplay.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } else {//消息发送成功
                if (TextUtils.isEmpty(event.bean.from) || event.bean.from == IConstant.YUNXINPORT + uid) {//自己的
                    if (TextUtils.isEmpty(chatId)) {
                        chatId = event.bean.data.chat_id.toString()
                    }
                    if (!TextUtils.isEmpty(event.bean.data.flag) && "1" == event.bean.data.flag) {
                        return
                    }
                    val loginBean = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                    val bean = TalkListData.TalkListBean()
                    bean.is_self = if (loginBean.data.user_id == event.bean.data.from_user_id) {
                        bean.identity_type = loginBean.data.identity_type
                        1
                    } else {
                        bean.identity_type = fromUserIdentity
                        0
                    }
                    bean.chat_id = event.bean.data.chat_id.toString()
                    bean.created_at = event.bean.data.created_at
                    bean.voice_len = event.bean.data.dialog_content_len.toString()
                    bean.avatar_url = event.bean.data.user_avatar_url
                    bean.resource_type = event.bean.data.dialog_content_type.toString()
                    bean.dialog_id = event.bean.data.dialog_id.toString()
                    bean.resource_url = event.bean.data.dialog_content_url
                    bean.from_user_id = event.bean.data.from_user_id
                    bean.offline_prompt = event.bean.data.offline_prompt
                    if (!TextUtils.isEmpty(event.bean.data.auto_reply_msg)) {
                        if (mData.size > 0) {
                            mData[0].isBusy = false
                            bean.isBusy = true
                        } else {
                            bean.isBusy = true
                        }
                    } else {
                        if (bean.is_self == 1) {
                            if (mData.size > 0) {
                                mData[0].isBusy = false
                            }
                            if ("1" == uid) {//声兮小二
                                if (isBusy) {
                                    bean.isBusy = true
                                }
                            } else {
                                isBusy = false
                            }
                        } else {
                            if (isBusy) {
                                if (mData.size > 0) {
                                    mData[0].isBusy = false
                                    bean.isBusy = true
                                }
                            } else {
                                if (mData.size > 0) {
                                    mData[0].isBusy = false
                                    isBusy = false
                                }
                            }
                        }
                    }
                    mData.add(0, bean)
                    adapter.notifyDataSetChanged()
                    scrollBottom()
                    clearUnreadTag()
                    //判断是否为0s 的状态
                    /*if (SPUtils.getInt(this@ChatActivity, IConstant.TOTALLENGTH + loginBean.data.user_id, 0) == 0 && "1" != uid && bean.is_self == 1) {
                        DialogEmptySendChat(this@ChatActivity).show()
                    }*/
                }
                tv_AutoReplay.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PHOTO) {
                data?.let {
                    val result = it.getSerializableExtra("result") as ArrayList<String>
                    /**
                     * 发送图片
                     */
                    if (result.size > 0) {
                        if (isCactus != "1")
                            updatePic(result[0])
                    }
                }
            } else if (requestCode == REQUEST_PRETREATMENT) {
                data?.let {
                    //发送预回复的消息
                    LocalLogUtils.writeLog("私聊 => 上传成功,socket发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(uid, 1, "0", it.getStringExtra("voiceUri"), it.getStringExtra("voiceLength"))))
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (audioMessageHelper != null) {
            if (audioMessageHelper!!.isRecording) {
                onEndAudioRecord(true)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        LocalLogUtils.writeLog("ChatActivity 程序被系统回收,onRestoreInstanceState", System.currentTimeMillis())
    }

    override fun finish() {
        try {
            audioMessageHelper?.let {
                if (it.isRecording) {
                    it.completeRecord(true)
                }
            }
            audioMessageHelper?.destroyAudioRecorder()
            audioMessageHelper = null
        } catch (e: Exception) {
        }
        super.finish()
        audioPlayer.stop()
        instances = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isHasCache > 0) {
                CenterHintDialog(this).setTitle("离开对话后，发送失败的语音\n" +
                        "将不会保存", "确定", "在想想").setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}