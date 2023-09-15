package org.xiaoxingqi.shengxi.modules.listen.alarm

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
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.SingleAlarmData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

//今日最佳台词 配音 pick   数组 展示第一条
class TodayBestWorkActivity : BaseAct() {
    private var operatorType = 0//0 台词 1 配音  3 声昔君pick

    private var type: String? = null//1 台词 2 配音
    private val audioPlayer by lazy { AudioPlayer(this) }
    private var data: BaseAlarmBean? = null
    private var dubbingId: String? = null
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            try {
                val currentPosition = audioPlayer.currentPosition.toInt()
                voiceProgress.updateProgress(currentPosition)
            }catch (e:Exception){

            }
        }
    }
    private var userInfo: UserInfoData? = null
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
        dubbingId = intent.getStringExtra("id")
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        operatorType = intent.getIntExtra("type", 0)
        when (operatorType) {
            0 -> {
                type = "1"
                voiceProgress.visibility = View.GONE
                linearOperate.visibility = View.GONE
                tv_Title.text = "今日最佳台词"
                request(0)
            }
            1 -> {
                type = "2"
                tv_Title.text = "今日最佳配音"
                request(1)
            }
            2 -> {
                type = "2"
                tv_Title.text = "声昔君Pick"
                operatorType = 1
                //查询台词pick状态
                request(2)
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
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
                        .putExtra("userInfo", if (operatorType == 0) it.user else it.to_user_info)
                        .putExtra("toUserId", it.to_user_id)
                        .putExtra("tagName", it.tag_name)
                        .putExtra("dubbingNum", if (operatorType == 0) it.dubbing_num else it.line.dubbing_num)
                        .putExtra("isDubbed", if (it.isSelf) 1 else 0)
                        .putExtra("id", if (type == "1") it.id else it.line_id)
                        .putExtra("lineContent", it.line_content))
            }
        }
        relative_Report.setOnClickListener {
            data?.let { bean ->
                if (bean.isSelf)
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
                            R.id.tv_Self -> editDub(bean)
                        }
                    }).show()
                else {
                    if (IConstant.userAdminArray.contains(userInfo!!.data.user_id)) {
                        AdminReportAlarmDialog(this@TodayBestWorkActivity).setAnonymous(bean.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Report -> {
                                    DialogNormalReport(this@TodayBestWorkActivity).show { reportType ->
                                        reportNormalItem(bean.id, reportType, "8")
                                    }
                                }
                                R.id.tv_deleteDubbing -> {
                                    dialogPwd = DialogCommitPwd(this@TodayBestWorkActivity).setOperator("deleteDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminDelete(bean, FormBody.Builder().add("confirmPasswd", pwd).build())
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_hide -> {
                                    dialogPwd = DialogCommitPwd(this@TodayBestWorkActivity).setOperator("hideDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminHide(bean, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_anonymous_user -> {
                                    startActivity(Intent(this@TodayBestWorkActivity, UserDetailsActivity::class.java).putExtra("id", bean.from_user_id))
                                }
                                R.id.tvPick -> {
                                    if (bean.picked_at != 0) {
                                        DialogDeleteWording(this).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                            dialogPwd = DialogCommitPwd(this).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                                adminPick(bean, FormBody.Builder().add("confirmPasswd", pwd).add("pickedAt", "${System.currentTimeMillis() / 1000}").build())
                                            })
                                            dialogPwd?.show()
                                        }).show()
                                    } else {
                                        checkPickStatus(bean)
                                    }
                                }
                            }
                        }).show()
                    } else {
                        DialogNormalReport(this@TodayBestWorkActivity).show {
                            reportNormalItem(bean.id, it, "8")
                        }
                    }
                }
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
                    startActivity(Intent(this@TodayBestWorkActivity, WordingVoiceActivity::class.java)
                            .putExtra("userInfo", if (operatorType == 0) bean.user else bean.to_user_info)
                            .putExtra("toUserId", bean.to_user_id)
                            .putExtra("isDubbed", bean.is_dubbed)
                            .putExtra("tagName", bean.tag_name)
                            .putExtra("id", if (operatorType == 0) bean.id else bean.line_id)
                            .putExtra("dubbingNum", if (operatorType == 0) bean.dubbing_num else bean.line.dubbing_num)
                            .putExtra("lineContent", bean.line_content))
                else {
                    startActivity(Intent(this@TodayBestWorkActivity, RecordVoiceActivity::class.java)
                            .putExtra("wording", bean.line_content)
                            .putExtra("wordingId", bean.id)
                            .putExtra("tagId", if (operatorType == 0) bean.tag_name else bean.line.tag_name)
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
                    if (operatorType == 0)
                        startActivity<UserDetailsActivity>("id" to bean.user.id)
                    else
                        startActivity<UserDetailsActivity>("id" to bean.from_user_info.id)
                }
            }
        }
        //只有台词有此功能
        linearWordDubbingStatus.setOnClickListener {
            data?.let { bean ->
                if (bean.is_dubbed == 1) {//配音详情
                    startActivity(Intent(this@TodayBestWorkActivity, WordingVoiceActivity::class.java)
                            .putExtra("userInfo", bean.user)
                            .putExtra("toUserId", bean.to_user_id)
                            .putExtra("isDubbed", bean.is_dubbed)
                            .putExtra("tagName", bean.tag_name)
                            .putExtra("id", bean.id)
                            .putExtra("dubbingNum", bean.dubbing_num)
                            .putExtra("lineContent", bean.line_content))
                } else {
                    startActivity(Intent(this@TodayBestWorkActivity, RecordVoiceActivity::class.java)
                            .putExtra("wording", bean.line_content)
                            .putExtra("wordingId", bean.id)
                            .putExtra("tagId", if (operatorType == 0) bean.tag_name else bean.line.tag_name)
                            .putExtra("resourceType", "22")
                            .putExtra("recordType", 6))
                    overridePendingTransition(0, 0)
                }
            }
        }
        relative.setOnClickListener {
            data?.let { bean ->
                if (bean.user_id == userInfo!!.data.user_id) {
                    //删除或者只为匿名
                    DialogAlarmEdit(this).hideAnonymous(true).setOnClickListener(View.OnClickListener {
                        when (it.id) {
                            R.id.tv_Delete ->
                                DialogDeleteWording(this).setOnClickListener(View.OnClickListener {
                                    deleteLine(bean)
                                }).show()
                        }
                    }).show()
                } else {
                    //判断是否是adminUser
                    if (IConstant.userAdminArray.contains(userInfo!!.data.user_id)) {
                        AdminReportAlarmDialog(this).setType(1).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Report -> {
                                    DialogNormalReport(this).show { reportType ->
                                        reportNormalItem(bean.id, reportType, "7")
                                    }
                                }
                                R.id.tv_deleteDubbing -> {
                                    dialogPwd = DialogCommitPwd(this).setOperator("deleteWord", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminDelete(bean, FormBody.Builder().add("confirmPasswd", pwd).build())
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_hide -> {
                                    dialogPwd = DialogCommitPwd(this).setOperator("deleteWord", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminHideLine(bean, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                    })
                                    dialogPwd?.show()
                                }
                            }
                        }).show()
                    } else {
                        DialogNormalReport(this).show {
                            reportNormalItem(bean.id, it, "7")
                        }
                    }
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
            glideUtil.loadGlide(bean.from_user_info.avatar_url, roundImg, 0, glideUtil.getLastModified(bean.from_user_info.avatar_url))
            tv_UserName.text = bean.from_user_info.nick_name
            iv_user_type.visibility = if (bean.from_user_info.identity_type == 0) View.GONE else View.VISIBLE
            iv_user_type.isSelected = bean.from_user_info.identity_type == 1
        }
        iv_more.isSelected = bean.isSelf
        tv_alarm_word.text = "#${bean.line.tag_name}#${bean.line.line_content}"
        try {
            tvTime.text = TimeUtils.getInstance().paserFriends(this@TodayBestWorkActivity, bean.created_at.toInt())
        } catch (e: Exception) {
        }
        if (tv_dubbing_count.visibility == View.VISIBLE)
            tv_dubbing_count.apply {
                text = if (bean.line.dubbing_num > "0") "${bean.line.dubbing_num}" else resources.getString(R.string.string_alarm_wording_dubbing)
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

    //展示台词的信息
    private fun showWordInfo(bean: BaseAlarmBean) {
        try {
            tvTime.text = TimeUtils.getInstance().paserFriends(this@TodayBestWorkActivity, bean.created_at.toInt())
        } catch (e: Exception) {
        }
        glideUtil.loadGlide(bean.user.avatar_url, roundImg, 0, glideUtil.getLastModified(bean.user.avatar_url))
        tv_UserName.text = bean.user.nick_name
        iv_user_type.visibility = if (bean.user.identity_type == 0) View.GONE else View.VISIBLE
        iv_user_type.isSelected = bean.user.identity_type == 1
        tv_alarm_word.text = "#${bean.tag_name}#${bean.line_content}"
        relative.visibility = View.VISIBLE
        relative.isSelected = userInfo!!.data.user_id == bean.user_id
        linearWordDubbingStatus.visibility = View.VISIBLE
        linearWordDubbingStatus.isSelected = bean.is_dubbed == 1
        if (tv_dubbing_count.visibility == View.VISIBLE)
            tv_dubbing_count.apply {
                text = if (bean.dubbing_num > "0") "${bean.dubbing_num}" else resources.getString(R.string.string_alarm_wording_dubbing)
            }
    }

    //超管删除台词
    private fun adminHideLine(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(this, "admin/lines/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    dialogPwd?.dismiss()
                    finish()
                } else {
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    private fun adminHide(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(this, "admin/dubbings/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    finish()
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showTitle(bean: BaseAlarmBean) {
        tv_angel.text = "您是天使" + if (bean.vote_option_one > 0) " ${bean.vote_option_one}" else ""
        tv_monster.text = "您是恶魔" + if (bean.vote_option_two > 0) " ${bean.vote_option_two}" else ""
        tv_god.text = "您是神" + if (bean.vote_option_three > 0) " ${bean.vote_option_three}" else ""
    }

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
                    SmallBang.attach2Window(this@TodayBestWorkActivity).bang(ivAngel, 60f, null)
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
                    SmallBang.attach2Window(this@TodayBestWorkActivity).bang(ivAngel, 60f, null)
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
            0 -> {
                //获取今日最佳台词
                executeRequest("leaderboard/lines/optimal?type=day") {
                    try {
                        if (it != null) {
                            it as WordingData
                            it.data?.let { bean ->
                                //展示第一条数据
                                this.data = bean[0]
                                showWordInfo(this.data!!)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }
            1 -> {
                //获取今日最佳配音
                executeRequest("leaderboard/dubbings/optimal?type=day") {
                    try {
                        if (it != null) {
                            it as WordingData
                            it.data?.let { data ->
                                this.data = data[0].checkUser(userInfo!!).checkExist().apply {
                                    dubbing_num = line.dubbing_num
                                    line_id = line.id
                                    tag_name = line.tag_name
                                }
                                showInfo(this.data!!)
                                voiceProgress.data = this.data
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
            }
            2 -> {//查询台词是否正在pick 用户自己的配音
                OkClientHelper.get(this, "dubbings/$dubbingId", SingleAlarmData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as SingleAlarmData
                        data = result.data.checkUser(userInfo!!).checkExist()
                        showInfo(data!!)
                        voiceProgress.data = data
                    }
                }, "V4.3")
                OkClientHelper.get(this, "dubbings/${dubbingId}/pickStatus", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                    }

                    override fun success(result: Any?) {
                        result as IntegerRespData
                        if (result.code == 0) {
                            if (result.data.pick_status == 1) {
                                //展示正在pick
                                ivAlarmPick.visibility = View.VISIBLE
                                tvPickHint.visibility = View.VISIBLE
                            } else if (result.data.pick_status == 2) {
                                tvPickHint.visibility = View.VISIBLE
                                tvPickHint.text = tvPickHint.text.toString().replace("正", "曾")
                            }
                        }
                    }
                }, "V4.3")
            }
        }
    }

    //超管删除台词
    private fun adminDelete(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.delete(this, "admin/lines/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    dialogPwd?.dismiss()
                    finish()
                } else {
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        progressHandler.stop()
        progressHandler.removeCallbacks(progressHandler)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AlarmUpdateEvent) {
        if (event.updateResource == 1 && event.type == 1) {
            data?.let {
                if (event.dubbingId == it.id) {//为当前台词
                    it.is_dubbed = 1
                    it.dubbing_num = (it.dubbing_num.toInt() + 1).toString()
                    tv_dubbing_count.text = it.dubbing_num
                    linearWordDubbingStatus.isSelected = true
                    tvDubbingStatus.text = "已配音"
                }
            }
        }
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