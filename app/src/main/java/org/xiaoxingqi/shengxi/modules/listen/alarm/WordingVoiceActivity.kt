package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_wording_voices.*
import kotlinx.android.synthetic.main.activity_wording_voices.recyclerView
import kotlinx.android.synthetic.main.activity_wording_voices.swipeRefresh
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException

/**
 * 台词配音单页
 */
class WordingVoiceActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private var lineId: String? = null
    private var lastId: Int = 1
    private var lineContent: String? = null
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var userInfo: UserInfoData
    private var isDubbed = 0//是否已经配音
    private var jumpPosition = -1//跳转到用户发过的记录
    private var isAutoScroll = false
    private var wordUser: BaseUserBean? = null
    private var tagName = "求配音"
    private var dubbingNum: String? = null
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }
    private var playBean: BaseAlarmBean? = null

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
        return R.layout.activity_wording_voices
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    override fun initData() {
        dubbingNum = intent.getStringExtra("dubbingNum")
        if (!TextUtils.isEmpty(dubbingNum)) {
            tv_Title.text = "配音X$dubbingNum"
        }
        lineId = intent.getStringExtra("id")
        wordUser = intent.getParcelableExtra("userInfo")
        //wordUser 可能为空, 判断touserId是否有值, 查询并展示
        val toUserId = intent.getStringExtra("toUserId")
        if (wordUser?.let {
                    ivUserType.visibility = if (it.identity_type == 0) View.GONE else View.VISIBLE
                    ivUserType.isSelected = it.identity_type == 1
                    glideUtil.loadGlide(it.avatar_url, ivAvatar, 0, glideUtil.getLastModified(it.avatar_url))
                    it
                } == null) {
            if (!TextUtils.isEmpty(toUserId)) {
                userInfo(toUserId)
            } else {//根据台词id查询台词信息
                queryLineInfo()
            }
        }
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        isDubbed = intent.getIntExtra("isDubbed", 0)
        if (isDubbed == 1) {
            tv_CurrentStatus.text = resources.getString(R.string.string_jump_to_user_dubbing)
        }
        audioPlayer = AudioPlayer(this)
        intent.getStringExtra("tagName")?.let {
            tagName = it
        }
        lineContent = intent.getStringExtra("lineContent")
        if (!TextUtils.isEmpty(lineContent)) {
            tvWording.text = "#$tagName#$lineContent"
        }
        adapter = object : QuickAdapter<BaseAlarmBean>(this, R.layout.item_alarm_dub, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
                helper!!.getView(R.id.relativeWord).visibility = View.GONE
                helper.getView(R.id.tv_dubbing_count).visibility = View.GONE
                item!!.show(helper, this@WordingVoiceActivity, glideUtil) { exceptionShow(helper) }
                helper.getView(R.id.iv_more).isSelected = item.isSelf
                (helper.getView(R.id.voiceProgress) as VoiceProgress).apply {
                    data = item
                    /* findViewById<View>(R.id.viewSeekProgress).*/setOnClickListener {
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
                        DialogAlarmEdit(this@WordingVoiceActivity).setProperty(item.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> DialogDeleteChatComment(this@WordingVoiceActivity).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                    deleteDub(item)
                                }).show()
                                R.id.tv_Self -> editDub(item)
                            }
                        }).show()
                    } else {
                        if (IConstant.userAdminArray.contains(userInfo.data.user_id)) {
                            AdminReportAlarmDialog(this@WordingVoiceActivity).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(this@WordingVoiceActivity).show { reportType ->
                                            reportNormalItem(item.id, reportType, "8")
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(this@WordingVoiceActivity).setOperator("deleteDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_hide -> {
                                        dialogPwd = DialogCommitPwd(this@WordingVoiceActivity).setOperator("hideDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminHide(item, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_anonymous_user -> {
                                        startActivity(Intent(this@WordingVoiceActivity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                                    }
                                    R.id.tvPick -> {
                                        if (item.picked_at != 0) {
                                            DialogDeleteWording(this@WordingVoiceActivity).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                                dialogPwd = DialogCommitPwd(this@WordingVoiceActivity).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
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
                            DialogNormalReport(this@WordingVoiceActivity).show {
                                reportNormalItem(item.id, it, "8")
                            }
                    }
                }
                helper.getView(R.id.iv_Privacy).setOnClickListener {
                    //下载判断本地是否有缓存
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
                        DialogDeleteLocalAlarm(this@WordingVoiceActivity).setOnClickListener(View.OnClickListener {
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
                helper.getView(R.id.roundImg).setOnClickListener(null)
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
                    downPlay(item.dubbing_url) { it, _ ->
                        audioPlayer.setDataSource(it)
                        audioPlayer.bean = item
                        audioPlayer.start(if (SPUtils.getBoolean(this@WordingVoiceActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recyclerView, false))
        request(0)
    }

    @SuppressLint("SetTextI18n")
    private fun showOperatorTitle(view: View, bean: BaseAlarmBean) {
        try {
            view.findViewById<TextView>(R.id.tv_angel).text = "您是天使" + if (bean.vote_option_one > 0) " ${bean.vote_option_one}" else ""
            view.findViewById<TextView>(R.id.tv_monster).text = "您是恶魔" + if (bean.vote_option_two > 0) " ${bean.vote_option_two}" else ""
            view.findViewById<TextView>(R.id.tv_god).text = "您是神" + if (bean.vote_option_three > 0) " ${bean.vote_option_three}" else ""
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_CurrentStatus.setOnClickListener {
            //跳转录制界面
            if (isDubbed == 1) {
                if (jumpPosition != -1) {
                    scrollToPosition(jumpPosition)
                } else {
                    isAutoScroll = true
                    request(0)
                }
            } else {
                startActivity(Intent(this, RecordVoiceActivity::class.java)
                        .putExtra("wording", lineContent)
                        .putExtra("wordingId", lineId)
                        .putExtra("tagId", tagName)
                        .putExtra("resourceType", "22")
                        .putExtra("recordType", 6))
                overridePendingTransition(0, 0)
            }
        }
        swipeRefresh.setOnRefreshListener {
            if (audioPlayer != null && audioPlayer.isPlaying)
                audioPlayer.stop()
            lastId = 1
            request(0)
        }
        adapter.setOnLoadListener {
            lastId++
            request(0)
        }
        linearUser.setOnClickListener {
            wordUser?.let {
                startActivity<UserDetailsActivity>("id" to it.id)
            }
        }
    }

    /**
     * 滚动到某一个位置
     */
    private fun scrollToPosition(position: Int) {
        isAutoScroll = false
        val scroller = TopSmoothScroller(this)
        scroller.targetPosition = position
        recyclerView.layoutManager?.startSmoothScroll(scroller)
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
            //变换tag
            tv_CurrentStatus.text = resources.getString(R.string.string_alarm_20)
            isDubbed = 0
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

    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        artVote(bean, transLayout, view, type) { childView, item ->
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
            showOperatorTitle(childView, bean)
            EventBus.getDefault().post(AlarmUpdateEvent(8, 8).apply {
                dubbingId = item.id
            })
        }
    }

    private fun queryLineInfo() {
        OkClientHelper.get(this, "lines/${lineId}", SingleAlarmData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SingleAlarmData
                if (result.code == 0) {
                    result.data?.let { bean ->
                        userInfo(bean.user_id)
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.2")
    }

    private fun userInfo(uid: String) {
        OkClientHelper.get(this, "users/$uid", UserInfoData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as UserInfoData
                if (result.code == 0)
                    result.data?.let { info ->
                        wordUser = BaseUserBean().apply {
                            id = info.user_id
                            avatar_url = info.avatar_url
                            identity_type = info.identity_type
                        }
                        ivUserType.visibility = if (info.identity_type == 0) View.GONE else View.VISIBLE
                        ivUserType.isSelected = info.identity_type == 1
                        glideUtil.loadGlide(info.avatar_url, ivAvatar, 0, glideUtil.getLastModified(info.avatar_url))
                    }
            }
        })
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "lines/$lineId/dubbings?pageNo=$lastId", WordingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                result as WordingData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.code == 0) {
                    if (lastId == 1) {
                        mData.clear()
                        try {
                            result.data?.forEach {
                                if (TextUtils.isEmpty(lineContent)) {
                                    lineContent = it.line.line_content
                                    tvWording.text = "#${it.line.tag_name}#" + lineContent
                                }
                                mData.add(it.checkUser(userInfo).checkExist())
                                if (isDubbed == 1) {//需要查询数据
                                    if (jumpPosition == -1) {
                                        if (it.from_user_id == userInfo.data.user_id) {
                                            jumpPosition = mData.size - 1
                                        }
                                    }
                                }
                                if (it.isSelf) {
                                    jumpPosition = mData.size - 1
                                    tv_CurrentStatus.text = resources.getString(R.string.string_jump_to_user_dubbing)
                                    isDubbed = 1
                                }
                            }
                        } catch (e: Exception) {
                        }
                        adapter.notifyDataSetChanged()
                        //展示本台词的用户信息

                    } else {
                        result.data?.forEach {
                            mData.add(it.checkUser(userInfo).checkExist())
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                            if (isDubbed == 1) {//需要查询数据
                                if (jumpPosition == -1) {
                                    if (it.from_user_id == userInfo.data.user_id) {
                                        jumpPosition = mData.size - 1
                                        if (isAutoScroll) {
                                            scrollToPosition(jumpPosition)
                                        }
                                    }
                                }
                            }
                            if (it.isSelf) {
                                jumpPosition = mData.size - 1
                                tv_CurrentStatus.text = resources.getString(R.string.string_jump_to_user_dubbing)
                                isDubbed = 1
                            }
                        }
                    }
                    if (result.data != null && result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        if (isAutoScroll) {
                            request(0)
                        }
                    }
                }
                if (dubbingNum == null)
                    tv_Title.text = "配音${if (mData.size == 0) "" else "X" + mData.size}"
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                swipeRefresh.isRefreshing = false
                transLayout.showContent()
            }
        }, "V4.3")
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        try {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
        } catch (e: Exception) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AlarmUpdateEvent) {
        if (event.updateResource == 1 && event.type == 1) {
            isDubbed = 1
            tv_CurrentStatus.text = resources.getString(R.string.string_jump_to_user_dubbing)
            lastId = 1
            request(0)
        }
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