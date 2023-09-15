package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_admin_complete_chat_list.*
import kotlinx.android.synthetic.main.activity_admin_complete_chat_list.btn_Back
import kotlinx.android.synthetic.main.activity_admin_complete_chat_list.swipeRefresh
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.model.TalkListData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.MsgThumbImageView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException
import java.util.ArrayList

//私聊完整对话
class AdminChatListActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<TalkListData.TalkListBean>
    private val mData by lazy { ArrayList<TalkListData.TalkListBean>() }
    private lateinit var chatId: String
    private var chatType: Int = 0
    private var lastId: String = ""
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: TalkListData.TalkListBean? = null

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

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
        return R.layout.activity_admin_complete_chat_list
    }

    override fun initView() {
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        manager.stackFromEnd = true//列表再底部开始展示，反转后由上面开始展示
        manager.reverseLayout = true//列表翻转
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.requestDisallowInterceptTouchEvent(true)
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        chatId = intent.getStringExtra("chatId")
        chatType = intent.getIntExtra("chatType", 0)
        adapter = object : QuickAdapter<TalkListData.TalkListBean>(this, R.layout.item_admin_chat, mData) {
            val cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: TalkListData.TalkListBean) {
                glideUtil.loadGlide(item.from_avatar_url, helper!!.getImageView(R.id.ivAvatar), 0, glideUtil.getLastModified(item.from_avatar_url))
                if (item.resource_type == "2") {
                    helper.getView(R.id.iv_showPic).visibility = View.VISIBLE
                    helper.getView(R.id.voiceProgress).visibility = View.GONE
                    (helper.getView(R.id.iv_showPic) as MsgThumbImageView).loadAsPath(item.resource_url, getImageMaxEdge(), getImageMinEdge(), R.drawable.message_item_round_bg, item.resource_url)
                    helper.getView(R.id.linear_content).visibility = View.GONE
                    helper.getView(R.id.iv_showPic).setOnClickListener {
                        startActivity<ShowPicActivity>("path" to item.resource_url)
                        overridePendingTransition(R.anim.act_enter_alpha, 0)
                    }
                } else {
                    helper.getView(R.id.linear_content).visibility = View.VISIBLE
                    helper.getTextView(R.id.tvContent).text = if (TextUtils.isEmpty(item.recognition_content)) {
                        "语音转文字失败"
                    } else
                        item.recognition_content
                    helper.getView(R.id.iv_showPic).visibility = View.GONE
                    helper.getView(R.id.voiceProgress).visibility = View.VISIBLE
                    val progress = helper.getView(R.id.voiceProgress) as VoiceProgress
                    progress.data = item
                    val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                    progress.setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = audioPlayer.currentPosition.toInt()
                                audioPlayer.stop()
                                progress.finish()
                                return@setOnClickListener
                            } else {
                                audioPlayer.stop()
                                progressHandler.stop()
                            }
                        }
                        playBean?.let {
                            if (item != it) {
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
                                    progress.finish()
                                }
                            }
                        }

                        override fun endTrack(progress: Float) {
                            /**
                             * 滑动停止
                             */
                            item.pasuePosition = (progress * item.allDuration).toInt()
                            download(helper, item)
                        }
                    })
                    progress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                audioPlayer.stop()
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = 1
                                progress.finish()
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
                helper.getView(R.id.cardLeft).setOnClickListener {
                    startActivity<UserDetailsActivity>("id" to item.from_user_id)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: TalkListData.TalkListBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    val file = getDownFilePath(item.resource_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@AdminChatListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@AdminChatListActivity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                                    return@downFile
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@AdminChatListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
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
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            progressHandler.stop()
                            changeStatue(false)
                        }

                        override fun onInterrupt() {
                            item.isPlaying = false
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            progressHandler.stop()
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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
        }
        recyclerView.adapter = adapter
//        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        swipeRefresh.setOnRefreshListener {
            request(0)
        }
    }

    private fun scrollBottom() {
        val scroller = TopSmoothScroller(this)
        scroller.targetPosition = 0
        recyclerView.layoutManager?.startSmoothScroll(scroller)
    }

    private fun getImageMaxEdge(): Int {
        return (165.0 / 320.0 * AppTools.getWindowsWidth(this)).toInt()
    }

    private fun getImageMinEdge(): Int {
        return (76.0 / 320.0 * AppTools.getWindowsHeight(this)).toInt()
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "admin/chats/${chatId}?token=${SPUtils.getString(this, IConstant.ADMINTOKEN, "")}&lastId=$lastId",
                TalkListData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
            }

            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                result as TalkListData
                result.data?.let {
                    mData.addAll(it)
                    adapter.notifyDataSetChanged()
                    if (it.size >= 10) {
                        lastId = it[it.size - 1].id
                    } else {
                        swipeRefresh.isEnabled = false
                    }
                    scrollBottom()
                }
            }
        })
    }

}