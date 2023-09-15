package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_check_chat.*
import kotlinx.android.synthetic.main.item_admin_voice_list.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.AudioPlayer
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.IOException

//审核私聊对话
class CheckChatActivity : BaseAct() {
    private lateinit var data: AdminCheckListData.AdminCheckBean
    private var bean: TalkListData.TalkListBean? = null
    private var userInfoData: UserInfoData? = null
    private lateinit var audioPlayer: AudioPlayer

    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            layoutChat.voiceProgress.updateProgress(audioPlayer.currentPosition.toInt())
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
                bean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                bean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                bean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                bean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_check_chat
    }

    override fun initView() {

    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        data = intent.getParcelableExtra("data")
        glideUtil.loadGlide(data.user.avatar_url, layoutChat.roundImg, 0, glideUtil.getLastModified(data.user.avatar_url))
        layoutChat.tv_UserName.text = data.user.nick_name
        request(0)
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvChatList.setOnClickListener {
            bean?.let {
                startActivity<AdminChatListActivity>("chatId" to it.chat_id, "chatType" to it.chat_type)
            }
        }
        ivChatImg.setOnClickListener {
            bean?.let {
                startActivity<ShowPicActivity>("path" to it.resource_url)
            }
        }
        tvIgnore.setOnClickListener {
            operator(2)
        }
        tvDelete.setOnClickListener {
            bean?.let {
                if (it.dialog_status != 3) {
                    deleteChat()
                }
            }
        }
        tvUserType.setOnClickListener {//仙人掌
            userInfoData?.let {
                adminUpdate(1, if (it.data.flag == "1") 0 else 1, FormBody.Builder().add("flag", if (it.data.flag == "1") "0" else "1").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build())
            }
        }
        tvBanUser.setOnClickListener {//封号
            userInfoData?.let {
                adminUpdate(2, if (it.data.user_status == "3") 0 else 1, FormBody.Builder().add("userStatus", if (it.data.user_status == "3") "1" else "3").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build())
            }
        }
        layoutChat.tv_Sub.setOnClickListener {
            startActivity<UserDetailsActivity>("id" to data.user.id)
        }
        layoutChat.voiceProgress.setOnClickListener {
            if (bean == null)
                return@setOnClickListener
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (bean!!.isPlaying) {//当前正在播放
                    handler.stop()
                    bean!!.isPlaying = false
                    bean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    layoutChat.voiceProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    handler.stop()
                }
            }
            download(bean!!)
        }
        val seekProgress = layoutChat.voiceProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        bean!!.allDuration = audioPlayer.duration
                        handler.stop()
                        audioPlayer.stop()
                        bean!!.isPlaying = false
                        layoutChat.voiceProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                /**
                 * 滑动停止
                 */
                bean!!.pasuePosition = (progress * bean!!.allDuration).toInt()
                download(bean!!)
            }
        })
        layoutChat.voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (bean!!.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    handler.stop()
                    bean!!.isPlaying = false
                    bean!!.pasuePosition = 1
                    layoutChat.voiceProgress.finish()
                }
            }
            bean!!.pasuePosition = 0
            download(bean!!)
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onCompletion() {
                bean!!.isPlaying = false
                layoutChat.voiceProgress.finish()
                handler.stop()
            }

            override fun onInterrupt() {
                bean!!.isPlaying = false
                layoutChat.voiceProgress.finish()
                handler.stop()
            }

            override fun onPrepared() {
                bean!!.allDuration = audioPlayer.duration
                audioPlayer.seekTo(bean!!.pasuePosition)
                bean!!.pasuePosition = 0
                bean!!.isPlaying = true
                handler.start()
            }
        }
    }

    private fun download(item: TalkListData.TalkListBean) {
        try {
            if (TextUtils.isEmpty(item.resource_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            val file = getDownFilePath(item.resource_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this, item.resource_url, { o ->
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

    private fun deleteChat() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/dialog/${data.data_id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    tvDelete.text = "已删除"
                    operator(3)
                }
            }
        })
    }

    /**
     * @param type 2 无问题 3 删除 5 封号
     */
    private fun operator(type: Int) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/suspiciousDatas/${data.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .add("operationType", "$type").build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
                    if (type == 2) {
                        tvIgnore.text = "已忽略"
                    } else {
                        tvIgnore.text = "已处理"
                    }
                    tvIgnore.isSelected = true
                }
            }
        })
    }

    /**
     * @param status 设置为 0 正常 1 异常
     */
    private fun adminUpdate(type: Int, status: Int, formBody: FormBody) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/users/${data.user.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (type == 1) {
                        tvUserType.text = if (status == 0) "设置为仙人掌" else "已设为仙人掌"
                        tvUserType.isSelected = status != 0
                    } else {
                        tvBanUser.text = if (status == 0) "封号" else "已封号"
                        operator(5)
                        tvBanUser.isSelected = status != 0
                    }
                    showToast("操作已执行")
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "admin/dialogs/${data.data_id}?token=${SPUtils.getString(this, IConstant.ADMINTOKEN, "")}",
                        CommentCallBackData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as CommentCallBackData
                        result.data?.let {
                            bean = it
                            if (it.resource_type == "1") {
                                if (!TextUtils.isEmpty(it.recognition_content)) {
                                    layoutChat.tvContent.text = it.recognition_content
                                }
                                layoutChat.voiceProgress.data = it
                            } else {
                                layoutChat.relative_resource.visibility = View.GONE
                                layoutChat.linear_content.visibility = View.GONE
                                cardLayout.visibility = View.VISIBLE
                                Glide.with(this@CheckChatActivity)
                                        .applyDefaultRequestOptions(RequestOptions().centerCrop().signature(ObjectKey(it.resource_url)))
                                        .load(it.resource_url)
                                        .into(ivChatImg)
                            }
                            tvDelete.text = if (it.dialog_status == 3) "已删除" else "删除对话"
                            tvDelete.isSelected = it.dialog_status == 3
                        }
                    }
                })
            }
            1 -> {//查询用户信息, 是否封号 仙人掌
                OkClientHelper.get(this, "users/${data.user.id}", UserInfoData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as UserInfoData
                        result.data?.let {
                            userInfoData = result
                            tvUserType.text = if (it.flag == "1") "已设为仙人掌" else "设为仙人掌"
                            tvUserType.isSelected = it.flag == "1"
                            tvBanUser.text = if (it.user_status == "3") "已封号" else "封号"
                            tvBanUser.isSelected = it.user_status == "3"
                        }
                    }
                })
            }
        }
    }
}