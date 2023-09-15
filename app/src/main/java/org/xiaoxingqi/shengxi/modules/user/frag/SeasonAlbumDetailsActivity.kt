package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import com.romainpiel.shimmer.Shimmer
import kotlinx.android.synthetic.main.activity_season_album_details.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogAlbumHowOperator
import org.xiaoxingqi.shengxi.dialog.DialogAlbumVoiceMenu
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.CropActivity
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.EditAlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.barrage.BarrageView
import skin.support.SkinCompatManager
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

class SeasonAlbumDetailsActivity : BaseNormalActivity() {
    companion object {
        const val MODE_LOOP = 0x01
        const val MODE_RANDOM = 0x02
        const val MODE_SINGLE = 0x03
        private const val REQUEST_GALLEY = 0x04
        private const val REQUEST_VOICE_SELF = 0x05
        private const val REQUEST_CROP_BG = 0x06
    }

    private lateinit var data: VoiceAlbumData.AlbumDataBean
    private var dialog: DialogAlbumVoiceMenu? = null
    private val mData by lazy { ArrayList<BaseBean>() }
    private var lastId: String = ""
    private lateinit var audioPlayer: AudioPlayer
    private var current = 0
    private var playMode = 1 //1列表循环 2随机 3单曲循环
    private var isDrag = false
    private var isContinua = true
    private val random by lazy { Random() }
    private var countDown: CountDownDelay? = null

    //是否匹配成功
    private var isMatched = false

    //是否需要轮询获取结果, 或者获取结果是否展示
    private var isLoopInteract = true
    private var shimmer: Shimmer? = null
    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            if (!isDrag) {
                seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                marqueeText.update(audioPlayer.currentPosition.toInt())
            }
            tv_ProgressTime.text = AppTools.parseTime2Str(audioPlayer.currentPosition)
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
                seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else {
            if (audioPlayer.isPlaying) {
                seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == data.user_id.toString()) {
            outAnim(0)
        }
        isContinua = false
        audioPlayer.stop()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_season_album_details
    }

    override fun initView() {
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    || OsUtil.isMIUI()
                    || OsUtil.isFlyme()) {
                setStatusBarFontIconDark(true)
            } else {
                view_status_bar.setBackgroundColor(Color.parseColor("#cccccc"))
            }
        }
        val params = view_status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this, 47).toFloat()).setDuration(0).start()
        marqueeText.setAutoGravity(true)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setItemOperator(object : ItemOperatorAdapter() {
            override fun onFailure(e: Any?) {
                if (e is String)
                    showToast(e.toString())
            }

            override fun onRecommend(bean: BaseBean?) {
                showMenu(mData[current])
            }

            override fun onUnRecommend(bean: BaseBean?) {
                showMenu(mData[current])
            }
        })
        audioPlayer = AudioPlayer(this)
        playMode = SPUtils.getInt(this, IConstant.TIMEMACHINEPLAYMODE, 1)
        iv_PlayMode.setImageResource(when (playMode) {
            1 -> R.mipmap.icon_play_mode_cycle
            2 -> R.mipmap.icon_play_mode_random
            3 -> R.mipmap.icon_play_mode_singl
            else -> {
                R.mipmap.icon_play_mode_cycle
            }
        })
        data = intent.getSerializableExtra("data") as VoiceAlbumData.AlbumDataBean
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (data.voice_total_len > 0) {
            relative_empty_hint.visibility = View.GONE
            if (loginBean.user_id == data.user_id) {
                tv_album_info.text = "${TimeUtils.parse(data.started_at)} - ${TimeUtils.parse(data.ended_at)}\n" + String.format(resources.getString(R.string.string_album_details_5), data.voice_num, TimeUtils.formatterS(this@SeasonAlbumDetailsActivity, data.voice_total_len), data.played_num)
            } else {
                tv_album_info.text = "${TimeUtils.parse(data.started_at)} - ${TimeUtils.parse(data.ended_at)}\n" + String.format(resources.getString(R.string.string_album_details_7), data.voice_num, TimeUtils.formatterS(this@SeasonAlbumDetailsActivity, data.voice_total_len))
            }
        } else {
            relative_empty_hint.visibility = View.VISIBLE
        }
        tv_Title.text = if (loginBean.user_id == data.user_id.toString()) {
            shimmer = Shimmer()
            shimmer?.duration = 1500L
            tv_Loading.reflectionColor = Color.YELLOW
            ivArrow.visibility = View.VISIBLE
            linear_msg.visibility = View.VISIBLE
            linear_add_album.visibility = View.VISIBLE
            tvRecordVoice.visibility = View.VISIBLE
            resources.getString(R.string.string_album_details_1)
        } else {
            tv_how_add.visibility = View.GONE
            relative_empty_hint.visibility = View.GONE
            iv_setting.visibility = View.GONE
            resources.getString(R.string.string_album_details_6)
        }

        Glide.with(this).load(data.album_cover_url)
                .into(ivMachineBg)
        tv_album_title.text = data.album_name
        request(0)
        request(1)
    }

    override fun onResume() {
        super.onResume()
        try {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (loginBean.user_id == data.user_id) {
                if (SPUtils.getBoolean(this, IConstant.IS_CLOSE_INTERACT + loginBean.user_id, true)) {
                    //默认打开打招呼的界面
                    initInteractView(0)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        iv_setting.setOnClickListener {
            startActivity(Intent(this, EditAlbumActivity::class.java).putExtra("data", data))
        }
        iv_Play.setOnClickListener {
            if (audioPlayer.isPlaying) {
                isContinua = false
                audioPlayer.stop()
            } else {
                if (mData.size > 0) {
                    download(mData[current])
                }
            }
        }
        iv_Next.setOnClickListener {
            if (mData.size == 0) {
                return@setOnClickListener
            }
            if (playMode == 2) {
                current = random.nextInt(mData.size)
            } else {
                current++
                if (current >= mData.size) {
                    current = 0
                }
            }
            isContinua = true
            audioPlayer.stop()
            seekBar.progress = 0
            download(mData[current])
        }
        iv_Pre.setOnClickListener {
            if (mData.size == 0) {
                return@setOnClickListener
            }
            if (playMode == 2) {
                current = random.nextInt(mData.size)
            } else {
                current--
                if (current < 0) {
                    current = 0
                    return@setOnClickListener
                }
            }
            isContinua = true
            audioPlayer.stop()
            seekBar.progress = 0
            download(mData[current])
        }
        iv_PlayMode.setOnClickListener {
            when (playMode) {
                1 -> {
                    playMode = 2
                    iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_random)
                }
                2 -> {
                    playMode = 3
                    iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_singl)
                }
                3 -> {
                    playMode = 1
                    iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_cycle)
                }
            }
            SPUtils.setInt(this, IConstant.TIMEMACHINEPLAYMODE, playMode)
            showToast(when (playMode) {
                2 -> resources.getString(R.string.string_play_mode_2)
                3 -> resources.getString(R.string.string_play_mode_3)
                else -> {
                    resources.getString(R.string.string_play_mode_1)
                }
            })
        }
        iv_VoiceList.setOnClickListener {
            if (dialog == null) {
                dialog = DialogAlbumVoiceMenu(this).setOnItemClickListener(object : IAlbumMenuOperatListener {
                    override fun clickItem(position: Int) {
                        /**
                         * 播放指定的角標
                         */
                        current = position
                        audioPlayer.stop()
                        seekBar.progress = 0
                        download(mData[position])
                    }

                    override fun operatedItem(bean: BaseBean) {
                        /**
                         * 移除当前item
                         */
                        removeItem(bean)
                    }
                })
                dialog!!.setData(mData, PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id == data.user_id.toString())
            }
            dialog!!.updateCurrent(current)
            dialog!!.show()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (audioPlayer.duration > 0L)
                    marqueeText.update((progress / 1000f * audioPlayer.duration).toInt())
                else {
                    marqueeText.update((progress / 1000f * mData!![current].allDuration).toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDrag = true
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDrag = false
                if (audioPlayer.isPlaying) {
                    audioPlayer.seekTo((seekBar!!.progress / 1000f * audioPlayer.duration).toInt())
                } else {
                    try {
                        if (audioPlayer.duration > 0)
                            tv_ProgressTime.text = (seekBar!!.progress / 1000f * audioPlayer.duration).toString()
                        else {
                            tv_ProgressTime.text = AppTools.parseTime2Str((seekBar!!.progress / 1000f * Integer.parseInt(mData[current].voice_len)).toLong() * 1000)
                        }
                    } catch (e: Exception) {
                        tv_ProgressTime.text = "00:00"
                    }
                }
            }
        })
        tv_AddFriends.setOnClickListener {

        }
        view_resource_type.setOnClickListener {
            anim()
        }
        ivMachineBg.setOnClickListener {
            anim()
        }
        ivMachineBg.setOnLongClickListener {
            /**
             * show save img
             */
            longClickSave()
            false
        }
        view_resource_type.setOnLongClickListener {
            longClickSave()
            false
        }
        //点赞
        relativeEcho.setOnClickListener {
            /**
             * 判断当前的归属
             */
            if (current != -1 && current < mData.size) {
                if (!TextUtils.isEmpty(mData[current].is_shared)) {//共享
                    if (audioPlayer.isPlaying) {
                        audioPlayer.stop()
                        iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    }
                    queryCount(mData[current], tv_Echo, iv_Thumb, transLayout)
                } else {//共鳴
                    if (mData[current].isNetStatus) {
                        return@setOnClickListener
                    }
                    if (mData[current].is_collected == 1) {
//                        transLayout.showProgress()
                        mData[current].isNetStatus = true
                        unThumb(mData[current], iv_Thumb)
                    } else {
                        transLayout.showProgress()
                        thumb(mData[current], iv_Thumb)
                    }
                }
            }
        }
        //回声或者进入单页
        lineaer_Recommend.setOnClickListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                iv_Play.setImageResource(R.mipmap.icon_play_pause)
            }
            if (current != -1 && current < mData.size) {
                if (!TextUtils.isEmpty(mData[current].is_shared)) {
                    startActivity(Intent(this, DynamicDetailsActivity::class.java)
                            .putExtra("id", mData[current].voice_id)
                            .putExtra("uid", mData[current].user.id)
                            .putExtra("isExpend", mData[current].chat_num > 0)
                    )
                } else {
                    if (mData[current].dialog_num == 0) {
                        queryPermission(mData[current], transLayout, AppTools.fastJson(mData[current].user_id.toString(), 1, mData[current].voice_id))
                    } else {
                        startActivity(Intent(this, TalkListActivity::class.java)
                                .putExtra("voice_id", mData[current].voice_id)
                                .putExtra("chat_id", mData[current].chat_id)
                                .putExtra("uid", mData[current].user_id.toString())
                        )
                    }
                }
            }
        }
        viewImgPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                tv_count.text = "${position + 1}/${viewImgPager.adapter?.count}"
            }
        })
        linear_msg.setOnClickListener {
            /**
             * 给自己留言
             */
            if (mData[current].note_num > 0) {
                startActivity(Intent(this, SelfMsgActivity::class.java).putExtra("voiceId", mData[current].voice_id))
            } else {
                startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                        .putExtra("isSelf", true)
                        .putExtra("resourceType", "13"), REQUEST_VOICE_SELF)
                overridePendingTransition(0, 0)
            }
        }
        linear_add_album.setOnClickListener {
            startActivity(Intent(this, DialogAddAlbumActivity::class.java).putExtra("voiceId", mData[current].voice_id))
        }

        ivArrow.setOnClickListener {
            /**
             * 扩散动画 展示隐藏View
             */
            initInteractView(500)
            SPUtils.setBoolean(this, IConstant.IS_CLOSE_INTERACT + PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id, true)
        }
        tvHide.setOnClickListener {
            SPUtils.setBoolean(this, IConstant.IS_CLOSE_INTERACT + PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id, false)
            //隐藏
            outAnim(500)
        }
        tvRecordVoice.setOnClickListener {
            startActivity(Intent(this, RecordTransparentActivity::class.java)
                    .putExtra("albumId", data.id)
                    .putExtra("type", 1))
            overridePendingTransition(0, 0)
        }
        tv_how_add.setOnClickListener {
            DialogAlbumHowOperator(this).setTitle(resources.getString(R.string.string_how_add_voices)).show()
        }
        barrageView.setOnDelayListener(object : BarrageView.OnDelayListener {
            override fun onDelay() {
                interaction(1)
            }
        })
    }

    private fun longClickSave() {
        try {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            DialogSelectPhoto(this).hideAction(true)
                    .hindOther(loginBean.user_id == data.user_id.toString())
                    .changeText(if (loginBean.user_id == data.user_id.toString()) "更改专辑封面" else "保存图片")
                    .setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                        override fun itemView(view: View) {
                            when (view.id) {
                                R.id.tv_Save -> {
                                    if (loginBean.user_id == data.user_id.toString()) {
                                        openGallery()
                                    } else {//保存
                                        if (!TextUtils.isEmpty(data.album_cover_url.toString())) {
                                            save(data.album_cover_url.toString())
                                        } else {
                                            showToast("Ta还没设置封面")
                                        }
                                    }
                                }
                                R.id.tv_Other -> {//保存
                                    if (!TextUtils.isEmpty(data.album_cover_url.toString())) {
                                        save(data.album_cover_url.toString())
                                    } else {
                                        showToast("还没设置封面")
                                    }
                                }
                            }
                        }
                    }).show()
        } catch (e: Exception) {

        }
    }

    private fun initInteractView(duration: Long) {
        try {
            shimmer?.start(tv_Loading)
            relativeMatch.visibility = View.VISIBLE
            val animator = ViewAnimationUtils.createCircularReveal(relativeMatch, AppTools.getWindowsWidth(this), 0, 0f, sqrt(AppTools.getWindowsWidth(this).toDouble().pow(2.0) * 2).toFloat())
            animator.duration = duration
            animator.start()
            ivArrow.isEnabled = false
            ivArrow.visibility = View.GONE
            animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator?) {
                    tvHide.isEnabled = true
                }
            })
            tvHide.visibility = View.VISIBLE
            tvHide.alpha = 0f
            val alphaAnim = ObjectAnimator.ofFloat(0f, 1f).setDuration(duration)
            alphaAnim.addUpdateListener {
                val alpha = it.animatedValue as Float
                tvHide.alpha = alpha
            }
            alphaAnim.start()
        } catch (e: Exception) {
            tvHide.visibility = View.VISIBLE
            ivArrow.visibility = View.GONE
            tvHide.isEnabled = true
            e.printStackTrace()
        }
        interaction(1)
    }

    private fun notifyPage() {
        relativeMatch.visibility = View.GONE
        ivArrow.visibility = View.VISIBLE
        ivArrow.isEnabled = true
        isMatched = false
    }

    /**
     * 退出匹配的动画
     */
    private fun outAnim(duration: Long) {
        shimmer?.cancel()
        interaction(2)
        isLoopInteract = false
        tvHide.isEnabled = false
        if (tv_Loading.visibility != View.GONE) {
            tv_Loading.visibility = View.GONE
        }
        if (tvHide.visibility != View.GONE)
            tvHide.visibility = View.GONE
        if (duration == 0L) {
            notifyPage()
        } else {
            val animator = ViewAnimationUtils.createCircularReveal(relativeMatch, AppTools.getWindowsWidth(this), 0, sqrt(AppTools.getWindowsWidth(this).toDouble().pow(2.0) * 2).toFloat(), 0f)
            animator.duration = duration
            animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator?) {
                    notifyPage()
                }
            })
            animator.start()
        }
        interactId = null
        interactName = null
    }

    private var interactName: String? = null
    private var interactId: String? = null

    /**
     * 互动网络请求
     */
    private fun interaction(type: Int) {
        when (type) {
            0 -> {//获取结果
                OkClientHelper.get(this, "users/${data.user_id}/interactions", SearchUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SearchUserData
                        if (result.code == 0) {
                            result.data?.let {
                                barrageView.setData(it.map { bean ->
                                    bean.nick_name
                                }.toTypedArray())
                                barrageView.setStart()
                            }
                            if (result.data == null || result.data.isEmpty()) {
                                tv_Loading.visibility = View.VISIBLE
                            } else {
                                tv_Loading.visibility = View.INVISIBLE
                            }
                        }
                        if (result.data == null) {
                            countDown = CountDownDelay(15000, 1000)
                            countDown?.start()
//                            handler.postDelayed({
//                                interaction(1)
//                            }, 15000)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            1 -> {
                OkClientHelper.post(this, "interaction", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            interaction(0)
                            isLoopInteract = true
                            if (tv_Loading.visibility != View.INVISIBLE)
                                tv_Loading.visibility = View.VISIBLE
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.1")
            }
            2 -> {
                OkClientHelper.delete(this, "interaction", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            handler.removeCallbacksAndMessages(null)
                            countDown?.let {
                                it.cancel()
                                countDown = null
                            }
                            if (audioPlayer.isPlaying) {
                                handler.start()
                            }
                            if (tv_Loading.visibility != View.GONE)
                                tv_Loading.visibility = View.GONE
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.1")
            }
        }
    }

    private fun anim() {
        if (relative_operator_menu.isSelected)
            if (relative_operator_menu.y == AppTools.getWindowsWidth(this).toFloat()) {
                val animtor = ObjectAnimator.ofFloat(relative_operator_menu, "translationY", 0f).setDuration(220)
                animtor.start()
            } else {
                val animtor = ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this, 47).toFloat()).setDuration(220)
                animtor.start()
            }
        else {
            ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this, 47).toFloat()).setDuration(0).start()
        }
    }

    private var preUrl: Any? = null
    private fun blur(url: Any, iv: ImageView) {
        val errorPath = if (url is String) {
            if (url.contains("?")) url.substring(0, url.indexOf("?")) else url
        } else {
            ""
        }
        Glide.with(this)
                .asBitmap()
                .apply(RequestOptions()
                        .placeholder(0)
                        .error(0)
                        .signature(ObjectKey(url)))
                .load(url)
                .error(if (isDestroyed) null else Glide.with(this)
                        .applyDefaultRequestOptions(RequestOptions()
                                .placeholder(0)
                                .error(0)
                                .signature(ObjectKey(errorPath)))
                        .asBitmap()
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                try {
                                    val fastBlur = FastBlur().fastblur(resource, 50, iv_Bg)
                                    iv_Bg.setImageBitmap(fastBlur)
                                } catch (e: Exception) {

                                }
                                return false
                            }
                        })
                        .load(url))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        try {
                            val fastBlur = FastBlur().fastblur(resource, 50, iv_Bg)
                            iv_Bg.setImageBitmap(fastBlur)
                            appbar.isSelected = true
                            view_status_bar.setBackgroundColor(Color.TRANSPARENT)
                            if (preUrl != url) {
                                val animator = ObjectAnimator.ofFloat(iv_Bg, "alpha", 0.5f, 1f).setDuration(220)
                                animator.addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                    }
                                })
                                animator.start()
                            }
                        } catch (e: Exception) {
                        }
                        preUrl = url
                        return false
                    }
                })
                .into(iv)
    }

    /**
     * 下载需要播放的文件, 展示信息
     */
    private fun download(item: BaseBean) {
        if (relative_show_copyWriting.visibility != View.GONE) {
            relative_show_copyWriting.visibility = View.GONE
        }
        view_resource_type.visibility = View.GONE
        if (!relative_operator_menu.isSelected) {
            val animtor = ObjectAnimator.ofFloat(relative_operator_menu, "translationY", 0f).setDuration(220)
            relative_operator_menu.isSelected = true
            animtor.start()
        }
        marqueeText.text = item.recognition_content ?: ""
        showItemInfo(item)
        try {
            tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(item.voice_len) * 1000).toLong())
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast("声兮路径出错")
                return
            }
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                if (currentMode == MODE_EARPIECE)
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                else
                    audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                loadingStatus.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
                OkClientHelper.downFile(this, item.voice_url, { o ->
                    loadingStatus.visibility = View.GONE
                    progress.visibility = View.GONE
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        if (currentMode == MODE_EARPIECE)
                            audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                        else
                            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { volleyError ->
                    showToast(VolleyErrorHelper.getMessage(volleyError))
                    loadingStatus.visibility = View.GONE
                    progress.visibility = View.GONE
                })
            }
            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                override fun onPrepared() {
                    if (audioPlayer.duration > 0) {
                        item.allDuration = audioPlayer.duration
                    }
                    addPlays(item)
                    item.isPlaying = true
                    marqueeText.calcDx(audioPlayer.duration.toInt())
                    dialog?.let {
                        if (it.isShowing) {
                            it.updateCurrent(current)
                        }
                    }
                    /*if (!TextUtils.isEmpty(item.is_shared)) {
                        if (TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()) != SPUtils.getString(this@SeasonAlbumDetailsActivity, IConstant.FIRST_PUSH_ACHIEVEMENT + item.user_id, "")) {
                            //提交成就播放
                            pushTravel(item.user_id.toString())
                        }
                    }*/
                    iv_Play.setImageResource(R.mipmap.icon_play_playing)
                    audioPlayer.seekTo((seekBar.progress / 1000f * Integer.parseInt(item.voice_len) * 1000).toInt())
                    handler.start()
                }

                @SuppressLint("SetTextI18n")
                override fun onCompletion() {
                    marqueeText.reset()
                    if (TextUtils.isEmpty(item.is_shared) && item.is_collected != 1) {
                        thumb(item, null)
                    }
                    item.isPlaying = false
                    seekBar.progress = 0
                    if (!isContinua)
                        iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    handler.stop()
                    tv_ProgressTime.text = "00:00"
                    playNext()
                }

                override fun onInterrupt() {
                    item.isPlaying = false
                    if (!isContinua)
                        iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    handler.stop()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 播放下一首
     */
    private fun playNext() {
        if (mData.size > 0) {
            when (playMode) {
                MODE_LOOP -> {
                    current++
                    if (current == mData.size) {
                        current = 0
                    }
                    download(mData[current])
                }
                MODE_RANDOM -> {
                    current = random.nextInt(mData.size)
                    download(mData[current])
                }
                MODE_SINGLE -> {
                    if (current != -1)
                        download(mData[current])//當去播放這一條
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showItemInfo(item: BaseBean) {
        showMenu(item)
        if (item.img_list != null) {
            tv_count.visibility = View.VISIBLE
            tv_count.text = "1/${item.img_list.size}"
            iv_MoviesCover.visibility = View.GONE
            iv_Music.visibility = View.GONE
            viewImgPager.adapter = ImgAdapter(mData[current].img_list)
            blur(item.img_list[0], ivMachineBg)
            ivMachineBg.visibility = View.GONE
        } else {
            tv_count.visibility = View.GONE
            viewImgPager.adapter = null
            if (item.resource != null) {
                view_resource_type.visibility = View.VISIBLE
                iv_MoviesCover.visibility = View.VISIBLE
                when (item.resource_type) {
                    1 -> {
                        iv_MoviesCover.visibility = View.VISIBLE
                        blur(item.resource.movie_poster, iv_MoviesCover)
                        iv_Music.visibility = View.GONE
                    }
                    2 -> {
                        iv_Music.visibility = View.GONE
                        blur(item.resource.book_cover, iv_MoviesCover)
                        iv_MoviesCover.visibility = View.VISIBLE
                    }
                    else -> {
                        iv_MoviesCover.visibility = View.GONE
                        iv_Music.visibility = View.VISIBLE
                        blur(item.resource.song_cover, iv_Music)
                    }
                }
                ivMachineBg.visibility = View.GONE
            } else {
                blur(data.album_cover_url, ivMachineBg)
                iv_MoviesCover.visibility = View.GONE
                iv_Music.visibility = View.GONE
                ivMachineBg.visibility = View.VISIBLE
            }
        }
        tv_VoiceTime.text = TimeUtils.getInstance().paserTimeMachine(this, item.created_at)
        if (!TextUtils.isEmpty(item.topic_name)) {
            tvTopic.text = "#${item.topic_name}#"
            tvTopic.visibility = View.VISIBLE
        } else {
            tvTopic.visibility = View.GONE
            tvTopic.text = ""
        }
        iv_Privacy.visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showMenu(item: BaseBean) {
        if (!TextUtils.isEmpty(item.is_shared)) {
            iv_Thumb.setImageResource(R.drawable.selector_white_photo_share)
            iv_Thumb.isSelected = item.is_shared == "1"
            if (item.is_shared == "1") {
                tv_Echo.text = "撤回"
            } else {
                tv_Echo.text = "共享"
            }
            tv_Recommend.text = resources.getString(R.string.string_echoing) + if (item.chat_num <= 0) {
                ""
            } else {
                " " + item.chat_num
            }
            tv_msg.text = resources.getString(R.string.string_self_comment) + if (item.note_num > 0) item.note_num else ""
        } else {
            iv_Thumb.isSelected = item.is_collected == 1
            iv_Thumb.setImageResource(R.drawable.selector_white_photo_thumb)
            tv_Echo.text = resources.getString(R.string.string_gongming)
            if (item.dialog_num <= 0) {
                tv_Recommend.text = resources.getString(R.string.string_echoing)
            } else {
                tv_Recommend.text = "${resources.getString(R.string.string_Talks)} " + item.dialog_num
            }
        }
    }

    /**
     * 添加播放次数
     */
    fun addPlays(bean: BaseBean) {
        if (TextUtils.isEmpty(bean.is_shared))
            OkClientHelper.get(this, "users/${bean.user_id}/voices/${bean.voice_id}", BaseRepData::class.java, object : OkResponse {
                @SuppressLint("SetTextI18n")
                override fun success(result: Any?) {

                }

                override fun onFailure(any: Any?) {

                }
            })
    }

    private fun removeItem(bean: BaseBean) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(this, "user/${loginBean.user_id}/albumVoice/${data.id}/${bean.voice_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    /**
                     * 删除集合中数据 更新View
                     */
                    if (audioPlayer.isPlaying) {
                        if (bean == mData[current]) {
                            audioPlayer.stop()
                        }
                    }
                    mData.remove(bean)
                    dialog?.notifyDataSetChange()
                    if (mData.size == 0) {
                        relative_show_copyWriting.visibility = View.VISIBLE
                        relative_empty_hint.visibility = View.VISIBLE
                        tv_album_info.visibility = View.GONE
                        appbar.isSelected = false
                        iv_Bg.setImageDrawable(null)
                        tv_count.visibility = View.GONE
                        viewImgPager.adapter = null
                        iv_MoviesCover.visibility = View.GONE
                        iv_Music.visibility = View.GONE
                        ivMachineBg.visibility = View.VISIBLE
                        Glide.with(this@SeasonAlbumDetailsActivity).load(data.album_cover_url)
                                .into(ivMachineBg)
                        iv_Privacy.visibility = View.GONE
                        linearVoiceInfo.visibility = View.GONE
                        seekBar.progress = 0
                        seekBar.max = 0
                        tv_ProgressTime.text = "00:00"
                        tv_Time.text = "00:00"
                        iv_Play.setImageResource(R.mipmap.icon_play_pause)
                        ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this@SeasonAlbumDetailsActivity, 47).toFloat()).setDuration(0).start()
                    } else {
                        current--
                        playNext()
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.8")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//请求专辑详情
                OkClientHelper.get(this, "user/${data.user_id}/voiceAlbum/${data.id}", SeasonAlbumDetails::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as SeasonAlbumDetails
                        if (result.code == 0 && result.data != null) {
                            data = result.data
                            tv_album_title.text = result.data.album_name
                            if (result.data.voice_total_len > 0) {
                                tv_album_info.visibility = View.VISIBLE
                                relative_empty_hint.visibility = View.GONE
                                val loginBean = PreferenceTools.getObj(this@SeasonAlbumDetailsActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                                if (loginBean.user_id == data.user_id)
                                    tv_album_info.text = "${TimeUtils.parse(result.data.started_at)} - ${TimeUtils.parse(result.data.ended_at)}\n" + String.format(resources.getString(R.string.string_album_details_5), result.data.voice_num, TimeUtils.formatterS(this@SeasonAlbumDetailsActivity, result.data.voice_total_len), result.data.played_num)
                                else {
                                    tv_album_info.text = "${TimeUtils.parse(result.data.started_at)} - ${TimeUtils.parse(result.data.ended_at)}\n" + String.format(resources.getString(R.string.string_album_details_7), result.data.voice_num, TimeUtils.formatterS(this@SeasonAlbumDetailsActivity, result.data.voice_total_len))
                                }
                            } else {
                                relative_empty_hint.visibility = View.VISIBLE
                                tv_album_info.visibility = View.GONE
                            }
                        } else {
                            showToast("专辑不存在")
                            transLayout.showEmpty()
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.8")
            }
            1 -> {//请求专辑下的音频文件
                OkClientHelper.get(this, "user/${data.user_id}/voiceAlbum/${data.id}/voice?lastId=$lastId&recognition=1", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceData
                        if (result.code == 0 && result.data != null) {
                            mData.addAll(result.data)
                            if (TextUtils.isEmpty(lastId)) {
                                seekBar.max = 1000
                                tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(mData[0].voice_len) * 1000).toLong())
                                iv_Privacy.visibility = if (mData[0].is_private == 1) View.VISIBLE else View.GONE
                            }
                            lastId = mData[mData.size - 1].voice_id
                            if (result.data.size >= 10) {
                                request(1)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.8")
            }
            2 -> {
                OkClientHelper.get(this, "user/${data.user_id}/voiceAlbum/${data.id}/voice?lastId=$lastId", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceData
                        if (result.code == 0 && result.data != null) {
                            mData.addAll(result.data)
                            if (TextUtils.isEmpty(lastId)) {
                                seekBar.max = 1000
                                tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(mData[0].voice_len) * 1000).toLong())
                                iv_Privacy.visibility = if (mData[0].is_private == 1) View.VISIBLE else View.GONE
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.8")
            }
        }
    }

    /**
     * 给自己留言
     */
    private fun selfComments(formBody: FormBody) {
        OkClientHelper.post(this, "voice/${mData[current].voice_id}/note", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    try {
                        mData[current].note_num = 1
                        showMenu(mData[current])
                    } catch (e: Exception) {
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    private fun updateCover(formBody: FormBody) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "user/${data.user_id}/voiceAlbum/${data.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code != 0) {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.8")
    }

    private inner class ImgAdapter() : PagerAdapter() {

        private lateinit var data: ArrayList<String>

        constructor(data: ArrayList<String>) : this() {
            this.data = data
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return data.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val iv = ImageView(this@SeasonAlbumDetailsActivity)
            iv.setBackgroundColor(Color.parseColor("#00ffffff"))
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            var url = data[position]
            if (url.contains(".gif")) {
                if (url.contains("?")) {
                    url = url.substring(0, url.indexOf("?"))
                }
            }
            Glide.with(this@SeasonAlbumDetailsActivity)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(url)))
                    .asBitmap()
                    .load(url)
                    .error(Glide.with(this@SeasonAlbumDetailsActivity)
                            .applyDefaultRequestOptions(RequestOptions()
                                    .placeholder(0)
                                    .error(0)
                                    .signature(ObjectKey(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url)))
                            .asBitmap()
                            .load(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url))
                    .into(iv)
            container.addView(iv)
            iv.setOnLongClickListener {
                /**
                 * show save img
                 */
                val loginBean = PreferenceTools.getObj(this@SeasonAlbumDetailsActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                DialogSelectPhoto(this@SeasonAlbumDetailsActivity).hideAction(true)
                        .hindOther(loginBean.user_id == this@SeasonAlbumDetailsActivity.data.user_id.toString())
                        .changeText(if (loginBean.user_id == this@SeasonAlbumDetailsActivity.data.user_id.toString()) "更改默认相片" else "保存图片")
                        .setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                            override fun itemView(view: View) {
                                when (view.id) {
                                    R.id.tv_Save -> {
                                        if (loginBean.user_id == this@SeasonAlbumDetailsActivity.data.user_id.toString()) {
                                            openGallery()
                                        } else {//保存
                                            if (!TextUtils.isEmpty(url)) {
                                                save(url)
                                            }
                                        }
                                    }
                                    R.id.tv_Other -> {//保存
                                        if (!TextUtils.isEmpty(url)) {
                                            save(url)
                                        }
                                    }
                                }
                            }
                        }).show()
                false
            }
            iv.setOnClickListener {
                anim()
            }
            return iv
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(url: String) {
        /*
         * 删除缩略图的 后缀
         */
        val path = if (url.contains("?")) {
            url.substring(0, url.indexOf("?"))
        } else url
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg", true) && !suffix.contains(".png", true) && !suffix.contains(".gif", true)) {
            suffix = "$suffix.png"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            showToast("图片已存在")
            return
        }
        transLayout.showProgress()
        object : AsyncTask<Void, Void, File?>() {

            override fun doInBackground(vararg voids: Void): File? {
                return try {
                    Glide.with(this@SeasonAlbumDetailsActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(path)))
                            .downloadOnly()
                            .load(path)
                            .submit()
                            .get()
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(s: File?) {
                transLayout.showContent()
                if (null == s) {
                    showToast("图片保存失败")
                } else {
                    val bootFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootFile.exists()) {
                        bootFile.mkdirs()
                    }
                    val file = File(bootFile.absolutePath + "/" + suffix)
                    FileUtils.copyFile(s, file)
                    //在手机相册中显示刚拍摄的图片
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun openGallery() {
        startActivityForResult(Intent(this, AlbumActivity::class.java)
                .putExtra("isChat", true)
                .putExtra("count", 1), REQUEST_GALLEY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLEY -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, CropActivity::class.java)
                                    .putExtra("resourceType", "18")
                                    .putExtra("path", result[0]), REQUEST_CROP_BG)
                        }
                    }
                }
                REQUEST_VOICE_SELF -> {
                    data?.let {
                        val voice = it.getStringExtra("voice")
                        val voiceLength = it.getStringExtra("voiceLength")
                        LocalLogUtils.writeLog("AlbumDetails : 给自己留言资源上传成功", System.currentTimeMillis())
                        selfComments(FormBody.Builder().add("bucketId", "${AppTools.bucketId}").add("noteUri", voice).add("noteLen", voiceLength).build())
                    }
                }
                REQUEST_RECORD -> {
                    data?.let {
                        val voice = it.getStringExtra("voice")
                        val voiceLength = it.getStringExtra("voiceLength")
                        LocalLogUtils.writeLog("AlbumDetails : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                        EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                    }
                }
                REQUEST_CROP_BG -> {
                    data?.let {
                        val url = it.getStringExtra("url")
                        this@SeasonAlbumDetailsActivity.data.album_cover_url = url
                        updateCover(FormBody.Builder().add("albumCoverUri", it.getStringExtra("result")).add("bucketId", AppTools.bucketId).build())
                        val event = IUpdateAlbumEvent(2, this@SeasonAlbumDetailsActivity.data.id)
                        event.originSort = this@SeasonAlbumDetailsActivity.data.album_type
                        event.visibleType = this@SeasonAlbumDetailsActivity.data.album_type
                        event.cover = url
                        EventBus.getDefault().post(event)
                    }
                }
            }
        }
    }

    /**
     * 发送socket 回声
     */
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
                    if (current != -1 && current < mData.size) {
                        if (it.voice_id == mData[current].voice_id) {//界面展示与回声为同一条数据 更新界面展示的数据
                            showMenu(mData[current])
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateEvent(event: IUpdateAlbumEvent) {
        if (event.type == 1) {//删除当前专辑
            if (event.id == data.id) {
                finish()
            }
        } else if (event.type == 2) {
            if (event.id == data.id) {
                if (!TextUtils.isEmpty(event.name)) {
                    data.album_name = event.name
                    tv_album_title.text = data.album_name
                }
                if (!TextUtils.isEmpty(event.cover)) {
                    data.album_cover_url = event.cover
                    if (mData.size > 0 && relative_show_copyWriting.visibility != View.VISIBLE) {
                        showItemInfo(mData[current])
                    } else {
                        Glide.with(this).load(data.album_cover_url)
                                .into(ivMachineBg)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSelfComment(event: IUpdataSelfCommentEvent) {
        try {
            if (mData.size > 0) {
                if (mData[current].voice_id == event.voiceId) {
                    if (event.type == 1) {
                        mData[current].note_num += 1
                    } else {
                        mData[current].note_num -= 1
                    }
                    showMenu(mData[current])
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        audioPlayer.stop()
        handler.stop()
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateInteraction(event: UpdateInteraction) {
        try {
            if (!TextUtils.isEmpty(event.interactionsType)) {
                if (!TextUtils.isEmpty(interactName))
                    if (isMatched) {

                    }
            } else if (event.bean != null) {
                if (isLoopInteract) {
                    isMatched = true
                    if (TextUtils.isEmpty(interactId)) {
                        interactId = event.bean.id
                        tv_Loading.visibility = View.GONE
                        interactName = event.bean.nick_name
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun finish() {
        super.finish()
        audioPlayer.stop()
        handler.removeCallbacks(handler)
        handler.removeCallbacksAndMessages(null)
        countDown?.cancel()
        countDown = null
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        try {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (loginBean.user_id == data.user_id.toString()) {
                outAnim(0)
            }
        } catch (e: Exception) {

        }
    }

    private inner class CountDownDelay(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            interaction(1)
        }

        override fun onTick(millisUntilFinished: Long) {

        }
    }

    /**
     * 发了新的心情, 更新日历
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewVoiceEvent(event: OperatorVoiceListEvent) {
        //当月
        if (event.type != 3) {//把数据加入到集合中
            if (mData.size > 0) {
                lastId = mData[mData.size - 1].voice_id
            } else {
                request(0)
            }
            request(2)
        }
    }
}