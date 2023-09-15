package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_check_voice.*
import kotlinx.android.synthetic.main.activity_check_voice.btn_Back
import kotlinx.android.synthetic.main.activity_check_voice.transLayout
import kotlinx.android.synthetic.main.item_admin_world.view.*
import kotlinx.android.synthetic.main.item_admin_world.view.linear_content
import kotlinx.android.synthetic.main.item_admin_world.view.roundImg
import kotlinx.android.synthetic.main.item_admin_world.view.tvContent
import kotlinx.android.synthetic.main.item_admin_world.view.tv_UserName
import kotlinx.android.synthetic.main.item_admin_world.view.voiceProgress
import kotlinx.android.synthetic.main.layout_admin_bottom_operator.tvIgnore
import kotlinx.android.synthetic.main.layout_admin_bottom_operator.tvReplace
import kotlinx.android.synthetic.main.layout_admin_bottom_operator.tvUserFlag
import kotlinx.android.synthetic.main.layout_admin_bottom_operator.tvUserStatus
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.model.AdminCheckListData
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.DynamicDatailData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.utils.AudioPlayer
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.IOException

class CheckVoiceActivity : BaseAct() {
    private lateinit var data: AdminCheckListData.AdminCheckBean
    private var voiceBean: BaseBean? = null
    private lateinit var audioPlayer: AudioPlayer
    private var userInfo: UserInfoData.UserBean? = null
    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            layoutData.voiceProgress.updateProgress(audioPlayer.currentPosition.toInt())
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
                voiceBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                voiceBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                voiceBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                voiceBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_check_voice
    }

    override fun initView() {
        layoutData.linearOperate.visibility = View.GONE
        tvReplace.text = "删除心情"
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        data = intent.getParcelableExtra("data")
        glideUtil.loadGlide(data.user.avatar_url, layoutData.roundImg, 0, glideUtil.getLastModified(data.user.avatar_url))
        layoutData.tv_UserName.text = data.user.nick_name
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvHide.setOnClickListener {
            voiceBean?.let {
                transLayout.showProgress()
                operatorVoice("admin/voices/${it.voice_id}", FormBody.Builder().add("isHidden", if (it.hide_at > 0) "3" else "1")
                        .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()
                ) { result ->
                    transLayout.showContent()
                    result?.let { _ ->
                        if (result.code == 0) {
                            it.hide_at = if (it.hide_at > 0) 0 else (System.currentTimeMillis() / 1000).toInt()
                            tvHide.text = if (it.hide_at > 0) "已隐藏" else "隐藏"
                        }
                    }
                }
            }
        }
        tvIgnore.setOnClickListener {
            transLayout.showProgress()
            operator("${data.id}", 2) {
                transLayout.showContent()
                tvIgnore.text = "已忽略"
                EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
            }
        }
        tvReplace.setOnClickListener {
            voiceBean?.let {
                transLayout.showProgress()
                deleteVoice("admin/voices/${it.voice_id}", FormBody.Builder()
                        .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()
                ) { result ->
                    transLayout.showContent()
                    result?.let { _ ->
                        if (result.code == 0) {
                            it.voice_status = if (it.voice_status == 1) 3 else 1
                            tvReplace.text = "已删除"
                            operator("${data.id}", 2) {
                                EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
                            }
                        }
                    }
                }
            }
        }
        tvUserFlag.setOnClickListener {
            userInfo?.let {
                transLayout.showProgress()
                operatorUser(it.user_id, FormBody.Builder().add("flag", if (it.flag == "1") "0" else "1").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()) {
                    transLayout.showContent()
                    userInfo!!.flag = if (userInfo!!.flag == "1") "0" else "1"
                    tvUserFlag.text = if (userInfo!!.flag == "1") "已设为仙人掌" else "设为仙人掌"
                }
            }
        }
        tvUserStatus.setOnClickListener {
            userInfo?.let {
                transLayout.showProgress()
                operatorUser(it.user_id, FormBody.Builder().add("userStatus", if (it.user_status == "3") "1" else "3").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()) {
                    transLayout.showContent()
                    userInfo!!.flag = if (userInfo!!.user_status == "3") "1" else "3"
                    tvUserFlag.text = if (userInfo!!.user_status == "1") "封号" else "已封号"
                }
            }
        }
        layoutData.voiceProgress.setOnClickListener {
            if (voiceBean == null)
                return@setOnClickListener
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    handler.stop()
                    voiceBean!!.isPlaying = false
                    voiceBean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    layoutData.voiceProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    handler.stop()
                }
            }
            download(voiceBean!!)
        }
        val seekProgress = layoutData.voiceProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceBean!!.allDuration = audioPlayer.duration
                        handler.stop()
                        audioPlayer.stop()
                        voiceBean!!.isPlaying = false
                        layoutData.voiceProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                /**
                 * 滑动停止
                 */
                voiceBean!!.pasuePosition = (progress * voiceBean!!.allDuration).toInt()
                download(voiceBean!!)
            }
        })
        layoutData.voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    handler.stop()
                    voiceBean!!.isPlaying = false
                    voiceBean!!.pasuePosition = 1
                    layoutData.voiceProgress.finish()
                }
            }
            voiceBean!!.pasuePosition = 0
            download(voiceBean!!)
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onCompletion() {
                voiceBean!!.isPlaying = false
                layoutData.voiceProgress.finish()
                handler.stop()
            }

            override fun onInterrupt() {
                voiceBean!!.isPlaying = false
                layoutData.voiceProgress.finish()
                handler.stop()
            }

            override fun onPrepared() {
                voiceBean!!.allDuration = audioPlayer.duration
                audioPlayer.seekTo(voiceBean!!.pasuePosition)
                voiceBean!!.pasuePosition = 0
                voiceBean!!.isPlaying = true
                handler.start()
            }
        }
    }

    private fun download(item: BaseBean) {
        try {
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                            return@downFile
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, {
                    showToast(VolleyErrorHelper.getMessage(it))
                })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询心情信息
                OkClientHelper.get(this, "voices/${data.user.id}/${data.voice_id}?recognition=1", DynamicDatailData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as DynamicDatailData
                        result.data?.let {
                            voiceBean = it
                            layoutData.voiceProgress.data = it
                            layoutData.imageGroup.setData(it.img_list)
                            layoutData.tvTime.text = TimeUtils.getInstance().paserFriends(this@CheckVoiceActivity, it.created_at)
                            if (!TextUtils.isEmpty(it.topic_name)) {
                                layoutData.tv_Action.text = "#${it.topic_name}#"
                            }
                            if (TextUtils.isEmpty(it.recognition_content)) {
                                layoutData.linear_content.visibility = View.GONE
                            } else
                                layoutData.tvContent.text = it.recognition_content
                            tvReplace.text = if (it.voice_status == 3) "已删除" else "删除心情"
                            tvHide.text = if (it.hide_at > 0) "已隐藏" else "隐藏"
                        }
                    }
                })
            }
            1 -> {//用户信息
                getUserInfo(data.user.id) {
                    userInfo = it.data
                    tvUserFlag.text = if (it.data.flag == "1") "已设为仙人掌" else "设为仙人掌"
                    tvUserFlag.isSelected = it.data.flag == "1"
                    tvUserStatus.isSelected = it.data.user_status == "3"
                    tvUserStatus.text = if (it.data.user_status == "3") "已封号" else "封号"
                }
            }
        }
    }
}