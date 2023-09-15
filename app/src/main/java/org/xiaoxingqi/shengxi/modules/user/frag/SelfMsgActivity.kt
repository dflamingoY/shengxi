package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_self_comment.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogDeleteConment
import org.xiaoxingqi.shengxi.dialog.DialogReport
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.NoteVoiceData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.File
import java.io.IOException

/**
 * 管理自己的留言
 */
class SelfMsgActivity : BaseAct() {
    companion object {
        private const val REQUEST_RECORD = 0x00
    }

    private var voiceId: String? = null
    private lateinit var adapter: QuickAdapter<NoteVoiceData.NoteVoiceBean>
    private val mData by lazy { ArrayList<NoteVoiceData.NoteVoiceBean>() }
    private var lastId: String = ""
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: NoteVoiceData.NoteVoiceBean? = null
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

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

    override fun getLayoutId(): Int {
        return R.layout.activity_self_comment
    }

    override fun initView() {

    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        voiceId = intent.getStringExtra("voiceId")
        adapter = object : QuickAdapter<NoteVoiceData.NoteVoiceBean>(this, R.layout.item_note, mData) {
            var cache = ArrayList<BaseAdapterHelper>()
            override fun convert(helper: BaseAdapterHelper?, item: NoteVoiceData.NoteVoiceBean?) {
                val parseTime = TimeUtils.getInstance().formatterTime(this@SelfMsgActivity, item!!.created_at)
                try {
                    val split = parseTime.split("_")
                    helper!!.getTextView(R.id.tv_FirstTime).text = split[0]
                    helper.getTextView(R.id.tv_SecondTime).text = split[1]
                } catch (e: Exception) {

                }
                val progress = helper!!.getView(R.id.voiceProgress) as VoiceProgress
                progress.data = item
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                progress.setOnClickListener {
                    //此条播放状态 暂停
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
                        if (item !== it) {
                            it.isPlaying = false
//                            it.isPause = false
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
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: NoteVoiceData.NoteVoiceBean) {
                try {
                    if (TextUtils.isEmpty(item.note_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    val file = getDownFilePath(item.note_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@SelfMsgActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@SelfMsgActivity,item.note_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                                    return@downFile
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@SelfMsgActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
                            progressHandler.stop()
                            changeStatue(false)
                        }

                        override fun onInterrupt() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
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
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        linear_comment.setOnClickListener {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                    .putExtra("isSelf", true)
                    .putExtra("resourceType", 13), REQUEST_RECORD)
            overridePendingTransition(0, 0)
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemLongClickListener { _, position ->
            /**
             * 长按事件
             */
            DialogReport(this).setIsReportNormal(true).setReportTitle("删除").setOnClickListener(View.OnClickListener {
                DialogDeleteConment(this).setHintText(resources.getString(R.string.string_delete_self_comment)).setOnClickListener(View.OnClickListener {
                    if (audioPlayer.isPlaying && mData[position].isPlaying) {
                        sendObserver()
                    }
                    deleteComment(mData[position])
                }).show()
            }).show()
        }
    }

    private fun deleteComment(item: NoteVoiceData.NoteVoiceBean) {
        OkClientHelper.delete(this, "voice/$voiceId/note/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    EventBus.getDefault().post(IUpdataSelfCommentEvent(2, voiceId))
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "voice/$voiceId/note?lastId=$lastId", NoteVoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NoteVoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.code == 0) {
                    if (result.data != null) {
                        if (TextUtils.isEmpty(lastId)) {
                            mData.clear()
                            mData.addAll(result.data)
                            adapter.notifyDataSetChanged()
                        } else {
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                        }
                        if (result.data.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                        lastId = mData[mData.size - 1].id
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    /**
     * 上传
     */
    private fun updateVoice(formBody: FormBody) {
        OkClientHelper.post(this, "voice/$voiceId/note", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    lastId = ""
                    request(0)
                    EventBus.getDefault().post(IUpdataSelfCommentEvent(1, voiceId))
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("TimeMachine : 给自己留言资源上传成功", System.currentTimeMillis())
                    updateVoice(FormBody.Builder().add("bucketId", AppTools.bucketId).add("noteUri", voice).add("noteLen", voiceLength).build())
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        if (!isVisibleActivity) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
                audioPlayer.stop()
            }
        }
    }

    override fun finish() {
        super.finish()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }

}