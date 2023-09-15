package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_report_alarm_details.*
import kotlinx.android.synthetic.main.item_alarm_dub.*
import kotlinx.android.synthetic.main.layout_voice_anim.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAlarmEdit
import org.xiaoxingqi.shengxi.dialog.DialogDeleteChatComment
import org.xiaoxingqi.shengxi.dialog.DialogDeleteLocalAlarm
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
import org.xiaoxingqi.shengxi.model.SimpleNullLineAlarmData
import org.xiaoxingqi.shengxi.model.SingleAlarmData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*

class DubbingDetailsActivity : BaseAct() {
    private lateinit var userInfo: UserInfoData
    private lateinit var alarmBean: BaseAlarmBean
    private lateinit var audioPlayer: AudioPlayer
    override fun writeHeadSet(): Boolean {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        return headsetOn || a2dpOn || scoOn
    }

    override fun changSpeakModel(type: Int) {
        try {
            if (type == 1) {
                if (audioPlayer.isPlaying) {
                    val currentPosition = audioPlayer.currentPosition
                    alarmBean.pasuePosition = currentPosition.toInt()
                    audioPlayer.stop()
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            } else {
                if (audioPlayer.isPlaying) {
                    val currentPosition = audioPlayer.currentPosition
                    alarmBean.pasuePosition = currentPosition.toInt()
                    audioPlayer.stop()
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        } catch (e: Exception) {
        }
    }

    val helperHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {
        override fun handleMessage(msg: Message?) {
            if (alarmBean.isPlaying) {
                voiceProgress.updateProgress(audioPlayer.currentPosition.toInt())
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_report_alarm_details
    }

    override fun initView() {
        tv_Title.text = "配音"
        linearOperator.visibility = View.GONE
        iv_more.isSelected = true
//        tv_dubbing_count.visibility = View.GONE
        transLayout.showProgress()
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        relative_Report.setOnClickListener {
            DialogAlarmEdit(this).setProperty(alarmBean.is_anonymous).setOnClickListener(View.OnClickListener {
                when (it.id) {
                    R.id.tv_Delete -> DialogDeleteChatComment(this).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                        deleteDub()
                    }).show()
                    R.id.tv_Self -> editDub()
                }
            }).show()
        }
        linear_god.setOnClickListener {
            if (!TextUtils.isEmpty(alarmBean.vote_id) && alarmBean.vote_option == "3")
                deleteVote(alarmBean, 3)
            else
                artVote(alarmBean, 3)
        }
        linear_monster.setOnClickListener {
            if (!TextUtils.isEmpty(alarmBean.vote_id) && alarmBean.vote_option == "2")
                deleteVote(alarmBean, 2)
            else
                artVote(alarmBean, 2)
        }
        linear_angel.setOnClickListener {
            if (!TextUtils.isEmpty(alarmBean.vote_id) && alarmBean.vote_option == "1")
                deleteVote(alarmBean, 1)
            else
                artVote(alarmBean, 1)
        }
        tv_alarm_word.setOnClickListener {
            startActivity(Intent(this, WordingVoiceActivity::class.java)
                    .putExtra("id", alarmBean.line_id)
                    .putExtra("tagName", alarmBean.line.tag_name)
                    .putExtra("userInfo", alarmBean.to_user_info)
                    .putExtra("toUserId", alarmBean.to_user_id)
                    .putExtra("isDubbed", alarmBean.is_dubbed)
                    .putExtra("dubbingNum", alarmBean.line.dubbing_num)
                    .putExtra("lineContent", alarmBean.line.line_content))
        }
        roundImg.setOnClickListener {
            startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", alarmBean.from_user_info.id))
        }
        iv_Privacy.setOnClickListener {
            if (!alarmBean.checkExist().isDownload) {
                downFile(alarmBean, "${alarmBean.from_user_info.nick_name} ${AppTools.getSuffix(alarmBean.dubbing_url)}") {
                    //下载成功
                    iv_Privacy.visibility = View.GONE
                    tv_download.visibility = View.VISIBLE
                }
            }
        }
        tv_download.setOnClickListener {
            if (alarmBean.checkExist().isDownload)
                DialogDeleteLocalAlarm(this).setOnClickListener(View.OnClickListener {
                    alarmBean.deleteFile {
                        iv_Privacy.visibility = View.VISIBLE
                        tv_download.visibility = View.GONE
                    }
                }).show()
        }
        viewSeekProgress.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (alarmBean.isPlaying) {//当前正在播放
                    helperHandler.stop()
                    alarmBean.pasuePosition = audioPlayer.currentPosition.toInt()
                    alarmBean.isPlaying = false
                    audioPlayer.stop()
                    voiceProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    voiceProgress.finish()
                }
            }
            download()
        }
        viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!viewSeekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        alarmBean.allDuration = audioPlayer.duration
                        helperHandler.stop()
                        audioPlayer.stop()
                        alarmBean.isPlaying = false
                        voiceProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                alarmBean.pasuePosition = (progress * alarmBean.allDuration).toInt()
                download()
            }
        })
        findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (alarmBean.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    helperHandler.stop()
                    alarmBean.isPlaying = false
                    alarmBean.pasuePosition = 1
                    voiceProgress.finish()
                }
            }
            alarmBean.pasuePosition = 0
            download()
        }
    }

    private fun download() {
        if (TextUtils.isEmpty(alarmBean.dubbing_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        findViewById<View>(R.id.play).isSelected = !findViewById<View>(R.id.play).isSelected

        downPlay(alarmBean.dubbing_url) {it, _ ->
            audioPlayer.setDataSource(it)
            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                alarmBean.allDuration = audioPlayer.duration
                audioPlayer.seekTo(alarmBean.pasuePosition)
                alarmBean.pasuePosition = 0
                alarmBean.isPlaying = true
                helperHandler.start()
            }

            override fun onCompletion() {
                alarmBean.isPlaying = false
                voiceProgress.finish()
                helperHandler.stop()
            }

            override fun onInterrupt() {
                alarmBean.isPlaying = false
                voiceProgress.finish()
                helperHandler.stop()
            }
        }
    }

    private fun deleteDub() {
        deleteDub(alarmBean, transLayout) {
            finish()
        }
    }

    /**
     * 编辑可见性
     */
    private fun editDub() {
        editDub(alarmBean, transLayout) {
            editUserVisible()
        }
    }

    private fun artVote(bean: BaseAlarmBean, type: Int) {
        artVote(bean, transLayout, linearOperate, type) { _, _ ->
            showOperatorTitle()
        }
    }

    //删除投票
    private fun deleteVote(item: BaseAlarmBean, voteType: Int) {
        deleteVote(item, transLayout, linearOperate, voteType) { _, _ ->
            showOperatorTitle()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showOperatorTitle() {
        try {
            tv_angel.text = "您是天使" + if (alarmBean.vote_option_one > 0) " ${alarmBean.vote_option_one}" else ""
            tv_monster.text = "您是恶魔" + if (alarmBean.vote_option_two > 0) " ${alarmBean.vote_option_two}" else ""
            tv_god.text = "您是神" + if (alarmBean.vote_option_three > 0) " ${alarmBean.vote_option_three}" else ""
        } catch (e: Exception) {
        }
    }

    private fun editUserVisible() {
        if (alarmBean.is_anonymous == "1") {
            tv_UserName.text = resources.getString(R.string.string_alarm_anonymous)
            Glide.with(this@DubbingDetailsActivity)
                    .load(R.mipmap.icon_user_default)
                    .into(roundImg)
            iv_user_type.visibility = View.GONE
        } else {
            glideUtil.loadGlide(alarmBean.from_user_info.avatar_url, roundImg, 0, glideUtil.getLastModified(alarmBean.from_user_info.avatar_url))
            tv_UserName.text = alarmBean.from_user_info.nick_name
            iv_user_type.isSelected = alarmBean.from_user_info.identity_type == 1
            iv_user_type.visibility = if (alarmBean.from_user_info.identity_type == 0) View.GONE else View.VISIBLE
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "dubbings/${intent.getStringExtra("id")}", SingleAlarmData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as SingleAlarmData).code == 0) {
                    alarmBean = result.data.let {
                        /* BaseAlarmBean().apply {
                             dubbing_len = it.dubbing_len
                             dubbing_status = it.dubbing_status
                             dubbing_url = it.dubbing_url
                             line_id = it.line_id
                             to_user_id = it.to_user_id
                             line_content = it.line_content
                             from_user_id = it.from_user_id
                             hide_at = it.hide_at
                             id = it.id
                             is_anonymous = it.is_anonymous
                             vote_option = it.vote_option
                             vote_option_one = it.vote_option_one
                             vote_option_two = it.vote_option_two
                             vote_option_three = it.vote_option_three
                         }*/
                        it
                    }.checkUser(userInfo).checkExist()
                    //show data
                    tv_dubbing_count.text = alarmBean.line.dubbing_num
                    voiceProgress.data = alarmBean
                    tv_alarm_word.text = "#${alarmBean.line.tag_name}#" + alarmBean.line.line_content
                    iv_Privacy.visibility = if (alarmBean.isDownload) View.GONE else View.VISIBLE
                    tv_download.visibility = if (!alarmBean.isDownload) View.GONE else View.VISIBLE
                    tvTime.text = TimeUtils.getInstance().paserFriends(this@DubbingDetailsActivity, alarmBean.created_at.toInt())
                    editUserVisible()
                    if (!TextUtils.isEmpty(alarmBean.vote_id))
                        when (alarmBean.vote_option) {
                            "1" -> iv_angel.isSelected = true
                            "2" -> iv_monster.isSelected = true
                            "3" -> iv_god.isSelected = true
                        }
                    showOperatorTitle()
                    transLayout.showContent()
                } else {
                    showToast(result.msg)
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showEmpty()
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        if (!isVisibleActivity)
            if (alarmBean.isPlaying) {
                alarmBean.isPlaying = false
                audioPlayer.stop()
                helperHandler.stop()
            }
    }

    override fun onDestroy() {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        super.onDestroy()
        helperHandler.removeCallbacks(helperHandler)
    }
}