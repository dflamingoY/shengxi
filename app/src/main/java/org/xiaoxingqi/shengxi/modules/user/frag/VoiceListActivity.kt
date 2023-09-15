package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.lauzy.freedom.lbehaviorlib.behavior.BottomBehavior
import kotlinx.android.synthetic.main.activity_voice_list.*
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
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogCreateVoice
import org.xiaoxingqi.shengxi.dialog.DialogLimitTalk
import org.xiaoxingqi.shengxi.dialog.DialogUserSet
import org.xiaoxingqi.shengxi.dialog.DialogVoiceSort
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.ImageGroupView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceListItemView
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import java.io.IOException
import kotlin.math.ceil

class VoiceListActivity : BaseAct() {
    private var userId: String? = null
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private var playBean: BaseBean? = null
    private var lastId: String? = null
    private var relation = 5//-1 表示自己   0陌生人 1 待验证 2 好友
    private var strangeView = 7//0=禁止，7=默认7天
    private lateinit var audioPlayer: AudioPlayer
    private var voiceTotalLength = 0
    private var loadView: View? = null
    private var createAt: Int = 0
    private var sort = "desc"

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
        return R.layout.activity_voice_list
    }

    override fun initView() {

    }

    override fun initData() {
        userId = intent.getStringExtra("userId")
        voiceTotalLength = intent.getIntExtra("voiceLength", 0)
        createAt = intent.getIntExtra("createdAt", 0)
        audioPlayer = AudioPlayer(this)
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
                val paserTime = TimeUtils.getInstance().formatterTime(this@VoiceListActivity, item.created_at)
                try {
                    val split = paserTime.split("_")
                    helper.getTextView(R.id.tv_FirstTime).text = split[0]
                    helper.getTextView(R.id.tv_SecondTime).text = split[1]
                } catch (e: Exception) {

                }
                imageGroupView.setOnClickViewListener {
                    startActivity(Intent(this@VoiceListActivity, ShowPicActivity::class.java)
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
                        startActivity(Intent(this@VoiceListActivity, TopicResultActivity::class.java)
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
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
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
        loadView = LayoutInflater.from(this).inflate(R.layout.view_loadmore_voice_list, recyclerView, false)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadView)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
//        if (IConstant.USER_EXTROVERT.equals(character, true)) {
//            transLayout.findViewById<TextView>(R.id.tv_Voice).text = resources.getString(R.string.string_moodList_memory_e_2)
//        }
        transLayout.findViewById<View>(R.id.tv_temp1).isSelected = AppTools.getLanguage(this) == IConstant.HK || AppTools.getLanguage(this) == IConstant.TW
        if (loginBean.user_id != userId) {//别人
            BottomBehavior.from(relative_operator).isEnableScroll(false)
            if (voiceTotalLength == 0) {
                transLayout.showEmpty()
                transLayout.findViewById<View>(R.id.relative_OtherEmpty).visibility = View.VISIBLE
                transLayout.findViewById<TextView>(R.id.tv_Other_Hint).text = resources.getString(R.string.string_empty_voice_list_19)
                transLayout.findViewById<View>(R.id.tv_CreateFriend).visibility = View.GONE
            } else {
                request(0)
            }
            tv_Title.text = resources.getString(R.string.string_title_2)
            linearSort.visibility = View.GONE
        } else {
            relative_operator.visibility = View.VISIBLE
            relativeInfo.visibility = View.VISIBLE
            relativeAddFriend.visibility = View.GONE
            request(2)
        }
        try {
            val parseTime = TimeUtils.getInstance().formatterTime(this, createAt)
            val split = parseTime.split("_")
            tv_FirstTime.text = split[0]
            tv_SecondTime.text = split[1]
        } catch (e: Exception) {
        }
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(this, mData.indexOf(item))
        }
        if (TextUtils.isEmpty(item.voice_url)) {
            showToast(resources.getString(R.string.string_error_file))
        }
        try {
            playBean = item
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this@VoiceListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this@VoiceListActivity, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this@VoiceListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, {
//                    showToast(VolleyErrorHelper.getMessage(it))
                    if (!item.isReDown) {
                        item.isReDown = true
                        down(item, false)
                    } else
                        nextAudio()
                })
            }
        } catch (e: Exception) {

        }
    }

    private fun nextAudio() {
        playBean?.let {
            if (SPUtils.getBoolean(this@VoiceListActivity, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerView.scrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    request(2)
                }
            } else {
                customPlayMenu.isSelected = false
            }
        }
    }

    override fun initEvent() {

        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                customPlayMenu.isSelected = true
                playBean?.let {
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    addPlays(it, null) {}
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1) {
                        thumb(it, null, 5)
                    }
                }
                progressHandler.stop()
                try {
                    nextAudio()
                } catch (e: Exception) {
                }
            }

            override fun onInterrupt() {
                customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                }
                progressHandler.stop()
            }
        }
        customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = mData.indexOf(it) + 1
                        /*if (index >= mData.size) {
                            index = 0
                            recyclerView.scrollToPosition(0)
                        }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(2)
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
                        recyclerView.scroll(this@VoiceListActivity, mData.indexOf(it), false)
                    }
            }
        })
        btn_Back.setOnClickListener {
            finish()
        }
        adapter.setOnLoadListener {
            request(2)
        }
        tv_Friend.setOnClickListener {
            if (!it.isSelected) {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                tv_Friend.isSelected = true
                requestFriends()
            }
        }
        transLayout.findViewById<View>(R.id.tv_CreateFriend).setOnClickListener {
            if (!it.isSelected) {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                tv_Friend.isSelected = true
                requestFriends()
            }
        }
        adapter.setOnItemClickListener { _, position ->
            if (null != mData[position].voice_id)
                startActivity(Intent(this, DynamicDetailsActivity::class.java)
                        .putExtra("uid", mData[position].user_id.toString())
                        .putExtra("id", mData[position].voice_id)
                )
        }
        transLayout.findViewById<View>(R.id.tv_Voice).setOnClickListener {
            if (!RecordTransparentActivity.isOnCreate) {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("type", 1))
                overridePendingTransition(0, 0)
            }
        }
        swipeRefresh.setOnRefreshListener {
            audioPlayer.stop()
            playBean = null
            lastId = null
            request(2)
        }
        ivRecord.setOnClickListener {
            if (!RecordTransparentActivity.isOnCreate) {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("type", 1))
                overridePendingTransition(0, 0)
            }
        }
        linearSort.setOnClickListener {
            DialogVoiceSort(this).setOnClickListener(if (sort == "desc") 1 else 0, View.OnClickListener { view ->
                when (view.id) {
                    R.id.tvOrder -> {
                        if (sort != "asc") {
                            sort = "asc"
                            if (audioPlayer.isPlaying) {
                                audioPlayer.stop()
                            }
                            ivArrow.rotation = 180f
                            lastId = null
                            transLayout.showProgress()
                            request(2)
                        }
                    }
                    R.id.tvDesc -> {
                        if (sort != "desc") {
                            if (audioPlayer.isPlaying) {
                                audioPlayer.stop()
                            }
                            ivArrow.rotation = 0f
                            lastId = null
                            sort = "desc"
                            transLayout.showProgress()
                            request(2)
                        }
                    }
                }
            }).setLocation(AppTools.getActionBarSize(this))
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询好友关系
                transLayout.showProgress()
                OkClientHelper.get(this, "relations/$userId", RelationData::class.java, object : OkResponse {
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
                                    relativeInfo.visibility = View.VISIBLE
                                } else if (result.data.friend_status == 0) {//无关系
                                    relativeAddFriend.visibility = View.VISIBLE
                                    if (result.data.whitelist != null) {
                                        relative_operator.visibility = View.VISIBLE
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                        tv_stranger_desc.text = String.format(resources.getString(R.string.string_add_white_list_voice), ceil((result.data.whitelist.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt())
                                        request(2)
                                    } else {
                                        request(1)
                                    }
                                } else if (result.data.friend_status == 1) {//待验证
                                    if (result.data.whitelist != null) {
                                        loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_23)
                                        relative_operator.visibility = View.VISIBLE
                                        tv_stranger_desc.text = String.format(resources.getString(R.string.string_add_white_list_voice), ceil((result.data.whitelist.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt())
                                        request(2)
                                    } else {
                                        request(1)
                                    }
                                    relativeAddFriend.visibility = View.VISIBLE
                                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                                    tv_Friend.isSelected = true
                                    tv_Friend.text = resources.getString(R.string.string_pending)
                                    transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (AppTools.isNetOk(this@VoiceListActivity)) {
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
                OkClientHelper.get(this, "users/$userId/settings?settingName=voice_visible_days&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        if (result.code == 0) {
                            result.data?.let {
                                if (it.setting_name == "voice_visible_days") {
                                    strangeView = it.setting_value
                                }
                            }
                        }
                        if (strangeView == 0) {
                            transLayout.showEmpty()
                            transLayout.findViewById<View>(R.id.relative_OtherEmpty).visibility = View.VISIBLE
                            transLayout.findViewById<TextView>(R.id.tv_Other_Hint).text = resources.getString(R.string.string_empty_voice_list_21)
                        } else {
                            //如果是全部, 则不显示任何弹窗
                            loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_21)
                            tv_stranger_desc.text = String.format(resources.getString(R.string.string_empty_voice_list_22), when (strangeView) {
                                7 -> {
                                    relative_operator.visibility = View.VISIBLE
                                    loadView?.findViewById<TextView>(R.id.tv_empty_desc)?.text = resources.getString(R.string.string_empty_voice_list_20)
                                    "七"
                                }
                                30 -> {
                                    relative_operator.visibility = View.VISIBLE
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

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            else -> {//查询数据
                OkClientHelper.get(this, "users/$userId/voices?lastId=$lastId&moduleId=2&recognition=1&sort=$sort", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        swipeRefresh.isRefreshing = false
                        (result as VoiceData)
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0) {
                            if (result.data != null) {
                                if (lastId == null) {
                                    if (audioPlayer.isPlaying) {
                                        audioPlayer.stop()
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
                                    relativeAddFriend.visibility = View.VISIBLE
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
                        if (AppTools.isNetOk(this@VoiceListActivity)) {
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
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                    tv_Friend.isSelected = true
                    tv_Friend.text = resources.getString(R.string.string_pending)
                    transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                    EventBus.getDefault().post(INotifyFriendStatus(1, userId))
                    if (SPUtils.getInt(this@VoiceListActivity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@VoiceListActivity).show()
                    } else if (SPUtils.getBoolean(this@VoiceListActivity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@VoiceListActivity).setOnClickListener(View.OnClickListener {
                            userId?.let { it1 ->
                                addWhiteList(it1)
                            }
                        }).show()
                    }
                } else {
                    if (SPUtils.getLong(this@VoiceListActivity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(this@VoiceListActivity).show()
                    } else {
                        showToast(result.msg)
                    }
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = false
                    tv_Friend.isSelected = false
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
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 1) {
            try {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                tv_Friend.isSelected = true
                tv_Friend.text = resources.getString(R.string.string_pending)
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
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            if (event.type == 3) {
                if (mData.size > 0) {
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
        super.onDestroy()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }
}