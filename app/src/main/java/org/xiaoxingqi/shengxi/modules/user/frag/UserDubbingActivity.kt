package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_user_dubbing.*
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
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.*
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress

//用户的可查看配音
class UserDubbingActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private var userId: String? = null
    private var tagId = ""
    private var lastId = ""
    private lateinit var userInfo: UserInfoData

    private var playBean: BaseAlarmBean? = null
    private val audioPlayer: AudioPlayer by lazy {
        AudioPlayer(this)
    }
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
        return R.layout.activity_user_dubbing
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {

        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        userId = intent.getStringExtra("uid")
        if (userId == obj.user_id) {
            tv_Title.text = "我的配音"
        } else {
            tv_Title.text = "ta的配音"
        }
        userInfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<BaseAlarmBean>(this, R.layout.item_alarm_dub, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
                item!!.show(helper!!, this@UserDubbingActivity, glideUtil) { exceptionShow(helper) }
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
                    helper.getView(R.id.iv_more).isSelected = item.isSelf
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
                helper.getView(R.id.relativeWord).setOnClickListener {
                    startActivity(Intent(this@UserDubbingActivity, WordingVoiceActivity::class.java)
                            .putExtra("isDubbed", if (item.isSelf) 1 else 0)
                            .putExtra("id", item.line_id)
                            .putExtra("dubbingNum", item.line.dubbing_num)
                            .putExtra("userInfo", item.to_user_info)
                            .putExtra("tagName", item.line.tag_name)
                            .putExtra("lineContent", item.line.line_content))
                }
                helper.getView(R.id.relative_Report).setOnClickListener {
                    if (item.isSelf) {
                        //删除或者只为匿名
                        DialogAlarmEdit(this@UserDubbingActivity).setProperty(item.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> DialogDeleteChatComment(this@UserDubbingActivity).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                    deleteDub(item)
                                }).show()
                                R.id.tv_Self -> editDub(item)
                            }
                        }).show()
                    } else {
                        if (IConstant.userAdminArray.contains(userInfo.data.user_id)) {
                            AdminReportAlarmDialog(this@UserDubbingActivity).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(this@UserDubbingActivity).show { reportType ->
                                            reportNormalItem(item.id, reportType, "8")
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(this@UserDubbingActivity).setOperator("deleteDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_hide -> {
                                        dialogPwd = DialogCommitPwd(this@UserDubbingActivity).setOperator("hideDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminHide(item, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_anonymous_user -> {
                                        startActivity(Intent(this@UserDubbingActivity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                                    }
                                }
                            }).show()
                        } else
                            DialogNormalReport(this@UserDubbingActivity).show {
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
                        DialogDeleteLocalAlarm(this@UserDubbingActivity).setOnClickListener(View.OnClickListener {
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

            private fun download(helper: BaseAdapterHelper, item: BaseAlarmBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
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

            private fun exceptionShow(helper: BaseAdapterHelper) {
                Glide.with(helper.getImageView(R.id.roundImg))
                        .load(R.mipmap.icon_user_default)
                        .into(helper.getImageView(R.id.roundImg))
                helper.getTextView(R.id.tv_UserName).text = "${resources.getString(R.string.string_alarm_anonymous)}(仅自己可见)"
                helper.getView(R.id.roundImg).setOnClickListener(null)
                helper.getView(R.id.iv_user_type).visibility = View.GONE
            }
        }
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recyclerView, false))
        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
        recyclerView.adapter = adapter
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        headItemClickView.setOnItemClick(object : OnAlarmItemClickListener {
            override fun itemClick(type: Int) {
                tagId = if (type == 0) "" else type.toString()
                lastId = ""
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                playBean = null
                request(0)
            }
        })
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            playBean = null
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                customPlayMenu.isSelected = true
                playBean?.let {
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                }
                adapter.changeStatue(false)
                progressHandler.stop()
                //next
                if (SPUtils.getBoolean(this@UserDubbingActivity, IConstant.PLAY_MENU_AUTO, false)) {
                    playBean?.let {
                        var index = mData.indexOf(it) + 1
                        if (index >= mData.size) index = 0
                        if (index == 0) recyclerView.scrollToPosition(0)
                        down(mData[index], index != 0)
                        if (mData.indexOf(it) == mData.size - 2) {
                            request(1)
                        }
                    }
                } else {
                    customPlayMenu.isSelected = false
                }
            }

            override fun onInterrupt() {
                customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                }
                adapter.changeStatue(false)
                progressHandler.stop()
            }
        }

        customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = mData.indexOf(it) + 1
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(0)
                        }
                        down(mData[index], index != 0)
                    }
                } catch (e: Exception) {

                }
            }

            override fun play() {
                if (audioPlayer.isPlaying) {
                    playBean?.let {
                        it.pasuePosition = audioPlayer.currentPosition.toInt()
                    }
                    audioPlayer.stop()
                } else {
                    if (playBean?.let {
                                down(it)
                                it
                            } == null) {
                        //播放0角标
                        if (mData.size > 0) {
                            down(mData[0])
                        }
                    }
                }
            }

            override fun pre() {
                audioPlayer.stop()
                playBean?.let {
                    var index = mData.indexOf(it) - 1
                    if (index < 0) index = 0
                    down(mData[index])
                }
            }

            override fun top() {
                if (!audioPlayer.isPlaying) {
                    recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        recyclerView.scroll(this@UserDubbingActivity, mData.indexOf(it) + 1, false)
                    }
            }
        })
    }

    private fun down(item: BaseAlarmBean, isScroll: Boolean = true) {
        if (TextUtils.isEmpty(item.dubbing_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        try {
            if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                recyclerView.scroll(this, mData.indexOf(item) + 1)
            }
            playBean = item
            downPlay(item.dubbing_url) { it, _ ->
                audioPlayer.setDataSource(it)
                audioPlayer.bean = item
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            }
        } catch (e: Exception) {
        }
    }

    //删除投票
    private fun deleteVote(item: BaseAlarmBean, view: View, voteType: Int) {
        deleteVote(item, null, view, voteType) { childView, bean ->
            showOperatorTitle(childView, bean)
        }
    }

    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        artVote(bean, null, view, type) { childView, item ->
            showOperatorTitle(childView, item)
        }
    }

    private fun deleteDub(bean: BaseAlarmBean) {
        deleteDub(bean, null) {
            val position = mData.indexOf(it)
            mData.remove(it)
            adapter.notifyItemRemoved(position)
            EventBus.getDefault().post(AlarmUpdateEvent(4, 4).apply {
                dubbingId = it.id
                deletePath = it.line_id
            })
        }
    }

    /*编辑可见性*/
    private fun editDub(bean: BaseAlarmBean) {
        editDub(bean, null) {
            adapter.notifyItemChanged(mData.indexOf(it))
        }
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

    override fun request(flag: Int) {
        OkClientHelper.get(this, "users/${userId}/dubbings?lastId=$lastId&tagId=$tagId", WordingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                result as WordingData
                if (result.data?.let {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                result.data?.forEach { bean ->
                                    mData.add(bean.checkUser(userInfo).checkExist())
                                }
                                adapter.notifyDataSetChanged()
                                progressHandler.postDelayed({ recyclerView.scrollToPosition(0) }, 200)
                            } else {
                                result.data?.forEach { bean ->
                                    mData.add(bean.checkUser(userInfo).checkExist())
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                            if (result.data != null && result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            } else {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                            }
                            if (mData.size > 0)
                                lastId = mData[mData.size - 1].id
                            it
                        } == null) {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else
                    transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlarmEvent(event: AlarmUpdateEvent) {
        if (event.type == 3 && event.updateResource == 3) {
            if (!isVisibleActivity)
                if (!TextUtils.isEmpty(event.deletePath)) {
                    mData.loop {
                        event.deletePath == "${it.from_user_info.nick_name} ${AppTools.getSuffix(it.dubbing_url)}"
                    }?.let {
                        it.isDownload = false
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
                }
        } else if (event.type == 4 && event.updateResource == 4) {
            //主动删除配音
            if (!isVisibleActivity)
                mData.loop {
                    it.id == event.dubbingId
                }?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
        } else if (event.type == 5) {
            if (!isVisibleActivity) {
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.isDownload = true
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        } else if (event.type == 6) {
            if (!isVisibleActivity) {
                if (!TextUtils.isEmpty(event.dubbingId))
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.is_anonymous = event.deletePath
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
            }
        } else if (event.updateResource == 7) {
            if (!isVisibleActivity)
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        if (!TextUtils.isEmpty(it.vote_id) && !TextUtils.isEmpty(it.vote_option)) {
                            when (it.vote_option) {
                                "1" -> it.vote_option_one--
                                "2" -> it.vote_option_two--
                                "3" -> it.vote_option_three--
                            }
                        }
                        it.vote_id = event.deletePath
                        it.vote_option = event.type.toString()
                        when (event.type) {
                            1 -> it.vote_option_one++
                            2 -> it.vote_option_two++
                            3 -> it.vote_option_three++
                        }
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
                }
        } else if (event.updateResource == 8) {
            if (!isVisibleActivity)
                if (!TextUtils.isEmpty(event.dubbingId)) {
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.vote_id = ""
                        when (it.vote_option) {
                            "1" -> it.vote_option_one--
                            "2" -> it.vote_option_two--
                            "3" -> it.vote_option_three--
                        }
                        it.vote_option = ""
                        adapter.notifyItemChanged(mData.indexOf(it))
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

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        try {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
        } catch (e: Exception) {
        }
    }
}