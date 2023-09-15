package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_alarm_report_details.*
import kotlinx.android.synthetic.main.item_alarm_dub.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAlarmEdit
import org.xiaoxingqi.shengxi.dialog.DialogDeleteChatComment
import org.xiaoxingqi.shengxi.dialog.DialogDeleteLocalAlarm
import org.xiaoxingqi.shengxi.dialog.DialogDeleteWording
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.modules.listen.alarm.*
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

/**
 * 台词和配音始终都是改用户的
 */
class AlarmResponseActivity : BaseAct() {
    private var id: String? = null
    private var type: String? = null//1 台词 2 配音
    private var data: BaseAlarmBean? = null
    private lateinit var userInfo: UserInfoData
    private val audioPlayer by lazy { AudioPlayer(this) }
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
                data?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                data?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                data?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                data?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_report_details
    }

    override fun initView() {
        linearOperateMain.visibility = View.GONE
    }

    override fun initData() {
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        id = intent.getStringExtra("id")
        type = intent.getStringExtra("type")
        tv_Title.text = if (type == "1") {
            voiceProgress.visibility = View.GONE
            request(0)
            resources.getString(R.string.string_response) + resources.getString(R.string.string_alarm_1)
        } else {
//            tv_dubbing_count.visibility = View.GONE
            request(1)
            "${resources.getString(R.string.string_response)}配音"
        }
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                progressHandler.stop()
                data?.let {
                    it.isPlaying = false
                }
                voiceProgress.finish()
            }

            override fun onPrepared() {
                data?.let {
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onInterrupt() {
                data?.let {
                    it.isPlaying = false
                }
                voiceProgress.finish()
                progressHandler.stop()
            }
        }
        btn_Back.setOnClickListener { finish() }
        linear_angel.setOnClickListener {
            data?.let { bean ->
                if (!TextUtils.isEmpty(bean.vote_id) && bean.vote_option == "1")
                    if (type == "1") deleteLineVote(bean, iv_angel)
                    else deleteDubVote(bean, linearOperate, 1)
                else
                    if (type == "1") artVoteLine(bean, iv_angel, 1)
                    else artVote(bean, linearOperate, 1)
            }
        }
        linear_monster.setOnClickListener {
            data?.let { bean ->
                if (!TextUtils.isEmpty(bean.vote_id) && bean.vote_option == "2")
                    if (type == "1") deleteLineVote(bean, iv_monster)
                    else deleteDubVote(bean, linearOperate, 2)
                else
                    if (type == "1") artVoteLine(bean, iv_monster, 2)
                    else artVote(bean, linearOperate, 2)
            }
        }
        linear_god.setOnClickListener {
            data?.let { bean ->
                if (!TextUtils.isEmpty(bean.vote_id) && bean.vote_option == "3")
                    if (type == "1") deleteLineVote(bean, iv_god)
                    else deleteDubVote(bean, linearOperate, 2)
                else
                    if (type == "1") artVoteLine(bean, iv_god, 3)
                    else artVote(bean, linearOperate, 3)
            }
        }
        tv_alarm_word.setOnClickListener {
            data?.let {
                startActivity(Intent(this, WordingVoiceActivity::class.java)
                        .putExtra("isDubbed", if (it.isSelf) 1 else 0)
                        .putExtra("id", if (type == "1") it.id else it.line_id)
                        .putExtra("lineContent", it.line_content))
            }
        }
        relative_Report.setOnClickListener {
            data?.let { bean ->
                DialogAlarmEdit(this).setProperty(bean.is_anonymous).setOnClickListener(View.OnClickListener {
                    when (it.id) {
                        R.id.tv_Delete ->
                            if (type == "1") {
                                DialogDeleteWording(this).setOnClickListener(View.OnClickListener {
                                    deleteLine(bean)
                                }).show()
                            } else
                                DialogDeleteChatComment(this).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                    deleteDub(bean)
                                }).show()
                        R.id.tv_Self -> if (type == "1") {
                            editLine(bean)
                        } else editDub(bean)
                    }
                }).show()
            }
        }
        iv_Privacy.setOnClickListener {
            //下载 判断本地是否有缓存
            data?.let { bean ->
                if (!bean.checkExist().isDownload) {
                    if (!bean.isDownCached)
                        downloadLog(bean.id)
                    downFile(bean, "${bean.from_user_info.nick_name} ${AppTools.getSuffix(bean.dubbing_url)}") {
                        //下载成功
                        iv_Privacy.visibility = View.GONE
                        tv_download.visibility = View.VISIBLE
                    }
                }
            }
        }
        tv_download.setOnClickListener {
            data?.let { bean ->
                if (bean.checkExist().isDownload)
                    DialogDeleteLocalAlarm(this).setOnClickListener(View.OnClickListener {
                        bean.deleteFile {
                            iv_Privacy.visibility = View.VISIBLE
                            tv_download.visibility = View.GONE
                        }
                    }).show()
            }
        }
        tv_dubbing_count.setOnClickListener {
            data?.let { bean ->
                if (bean.dubbing_num > "0")
                    startActivity(Intent(this@AlarmResponseActivity, WordingVoiceActivity::class.java)
                            .putExtra("isDubbed", bean.is_dubbed)
                            .putExtra("id", bean.id)
                            .putExtra("lineContent", bean.line_content))
                else {
                    startActivity(Intent(this@AlarmResponseActivity, RecordVoiceActivity::class.java)
                            .putExtra("wording", bean.line_content)
                            .putExtra("wordingId", bean.id)
                            .putExtra("resourceType", "22")
                            .putExtra("recordType", 6))
                    overridePendingTransition(0, 0)
                }
            }

        }
        voiceProgress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {//停止
                data?.let {
                    progressHandler.stop()
                    it.isPlaying = false
                    it.pasuePosition = audioPlayer.currentPosition.toInt()
                    voiceProgress.finish()
                    audioPlayer.stop()
                    return@setOnClickListener
                }
            }
            down()
        }
        val viewSeekProgress = voiceProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!viewSeekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        data?.let {
                            it.allDuration = audioPlayer.duration
                            progressHandler.stop()
                            audioPlayer.stop()
                            it.isPlaying = false
                            voiceProgress.finish()
                        }
                    }
                }
            }

            override fun endTrack(progress: Float) {
                data?.let {
                    it.pasuePosition = (progress * it.allDuration).toInt()
                }
                down()
            }
        })
        voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            //停止之后继续播放
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                voiceProgress.finish()
                progressHandler.stop()
                data?.let {
                    it.isPlaying = false
                    it.pasuePosition = 0
                }
            }
            down()
        }
        roundImg.setOnClickListener {
            data?.let { bean ->
                if (bean.is_anonymous == "0") {
                    startActivity<UserDetailsActivity>("id" to bean.user.id)
                }
            }
        }
    }

    private fun down() {
        voiceProgress.findViewById<View>(R.id.play).isSelected = true
        data?.let { bean ->
            downPlay(bean.dubbing_url) {it, _ ->
                audioPlayer.setDataSource(it)
                audioPlayer.bean = data
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            }
        }
    }

    private fun changeSelected() {
        iv_angel.isSelected = false
        iv_monster.isSelected = false
        iv_god.isSelected = false
    }

    private fun showInfo(bean: BaseAlarmBean) {
        if (type == "2") {
            iv_Privacy.visibility = if (bean.isDownload) View.GONE else View.VISIBLE
            tv_download.visibility = if (bean.isDownload) View.VISIBLE else View.GONE
        }
        if (bean.is_anonymous == "1") {
            Glide.with(roundImg)
                    .load(R.mipmap.icon_user_default)
                    .into(roundImg)
            tv_UserName.text = resources.getString(R.string.string_alarm_anonymous)
            iv_user_type.visibility = View.GONE
        } else {
            glideUtil.loadGlide(bean.user.avatar_url, roundImg, 0, glideUtil.getLastModified(bean.user.avatar_url))
            tv_UserName.text = bean.user.nick_name
            iv_user_type.visibility = if (bean.user.identity_type == 0) View.GONE else View.VISIBLE
            iv_user_type.isSelected = bean.user.identity_type == 1
        }
        iv_more.isSelected = true
        try {
            tvTime.text = TimeUtils.getInstance().paserFriends(this@AlarmResponseActivity, bean.created_at.toInt())
        } catch (e: Exception) {
        }
        if (type == "1") {
            tv_dubbing_count.text = if (bean.dubbing_num > "0") "${bean.dubbing_num}" else resources.getString(R.string.string_alarm_wording_dubbing)
            tv_alarm_word.text = "#${bean.tag_name}#" + bean.line_content
        } else {
            tv_alarm_word.text = "#${bean.line.tag_name}#" + bean.line.line_content
            tv_dubbing_count.text = if (bean.line.dubbing_num > "0") "${bean.line.dubbing_num}" else resources.getString(R.string.string_alarm_wording_dubbing)
        }
        showTitle(bean)
        changeSelected()
        if (!TextUtils.isEmpty(bean.vote_id))
            when (bean.vote_option) {
                "1" -> iv_angel.isSelected = true
                "2" -> iv_monster.isSelected = true
                "3" -> iv_god.isSelected = true
            }
    }

    @SuppressLint("SetTextI18n")
    private fun showTitle(bean: BaseAlarmBean) {
        tv_angel.text = "您是天使" + if (bean.vote_option_one > 0) " ${bean.vote_option_one}" else ""
        tv_monster.text = "您是恶魔" + if (bean.vote_option_two > 0) " ${bean.vote_option_two}" else ""
        tv_god.text = "您是神" + if (bean.vote_option_three > 0) " ${bean.vote_option_three}" else ""
    }

    /*删除台词投票*/
    private fun deleteLineVote(bean: BaseAlarmBean, ivAngel: ImageView) {
        transLayout.showContent()
        OkClientHelper.delete(this, "linesVote/${bean.vote_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(bean.vote_id)) {
                        when (bean.vote_option) {
                            "1" -> bean.vote_option_one--
                            "2" -> bean.vote_option_two--
                            "3" -> bean.vote_option_three--
                        }
                    }
                    bean.vote_id = null
                    SmallBang.attach2Window(this@AlarmResponseActivity).bang(ivAngel, 60f, null)
                    showTitle(bean)
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    /*臺詞投票*/
    private fun artVoteLine(bean: BaseAlarmBean, ivAngel: ImageView, lineType: Int) {
        transLayout.showProgress()
        OkClientHelper.post(this, "linesVote", FormBody.Builder()
                .add("lineId", bean.id)
                .add("voteOption", "$lineType").build(), IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(bean.vote_id)) {
                        when (bean.vote_option) {
                            "1" -> bean.vote_option_one--
                            "2" -> bean.vote_option_two--
                            "3" -> bean.vote_option_three--
                        }
                    }
                    bean.vote_id = result.data.id.toString()
                    when (lineType) {
                        1 -> bean.vote_option_one += 1
                        2 -> bean.vote_option_two += 1
                        3 -> bean.vote_option_three += 1
                    }
                    bean.vote_option = type.toString()
                    SmallBang.attach2Window(this@AlarmResponseActivity).bang(ivAngel, 60f, null)
                    showTitle(bean)
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    /*删除台词*/
    private fun deleteLine(bean: BaseAlarmBean) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "lines/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                if ((result as BaseRepData).code == 0) {
                    finish()
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    /*编辑台词*/
    private fun editLine(bean: BaseAlarmBean) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "lines/${bean.id}", FormBody.Builder().add("isAnonymous", if (bean.is_anonymous == "1") "0" else "1").build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.is_anonymous = if (bean.is_anonymous == "1") "0" else "1"
                    showInfo(bean)
                } else showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    //删除投票
    private fun deleteDubVote(item: BaseAlarmBean, view: View, voteType: Int) {
        deleteVote(item, transLayout, view, voteType) { _, bean ->
            showTitle(bean)
        }
    }

    /*
     * 删除投票
     */
    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        artVote(bean, transLayout, view, type) { _, item ->
            showTitle(item)
        }
    }

    //编辑可见性
    private fun editDub(bean: BaseAlarmBean) {
        editDub(bean, transLayout) {
            showInfo(it)
        }
    }

    private fun deleteDub(bean: BaseAlarmBean) {
        deleteDub(bean, transLayout) {
            showInfo(it)
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询台词
                OkClientHelper.get(this, "lines/${id}", SingleAlarmData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SingleAlarmData
                        if (result.code == 0) {
                            result.data?.let { bean ->
                                data = bean.apply {
                                    user = BaseUserBean().apply {
                                        avatar_url = userInfo.data.avatar_url
                                        nick_name = userInfo.data.nick_name
                                        id = userInfo.data.user_id
                                        identity_type = userInfo.data.identity_type
                                    }
                                }
                                showInfo(data!!)
                            }
                        } else {
                            transLayout.showOffline()
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            1 -> {
                OkClientHelper.get(this, "dubbings/${id}", SingleAlarmData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SingleAlarmData
                        if (result.code == 0) {
                            result.data?.let { bean ->
                                data = bean.checkUser(userInfo).checkExist()
                                data!!.user = data!!.from_user_info
                                voiceProgress.data = data
                                showInfo(data!!)
                            }
                        } else {
                            transLayout.showOffline()
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.3")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        progressHandler.stop()
        progressHandler.removeCallbacks(progressHandler)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                progressHandler.stop()
            }
        }
    }
}