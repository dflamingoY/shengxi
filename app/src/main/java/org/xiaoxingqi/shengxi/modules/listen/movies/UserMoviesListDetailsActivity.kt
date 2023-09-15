package org.xiaoxingqi.shengxi.modules.listen.movies

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_moive.*
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
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.LightUtils
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.isOffsetScreen
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.HeartWaveView
import org.xiaoxingqi.shengxi.wedgit.ItemDynamicView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.File
import java.io.IOException

class UserMoviesListDetailsActivity : BaseAct(), ItemOperator {

    private val mData by lazy { ArrayList<BaseBean>() }
    private lateinit var adapter: QuickAdapter<BaseBean>
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: BaseBean? = null
    private var lastId: String = ""
    private var movieId: String = ""
    private var userId: String? = null
    private var resourceType = 1//1 影评  2 书评 3 唱回忆
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
        return R.layout.activity_similar_white
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        resourceType = intent.getIntExtra("resourceType", 1)
        userId = intent.getStringExtra("uid")
        val title = intent.getStringExtra("title")
        when (resourceType) {
            1 -> {
                if (!TextUtils.isEmpty(title)) {
                    tv_Title.text = resources.getString(R.string.string_movies_other_comment)
                } else {
                    tv_Title.text = "我${resources.getString(R.string.string_movies_comment_1)}"
                }
            }
            2 -> {
                if (!TextUtils.isEmpty(title)) {
                    tv_Title.text = resources.getString(R.string.string_book_other_comment)
                } else {
                    tv_Title.text = "我${resources.getString(R.string.string_books_comment_1)}"
                }
            }
            3 -> {
                if (!TextUtils.isEmpty(title)) {
                    tv_Title.text = "ta的版本"
                } else {
                    tv_Title.text = "我的版本"
                }
            }
        }
        recyclerReview.layoutManager = LinearLayoutManager(this)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        movieId = intent.getStringExtra("id")
        setItemOperator(this)
        audioPlayer = AudioPlayer(this)
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_voice, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            private var anim: ValueAnimator? = null

            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                helper!!.getView(R.id.imageGroup).visibility = View.GONE
                helper.getView(R.id.itemDynamic).visibility = View.VISIBLE
                item?.resource?.let {
                    (helper.getView(R.id.itemDynamic) as ItemDynamicView).setData(it, item.resource_type, item.user_score)
                }
                glideUtil.loadGlide(item!!.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_UserName)?.text = item.user.nick_name
                helper.getTextView(R.id.tvTime)?.text = TimeUtils.getInstance().paserFriends(this@UserMoviesListDetailsActivity, item.created_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == 1) View.VISIBLE else View.GONE
                val position = helper.itemView.tag as Int
                helper.getView(R.id.viewLineDivide).visibility = if (position == 0) View.GONE else View.VISIBLE
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                if (!TextUtils.isEmpty(item.is_shared)) {
                    helper.getView(R.id.heartView).visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).isSelected = item.is_shared == "1"
                    helper.getView(R.id.tv_Sub).visibility = if (item.is_private == 1) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_Privacy).visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
                    val params = helper.getView(R.id.iv_Privacy).layoutParams
                    params.width = AppTools.dp2px(this@UserMoviesListDetailsActivity, 27)
                    params.height = AppTools.dp2px(this@UserMoviesListDetailsActivity, 27)
                    helper.getView(R.id.iv_Privacy).layoutParams = params
                    helper.getTextView(R.id.tv_Sub).text = if (item.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${item.played_num}"
                    if (item.is_shared == "1") {
                        helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_unshare_world)
                    } else {
                        helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_share_world)
                    }
                    helper.getTextView(R.id.tv_Recommend).text = resources.getString(R.string.string_echoing) + if (item.chat_num == 0) {
                        ""
                    } else {
                        " " + item.chat_num
                    }
                } else {
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    helper.getView(R.id.iv_Privacy).visibility = View.GONE
                    helper.getView(R.id.tv_Sub).visibility = View.GONE
                    helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_gongming)
                    if (item.dialog_num == 0) {
                        helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing)
                    } else {
                        helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_Talks) + item.dialog_num
                    }
                }
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                helper.getView(R.id.lineaer_Recommend).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(this@UserMoviesListDetailsActivity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id.toString())
                                .putExtra("uid", item.user.id)
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num == 0) {
                            queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        } else {
                            startActivity(Intent(this@UserMoviesListDetailsActivity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id)
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user_id.toString())
                            )
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        DialogMore(this@UserMoviesListDetailsActivity).setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_ShareWechat -> {
                                    DialogShare(this@UserMoviesListDetailsActivity, true).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearWechat -> {
                                                ShareUtils.share(this@UserMoviesListDetailsActivity, Wechat.NAME, item.voice_url, item.share_url, "", item.user)
                                            }
                                            R.id.linearMoment -> {
                                                ShareUtils.share(this@UserMoviesListDetailsActivity, WechatMoments.NAME, item.voice_url, item.share_url, "", item.user)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_ShareWeibo -> {
                                    ShareUtils.share(this@UserMoviesListDetailsActivity, SinaWeibo.NAME, item.voice_url, item.share_url, "", item.user)
                                }
                                R.id.tv_ShareQQ -> {
                                    DialogShare(this@UserMoviesListDetailsActivity, false).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearQQ -> {
                                                ShareUtils.share(this@UserMoviesListDetailsActivity, QQ.NAME, item.voice_url, item.share_url, "", item.user)
                                            }
                                            R.id.linearQzone -> {
                                                ShareUtils.share(this@UserMoviesListDetailsActivity, QZone.NAME, item.voice_url, item.share_url, "", item.user)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@UserMoviesListDetailsActivity).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        transLayout.showProgress()
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(this@UserMoviesListDetailsActivity).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                                R.id.tv_add_album -> {
                                    startActivity(Intent(this@UserMoviesListDetailsActivity, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
                                }
                            }
                        }).show()
                    else {
                        DialogReport(this@UserMoviesListDetailsActivity).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@UserMoviesListDetailsActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(this@UserMoviesListDetailsActivity).setOnResultListener(OnReportItemListener {
                                        transLayout.showProgress()
                                        report(item.voice_id, it, item.user.id)
                                    }).show()
                                R.id.tv_report_normal -> {
                                    DialogNormalReport(this@UserMoviesListDetailsActivity).setOnClickListener(View.OnClickListener { report ->
                                        when (report.id) {
                                            R.id.tv_Attach -> {
                                                reportNormalItem(item.voice_id, "1")
                                            }
                                            R.id.tv_Porn -> {
                                                reportNormalItem(item.voice_id, "2")
                                            }
                                            R.id.tv_Junk -> {
                                                reportNormalItem(item.voice_id, "3")
                                            }
                                            R.id.tv_illegal -> {
                                                reportNormalItem(item.voice_id, "4")
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Follow -> {
                                    if (item.subscription_id == 0) {
                                        addSubscriber(item)
                                    } else {
                                        DialogGraffiti(this@UserMoviesListDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (item.resource_type) {
                                            1 -> resources.getString(R.string.string_follow_movies)
                                            2 -> resources.getString(R.string.string_follow_book)
                                            3 -> resources.getString(R.string.string_follow_song)
                                            else -> resources.getString(R.string.string_follow_movies)
                                        }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                            deletedSubscriber(item)
                                        }).show()
                                    }
                                }
                            }
                        }).show()
                    }
                }

                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb))
                    } else {//共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
//                            transLayout.showProgress()
                            item.isNetStatus = true
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            transLayout.showProgress()
                            LightUtils.addItem(item.voice_id)
                            thumb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getImageView(R.id.roundImg)?.setOnClickListener { UserDetailsActivity.start(this@UserMoviesListDetailsActivity, item.user.avatar_url, item.user.id, helper.getImageView(R.id.roundImg)) }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                voiceProgress.setOnClickListener {
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

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                try {
                    if (TextUtils.isEmpty(item.voice_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    val file = getDownFilePath(item.voice_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@UserMoviesListDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@UserMoviesListDetailsActivity, item.voice_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@UserMoviesListDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
                            changeStatue(false)
                            if (TextUtils.isEmpty(item.is_shared) && item.is_collected != 1 && LightUtils.contains(item.voice_id)) {
                                item.is_collected = 1
                                thumb(item, null, 5)
                            }
                            animStatus(false)
                            progressHandler.stop()
                        }

                        override fun onInterrupt() {
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            item.isPlaying = false
                            animStatus(false)
                            progressHandler.stop()
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            addPlays(item, helper.getTextView(R.id.tv_Sub)) {}
                            playBean = item
                            item.isPlaying = true
                            if (TextUtils.isEmpty(item.is_shared) && LightUtils.contains(item.voice_id)) {
                                animStatus(true)
                            }
                            progressHandler.start()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
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
                                if (recyclerReview.isOffsetScreen(mData.indexOf(bean))) {
                                    SmallBang.attach2Window(this@UserMoviesListDetailsActivity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                super.changeStatue(isSelect)
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                        (helper.getView(R.id.heartView) as HeartWaveView).waterLevelRatio = currentPosition / audioPlayer.duration.toFloat()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerReview.adapter = adapter
        adapter.setLoadMoreEnable(recyclerReview, recyclerReview.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recyclerReview, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        adapter.setOnLoadListener {
            lastId = mData[mData.size - 1].voice_id
            request(1)
        }
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "review/users/$userId/$resourceType/$movieId?lastId=$lastId&recognition=1", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        if (audioPlayer.isPlaying) {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                        mData.clear()
                    }
                    mData.addAll(result.data)
                    adapter.notifyDataSetChanged()
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                } else {
                    showToast(result.msg)
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        }, "V3.2")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
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
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        audioPlayer.stop()
    }

    override fun onDelete(bean: BaseBean?) {
        transLayout.showContent()
        showToast("删除成功")
        mData.remove(bean)
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(OnDeletedMovieEvent(bean?.user_score.toString(), resourceType))
        if (mData.size == 0) {
            finish()
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
        showToast(e.toString())
        transLayout.showContent()
    }

    override fun onComment(from_id: String?) {
        commentBean?.let {
            it.dialog_num = 1
            it.chat_id = from_id
            adapter.notifyDataSetChanged()
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
        adapter.notifyItemChanged(mData.indexOf(bean))
        transLayout.showContent()
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        try {
            val indexOf = mData.indexOf(bean)
            mData.remove(bean)
            adapter.notifyItemRemoved(indexOf)
            dialogPwd?.dismiss()
        } catch (e: Exception) {

        }
    }

    override fun onAdminFail() {
        dialogPwd?.setCallBack()
    }
}