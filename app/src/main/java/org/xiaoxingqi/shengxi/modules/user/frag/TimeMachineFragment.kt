package org.xiaoxingqi.shengxi.modules.user.frag

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.Message
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
import com.nineoldandroids.animation.ObjectAnimator
import com.romainpiel.shimmer.Shimmer
import kotlinx.android.synthetic.main.frag_time_machine.*
import kotlinx.android.synthetic.main.frag_time_machine.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.BaseObjectActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.*
import org.xiaoxingqi.shengxi.modules.user.CropActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.barrage.BarrageView
import skin.support.SkinCompatManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 时光机
 * 列表循环-> 随机 ->单曲
 * 默认展示全部
 * @DialogEmptyVoiceList
 *
 * 新增垃圾需求
 *          陌生人可以访问 7 30 全部的心情
 *          当设置了仅陌生人可见, 好友通过的请求时,加载数据,
 *          否则,只有当本地集合中没有数据, 才加载列表
 *
 */
class TimeMachineFragment : BaseFragment(), ITabClickCall {

    companion object {
        private const val REQUEST_GALLEY = 0x01
        private const val REQUEST_CROP_BG = 0x02
        private const val MODE_LOOP = 0x01
        private const val MODE_RANDOM = 0x02
        private const val MODE_SINGLE = 0x03
        private const val ALL_KEY = "全部"
        private const val REQUEST_VOICE_SELF = 0x04
    }

    //是否匹配成功
    private var isMatched = false

    //是否需要轮询获取结果, 或者获取结果是否展示
    private var isLoopInteract = false
    private var visible = false
    private var isContinua = true
    private var strangeView = 7//0=禁止，7=默认7天
    override fun tabClick(isVisible: Boolean) {
        if (visible == isVisible) {
            return
        }
        visible = isVisible
        if (!isVisible) {
            try {
                isContinua = false
                if (audioPlayer.isPlaying)
                    audioPlayer.stop()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 分类
     */
    private val map by lazy { LinkedHashMap<String, ArrayList<BaseBean>>() }

    override fun doubleClickRefresh() {

    }

    private var userId: String? = null
    private var lastId: String? = null
    private lateinit var audioPlayer: AudioPlayer
    private var isDrag = false
    private var playMode = 1 //1列表循环 2随机 3单曲循环
    private lateinit var userInfo: UserInfoData

    //播放列表
    private var mData: ArrayList<BaseBean>? = null
    private val random by lazy { Random() }
    private var current = 0
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            if (!isDrag) {
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                mView!!.marqueeText.update(audioPlayer.currentPosition.toInt())
            }
            mView!!.tv_ProgressTime.text = AppTools.parseTime2Str(audioPlayer.currentPosition)
        }
    }
    private var showMachineDialog: DialogShowMachineList? = null
    private lateinit var transLayout: TransLayout

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorEvent(event: SensorChangeMoodEvent) {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        if (headsetOn || a2dpOn || scoOn) {
            return
        }
        if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) {
            return
        }
        if (event.type == 1) {
            if (audioPlayer.isPlaying) {
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else if (event.type == 2) {
            if (audioPlayer.isPlaying) {
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_time_machine
    }

    override fun onResume() {
        super.onResume()
        /**
         * 恢复自己的打招呼界面
         */
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId)
            if (SPUtils.getBoolean(activity, IConstant.IS_CLOSE_INTERACT + loginBean.user_id, true)) {
                //默认打开打招呼的界面
                initInteractView(0)
            }
    }


    override fun initView(view: View?) {
        transLayout = view!!.transLayout
        ObjectAnimator.ofFloat(view.relative_operator_menu, "translationY", AppTools.dp2px(activity, 47).toFloat()).setDuration(0).start()
        view.marqueeText.setAutoGravity(true)
    }

    private var shimmer: Shimmer? = null
    override fun initData() {
        setOperator(object : ItemOperatorAdapter() {
            override fun onDelete(bean: BaseBean?) {

            }

            override fun onRecommend(bean: BaseBean?) {
                bean?.let { showMenu(it) }
            }

            override fun onUnRecommend(bean: BaseBean?) {
                bean?.let { showMenu(it) }
            }

            override fun onthumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onUnThumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
            }

            override fun onComment(from_id: String?) {

            }

            override fun onReport(type: String?) {

            }

            override fun onFriend() {

            }

            override fun onPrivacy(bean: BaseBean?) {

            }
        })
        EventBus.getDefault().register(this)
        map[ALL_KEY] = ArrayList()
        mData = map[ALL_KEY]
        userId = (activity as TimeMachineActivity).userId
        audioPlayer = AudioPlayer(activity)
        playMode = SPUtils.getInt(activity, IConstant.TIMEMACHINEPLAYMODE, 1)
        mView!!.iv_PlayMode.setImageResource(
                when (playMode) {
                    1 -> R.mipmap.icon_play_mode_cycle
                    2 -> R.mipmap.icon_play_mode_random
                    3 -> R.mipmap.icon_play_mode_singl
                    else -> {
                        R.mipmap.icon_play_mode_cycle
                    }
                }
        )
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {//自己
            shimmer = Shimmer()
            shimmer?.duration = 1500L
            mView!!.tv_Loading.reflectionColor = Color.YELLOW
            mView!!.ivArrow.visibility = View.VISIBLE
            userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
            if (!TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                mView!!.tv_SetCover.visibility = View.GONE
                mView!!.tv_LongPressChange.visibility = View.INVISIBLE
                glideUtil.loadGlide(userInfo.data.voice_cover_photo_url, mView!!.ivMachineBg, if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.draw_machine_default_copy else R.drawable.shape_white, glideUtil.getLastModified(userInfo.data.voice_cover_photo_url))
            } else {
                mView!!.tv_LongPressChange.visibility = View.VISIBLE
                mView!!.ivMachineBg.setImageResource(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.shape_white else R.drawable.draw_machine_default_night)
            }
            parseInfo()
            request(2)
            mView!!.linear_msg.visibility = View.VISIBLE
            mView!!.linear_add_album.visibility = View.VISIBLE
            mView!!.tv_SetCover.setBackgroundResource(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.draw_time_machine_self_bg else R.drawable.draw_time_machine_self_bg_night)
            mView!!.tvRecordVoice.visibility = View.VISIBLE
        } else {
            mView!!.linear_msg.visibility = View.GONE
            mView!!.linear_add_album.visibility = View.GONE
            val params = mView!!.tv_SetCover.layoutParams
            params.width = AppTools.dp2px(activity, 238)
            params.height = AppTools.dp2px(activity, 36)
            mView!!.tv_SetCover.layoutParams = params
            mView!!.tv_SetCover.setBackgroundResource(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.shape_stoken_bound_r5_fbf8f5 else R.drawable.shape_stoken_bound_r5_fbf8f5_night)
            mView!!.tv_SetCover.text = resources.getString(R.string.string_time_machine_other_setting_cover)
            request(0)
        }
    }

    override fun onPause() {
        super.onPause()
        isContinua = false
        audioPlayer.stop()
        //如果匹配中, 则退出匹配
        if (mView!!.relativeMatch.visibility != View.GONE) {
            isLoopInteract = false
            outAnim(0)
        }
    }

    private fun parseInfo() {
        try {
            val timeMachine = TimeUtils.getInstance().paserTimeMachine(activity, userInfo.data.created_at)
            mView!!.tv_VoiceTime.text = String.format(resources.getString(R.string.string_since_start), timeMachine.substring(0, timeMachine.indexOf(" ")))
            var limitDay = TimeUtils.getInstance().getLimitDay(userInfo.data.created_at) + 1
            if (limitDay == 0) {
                limitDay = 1
            }
            mView!!.tvTopic.visibility = View.VISIBLE
            mView!!.tvTopic.text = String.format(resources.getString(R.string.string_all_memory_length), limitDay)
        } catch (e: Exception) {
        }
    }

    override fun initEvent() {
        mView!!.iv_PlayMode.setOnClickListener {
            when (playMode) {
                1 -> {
                    playMode = 2
                    mView!!.iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_random)
                }
                2 -> {
                    playMode = 3
                    mView!!.iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_singl)
                }
                3 -> {
                    playMode = 1
                    mView!!.iv_PlayMode.setImageResource(R.mipmap.icon_play_mode_cycle)
                }
            }
            SPUtils.setInt(activity, IConstant.TIMEMACHINEPLAYMODE, playMode)
            showToast(when (playMode) {
                1 -> resources.getString(R.string.string_play_mode_1)
                2 -> resources.getString(R.string.string_play_mode_2)
                3 -> resources.getString(R.string.string_play_mode_3)
                else -> {
                    resources.getString(R.string.string_play_mode_1)
                }
            })
        }
        mView!!.iv_Play.setOnClickListener {
            //开始播放
            if (audioPlayer.isPlaying) {
                isContinua = false
                audioPlayer.stop()
            } else {
                isContinua = true
                if (mData?.size!! > 0) {
                    if (current == -1) {
                        current = 0
                    }
                    try {
                        download(mData!![current])
                    } catch (e: Exception) {
                    }
                } else {
                    DialogEmptyVoiceList(activity!!).isSelf(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id == userId).setOnClickListener(View.OnClickListener {
                        startActivity(Intent(activity, SendAct::class.java).putExtra("type", 1))
                        activity!!.overridePendingTransition(R.anim.operate_enter, 0)
                    }).show()
                }
            }
        }
        mView!!.iv_Pre.setOnClickListener {
            //随机模式依然随机

            if (null == mData || mData?.size == 0) {
                DialogEmptyVoiceList(activity!!).isSelf(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id == userId).setOnClickListener(View.OnClickListener {
                    startActivity(Intent(activity, SendAct::class.java).putExtra("type", 1))
                    activity!!.overridePendingTransition(R.anim.operate_enter, 0)
                }).show()
                return@setOnClickListener
            }
            if (playMode == 2) {
                current = random.nextInt(mData!!.size)
            } else {
                current--
                if (current < 0) {
                    current = 0
                    return@setOnClickListener
                }
            }
            isContinua = true
            audioPlayer.stop()
            mView!!.seekBar.progress = 0
            try {
                download(mData!![current])
            } catch (e: Exception) {
            }
        }
        mView!!.iv_Next.setOnClickListener {
            //随机模式的时候 依然随机
            if (null == mData || mData?.size == 0) {
                DialogEmptyVoiceList(activity!!).isSelf(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id == userId).setOnClickListener(View.OnClickListener {
                    startActivity(Intent(activity, SendAct::class.java).putExtra("type", 1))
                    activity!!.overridePendingTransition(R.anim.operate_enter, 0)
                }).show()
                return@setOnClickListener
            }
            if (playMode == 2) {
                current = random.nextInt(mData!!.size)
            } else {
                current++
                if (current >= mData!!.size) {
                    current = 0
                }
            }
            isContinua = true
            audioPlayer.stop()
            mView!!.seekBar.progress = 0
            try {
                download(mData!![current])
            } catch (e: Exception) {
            }
        }
        mView!!.iv_VoiceList.setOnClickListener {
            if (null == showMachineDialog) {
                showMachineDialog = DialogShowMachineList(activity!!).isSelf(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id == userId)
                showMachineDialog?.setStrange(strangeView)
                showMachineDialog?.setData(map)
                showMachineDialog?.setLoadmoreListener(object : DialogLoadMoreListener {
                    override fun onClickItem(position: Int) {
                        /**
                         * 播放指定的角标
                         */
                        current = position
                        audioPlayer.stop()
                        mView!!.seekBar.progress = 0
                        if (mData != null)
                            if (position < mData!!.size) {
                                try {
                                    download(mData!![position])
                                } catch (e: Exception) {
                                }
                            }
                    }

                    override fun changeData(key: String?) {
                        current = 0
                        mData = map[key]
                        if (audioPlayer.isPlaying) {
                            audioPlayer.stop()
                        }
                        mView!!.seekBar.progress = 0
                        mView!!.tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(mData!![0].voice_len) * 1000).toLong())
                        showItemInfo(mData!![0])
                        mView!!.iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    }
                })
            }
            showMachineDialog?.updatePlayPosition(current)
            showMachineDialog?.show()
        }
        mView!!.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (audioPlayer.duration > 0L)
                    mView!!.marqueeText.update((progress / 1000f * audioPlayer.duration).toInt())
                else {
                    mView!!.marqueeText.update((progress / 1000f * mData!![current].allDuration).toInt())
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
                            mView!!.tv_ProgressTime.text = (seekBar!!.progress / 1000f * audioPlayer.duration).toString()
                        else {
                            mView!!.tv_ProgressTime.text = AppTools.parseTime2Str((seekBar!!.progress / 1000f * Integer.parseInt(mData!![current].voice_len)).toLong() * 1000)
                        }
                    } catch (e: Exception) {
                        mView!!.tv_ProgressTime.text = "00:00"
                    }
                }
            }
        })
        mView!!.tv_AddFriends.setOnClickListener {
            requestFriends()
        }
        mView!!.view_resource_type.setOnClickListener {
            anim()
        }
        mView!!.ivMachineBg.setOnClickListener {
            anim()
        }
        mView!!.view_resource_type.setOnLongClickListener {
            longClickSave()
            false
        }
        mView!!.ivMachineBg.setOnLongClickListener {
            /**
             * show save img
             */
            longClickSave()
            false
        }
        //点赞
        mView!!.relativeEcho.setOnClickListener {
            /**
             * 判断当前的归属
             */
            mData?.let {
                if (current != -1 && current < it.size) {
                    if (!TextUtils.isEmpty(it[current].is_shared)) {//共享
                        if (audioPlayer.isPlaying) {
                            audioPlayer.stop()
                            iv_Play.setImageResource(R.mipmap.icon_play_pause)
                        }
                        queryCount(it[current], mView!!.tv_Echo, mView!!.iv_Thumb)
                    } else {//共鳴
                        if (it[current].isNetStatus) {
                            return@setOnClickListener
                        }
                        if (it[current].is_collected == 1) {
//                            transLayout.showProgress()
                            it[current].isNetStatus = true
                            unThumb(it[current], mView!!.iv_Thumb)
                        } else {
                            transLayout.showProgress()
                            tuhmb(it[current], mView!!.iv_Thumb)
                        }
                    }
                }
            }
        }
        //回声或者进入单页
        mView!!.lineaer_Recommend.setOnClickListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                mView!!.iv_Play.setImageResource(R.mipmap.icon_play_pause)
            }
            mData?.let {
                if (current != -1 && current < it.size) {
                    if (!TextUtils.isEmpty(it[current].is_shared)) {
                        startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                                .putExtra("id", it[current].voice_id)
                                .putExtra("uid", it[current].user.id)
                                .putExtra("isExpend", it[current].chat_num > 0)
                        )
                    } else {
                        if (it[current].dialog_num == 0) {
                            queryPermission(it[current], transLayout, AppTools.fastJson(it[current].user_id.toString(), 1, it[current].voice_id))
                        } else {
                            startActivity(Intent(activity, TalkListActivity::class.java)
                                    .putExtra("voice_id", it[current].voice_id)
                                    .putExtra("chat_id", it[current].chat_id)
                                    .putExtra("uid", it[current].user_id.toString())
                            )
                        }
                    }
                }
            }
        }
        mView!!.viewImgPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                mView!!.tv_count.text = "${position + 1}/${mView!!.viewImgPager.adapter?.count}"
            }
        })
        mView!!.linear_msg.setOnClickListener {
            /**
             * 给自己留言
             */
            if (mData!![current].note_num > 0) {
                startActivity(Intent(activity, SelfMsgActivity::class.java).putExtra("voiceId", mData!![current].voice_id))
            } else {
                startActivityForResult(Intent(activity, RecordVoiceActivity::class.java)
                        .putExtra("isSelf", true)
                        .putExtra("resourceType", "13"), REQUEST_VOICE_SELF)
                activity!!.overridePendingTransition(0, 0)
            }
        }
        mView!!.linear_add_album.setOnClickListener {
            startActivity(Intent(activity, DialogAddAlbumActivity::class.java).putExtra("voiceId", mData!![current].voice_id))
        }
        mView!!.ivArrow.setOnClickListener {
            /**
             * 扩散动画 展示隐藏View
             */
            initInteractView(500)
            SPUtils.setBoolean(activity, IConstant.IS_CLOSE_INTERACT + PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id, true)
        }
        mView!!.tvHide.setOnClickListener {
            //隐藏
            SPUtils.setBoolean(activity, IConstant.IS_CLOSE_INTERACT + PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id, false)
            outAnim(500)
        }
        mView!!.tvRecordVoice.setOnClickListener {
            startActivity(Intent(context, RecordTransparentActivity::class.java)
                    .putExtra("type", 1))
            activity?.overridePendingTransition(0, 0)
        }
        mView!!.barrageView.setOnDelayListener(object : BarrageView.OnDelayListener {
            override fun onDelay() {
                interaction(1)
            }
        })
    }

    private fun longClickSave() {
        try {
            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            DialogSelectPhoto(activity!!).hideAction(true)
                    .hindOther(loginBean.user_id == userId)
                    .changeText(if (loginBean.user_id == userId) "更改默认相片" else "保存图片")
                    .setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                        override fun itemView(view: View) {
                            when (view.id) {
                                R.id.tv_Save -> {
                                    if (loginBean.user_id == userId) {
                                        openGallery()
                                    } else {//保存
                                        if (!TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                                            save(userInfo.data.voice_cover_photo_url)
                                        } else {
                                            showToast("Ta还没设置封面")
                                        }
                                    }
                                }
                                R.id.tv_Other -> {//保存
                                    if (!TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                                        save(userInfo.data.voice_cover_photo_url)
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

    /**
     * 初始化默认打开界面, 并开始匹配
     */
    private fun initInteractView(duration: Long) {
        shimmer?.start(mView!!.tv_Loading)
        mView!!.relativeMatch.visibility = View.VISIBLE
        try {
            val animator = ViewAnimationUtils.createCircularReveal(mView!!.relativeMatch, AppTools.getWindowsWidth(activity), 0, 0f, sqrt(AppTools.getWindowsWidth(activity).toDouble().pow(2.0) * 2).toFloat())
            animator.duration = duration
            animator.start()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mView!!.tvHide.isEnabled = true
                }
            })
        } catch (e: Exception) {
            mView!!.tvHide.isEnabled = true
        }
        mView!!.ivArrow.isEnabled = false
        mView!!.ivArrow.visibility = View.GONE

        mView!!.tvHide.visibility = View.VISIBLE
        mView!!.tvHide.alpha = 0f
        val alphaAnim = ObjectAnimator.ofFloat(0f, 1f).setDuration(duration)
        alphaAnim.addUpdateListener {
            val alpha = it.animatedValue as Float
            mView!!.tvHide.alpha = alpha
        }
        alphaAnim.start()
        mView!!.tv_Loading.visibility = View.VISIBLE
        interaction(1)
    }

    private fun notifyPage() {
        mView!!.relativeMatch.visibility = View.GONE
        mView!!.ivArrow.visibility = View.VISIBLE
        mView!!.ivArrow.isEnabled = true
        isMatched = false
    }

    /**
     * 退出匹配的动画
     */
    private fun outAnim(duration: Long) {
        shimmer?.cancel()
        isLoopInteract = false
        interaction(2)
        mView!!.barrageView.stop()
        mView!!.tvHide.isEnabled = false
        if (mView!!.tv_Loading.visibility != View.GONE) {
            mView!!.tv_Loading.visibility = View.GONE
        }
        if (mView!!.tvHide.visibility != View.GONE)
            mView!!.tvHide.visibility = View.GONE
        if (duration == 0L) {
            notifyPage()
        } else {
            val animator = ViewAnimationUtils.createCircularReveal(mView!!.relativeMatch, AppTools.getWindowsWidth(activity), 0, sqrt(AppTools.getWindowsWidth(activity).toDouble().pow(2.0) * 2).toFloat(), 0f)
            animator.duration = duration
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    notifyPage()
                }
            })
            animator.start()
        }

        interactId = null
        interactName = null
    }

    private var interactId: String? = null
    private var interactName: String? = null

    private fun anim() {
        if (mView!!.relative_operator_menu.isSelected)
            if (mView!!.relative_operator_menu.y == AppTools.getWindowsWidth(activity).toFloat()) {
                val animtor = ObjectAnimator.ofFloat(mView!!.relative_operator_menu, "translationY", 0f).setDuration(220)
                animtor.start()
            } else {
                val animtor = ObjectAnimator.ofFloat(mView!!.relative_operator_menu, "translationY", AppTools.dp2px(activity, 47).toFloat()).setDuration(220)
                animtor.start()
            }
        else {
            ObjectAnimator.ofFloat(mView!!.relative_operator_menu, "translationY", AppTools.dp2px(activity, 47).toFloat()).setDuration(0).start()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun openGallery() {
        startActivityForResult(Intent(activity, AlbumActivity::class.java)
                .putExtra("isChat", true)
                .putExtra("count", 1), REQUEST_GALLEY)
    }

    private fun blur(url: String?, iv: ImageView) {
        if (TextUtils.isEmpty(url)) {
            mView!!.iv_Bg.setImageBitmap(null)
            iv.setImageResource(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.draw_machine_default_copy else R.drawable.draw_machine_default_night)
        } else {
            val errorPath = if (url!!.contains("?")) url.substring(0, url.indexOf("?")) else url
            Glide.with(activity!!)
                    .asBitmap()
                    .apply(RequestOptions()
                            .signature(ObjectKey(if (iv == this.mView!!.ivMachineBg) glideUtil.getLastModified(url) else url)))
                    .load(url)
                    .error(Glide.with(activity!!)
                            .applyDefaultRequestOptions(RequestOptions()
                                    .signature(ObjectKey(errorPath)))
                            .asBitmap()
                            .listener(object : RequestListener<Bitmap> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    try {
                                        val fastblur = FastBlur().fastblur(resource, 50, mView!!.iv_Bg)
                                        mView!!.iv_Bg.setImageBitmap(fastblur)
                                    } catch (e: Exception) {

                                    }
                                    return false
                                }
                            })
                            .load(errorPath))
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            try {
                                val fastblur = FastBlur().fastblur(resource, 50, mView!!.iv_Bg)
                                mView!!.iv_Bg.setImageBitmap(fastblur)
                            } catch (e: Exception) {

                            }
                            return false
                        }
                    })
                    .into(iv)
        }
    }

    /**
     * 下载资源
     * 下载过程中不允许点击,或者下载过程中切换了播放的资源, 继续下载但是下载之后不播放
     */
    @SuppressLint("SetTextI18n")
    private fun download(item: BaseBean) {
        mView!!.view_resource_type.visibility = View.GONE
        if (!mView!!.relative_operator_menu.isSelected) {
            val animtor = ObjectAnimator.ofFloat(mView!!.relative_operator_menu, "translationY", 0f).setDuration(220)
            mView!!.relative_operator_menu.isSelected = true
            animtor.start()
        }
        mView!!.marqueeText.text = item.recognition_content ?: ""
        showItemInfo(item)
        try {
            mView!!.tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(item.voice_len) * 1000).toLong())
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast("路径出错")
                return
            }
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                if (BaseObjectActivity.currentMode == MODE_EARPIECE)
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                else
                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                mView!!.loadingStatus.visibility = View.VISIBLE
                mView!!.progress.visibility = View.VISIBLE
                OkClientHelper.downFile(activity, item.voice_url, { o ->
                    mView!!.loadingStatus.visibility = View.GONE
                    mView!!.progress.visibility = View.GONE
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downFile
                        }
                        audioPlayer.setDataSource(o.toString())
                        if (BaseObjectActivity.currentMode == MODE_EARPIECE)
                            audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                        else
                            audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { volleyError ->
                    showToast(VolleyErrorHelper.getMessage(volleyError))
                    mView!!.loadingStatus.visibility = View.GONE
                    mView!!.progress.visibility = View.GONE
                })
            }
            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                override fun onPrepared() {
                    item.isPlaying = true
                    if (audioPlayer.duration > 0) {
                        item.allDuration = audioPlayer.duration
                    }
                    mView!!.marqueeText.calcDx(audioPlayer.duration.toInt())
                    showMachineDialog?.let {
                        if (it.isShowing) {
                            it.updatePlayPosition(current)
                        }
                    }
                   /* if (!TextUtils.isEmpty(item.is_shared)) {
                        if (TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()) != SPUtils.getString(activity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, "")) {
                            //提交成就播放
                            pushTravel(userId!!)
                        }
                    }*/
                    mView!!.iv_Play.setImageResource(R.mipmap.icon_play_playing)
                    audioPlayer.seekTo((mView!!.seekBar.progress / 1000f * if (item.allDuration != 0L) {
                        item.allDuration.toInt()
                    } else {
                        Integer.parseInt(item.voice_len) * 1000
                    }).toInt())
                    progressHandler.start()
                }

                @SuppressLint("SetTextI18n")
                override fun onCompletion() {
                    mView!!.marqueeText.reset()
                    if (TextUtils.isEmpty(item.is_shared) && item.is_collected != 1) {
                        tuhmb(item, null, 5)
                    }
                    item.isPlaying = false
                    mView!!.seekBar.progress = 0
                    if (!isContinua)
                        mView!!.iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    progressHandler.stop()
                    mView!!.tv_ProgressTime.text = "00:00"
                    if (mData != null && mData!!.size > 0) {
                        when (playMode) {
                            MODE_LOOP -> {
                                current++
                                if (current == mData!!.size) {
                                    current = 0
                                }
                                try {
                                    download(mData!![current])
                                } catch (e: Exception) {
                                }
                            }
                            MODE_RANDOM -> {
                                current = random.nextInt(mData!!.size)
                                try {
                                    download(mData!![current])
                                } catch (e: Exception) {
                                }
                            }
                            MODE_SINGLE -> {
                                if (current != -1) {
                                    try {
                                        download(mData!![current])//當去播放這一條
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onInterrupt() {
                    item.isPlaying = false
                    if (!isContinua)
                        mView!!.iv_Play.setImageResource(R.mipmap.icon_play_pause)
                    progressHandler.stop()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 展示当前条目的信息
     */
    @SuppressLint("SetTextI18n")
    private fun showItemInfo(item: BaseBean) {
        showMenu(item)
        try {
            if (item.img_list != null) {
                mView!!.tv_count.visibility = View.VISIBLE
                mView!!.tv_count.text = "1/${item.img_list.size}"
                mView!!.iv_MoviesCover.visibility = View.GONE
                mView!!.iv_Music.visibility = View.GONE
                mView!!.viewImgPager.adapter = ImgAdapter(mData!![current].img_list)
                blur(item.img_list[0], mView!!.ivMachineBg)
                mView!!.ivMachineBg.visibility = View.GONE
            } else {
                mView!!.tv_count.visibility = View.GONE
                mView!!.viewImgPager.adapter = null
                if (item.resource != null) {
                    mView!!.view_resource_type.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                        mView!!.tv_SetCover.visibility = View.GONE
                        mView!!.tv_LongPressChange.visibility = View.INVISIBLE
                    }
                    mView!!.iv_MoviesCover.visibility = View.VISIBLE
                    when (item.resource_type) {
                        1 -> {
                            mView!!.iv_MoviesCover.visibility = View.VISIBLE
                            blur(item.resource.movie_poster, mView!!.iv_MoviesCover)
                            mView!!.iv_Music.visibility = View.GONE
                        }
                        2 -> {
                            mView!!.iv_Music.visibility = View.GONE
                            blur(item.resource.book_cover, mView!!.iv_MoviesCover)
                            mView!!.iv_MoviesCover.visibility = View.VISIBLE
                        }
                        else -> {
                            mView!!.iv_MoviesCover.visibility = View.GONE
                            mView!!.iv_Music.visibility = View.VISIBLE
                            blur(item.resource.song_cover, mView!!.iv_Music)
                        }
                    }
                    mView!!.ivMachineBg.visibility = View.GONE
                } else {
                    blur(userInfo.data.voice_cover_photo_url, mView!!.ivMachineBg)
                    mView!!.iv_MoviesCover.visibility = View.GONE
                    mView!!.iv_Music.visibility = View.GONE
                    mView!!.ivMachineBg.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                        mView!!.tv_SetCover.visibility = View.VISIBLE
                        if (mView!!.tv_LongPressChange.visibility == View.INVISIBLE)
                            mView!!.tv_LongPressChange.visibility = View.VISIBLE
                    }
                }
            }
            mView!!.tv_VoiceTime.text = TimeUtils.getInstance().paserTimeMachine(activity, item.created_at)
            if (!TextUtils.isEmpty(item.topic_name)) {
                mView!!.tvTopic.text = "#${item.topic_name}#"
                mView!!.tvTopic.visibility = View.VISIBLE
            } else {
                mView!!.tvTopic.visibility = View.GONE
                mView!!.tvTopic.text = ""
            }
            mView!!.iv_Privacy.visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
        } catch (e: Exception) {
        }
    }

    /**
     * 展示回声 共鸣的按钮
     */
    @SuppressLint("SetTextI18n")
    private fun showMenu(item: BaseBean) {
        try {
            if (!TextUtils.isEmpty(item.is_shared)) {
                mView!!.iv_Thumb.setImageResource(R.drawable.selector_white_photo_share)
                mView!!.iv_Thumb.isSelected = item.is_shared == "1"
                if (item.is_shared == "1") {
                    mView!!.tv_Echo.text = "撤回"
                } else {
                    mView!!.tv_Echo.text = "共享"
                }
                mView!!.tv_Recommend.text = resources.getString(R.string.string_echoing) + if (item.chat_num <= 0) {
                    ""
                } else {
                    " " + item.chat_num
                }
                mView!!.tv_msg.text = resources.getString(R.string.string_self_comment) + if (item.note_num > 0) item.note_num else ""
            } else {
                mView!!.iv_Thumb.isSelected = item.is_collected == 1
                mView!!.iv_Thumb.setImageResource(R.drawable.selector_white_photo_thumb)
                mView!!.tv_Echo.text = resources.getString(R.string.string_gongming)
                if (item.dialog_num <= 0) {
                    mView!!.tv_Recommend.text = resources.getString(R.string.string_echoing)
                } else {
                    mView!!.tv_Recommend.text = "${resources.getString(R.string.string_Talks)} " + item.dialog_num
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun requestFriends() {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("toUserId", userId)
                .build()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    mView!!.tv_AddFriends.isSelected = true
                    mView!!.tv_AddFriends.text = resources.getString(R.string.string_pending)
                    EventBus.getDefault().post(INotifyFriendStatus(1, userId))
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                userId?.let { it1 -> addWhiteBlack(it1) }
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else
                        showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 互动网络请求
     * 如果在15s内请求退出, 再次进来, 如果算15s ,那永远只能获取到稻草人
     */
    private fun interaction(type: Int) {
        when (type) {
            0 -> {//获取结果
                OkClientHelper.get(activity, "users/${userId}/interactions", SearchUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SearchUserData
                        if (result.code == 0) {
                            result.data?.let {
                                mView!!.barrageView.setData(it.map { bean ->
                                    bean.nick_name
                                }.toTypedArray())
                                mView!!.barrageView.setStart()
                            }
                            if (result.data == null || result.data.isEmpty()) {//下一轮无数据
                                mView!!.tv_Loading.visibility = View.VISIBLE
                            } else {
                                mView!!.tv_Loading.visibility = View.INVISIBLE
                            }
                        }
                        if (result.data == null) {
                            progressHandler.postDelayed({
                                interaction(1)
                            }, 15000)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.tv_Loading.visibility = View.VISIBLE
                    }
                }, "V4.2")
            }
            1 -> {
                OkClientHelper.post(activity, "interaction", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            isLoopInteract = true
                            if (mView!!.tv_Loading.visibility != View.INVISIBLE)
                                mView!!.tv_Loading.visibility = View.VISIBLE
                            interaction(0)
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.1")
            }
            2 -> {
                OkClientHelper.delete(activity, "interaction", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            progressHandler.removeCallbacksAndMessages(null)
                            if (audioPlayer.isPlaying) {
                                progressHandler.start()
                            }
                            if (mView!!.tv_Loading.visibility != View.GONE)
                                mView!!.tv_Loading.visibility = View.GONE
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

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "users/$userId", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        userInfo = result as UserInfoData
                        if (userInfo.code == 0) {
                            if (!TextUtils.isEmpty(userInfo.data.voice_cover_photo_url)) {
                                mView!!.tv_SetCover.visibility = View.GONE
                                mView!!.tv_LongPressChange.visibility = View.INVISIBLE
                                glideUtil.loadGlide(userInfo.data.voice_cover_photo_url, mView!!.ivMachineBg, if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.shape_white else R.drawable.draw_machine_default_night, glideUtil.getLastModified(userInfo.data.voice_cover_photo_url))
                            } else {
                                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                                mView!!.ivMachineBg.setImageResource(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                                    if (loginBean.user_id == userId)
                                        R.drawable.shape_white
                                    else R.drawable.draw_machine_default_copy
                                else R.drawable.draw_machine_default_night)
                            }
                            request(2)
                            if (userInfo.data.relation_status != 1) {
                                if (PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id != userId) {
                                    queryRelation()
                                    request(1)
                                }
                            } else {
                                strangeView = -1
                            }
                            parseInfo()
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            1 -> {//查询用户的设置
                OkClientHelper.get(activity, "users/$userId/settings?settingName=voice_visible_days&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
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
                            mView!!.relativeEmpty_Strange.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            else -> {
                OkClientHelper.get(activity, "users/$userId/voices?lastId=$lastId&sort=asc&moduleId=1&recognition=1", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        (result as VoiceData)
                        if (result.data != null) {
                            map.forEach {
                                it.value.clear()
                            }
                            showMachineDialog?.notifyAllResource()
                            for (bean in result.data) {
                                val machine = TimeUtils.getInstance().timeMachine(bean.created_at)
                                if (map.containsKey(machine)) {
                                    map[machine]?.add(bean)
                                } else {
                                    map[machine] = ArrayList()
                                    map[machine]?.add(bean)
                                    if (null != showMachineDialog) {
                                        showMachineDialog?.notifyNewMonth(machine, true)
                                    }
                                }
                                map[ALL_KEY]?.add(bean)
                                if (null != showMachineDialog) {
                                    showMachineDialog?.updateData(bean)
                                }
                            }
                            if (mData != null && mData!!.size > 0) {
                                mView!!.seekBar.max = 1000
                                mView!!.tv_Time.text = AppTools.parseTime2Str((Integer.parseInt(mData!![0].voice_len) * 1000).toLong())
                            }
                            if (result.data.size >= 10) {
                                lastId = result.data[result.data.size - 1].voice_id
                                loadSilent()
                            }
                        } else {
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (AppTools.isNetOk(activity)) {
                            showToast(any.toString())
                        }
                    }
                })
            }
        }
    }

    /**
     * 静默加载更多，直到没有数据为止,此过程不能中断
     */
    private fun loadSilent() {
        OkClientHelper.get(activity, "users/$userId/voices?lastId=$lastId&sort=asc&moduleId=1&recognition=1", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                if (result.code == 0) {
                    if (result.data != null) {
                        for (bean in result.data) {
                            val machine = TimeUtils.getInstance().timeMachine(bean.created_at)
                            if (map.containsKey(machine)) {
                                map[machine]?.add(bean)
                            } else {
                                map[machine] = ArrayList()
                                map[machine]?.add(bean)
                                if (null != showMachineDialog) {
//                                    if (showMachineDialog?.isShowing!!) {}
                                    showMachineDialog?.notifyNewMonth(machine, true)
                                }
                            }
                            map[ALL_KEY]?.add(bean)
                            if (null != showMachineDialog) {
                                if (showMachineDialog?.isShowing!!) {
                                    showMachineDialog?.updateData(bean)
                                }
                            }
                        }
                        if (result.data.size >= 10) {
                            lastId = result.data[result.data.size - 1].voice_id
                            loadSilent()
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {
            }
        })
    }

    private fun queryRelation() {
        transLayout.showProgress()
        OkClientHelper.get(activity, "relations/$userId", RelationData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                /**
                 * 查询用户关系, 是否是好友
                 */
                result as RelationData
                if (result.code == 0) {
                    if (result.data.friend_status == 1) {
                        mView!!.tv_AddFriends.text = resources.getString(R.string.string_pending)
                        mView!!.tv_AddFriends.isSelected = true
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })

    }

    /**
     * 更新用户的时光机封面
     */
    private fun updateUserInfo(formBody: FormBody) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(activity, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("默认相片更换成功")
                    mView!!.tv_SetCover.visibility = View.GONE
                    mView!!.tv_LongPressChange.visibility = View.INVISIBLE
                    queryInfo()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun queryInfo() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "users/${loginBean.user_id}", UserInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserInfoData
                if (result.code == 0) {
                    userInfo = result
                    glideUtil.loadGlide(userInfo.data.voice_cover_photo_url, mView!!.ivMachineBg, -1, glideUtil.getLastModified(userInfo.data.voice_cover_photo_url))
                    PreferenceTools.saveObj(activity, IConstant.USERCACHE, result)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 给自己留言
     */
    private fun selfComments(formBody: FormBody) {
        OkClientHelper.post(activity, "voice/${mData!![current].voice_id}/note", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    try {
                        mData!![current].note_num = 1
                        showMenu(mData!![current])
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

            val iv = ImageView(activity)
            iv.setBackgroundColor(Color.parseColor("#00ffffff"))
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            var url = data[position]
            if (url.contains(".gif")) {
                if (url.contains("?")) {
                    url = url.substring(0, url.indexOf("?"))
                }
            }
            Glide.with(activity!!)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(url)))
                    .asBitmap()
                    .load(url)
                    .error(Glide.with(activity!!)
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
                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                DialogSelectPhoto(activity!!).hideAction(true)
                        .hindOther(loginBean.user_id == userId)
                        .changeText(if (loginBean.user_id == userId) "更改默认相片" else "保存图片")
                        .setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                            override fun itemView(view: View) {
                                when (view.id) {
                                    R.id.tv_Save -> {
                                        if (loginBean.user_id == userId) {
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
        transLayout.showProgress()
        val path = url
        var suffix = System.currentTimeMillis().toString() + path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg", true) && !suffix.contains(".png", true) && !suffix.contains(".gif", true)) {
            suffix = "$suffix.png"
        }
        val bootfile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
        if (!bootfile.exists()) {
            bootfile.mkdirs()
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        object : AsyncTask<Void, Void, Boolean>() {

            override fun doInBackground(vararg voids: Void): Boolean? {
                try {
                    val url = URL(url)
                    val cont = url.openConnection() as HttpURLConnection
                    if (cont.responseCode == 200) {
                        /**
                         * 下载数据
                         */
                        val ins = cont.inputStream
                        val fos = FileOutputStream(file)
                        val bytes = ByteArray(1024)
                        var len = 0
                        var current = 0
                        ins.use {
                            fos.use {
                                while (ins.read(bytes).also { len = it } != -1) {
                                    it.write(bytes, 0, len)
                                    it.flush()
                                    current += len
                                }
                            }
                        }
                        fos.close()
                        ins.close()
                        return true
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
                return false
            }

            override fun onPostExecute(s: Boolean?) {
                transLayout.showContent()
                if (!s!!) {
                    showToast("图片保存失败")
                } else {
                    //在手机相册中显示刚拍摄的图片
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    activity!!.sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLEY -> data?.let {
                    val result = it.getSerializableExtra("result") as ArrayList<String>
                    if (result != null && result.size > 0) {
                        startActivityForResult(Intent(activity, CropActivity::class.java)
                                .putExtra("resourceType", "10")
                                .putExtra("path", result[0]), REQUEST_CROP_BG)
                    }
                }
                REQUEST_CROP_BG -> data?.let {
                    val result = it.getStringExtra("result")
                    val formBody = FormBody.Builder()
                            .add("voiceCoverPhotoUri", result)
                            .add("bucketId", AppTools.bucketId)
                            .build()
                    updateUserInfo(formBody)
                }
                REQUEST_RECORD -> data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("TimeMachine : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
                REQUEST_VOICE_SELF -> {//给自己留言
                    data?.let {
                        val voice = it.getStringExtra("voice")
                        val voiceLength = it.getStringExtra("voiceLength")
                        LocalLogUtils.writeLog("TimeMachine : 给自己留言资源上传成功", System.currentTimeMillis())
                        selfComments(FormBody.Builder().add("bucketId", "${AppTools.bucketId}").add("noteUri", voice).add("noteLen", voiceLength).build())
                    }
                }
            }
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
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (it.user_id.toString() == loginBean.user_id) {
                        it.chat_num++
                    } else {
                        it.dialog_num++
                    }
                    mData?.let { list ->
                        if (current != -1 && current < list.size) {
                            if (it.voice_id == list[current].voice_id) {//界面展示与回声为同一条数据 更新界面展示的数据
                                showMenu(list[current])
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 1) {
            try {
                mView!!.tv_AddFriends.isSelected = true
                mView!!.tv_AddFriends.text = resources.getString(R.string.string_pending)
            } catch (e: Exception) {
            }
        } else if (event.status == 2) {
            try {
                if (event.userId == userId) {
                    lastId = null
                    mView!!.relativeEmpty_Strange.visibility = View.GONE
                    //成为好友,仅当 数据为空才请求数据
                    strangeView = -1
                    showMachineDialog?.setStrange(strangeView)
                    if (map[ALL_KEY]?.size == 0) {
                        request(2)
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: OperatorVoiceListEvent) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        try {
            if (loginBean.user_id == userId) {
                if (event.type == 3) {//删除声兮
                    /**
                     * 1.遍历列表查询
                     * 2.删除所有key值下的数据
                     * 3.是否在播放中
                     */
                    if (map[ALL_KEY]!!.size > 0) {
                        var tempBean: BaseBean? = null
                        for (bean in map[ALL_KEY]!!) {
                            if (bean.voice_id == event.voice_id) {
                                tempBean = bean
                                break
                            }
                        }
                        if (tempBean != null) {
                            /** 如果移除的为当前正在播放的数据, 则默认展示null
                             * 如果列表为空, 则展示'00:00'
                             */
                            mData?.let {
                                if (it.indexOf(tempBean!!) == current) {
                                    if (it.size > 1 && it.size > current + 1) {
                                        /**
                                         * 重新设置界面信息
                                         */
                                        try {
                                            showItemInfo(it[current + 1])
                                        } catch (e: Exception) {
                                            mView!!.seekBar.progress = 0
                                            mView!!.tv_Time.text = "00:00"
                                            mView!!.seekBar.max = 0
                                            parseInfo()
                                            mView!!.relative_operator_menu.isSelected = false
                                            anim()
                                        }
                                    } else {
                                        mView!!.seekBar.progress = 0
                                        mView!!.tv_Time.text = "00:00"
                                        mView!!.seekBar.max = 0
                                        parseInfo()
                                        mView!!.relative_operator_menu.isSelected = false
                                        anim()
                                    }
                                }
                            }
                            val key = TimeUtils.getInstance().timeMachine(tempBean.created_at)
                            if (map.containsKey(key) && map[key]!!.contains(tempBean)) {
                                map[key]!!.remove(tempBean)
                                if (map[key]!!.size == 0) {
                                    map.remove(key)
                                    showMachineDialog?.notifyNewMonth(TimeUtils.getInstance().timeMachine(tempBean.created_at), false)
                                }
                            }
                            map[ALL_KEY]!!.remove(tempBean)
                            if (null != showMachineDialog) {
                                showMachineDialog!!.deleteNotify(tempBean)
                            }
                        }
                    }
                } else {
                    lastId = null
                    if (audioPlayer.isPlaying) {
                        isContinua = false
                        audioPlayer.stop()
                    }
                    request(2)
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
        if (!visible) {
            isContinua = false
            audioPlayer.stop()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun secondary(event: EventVoiceBean) {
        /**
         * 获取月份的key
         */
        val machine = TimeUtils.getInstance().timeMachine(event.create_at)
        map[machine]?.let {
            for (bean in it) {
                if (bean.voice_id == event.voiceId) {
                    if (event.type == 1) {
                        bean.is_shared = event.isShare
                    } else if (event.type == 2) {
                        bean.is_collected = event.isCollected
                    } else if (event.type == 3) {
                        bean.is_private = event.isPrivacy
                    } else if (event.type == 4) {
                        if (TextUtils.isEmpty(bean.is_shared)) {//别人 则是dialog_num
                            bean.dialog_num = event.dialogNum
                        } else {//自己
                            bean.chat_num = event.dialogNum
                        }
                    }
                    showItemInfo(bean)
                    break
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSelfComment(event: IUpdataSelfCommentEvent) {
        try {
            mData?.let {
                if (it.size > 0) {
                    if (it[current].voice_id == event.voiceId) {
                        if (event.type == 1) {
                            it[current].note_num += 1
                        } else {
                            it[current].note_num -= 1
                        }
                        showMenu(it[current])
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateInteraction(event: UpdateInteraction) {
        try {
            if (!TextUtils.isEmpty(event.interactionsType)) {
                if (!TextUtils.isEmpty(interactName))
                    if (isMatched) {

                    }
            } else if (event.bean != null) {
                /**
                 * 判断是否匹配上了其他用户
                 */
                if (isLoopInteract) {
                    isMatched = true
                    if (TextUtils.isEmpty(interactId)) {

                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    /**
     * 退出匹配队列, 初始化界面
     */
    fun quitMatch() {
        if (isLoopInteract)
            outAnim(0)
    }

    fun restoreMatch() {
        if (visible) {
            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (loginBean.user_id == userId)
                if (SPUtils.getBoolean(activity, IConstant.IS_CLOSE_INTERACT + loginBean.user_id, true)) {
                    //默认打开打招呼的界面
                    initInteractView(0)
                }
        }
    }

    override fun onDestroy() {
        isLoopInteract = false
        progressHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        try {
            audioPlayer.stop()
        } catch (e: Exception) {
        }
    }
}