package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_talk.*
import kotlinx.android.synthetic.main.item_voice.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogDeleteConment
import org.xiaoxingqi.shengxi.dialog.DialogNormalReport
import org.xiaoxingqi.shengxi.dialog.DialogReport
import org.xiaoxingqi.shengxi.dialog.DialogReportSuccess
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddTalkAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.time.DateUtils
import org.xiaoxingqi.shengxi.wedgit.EchoesProgress
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.IOException

class TalkListActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<TalkListData.TalkListBean>
    private var playBean: TalkListData.TalkListBean? = null

    /**
     * 声兮所属用户的id
     */
    private var uid: String? = null

    private var voiceId: String? = null
    private var chatId: String? = null
    private var lastId: String = ""
    private var talkId: String? = null
    private var isScrolled = false
    private var voiceBean: BaseBean? = null
    private val mData by lazy {
        ArrayList<TalkListData.TalkListBean>()
    }
    private var deleteItem: TalkListData.TalkListBean? = null
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var footView: View

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(10) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
            voiceBean?.let {
                if (it.isPlaying) {
                    footView.voiceProgress.updateProgress(audioPlayer.currentPosition.toInt())
                }
            }
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
        return R.layout.activity_talk
    }

    override fun initView() {
        val layout = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        layout.stackFromEnd = true//列表再底部开始展示，反转后由上面开始展示
        layout.reverseLayout = true//列表翻转
        recyclerView.layoutManager = layout
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
        footView = LayoutInflater.from(this).inflate(R.layout.head_talk_view, recyclerView, false)
        footView.linearOperate.visibility = View.GONE
        footView.viewLine.visibility = View.GONE
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        voiceId = intent.getStringExtra("voice_id")
        chatId = intent.getStringExtra("chat_id")
        uid = intent.getStringExtra("uid")
        talkId = intent.getStringExtra("talkId")
        if (TextUtils.isEmpty(talkId)) {
            talkId = uid
        }
        adapter = object : QuickAdapter<TalkListData.TalkListBean>(this, R.layout.item_talk_list, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            var hasNextPlay = false//是否自动播放吓一条音频
            override fun convert(helper: BaseAdapterHelper?, item: TalkListData.TalkListBean?) {
                val position = helper!!.itemView.tag as Int
                helper.getView(R.id.ivOfficialRight).visibility = View.GONE
                helper.getView(R.id.ivOfficialLeft).visibility = View.GONE
                if (item!!.is_self == 1) {
                    helper.getView(R.id.cardLeft).visibility = View.GONE
                    helper.getView(R.id.cardRight).visibility = View.VISIBLE
                    helper.getView(R.id.viewReadStatus).visibility = View.GONE
                    helper.getView(R.id.iv_user_type_right).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_right).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_left).visibility = View.GONE
                    helper.getView(R.id.ivOfficialRight).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                } else {
                    helper.getView(R.id.cardLeft).visibility = View.VISIBLE
                    helper.getView(R.id.cardRight).visibility = View.GONE
                    helper.getView(R.id.viewReadStatus).visibility = if (item.read_at == 0 && item.resource_type == "1") View.VISIBLE else View.GONE
                    helper.getView(R.id.iv_user_type_left).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type_left).isSelected = item.identity_type == 1
                    helper.getView(R.id.iv_user_type_right).visibility = View.GONE
                    helper.getView(R.id.ivOfficialLeft).visibility = if (item.from_user_id == "1") View.VISIBLE else View.GONE
                }
                try {
                    if (position != mData.size - 1) {
                        if (DateUtils.isCloseEnough(item.created_at * 1000L, mData[position + 1].created_at * 1000L)) {
                            helper.getTextView(R.id.tv_Time).visibility = View.GONE
                        } else {
                            helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                            helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkListActivity, item.created_at)
                        }
                    } else {
                        helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                        helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkListActivity, item.created_at)
                    }
                } catch (e: Exception) {
                    helper.getTextView(R.id.tv_Time).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@TalkListActivity, item.created_at)
                }
                if (item.isBusy) {
                    helper.getView(R.id.tv_Customer_busy).visibility = View.VISIBLE
                    if (!isScrolled) {
                        recyclerView.smoothScrollBy(0, 100)
                        isScrolled = true
                    }
                } else {
                    helper.getView(R.id.tv_Customer_busy).visibility = View.GONE
                }
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_leftimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_rightimg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                val echoesProgress = helper.getView(R.id.echoesProgress) as EchoesProgress
                echoesProgress.setData(item)
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                seekProgress.setOnClickListener {
                    hasNextPlay = false
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放   暂停
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            echoesProgress.finish()
                            playBean = null
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
                    download(item)
                }
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                echoesProgress.finish()
                            }
                        }
                    }

                    override fun endTrack(progress: Float) {
                        hasNextPlay = false
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(item)
                    }
                })

                echoesProgress.setReOnClickListener {
                    //点击之后立即重播此条数据
                    hasNextPlay = false
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            echoesProgress.finish()
                            playBean = null
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    item.pasuePosition = 0
                    download(item)
                }
                helper.getView(R.id.cardLeft).setOnClickListener {
                    startActivity(Intent(this@TalkListActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                helper.getView(R.id.cardRight).setOnClickListener {
                    startActivity(Intent(this@TalkListActivity, UserDetailsActivity::class.java)
                            .putExtra("url", item.avatar_url)
                            .putExtra("id", item.from_user_id)
                    )
                }
                seekProgress.setOnLongClickListener {
                    DialogReport(this@TalkListActivity).setCollectionAble(true, resources.getString(R.string.string_20)).setIsReportNormal(true).setReportTitle(resources.getString(R.string.string_report_normal)).setDeleteStatus(true).setReportStatus(item.is_self != 1).setOnClickListener(View.OnClickListener {
                        when (it.id) {
                            R.id.tv_Delete -> {
                                DialogDeleteConment(this@TalkListActivity).setOnClickListener(View.OnClickListener {
                                    deleteComment(item)
                                }).show()
                            }
                            R.id.tv_Report -> {
                                DialogNormalReport(this@TalkListActivity).setOnClickListener(View.OnClickListener { report ->
                                    when (report.id) {
                                        R.id.tv_Attach -> {
                                            reportComment(item.dialog_id.toString(), "1")
                                        }
                                        R.id.tv_Porn -> {
                                            reportComment(item.dialog_id.toString(), "2")
                                        }
                                        R.id.tv_Junk -> {
                                            reportComment(item.dialog_id.toString(), "3")
                                        }
                                        R.id.tv_illegal -> {
                                            reportComment(item.dialog_id.toString(), "4")
                                        }
                                    }
                                }).show()
                            }
                            R.id.tvCollection -> {
                                startActivity<DialogAddTalkAlbumActivity>("talkId" to item.dialog_id)
                            }
                        }
                    }).show()
                    false
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(item: TalkListData.TalkListBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    if (item.read_at == 0) {
                        hasNextPlay = true
                        clearRead(item.dialog_id)
                        item.read_at = (System.currentTimeMillis() / 1000).toInt()
                        changeStatue(true)
                    }
                    val file = getDownFilePath(item.resource_url)
                    if (file.exists() && file.length() > 0) {
                        audioPlayer.setDataSource(file.absolutePath)
                        if (currentMode == MODE_EARPIECE) {
                            audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                        } else
                            audioPlayer.start(if (SPUtils.getBoolean(this@TalkListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@TalkListActivity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioPlayer.setDataSource(o.toString())
                                if (currentMode == MODE_EARPIECE) {
                                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                                } else
                                    audioPlayer.start(if (SPUtils.getBoolean(this@TalkListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }

                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
                        override fun onCompletion() {
                            item.isPlaying = false
                            changeStatue(true)
                            progressHandler.stop()
                            if (hasNextPlay) {
                                /**
                                 * 查找下一条音频
                                 */
                                val indexOf = mData.indexOf(item)
                                if (item.is_self != 1)
                                    for (index in indexOf downTo 0) {
                                        if (mData[index].read_at == 0 && mData[index].is_self != 1) {
                                            /**
                                             * 下一条要播放的音频
                                             */
                                            download(mData[index])
                                            break
                                        }
                                    }
                            }
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            playBean = item
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            item.isPlaying = true
                            progressHandler.start()
                        }

                        override fun onInterrupt() {
                            if (item.pasuePosition == 0) {
                                if (hasNextPlay)
                                    hasNextPlay = false
                            }
                            item.isPlaying = false
                            changeStatue(true)
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
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.echoesProgress) as EchoesProgress
                        if (voiceProgress.bean.read_at != 0) {
                            helper.getView(R.id.viewReadStatus).visibility = View.GONE
                        }
                        voiceProgress.changeProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, footView)
        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
        queryInfo()
        request(0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            voiceId = it.getStringExtra("voice_id")
            chatId = it.getStringExtra("chat_id")
            uid = it.getStringExtra("uid")
            talkId = it.getStringExtra("talkId")
            if (TextUtils.isEmpty(talkId)) {
                talkId = uid
            }
            transLayout.showProgress()
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            queryInfo()
            mData.clear()
            adapter.notifyDataSetChanged()
            request(0)
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        linearEcho.setOnClickListener {
            /**
             * 先查询权限
             */
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (loginBean.user_id == uid) {
                /* startActivityForResult(Intent(this@TalkListActivity, RecordVoiceActivity::class.java), REQUEST_RECORD)
                 overridePendingTransition(0, 0)*/
                queryOtherSetting()//是自己声兮
            } else
                querySetting()
        }
        swipeRefresh.setOnRefreshListener {
            lastId = if (mData.size > 0) {
                mData[mData.size - 1].dialog_id
            } else {
                ""
            }
            request(0)
        }
//        iv_Close.setOnClickListener {
//            linear_Hint.visibility = View.GONE
//            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//            SPUtils.setBoolean(this, IConstant.COMMENTHINT + loginBean.user_id, true)
//        }
        footView.imageGroup.setOnClickViewListener { position ->
            voiceBean?.let {
                startActivity(Intent(this, ShowPicActivity::class.java)
                        .putExtra("index", position)
                        .putExtra("data", it.img_list))
                overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
        }
        footView.voiceProgress.setOnClickListener {
            sendObserver()
            voiceBean?.let {
                if (audioPlayer.isPlaying) {
                    if (voiceBean!!.isPlaying) {//当前正在播放
                        progressHandler.stop()
                        voiceBean!!.isPlaying = false
                        voiceBean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                        audioPlayer.stop()
                        footView.voiceProgress.finish()
                        return@setOnClickListener
                    } else {
                        audioPlayer.stop()
                        progressHandler.stop()
                    }
                }
                download()
            }
        }
        footView.voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            voiceBean?.let {
                if (audioPlayer.isPlaying) {
                    if (voiceBean!!.isPlaying) {//当前正在播放
                        audioPlayer.stop()
                        progressHandler.stop()
                        voiceBean!!.isPlaying = false
                        footView.voiceProgress.finish()
                    }
                }
                voiceBean!!.pasuePosition = 0
                download()
            }
        }
        val seekProgress = footView.voiceProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceBean?.let {
                            voiceBean!!.allDuration = audioPlayer.duration
                            progressHandler.stop()
                            audioPlayer.stop()
                            voiceBean!!.isPlaying = false
                            footView.voiceProgress.finish()
                        }
                    }
                }
            }

            override fun endTrack(progress: Float) {
                voiceBean?.let {
                    voiceBean!!.pasuePosition = (progress * voiceBean!!.allDuration).toInt()
                    download()
                }
            }
        })
    }

    /**
     * 下载资源
     */
    private fun download() {
        try {
            if (TextUtils.isEmpty(voiceBean!!.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            findViewById<View>(R.id.play).isSelected = !findViewById<View>(R.id.play).isSelected
            val file = getDownFilePath(voiceBean!!.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this, voiceBean!!.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { showToast(VolleyErrorHelper.getMessage(it)) })
            }
            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
                override fun onCompletion() {
                    footView.voiceProgress.finish()
                    voiceBean!!.isPlaying = false
                    progressHandler.stop()
                }

                override fun onInterrupt() {
                    footView.voiceProgress.finish()
                    voiceBean!!.isPlaying = false
                    progressHandler.stop()
                }

                override fun onPrepared() {
                    voiceBean!!.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(voiceBean!!.pasuePosition)
                    voiceBean!!.pasuePosition = 0
                    addPlays(voiceBean!!, footView.tv_Sub) {}
                    isPlayed = true
                    voiceBean!!.isPlaying = true
                    progressHandler.start()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 更新已读
     */
    private fun clearRead(dialog: String) {
        OkClientHelper.patch(this, "chats/$chatId/$dialog", FormBody.Builder().add("readAt", (System.currentTimeMillis() / 1000).toString()).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private var isBusy = false

    private fun queryBusy() {
        OkClientHelper.get(this, "users/$talkId/setting/autoReply", PatchData::class.java, object : OkResponse {
            override fun success(result: Any) {
                result as PatchData
                if (result.code == 0) {
                    if (mData.size > 0) {
                        mData[0].isBusy = result.data.auto_reply == 1  //用户设置了繁忙状态
                        if (result.data.auto_reply == 1) {
                            isBusy = true
                            adapter.notifyItemChanged(0)
                        } else {
                            isBusy = false
                        }
                    }
                } else
                    isBusy = false
            }

            override fun onFailure(any: Any) {
                isBusy = false
            }
        })
    }

    private fun queryOtherSetting() {
        transLayout.showProgress()
        OkClientHelper.get(this, "users/$talkId/setting/chatHobby", PatchData::class.java, object : OkResponse {
            override fun success(result: Any) {
                result as PatchData
                if (result.code == 0) {
                    val intent = Intent(this@TalkListActivity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", voiceBean?.user?.avatar_url)
                            .putExtra("sendPath", AppTools.fastJson(talkId, 1, voiceId))
                    if (result.data != null) {
                        intent.putExtra("isBusy", result.data.auto_reply)
                                .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any) {
                transLayout.showContent()
            }
        })
    }

    private fun querySetting() {
        transLayout.showProgress()
        OkClientHelper.get(this, "chats/check/$talkId/$voiceId", ChechOutReplyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as ChechOutReplyData).code == 0) {
                    startActivityForResult(Intent(this@TalkListActivity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", voiceBean?.user?.avatar_url)
                            .putExtra("sendPath", AppTools.fastJson(talkId, 1, voiceId))
                            .putExtra("isBusy", result.data.auto_reply)
                            .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                            , REQUEST_RECORD)
                    overridePendingTransition(0, 0)
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun reportComment(id: String, type: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("reportType", type)
                .add("resourceType", "3")
                .add("resourceId", id)
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    DialogReportSuccess(this@TalkListActivity).show()
//                    showToast(resources.getString(R.string.string_report_success))
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun deleteComment(bean: TalkListData.TalkListBean) {
        transLayout.showProgress()
        deleteItem = bean
        EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(talkId, voiceId, bean.chat_id, bean.dialog_id), false))
    }

    private var fromUserIdentity = 0
    private fun queryInfo() {
        OkClientHelper.get(this, "voices/$uid/$voiceId", DynamicDatailData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as DynamicDatailData
                //展示声昔的信息
                result.data?.let {
                    voiceBean = result.data
                    glideUtil.loadGlide(result.data.user.avatar_url, footView.roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(result.data.user.avatar_url))
                    footView.tv_UserName.text = result.data.user.nick_name
                    footView.tvTime.text = TimeUtils.getInstance().paserFriends(this@TalkListActivity, result.data.created_at)
                    footView.tv_Action.text = if (TextUtils.isEmpty(result.data.topic_name)) "" else "#${result.data.topic_name}#"
                    try {
                        footView.tv_Sub.text = if (result.data.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${result.data.played_num}"
                    } catch (e: Exception) {
                        footView.tv_Sub.text = resources.getString(R.string.string_Listener)
                    }
                    footView.iv_user_type.visibility = if (result.data.user.identity_type == 0) View.GONE else View.VISIBLE
                    footView.iv_user_type.isSelected = result.data.user.identity_type == 1
                    if (result.data.user_id == 1) {
                        footView.ivOfficial.visibility = View.VISIBLE
                    }
                    if (!TextUtils.isEmpty(result.data.is_shared)) {
                        footView.tv_Sub.visibility = if (result.data.is_private == 1) View.GONE else View.VISIBLE
                        footView.iv_Privacy.visibility = if (result.data.is_private == 1) View.VISIBLE else View.GONE
                        footView.linearStatusText.visibility = View.GONE
                    } else {
                        footView.tv_Sub.visibility = View.GONE
                        footView.iv_Privacy.visibility = View.GONE
                        when (result.data.friend_status) {
                            2 -> //好友
                                footView.linearStatusText.visibility = View.GONE
                            1 -> {
                                footView.linearStatusText.visibility = View.VISIBLE
                                footView.linearStatusText.isSelected = false
                            }
                            0 -> {
                                footView.linearStatusText.visibility = View.VISIBLE
                                footView.linearStatusText.isSelected = true
                            }
                        }
                    }
                    footView.voiceProgress.data = voiceBean
                    footView.imageGroup.setData(result.data.img_list)
                    result.data.resource?.let {
                        if (!TextUtils.isEmpty(it.id)) {
                            footView.itemDynamic.visibility = View.VISIBLE
                            footView.itemDynamic.setData(it, result.data.resource_type, result.data.user_score)
                        } else {
                            footView.itemDynamic.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
        OkClientHelper.get(this, "users/$talkId", UserInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserInfoData
                if (result.code == 0) {
                    fromUserIdentity = result.data.identity_type
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "chats/$chatId?lastId=$lastId", TalkListData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as TalkListData
                if (result.code == 0) {
                    if (result.data != null) {
                        if (TextUtils.isEmpty(lastId)) {
                            mData.clear()
                            mData.addAll(result.data)
                            adapter.notifyDataSetChanged()
                            queryBusy()
                        } else {
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
//                            recyclerView.smoothScrollToPosition(mData.size - result.data.size)
                        }
//                        recyclerView.scrollToPosition(mData.size - result.data.size)
                        if (result.data.size < 10) {
                            swipeRefresh.isEnabled = false
                        }
                    }
                }
                swipeRefresh.isRefreshing = false
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                transLayout.showContent()
            }
        }, "V4.3")
    }

    private fun clearFlag() {
        OkClientHelper.get(this, "chats/$talkId/$voiceId?lastId=null", TalkListData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("回声界面: uid:$uid  talkId : $talkId ; 资源上传成功,请求socket发送;  ${MainActivity.sInstance}", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(talkId, 1, voiceId, voice, voiceLength)))
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateIdentity(event: UpdateIdentityEvent) {
        if (mData.size > 0) {
            mData.forEach {
                if (it.is_self == 1) {
                    it.identity_type = event.type
                    val indexOf = mData.indexOf(it)
                    adapter.notifyItemChanged(indexOf)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteEvent(event: OnDeleteMsg) {
        if (event.data.code == 0) {
            /**
             * 删除成功  移除当前的 isBusy
             *
             * 撤回判断是否是撤回的最新的消息,
             * 并且判断 是否处于繁忙状态, 需要更新UI
             */
            try {
                if (mData.size > 0 && mData.contains(deleteItem)) {
                    val indexOf = mData.indexOf(deleteItem)
                    if (indexOf >= 0) {
                        mData.remove(deleteItem)
                        if (isBusy) {
                            if (mData.size > 0)
                                if (!mData[0].isBusy) {
                                    mData[0].isBusy = true
                                }
                        }
                        adapter.notifyDataSetChanged()
                        deleteItem = null
                    }
                }
            } catch (e: Exception) {
            }
        } else {
            if (deleteItem != null)
                showToast("撤回失败")
        }
        transLayout.showContent()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgReceive(event: OnNewMsgEvent) {
        if (!TextUtils.isEmpty(event.result.action)) {
            try {
                if ("delete" == event.result.action && voiceId == event.result.data.voiceId) {
                    /**
                     * 当前需要删除列表中的一条会话
                     */
                    if (mData.size > 0) {
                        var tempBean: TalkListData.TalkListBean? = null
                        for (item in mData) {
                            if (item.dialog_id == event.result.data.dialogId) {
                                tempBean = item
                                break
                            }
                        }
                        if (null != tempBean) {
                            if (0 == mData.indexOf(tempBean)) {
                                if (mData.size >= 2) {
                                    if (isBusy) {
                                        mData[1].isBusy = true
                                    }
                                }
                            }
                            if (tempBean.isPlaying) {
                                if (audioPlayer.isPlaying)
                                    audioPlayer.stop()
                            }
                            mData.remove(tempBean)
                            adapter.notifyDataSetChanged()
                        }
                    }

                }
            } catch (e: Exception) {
            }
        } else
            if (chatId == event.result.data.chat_id.toString()) {
                if (!TextUtils.isEmpty(event.result.data.flag) && "1" == event.result.data.flag) {
                    return
                }
                val loginBean = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                val bean = TalkListData.TalkListBean()
                bean.is_self = if (loginBean.data.user_id == event.result.data.from_user_id) {
                    bean.identity_type = loginBean.data.identity_type
                    1
                } else {
                    bean.identity_type = fromUserIdentity
                    0
                }
                bean.created_at = event.result.data.created_at
                bean.voice_len = event.result.data.dialog_content_len.toString()
                bean.avatar_url = event.result.data.user_avatar_url
                bean.resource_type = event.result.data.dialog_content_type.toString()
                bean.dialog_id = event.result.data.dialog_id.toString()
                bean.resource_url = event.result.data.dialog_content_url
                bean.from_user_id = event.result.data.from_user_id
                bean.chat_id = event.result.data.chat_id.toString()
                if (!TextUtils.isEmpty(event.result.data.auto_reply_msg)) {
                    if (mData.size > 0) {
                        mData[0].isBusy = false
                        bean.isBusy = true
                    }
                } else {
                    if (bean.is_self == 1) {
                        if (mData.size > 0) {
                            mData[0].isBusy = false
                            isBusy = false
                        }
                    } else {
                        if (isBusy) {
                            if (mData.size > 0) {
                                mData[0].isBusy = false
                                bean.isBusy = true
                            }
                        } else {
                            if (mData.size > 0) {
                                mData[0].isBusy = false
                                isBusy = false
                            }
                        }
                    }
                }
                mData.add(0, bean)
                adapter.notifyDataSetChanged()
                recyclerView.smoothScrollToPosition(0)
                /**
                 * 请求清空角标
                 */
                clearFlag()
            }
    }

    class OnNewMsgEvent(var result: SocketData)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        if (!isVisibleActivity) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
                audioPlayer.stop()
            }
        }
    }

    override fun finish() {
        super.finish()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }
}