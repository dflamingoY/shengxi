package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.lauzy.freedom.lbehaviorlib.behavior.BottomBehavior
import kotlinx.android.synthetic.main.frag_voice_list.view.*
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
import org.xiaoxingqi.shengxi.dialog.DialogCreateVoice
import org.xiaoxingqi.shengxi.dialog.DialogLimitTalk
import org.xiaoxingqi.shengxi.dialog.DialogUserSet
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.*
import java.io.IOException
import kotlin.math.ceil

/**
 *声兮列表
 */
class VoiceListFragment : BaseFragment(), ITabClickCall {
    private var visible = false
    override fun tabClick(isVisible: Boolean) {
        if (visible == isVisible) {
            return
        }
        visible = isVisible
    }

    override fun doubleClickRefresh() {
    }

    private var userId: String? = null
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private var playBean: BaseBean? = null
    private var lastId: String? = null
    private var relation = 5//-1 表示自己   0陌生人 1 待验证 2 好友
    private var strange_view = 7//0=禁止，7=默认7天
    private lateinit var audioplyer: AudioPlayer
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var loadView: View? = null
    private var voiceTotalLength = 0
    override fun getLayoutId(): Int {
        return R.layout.frag_voice_list
    }

    private lateinit var transLayout: TransLayout
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorEvent(event: SensorChangeMoodEvent) {
        val headsetOn = audioplyer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioplyer.audioManager.isBluetoothA2dpOn
        val scoOn = audioplyer.audioManager.isBluetoothScoOn
        if (headsetOn || a2dpOn || scoOn) {
            return
        }
        if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) {
            return
        }
        if (event.type == 1) {
            if (audioplyer.isPlaying) {
                val currentPosition = audioplyer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioplyer.stop()
                playBean?.let {
                    audioplyer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else if (event.type == 2) {
            if (audioplyer.isPlaying) {
                val currentPosition = audioplyer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioplyer.stop()
                playBean?.let {
                    audioplyer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun initView(view: View?) {
        transLayout = view!!.transLayout
        swipeRefresh = view.swipeRefresh
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        audioplyer = AudioPlayer(activity)
//        (activity as UserHomeActivity).let {
//            userId = it.uid
//            voiceTotalLength = it.voiceTotalLength
//        }
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_user_home, mData) {
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
                val paserTime = TimeUtils.getInstance().formatterTime(activity, item.created_at)
                try {
                    val split = paserTime.split("_")
                    helper.getTextView(R.id.tv_FirstTime).text = split[0]
                    helper.getTextView(R.id.tv_SecondTime).text = split[1]
                } catch (e: Exception) {

                }
                imageGroupView.setOnClickViewListener {
                    startActivity(Intent(activity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    activity!!.overridePendingTransition(R.anim.act_enter_alpha, 0)
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
                        startActivity(Intent(activity, TopicResultActivity::class.java)
                                .putExtra("tagId", item.topic_id.toString())
                                .putExtra("tag", item.topic_name))
                    }
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                progress.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioplyer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPause = true
                            item.isPlaying = false
                            item.pasuePosition = audioplyer.currentPosition.toInt()
                            audioplyer.stop()//暂停 记录需要暂停的位置
                            progress.finish()
                            return@setOnClickListener
                        } else {
                            audioplyer.stop()
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
                            if (audioplyer.isPlaying) {
                                item.allDuration = audioplyer.duration
                                progressHandler.stop()
                                audioplyer.stop()
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
                    if (audioplyer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioplyer.stop()
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
                        audioplyer.setDataSource(file.absolutePath)
                        audioplyer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(activity, item.voice_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioplyer.setDataSource(o.toString())
                                audioplyer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioplyer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onPrepared() {
                            item.allDuration = audioplyer.duration
                            audioplyer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            addPlays(item, helper.getTextView(R.id.tv_Sub)) {}
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
                    val currentPosition = audioplyer.currentPosition
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        loadView = LayoutInflater.from(activity).inflate(R.layout.view_loadmore_voice_list, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadView)
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(activity, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            transLayout.findViewById<TextView>(R.id.tv_temp1).text = resources.getString(R.string.string_moodList_memory_e_1)
            transLayout.findViewById<TextView>(R.id.tv_Voice).text = resources.getString(R.string.string_moodList_memory_e_2)
        }
        if (loginBean.user_id != userId) {//别人
            BottomBehavior.from(mView!!.relative_operator).isEnableScroll(false)
            if (voiceTotalLength == 0) {
                transLayout.showEmpty()
                transLayout.findViewById<View>(R.id.relative_OtherEmpty).visibility = View.VISIBLE
                transLayout.findViewById<TextView>(R.id.tv_Other_Hint).text = resources.getString(R.string.string_empty_voice_list_19)
                transLayout.findViewById<View>(R.id.tv_CreateFriend).visibility = View.GONE
            } else {
                request(0)
            }
        } else {
            mView!!.relative_operator.visibility = View.VISIBLE
            mView!!.relativeInfo.visibility = View.VISIBLE
            mView!!.relativeAddFriend.visibility = View.GONE
            request(2)
        }
        /*try {
            val paserTime = TimeUtils.getInstance().formatterTime(activity, (activity as UserHomeActivity).userInfo!!.data.created_at)
            val split = paserTime.split("_")
            mView!!.tv_FirstTime.text = split[0]
            mView!!.tv_SecondTime.text = split[1]
        } catch (e: Exception) {

        }*/
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(2)
        }
        mView!!.tv_Friend.setOnClickListener {
            if (!it.isSelected) {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                mView!!.tv_Friend.isSelected = true
                requestFriends()
            }
        }
        transLayout.findViewById<View>(R.id.tv_CreateFriend).setOnClickListener {
            if (!it.isSelected) {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                mView!!.tv_Friend.isSelected = true
                requestFriends()
            }
        }
        adapter.setOnItemClickListener { _, position ->
            if (null != mData[position].voice_id)
                startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                        .putExtra("uid", mData[position].user_id.toString())
                        .putExtra("id", mData[position].voice_id)
                )
        }
        transLayout.findViewById<View>(R.id.tv_Voice).setOnClickListener {
            startActivity(Intent(activity, SendAct::class.java).putExtra("type", 1))
            activity?.overridePendingTransition(R.anim.operate_enter, 0)
        }
        swipeRefresh.setOnRefreshListener {
            audioplyer.stop()
            lastId = null
            request(2)
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询好友关系
                transLayout.showProgress()
                OkClientHelper.get(activity, "relations/$userId", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 查询用户关系, 是否是好友
                         */
                        result as RelationData
                        if (result.code == 0) {
                            relation = result.data.friend_status
                            if (result.data != null) {
                                if (result.data.friend_status == 2) {//好友
                                    request(2)
                                    mView!!.relativeInfo.visibility = View.VISIBLE
                                } else if (result.data.friend_status == 0) {//无关系
                                    mView!!.relativeAddFriend.visibility = View.VISIBLE
                                    if (result.data.whitelist != null) {
                                        mView!!.relative_operator.visibility = View.VISIBLE
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                        mView!!.tv_stranger_desc.text = String.format(resources.getString(R.string.string_add_white_list_voice), ceil((result.data.whitelist.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt())
                                        request(2)
                                    } else {
                                        request(1)
                                    }
                                } else if (result.data.friend_status == 1) {//待验证
                                    if (result.data.whitelist != null) {
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                        mView!!.relative_operator.visibility = View.VISIBLE
                                        mView!!.tv_stranger_desc.text = String.format(resources.getString(R.string.string_add_white_list_voice), ceil((result.data.whitelist.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt())
                                        request(2)
                                    } else {
                                        request(1)
                                    }
                                    mView!!.relativeAddFriend.visibility = View.VISIBLE
                                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                                    mView!!.tv_Friend.isSelected = true
                                    mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
                                    transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (AppTools.isNetOk(activity)) {
                            showToast(any.toString())
                        } else {
                            mData.clear()
                            adapter.notifyDataSetChanged()
                            transLayout.showOffline()
                        }
                    }
                })
            }
            1 -> {//查询隐私设置是否开放
                OkClientHelper.get(activity, "users/$userId/setting/strangeView", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            strange_view = result.data.strange_view
                            if (strange_view == 0) {
                                transLayout.showEmpty()
                                transLayout.findViewById<View>(R.id.relative_OtherEmpty).visibility = View.VISIBLE
                                transLayout.findViewById<TextView>(R.id.tv_Other_Hint).text = resources.getString(R.string.string_empty_voice_list_21)
                            } else {
                                //如果是全部, 则不显示任何弹窗
                                loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_21)
                                mView!!.tv_stranger_desc.text = String.format(resources.getString(R.string.string_empty_voice_list_22), when (strange_view) {
                                    7 -> {
                                        mView!!.relative_operator.visibility = View.VISIBLE
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_20)
                                        "七"
                                    }
                                    30 -> {
                                        mView!!.relative_operator.visibility = View.VISIBLE
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_20)
                                        "三十"
                                    }
                                    else -> {
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                        "三十"
                                    }
                                })
                                request(2)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            else -> {//查询数据
                OkClientHelper.get(activity, "users/$userId/voices?lastId=$lastId&moduleId=2", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        swipeRefresh.isRefreshing = false
                        (result as VoiceData)
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0) {

                            if (result.data != null) {
                                if (lastId == null) {
                                    if (audioplyer.isPlaying) {
                                        audioplyer.stop()
                                        progressHandler.stop()
                                    }
                                    mData.clear()
                                    mData.addAll(result.data)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    for (bean in result.data) {
                                        mData.add(bean)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                    }
                                }
                                lastId = mData[mData.size - 1].voice_id
                                if (result.data != null && result.data.size >= 10) {
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            } else {
                                /**
                                 * 如果 mdata.size() >0  则加上一条默认
                                 */
                                if (TextUtils.isEmpty(lastId)) {
                                    mData.clear()
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            transLayout.showContent()
                            if (mData.size == 0) {
                                if (relation == 5) {//自己
                                    transLayout.showEmpty()
                                    transLayout.findViewById<View>(R.id.relative_MyEmpty).visibility = View.VISIBLE
                                } else if (relation == 0 || relation == 1) {//陌生人
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                                } else {//好友 提示没有可见心情
                                    loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                                    transLayout.findViewById<View>(R.id.relative_OtherEmpty).visibility = View.VISIBLE
                                    transLayout.findViewById<View>(R.id.tv_CreateFriend).visibility = View.GONE
                                    transLayout.findViewById<TextView>(R.id.tv_Other_Hint).text = resources.getString(R.string.string_empty_voice_list_23)
                                }
                            } else {
                                if (relation == 0 || relation == 1) {
                                    mView!!.relativeAddFriend.visibility = View.VISIBLE
                                    loadView?.findViewById<View>(R.id.viewPlace)?.visibility = View.VISIBLE
                                    /*if (mData.size < 10) {//陌生人数组不够时， 填充View 的高度
                                    } else {
                                        loadView?.findViewById<View>(R.id.viewPlace)?.visibility = View.GONE
                                    }*/
                                } else {
                                    loadView?.findViewById<View>(R.id.viewPlace)?.visibility = View.GONE
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (AppTools.isNetOk(activity)) {
                            showToast(any.toString())
                        } else {
                            mData.clear()
                            adapter.notifyDataSetChanged()
                            transLayout.showOffline()
                            swipeRefresh.isRefreshing = false
                        }
                    }
                })
            }
        }
    }

    private fun requestFriends() {
        val formBody = FormBody.Builder()
                .add("toUserId", userId)
                .build()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                    mView!!.tv_Friend.isSelected = true
                    mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
                    transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                    EventBus.getDefault().post(INotifyFriendStatus(1, userId))
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                userId?.let { it1 -> addWhiteBlack(it1) }
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else {
                        showToast(result.msg)
                    }
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = false
                    mView!!.tv_Friend.isSelected = false
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         *
         * 如果是app 退到后台, 则需要考虑摒弃此条件
         */
        if (!visible || !ActivityLifecycleHelper.isAppPause) {
            playBean?.let {
                if (it.isPlaying || it.isPause) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    progressHandler.stop()
                    adapter.notifyDataSetChanged()
                }
            }
            audioplyer.stop()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 1) {
            try {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                mView!!.tv_Friend.isSelected = true
                mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
                transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
            } catch (e: Exception) {

            }
        } else if (event.status == 2 || event.status == 0) {
            try {
                if (event.userId == userId) {
                    lastId = null
                    request(0)
                }
            } catch (e: Exception) {

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: OperatorVoiceListEvent) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            if (event.type == 3) {
                if (null != mData && mData.size > 0) {
                    var tempBean: BaseBean? = null
                    for (bean in mData) {
                        if (bean.voice_id == event.voice_id) {
                            tempBean = bean
                            break
                        }
                    }
                    if (null != tempBean) {
                        mData.remove(tempBean)
                        adapter.notifyDataSetChanged()
                    }
                    if (mData.size == 0) {
                        transLayout.showEmpty()
                        transLayout.findViewById<View>(R.id.relative_MyEmpty).visibility = View.VISIBLE
                    }
                }
            } else {
                lastId = null
                request(2)
            }
        }
    }

    override fun onDestroy() {
        try {
            audioplyer.stop()
        } catch (e: Exception) {

        }
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}