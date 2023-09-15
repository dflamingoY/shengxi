package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.Message
import android.os.Vibrator
import android.text.TextUtils
import android.view.View
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import kotlinx.android.synthetic.main.activity_shack.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.DynamicDatailData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.IOException

/**
 * 摇一摇 嗨冰果
 */
class ShackActivity : BaseAct(), ItemOperator {
    private var mShakeListener: ShakeDetector.OnShakeListener? = null
    private lateinit var mShakeDetector: ShakeDetector
    //音频池
    private lateinit var mSoundPool: SoundPool
    //音频流
    private var hitOkSfx: Int = 0

    private lateinit var vibrator: Vibrator
    /**
     * 是否摇动  限制頻繁的搖動
     */
    private var isShark = true
    private var voiceBean: BaseBean? = null
    private lateinit var audioPlayer: AudioPlayer
    private var isShaked = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_shack
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
                voiceBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                voiceBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                voiceBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                voiceBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    val helperHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {
        override fun handleMessage(msg: Message?) {
            if (voiceBean!!.isPlaying) {
                voiceAnimProgress.updateProgress(audioPlayer.currentPosition.toInt())
            }
        }
    }

    override fun initView() {
        transLayout.showEmpty()
    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            tv_shack_hint_1.text = resources.getString(R.string.string_shack_e_1)
            tv_test.text = resources.getString(R.string.string_test_button_1_e)
        }
        sendObserver()
        audioPlayer = AudioPlayer(this)
        setItemOperator(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mSoundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 5)
        hitOkSfx = mSoundPool.load(this, R.raw.shake_sound_male, 0)
        mShakeDetector = ShakeDetector(this)
        mShakeListener = ShakeDetector.OnShakeListener {
            if (isShark) {
                mSoundPool.play(hitOkSfx, 1f, 1f, 0, 0, 1f)
                vibrator.vibrate(200)
                //請求網絡
                audioPlayer.stop()
                helperHandler.stop()
                request(0)
                isShark = false
                helperHandler.postDelayed({
                    isShark = true
                }, 1500)
            }
        }

        mShakeDetector.registerOnShakeListener(mShakeListener)
    }

    override fun onPause() {
        super.onPause()
        try {
            mShakeDetector.stop()
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            /**
             * 判断是否完成测一测
             */
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            val result = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
            if (TextUtils.isEmpty(result)) {
                transLayout.showContent()
                relative_need_test.visibility = View.VISIBLE
            } else {
                relative_need_test.visibility = View.GONE
                if (voiceBean == null) {
                    transLayout.showEmpty()
                } else {
                    transLayout.showContent()
                }
                mShakeDetector.start()
            }
        } catch (e: Exception) {

        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_test.setOnClickListener {
            startActivity(Intent(this, PersonalityActivity::class.java)
                    .putExtra("isShack", true)
            )
        }
        linearStatusText.setOnClickListener {
            if (linearStatusText.isSelected) {
                /**
                 * 请求加好友
                 */
                requestFriends(voiceBean!!, it)
            }
        }
        /**
         * 共享/共鸣
         */
        relativeEcho.setOnClickListener {
            if (!TextUtils.isEmpty(voiceBean!!.is_shared)) {
                queryCount(voiceBean!!, tv_Echo, iv_Thumb)
            } else {
                if (voiceBean!!.is_collected == 1) {//取消共鸣
                    transLayout.showProgress()
                    unThumb(voiceBean!!, iv_Thumb)
                } else {//共鸣
                    transLayout.showProgress()
                    thumb(voiceBean!!, iv_Thumb)
                }
            }
        }
        /**
         * 更多
         */
        relativeShare.setOnClickListener {
            if (null != voiceBean && !TextUtils.isEmpty(voiceBean!!.is_shared)) {
                DialogMore(this).setPrivacyStatus(voiceBean?.is_private!!).setOnClickListener(View.OnClickListener {
                    when (it.id) {
                        R.id.tv_ShareWechat -> {
                            DialogShare(this, true).setOnClickListener(View.OnClickListener { share ->
                                when (share.id) {
                                    R.id.linearWechat -> {
                                        ShareUtils.share(this, Wechat.NAME, voiceBean?.voice_url, voiceBean?.share_url, "", voiceBean?.user)
                                    }
                                    R.id.linearMoment -> {
                                        ShareUtils.share(this, WechatMoments.NAME, voiceBean?.voice_url, voiceBean?.share_url, "", voiceBean?.user)
                                    }
                                }
                            }).show()
                        }
                        R.id.tv_ShareWeibo -> {
                            ShareUtils.share(this, SinaWeibo.NAME, voiceBean?.voice_url, voiceBean?.share_url, "", voiceBean?.user)
                        }
                        R.id.tv_ShareQQ -> {
                            DialogShare(this, false).setOnClickListener(View.OnClickListener { share ->
                                when (share.id) {
                                    R.id.linearQQ -> {
                                        ShareUtils.share(this, QQ.NAME, voiceBean?.voice_url, voiceBean?.share_url, "", voiceBean?.user)
                                    }
                                    R.id.linearQzone -> {
                                        ShareUtils.share(this, QZone.NAME, voiceBean?.voice_url, voiceBean?.share_url, "", voiceBean?.user)
                                    }
                                }
                            }).show()
                        }
                        R.id.tv_Delete -> {
                            DialogDeleteConment(this).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                delete(voiceBean!!)
                            }).show()
                        }
                        R.id.tv_Self -> {
                            transLayout.showProgress()
                            if (voiceBean?.is_private == 1) {
                                setVoicePrivacy(voiceBean!!)
                            } else {
                                DialogPrivacy(this).setOnClickListener(View.OnClickListener {
                                    transLayout.showProgress()
                                    setVoicePrivacy(voiceBean!!)
                                }).show()
                            }
                        }
                        R.id.tv_add_album -> {
                            startActivity(Intent(this@ShackActivity, DialogAddAlbumActivity::class.java).putExtra("voiceId", voiceBean!!.voice_id))
                        }
                    }
                }).show()
            } else {
                DialogReport(this).setResource(voiceBean!!.resource_type, voiceBean!!.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                    when (report.id) {
                        R.id.tv_admin_setPrivacy -> {
                            dialogPwd = DialogCommitPwd(this@ShackActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), voiceBean!!)
                            })
                            dialogPwd?.show()
                        }
                        R.id.tv_Report ->
                            DialogReportContent(this)
                                    .setOnResultListener(OnReportItemListener {
                                        transLayout.showProgress()
                                        report(voiceBean!!.voice_id, it, voiceBean!!.user.id)
                                    }).show()
                        R.id.tv_report_normal -> {
                            DialogNormalReport(this).setOnClickListener(View.OnClickListener { report ->
                                when (report.id) {
                                    R.id.tv_Attach -> {
                                        reportNormalItem(voiceBean!!.voice_id, "1")
                                    }
                                    R.id.tv_Porn -> {
                                        reportNormalItem(voiceBean!!.voice_id, "2")
                                    }
                                    R.id.tv_Junk -> {
                                        reportNormalItem(voiceBean!!.voice_id, "3")
                                    }
                                    R.id.tv_illegal -> {
                                        reportNormalItem(voiceBean!!.voice_id, "4")
                                    }
                                }
                            }).show()
                        }
                        R.id.tv_Follow -> {
                            if (voiceBean!!.subscription_id == 0) {
                                addSubscriber(voiceBean!!)
                            } else {
                                DialogGraffiti(this@ShackActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (voiceBean!!.resource_type) {
                                    1 -> resources.getString(R.string.string_follow_movies)
                                    2 -> resources.getString(R.string.string_follow_book)
                                    3 -> resources.getString(R.string.string_follow_song)
                                    else -> resources.getString(R.string.string_follow_movies)
                                }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    deletedSubscriber(voiceBean!!)
                                }).show()
                            }
                        }
                    }
                }).show()
            }
        }
        voiceAnimProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    helperHandler.stop()
                    voiceBean?.isPlaying = false
                    voiceBean?.pasuePosition = 1
                    voiceAnimProgress.finish()
                }
            }
            voiceBean?.pasuePosition = 0
            download()
        }
        val seekProgress = voiceAnimProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        voiceAnimProgress.setOnClickListener {
            sendObserver()

            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    helperHandler.stop()
                    voiceBean!!.isPlaying = false
                    voiceBean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    voiceAnimProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    helperHandler.stop()
                }
            }
            voiceBean?.isPlaying = false
            download()
        }
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceBean!!.allDuration = audioPlayer.duration
                        helperHandler.stop()
                        audioPlayer.stop()
                        voiceBean!!.isPlaying = false
                        voiceAnimProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                voiceBean!!.pasuePosition = (progress * voiceBean!!.allDuration).toInt()
                download()
            }
        })
        imageGroup.setOnClickViewListener { position ->
            voiceBean?.let {
                startActivity(Intent(this, ShowPicActivity::class.java)
                        .putExtra("index", position)
                        .putExtra("data", it.img_list))
                overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
        }
        roundImg.setOnClickListener {
            voiceBean?.let {
                startActivity(Intent(this, UserDetailsActivity::class.java)
                        .putExtra("url", it.user.avatar_url)
                        .putExtra("id", it.user.id)
                )
            }
        }
        tv_Action.setOnClickListener {
            voiceBean?.let {
                startActivity(Intent(this, TopicResultActivity::class.java)
                        .putExtra("tagId", it.topic_id.toString())
                        .putExtra("tag", it.topic_name)
                )
            }
        }
        lineaer_Recommend.setOnClickListener {
            voiceBean?.let {
                if (!TextUtils.isEmpty(it.is_shared)) {
                    startActivity(Intent(this, DynamicDetailsActivity::class.java)
                            .putExtra("id", it.voice_id)
                            .putExtra("uid", it.user.id)
                            .putExtra("isExpend", it.chat_num > 0)
                    )
                } else {
                    if (it.dialog_num == 0) {
                        queryPermission(it, transLayout, AppTools.fastJson(it.user_id.toString(), 1, it.voice_id))
                    } else {
                        startActivity(Intent(this, TalkListActivity::class.java)
                                .putExtra("voice_id", it.voice_id)
                                .putExtra("chat_id", it.chat_id)
                                .putExtra("uid", it.user_id.toString())
                        )
                    }
                }
            }
        }
    }

    /**
     * 下载资源
     */
    private fun download() {
        try {
            if (TextUtils.isEmpty(voiceBean?.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
                return
            }
            findViewById<View>(R.id.play).isSelected = !findViewById<View>(R.id.play).isSelected
            val file = getDownFilePath(voiceBean!!.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this, voiceBean?.voice_url, { o ->
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
                    voiceAnimProgress.finish()
                    voiceBean?.isPlaying = false
                    helperHandler.stop()
                }

                override fun onInterrupt() {
                    voiceAnimProgress.finish()
                    voiceBean?.isPlaying = false
                    helperHandler.stop()
                }

                override fun onPrepared() {
                    addPlays(voiceBean!!, tv_Sub){}
                    audioPlayer.seekTo(voiceBean?.pasuePosition!!)
                    voiceBean!!.pasuePosition = 0
                    voiceBean?.isPlaying = true
                    helperHandler.start()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "voices/shake?resetShaked=$isShaked", DynamicDatailData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as DynamicDatailData
                if (result.code == 0) {
                    transLayout.showContent()
                    if (result.data != null) {
                        isShaked = 0
                        voiceBean = result.data
                        glideUtil.loadGlide(voiceBean!!.user.avatar_url, roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(voiceBean!!.user.avatar_url))
                        tv_UserName.text = result.data.user.nick_name
                        tvTime.text = TimeUtils.getInstance().paserFriends(this@ShackActivity, result.data.created_at)
                       /* if (result.data.may_interested == 1) {
                            tv_Interested.visibility = View.VISIBLE
                            when {
                                result.data.user_gender == 1 -> tv_Interested.isSelected = true
                                result.data.user_gender == 2 -> tv_Interested.isSelected = false
                                else -> {
                                    tv_Interested.visibility = View.GONE
                                }
                            }
                        } else {
                            tv_Interested.visibility = View.GONE
                        }*/
                        iv_user_type.visibility = if (result.data.user.identity_type == 0) View.GONE else View.VISIBLE
                        iv_user_type.isSelected = result.data.user.identity_type == 1
                        tv_Action.text = if (TextUtils.isEmpty(result.data.topic_name)) "" else "#${result.data.topic_name}#"
                        try {
                            tv_Sub.text = if (result.data.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${result.data?.played_num}"
                        } catch (e: Exception) {
                            tv_Sub.text = resources.getString(R.string.string_Listener)
                        }
                        if (!TextUtils.isEmpty(result.data.is_shared)) {
                            tv_Sub.visibility = View.VISIBLE
                            linearStatusText.visibility = View.GONE
                        } else {
                            tv_Sub.visibility = View.GONE
                            when {
                                result.data.friend_status == 2 -> //好友
                                    linearStatusText.visibility = View.GONE
                                result.data.friend_status == 1 -> {
                                    linearStatusText.visibility = View.VISIBLE
                                    linearStatusText.isSelected = false
                                }
                                result.data.friend_status == 0 -> {
                                    linearStatusText.visibility = View.VISIBLE
                                    linearStatusText.isSelected = true
                                }
                            }
                        }
                        voiceAnimProgress.data = result.data
                        imageGroup.setData(result.data.img_list)
                        if (!TextUtils.isEmpty(result.data.is_shared)) {
                            iv_Thumb.isSelected = result.data.is_shared == "1"
                            tv_Recommend.text = resources.getString(R.string.string_echoing) + if (result.data.chat_num == 0) "" else result.data.chat_num
                            iv_Thumb.setImageResource(R.drawable.selector_share_world)
                            if (result.data.is_shared == "1") {
                                tv_Echo.text = resources.getString(R.string.string_unshare_world)
                            } else {
                                tv_Echo.text = resources.getString(R.string.string_share_world)
                            }
                        } else {
                            iv_Thumb.isSelected = result.data.is_collected == 1
                            tv_Echo.text = resources.getString(R.string.string_gongming)
                            if (result.data.dialog_num <= 0) {
                                tv_Recommend.text = resources.getString(R.string.string_echoing)
                            } else {

                                tv_Recommend.text = resources.getString(R.string.string_Talks) + if (result.data.dialog_num == 0) "" else result.data.dialog_num
                            }
                            iv_Thumb.setImageResource(R.drawable.selector_thumb)
                        }
                        if (null == result.data.resource) {
                            itemDynamic.visibility = View.GONE
                        }
                        result.data?.resource?.let {
                            if (!TextUtils.isEmpty(it.id)) {
                                itemDynamic.visibility = View.VISIBLE
                                itemDynamic.setData(it, result.data.resource_type, result.data.user_score)
                            } else {
                                itemDynamic.visibility = View.GONE
                            }
                        }

                    } else {
                        transLayout.showEmpty()
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showOffline()
            }
        }, "V3.3")
    }

    override fun onDelete(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onRecommend(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onUnRecommend(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onthumb(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onUnThumb(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onFailure(e: Any?) {
        transLayout.showContent()
    }

    @SuppressLint("SetTextI18n")
    override fun onComment(from_id: String?) {
        voiceBean?.let {
            it.dialog_num = 1
            it.chat_id = from_id
            tv_Recommend.text = resources.getString(R.string.string_Talks) + it.dialog_num
        }
        transLayout.showContent()
    }

    override fun onReport(type: String) {
        transLayout.showContent()
        DialogPBBlack(this).setType(type).show()
    }

    override fun onFriend() {
        transLayout.showContent()
    }

    override fun onPrivacy(bean: BaseBean?) {
        iv_Thumb.isSelected = false
        tv_Sub.visibility = if (bean?.is_private == 1) View.GONE else View.VISIBLE
        iv_Privacy.visibility = if (bean?.is_private == 1) View.VISIBLE else View.GONE
        transLayout.showContent()
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        /**
         * 摇一摇清空掉数据
         */
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        transLayout.showEmpty()
        voiceBean = null
    }

    override fun onAdminFail() {
        dialogPwd?.setCallBack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    voiceBean?.let {
                        EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(it.user_id.toString(), 1, it.voice_id, voice, voiceLength)))
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity)
            voiceBean?.let {
                if (it.isPlaying || it.isPause) {
                    it.isPlaying = false
                    audioPlayer.stop()
                    helperHandler.stop()
                }
            }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            voiceBean?.let {
                if (event.voiceId == it.voice_id) {
                    it.dialog_num++
                    tv_Recommend.text = resources.getString(R.string.string_Talks) + it.dialog_num
                    it.chat_id = event.chatId
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mShakeDetector.unregisterOnShakeListener(mShakeListener)
        mShakeDetector.removeListener()
        helperHandler.removeCallbacks(helperHandler)
        audioPlayer.stop()
    }

}