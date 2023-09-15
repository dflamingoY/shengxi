package org.xiaoxingqi.shengxi.modules.listen.movies

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.transition.ArcMotion
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_one_movie_details.*
import kotlinx.android.synthetic.main.layout_head_movie_details.view.*
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
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddMovieItemActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.IOException

class OneMovieDetailsActivity : BaseAct(), ItemOperator {
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val ADMIN_EDIT_ITEM = 0x01
    private var movieData: BaseSearchBean? = null
    private var current = 1
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private var playBean: BaseBean? = null
    private var movieId: String? = null
    private var lastId: String? = ""
    private var sortType = 1
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var headView: View
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
        return R.layout.activity_one_movie_details
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.layout_head_movie_details, null, false)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators), ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
        ivRecord.setImageResource(R.mipmap.icon_record_movies)
        try {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (IConstant.userAdminArray.contains(loginBean.user_id)) {
                iv_setting.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        movieId = intent.getStringExtra("id")
        setItemOperator(this)
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_voice, mData, headView) {
            var cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                item!!.resourceContent(helper!!, this@OneMovieDetailsActivity, glideUtil, 0)
                helper.getView(R.id.relative_Content).visibility = View.GONE
                helper.getView(R.id.scoreType).visibility = View.VISIBLE
                (helper.getView(R.id.scoreType) as SelectorImage).setImagResId(item.user_score.toString(), item.resource_type)
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                item.echoClick(this@OneMovieDetailsActivity, helper) {
                    queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        shareDialog(item) { id ->
                            when (id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@OneMovieDetailsActivity).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        transLayout.showProgress()
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(this@OneMovieDetailsActivity).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                            }
                        }
                    } else {
                        DialogReport(this@OneMovieDetailsActivity).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@OneMovieDetailsActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(this@OneMovieDetailsActivity).setOnResultListener(OnReportItemListener {
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
                                        DialogGraffiti(this@OneMovieDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_movies)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                            deletedSubscriber(item)
                                        }).show()
                                    }
                                }

                            }
                        }).show()
                    }
                }
                helper.getView(R.id.tv_push).setOnClickListener {
                    if (item.subscription_id == 0) {
                        addSubscriber(item)
                    } else {
                        DialogGraffiti(this@OneMovieDetailsActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_movies)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                            deletedSubscriber(item)
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
                            LightUtils.contains(item.voice_id)
                            thumb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getImageView(R.id.roundImg)?.setOnClickListener { UserDetailsActivity.start(this@OneMovieDetailsActivity, item.user.avatar_url, item.user.id, helper.getImageView(R.id.roundImg)) }
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
                                    SmallBang.attach2Window(this@OneMovieDetailsActivity).bang(it, 60f, null)
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
        request(current)
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(this@OneMovieDetailsActivity, mData.indexOf(item) + 1)
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
                audioPlayer.start(if (SPUtils.getBoolean(this@OneMovieDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this@OneMovieDetailsActivity, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this@OneMovieDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
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
            if (SPUtils.getBoolean(this@OneMovieDetailsActivity, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerView.scrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    current++
                    request(current)
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
                        /*if (index >= mData.size) {
                            index = 0
                            recyclerView.scrollToPosition(0)
                        }*/

                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            current++
                            request(current)
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
                        recyclerView.scroll(this@OneMovieDetailsActivity, mData.indexOf(it) + 1, false)
                    }
            }
        })
        headView.tv_Score.setOnClickListener {
            movieData?.let {
                if (!RecordTransparentActivity.isOnCreate) {
                    startActivity(Intent(this, SendAct::class.java)
                            .putExtra("type", 2)
                            .putExtra("data", it))
                    overridePendingTransition(R.anim.operate_enter, 0)
                }
            }
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            current++
            request(current)
        }
        swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            playBean = null
            current = 1
            lastId = ""
            request(0)
            request(1)
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
                transLayout.showProgress()
                request(current)
            }).setSelectTitle(sortType).setLocation(AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 119 + 15), loca[1])
        }
        iv_setting.setOnClickListener {
            if (intent.getBooleanExtra("token", false)) {
                startActivityForResult(Intent(this@OneMovieDetailsActivity, AddMovieItemActivity::class.java).putExtra("data", movieData), ADMIN_EDIT_ITEM)
            } else
                dialogPwd = DialogCommitPwd(this).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                    loginAdmin(pwd)
                })
            dialogPwd?.show()
        }
        ivRecord.setOnClickListener {
            movieData?.let {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("data", it)
                        .putExtra("type", 2))
                overridePendingTransition(0, 0)
            }
        }
        headView.tvAddWish.setOnClickListener {
            transLayout.showProgress()
            if (movieData?.subscription?.subscription_type == 1) {//delete
                removeWish(movieData?.subscription?.id.toString()) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            movieData?.subscription?.subscription_type = 0
                            headView.tvRead.isSelected = false
                            headView.tvAddWish.isSelected = false
                            headView.tvAddWish.text = resources.getString(R.string.string_movies_wish)
                        } else
                            showToast(it.msg)
                    }
                }
            } else {
                addWish(1, movieId!!, 1) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            //判断是否已经加入了想看, 删除想看
                            movieData?.subscription?.let { subscribe ->
                                if (subscribe.subscription_type == 2) {
                                    //清除选中
                                    headView.tvRead.text = resources.getString(R.string.string_movies_look)
                                }
                            }
                            headView.tvAddWish.text = resources.getString(R.string.string_movies_add_wish)
                            headView.tvAddWish.isSelected = true
                            headView.tvRead.isSelected = false
                            if (null == movieData?.subscription) {
                                movieData?.subscription = SubscribeBean().apply {
                                    id = it.data.id
                                    subscription_type = 1
                                }
                            } else {
                                movieData?.subscription?.subscription_type = 1
                            }
                        } else
                            showToast(it.msg)
                    }
                }
            }
        }
        headView.tvRead.setOnClickListener {
            transLayout.showProgress()
            if (movieData?.subscription?.subscription_type == 2) {
                removeWish(movieData?.subscription?.id.toString()) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            movieData?.subscription?.subscription_type = 0
                            headView.tvAddWish.isSelected = false
                            headView.tvRead.isSelected = false
                            headView.tvRead.text = resources.getString(R.string.string_movies_look)
                        } else
                            showToast(it.msg)
                    }
                }
            } else {
                addWish(1, movieId!!, 2) {
                    transLayout.showContent()
                    it?.let {
                        if (it.code == 0) {
                            movieData?.subscription?.let { subscribe ->
                                if (subscribe.subscription_type == 1) {
                                    //清除选中
                                    headView.tvAddWish.text = resources.getString(R.string.string_movies_wish)
                                }
                            }
                            headView.tvRead.text = resources.getString(R.string.string_movies_looked)
                            headView.tvRead.isSelected = true
                            headView.tvAddWish.isSelected = false
                            if (null == movieData?.subscription) {
                                movieData?.subscription = SubscribeBean().apply {
                                    id = it.data.id
                                    subscription_type = 2
                                }
                            } else {
                                movieData?.subscription?.subscription_type = 2
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
                    SPUtils.setString(this@OneMovieDetailsActivity, IConstant.ADMINTOKEN, result.data.token)
                    startActivityForResult(Intent(this@OneMovieDetailsActivity, AddMovieItemActivity::class.java).putExtra("data", movieData), ADMIN_EDIT_ITEM)
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
                OkClientHelper.get(this, "resources/1/$movieId", OneDetailsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as OneDetailsData
                        movieData = result.data
                        Glide.with(this@OneMovieDetailsActivity)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .placeholder(R.drawable.drawable_default_tmpry)
                                        .centerCrop()
                                        .error(R.drawable.drawable_default_tmpry)
                                        .signature(ObjectKey(result.data.movie_poster)))
                                .load(result.data.movie_poster)
                                .into(headView.ivMovieCover)
                        headView.expendView.setTvShowText(result.data.movie_intro)
                        headView.tvMovvieName.text = result.data.movie_title
                        headView.tvMovieType.text = AppTools.getActorsDetails(result.data.movie_type)
                        headView.tvScore.text = "${result.data.movie_score / 10f}"
                        headView.tvUpdateTime.text = if (result.data.released_at != 0 || TimeUtils.string2Long(result.data.released_date) == 0) {
                            TimeUtils.getInstance().paserYyMm(result.data.released_at) + " ${resources.getString(R.string.string_movie_release)}"
                        } else {
                            result.data.released_date.substring(0, result.data.released_date.indexOf(" "))
                        }
                        result.data.subscription?.let {
                            //切换状态
                            headView.tvAddWish.isSelected = it.subscription_type == 1
                            headView.tvRead.isSelected = it.subscription_type == 2
                            headView.tvRead.text = if (it.subscription_type == 2) resources.getString(R.string.string_movies_looked) else resources.getString(R.string.string_movies_look)
                            headView.tvAddWish.text = if (it.subscription_type == 1) resources.getString(R.string.string_movies_add_wish) else resources.getString(R.string.string_movies_wish)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        swipeRefresh.isRefreshing = false
                        if (!AppTools.isNetOk(this@OneMovieDetailsActivity)) {
                            transLayout.showOffline()
                        }
                    }
                }, "V3.2")
            }
            else -> {
                OkClientHelper.get(this, "resources/1/$movieId/comment?sortType=$sortType&lastId=$lastId&pageNo=$flag&recognition=1", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as VoiceData
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.data != null) {
                            if (flag == 1) {
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
                                    adapter.notifyItemChanged(adapter.itemCount - 1)
                                }
                            }
                            lastId = mData[mData.size - 1].voice_id
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        } else {
                            if (flag == 1) {
                                mData.clear()
                                adapter.notifyDataSetChanged()
                            }
                        }
                        swipeRefresh.isRefreshing = false
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showOffline()
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

    override fun onComment(form_id: String?) {
        commentBean?.let {
            it.dialog_num = 1
            it.chat_id = form_id
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

    /**
     * 发布新的影评 刷新评分和数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMovie(event: UpdateMovieEvent) {
        if (event.type == 3) {
            transLayout.showProgress()
            lastId = ""
            current = 1
            request(1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("OneMovieDetails : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            } else if (requestCode == ADMIN_EDIT_ITEM) {
                request(0)
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity, url: String, id: String, imageView: ImageView) {
            val intent = Intent(context, OneMovieDetailsActivity::class.java)
            intent.putExtra("url", url).putExtra("id", id)
//            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context,
//                    imageView, "transition_movie_img")//与xml文件对应
            ActivityCompat.startActivity(context, intent, null)
        }
    }

    private fun setMotion(imageView: ImageView, isShow: Boolean) {
        if (!isShow) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //定义ArcMotion
            val arcMotion = ArcMotion()
            arcMotion.minimumHorizontalAngle = 50f
            arcMotion.minimumVerticalAngle = 50f
            //插值器，控制速度
            val interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)
            //实例化自定义的ChangeBounds
            val changeBounds = CustomChangeBounds()
            changeBounds.pathMotion = arcMotion
            changeBounds.interpolator = interpolator
            changeBounds.addTarget(imageView)
            //将切换动画应用到当前的Activity的进入和返回
            window.sharedElementEnterTransition = changeBounds
            window.sharedElementReturnTransition = changeBounds
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        audioPlayer.stop()
    }

}