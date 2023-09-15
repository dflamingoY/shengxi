package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_season_album_details.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogTalkAlbumMenu
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddTalkAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.frag.SeasonAlbumDetailsActivity.Companion.MODE_LOOP
import org.xiaoxingqi.shengxi.modules.user.frag.SeasonAlbumDetailsActivity.Companion.MODE_RANDOM
import org.xiaoxingqi.shengxi.modules.user.frag.SeasonAlbumDetailsActivity.Companion.MODE_SINGLE
import org.xiaoxingqi.shengxi.modules.user.frag.talkAlbum.EditTalkAlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import skin.support.SkinCompatManager
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TalkAlbumDetailsActivity : BaseNormalActivity() {
    private lateinit var data: VoiceAlbumData.AlbumDataBean
    private val mData by lazy { ArrayList<BaseTalkVoiceBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var isDrag = false
    private var current = 0
    private var playMode = 1 //1列表循环 2随机 3单曲循环
    private val random by lazy { Random() }
    private var isContinua = true
    private var playBean: BaseTalkVoiceBean? = null
    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {
        override fun handleMessage(msg: Message?) {
            if (!isDrag) {
                seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
            }
            tv_ProgressTime.text = AppTools.parseTime2Str(audioPlayer.currentPosition)
        }
    }
    private var dialogMenu: DialogTalkAlbumMenu? = null
    private var lastId: String = ""
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
        marqueeText.visibility = View.GONE
        relative_empty_hint.visibility = View.GONE
        relativeEcho.visibility = View.GONE
        lineaer_Recommend.visibility = View.GONE
        linear_msg.visibility = View.GONE
        linear_add_album.visibility = View.GONE
        linearTalkAlbum.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this, 47).toFloat()).setDuration(0).start()
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        data = intent.getSerializableExtra("data") as VoiceAlbumData.AlbumDataBean
        Glide.with(this).load(data.album_cover_url)
                .into(ivMachineBg)
        tv_Title.text = resources.getString(R.string.string_9)
        tv_album_title.text = data.album_name
        tv_album_info.text = "包含${data.dialog_num}条对话"
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        iv_setting.setOnClickListener {
            startActivity<EditTalkAlbumActivity>("data" to data)
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
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                if (audioPlayer.duration > 0) {
                    playBean?.allDuration = audioPlayer.duration
                }
                playBean?.isPlaying = true
                dialogMenu?.let {
                    if (it.isShowing) {
                        it.updateCurrent(current)
                    }
                }
                iv_Play.setImageResource(R.mipmap.icon_play_playing)
                playBean?.let {
                    audioPlayer.seekTo((seekBar.progress / 1000f * it.resource_len * 1000).toInt())
                }
                handler.start()
            }

            @SuppressLint("SetTextI18n")
            override fun onCompletion() {
                playBean?.isPlaying = false
                seekBar.progress = 0
                if (!isContinua)
                    iv_Play.setImageResource(R.mipmap.icon_play_pause)
                handler.stop()
                tv_ProgressTime.text = "00:00"
                playNext()
            }

            override fun onInterrupt() {
                playBean?.isPlaying = false
                if (!isContinua)
                    iv_Play.setImageResource(R.mipmap.icon_play_pause)
                handler.stop()
            }
        }
        iv_VoiceList.setOnClickListener {
            if (dialogMenu == null) {
                dialogMenu = DialogTalkAlbumMenu(this).setOnItemClickListener(object : ITalkAlbumOperatorListener {
                    override fun clickItem(position: Int) {
                        /**
                         * 播放指定的角標
                         */
                        current = position
                        audioPlayer.stop()
                        seekBar.progress = 0
                        download(mData[position])
                    }

                    override fun operatedItem(bean: BaseTalkVoiceBean) {
                        /**
                         * 移除当前item
                         */
                        removeItem(bean)
                    }
                })
                dialogMenu!!.setData(mData)
            }
            dialogMenu!!.updateCurrent(current)
            dialogMenu!!.show()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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
                            tv_ProgressTime.text = AppTools.parseTime2Str((seekBar!!.progress / 1000f * mData[current].resource_len).toLong() * 1000)
                        }
                    } catch (e: Exception) {
                        tv_ProgressTime.text = "00:00"
                    }
                }
            }
        })
        view_resource_type.setOnClickListener {
            anim()
        }
        ivMachineBg.setOnClickListener {
            anim()
        }
        linearTalkAlbum.setOnClickListener {
            //添加到另外一个专辑
            startActivity<DialogAddTalkAlbumActivity>(
                    "talkId" to "${playBean?.dialog_id}",
                    "originalAlbumId" to "${playBean?.id}",
                    "albumId" to data.id
            )
        }
    }

    /**
     * 移除当前列表
     */
    private fun removeItem(bean: BaseTalkVoiceBean) {
        OkClientHelper.delete(this, "albumDialogs/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (audioPlayer.isPlaying) {
                        if (mData[current] == playBean) {
                            audioPlayer.stop()
                        }
                    }
                    mData.remove(bean)
                    dialogMenu?.notifyDataSetChange()
                    data.dialog_num -= 1
                    tv_album_info.text = "包含${data.dialog_num}条对话"
                    if (mData.size == 0) {//展示空界面
                        iv_Play.setImageResource(R.mipmap.icon_play_pause)
                        seekBar.max = 0
                        tv_ProgressTime.text = "00:00"
                        tv_Time.text = "00:00"
                        ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this@TalkAlbumDetailsActivity, 47).toFloat()).setDuration(0).start()
                        relative_operator_menu.isSelected = false
                        relative_show_copyWriting.visibility = View.VISIBLE
                        linearVoiceInfo.visibility = View.GONE
                    } else {
                        if (current >= mData.size) {
                            current = 0
                        }
                        playNext()
                    }
                } else {
                    showToast(result.msg)
                }
            }
        }, "V4.3")
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

    /**
     * 下载需要播放的文件, 展示信息
     */
    private fun download(item: BaseTalkVoiceBean) {
        if (relative_show_copyWriting.visibility != View.GONE) {
            relative_show_copyWriting.visibility = View.GONE
        }
        tv_VoiceTime.text = TimeUtils.getInstance().paserTimeMachine(this, item.dialog_at)
        try {
            if (!relative_operator_menu.isSelected) {
                val animator = ObjectAnimator.ofFloat(relative_operator_menu, "translationY", 0f).setDuration(220)
                relative_operator_menu.isSelected = true
                animator.start()
            }
            tv_Time.text = AppTools.parseTime2Str((item.resource_len * 1000L))
            if (TextUtils.isEmpty(item.resource_url)) {
                showToast("声兮路径出错")
                return
            }
            playBean = item
            val file = getDownFilePath(item.resource_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                if (currentMode == MODE_EARPIECE)
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                else
                    audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                loadingStatus.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
                OkClientHelper.downFile(this, item.resource_url, { o ->
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
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "dialogAlbums/${data.id}/dialog?lastDialogId=$lastId", TalkAlbumData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as TalkAlbumData
                result.data?.let {
                    mData.addAll(it)
                    if (it.size >= 10) {
                        lastId = it[it.size - 1].dialog_id.toString()
                        request(0)
                    }
                    seekBar.max = 1000
                }
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun albumEvent(event: IUpdateTalkAlbumEvent) {
        if (event.type == 1) {
            finish()
        } else if (event.type == 2) {
            if (!TextUtils.isEmpty(event.name)) {
                data.album_name = event.name
                tv_album_title.text = data.album_name
            }
            if (!TextUtils.isEmpty(event.cover)) {
                data.album_cover_url = event.cover
                Glide.with(this).load(data.album_cover_url)
                        .into(ivMachineBg)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changeAlbumEvent(event: OnTalkAddAlbumEvent) {
        mData.loop {
            it.id.toString() == event.dialogId
        }?.let {
            if (it.isPlaying) {
                audioPlayer.stop()
            }
            mData.remove(it)
            dialogMenu?.notifyDataSetChange()
            data.dialog_num -= 1
            tv_album_info.text = "包含${data.dialog_num}条对话"
            if (mData.size == 0) {
                iv_Play.setImageResource(R.mipmap.icon_play_pause)
                seekBar.max = 0
                tv_ProgressTime.text = "00:00"
                tv_Time.text = "00:00"
                ObjectAnimator.ofFloat(relative_operator_menu, "translationY", AppTools.dp2px(this@TalkAlbumDetailsActivity, 47).toFloat()).setDuration(0).start()
                relative_operator_menu.isSelected = false
                relative_show_copyWriting.visibility = View.VISIBLE
                linearVoiceInfo.visibility = View.GONE
            } else {
                if (current >= mData.size) {
                    current = 0
                }
                playNext()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }

}