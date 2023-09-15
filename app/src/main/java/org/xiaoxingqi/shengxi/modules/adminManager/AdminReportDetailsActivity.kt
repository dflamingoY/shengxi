package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_admin_report_details.*
import kotlinx.android.synthetic.main.activity_admin_report_details.btn_Back
import kotlinx.android.synthetic.main.activity_admin_report_details.transLayout
import kotlinx.android.synthetic.main.activity_admin_report_details.tv_Title
import kotlinx.android.synthetic.main.activity_check_chat.*
import kotlinx.android.synthetic.main.item_admin_voice_list.view.*
import kotlinx.android.synthetic.main.layout_voice_anim.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
import org.xiaoxingqi.shengxi.impl.event.AdminReportEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.IOException

class AdminReportDetailsActivity : BaseAct() {
    private lateinit var data: BaseAdminReportBean
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var playBean: BaseAnimBean

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
                playBean.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_admin_report_details
    }

    override fun initView() {

    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        data = intent.getParcelableExtra("data")
        try {
            glideUtil.loadGlide(data.to_user_info.avatar_url, roundImg, 0, glideUtil.getLastModified(data.to_user_info.avatar_url))
            tv_UserName.text = data.to_user_info.nick_name
            tvTime.text = TimeUtils.getInstance().paserFriends(this, data.created_at)
            iv_user_type.visibility = if (data.to_user_info.identity_type == 0) View.GONE else View.VISIBLE
            iv_user_type.isSelected = data.to_user_info.identity_type == 1
            when (data.resource_type) {
                1 -> {//举报声兮 展示图片或者音频
                    imageGroup.setData(data.voice.img_list)
                    linearOperate.visibility = View.VISIBLE
                    tv_confirm.text = "隐藏"
                    tv_Sub.visibility = View.GONE
                    val resource = intent.getParcelableExtra<BaseSearchBean?>("resource")
                    if (resource != null) {
                        if (!TextUtils.isEmpty(resource.id)) {
                            itemDynamic.visibility = View.VISIBLE
                            itemDynamic.setData(resource, data.voice.resource_type, data.voice.user_score)
                        } else {
                            itemDynamic.visibility = View.GONE
                        }
                    } else {
                        itemDynamic.visibility = View.GONE
                    }
                    voiceProgress.data = data.voice
                    tv_Title.text = "被举报心情"
                    playBean = data.voice
                    if (data.voice.hide_at > 0) {
                        tv_confirm.text = "已隐藏"
                    }
                    tv_delete.text = when (data.voice.voice_status) {
                        1 -> "删除"
                        2 -> "用户已删除"
                        3 -> "系统已删除"
                        else -> "删除"
                    }
                }
                3 -> {//举报对话
                    playBean = data.dialog
                    relative_Content.visibility = View.GONE
                    tv_Sub.visibility = View.VISIBLE
                    tv_Title.text = if (data.dialog.chat_type == 1) {//私聊
                        "被举报的回声"
                    } else if (data.dialog.chat_type == 2) {
                        tv_Sub.text = "查看对象"
                        "被举报的私聊"
                    } else {
                        tvDeleteGraffiti.visibility = View.VISIBLE
                        tvDeleteGraffiti.text = if (data.dialog.dialog_status == 1) "删除" else "已删除"
                        tv_Sub.text = "查看对话"
                        "被举报的涂鸦对话"
                    }
                    if (data.dialog.chat_type == 2 || data.dialog.chat_type == 3) {
                        if (data.dialog.resource_type == "2") {
                            voiceProgress.visibility = View.GONE
                            square_art.visibility = View.VISIBLE
                            Glide.with(this)
                                    .applyDefaultRequestOptions(RequestOptions().error(R.drawable.drawable_default_tmpry)
                                            .signature(ObjectKey(data.dialog.resource_url)))
                                    .load(data.dialog.resource_url)
                                    .into(iv_art)
                        } else {
                            voiceProgress.data = data.dialog
                        }
                    } else {
                        voiceProgress.data = data.dialog
                    }
                    request(0)
                }
                5 -> {//举报艺术评
                    linearOperate.visibility = View.VISIBLE
                    tv_Title.text = "被举报灵魂画手"
                    relative_Content.visibility = View.GONE
                    tv_Sub.visibility = View.GONE
                    voiceProgress.visibility = View.GONE
                    square_art.visibility = View.VISIBLE
                    Glide.with(this)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(data.artwork.artwork_url)))
                            .load(data.artwork.artwork_url)
                            .into(iv_art)
                    if (data.artwork.hide_at > 0) {
                        tv_confirm.text = "已设为仅作者可见"
                    }
                    tv_delete.text = when (data.artwork.artwork_status) {
                        1 -> "删除"
                        2 -> "用户已删除"
                        3 -> "系统已删除"
                        else -> "删除"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initEvent() {
        tvDeleteGraffiti.setOnClickListener {
            deleteGraffiti()
        }
        btn_Back.setOnClickListener { finish() }
        tv_confirm.setOnClickListener {
            when (data.resource_type) {
                1 -> {
                    hideItem(if (data.voice.hide_at > 0) 0 else 1)
                }
                5 -> {
                    setPrivacy(if (data.artwork.hide_at > 0) 0 else 1)
                }
            }
        }
        tv_delete.setOnClickListener {
            when (data.resource_type) {
                1 -> if (data.voice.voice_status == 1) operatorDelete() else if (data.voice.voice_status == 3) restoreVoice()
                5 -> if (data.artwork.artwork_status == 1) deleteArt() else if (data.artwork.artwork_status == 3) restoreArt()
            }
        }
        voiceProgress.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                playBean.pasuePosition = audioPlayer.currentPosition.toInt()
                audioPlayer.stop()
                progressHandler.stop()
                voiceProgress.finish()
                return@setOnClickListener
            } else {
                progressHandler.stop()
            }
            download(when (data.resource_type) {
                1 -> data.voice.voice_url
                3 -> data.dialog.resource_url
                else -> ""
            })
        }
        viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!viewSeekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        playBean.allDuration = audioPlayer.duration
                        progressHandler.stop()
                        audioPlayer.stop()
                        playBean.isPlaying = false
                        voiceProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                playBean.pasuePosition = (progress * playBean.allDuration).toInt()
                download(when (data.resource_type) {
                    1 -> data.voice.voice_url
                    3 -> data.dialog.resource_url
                    else -> ""
                })
            }
        })
        voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (playBean.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    progressHandler.stop()
                    playBean.isPlaying = false
                    playBean.pasuePosition = 1
                    voiceProgress.finish()
                }
            }
            playBean.isPlaying = false
            playBean.pasuePosition = 0
            download(when (data.resource_type) {
                1 -> data.voice.voice_url
                3 -> data.dialog.resource_url
                else -> ""
            })
        }
        tv_Sub.setOnClickListener {
            if (data.resource_type == 3) {//对话
                startActivity<AdminChatListActivity>("chatId" to data.dialog.chat_id, "chatType" to data.dialog.chat_type)
            } else {
                startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", data.to_user_info.id))
            }
            /* if (TextUtils.isEmpty(data.dialog.voice_id) || "0" == data.dialog.voice_id) {//私聊
             } else {
                 startActivity(Intent(this, DynamicDetailsActivity::class.java).putExtra("id", data.dialog.voice_id).putExtra("uid", data.dialog.voice_user_id))
             }*/
        }
        roundImg.setOnClickListener {
            startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", data.to_user_info.id))
        }
        iv_art.setOnClickListener {
            startActivity(Intent(this, ShowPicActivity::class.java).putExtra("path", when (data.resource_type) {
                3 -> data.dialog.resource_url
                5 -> data.artwork.artwork_url
                else -> ""
            }))
            overridePendingTransition(0, 0)
        }
    }

    private fun deleteGraffiti() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/dialog/${data.dialog.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    tvDeleteGraffiti.text = "已删除"
                    showToast("操作成功")
                }
            }
        })
    }

    private fun download(voiceUrl: String) {
        if (TextUtils.isEmpty(voiceUrl)) {
            showToast("资源路径错误")
            return
        }
        play.isSelected = !play.isSelected

        val file = getDownFilePath(voiceUrl)
        if (file.exists()) {
            audioPlayer.setDataSource(file.absolutePath)
            audioPlayer.start(if (SPUtils.getBoolean(this@AdminReportDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        } else {
            OkClientHelper.downFile(this@AdminReportDetailsActivity, voiceUrl, { o ->
                try {
                    if (null == o) {
                        showToast(resources.getString(R.string.string_error_file))
                        return@downFile
                    }
                    if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                        return@downFile
                    audioPlayer.setDataSource(o.toString())
                    audioPlayer.start(if (SPUtils.getBoolean(this@AdminReportDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, { showToast(VolleyErrorHelper.getMessage(it)) })
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onCompletion() {
                voiceProgress.finish()
                playBean.isPlaying = false
                progressHandler.stop()
            }

            override fun onInterrupt() {
                voiceProgress.finish()
                playBean.isPlaying = false
                progressHandler.stop()
            }

            override fun onPrepared() {
                playBean.allDuration = audioPlayer.duration
                audioPlayer.seekTo(playBean.pasuePosition)
                playBean.pasuePosition = 0
                playBean.isPlaying = true
                progressHandler.start()
            }
        }
    }

    /**
     * 回复系统删除的心情
     */
    private fun restoreVoice() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/voices/${data.voice.voice_id}", FormBody.Builder().add("voiceStatus", "1")
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_delete.text = "删除"
                    showToast("用户心情已恢复")
                    data.voice.voice_status = 1
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.voice.hide_at, data.voice.voice_status))
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
     * 删除心情
     */
    private fun operatorDelete() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/voices/${data.voice.voice_id}",
                FormBody.Builder().add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_delete.text = "已删除"
                    data.voice.voice_status = 3
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.voice.hide_at, data.voice.voice_status))
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
     * 隐藏心情 或者显示心情
     */
    private fun hideItem(i: Int) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/voices/${data.voice.voice_id}", FormBody.Builder().add("isHidden", "$i")
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_confirm.text = if (i == 0) "隐藏" else "已隐藏"
                    data.voice.hide_at = if (i == 0) 0 else (System.currentTimeMillis() / 1000).toInt()
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.voice.hide_at, data.voice.voice_status))
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
     * 编辑艺术品
     */
    private fun setPrivacy(i: Int) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/artwork/${data.artwork.id}", FormBody.Builder().add("isHidden", "$i")
                .add("artworkStatus", data.artwork.artwork_status.toString())
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_confirm.text = if (i == 0) "设为仅作者可见" else "已设为仅作者可见"
                    data.artwork.hide_at = if (i == 0) 0 else System.currentTimeMillis().toInt() / 1000
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.artwork.hide_at, data.artwork.artwork_status))
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
     * 恢复用户的艺术品
     */
    private fun restoreArt() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/artwork/${data.artwork.id}", FormBody.Builder().add("artworkStatus", "1")
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_delete.text = "删除"
                    showToast("已恢复灵魂画手")
                    data.artwork.artwork_status = 1
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.artwork.hide_at, data.artwork.artwork_status))
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

    //刪除藝術品
    private fun deleteArt() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/artwork/${data.artwork.id}", FormBody.Builder().add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    tv_delete.text = "已删除"
                    data.artwork.artwork_status = 3
                    EventBus.getDefault().post(AdminReportEvent(data.id, data.artwork.hide_at, data.artwork.artwork_status))
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

    override fun request(flag: Int) {
        OkClientHelper.get(this, "admin/dialogs/${data.dialog.id}?token=${SPUtils.getString(this, IConstant.ADMINTOKEN, "")}",
                CommentCallBackData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as CommentCallBackData
                result.data?.let {
                    if (it.resource_type == "1") {
                        linear_content.visibility = View.VISIBLE
                        tvContent.text = if (!TextUtils.isEmpty(it.recognition_content)) {
                            it.recognition_content
                        } else "语音转文字失败"
                    }
                }
            }
        })
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            if (playBean.isPlaying) {
                playBean.isPlaying = false
                playBean.pasuePosition = 0
                progressHandler.stop()
            }
            audioPlayer.stop()
        }
    }

}
