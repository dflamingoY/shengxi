package org.xiaoxingqi.shengxi.modules.listen.music

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_one_movie_details.*
import kotlinx.android.synthetic.main.layout_music_head_details.view.*
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
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddSongActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.HeartWaveView
import org.xiaoxingqi.shengxi.wedgit.SelectorImage
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager
import java.io.IOException

class OneMusicDetailsActivity : BaseAct(), ItemOperator {
    private lateinit var headView: View
    private lateinit var musicId: String
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy { ArrayList<BaseBean>() }
    private var sortType = 1
    private var current = 1
    private var lastId = ""
    private var musicData: BaseSearchBean? = null
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: BaseBean? = null
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }
    private val ADMIN_EDIT_ITEM = 0x01
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
        return R.layout.activity_one_movie_details
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.layout_music_head_details, recyclerView, false)
        tv_Title.text = resources.getString(R.string.string_music_9)
        try {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (IConstant.userAdminArray.contains(loginBean.user_id)) {
                iv_setting.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
        ivRecord.setImageResource(R.mipmap.icon_transparent_recording_music)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    override fun initData() {
        setItemOperator(this)
        audioPlayer = AudioPlayer(this)
        musicId = intent.getStringExtra("id")
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_voice, mData, headView) {
            var cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                item!!.resourceContent(helper!!, this@OneMusicDetailsActivity, glideUtil, 0)
                helper.getView(R.id.relative_Content).visibility = View.GONE
                helper.getView(R.id.scoreType).visibility = View.VISIBLE
                (helper.getView(R.id.scoreType) as SelectorImage).setImagResId(item.user_score.toString(), item.resource_type)
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                item.echoClick(this@OneMusicDetailsActivity, helper) {
                    queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                }
                helper.getView(R.id.tv_push).setOnClickListener {
                    if (item.subscription_id == 0) {
                        addSubscriber(item)
                    } else {
                        DialogGraffiti(this@OneMusicDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_song)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                            deletedSubscriber(item)
                        }).show()
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        shareDialog(item) { id ->
                            when (id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@OneMusicDetailsActivity).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        transLayout.showProgress()
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(this@OneMusicDetailsActivity).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                            }
                        }
                    else {
                        DialogReport(this@OneMusicDetailsActivity).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@OneMusicDetailsActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(this@OneMusicDetailsActivity).setOnResultListener(OnReportItemListener {
                                        transLayout.showProgress()
                                        report(item.voice_id, it, item.user.id)
                                    }).show()
                                R.id.tv_report_normal -> {
                                    reportNormal { reportType ->
                                        reportNormalItem(item.voice_id, reportType)
                                    }
                                }
                                R.id.tv_Follow -> {
                                    if (item.subscription_id == 0) {
                                        addSubscriber(item)
                                    } else {
                                        DialogGraffiti(this@OneMusicDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_song)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
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
                helper.getImageView(R.id.roundImg)?.setOnClickListener { UserDetailsActivity.start(this@OneMusicDetailsActivity, item.user.avatar_url, item.user.id, helper.getImageView(R.id.roundImg)) }
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

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
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

            override fun notifyHelperStatus(bean: BaseBean) {
                if (!TextUtils.isEmpty(bean.is_shared))
                    if (playBean == bean) {
                        //更新当前播放的次数++ 正在播放的item
                        cache.loop { baseAda ->
                            (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                        }?.let { ada ->
                            ada.getTextView(R.id.tv_Sub).text = if (bean.played_num == 0) context.resources.getString(R.string.string_Listener) else context.resources.getString(R.string.string_Listener) + " ${bean.played_num}"
                        }
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
                                if (recyclerView.isOffsetScreen(mData.indexOf(bean) + 1)) {
                                    SmallBang.attach2Window(this@OneMusicDetailsActivity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.loadmore_padding_bottom, recyclerView, false))
        request(0)
        request(1)
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(this@OneMusicDetailsActivity, mData.indexOf(item) + 1)
        }
        if (TextUtils.isEmpty(item.voice_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        try {
            playBean = item
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this@OneMusicDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this@OneMusicDetailsActivity, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this@OneMusicDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
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
            if (SPUtils.getBoolean(this@OneMusicDetailsActivity, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerView.scrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    current++
                    request(1)
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
                    if (TextUtils.isEmpty(it.is_shared) && LightUtils.contains(it.voice_id)) {
                        adapter.animStatus(true)
                    }
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    addPlays(it, null) { bean ->
                        adapter.notifyHelperStatus(bean)
                    }
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                        it.is_collected = 1
                        thumb(it, null, 5)
                    }
                }
                adapter.animStatus(false)
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
                adapter.animStatus(false)
                progressHandler.stop()
            }
        }
        customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        var index = mData.indexOf(it) + 1
                        /* if (index >= mData.size) {
                             index = 0
                             recyclerView.scrollToPosition(0)
                         }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            current++
                            request(1)
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
                        recyclerView.scroll(this@OneMusicDetailsActivity, mData.indexOf(it) + 1, false)
                    }
            }
        })
        btn_Back.setOnClickListener { finish() }
        headView.tv_Score.setOnClickListener {
            musicData?.let {
                if (!RecordTransparentActivity.isOnCreate) {
                    startActivity(Intent(this, SendAct::class.java)
                            .putExtra("data", it)
                            .putExtra("type", 4))
                    overridePendingTransition(R.anim.operate_enter, 0)
                }
            }
        }
        headView.tv_Sort.setOnClickListener {
            val loca = IntArray(2)
            it.getLocationOnScreen(loca)
            DialogSort(this).setOnClickListener(View.OnClickListener { sort ->
                when (sort.id) {
                    R.id.viewNew -> {
                        headView.tv_Sort.text = resources.getString(R.string.string_new)
                        sortType = 1
                    }
                    R.id.viewHot -> {
                        headView.tv_Sort.text = resources.getString(R.string.string_hot)
                        sortType = 2
                    }
                }
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                playBean = null
                lastId = ""
                current = 1
                request(current)
            }).setSelectTitle(sortType).setLocation(AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 119 + 15), loca[1])
        }

        swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying) audioPlayer.stop()
            playBean = null
            current = 1
            lastId = ""
            request(0)
            request(1)
        }
        adapter.setOnLoadListener {
            current++
            request(1)
        }
        iv_setting.setOnClickListener {
            if (intent.getBooleanExtra("token", false)) {
                startActivityForResult(Intent(this@OneMusicDetailsActivity, AddSongActivity::class.java).putExtra("data", musicData), ADMIN_EDIT_ITEM)
            } else
                dialogPwd = DialogCommitPwd(this).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                    loginAdmin(pwd)
                })
            dialogPwd?.show()
        }
        ivRecord.setOnClickListener {
            musicData?.let {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("data", it)
                        .putExtra("isMusic", true)
                        .putExtra("type", 4))
                overridePendingTransition(0, 0)
            }
        }
        headView.tvAddWish.setOnClickListener {
            transLayout.showProgress()
            if (musicData?.subscription?.subscription_type == 1) {//delete
                removeWish(musicData?.subscription?.id.toString()) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            musicData?.subscription?.subscription_type = 0
                            headView.tvRead.isSelected = false
                            headView.tvAddWish.isSelected = false
                            headView.tvAddWish.text = resources.getString(R.string.string_song_wish)
                        } else
                            showToast(it.msg)
                    }
                }
            } else {
                addWish(3, musicId, 1) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            //判断是否已经加入了想看, 删除想看
                            musicData?.subscription?.let { subscribe ->
                                if (subscribe.subscription_type == 2) {
                                    //清除选中
                                    headView.tvRead.text = resources.getString(R.string.string_song_listen)
                                }
                            }
                            headView.tvAddWish.text = resources.getString(R.string.string_song_add_wished)
                            headView.tvAddWish.isSelected = true
                            headView.tvRead.isSelected = false
                            if (null == musicData?.subscription) {
                                musicData?.subscription = SubscribeBean().apply {
                                    id = it.data.id
                                    subscription_type = 1
                                }
                            } else {
                                musicData?.subscription?.subscription_type = 1
                            }
                        } else
                            showToast(it.msg)
                    }
                }
            }
        }
        headView.tvRead.setOnClickListener {
            transLayout.showProgress()
            if (musicData?.subscription?.subscription_type == 2) {
                removeWish(musicData?.subscription?.id.toString()) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            musicData?.subscription?.subscription_type = 0
                            headView.tvAddWish.isSelected = false
                            headView.tvRead.isSelected = false
                            headView.tvRead.text = resources.getString(R.string.string_song_listen)
                        } else
                            showToast(it.msg)
                    }
                }
            } else {
                addWish(3, musicId, 2) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            musicData?.subscription?.let { subscribe ->
                                if (subscribe.subscription_type == 1) {
                                    //清除选中
                                    headView.tvAddWish.text = resources.getString(R.string.string_song_wish)
                                }
                            }
                            headView.tvRead.text = resources.getString(R.string.string_song_listened)
                            headView.tvRead.isSelected = true
                            headView.tvAddWish.isSelected = false
                            if (null == musicData?.subscription) {
                                musicData?.subscription = SubscribeBean().apply {
                                    id = it.data.id
                                    subscription_type = 2
                                }
                            } else {
                                musicData?.subscription?.subscription_type = 2
                            }
                        } else
                            showToast(it.msg)
                    }
                }
            }
        }

    }

    private fun loginAdmin(pwd: String) {
        OkClientHelper.post(this, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminLoginData
                if (result.code == 0) {
                    SPUtils.setString(this@OneMusicDetailsActivity, IConstant.ADMINTOKEN, result.data.token)
                    startActivityForResult(Intent(this@OneMusicDetailsActivity, AddSongActivity::class.java).putExtra("data", musicData), ADMIN_EDIT_ITEM)
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                showToast(any.toString())
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "resources/3/$musicId", OneDetailsData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as OneDetailsData
                        if (result.code == 0) {
                            if (result.data != null) {
                                musicData = result.data
                                Glide.with(this@OneMusicDetailsActivity)
                                        .applyDefaultRequestOptions(RequestOptions()
                                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                                .centerCrop()
                                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                                .signature(ObjectKey(result.data.song_cover)))
                                        .load(result.data.song_cover)
                                        .into(headView.ivMovieCover)
                                headView.tvMovieType.text = result.data.song_singer + "/" + result.data.album_name
                                headView.tvMovvieName.text = result.data.song_name

                                result.data.subscription?.let {
                                    //切换状态
                                    headView.tvAddWish.isSelected = it.subscription_type == 1
                                    headView.tvRead.isSelected = it.subscription_type == 2
                                    headView.tvRead.text = if (it.subscription_type == 2) resources.getString(R.string.string_song_listened) else resources.getString(R.string.string_song_listen)
                                    headView.tvAddWish.text = if (it.subscription_type == 1) resources.getString(R.string.string_song_add_wished) else resources.getString(R.string.string_song_wish)
                                }
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
            else -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "resources/3/$musicId/comment?sortType=$sortType&pageNo=$current&lastId=$lastId&recognition=1", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as VoiceData
                        if (result.data != null) {
                            if (TextUtils.isEmpty(lastId)) {
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
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                            lastId = mData[mData.size - 1].voice_id
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        swipeRefresh.isRefreshing = false
                    }
                }, "V3.2")
            }
        }
    }

    override fun onDelete(bean: BaseBean?) {
        showToast("删除成功")
        mData.remove(bean)
        adapter.notifyDataSetChanged()
        transLayout.showContent()
    }

    override fun onRecommend(bean: BaseBean?) {
        adapter.notifyItemChanged(mData.indexOf(bean) + 1)
    }

    override fun onUnRecommend(bean: BaseBean?) {
        adapter.notifyItemChanged(mData.indexOf(bean) + 1)
    }

    override fun onthumb(bean: BaseBean?) {
        adapter.notifyHeart(bean)
        transLayout.showContent()
    }

    override fun onUnThumb(bean: BaseBean?) {
        transLayout.showContent()
    }

    override fun onFailure(e: Any?) {
        if (e is String) {
            showToast(e)
        }
        transLayout.showContent()
    }

    override fun onComment(from_id: String?) {
        commentBean?.let {
            it.dialog_num = 1
            it.chat_id = from_id
            adapter.notifyItemChanged(mData.indexOf(it) + 1)
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
        adapter.notifyItemChanged(mData.indexOf(bean) + 1)
        transLayout.showContent()
    }

    override fun onAdminPrivacy(bean: BaseBean?) {
        try {
            val indexOf = mData.indexOf(bean)
            mData.remove(bean)
            adapter.notifyItemRemoved(indexOf + 1)
            dialogPwd?.dismiss()
        } catch (e: Exception) {

        }
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
                    adapter.notifyItemChanged(mData.indexOf(it) + 1)
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
                    LocalLogUtils.writeLog("oneMusicDetails : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            } else if (requestCode == ADMIN_EDIT_ITEM) {
                request(0)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMusic(event: UpdateMovieEvent) {
        if (event.type == 5) {
            transLayout.showProgress()
            lastId = ""
            current = 1
            request(0)
            request(1)
        }
    }

    override fun finish() {
        super.finish()
        audioPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }

}