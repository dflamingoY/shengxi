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
import org.xiaoxingqi.shengxi.dialog.DialogAdminOperatorConfirm
import org.xiaoxingqi.shengxi.dialog.DialogCancelMsg
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.TestEnjoyCountData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.AdminImageGroup
import org.xiaoxingqi.shengxi.wedgit.ItemDynamicView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.File
import java.io.IOException

/**
 * 管理世界界面
 */
class ManagerWorldFragment : BaseFragment(), ITabClickCall {

    private var visible = false
    override fun tabClick(isVisible: Boolean) {
        if (visible == isVisible) {
            return
        }
        visible = isVisible
    }

    override fun doubleClickRefresh() {

    }

    private var lastId: String? = ""
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy { ArrayList<BaseBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: BaseBean? = null
    private var preReadTag: String = ""
    private var tagBean: BaseBean? = null
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
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        audioPlayer = AudioPlayer(activity)
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_admin_world, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                glideUtil.loadGlide(item!!.user.avatar_url, helper!!.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tv_Action).visibility = if (TextUtils.isEmpty(item.topic_name)) View.GONE else View.VISIBLE
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.shared_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                helper.getTextView(R.id.tvContent).text = if (TextUtils.isEmpty(item.recognition_content)) "转文字失败" else item.recognition_content
                helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                helper.getView(R.id.linear_tag).visibility = if (item.isReadTag) View.VISIBLE else View.GONE
                val imgGroup = helper.getView(R.id.imageGroup) as AdminImageGroup
                imgGroup.setData(item.img_list)
//                helper.getView(R.id.linear_content).visibility = if (TextUtils.isEmpty(item.resource_content)) View.GONE else View.VISIBLE
                if (item.resource == null) {
                    helper.getView(R.id.itemDynamic).visibility = View.GONE
                }
                item.resource?.let {
                    if (!TextUtils.isEmpty(it.id)) {
                        helper.getView(R.id.itemDynamic).visibility = View.VISIBLE
                        (helper.getView(R.id.itemDynamic) as ItemDynamicView).setData(it, item.resource_type, item.user_score)
                    } else {
                        helper.getView(R.id.itemDynamic).visibility = View.GONE
                    }
                }
                val progress = (helper.getView(R.id.voiceProgress) as VoiceProgress)
                progress.data = item
                helper.getView(R.id.tv_Sub).setOnClickListener {
                    startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                            .putExtra("uid", item.user.id)
                            .putExtra("id", item.voice_id))
                }
                helper.getImageView(R.id.roundImg).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user_id.toString()))
                }
                helper.getView(R.id.tv_retract).setOnClickListener {
                    DialogAdminOperatorConfirm(activity!!).setOnClickListener(View.OnClickListener { retract(item) }).setTitle("从世界撤回心情").show()
                }
                helper.getView(R.id.tv_hide).setOnClickListener {
                    DialogAdminOperatorConfirm(activity!!).setOnClickListener(View.OnClickListener { hide(item) }).setTitle("隐藏心情").show()
                }
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    DialogAdminOperatorConfirm(activity!!).setOnClickListener(View.OnClickListener { deleteItem(item) }).setTitle("删除心情").show()
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                progress./*findViewById<View>(R.id.viewSeekProgress).*/setOnClickListener {
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
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                try {
                    if (TextUtils.isEmpty(item.voice_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    val file = getDownFilePath(item.voice_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(activity, item.voice_url, { o ->
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
        adapter.setOnItemLongClickListener { _, position ->
            DialogCancelMsg(activity!!).setTitle("设置审阅到此").setOnClickListener(View.OnClickListener {
                setReadTag(if (position > 0) mData[position - 1] else mData[position], position)
            }).show()
        }
        adapter.setOnLoadListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                progressHandler.stop()
            }
            request(1)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = preReadTag
            request(1)
        }
    }

    private fun setReadTag(bean: BaseBean, position: Int) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "admin/${loginBean.user_id}/point", FormBody.Builder().add("pointKey", "worldVoicePoint")
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

    /**
     * 删除心情
     */
    private fun deleteItem(item: BaseBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "admin/voices/${item.voice_id}", FormBody.Builder().add("token", SPUtils.getString(activity, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("删除成功")
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
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

    /**
     * 隐藏心情
     */
    private fun hide(item: BaseBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.patch(activity, "admin/voices/${item.voice_id}", FormBody.Builder().add("isHidden", "1").add("token", SPUtils.getString(activity, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("隐藏成功")
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
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

    /**
     * 从世界撤回
     */
    private fun retract(item: BaseBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "admin/voicesShare/${item.voice_id}", FormBody.Builder().add("token", SPUtils.getString(activity, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已从世界撤回")
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
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
                OkClientHelper.get(activity, "admin/${loginBean.user_id}/point/worldVoicePoint", TestEnjoyCountData::class.java, object : OkResponse {
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
            1 -> {
                OkClientHelper.get(activity, "admin/voices/type/2?token=${SPUtils.getString(activity, IConstant.ADMINTOKEN, "")}&lastId=$lastId", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as VoiceData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                if (lastId == preReadTag) {
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
                                lastId = result.data[result.data.size - 1].id
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