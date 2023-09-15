package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.frag_world_coordinlayout.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.EditSendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.download.DownVoiceProxy
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.IOException

class WorldFragment : BaseFragment(), HomeTabClick {
    private lateinit var adapter: QuickAdapter<BaseBean>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var transLayout: TransLayout
    private var playBean: BaseBean? = null
    private var lastShare: String = ""
    private var isVisiblePage = true
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private lateinit var audioPlayer: AudioPlayer
    private var isFirst = true
    private var currentIndex = 0
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSensorEvent(event: SensorChangeEvent) {
        /**
         * 发生变化
         */
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
                    audioPlayer.seekTo(it.pasuePosition)
                }
            }
        } else if (event.type == 2) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                    audioPlayer.seekTo(it.pasuePosition)
                }
            }
        }
    }

    override fun currentPage(page: Int) {
        isVisiblePage = (page == currentIndex)
        try {
            if (!isVisiblePage) {
                if (refreshLayout.isRefreshing) {
                    refreshLayout.isRefreshing = false
                }
            }
            if (isVisiblePage && isFirst) {
                isFirst = false
                pageNo = 1
                requestTop()
//                request(0)
            }
        } catch (e: Exception) {

        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_world_coordinlayout
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        refreshLayout = view.swipeRefresh
        transLayout = view.transLayout
        recyclerView.layoutManager = LinearLayoutManager(activity)
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators),
                ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    override fun initData() {
        val login = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        try {
            if (SPUtils.getString(activity, IConstant.TAB_HOME_INDEX + login.user_id, "0") == "1") {
                isVisiblePage = false
                currentIndex = 1
            } else {
                isFirst = false
            }
        } catch (e: Exception) {
        }
        audioPlayer = AudioPlayer(activity)
        setOperator(object : ItemOperator {
            override fun onAdminPrivacy(bean: BaseBean?) {
                val indexOf = mData.indexOf(bean)
                mData.remove(bean)
                adapter.notifyItemRemoved(indexOf)
                dialogPwd?.dismiss()
            }

            override fun onAdminFail() {
                dialogPwd?.setCallBack()
            }

            override fun onPrivacy(bean: BaseBean?) {
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                transLayout.showContent()
            }

            override fun onFriend() {

            }

            override fun onReport(type: String) {
                transLayout.showContent()
                DialogPBBlack(activity!!).setType(type).show()
            }

            override fun onDelete(bean: BaseBean?) {
                showToast("删除成功")
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onRecommend(bean: BaseBean?) {

            }

            override fun onUnRecommend(bean: BaseBean?) {
            }

            override fun onthumb(bean: BaseBean?) {
                adapter.notifyHeart(bean)
                transLayout.showContent()
            }

            override fun onUnThumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
                transLayout.showContent()
            }

            override fun onComment(from_id: String?) {
                commentBean?.let {
                    it.dialog_num = 1
                    it.chat_id = from_id
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
                transLayout.showContent()
            }
        })
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_voice, mData) {
            var cache: MutableList<BaseAdapterHelper> = java.util.ArrayList()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                val statusText = helper!!.getView(R.id.linearStatusText) as LinearStatusText
                statusText.visibility = View.VISIBLE
                item!!.show(helper, activity!!, glideUtil) {
                    true
                }
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                if (item.isTop) {
                    helper.getView(R.id.ivOfficialFlag).visibility = View.VISIBLE
                    helper.getView(R.id.relative_Operate).visibility = View.GONE
                    helper.getView(R.id.tvTime).visibility = View.GONE
                } else {
                    helper.getView(R.id.tvTime).visibility = View.VISIBLE
                    helper.getView(R.id.ivOfficialFlag).visibility = View.GONE
                    helper.getView(R.id.relative_Operate).visibility = View.VISIBLE
                }
                helper.getTextView(R.id.tvTime)?.text = resources.getString(R.string.string_home_hint_2) + TimeUtils.getInstance().paserWorl(activity, item.shared_at)
                if (!TextUtils.isEmpty(item.is_shared)) {//是自己
                    statusText.visibility = View.GONE
                    helper.getView(R.id.heartView).visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                } else {
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    statusText.isSelected = item.friend_status == 0
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("url", item.user.avatar_url).putExtra("id", item.user.id))
                }
                (helper.getView(R.id.imageGroup) as ImageGroupView).setOnClickViewListener {
                    startActivity(Intent(activity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    activity!!.overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
                helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id)
                                .putExtra("uid", item.user.id)
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num == 0) {
                            queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        } else {
                            startActivity(Intent(activity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id)
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user_id.toString())
                            )
                        }
                    }
                }
                statusText.setOnClickListener {
                    if (statusText.isSelected)
                        requestFriends(item, it)
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb))
                    } else {                //共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
                            item.isNetStatus = true
//                            transLayout.showProgress()
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            transLayout.showProgress()
                            LightUtils.addItem(item.voice_id)
                            tuhmb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        activity!!.share(item) {
                            when (it) {
                                1 -> {
                                    activity!!.delete(item) { delBean ->
                                        queryVoicesStatus(delBean.created_at)
                                        showToast("删除成功")
                                        mData.remove(delBean)
                                        adapter.notifyDataSetChanged()
                                        transLayout.showContent()
                                        if (mData.size == 0) {
                                            transLayout.showEmpty()
                                        }
                                        activity!!.about()
                                    }
                                }
                                2 -> {
                                    transLayout.showProgress()
                                    setVoicePrivacy(item)
                                }
                                4 -> {//取消置顶
                                    editTopVoice(item)
                                }
                                5 -> {
                                    startActivity<EditSendAct>("data" to item)
                                }
                            }
                        }
                    else
                        DialogReport(this@WorldFragment.activity!!).isTopEnable(true, item.isTop).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogReportContent(activity!!)
                                            .setOnResultListener(OnReportItemListener { report ->
                                                transLayout.showProgress()
                                                report(item.voice_id, report, item.user.id)
                                            }).show()
                                }
                                R.id.tv_report_normal -> {
                                    activity!!.reportNormal { type ->
                                        reportNormalItem(item.voice_id, type)
                                    }
                                }
                                R.id.tv_Follow -> {
                                    if (item.subscription_id == 0) {
                                        addSubscriber(item)
                                    } else {
                                        activity!!.deleteSubscriber(item) {
                                            deletedSubscriber(item)
                                        }
                                    }
                                }
                                R.id.tvTop -> {//置顶
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("topVoice", if (item.isTop) "0" else (System.currentTimeMillis() / 1000).toInt().toString()).setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminVoiceTop(FormBody.Builder().add("topAt", value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                            }
                        }).show()
                }
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                voiceProgress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            voiceProgress.finish()
                            return@setOnClickListener
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    playBean?.let {
                        if (item !== it) {
                            it.isPlaying = false
                            it.isPause = false
                            it.pasuePosition = 0
                        }
                    }
                    download(helper, item)
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                voiceProgress.finish()
                            }
                        }
                    }

                    override fun endTrack(progress: Float) {
                        /**
                         * 滑动停止
                         */
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(helper, item)
                    }
                })
                voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            voiceProgress.finish()
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    item.pasuePosition = 0
                    download(helper, item)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun animStatus(isStart: Boolean) {
                if (isStart) {
                    if (anim == null) {
                        anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1500)
                        anim!!.interpolator = LinearInterpolator()
                        anim!!.repeatCount = ValueAnimator.INFINITE
                        anim!!.addUpdateListener {
                            val value = it.animatedValue as Float
                            if (audioPlayer.isPlaying) {
                                cache.forEach { helper ->
                                    (helper.getView(R.id.heartView) as HeartWaveView).waveShiftRatio = value
                                }
                            }
                        }
                    }
                    anim!!.start()
                } else {
                    cache.forEach { helper ->
                        (helper.getView(R.id.heartView) as HeartWaveView).end()
                    }
                    anim?.let {
                        it.cancel()
                        anim = null
                    }
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                        (helper.getView(R.id.heartView) as HeartWaveView).waterLevelRatio = currentPosition / audioPlayer.duration.toFloat()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun notifyHelperStatus(bean: BaseBean) {
                if (!TextUtils.isEmpty(bean.is_shared))
                    if (playBean == bean) {
                        //更新当前播放的次数++ 正在播放的item
                        cache.loop { baseAda ->
                            (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                        }?.let { ada ->
                            ada.getTextView(R.id.tv_Sub).text = if (bean.played_num == 0) context.resources.getString(R.string.string_Listener) else context.resources.getString(R.string.string_Listener) + " ${bean.played_num}"
                        }
                    }
            }

            override fun notifyHeart(bean: BaseBean?) {
                bean?.let { b ->
                    cache.loop { baseAda ->
                        (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                    }?.let { helper ->
                        if (TextUtils.isEmpty(b.is_shared)) {
                            helper.getView(R.id.iv_Thumb).let {
                                it.isSelected = b.is_collected == 1
                                if (recyclerView.isOffsetScreen(mData.indexOf(bean))) {
                                    SmallBang.attach2Window(activity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_transparent, recyclerView, false))
        if (isVisiblePage) {
            pageNo = 1
            requestTop()
//            request(0)
        }
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(context, mData.indexOf(item))
        }
        try {
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            DownVoiceProxy.downProxy.clearFocus()
            playBean = item
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downProxy(activity, item, {
                    audioPlayer.setDataSource(it)
                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                }, {
                    //跳过这一首, 直接下一曲
//                    showToast(VolleyErrorHelper.getMessage(it))
                    if (!item.isReDown) {
                        item.isReDown = true
                        down(item, false)
                    } else
                        nextVoice()
                })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //下一曲语音
    private fun nextVoice() {
        playBean?.let {
            if (SPUtils.getBoolean(context, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerView.smoothScrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    request(0)
                }
            } else {
                mView!!.customPlayMenu.isSelected = false
            }
        }
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                mView!!.customPlayMenu.isSelected = true
                playBean?.let {
                    if (TextUtils.isEmpty(it.is_shared) && LightUtils.contains(it.voice_id)) {
                        adapter.animStatus(true)
                    }
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    addPlays(it, null) { bean ->
                        adapter.notifyHelperStatus(bean)
                    }
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                        it.is_collected = 1
                        tuhmb(it, null, 5)
                    }
                }
                adapter.animStatus(false)
                progressHandler.stop()
                try {
                    nextVoice()
                } catch (e: Exception) {
                }
            }

            override fun onInterrupt() {
                adapter.animStatus(false)
                mView!!.customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                }
                progressHandler.stop()
            }
        }
        mView!!.customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = mData.indexOf(it) + 1
                        if (index >= mData.size) {
                            return@let
                        }
                        if (mData.indexOf(it) == mData.size - 2) {//加载下一页, 重复加载问题
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
                        recyclerView.scroll(context, mData.indexOf(it), false)
                    }
            }
        })
        refreshLayout.setOnRefreshListener {
            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            lastShare = ""
            playBean?.let {
                if (it.isPlaying) {
                    if (audioPlayer.isPlaying) {
                        progressHandler.stop()
                        audioPlayer.stop()
                        it.isPlaying = false
                    }
                }
            }
            playBean = null
            mData.clear()
            adapter.notifyDataSetChanged()
            pageNo = 1
            requestTop()
//            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }

    }

    //当前正在请求的 lastId的 key 值 绑定不允许多次重复请求
    private var currentLastKey: String? = null

    private fun adminVoiceTop(formBody: FormBody, bean: BaseBean) {
        OkClientHelper.patch(activity, "admin/voices/${bean.voice_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作已执行")
                    if (bean.isTop) {
                        mData.loop {
                            bean.voice_id == it.voice_id && it.isTop
                        }?.let {
                            mData.remove(it)
                            adapter.notifyDataSetChanged()
                        }
                    }
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

    private fun editTopVoice(item: BaseBean) {
        OkClientHelper.patch(activity, "users/${item.user_id}/voices/${item.voice_id}", FormBody.Builder().add("topAt", "0").build(),
                BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                } else
                    showToast(result.msg)
            }
        })
    }

    private var pageNo = 1

    //请求置顶的数据
    private fun requestTop() {
        refreshLayout.isRefreshing = true
        OkClientHelper.get(activity, "voices/top?pageNo=$pageNo", VoiceData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as VoiceData

                if (result.data?.let {
                            it.forEach { bean ->
                                bean.isTop = true
                                mData.add(bean)
                            }
                            adapter.notifyDataSetChanged()
                            if (it.size >= 10) {
                                pageNo++
                                requestTop()
                            } else {
                                request(0)
                            }
                            it
                        } == null) {
                    request(0)
                }
                /* if (mData.size > 0) {
                     transLayout.showContent()
                 }
                 adapter.notifyDataSetChanged()*/
            }
        }, "V4.3")
    }

    //对加载id 进行绑定
    override fun request(flag: Int) {
        if (currentLastKey == lastShare) {
            return
        }
        currentLastKey = lastShare
        OkClientHelper.get(activity, "voices/type/2?lastId=$lastShare&recognition=1", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.code == 0 && result.data != null) {
                    if (TextUtils.isEmpty(lastShare)) {
                        if (audioPlayer.isPlaying) {
                            progressHandler.stop()
                            audioPlayer.stop()
                        }
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (item in result.data) {
                            mData.add(item)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    try {
                        lastShare = mData[mData.size - 1].share_id
                    } catch (e: Exception) {
                    }
                } else {
                    if (TextUtils.isEmpty(lastShare)) {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                refreshLayout.isRefreshing = false
                currentLastKey = null
            }

            override fun onFailure(any: Any?) {
                refreshLayout.isRefreshing = false
                if (AppTools.isNetOk(activity)) {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    showToast(any.toString())
                } else {
                    if (mData.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    } else {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                        transLayout.showOffline()
                    }
                }
                currentLastKey = null
            }
        })
    }

    private fun requestFriends(bean: BaseBean, view: View) {
        transLayout.showProgress()
        val infoData = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("toUserId", bean.user_id.toString())
                .build()
        OkClientHelper.post(activity, "users/${infoData.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.friend_status = 1
                    view.isSelected = false
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + infoData.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + infoData.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                transLayout.showProgress()
                                addWhiteBlack(bean.user_id.toString())
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else
                        showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("HomeWorld : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            }
        }
    }

    /**
     * 用户更新类型之后 立刻刷新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateIdentity(event: UpdateIdentityEvent) {
        if (mData.size > 0) {
            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            mData.forEach {
                if (loginBean.user_id == it.user_id.toString()) {
                    it.user.identity_type = event.type
                    val indexOf = mData.indexOf(it)
                    adapter.notifyItemChanged(indexOf)
                }
            }
        }
    }

    /**
     * 用户共享之后, 及时更新数据到本地
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shareData(event: IShareWorldData) {
        if (null != event.bean) {
            //共享时间需要更新为最新时间
            event.bean.shared_at = (System.currentTimeMillis() / 1000).toInt()
            mData.add(0, event.bean)
            adapter.notifyItemInserted(0)
            recyclerView.scrollToPosition(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateEvent(update: UpdateSendVoice) {
        if (update.type == 1) {
            lastShare = ""
            request(0)
        } else if (update.type == 3) {
            if (isVisiblePage) {
                lastShare = ""
                recyclerView.scrollToPosition(0)
                refreshLayout.isRefreshing = true
                request(0)
            }
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHomeTab(event: HomeTabChangeEvent) {
        currentIndex = if (event.index == "0") 0 else 1
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisiblePage || !isResumed) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            commentBean?.let {
                if (it.voice_id == event.voiceId) {
                    /**
                     * 更新数据
                     */
                    it.chat_id = event.chatId
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (it.user_id.toString() == loginBean.user_id) {
                        it.chat_num++
                    } else {
                        it.dialog_num++
                    }
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun topicChangeEvent(event: ChangeTopicInfoEvent) {
        mData.loop {
            event.voiceId == it.voice_id
        }?.let {
            try {
                it.topic_id = event.topicId.toInt()
                it.topic_name = event.topicName
                adapter.notifyItemChanged(mData.indexOf(it))
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}