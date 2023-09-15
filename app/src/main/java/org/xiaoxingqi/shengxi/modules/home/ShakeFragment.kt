package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.os.Vibrator
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.frag_shake.view.*
import kotlinx.android.synthetic.main.frag_shake.view.heartView
import kotlinx.android.synthetic.main.frag_shake.view.imageGroup
import kotlinx.android.synthetic.main.frag_shake.view.itemDynamic
import kotlinx.android.synthetic.main.frag_shake.view.iv_Thumb
import kotlinx.android.synthetic.main.frag_shake.view.iv_user_type
import kotlinx.android.synthetic.main.frag_shake.view.lineaer_Recommend
import kotlinx.android.synthetic.main.frag_shake.view.linearStatusText
import kotlinx.android.synthetic.main.frag_shake.view.relativeEcho
import kotlinx.android.synthetic.main.frag_shake.view.relativeShare
import kotlinx.android.synthetic.main.frag_shake.view.roundImg
import kotlinx.android.synthetic.main.frag_shake.view.tv_Action
import kotlinx.android.synthetic.main.frag_shake.view.tv_Echo
import kotlinx.android.synthetic.main.frag_shake.view.tv_Recommend
import kotlinx.android.synthetic.main.frag_shake.view.tv_Sub
import kotlinx.android.synthetic.main.frag_shake.view.tv_UserName
import kotlinx.android.synthetic.main.frag_shake.view.voiceAnimProgress
import kotlinx.android.synthetic.main.head_dynamic.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.DynamicDatailData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.LightUtils
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.reportNormal
import org.xiaoxingqi.shengxi.modules.shareDialog
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.download.DownVoiceProxy
import org.xiaoxingqi.shengxi.wedgit.CircleCountDown
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.IOException

class ShakeFragment : BaseFragment(), HomeTabClick {

    private var mShakeListener: ShakeDetector.OnShakeListener? = null
    private lateinit var mShakeDetector: ShakeDetector
    private lateinit var vibrator: Vibrator
    private lateinit var audioPlayer: AudioPlayer
    private var voiceBean: BaseBean? = null
    private var isAutoPlay = true

    /**
     * 是否摇动  限制頻繁的搖動
     */
    private var isShark = true
    private var visiblePage = true
    private var anim: ValueAnimator? = null

    override fun currentPage(page: Int) {
        visiblePage = page == 1
        try {
            if (mShakeDetector != null) {
                if (visiblePage) {
                    mShakeDetector.start()
                } else
                    mShakeDetector.stop()
            }
        } catch (e: Exception) {
        }
        try {
            if (!visiblePage) {
                resetPage()
                stopCountDown()
            }
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (visiblePage) {
            try {
                mShakeDetector.start()
            } catch (e: Exception) {

            }
        }
    }

    override fun onPause() {
        super.onPause()
        mShakeDetector.stop()
//        stopCountDown()
//        resetPage()
    }

    private val helperHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            if (voiceBean!!.isPlaying) {
                mView!!.voiceAnimProgress.updateProgress(audioPlayer.currentPosition.toInt())
                mView!!.heartView.waterLevelRatio = audioPlayer.currentPosition / audioPlayer.duration.toFloat()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_shake
    }

    override fun initView(view: View?) {

    }

    override fun initData() {
        EventBus.getDefault().register(this)
        setOperator(object : ItemOperator {
            override fun onFriend() {
            }

            override fun onDelete(bean: BaseBean?) {
            }

            override fun onUnRecommend(bean: BaseBean?) {
            }

            override fun onReport(type: String?) {
                mView!!.transLayout.showContent()
                DialogPBBlack(activity!!).setType(type!!).show()
            }

            override fun onRecommend(bean: BaseBean?) {
            }

            override fun onPrivacy(bean: BaseBean?) {
            }

            override fun onAdminFail() {
                dialogPwd?.setCallBack()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
                mView!!.transLayout.showContent()
            }

            override fun onComment(from_id: String?) {
            }

            override fun onAdminPrivacy(bean: BaseBean?) {
                dialogPwd?.dismiss()
            }

            override fun onUnThumb(bean: BaseBean?) {
                mView!!.transLayout.showContent()
            }

            override fun onthumb(bean: BaseBean?) {
                mView!!.heartView.isSelected = bean?.is_collected == 1
                mView!!.transLayout.showContent()
            }
        })
        audioPlayer = AudioPlayer(activity)
        vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mShakeDetector = ShakeDetector(context)
        mShakeListener = ShakeDetector.OnShakeListener {
            if (isShark) {
//                isAutoPlay = false
                vibrator.vibrate(200)
                //請求網絡
                audioPlayer.stop()
                helperHandler.stop()
                shakeExecute()
            }
        }
        mShakeDetector.registerOnShakeListener(mShakeListener)
    }

    private fun shakeExecute() {
        request(0)
        isShark = false
        helperHandler.postDelayed({
            isShark = true
        }, 1500)
    }

    private fun stopCountDown() {
        try {
            mView!!.linearCountDown.visibility = View.GONE
            mView!!.circleCountDown.stop()
        } catch (e: Exception) {
        }
    }

    private fun resetPage() {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        mView!!.transLayout.showEmpty()
        mView!!.linearContainer.visibility = View.GONE
        try {
            mView!!.linearCountDown.visibility = View.GONE
            mView!!.circleCountDown.stop()
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                mView!!.voiceAnimProgress.finish()
                voiceBean?.isPlaying = false
                helperHandler.stop()
//                mView!!.linearCountDown.visibility = View.VISIBLE
//                mView!!.circleCountDown.start()
                //自动点亮
                createAnim(false)
                mView!!.heartView.end()
                if (LightUtils.contains(voiceBean!!.voice_id)) {
                    voiceBean!!.is_collected = 1
                    tuhmb(voiceBean!!, null, 5)
                }
            }

            override fun onInterrupt() {
                mView!!.voiceAnimProgress.finish()
                voiceBean?.isPlaying = false
                createAnim(false)
                mView!!.heartView.end()
                helperHandler.stop()
            }

            override fun onPrepared() {
                addPlays(voiceBean!!, mView!!.tv_Sub) {}
                voiceBean!!.allDuration = audioPlayer.duration
                audioPlayer.seekTo(voiceBean?.pasuePosition!!)
                voiceBean!!.pasuePosition = 0
                voiceBean?.isPlaying = true
                if (TextUtils.isEmpty(voiceBean!!.is_shared) && LightUtils.contains(voiceBean!!.voice_id)) {
                    createAnim(true)
                }
                helperHandler.start()
            }
        }
        mView!!.relativeCountDown.setOnClickListener {
            stopCountDown()
        }
        mView!!.circleCountDown.setOnAnimatorEndListener(object : CircleCountDown.OnAnimatorEndListener {
            override fun end() {
//                if (isShark) {
//                    isAutoPlay = true
//                    shakeExecute()
//                }
            }

            @SuppressLint("SetTextI18n")
            override fun timeBeat(time: Int) {
                mView!!.tvTimer.text = "$time${resources.getString(R.string.string_auto_play_next_shake)}"
            }

        })
        mView!!.linearStatusText.setOnClickListener {
            if (it.isSelected) {
                requestFriends(voiceBean!!, it)
            }
        }
        mView!!.relativeEcho.setOnClickListener {
            if (!TextUtils.isEmpty(voiceBean!!.is_shared)) {
                queryCount(voiceBean!!, mView!!.tv_Echo, mView!!.iv_Thumb)
            } else {
                if (voiceBean!!.isNetStatus) {
                    return@setOnClickListener
                }
                if (voiceBean!!.is_collected == 1) {//取消共鸣
                    voiceBean!!.isNetStatus = true
//                    mView!!.transLayout.showProgress()
                    unThumb(voiceBean!!, mView!!.heartView)
                } else {//共鸣
                    mView!!.transLayout.showProgress()
                    LightUtils.addItem(voiceBean!!.voice_id)
                    tuhmb(voiceBean!!, mView!!.heartView)
                }
            }
        }
        mView!!.relativeShare.setOnClickListener {
            stopCountDown()
            if (null != voiceBean && !TextUtils.isEmpty(voiceBean!!.is_shared)) {
                activity!!.shareDialog(voiceBean!!) { id ->
                    when (id) {
                        R.id.tv_Delete -> {
                            DialogDeleteConment(activity!!).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                mView!!.transLayout.showProgress()
                                delete(voiceBean!!)
                            }).show()
                        }
                        R.id.tv_Self -> {
                            if (voiceBean!!.is_private == 1) {
                                mView!!.transLayout.showProgress()
                                setVoicePrivacy(voiceBean!!)
                            } else {
                                DialogPrivacy(activity!!).setOnClickListener(View.OnClickListener {
                                    mView!!.transLayout.showProgress()
                                    setVoicePrivacy(voiceBean!!)
                                }).show()
                            }
                        }
                    }
                }
            } else {
                DialogReport(activity!!).setResource(voiceBean!!.resource_type, voiceBean!!.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                    when (report.id) {
                        R.id.tv_admin_setPrivacy -> {
                            dialogPwd = DialogCommitPwd(activity!!).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), voiceBean!!)
                            })
                            dialogPwd?.show()
                        }
                        R.id.tv_Report ->
                            DialogReportContent(activity!!)
                                    .setOnResultListener(OnReportItemListener {
                                        mView!!.transLayout.showProgress()
                                        report(voiceBean!!.voice_id, it, voiceBean!!.user.id)
                                    }).show()
                        R.id.tv_report_normal -> {
                            activity!!.reportNormal { reportType ->
                                reportNormalItem(voiceBean!!.voice_id, reportType)
                            }
                        }
                        R.id.tv_Follow -> {
                            if (voiceBean!!.subscription_id == 0) {
                                addSubscriber(voiceBean!!)
                            } else {
                                DialogGraffiti(activity!!).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (voiceBean!!.resource_type) {
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
        mView!!.voiceAnimProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    audioPlayer.stop()
                    helperHandler.stop()
                    voiceBean?.isPlaying = false
                    voiceBean?.pasuePosition = 1
                    mView!!.voiceAnimProgress.finish()
                }
            }
            voiceBean?.pasuePosition = 0
            down(voiceBean!!)
        }
        val seekProgress = mView!!.voiceAnimProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        mView!!.voiceAnimProgress.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceBean!!.isPlaying) {//当前正在播放
                    helperHandler.stop()
                    voiceBean!!.isPlaying = false
                    voiceBean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                    mView!!.voiceAnimProgress.finish()
                    return@setOnClickListener
                } else {
                    audioPlayer.stop()
                    helperHandler.stop()
                }
            }
            voiceBean?.isPlaying = false
            down(voiceBean!!)
        }
        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceBean!!.allDuration = audioPlayer.duration
                        helperHandler.stop()
                        audioPlayer.stop()
                        voiceBean!!.isPlaying = false
                        mView!!.voiceAnimProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                voiceBean!!.pasuePosition = (progress * voiceBean!!.allDuration).toInt()
                down(voiceBean!!)
            }
        })
        mView!!.imageGroup.setOnClickViewListener { position ->
            voiceBean?.let {
                startActivity(Intent(activity, ShowPicActivity::class.java)
                        .putExtra("index", position)
                        .putExtra("data", it.img_list))
                activity!!.overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
        }
        mView!!.roundImg.setOnClickListener {
            voiceBean?.let {
                startActivity(Intent(activity, UserDetailsActivity::class.java)
                        .putExtra("url", it.user.avatar_url)
                        .putExtra("id", it.user.id)
                )
            }
        }
        mView!!.tv_Action.setOnClickListener {
            voiceBean?.let {
                startActivity(Intent(activity, TopicResultActivity::class.java)
                        .putExtra("tagId", it.topic_id.toString())
                        .putExtra("tag", it.topic_name)
                )
            }
        }
        mView!!.lineaer_Recommend.setOnClickListener {
            voiceBean?.let {
                if (!TextUtils.isEmpty(it.is_shared)) {
                    startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                            .putExtra("id", it.voice_id)
                            .putExtra("uid", it.user.id)
                            .putExtra("isExpend", it.chat_num > 0)
                    )
                } else {
                    if (it.dialog_num == 0) {
                        queryPermission(it, mView!!.transLayout, AppTools.fastJson(it.user_id.toString(), 1, it.voice_id))
                    } else {
                        startActivity(Intent(activity, TalkListActivity::class.java)
                                .putExtra("voice_id", it.voice_id)
                                .putExtra("chat_id", it.chat_id)
                                .putExtra("uid", it.user_id.toString())
                        )
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
                    mView!!.heartView.waveShiftRatio = value
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

    private fun requestFriends(bean: BaseBean, view: View) {
        mView!!.transLayout.showProgress()
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
                                mView!!.transLayout.showProgress()
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
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }

    //资源下载
    private fun down(item: BaseBean) {
//        stopCountDown()
        try {
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            DownVoiceProxy.downProxy.clearFocus()
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downProxy(activity, item, {
                    audioPlayer.setDataSource(it)
                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                }, {
                    showToast(VolleyErrorHelper.getMessage(it))
                })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mShakeDetector.unregisterOnShakeListener(mShakeListener)
        mShakeDetector.removeListener()
        EventBus.getDefault().unregister(this)
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }

    override fun request(flag: Int) {
        mView!!.transLayout.showProgress()
        mView!!.linearCountDown.visibility = View.GONE
        mView!!.linearContainer.visibility = View.GONE
        OkClientHelper.get(activity, "voices/shake?recognition=1", DynamicDatailData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as DynamicDatailData
                if (result.code == 0) {
                    mView!!.transLayout.showContent()
                    if (result.data != null) {
                        voiceBean = result.data
                        mView!!.heartView.attachBean(voiceBean)
                        mView!!.linearContainer.visibility = View.VISIBLE
                        glideUtil.loadGlide(result.data.user.avatar_url, mView!!.roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(result.data.user.avatar_url))
                        mView!!.tv_UserName.text = result.data.user.nick_name
                        mView!!.iv_user_type.visibility = if (result.data.user.identity_type == 0) View.GONE else View.VISIBLE
                        mView!!.iv_user_type.isSelected = result.data.user.identity_type == 1
                        mView!!.tv_Action.text = if (TextUtils.isEmpty(result.data.topic_name)) "" else "#${result.data.topic_name}#"
                        mView!!.tv_Action.visibility = if (TextUtils.isEmpty(result.data.topic_name)) View.GONE else View.VISIBLE
                        mView!!.tv_UserName.textSize = if (TextUtils.isEmpty(result.data.topic_name)) 14f else 12f
                        try {
                            mView!!.tv_Sub.text = if (result.data.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${result.data?.played_num}"
                        } catch (e: Exception) {
                            mView!!.tv_Sub.text = resources.getString(R.string.string_Listener)
                        }
                        if (!TextUtils.isEmpty(result.data.is_shared)) {
                            mView!!.tv_Sub.visibility = View.VISIBLE
                            mView!!.linearStatusText.visibility = View.GONE
                        } else {
                            mView!!.tv_Sub.visibility = View.GONE
                            when (result.data.friend_status) {
                                2 -> //好友
                                    mView!!.linearStatusText.visibility = View.GONE
                                1 -> {
                                    mView!!.linearStatusText.visibility = View.VISIBLE
                                    mView!!.linearStatusText.isSelected = false
                                }
                                0 -> {
                                    mView!!.linearStatusText.visibility = View.VISIBLE
                                    mView!!.linearStatusText.isSelected = true
                                }
                            }
                        }
                        mView!!.voiceAnimProgress.data = result.data
                        mView!!.imageGroup.setData(result.data.img_list)
                        if (!TextUtils.isEmpty(result.data.is_shared)) {
                            mView!!.iv_Thumb.visibility = View.VISIBLE
                            mView!!.heartView.visibility = View.GONE
                            mView!!.iv_Thumb.isSelected = result.data.is_shared == "1"
                            mView!!.tv_Recommend.text = resources.getString(R.string.string_echoing) + if (result.data.chat_num == 0) "" else result.data.chat_num
                            if (result.data.is_shared == "1") {
                                mView!!.tv_Echo.text = resources.getString(R.string.string_unshare_world)
                            } else {
                                mView!!.tv_Echo.text = resources.getString(R.string.string_share_world)
                            }
                        } else {
                            mView!!.iv_Thumb.visibility = View.GONE
                            mView!!.heartView.visibility = View.VISIBLE
                            mView!!.tv_Echo.text = resources.getString(R.string.string_gongming)
                            if (result.data.dialog_num <= 0) {
                                mView!!.tv_Recommend.text = resources.getString(R.string.string_echoing)
                            } else {
                                mView!!.tv_Recommend.text = resources.getString(R.string.string_Talks) + if (result.data.dialog_num == 0) "" else result.data.dialog_num
                            }
                        }
                        if (null == result.data.resource) {
                            mView!!.itemDynamic.visibility = View.GONE
                        }
                        result.data?.resource?.let {
                            if (!TextUtils.isEmpty(it.id)) {
                                mView!!.itemDynamic.visibility = View.VISIBLE
                                mView!!.itemDynamic.setData(it, result.data.resource_type, result.data.user_score)
                            } else {
                                mView!!.itemDynamic.visibility = View.GONE
                            }
                        }
                        if (isAutoPlay)
                            down(voiceBean!!)
                    } else {
                        mView!!.transLayout.showEmpty()
                    }
                } else {
                    mView!!.transLayout.showEmpty()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showOffline()
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            voiceBean?.let {
                if (event.voiceId == it.voice_id) {
                    it.dialog_num++
                    mView!!.tv_Recommend.text = resources.getString(R.string.string_Talks) + it.dialog_num
                    it.chat_id = event.chatId
                }
            }
        } catch (e: Exception) {

        }
    }

}