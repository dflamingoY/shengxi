package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm_report_details.*
import kotlinx.android.synthetic.main.item_alarm_dub.*
import kotlinx.android.synthetic.main.layout_voice_anim.*
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmAdminBean
import org.xiaoxingqi.shengxi.modules.listen.alarm.WordingVoiceActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.downPlay
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.AudioPlayer
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

class AlarmReportDetailsActivity : BaseAct() {
    private lateinit var data: BaseAlarmAdminBean
    private var type = 7 //7 台词 8 配音

    private lateinit var audioPlayer: AudioPlayer
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            val currentPosition = audioPlayer.currentPosition.toInt()
            voiceProgress.updateProgress(currentPosition)
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
                data.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                data.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_report_details
    }

    override fun initView() {
        linearOperate.visibility = View.GONE
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        data = intent.getParcelableExtra("data")
        type = intent.getIntExtra("type", 7)
        tvHide.text = if (data.hide_at > 0) "已隐藏" else "隐藏"
        if (type == 7) {
            tv_Title.text = "被举报的台词"
            tvDeleteResource.visibility = View.GONE
            voiceProgress.visibility = View.GONE
            tvDelete.text = when (data.line_status) {
                "1" -> "删除"
                "2" -> "用户已删除"
                "3" -> "已删除"
                else -> "删除"
            }
        } else {
            tv_Title.text = "被举报的配音"
            voiceProgress.data = data
            tvDelete.text = when (data.dubbing_status) {
                "1" -> "删除"
                "2" -> "用户已删除"
                "3" -> "已删除"
                else -> "删除"
            }
            tv_dubbing_count.text = data.dubbing_num
        }
        if (data.dubbing_status != "1" && data.line_status != "1") {
            tvDeleteResource.text = "恢复台词和配音"
        }
        glideUtil.loadGlide(if (type == 7) data.user.avatar_url else data.from_user_info.avatar_url, roundImg, 0, glideUtil.getLastModified(if (type == 7) data.user.avatar_url else data.from_user_info.avatar_url))
        tv_UserName.text = if (type == 7) data.user.nick_name else data.from_user_info.nick_name
        tvTime.text = TimeUtils.getInstance().paserFriends(this, data.created_at.toInt())
        tv_alarm_word.text = data.line_content
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvDelete.setOnClickListener {
            if (type == 7) {
                if (data?.line_status == "1") {
                    deleteWord()
                } else
                    restoreWord()
            } else {
                if (data?.dubbing_status == "1")
                    deleteDubbing()
                else {
                    if (data.line_status != "1") {
                        showToast("请先恢复台词")
                    } else
                        restoreDub()
                }
            }
        }
        tvHide.setOnClickListener {
            if (type == 7) {//隐藏台词
                if (data.line_status == "1" && data.hide_at == 0) {
                    hideWord(System.currentTimeMillis())
                } else {//展示台词
                    hideWord(0)
                }
            } else
                editDubbing()
        }
        voiceProgress.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (data.isPlaying) {//当前正在播放
                    progressHandler.stop()
                    data.isPlaying = false
                    data.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    voiceProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    progressHandler.stop()
                }
            }
            download()
        }
        viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!viewSeekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        data.allDuration = audioPlayer.duration
                        progressHandler.stop()
                        audioPlayer.stop()
                        data.isPlaying = false
                        voiceProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                data.pasuePosition = (progress * data.allDuration).toInt()
                download()
            }
        })
        findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (data.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    progressHandler.stop()
                    data.isPlaying = false
                    data.pasuePosition = 1
                    finish()
                }
            }
            data.pasuePosition = 0
            download()
        }
        roundImg.setOnClickListener {
            try {
                startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", if (type == 7) data.user.id else data.from_user_info.id))
            } catch (e: Exception) {
            }
        }
        tvDeleteResource.setOnClickListener {
            //删除台词和配音
            if (data.line_status == "1") {
                //删除台词, 等于删除所有
                deleteWord()
            } else {//恢复
                restoreWord()
            }
        }
        relativeWord.setOnClickListener {
            startActivity<WordingVoiceActivity>("userInfo" to if (type == 7) data.user else null,
                    "toUserId" to data.to_user_id,
                    "isDubbed" to data.is_dubbed,
                    "tagName" to data.tag_name,
                    "id" to if (type == 7) data.id else data.line_id,
                    "lineContent" to data.line_content
            )
        }
    }

    private fun download() {
        if (TextUtils.isEmpty(data.dubbing_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        downPlay(data!!.dubbing_url) { it, _ ->
            audioPlayer.setDataSource(it)
            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                data.allDuration = audioPlayer.duration
                audioPlayer.seekTo(data.pasuePosition)
                data.pasuePosition = 0
                data.isPlaying = true
                progressHandler.start()
            }

            override fun onCompletion() {
                data.isPlaying = false
                voiceProgress.finish()
                progressHandler.stop()
            }

            override fun onInterrupt() {
                data.isPlaying = false
                voiceProgress.finish()
                progressHandler.stop()
            }
        }
    }

    private fun hideWord(hide_at: Long) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/lines/${data.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
//                .add("lineStatus", "1")
                .add("hideAt", "$hide_at")
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    if (hide_at == 0L) {
                        tvHide.text = "隐藏"
                    } else {
                        tvHide.text = "已隐藏"
                    }
                    data.hide_at = hide_at.toInt()
//                    data?.line_status = "3"
//                    data?.line_status = "1"
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

    private fun editDubbing() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/dubbings/${data?.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .add("hideAt", if (data.hide_at > 0) "0" else "${System.currentTimeMillis()}").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    if (data.hide_at > 0) {
                        tvHide.text = "隐藏"
                        data.hide_at = 0
                    } else {
                        tvHide.text = "已隐藏"
                        data.hide_at = (System.currentTimeMillis() / 1000).toInt()
                    }
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

    private fun deleteDubbing() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/dubbings/${data?.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    tvDelete.text = "已删除"
                    data?.dubbing_status = "3"
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun deleteWord() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/lines/${if (type == 7) data.id else data.line_id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    data.line_status = "3"
                    data.dubbing_status = "3"
                    if (type == 7) {
                        tvDelete.text = "已删除"
                    } else {
                        tvDeleteResource.text = "恢复台词和配音"
                        tvDelete.text = "恢复"
                    }
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    //恢复台词
    private fun restoreWord() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/lines/${if (type == 7) data.id else data.line_id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .add("lineStatus", "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    data?.line_status = "1"
                    if (type == 7) {
                        tvDelete.text = "删除"
                    } else {
                        tvDeleteResource.text = "删除台词和配音"
                        if (data.dubbing_status != "1") {
                            restoreDub()
                        }
                    }
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

    //恢复删除配音
    private fun restoreDub() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/dubbings/${data.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).add("dubbingStatus", "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    tvDelete.text = "删除"
                    data?.dubbing_status = "1"
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

    override fun onDestroy() {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }
}