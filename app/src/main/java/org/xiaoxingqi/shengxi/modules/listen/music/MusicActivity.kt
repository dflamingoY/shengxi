package org.xiaoxingqi.shengxi.modules.listen.music

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_resource_music.*
import kotlinx.android.synthetic.main.music_head.*
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
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.SearchResData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.listen.FriendsBangListActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.listen.movies.UserAttentionActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.userResource.HomeUserSongActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.GroupToggleView
import org.xiaoxingqi.shengxi.wedgit.HeartWaveView
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager
import java.io.IOException

/**
 * 唱回忆
 */
class MusicActivity : BaseAct(), ItemOperator {
    //    private lateinit var headView: View
    private lateinit var adapter: QuickAdapter<BaseBean>
    private val mData by lazy { ArrayList<BaseBean>() }
    private var sortType = 1
    private lateinit var headAdapter: QuickAdapter<BaseSearchBean>
    private val headData by lazy { ArrayList<BaseSearchBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var lastId = ""
    private var current = 1
    private var playBean: BaseBean? = null
    private var defaultType = 0
    private lateinit var loadView: View
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
        return R.layout.activity_resource_music
    }

    override fun initView() {
        recyclerReview.layoutManager = LinearLayoutManager(this)
//        headView = LayoutInflater.from(this).inflate(R.layout.music_head, recyclerReview, false)
        tv_Title.text = resources.getString(R.string.string_search_music)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {// e 型
            iv_movie_banner.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.mipmap.icon_music_baner_e_2 else R.mipmap.icon_music_baner_e_1)
        } else { //I 型
            iv_movie_banner.setImageResource(if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) R.mipmap.icon_music_baner_i_2 else R.mipmap.icon_music_baner_i_1)
        }
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    override fun initData() {
        setItemOperator(this)
        audioPlayer = AudioPlayer(this)
        adapter = object : QuickAdapter<BaseBean>(this, R.layout.item_voice, mData) {
            var cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                item!!.resourceContent(helper!!, this@MusicActivity, glideUtil, defaultType)
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                item.echoClick(this@MusicActivity, helper) {
                    queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                }
                helper.getView(R.id.relativeShare)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        shareDialog(item) { id ->
                            when (id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@MusicActivity).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        transLayout.showProgress()
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(this@MusicActivity).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                            }
                        }
                    else {
                        DialogReport(this@MusicActivity).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@MusicActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(this@MusicActivity).setOnResultListener(OnReportItemListener {
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
                                        DialogGraffiti(this@MusicActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_song)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
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
                        DialogGraffiti(this@MusicActivity).setTitle(String.format(resources.getString(R.string.string_follow_explor1), resources.getString(R.string.string_follow_song)), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
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
                helper.getImageView(R.id.roundImg)?.setOnClickListener { UserDetailsActivity.start(this@MusicActivity, item.user.avatar_url, item.user.id, helper.getImageView(R.id.roundImg)) }
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
                        if (item !== it) {
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
                                if (recyclerReview.isOffsetScreen(mData.indexOf(bean) + 1)) {
                                    SmallBang.attach2Window(this@MusicActivity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }

        }
        recyclerReview.adapter = adapter
        headAdapter = object : QuickAdapter<BaseSearchBean>(this, R.layout.item_music_head, headData) {
            var params: RelativeLayout.LayoutParams? = null
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                when {
                    0 == helper!!.itemView.tag as Int -> params?.setMargins(AppTools.dp2px(this@MusicActivity, 15), 0, 0, 0)
                    headData.size - 1 == helper.itemView.tag as Int -> params?.setMargins(AppTools.dp2px(this@MusicActivity, 10), 0, AppTools.dp2px(this@MusicActivity, 15), 0)
                    else -> params?.setMargins(AppTools.dp2px(this@MusicActivity, 10), 0, 0, 0)
                }
                helper.itemView.layoutParams = params
                Glide.with(this@MusicActivity)
                        .applyDefaultRequestOptions(RequestOptions()
                                .centerCrop()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .signature(ObjectKey(item!!.song_cover)))
                        .load(item.song_cover)
                        .into(helper.getImageView(R.id.iv_Movies))
                helper.getTextView(R.id.tvMoviesName).text = item.song_name
            }
        }
        recyclerMovieList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerMovieList.adapter = headAdapter
        loadView = LayoutInflater.from(this).inflate(R.layout.loadmore_reources, recyclerReview, false)
        adapter.setLoadMoreEnable(recyclerReview, recyclerReview.layoutManager, loadView)
        request(0)
        request(1)
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerReview.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerReview.scroll(this@MusicActivity, mData.indexOf(item) + 1)
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
                audioPlayer.start(if (SPUtils.getBoolean(this@MusicActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downFile(this@MusicActivity, item.voice_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this@MusicActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, {
//                    showToast(VolleyErrorHelper.getMessage(it))
                    if (!item.isReDown) {
                        item.isReDown = true
                        down(item, false)
                    } else
                        nextVoice()
                })
            }
        } catch (e: Exception) {
        }
    }

    private fun nextVoice() {
        playBean?.let {
            if (SPUtils.getBoolean(this@MusicActivity, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerReview.smoothScrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    if (defaultType == 0) {
                        current++
                        request(1)
                    } else {
                        subscriber()
                    }
                }
            } else {
                customPlayMenu.isSelected = false
            }
        }
    }

    override fun initEvent() {
        appbar.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, i ->
                    swipeRefresh.isEnabled = i >= 0
                })
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
                    nextVoice()
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
                        val index = mData.indexOf(it) + 1
                        /*if (index >= mData.size) {
                            index = 0
                            recyclerReview.smoothScrollToPosition(0)
                        }*/
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            if (defaultType == 0) {
                                current++
                                request(1)
                            } else {
                                subscriber()
                            }
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
                    recyclerReview.scrollToPosition(0)
                } else
                    playBean.let {
                        recyclerReview.scroll(this@MusicActivity, mData.indexOf(it) + 1, false)
                    }
            }
        })

        loadView.findViewById<View>(R.id.tvEmpty).setOnClickListener {
            DialogAlbumHowOperator(this).setTitle(resources.getString(R.string.string_what_follow_music)).show()
        }
        btn_Back.setOnClickListener { finish() }
        tv_Title.setOnClickListener {
            startActivity(Intent(this, MusicSearchActivity::class.java))
        }
        linear_similar.setOnClickListener {
            startActivity(Intent(this, SimilarMusicActivity::class.java))
        }
        linear_userMusic.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            startActivity(Intent(this, HomeUserSongActivity::class.java)
                    .putExtra("userId", loginBean.user_id))
        }
        headAdapter.setOnItemClickListener { _, position ->
            startActivity(Intent(this, OneMusicDetailsActivity::class.java)
                    .putExtra("id", headData[position].id)
            )
        }
        tvSort.setOnClickListener {
            val loca = IntArray(2)
            it.getLocationOnScreen(loca)
            DialogSort(this).setOnClickListener(View.OnClickListener { sort ->
                when (sort.id) {
                    R.id.viewNew -> {
                        tvSort.text = resources.getString(R.string.string_new)
                        sortType = 1
                    }
                    R.id.viewHot -> {
                        tvSort.text = resources.getString(R.string.string_hot)
                        sortType = 2
                    }
                }
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                lastId = ""
                current = 1
                request(current)
            }).setSelectTitle(sortType).setLocation(AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 119 + 15), loca[1])
        }
        tvMore.setOnClickListener {
            startActivity(Intent(this, HotMusicListActivity::class.java))
        }
        swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying) audioPlayer.stop()
            playBean = null
            lastId = ""
            if (defaultType == 0) {
                current = 1
                request(0)
                request(1)
            } else {
                subscriber()
            }
        }
        adapter.setOnLoadListener {
            if (defaultType == 0) {
                current++
                request(1)
            } else {
                subscriber()
            }
        }
        toggle_attention.setOnChildClickListener(object : GroupToggleView.OnChildClickListener {
            override fun onClick(position: Int, childView: View) {
                defaultType = position
                if (audioPlayer.isPlaying) audioPlayer.stop()
                playBean = null
                if (position == 1) {
                    lastId = ""
                    subscriber()
                } else {
                    request(1)
                }
            }
        })
        pagerSliding.setOnClickListener {
            defaultType = pagerSliding.indexOfChild(it)
            if (audioPlayer.isPlaying) audioPlayer.stop()
            playBean = null
            if (defaultType == 1) {
                lastId = ""
                subscriber()
            } else {
                request(1)
            }
        }
        linearLeadBoard.setOnClickListener {
            startActivity(Intent(this, FriendsBangListActivity::class.java))
        }
        linearAttention.setOnClickListener {
            startActivity(Intent(this, UserAttentionActivity::class.java).putExtra("current", 2))
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "resources/3?sortType=2&recentDay=30", SearchResData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SearchResData
                        result.data?.let {
                            headData.clear()
                            headData.addAll(it)
                            headAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.2")
            }
            else -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "resources/3/0/comment?sortType=$sortType&pageNo=$current&lastId=$lastId&recognition=1", VoiceData::class.java, object : OkResponse {
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
                            lastId = result.data[result.data.size - 1].voice_id
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
        }
    }

    private fun subscriber() {
        transLayout.showProgress()
        OkClientHelper.get(this, "voices/subscription?resourceType=3&lastId=$lastId", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as VoiceData
                if (result.code == 0) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        result.data?.let {
                            mData.addAll(it)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        if (result.data != null) {
                            result.data.forEach {
                                mData.add(it)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                        }
                    }
                    result.data?.let {
                        lastId = result.data[result.data.size - 1].voice_id
                    }
                    if (result.data != null && result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    if (mData.size == 0) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
                swipeRefresh.isRefreshing = false
            }
        }, "V4.1")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("MusicAct : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            }
        }
    }

    override fun onDelete(bean: BaseBean?) {
        transLayout.showContent()
        showToast("删除成功")
        mData.remove(bean)
        adapter.notifyDataSetChanged()
    }

    override fun onRecommend(bean: BaseBean?) {
        if (defaultType == 0) {
            adapter.notifyItemChanged(mData.indexOf(bean))
        }
    }

    override fun onUnRecommend(bean: BaseBean?) {
        if (defaultType == 0) {
            adapter.notifyItemChanged(mData.indexOf(bean))
        } else {//取消关注之后要刷新列表
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            lastId = ""
            subscriber()
        }
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

    override fun finish() {
        super.finish()
        audioPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
    }

}