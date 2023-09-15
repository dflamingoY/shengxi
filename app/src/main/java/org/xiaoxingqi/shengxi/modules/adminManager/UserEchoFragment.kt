package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.frag_admin_recycler.view.*
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
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogCancelMsg
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.TalkListData
import org.xiaoxingqi.shengxi.model.TestEnjoyCountData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.File
import java.io.IOException

/**
 * 用户首2条回声
 */
class UserEchoFragment : BaseFragment(), ITabClickCall {
    private var visible = true
    override fun tabClick(isVisible: Boolean) {
        if (visible == isVisible) {
            return
        }
        visible = isVisible
    }

    override fun doubleClickRefresh() {

    }

    private var lastId = ""
    private lateinit var adapter: QuickAdapter<TalkListData.TalkListBean>
    private val mData by lazy { ArrayList<TalkListData.TalkListBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: TalkListData.TalkListBean? = null
    private var preReadTag: String = ""
    private var tagBean: TalkListData.TalkListBean? = null
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorAdmin(event: SensorChangeAdminEvent) {
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

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_admin_recycler
    }

    override fun initView(view: View?) {
        view!!.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
        view.recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun initData() {
        audioPlayer = AudioPlayer(activity)
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<TalkListData.TalkListBean>(activity, R.layout.item_admin_voice_list, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: TalkListData.TalkListBean?) {
                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.created_at)
                helper.getTextView(R.id.tv_UserName)?.text = item.nick_name
                val progress = helper.getView(R.id.voiceProgress) as VoiceProgress
                progress.visibility = View.VISIBLE
                helper.getView(R.id.relative_img).visibility = View.GONE
                progress.data = item
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                helper.getView(R.id.linear_tag).visibility = if (item.isReadTag) View.VISIBLE else View.GONE
//                helper.getView(R.id.linear_content).visibility = if (TextUtils.isEmpty(item.resource_content)) View.GONE else View.VISIBLE
                helper.getTextView(R.id.tvContent).text = if (TextUtils.isEmpty(item.recognition_content)) "转文字失败" else item.recognition_content
                progress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
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
                helper.getView(R.id.tv_Sub).setOnClickListener {
                    startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                            .putExtra("id", item.voice_id)
                            .putExtra("uid", item.voice_user_id)
                    )
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java)
                            .putExtra("id", item.user_id))
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: TalkListData.TalkListBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    val file = getDownFilePath(item.resource_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(activity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                                    return@downFile
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, {
                            showToast(VolleyErrorHelper.getMessage(it))
                        })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

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

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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
        }
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(1)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = preReadTag
            request(1)
        }
        adapter.setOnItemLongClickListener { _, position ->
            DialogCancelMsg(activity!!).setTitle("设置审阅到此").setOnClickListener(View.OnClickListener {
                setReadTag(if (position > 0) mData[position - 1] else mData[position], position)
            }).show()
        }
    }

    /**
     * 设置审阅点
     */
    private fun setReadTag(bean: TalkListData.TalkListBean, position: Int) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "admin/${loginBean.user_id}/point", FormBody.Builder().add("pointKey", "chatPoint")
                .add("pointValue", bean.id).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("设置成功")
                    tagBean?.let {
                        it.isReadTag = false
                    }
                    preReadTag = bean.id
                    val indexOf = mData.indexOf(bean)
                    tagBean = if (indexOf + 1 < mData.size - 1 && position != 0) {
                        mData[indexOf + 1].isReadTag = true
                        adapter.notifyDataSetChanged()
                        mData[indexOf + 1]
                    } else {
                        bean.isReadTag = true
                        adapter.notifyDataSetChanged()
                        bean
                    }
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(activity, "admin/${loginBean.user_id}/point/chatPoint", TestEnjoyCountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as TestEnjoyCountData
                        if (result.data != null) {
                            if (!TextUtils.isEmpty(result.data.pointValue)) {
                                preReadTag = result.data.pointValue
                                lastId = preReadTag
                            }
                        }
                        request(1)
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            else -> {
                OkClientHelper.get(activity, "admin/dialog?token=${SPUtils.getString(activity, IConstant.ADMINTOKEN, "")}&chatType=1&lastId=$lastId", TalkListData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as TalkListData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                if (preReadTag == lastId) {
                                    mData.clear()
                                    result.data[0].isReadTag = true
                                    tagBean = result.data[0]
                                    mData.addAll(result.data)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    for (bean in result.data) {
                                        mData.add(bean)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                    }
                                }
                                lastId = mData[mData.size - 1].id
                                if (result.data.size >= 10) {
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    }
                })
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         *
         * 如果是app 退到后台, 则需要考虑摒弃此条件
         */
        if (!visible || !ActivityLifecycleHelper.isAppPause || !isResumed) {
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

    override fun onDestroy() {
        try {
            audioPlayer.stop()
        } catch (e: Exception) {

        }
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}