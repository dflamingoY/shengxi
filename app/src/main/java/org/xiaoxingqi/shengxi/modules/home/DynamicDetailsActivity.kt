package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_dynamic_details.*
import kotlinx.android.synthetic.main.head_dynamic.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.LightUtils
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.EditSendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.reportNormal
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceAnimaProgress
import java.io.IOException

class DynamicDetailsActivity : BaseAct(), ItemOperator {
    companion object {
        private const val REQUEST_ECHOES = 0x777
    }

    override fun onFriend() {
        transLayout.showContent()
    }

    private lateinit var audioPlayer: AudioPlayer
    private lateinit var adapter: QuickAdapter<DynamicTalkData.DynamicTalkBean>
    private lateinit var voiceBean: BaseBean
    private var playBean: DynamicTalkData.DynamicTalkBean? = null
    private val mData by lazy {
        ArrayList<DynamicTalkData.DynamicTalkBean>()
    }
    private var voiceId: String? = null
    private var userinfo: UserInfoData? = null

    /**
     * 声兮拥有着的id
     */
    private var uid: String? = null
    private var comment: DynamicTalkData.DynamicTalkBean? = null
    private var lastId: String? = null
    private lateinit var headView: View
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
            if (voiceBean.isPlaying) {
                headView.voiceAnimProgress.updateProgress(audioPlayer.currentPosition.toInt())
                headView.heartView.waterLevelRatio = audioPlayer.currentPosition / audioPlayer.duration.toFloat()
            }
        }
    }
    private var anim: ValueAnimator? = null
    private lateinit var loginBean: LoginData.LoginBean

    override fun writeHeadSet(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioPlayer.audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            if (devices != null && devices.isNotEmpty()) {
                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                        return true
                    }
                }
            }
        } else {
            val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
            val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
            val scoOn = audioPlayer.audioManager.isBluetoothScoOn
            return headsetOn || a2dpOn || scoOn
        }
        return false
    }

    override fun changSpeakModel(type: Int) {
        if (type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                if (voiceBean.isPlaying) {
                    voiceBean.pasuePosition = currentPosition.toInt()
                }
                playBean?.let {
                    if (it.isPlaying) {
                        it.pasuePosition = currentPosition.toInt()
                    }
                }
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                if (voiceBean.isPlaying) {
                    voiceBean.pasuePosition = currentPosition.toInt()
                }
                playBean?.let {
                    if (it.isPlaying) {
                        it.pasuePosition = currentPosition.toInt()
                    }
                }
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_dynamic_details
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.head_dynamic, recyclerView, false)
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        voiceId = intent.getStringExtra("id")
        userinfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        uid = intent.getStringExtra("uid")
        adapter = object : QuickAdapter<DynamicTalkData.DynamicTalkBean>(this, R.layout.item_comment, mData, headView) {
            var cache = ArrayList<BaseAdapterHelper>()
            override fun convert(helper: BaseAdapterHelper?, item: DynamicTalkData.DynamicTalkBean?) {
                glideUtil.loadGlide(item!!.user.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tvUserName).text = item.user.nick_name
                helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@DynamicDetailsActivity, item.created_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                val voiceProgress = helper.getView(R.id.voiceAnimProgress) as VoiceAnimaProgress
                voiceProgress.data = item
                helper.getTextView(R.id.tv_Count).text = String.format(resources.getString(R.string.string_look_conversion), item.dialog_num.toString())
                voiceProgress.findViewById<View>(R.id.viewSeekProgress).setOnClickListener {
                    sendObserver()
                    if (voiceBean.isPlaying) {
                        headView.voiceAnimProgress.finish()
                    }
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            voiceProgress.finish()
                            playBean = null
                            return@setOnClickListener
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
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
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(helper, item)
                    }
                })
                voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    sendObserver()
                    if (voiceBean.isPlaying) {
                        headView.voiceAnimProgress.finish()
                    }
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            item.pasuePosition = 1
                            voiceProgress.finish()
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            playBean = null
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    item.pasuePosition = 0
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    download(helper, item)
                }
                helper.getView(R.id.tv_Count).setOnClickListener {
                    startActivity(Intent(this@DynamicDetailsActivity, TalkListActivity::class.java)
                            .putExtra("voice_id", voiceBean.voice_id)
                            .putExtra("chat_id", item.chat_id.toString())
                            .putExtra("uid", voiceBean.user.id)
                            .putExtra("talkId", if (item.from_user_id.toString() == userinfo?.data?.user_id) "" else item.from_user_id.toString())
                    )
                }
                helper.getView(R.id.relative_Echos).setOnClickListener {
                    /**
                     * 点击录音回声
                     */
                    comment = item
                    if (userinfo?.data?.user_id == voiceBean.user_id.toString()) {//是当前用户的声兮,查询对方的隐私设置
                        querySetting(item)
                    } else
                        queryTalk(item)
                }
                helper.getView(R.id.iv_img).setOnClickListener {
                    UserDetailsActivity.start(this@DynamicDetailsActivity, item.user.avatar_url, item.user.id, it as ImageView)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: DynamicTalkData.DynamicTalkBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    if (item.dialog_num == 1)
                        clearRead(item.chat_id, item.dialog_id)
                    val file = getDownFilePath(item.resource_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@DynamicDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@DynamicDetailsActivity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@DynamicDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            (helper.getView(R.id.voiceAnimProgress) as VoiceAnimaProgress).finish()
                            item.isPlaying = false
                            progressHandler.stop()
                        }

                        override fun onInterrupt() {
                            (helper.getView(R.id.voiceAnimProgress) as VoiceAnimaProgress).finish()
                            item.isPlaying = false
                            progressHandler.stop()
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            playBean = item
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
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
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.voiceAnimProgress) as VoiceAnimaProgress
                        voiceProgress.changeProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_dynamic_loadmore, recyclerView, false))
        setItemOperator(this)
        request(0)
    }

    /**
     * 更新
     */
    private fun clearRead(chatId: String, dialog: String) {
        if (TextUtils.isEmpty(chatId)) {
            return
        }
        OkClientHelper.patch(this, "chats/$chatId/$dialog", FormBody.Builder().add("readAt", (System.currentTimeMillis() / 1000).toString()).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun initEvent() {
        headView.iv_Close.setOnClickListener {
            headView.linear_Hint.visibility = View.GONE
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setBoolean(this, IConstant.COMMENTHINT + loginBean.user_id, true)
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(1)
        }
        adapter.setOnItemLongClickListener { _, position ->
            DialogReport(this).setIsReportNormal(true).setReportTitle(resources.getString(R.string.string_report_normal)).setDeleteStatus(true).setReportStatus(mData[position].user.id != loginBean.user_id).setOnClickListener(View.OnClickListener { view ->
                when (view.id) {
                    R.id.tv_Delete -> {
                        DialogDeleteConment(this).setOnClickListener(View.OnClickListener {
                            deleteComment(mData[position])
                        }).show()
                    }
                    R.id.tv_Report -> {
                        DialogNormalReport(this).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Attach -> {
                                    reportComment(mData[position].dialog_id, "1")
                                }
                                R.id.tv_Porn -> {
                                    reportComment(mData[position].dialog_id, "2")
                                }
                                R.id.tv_Junk -> {
                                    reportComment(mData[position].dialog_id, "3")
                                }
                                R.id.tv_illegal -> {
                                    reportComment(mData[position].dialog_id, "4")
                                }
                            }
                        }).show()
                    }
                }
            }).show()
        }
        headView.linearStatusText.setOnClickListener {
            if (headView.linearStatusText.isSelected) {
                /**
                 * 请求加好友
                 */
                requestFriends(voiceBean, it)
            }
        }
        /**
         * 共享/共鸣
         */
        headView.relativeEcho.setOnClickListener {
            if (!TextUtils.isEmpty(voiceBean.is_shared)) {
                queryCount(voiceBean, headView.tv_Echo, headView.iv_Thumb)
            } else {
                if (voiceBean.isNetStatus) {
                    return@setOnClickListener
                }
                if (voiceBean.is_collected == 1) {//取消共鸣
//                    transLayout.showProgress()
                    voiceBean.isNetStatus = true
                    unThumb(voiceBean, headView.heartView)
                } else {//共鸣
                    transLayout.showProgress()
                    LightUtils.addItem(voiceBean.voice_id)
                    thumb(voiceBean, headView.heartView)
                }
            }
        }
        /**
         * 更多
         */
        headView.relativeShare.setOnClickListener {
            if (!TextUtils.isEmpty(voiceBean.is_shared)) {
                DialogMore(this).setEditable(voiceBean.resource_id == "0").setPrivacyStatus(voiceBean.is_private).setOnClickListener(View.OnClickListener {
                    when (it.id) {
                        R.id.tv_ShareWechat -> {
                            DialogShare(this, true).setOnClickListener(View.OnClickListener { view ->
                                when (view.id) {
                                    R.id.linearWechat -> {
                                        ShareUtils.share(this, Wechat.NAME, voiceBean.voice_url, voiceBean.share_url, "", voiceBean.user)
                                    }
                                    R.id.linearMoment -> {
                                        ShareUtils.share(this, WechatMoments.NAME, voiceBean.voice_url, voiceBean.share_url, "", voiceBean.user)
                                    }
                                }
                            }).show()
                        }
                        R.id.tv_ShareWeibo -> {
                            ShareUtils.share(this, SinaWeibo.NAME, voiceBean.voice_url, voiceBean.share_url, "", voiceBean.user)
                        }
                        R.id.tv_ShareQQ -> {
                            DialogShare(this, false).setOnClickListener(View.OnClickListener { share ->
                                when (share.id) {
                                    R.id.linearQQ -> {
                                        ShareUtils.share(this, QQ.NAME, voiceBean.voice_url, voiceBean.share_url, "", voiceBean.user)
                                    }
                                    R.id.linearQzone -> {
                                        ShareUtils.share(this, QZone.NAME, voiceBean.voice_url, voiceBean.share_url, "", voiceBean.user)
                                    }
                                }
                            }).show()
                        }
                        R.id.tv_Delete -> {
                            DialogDeleteConment(this).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                delete(voiceBean)
                            }).show()
                        }
                        R.id.tv_Self -> {
                            if (voiceBean.is_private == 1) {
                                transLayout.showProgress()
                                setVoicePrivacy(voiceBean)
                            } else {
                                DialogPrivacy(this@DynamicDetailsActivity).setOnClickListener(View.OnClickListener {
                                    transLayout.showProgress()
                                    setVoicePrivacy(voiceBean)
                                }).show()
                            }
                        }
                        R.id.tv_add_album -> {
                            startActivity(Intent(this@DynamicDetailsActivity, DialogAddAlbumActivity::class.java).putExtra("voiceId", voiceBean.voice_id))
                        }
                        R.id.tvReEditVoice -> {
                            startActivity<EditSendAct>("data" to voiceBean)
                        }
                    }
                }).show()
            } else {
                DialogReport(this).setResource(voiceBean.resource_type, voiceBean.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                    when (report.id) {
                        R.id.tv_admin_setPrivacy -> {
                            dialogPwd = DialogCommitPwd(this@DynamicDetailsActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), voiceBean)
                            })
                            dialogPwd?.show()
                        }
                        R.id.tv_Report ->
                            DialogReportContent(this)
                                    .setOnResultListener(OnReportItemListener {
                                        transLayout.showProgress()
                                        report(voiceBean.voice_id, it, voiceBean.user.id)
                                    }).show()
                        R.id.tv_report_normal -> {
                            reportNormal { reportType ->
                                reportNormalItem(voiceBean.voice_id, reportType)
                            }
                        }
                        R.id.tv_Follow -> {
                            if (voiceBean.subscription_id == 0) {
                                addSubscriber(voiceBean)
                            } else {
                                DialogGraffiti(this@DynamicDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (voiceBean.resource_type) {
                                    1 -> resources.getString(R.string.string_follow_movies)
                                    2 -> resources.getString(R.string.string_follow_book)
                                    3 -> resources.getString(R.string.string_follow_song)
                                    else -> resources.getString(R.string.string_follow_movies)
                                }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    deletedSubscriber(voiceBean)
                                }).show()
                            }
                        }
                    }
                }).show()
            }
        }
        headView.voiceAnimProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    progressHandler.stop()
                    voiceBean.isPlaying = false
                    headView.voiceAnimProgress.finish()
                }
            }
            voiceBean.pasuePosition = 0
            download()
        }
        headView.voiceAnimProgress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean.isPlaying) {//当前正在播放
                    progressHandler.stop()
                    voiceBean.isPlaying = false
                    voiceBean.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    headView.voiceAnimProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    progressHandler.stop()
                }
            }
            download()
        }
        val seekProgress = headView.voiceAnimProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceBean.allDuration = audioPlayer.duration
                        progressHandler.stop()
                        audioPlayer.stop()
                        voiceBean.isPlaying = false
                        headView.voiceAnimProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                voiceBean.pasuePosition = (progress * voiceBean.allDuration).toInt()
                download()
            }
        })
        headView.imageGroup.setOnClickViewListener { position ->
            voiceBean.let {
                startActivity(Intent(this, ShowPicActivity::class.java)
                        .putExtra("index", position)
                        .putExtra("data", it.img_list))
                overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
        }
        headView.roundImg.setOnClickListener {
            voiceBean.let {
                startActivity(Intent(this, UserDetailsActivity::class.java)
                        .putExtra("url", it.user.avatar_url)
                        .putExtra("id", it.user.id)
                )
            }
        }
        headView.tv_Action.setOnClickListener {
            voiceBean.let {
                startActivity(Intent(this, TopicResultActivity::class.java)
                        .putExtra("tagId", it.topic_id.toString())
                        .putExtra("tag", it.topic_name)
                )
            }
        }
        headView.lineaer_Recommend.setOnClickListener {
            voiceBean.let {
                if (TextUtils.isEmpty(it.is_shared)) {
                    if (it.dialog_num == 0) {
                        queryPermission(voiceBean, transLayout, AppTools.fastJson(uid, 1, voiceId))
                    } else {
                        if (it.dialog_num > 0) {
                            //跳转到对应用户的对话界面
                            startActivity(Intent(this@DynamicDetailsActivity, TalkListActivity::class.java)
                                    .putExtra("voice_id", voiceBean.voice_id)
                                    .putExtra("chat_id", voiceBean.chat_id.toString())
                                    .putExtra("uid", voiceBean.user.id)
                                    .putExtra("talkId", "")
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createAnim(isStart: Boolean) {
        if (isStart) {
            anim?.let {
                it.cancel()
                anim = null
            }
            anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1500)
            anim!!.interpolator = LinearInterpolator()
            anim!!.repeatCount = ValueAnimator.INFINITE
            anim!!.addUpdateListener {
                val value = it.animatedValue as Float
                if (audioPlayer.isPlaying) {
                    headView.heartView.waveShiftRatio = value
                }
            }
            anim!!.start()
        } else {
            anim?.let {
                it.cancel()
                anim = null
            }
        }
    }

    /**
     * 下载资源
     */
    private fun download() {
        try {
            if (TextUtils.isEmpty(voiceBean.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            findViewById<View>(R.id.play).isSelected = !findViewById<View>(R.id.play).isSelected
            val file = getDownFilePath(voiceBean.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this, voiceBean.voice_url, { o ->
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
                    headView.voiceAnimProgress.finish()
                    voiceBean.isPlaying = false
                    progressHandler.stop()
                    if (TextUtils.isEmpty(voiceBean.is_shared) && voiceBean.is_collected != 1 && LightUtils.contains(voiceBean.voice_id)) {
                        //自动共鸣
                        voiceBean.is_collected = 1
                        thumb(voiceBean, null, 5)
                    }
                    createAnim(false)
                    headView.heartView.end()
                }

                override fun onInterrupt() {
                    headView.voiceAnimProgress.finish()
                    voiceBean.isPlaying = false
                    createAnim(false)
                    headView.heartView.end()
                    progressHandler.stop()
                }

                override fun onPrepared() {
                    voiceBean.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(voiceBean.pasuePosition)
                    voiceBean.pasuePosition = 0
                    if (LightUtils.contains(voiceBean.voice_id) && TextUtils.isEmpty(voiceBean.is_shared))
                        createAnim(true)
                    addPlays(voiceBean, headView.tv_Sub) {}
                    isPlayed = true
                    voiceBean.isPlaying = true
                    progressHandler.start()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "voices/$uid/$voiceId?recognition=1", DynamicDatailData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as DynamicDatailData
                        if (result.code == 0) {
                            voiceBean = result.data
                            glideUtil.loadGlide(voiceBean.user.avatar_url, headView.roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(voiceBean.user.avatar_url))
                            headView.tv_UserName.text = result.data.user.nick_name
                            headView.tvTime.text = TimeUtils.getInstance().paserFriends(this@DynamicDetailsActivity, voiceBean.created_at)
                            headView.tv_Action.text = if (TextUtils.isEmpty(voiceBean.topic_name)) "" else "#${voiceBean.topic_name}#"
                            try {
                                headView.tv_Sub.text = if (voiceBean.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${voiceBean.played_num}"
                            } catch (e: Exception) {
                                headView.tv_Sub.text = resources.getString(R.string.string_Listener)
                            }
                            headView.iv_user_type.visibility = if (result.data.user.identity_type == 0) View.GONE else View.VISIBLE
                            headView.iv_user_type.isSelected = result.data.user.identity_type == 1
                            if (!TextUtils.isEmpty(voiceBean.is_shared)) {
                                headView.tv_Sub.visibility = if (voiceBean.is_private == 1) View.GONE else View.VISIBLE
                                headView.iv_Privacy.visibility = if (voiceBean.is_private == 1) View.VISIBLE else View.GONE
                                headView.linearStatusText.visibility = View.GONE
                            } else {
                                headView.tv_Sub.visibility = View.GONE
                                headView.iv_Privacy.visibility = View.GONE
                                when (voiceBean.friend_status) {
                                    2 -> //好友
                                        headView.linearStatusText.visibility = View.GONE
                                    1 -> {
                                        headView.linearStatusText.visibility = View.VISIBLE
                                        headView.linearStatusText.isSelected = false
                                    }
                                    0 -> {
                                        headView.linearStatusText.visibility = View.VISIBLE
                                        headView.linearStatusText.isSelected = true
                                    }
                                }
                            }
                            headView.voiceAnimProgress.data = voiceBean
                            headView.imageGroup.setData(voiceBean.img_list)
                            headView.heartView.attachBean(voiceBean)
                            if (!TextUtils.isEmpty(voiceBean.is_shared)) {
                                headView.tv_Recommend.text = resources.getString(R.string.string_echoing) + if (voiceBean.chat_num == 0) "" else voiceBean.chat_num
                                headView.iv_Thumb.visibility = View.VISIBLE
                                if (voiceBean.is_shared == "1") {
                                    headView.tv_Echo.text = resources.getString(R.string.string_unshare_world)
                                } else {
                                    headView.tv_Echo.text = resources.getString(R.string.string_share_world)
                                }
                                headView.iv_Thumb.isSelected = voiceBean.is_shared == "1"
                                headView.heartView.visibility = View.GONE
                            } else {
                                headView.heartView.visibility = View.VISIBLE
                                headView.tv_Echo.text = resources.getString(R.string.string_gongming)
                                if (voiceBean.dialog_num <= 0) {
                                    headView.tv_Recommend.text = resources.getString(R.string.string_echoing)
                                } else {
                                    headView.tv_Recommend.text = resources.getString(R.string.string_Talks) + if (voiceBean.dialog_num == 0) "" else voiceBean.dialog_num
                                }
                            }
                            voiceBean.resource?.let {
                                if (!TextUtils.isEmpty(it.id)) {
                                    headView.itemDynamic.visibility = View.VISIBLE
                                    headView.itemDynamic.setData(it, voiceBean.resource_type, voiceBean.user_score)
                                } else {
                                    headView.itemDynamic.visibility = View.GONE
                                }
                            }
                            headView.ivOfficial.visibility = if (voiceBean.user_id == 1) View.VISIBLE else View.GONE
                            request(1)
                        } else {
                            transLayout.showEmpty()
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showOffline()
                    }
                })
            }
            else -> {
                transLayout.showProgress()
                mData.clear()
                adapter.notifyDataSetChanged()
                OkClientHelper.get(this, "voices/$voiceId/chats?lastId=$lastId", DynamicTalkData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as DynamicTalkData
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.data != null) {
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                                lastId = bean.chat_id.toString()
                            }
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        }
                        if (mData.size == 0) {
                            headView.tv_Hint_Echoes.visibility = View.GONE
                            headView.linear_Hint.visibility = View.GONE
                            if (loginBean.user_id == uid && voiceBean.is_shared == "0") {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                            }
                        } else {
                            try {
                                headView.tv_Hint_Echoes.visibility = View.VISIBLE
                                if (loginBean.user_id == uid) {
                                    headView.linear_Hint.visibility = if (SPUtils.getBoolean(this@DynamicDetailsActivity, IConstant.COMMENTHINT + loginBean.user_id, false)) View.GONE else View.VISIBLE
                                    headView.tv_Recommend.text = resources.getString(R.string.string_echoing) + mData.size
                                }
                            } catch (e: Exception) {
                            }
                        }
                        if (result.code != 0) {
                            showToast(result.msg)
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
        }
    }

    private fun reportComment(id: String, type: String) {
        val formBody = FormBody.Builder()
                .add("reportType", type)
                .add("resourceType", "3")
                .add("resourceId", id)
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
//                    showToast(resources.getString(R.string.string_report_success))
                    DialogReportSuccess(this@DynamicDetailsActivity).show()
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 查询用户的隐私设置
     */
    private fun querySetting(item: DynamicTalkData.DynamicTalkBean) {
        transLayout.showProgress()
        OkClientHelper.get(this, "users/${item.user.id}/setting/chatHobby", PatchData::class.java, object : OkResponse {
            override fun success(result: Any) {
                result as PatchData
                if (result.code == 0) {
                    val recordIntent = Intent(this@DynamicDetailsActivity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", item.user.avatar_url)
                            .putExtra("sendPath", AppTools.fastJson(if (comment?.from_user_id.toString() == userinfo?.data?.user_id) voiceBean.user_id.toString()
                            else comment?.from_user_id, 1, voiceId))
                    if (result.data != null) {
                        recordIntent.putExtra("isBusy", result.data.auto_reply)
                                .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                    }
                    startActivityForResult(recordIntent, REQUEST_ECHOES)
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

    /**
     *查询是否可以对话
     */
    private fun queryTalk(item: DynamicTalkData.DynamicTalkBean) {
        transLayout.showProgress()
        OkClientHelper.get(this, "chats/check/$uid/$voiceId", ChechOutReplyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as ChechOutReplyData).code == 0) {
                    startActivityForResult(Intent(this@DynamicDetailsActivity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", item.user.avatar_url)
                            .putExtra("sendPath", AppTools.fastJson(uid, 1, voiceId))
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

    private fun deleteComment(bean: DynamicTalkData.DynamicTalkBean) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "chats/${bean.chat_id}", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                    /**
                     * 更新数量
                     */
                    voiceBean.chat_num--
                    headView.tv_Recommend.text = resources.getString(R.string.string_echoing) + if (voiceBean.chat_num == 0) "" else voiceBean.chat_num
                    showToast("删除成功")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ECHOES) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(if (comment?.from_user_id.toString() == userinfo?.data?.user_id) voiceBean.user_id.toString()
                    else comment?.from_user_id, 1, voiceId, voice, voiceLength)))
                }
            } else if (requestCode == REQUEST_RECORD) {
                /**
                 * 创建回声
                 */
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(uid, 1, voiceId, voice, voiceLength)))
                }
            }
        }
    }

    override fun onDelete(bean: BaseBean?) {
        showToast("删除成功")
        EventBus.getDefault().post(OperatorVoiceListEvent(3, voiceId))
        finish()
    }

    override fun onRecommend(bean: BaseBean?) {
        val event = EventVoiceBean(voiceId, 1, voiceBean.created_at)
        event.isShare = "1"
        EventBus.getDefault().post(event)
    }

    override fun onUnRecommend(bean: BaseBean?) {
        val event = EventVoiceBean(voiceId, 1, voiceBean.created_at)
        event.isShare = "0"
        EventBus.getDefault().post(event)
    }

    override fun onthumb(bean: BaseBean?) {
        val event = EventVoiceBean(voiceId, 2, voiceBean.created_at)
        event.isCollected = 1
        EventBus.getDefault().post(event)
        transLayout.showContent()
    }

    override fun onUnThumb(bean: BaseBean?) {
        val event = EventVoiceBean(voiceId, 2, voiceBean.created_at)
        event.isCollected = 0
        EventBus.getDefault().post(event)
        transLayout.showContent()
    }

    override fun onFailure(e: Any?) {
        if (e is String)
            showToast(e.toString())

        transLayout.showContent()
    }

    override fun onComment(from_id: String?) {
        /**
         * 评论成功  刷新列表
         */
        voiceBean.dialog_num = 1
        headView.tv_Recommend.text = resources.getString(R.string.string_Talks) + 1

        val event = EventVoiceBean(voiceId, 4, voiceBean.created_at)
        event.dialogNum = 1
        EventBus.getDefault().post(event)

        request(1)
    }

    override fun onReport(type: String) {
        transLayout.showContent()
        DialogPBBlack(this).setType(type).show()
    }

    override fun onPrivacy(bean: BaseBean?) {
        /**
         * 隐私设置
         */
        if (bean?.is_private == 0) {
            headView.tv_Sub.visibility = View.VISIBLE
            headView.iv_Privacy.visibility = View.GONE
        } else {
            headView.tv_Sub.visibility = View.GONE
            headView.iv_Privacy.visibility = View.VISIBLE
        }

        val event = EventVoiceBean(voiceId, 3, voiceBean.created_at)
        event.isPrivacy = bean?.is_private!!
        EventBus.getDefault().post(event)
        headView.iv_Thumb.isSelected = false
        transLayout.showContent()
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        finish()
    }

    override fun onAdminFail() {
        dialogPwd?.setCallBack()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            audioPlayer.stop()
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            headView.voiceAnimProgress.data?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    headView.voiceAnimProgress.finish()
                    progressHandler.stop()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            if (event.voiceId == voiceId) {

                if (mData.size == 0) {//数据为空的时候
                    voiceBean.dialog_num = 1
                    headView.tv_Recommend.text = resources.getString(R.string.string_Talks) + 1

                    val event = EventVoiceBean(voiceId, 4, voiceBean.created_at)
                    event.dialogNum = 1
                    EventBus.getDefault().post(event)

                    request(1)
                } else {
                    var isContains = false
                    for (bean in mData) {
                        if (bean.chat_id.toString() == event.chatId) {//列表中有该条数据
                            bean.dialog_num++
                            bean.chat_id = event.chatId
                            adapter.notifyItemChanged(mData.indexOf(bean) + 1)
                            isContains = true

                            val event = EventVoiceBean(voiceId, 4, voiceBean.created_at)
                            event.dialogNum = bean.dialog_num
                            EventBus.getDefault().post(event)

                            break
                        }
                    }
                    if (!isContains) {
                        lastId = null
                        request(1)
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun topicChangeEvent(event: ChangeTopicInfoEvent) {
        if (voiceId == event.voiceId) {
            try {
                voiceBean.topic_name = event.topicName
                voiceBean.topic_id = event.topicId.toInt()
                headView.tv_Action.text = if (TextUtils.isEmpty(voiceBean.topic_name)) "" else "#${voiceBean.topic_name}#"
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        audioPlayer.stop()
    }
}