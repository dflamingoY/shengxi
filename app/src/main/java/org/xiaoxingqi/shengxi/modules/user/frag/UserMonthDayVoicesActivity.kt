package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_hot_movies.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.ImageGroupView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceListItemView
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException

class UserMonthDayVoicesActivity : BaseAct() {
    private var currentDay: String? = null
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }
    private val audioPlayer: AudioPlayer by lazy { AudioPlayer(this) }
    private var playBean: BaseBean? = null
    private var lastId: String? = ""
    override fun getLayoutId(): Int {
        return R.layout.activity_hot_movies
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

    override fun initView() {
        swipeRefresh.isEnabled = false
    }

    override fun initData() {
        currentDay = intent.getStringExtra("date")
        try {
            if (!TextUtils.isEmpty(currentDay)) {
                tv_Title.text = TimeUtils.getCharMonth(currentDay!!.substring(4, 6).toInt().toString()) + ".${currentDay!!.substring(6, 8)} ${currentDay!!.substring(0, 4)}"
            }
        } catch (e: Exception) {
        }
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_user_home, mData) {
            var cache = ArrayList<BaseAdapterHelper>()
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                helper!!.getView(R.id.tv_BottomView).visibility = View.GONE
                helper.getView(R.id.voiceProgress).visibility = View.VISIBLE
                val progress = helper.getView(R.id.voiceProgress) as VoiceProgress
                progress.data = item
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item!!.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tv_Action).visibility = if (TextUtils.isEmpty(item.topic_name)) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_Privacy).visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
                val imageGroupView = helper.getView(R.id.imageGroup) as ImageGroupView
                imageGroupView.setData(item.img_list)
                val paserTime = TimeUtils.getInstance().formatterTime(this@UserMonthDayVoicesActivity, item.created_at)
                try {
                    val split = paserTime.split("_")
                    helper.getTextView(R.id.tv_FirstTime).text = split[0]
                    helper.getTextView(R.id.tv_SecondTime).text = split[1]
                } catch (e: Exception) {
                }
                imageGroupView.setOnClickViewListener {
                    startActivity(Intent(this@UserMonthDayVoicesActivity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
                if (item.resource == null) {
                    helper.getView(R.id.voiceItemView).visibility = View.GONE
                }
                item.resource?.let {
                    if (!TextUtils.isEmpty(it.id)) {
                        helper.getView(R.id.voiceItemView).visibility = View.VISIBLE
                        (helper.getView(R.id.voiceItemView) as VoiceListItemView).setData(it, item.resource_type, item.user_score)
                    } else {
                        helper.getView(R.id.voiceItemView).visibility = View.GONE
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    item.topic_name?.let {
                        startActivity(Intent(this@UserMonthDayVoicesActivity, TopicResultActivity::class.java)
                                .putExtra("tagId", item.topic_id.toString())
                                .putExtra("tag", item.topic_name))
                    }
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                progress.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPause = true
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()//暂停 记录需要暂停的位置
                            progress.finish()
                            return@setOnClickListener
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    playBean?.let {
                        if (item != it) {
                            it.isPlaying = false
                            it.isPause = false
                            it.pasuePosition = 0
                        }
                    }
                    download(helper, item)
                }
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                progress.finish()
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

                progress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    //播放状态直接停止
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            progress.finish()
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


            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                try {
                    if (TextUtils.isEmpty(item.voice_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    val file = getDownFilePath(item.voice_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@UserMonthDayVoicesActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@UserMonthDayVoicesActivity, item.voice_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@UserMonthDayVoicesActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            addPlays(item, helper.getTextView(R.id.tv_Sub)){}
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }

                        override fun onCompletion() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
                            item.isPause = false
                            progressHandler.stop()
                        }

                        override fun onInterrupt() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
                            item.isPause = false
                            progressHandler.stop()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                super.changeStatue(isSelect)
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemClickListener { _, position ->
            if (null != mData[position].voice_id)
                startActivity(Intent(this, DynamicDetailsActivity::class.java)
                        .putExtra("uid", mData[position].user_id.toString())
                        .putExtra("id", mData[position].voice_id)
                )
        }
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/voices/calendar/$currentDay?lastId=$lastId", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as VoiceData
                if (result.code == 0) {
                    result.data?.let {
                        mData.addAll(it)
                        adapter.notifyDataSetChanged()
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            lastId = it[it.size - 1].voice_id
                        }
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V4.2")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         *
         * 如果是app 退到后台, 则需要考虑摒弃此条件
         */
        if (!isVisibleActivity || !ActivityLifecycleHelper.isAppPause) {
            playBean?.let {
                if (it.isPlaying || it.isPause) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    progressHandler.stop()
                    adapter.notifyDataSetChanged()
                }
            }
            audioPlayer.stop()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: OperatorVoiceListEvent) {
        if (event.type == 3) {
            if (mData.size > 0) {
                mData.loop {
                    it.voice_id == event.voice_id
                }?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
                if (mData.size == 0) {
                    finish()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoiceEvent(event: EventVoiceBean) {
        if (event.type == 3) {
            mData.loop { it.voice_id == event.voiceId }?.let {
                it.is_private = event.isPrivacy
                adapter.notifyItemChanged(mData.indexOf(it))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }
}