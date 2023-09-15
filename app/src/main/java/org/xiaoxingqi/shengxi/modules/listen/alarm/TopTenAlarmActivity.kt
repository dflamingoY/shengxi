package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_swipe_recycler.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException

/**
 * 配音Top10
 */
class TopTenAlarmActivity : BaseAct() {

    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private lateinit var userInfo: UserInfoData
    private var playBean: BaseAlarmBean? = null
    private lateinit var audioPlayer: AudioPlayer
    private var voteOption: String = ""
    private var uid: String? = null
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
        return R.layout.activity_swipe_recycler
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        tv_Title.text = when (intent.getIntExtra("type", 1)) {
            2 -> {
                voteOption = "month"
                "本月"
            }
            3 -> {
                voteOption = "day"
                "今日"
            }
            4 -> {
                voteOption = "week"
                "本周"
            }
            else -> {
                voteOption = "all"
                ""
            }
        } + when (intent.getStringExtra("voteOption")) {
            "1" -> "天使"
            "2" -> "恶魔"
            "3" -> "神"
            else -> "天使"
        } + when (intent.getIntExtra("type", 1)) {
            2 -> "Top 10"
            3 -> "Top 3"
            4 -> "Top 5"
            else -> "Top 10"
        }
    }

    override fun initData() {
        uid = intent.getStringExtra("id")
        Log.d("Mozator", "id:$uid")
        audioPlayer = AudioPlayer(this)
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<BaseAlarmBean>(this, R.layout.item_alarm_dub, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
//                helper!!.getView(R.id.tv_dubbing_count).visibility = View.GONE
                item!!.show(helper!!, this@TopTenAlarmActivity, glideUtil) { exceptionShow(helper) }
                helper.getView(R.id.iv_more).isSelected = item.isSelf
                (helper.getView(R.id.voiceProgress) as VoiceProgress).apply {
                    data = item
                    /*findViewById<View>(R.id.viewSeekProgress).*/setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = audioPlayer.currentPosition.toInt()
                                audioPlayer.stop()
                                finish()
                                return@setOnClickListener
                            } else {
                                audioPlayer.stop()
                                progressHandler.stop()
                            }
                        }
                        playBean?.let {
                            if (item !== it) {
                                it.isPlaying = false
                                it.pasuePosition = 0
                            }
                        }
                        download(helper, item)
                    }
                    val viewSeekProgress = findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
                    viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
                        override fun startTrack() {
                            if (!viewSeekProgress.isPressed) {
                                if (audioPlayer.isPlaying) {
                                    item.allDuration = audioPlayer.duration
                                    progressHandler.stop()
                                    audioPlayer.stop()
                                    item.isPlaying = false
                                    finish()
                                }
                            }
                        }

                        override fun endTrack(progress: Float) {
                            item.pasuePosition = (progress * item.allDuration).toInt()
                            download(helper, item)
                        }
                    })
                    findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                        sendObserver()
                        if (audioPlayer.isPlaying) {
                            if (item.isPlaying) {//当前正在播放
                                audioPlayer.stop()
                                progressHandler.stop()
                                item.isPlaying = false
                                item.pasuePosition = 1
                                finish()
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
                showOperatorTitle(helper.getView(R.id.linearOperate), item)
                helper.getView(R.id.tv_alarm_word).setOnClickListener {
                    startActivity(Intent(this@TopTenAlarmActivity, WordingVoiceActivity::class.java)
                            .putExtra("userInfo", item.to_user_info)
                            .putExtra("id", item.line_id)
                            .putExtra("userInfo", item.to_user_info)
                            .putExtra("dubbingNum", item.line.dubbing_num)
                            .putExtra("tagName", item.line.tag_name)
                            .putExtra("lineContent", item.line.line_content))
                }
                helper.getView(R.id.linear_angel).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "1")
                        deleteVote(item, helper.getView(R.id.linearOperate), 1)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 1)
                }
                helper.getView(R.id.linear_monster).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "2")
                        deleteVote(item, helper.getView(R.id.linearOperate), 2)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 2)
                }
                helper.getView(R.id.linear_god).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "3")
                        deleteVote(item, helper.getView(R.id.linearOperate), 3)
                    else
                        artVote(item, helper.getView(R.id.linearOperate), 3)
                }
                helper.getView(R.id.relative_Report).setOnClickListener {
                    if (item.isSelf) {
                        //删除或者只为匿名
                        DialogAlarmEdit(this@TopTenAlarmActivity).setProperty(item.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> DialogDeleteChatComment(this@TopTenAlarmActivity).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                    deleteDub(item)
                                }).show()
                                R.id.tv_Self -> editDub(item)
                            }
                        }).show()
                    } else {
                        if (IConstant.userAdminArray.contains(userInfo.data.user_id)) {
                            AdminReportAlarmDialog(this@TopTenAlarmActivity).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(this@TopTenAlarmActivity).show { reportType ->
                                            reportNormalItem(item.id, reportType, "8")
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(this@TopTenAlarmActivity).setOperator("deleteDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_hide -> {
                                        dialogPwd = DialogCommitPwd(this@TopTenAlarmActivity).setOperator("hideDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminHide(item, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_anonymous_user -> {
                                        startActivity(Intent(this@TopTenAlarmActivity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                                    }
                                    R.id.tvPick -> {
                                        if (item.picked_at != 0) {
                                            DialogDeleteWording(this@TopTenAlarmActivity).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                                dialogPwd = DialogCommitPwd(this@TopTenAlarmActivity).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                                    adminPick(item, FormBody.Builder().add("confirmPasswd", pwd).add("pickedAt", "${System.currentTimeMillis() / 1000}").build())
                                                })
                                                dialogPwd?.show()
                                            }).show()
                                        } else {
                                            checkPickStatus(item)
                                        }
                                    }
                                }
                            }).show()
                        } else
                            DialogNormalReport(this@TopTenAlarmActivity).show {
                                reportNormalItem(item.id, it, "8")
                            }
                    }
                }
                helper.getView(R.id.iv_Privacy).setOnClickListener {
                    //下载 判断本地是否有缓存
                    if (!item.checkExist().isDownload) {
                        if (!item.isDownCached)
                            downloadLog(item.id)
                        downFile(item, "${item.from_user_info.nick_name} ${AppTools.getSuffix(item.dubbing_url)}") {
                            //下载成功
                            helper.getView(R.id.iv_Privacy).visibility = View.GONE
                            helper.getView(R.id.tv_download).visibility = View.VISIBLE
                            EventBus.getDefault().post(AlarmUpdateEvent(5, 5).apply {
                                dubbingId = item.id
                            })
                        }
                    }
                }
                helper.getView(R.id.tv_download).setOnClickListener {
                    if (item.checkExist().isDownload)
                        DialogDeleteLocalAlarm(this@TopTenAlarmActivity).setOnClickListener(View.OnClickListener {
                            item.deleteFile {
                                helper.getView(R.id.iv_Privacy).visibility = View.VISIBLE
                                helper.getView(R.id.tv_download).visibility = View.GONE
                                EventBus.getDefault().post(AlarmUpdateEvent(3, 3).apply {
                                    deletePath = "${item.from_user_info.nick_name} ${AppTools.getSuffix(item.dubbing_url)}"
                                })
                            }
                        }).show()
                }

                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun exceptionShow(helper: BaseAdapterHelper) {
                Glide.with(helper.getImageView(R.id.roundImg))
                        .load(R.mipmap.icon_user_default)
                        .into(helper.getImageView(R.id.roundImg))
                helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_alarm_anonymous)
                helper.getView(R.id.iv_user_type).visibility = View.GONE
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

            private fun download(helper: BaseAdapterHelper, item: BaseAlarmBean) {
                try {
                    if (TextUtils.isEmpty(item.dubbing_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                        return
                    }
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    downPlay(item.dubbing_url) {it, _ ->
                        audioPlayer.setDataSource(it)
                        audioPlayer.bean = item
                        audioPlayer.start(if (SPUtils.getBoolean(this@TopTenAlarmActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }

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
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        recyclerReview.layoutManager = LinearLayoutManager(this)
        recyclerReview.adapter = adapter
        request(0)
    }

    //根据榜单类型展示所属的数据
    @SuppressLint("SetTextI18n")
    private fun showOperatorTitle(view: View, bean: BaseAlarmBean) {
        try {
            view.findViewById<TextView>(R.id.tv_angel).text = "您是天使" + when (voteOption) {
                "day" -> if (bean.vote_option_one_d > 0) " ${bean.vote_option_one_d}" else ""
                "week" -> if (bean.vote_option_one_w > 0) " ${bean.vote_option_one_w}" else ""
                "month" -> if (bean.vote_option_one_m > 0) " ${bean.vote_option_one_m}" else ""
                else -> if (bean.vote_option_one > 0) " ${bean.vote_option_one}" else ""
            }
            view.findViewById<TextView>(R.id.tv_monster).text = "您是恶魔" + when (voteOption) {
                "day" -> if (bean.vote_option_two_d > 0) " ${bean.vote_option_two_d}" else ""
                "week" -> if (bean.vote_option_two_w > 0) " ${bean.vote_option_two_w}" else ""
                "month" -> if (bean.vote_option_two_m > 0) " ${bean.vote_option_two_m}" else ""
                else -> if (bean.vote_option_two > 0) " ${bean.vote_option_two}" else ""
            }
            view.findViewById<TextView>(R.id.tv_god).text = "您是神" + when (voteOption) {
                "day" -> if (bean.vote_option_three_d > 0) " ${bean.vote_option_three_d}" else ""
                "week" -> if (bean.vote_option_three_w > 0) " ${bean.vote_option_three_w}" else ""
                "month" -> if (bean.vote_option_three_m > 0) " ${bean.vote_option_three_m}" else ""
                else -> if (bean.vote_option_three > 0) " ${bean.vote_option_three}" else ""
            }
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        swipeRefresh.setOnRefreshListener {
            if (audioPlayer != null && audioPlayer.isPlaying)
                audioPlayer.stop()
            request(0)
        }
    }

    private fun adminDelete(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.delete(this, "admin/dubbings/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
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

    private fun adminHide(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(this, "admin/dubbings/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
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

    private fun deleteDub(bean: BaseAlarmBean) {
        deleteDub(bean, transLayout) {
            val position = mData.indexOf(it)
            mData.remove(it)
            adapter.notifyItemRemoved(position)
            EventBus.getDefault().post(AlarmUpdateEvent(4, 4).apply {
                dubbingId = it.id
                deletePath = it.line_id
            })
        }
    }

    /**
     * 编辑可见性
     */
    private fun editDub(bean: BaseAlarmBean) {
        editDub(bean, transLayout) {
            adapter.notifyItemChanged(mData.indexOf(it))
            EventBus.getDefault().post(AlarmUpdateEvent(6, 6).apply {
                dubbingId = it.id
                deletePath = it.is_anonymous
            })
        }
    }

    //投票
    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        artVote(bean, transLayout, view, type) { childView, item ->
            //对应榜单的票做加
            when (type) {
                1 -> {
                    if (voteOption != "all") {
                        item.vote_option_one_d++
                        item.vote_option_one_w++
                        item.vote_option_one_m++
                    }
                }
                2 -> {
                    if (voteOption != "all") {
                        item.vote_option_two_d++
                        item.vote_option_two_w++
                        item.vote_option_two_m++
                    }
                }
                3 -> {
                    if (voteOption != "all") {
                        item.vote_option_three_d++
                        item.vote_option_three_w++
                        item.vote_option_three_m++
                    }
                }
            }
            showOperatorTitle(childView, item)
            EventBus.getDefault().post(AlarmUpdateEvent(7, type).apply {
                dubbingId = item.id
                deletePath = item.vote_id
            })
        }
    }

    //删除投票
    private fun deleteVote(item: BaseAlarmBean, view: View, voteType: Int) {
        deleteVote(item, transLayout, view, voteType) { childView, bean ->
            when (voteType) {
                1 -> {
                    if (voteOption != "all") {
                        item.vote_option_one_d--
                        item.vote_option_one_w--
                        item.vote_option_one_m--
                    }
                }
                2 -> {
                    if (voteOption != "all") {
                        item.vote_option_two_d--
                        item.vote_option_two_w--
                        item.vote_option_two_m--
                    }
                }
                3 -> {
                    if (voteOption != "all") {
                        item.vote_option_three_d--
                        item.vote_option_three_w--
                        item.vote_option_three_m--
                    }
                }
            }

            showOperatorTitle(childView, bean)
            EventBus.getDefault().post(AlarmUpdateEvent(8, 8).apply {
                dubbingId = item.id
            })
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "leaderboard/dubbings/${intent.getStringExtra("voteOption")}/top?type=$voteOption&toUserId=$uid",
                WordingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                result as WordingData
                if (result.code == 0) {
                    mData.clear()
                    result.data?.forEach {
                        mData.add(it.checkUser(userInfo).checkExist())
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
            }
        }, "V4.3")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
        } catch (e: Exception) {
        }
        progressHandler.removeCallbacks(progressHandler)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
        }
    }

}