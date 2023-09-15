package org.xiaoxingqi.shengxi.modules.listen.alarm

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
import kotlinx.android.synthetic.main.frag_alarm_leaderboard.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
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
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress

class HotAlarmFragment : BaseFragment(), ITabAlarmClickCall {
    private var pageNo = 1

    //初始化打开首页
    private var isCurrent = true
    override fun tabSelected(position: Int) {
        isCurrent = position == 0
    }

    override fun tabClick(isVisible: Boolean) {
        try {
            if (mView!!.swipeRefresh.isEnabled != isVisible) {
                mView!!.swipeRefresh.isEnabled = isVisible
                mView!!.swipeRefresh.isRefreshing = false
            }
        } catch (e: Exception) {

        }
    }

    override fun doubleClickRefresh() {

    }

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorEvent(event: SensorChangeMoodEvent) {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        if (headsetOn || a2dpOn || scoOn) {
            return
        }
        if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) {
            return
        }
        if (event.type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else if (event.type == 2) {
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

    private var loadMoreView: View? = null
    private lateinit var userInfo: UserInfoData
    private var playBean: BaseAlarmBean? = null
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private val audioPlayer: AudioPlayer by lazy {
        AudioPlayer(activity)
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_alarm_leaderboard
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        activity?.let {
            view.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(it, R.color.colorIndecators), ContextCompat.getColor(it, R.color.colorMovieTextColor),
                    ContextCompat.getColor(it, R.color.color_Text_Black))
        }
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<BaseAlarmBean>(activity, R.layout.item_alarm_dub, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
                item!!.show(helper!!, activity!!, glideUtil) { exceptionShow(helper) }
                helper.getView(R.id.tvTime).visibility = View.GONE
                helper.getView(R.id.ivAlarmPick).visibility = if (item.isPick) View.VISIBLE else View.GONE
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
                helper.getView(R.id.iv_more).isSelected = item.isSelf
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
                helper.getView(R.id.tv_alarm_word).setOnClickListener {
                    startActivity(Intent(activity!!, WordingVoiceActivity::class.java)
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
                        DialogAlarmEdit(activity!!).setProperty(item.is_anonymous).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete ->
                                    DialogDeleteChatComment(activity!!).setHint("确定删除？").setOnClickListener(View.OnClickListener {
                                        deleteDub(item)
                                    }).show()
                                R.id.tv_Self -> editDub(item)
                            }
                        }).show()
                    } else {
                        if (IConstant.userAdminArray.contains(userInfo.data.user_id)) {
                            AdminReportAlarmDialog(activity!!).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(activity!!).show { reportType ->
                                            reportNormalItem(item.id, reportType, 8)
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(activity!!).setOperator("deleteDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_hide -> {
                                        dialogPwd = DialogCommitPwd(activity!!).setOperator("hideDubbing", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminHide(item, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_anonymous_user -> {
                                        startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.from_user_id))
                                    }
                                    R.id.tvPick -> {
                                        if (item.picked_at != 0) {
                                            DialogDeleteWording(activity!!).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                                dialogPwd = DialogCommitPwd(activity!!).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
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
                            DialogNormalReport(activity!!).show {
                                reportNormalItem(item.id, it, 8)
                            }
                    }
                }
                helper.getView(R.id.iv_Privacy).setOnClickListener {
                    //下载 判断本地是否有缓存
                    if (!item.checkExist().isDownload) {
                        if (!item.isDownCached)
                            activity!!.downloadLog(item.id)
                        activity!!.downFile(item, "${item.from_user_info.nick_name} ${AppTools.getSuffix(item.dubbing_url)}") {
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
                        DialogDeleteLocalAlarm(activity!!).setOnClickListener(View.OnClickListener {
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
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
            }

            private fun exceptionShow(helper: BaseAdapterHelper) {
                Glide.with(helper.getImageView(R.id.roundImg))
                        .load(R.mipmap.icon_user_default)
                        .into(helper.getImageView(R.id.roundImg))
                helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_alarm_anonymous)
                helper.getView(R.id.roundImg).setOnClickListener(null)
                helper.getView(R.id.iv_user_type).visibility = View.GONE
            }
        }
        mView!!.recyclerView.adapter = adapter
        loadMoreView = LayoutInflater.from(activity).inflate(R.layout.loadmore_hot_alarm, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMoreView)
        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
        request(0)
    }

    override fun initEvent() {
        loadMoreView?.setOnClickListener {
            (activity as AlarmListActivity).changeCurrentPage(1, true)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            pageNo = 1
            request(0)
        }

        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                mView!!.customPlayMenu.isSelected = true
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
                nextAudio()
            }

            override fun onInterrupt() {
                mView!!.customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                }
                adapter.changeStatue(false)
                progressHandler.stop()
            }
        }
        mView!!.customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = mData.indexOf(it) + 1
                        /*if (index >= mData.size) {
                            index = 0
                            mView!!.recyclerView.scrollToPosition(0)
                        }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            pageNo++
                            request(1)
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
                    mView!!.recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        mView!!.recyclerView.scroll(activity, mData.indexOf(it), false)
                    }
            }
        })
        adapter.setOnLoadListener {
            pageNo++
            request(1)
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

    private fun nextAudio() {
        try {
            if (SPUtils.getBoolean(activity, IConstant.PLAY_MENU_AUTO, false)) {
                playBean?.let {
                    var index = mData.indexOf(it) + 1
                    if (index >= mData.size) index = 0
                    if (index == 0) mView!!.recyclerView.scrollToPosition(0)
                    down(mData[index], index != 0)
                    if (mData.indexOf(it) == mData.size - 2) {
                        pageNo++
                        request(1)
                    }
                }
            } else {
                mView!!.customPlayMenu.isSelected = false
            }
        } catch (e: Exception) {
        }
    }

    private fun down(item: BaseAlarmBean, isScroll: Boolean = true) {
        if (TextUtils.isEmpty(item.dubbing_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        try {
            if (isScroll && mView!!.recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                mView!!.recyclerView.scroll(activity, mData.indexOf(item))
            }
            playBean = item
            activity!!.downPlay(item.dubbing_url) { it, type ->
                if (type == 0) {
                    audioPlayer.setDataSource(it)
                    audioPlayer.bean = item
                    audioPlayer.start(if (SPUtils.getBoolean(context, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                } else {
                    if (!item.isReDown) {
                        item.isReDown = true
                        down(item, false)
                    } else
                        nextAudio()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun adminDelete(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.delete(activity, "admin/dubbings/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
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
        OkClientHelper.patch(activity, "admin/dubbings/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
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

    //编辑可见性
    private fun editDub(bean: BaseAlarmBean) {
        activity!!.editDub(bean, mView!!.transLayout) {
            adapter.notifyItemChanged(mData.indexOf(it))
        }
    }

    private fun deleteDub(bean: BaseAlarmBean) {
        activity!!.deleteDub(bean, mView!!.transLayout) {
            val position = mData.indexOf(it)
            mData.remove(it)
            adapter.notifyItemRemoved(position)
            EventBus.getDefault().post(AlarmUpdateEvent(4, 4).apply {
                dubbingId = it.id
                deletePath = it.line_id
            })
        }
    }

    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        activity!!.artVote(bean, mView!!.transLayout, view, type) { childView, item ->
            showOperatorTitle(childView, item)
            EventBus.getDefault().post(AlarmUpdateEvent(7, type).apply {
                dubbingId = item.id
                deletePath = item.vote_id
            })
        }
    }

    //删除投票
    private fun deleteVote(item: BaseAlarmBean, view: View, voteType: Int) {
        activity!!.deleteVote(item, mView!!.transLayout, view, voteType) { childView, bean ->
            showOperatorTitle(childView, bean)
            EventBus.getDefault().post(AlarmUpdateEvent(8, 8).apply {
                dubbingId = item.id
            })
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "dubbings/top", WordingData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }

                    override fun success(result: Any?) {
                        result as WordingData
                        mData.clear()
                        adapter.notifyDataSetChanged()
                        result.data?.let {
                            it.forEach {
                                it.isPick = true
                                mData.add(0, it.checkUser(userInfo).checkExist())
                            }
                            adapter.notifyDataSetChanged()
                        }
                        request(1)
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(activity, "dubbings/hot?pageNo=$pageNo", WordingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        result as WordingData
                        if (result.data?.let {
                                    if (pageNo == 1) {//刷新数据 重新到入
                                        result.data?.forEach {
                                            mData.add(it.checkUser(userInfo).checkExist())
                                        }
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        result.data?.forEach {
                                            mData.add(it.checkUser(userInfo).checkExist())
                                            adapter.notifyItemInserted(adapter.itemCount - 1)
                                        }
                                    }
                                    if (it.size >= 10) {
                                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                    } else {
                                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                                    }
                                    it
                                } == null)
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }, "V4.3")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: AlarmUpdateEvent) {
        if (event.type == 1 && event.updateResource == 1) {
            //更新数据
        } else if (event.type == 4 && event.updateResource == 4) {
            //主动删除配音
            if (!isCurrent || !isResumed)
                mData.loop {
                    it.id == event.dubbingId
                }?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
        } else if (event.type == 3 && event.updateResource == 3) {
            if (!isCurrent || !isResumed)
                mData.loop {
                    event.deletePath == "${it.from_user_info.nick_name} ${AppTools.getSuffix(it.dubbing_url)}"
                }?.let {
                    it.isDownload = false
                    adapter.notifyDataSetChanged()
                }
        } else if (event.type == 5) {
            if (!isCurrent || !isResumed) {
                if (!TextUtils.isEmpty(event.dubbingId))
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.isDownload = true
                        adapter.notifyDataSetChanged()
                    }
            }
        } else if (event.type == 6) {
            if (!isCurrent || !isResumed) {
                if (!TextUtils.isEmpty(event.dubbingId))
                    mData.loop {
                        event.dubbingId == it.id
                    }?.let {
                        it.is_anonymous = event.deletePath
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
            }
        } else if (event.updateResource == 7) {
            if (!isCurrent || !isResumed)
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
            if (!isCurrent || !isResumed)
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
        if (!isCurrent || !isResumed) {
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
        try {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
        } catch (e: Exception) {
        }
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}