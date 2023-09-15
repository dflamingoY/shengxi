package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.frag_dubbing.view.*
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
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException

class DubbingUserFragment : BaseFragment(), IAlarmTabCall {

    private var isCurrent = false
    private var tabType = 0
    override fun tabSelected(position: Int) {
        isCurrent = position == 1
    }

    override fun itemTabSelected(page: Int, isRequest: Boolean, tabType: Int) {
        isCurrent = page == 1
        if (this.tabType == tabType) {
            return
        }
        if (!isRequest || isCurrent) {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
            playBean = null
            this.tabType = tabType
            lastId = ""
            tagId = if (tabType == 0) "" else "$tabType"
            request(0)
        }
    }

    override fun tabClick(isVisible: Boolean) {
    }

    override fun doubleClickRefresh() {

    }

    private var tagId: String = ""
    private var lastId = ""
    private lateinit var userInfo: UserInfoData
    private var playBean: BaseAlarmBean? = null
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private val audioPlayer: AudioPlayer by lazy {
        AudioPlayer(activity)
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

    override fun getLayoutId(): Int {
        return R.layout.frag_dubbing
    }

    override fun initView(view: View?) {
        view!!.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<BaseAlarmBean>(context, R.layout.item_alarm_dub, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
//                helper!!.getView(R.id.tv_dubbing_count).visibility = View.GONE
                item!!.show(helper!!, activity!!, glideUtil) { exceptionShow(helper) }
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
                            .putExtra("tagName", item.line.tag_name)
                            .putExtra("dubbingNum",item.line.dubbing_num)
                            .putExtra("userInfo", item.to_user_info)
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
        mView!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore, mView!!.recyclerView, false))
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
            activity!!.downPlay(item.dubbing_url) {it, _ ->
                audioPlayer.setDataSource(it)
                audioPlayer.bean = item
                audioPlayer.start(if (SPUtils.getBoolean(context, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            }
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
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
                if (SPUtils.getBoolean(activity, IConstant.PLAY_MENU_AUTO, false)) {
                    playBean?.let {
                        var index = mData.indexOf(it) + 1
                        if (index >= mData.size) index = 0
                        if (index == 0) mView!!.recyclerView.scrollToPosition(0)
                        down(mData[index], index != 0)
                        if (mData.indexOf(it) == mData.size - 2) {
                            request(1)
                        }
                    }
                } else {
                    mView!!.customPlayMenu.isSelected = false
                }
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
                        var index = mData.indexOf(it) + 1
                        /*if (index >= mData.size) {
                            index = 0
                            mView!!.recyclerView.scrollToPosition(0)
                        }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
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

        mView!!.swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
            playBean = null
            lastId = ""
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
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
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "dubbings?toUserId=${loginBean.user_id}&lastId=$lastId&tagId=$tagId", WordingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as WordingData
                if (result.code == 0) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
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
                    if (result.data == null || result.data.size == 0) {
                        if (TextUtils.isEmpty(lastId)) {
                            mData.clear()
                            adapter.notifyDataSetChanged()
                        }
                    }
                    if (result.data != null && result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    if (mData.size > 0)
                        lastId = mData[mData.size - 1].id
                }
                if (mData.size == 0) {
                    mView!!.transLayout.showEmpty()
                } else {
                    mView!!.transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: AlarmUpdateEvent) {
        if (event.type == 1 && event.updateResource == 1) {
            //更新数据
            lastId = ""
            request(0)
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