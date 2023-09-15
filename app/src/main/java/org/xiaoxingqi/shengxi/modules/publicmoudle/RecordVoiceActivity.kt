package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.nineoldandroids.animation.*
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_record_voice.*
import kotlinx.android.synthetic.main.view_progress_speed.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelEchoes
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.SendVoiceData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.login.ArgumentActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.util.*

private const val IDEL = 0//空闲
private const val RECORDING = 1//录制中
private const val RECORDED = 2//录制完成 空闲状态
private const val PLAYING = 3//播放中
private const val REQUESTRECORD = 0x04

/**
 * 1 请求录制权限
 * 2 文件访问权限
 */
class RecordVoiceActivity : BaseNormalActivity(), IAudioRecordCallback {

    private val iconIeda = intArrayOf(R.mipmap.icon_record_idea_8, R.mipmap.icon_record_idea_1, R.mipmap.icon_record_idea_2, R.mipmap.icon_record_idea_3, R.mipmap.icon_record_idea_4,
            R.mipmap.icon_record_idea_5, R.mipmap.icon_record_idea_6, R.mipmap.icon_record_idea_7)
    private val iconRecord = intArrayOf(R.mipmap.icon_record_recording_8, R.mipmap.icon_record_recording_1, R.mipmap.icon_record_recording_2, R.mipmap.icon_record_recording_3, R.mipmap.icon_record_recording_4
            , R.mipmap.icon_record_recording_5, R.mipmap.icon_record_recording_6, R.mipmap.icon_record_recording_7)
    private val colorArray = intArrayOf(R.color.color_record_1, R.color.color_record_2, R.color.color_record_3, R.color.color_record_4,
            R.color.color_record_5, R.color.color_record_6, R.color.color_record_7, R.color.color_record_8)
    var recordState = IDEL
    private var audioMessageHelper: AudioRecorder? = null
    var filePath: String? = null
    private var time: Long = 0
    private var voiceLength: Int = 0
    private var recordType = 3 //  默认回声  1 发声兮  2 更改声波  3 回声   4 发声兮唱回忆    5 录制预设回复   6 录制铃声台词配音 7录制cheers
    private var isSend = true// 是否发送
    private var resourceType = "4"//资源上传类型
    private var randomIndex = Random().nextInt(7)
    private var photoList: ArrayList<String>? = null
    private var current = 0
    private lateinit var audioPlayer: AudioPlayer
    private var pausePosition = 0L
    private var idleResource = R.mipmap.btn_iedl
    private var recordLength = 121
    private var isMusic = false
    private var sendPath: String? = null
    val mHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper() {
        override fun handleMessage(msg: Message) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                var progress = (voiceLength * 1000 - currentPosition) / 1000
                if (isMusic) {
                    if (progress > 300) {
                        progress = 300
                    }
                } else {
                    if (progress > 120) {
                        progress = 120
                    }
                }
                if (progress < 0) {
                    progress = 0
                }
                tv_Time.text = "${progress}S"
            } else {
                var dtime = ((System.currentTimeMillis() - time) / 1000).toInt()
                if (!isMusic) {
                    if (dtime > 120) {
                        dtime = 120
                    }
                } else {
                    if (dtime > 300) {
                        dtime = 300
                    }
                }
                tv_Time.text = "${dtime}S"
                tv_RecordTime.text = "${dtime}S"
            }
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
                pausePosition = currentPosition
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                pausePosition = currentPosition
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_record_voice
    }

    override fun initView() {
        // 禁止自动锁屏
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val isOpenRecord = intent.getBooleanExtra("isOpenRecord", false)
        if (isOpenRecord) {
            //打开相册
            loadAlbum()
        }
        linearRecord.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                linearRecord.viewTreeObserver.removeOnGlobalLayoutListener(this)
                ObjectAnimator.ofFloat(linearRecord, "translationY", AppTools.dp2px(this@RecordVoiceActivity, 288).toFloat(), 0F).setDuration(320).start()
                val animator = ObjectAnimator.ofFloat(viewColse, "alpha", 0f, 1f).setDuration(320)
                animator.start()
                animator.addListener(object : AnimatorListenerAdapter() {
                    @SuppressLint("SetTextI18n")
                    override fun onAnimationEnd(animation: Animator?) {

                        /**
                         * 判断是否有缓存数据
                         */
                        val original = intent.getStringExtra("original")
                        original?.let {
                            //表示有数据
                            voiceLength = intent.getStringExtra("voicelength").toInt()
                            tv_Time.text = "${voiceLength}S"
                            tv_Hint.text = "点击播放"
                            filePath = it
                            openAnim()
                            iv_Record.setImageResource(R.mipmap.btn_pause)
                            recordState = RECORDED
                            tv_Gallery.visibility = View.GONE
                        }
                    }
                })
            }
        })
        iv_Record.isSelected = true
        iv_Record_Type.setImageResource(iconIeda[randomIndex])
        tv_AdvanceModel.setTextColor(resources.getColor(colorArray[randomIndex]))
        tvRecordMaxLength.text = resources.getString(R.string.string_recordAct_2) + "120秒"
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        recordType = intent.getIntExtra("recordType", 3)
        isSend = intent.getBooleanExtra("isSend", true)
        isMusic = intent.getBooleanExtra("isMusic", false)
        var tagName = intent.getStringExtra("tagId")
        if (recordType == 1) {
            if (isMusic) {
                recordLength = 301
                idleResource = R.mipmap.icon_record_music
                iv_Record.setImageResource(idleResource)
                tvRecordMaxLength.text = resources.getString(R.string.string_recordAct_2) + "300秒"
            }
        }
        intent.getStringExtra("resourceType")?.let {
            resourceType = it
        }
        sendObserver()
        iv_Close.visibility = if (intent.getBooleanExtra("isClose", false)) View.VISIBLE else View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTPERMSSIONCODE)
            } else {
                mkdris()
            }
        } else {
            mkdris()
        }
        audioMessageHelper = AudioRecorder(this, RecordType.AAC, recordLength, this)
        waveView.setColor(resources.getColor(R.color.colorTextGray))
        waveView.setSpeed(1200)
        waveView.setInitialRadius(AppTools.dp2px(this, 40).toFloat())
        waveView.setMaxRadiusRate(1f)
        when (recordType) {
            1 -> {
                tv_Gallery.visibility = View.VISIBLE
            }
            2 -> {//修改声波
                tv_OpenGallery.visibility = View.GONE
                iv_Close.visibility = View.VISIBLE
                tv_Gallery.visibility = View.GONE
                tv_record_wave.visibility = View.VISIBLE
            }
            3 -> {//回声
                relativeEcho.visibility = View.VISIBLE
                val avatar = intent.getStringExtra("avatar")
                if (!TextUtils.isEmpty(avatar))
                    glideUtil.loadGlide(avatar, ivUser, 0, glideUtil.getLastModified(avatar))
                val hobby = intent.getStringExtra("hobby")
                relativeRule.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(hobby)) {
                    tv_hobby.visibility = View.VISIBLE
                    tv_hobby.text = "ta喜欢:$hobby"
                }
                tv_OpenGallery.visibility = View.GONE
                linear_Echoes.visibility = View.VISIBLE
                if (intent.getBooleanExtra("isSelf", false)) {
                    tv_comment_type.text = resources.getString(R.string.string_comment_isSef_visiable)
                    relativeEcho.visibility = View.GONE
                } else {
                    tv_comment_type.visibility = View.GONE
                    ivAlert.visibility = View.GONE
                }
                tv_Gallery.visibility = View.GONE
                val isBusy = intent.getIntExtra("isBusy", 0)
                ivUserStatus.isSelected = isBusy == 1
                sendPath = intent.getStringExtra("sendPath")
            }
            5 -> {
                tv_OpenGallery.visibility = View.GONE
                iv_Close.visibility = View.GONE
                tv_Gallery.visibility = View.GONE
                tv_record_wave.visibility = View.VISIBLE
                tv_record_wave.text = "录制预设回复语音"
            }
            6 -> {
                tv_Gallery.visibility = View.GONE
                tv_OpenGallery.visibility = View.GONE
                tvWordingAnonymous.visibility = View.VISIBLE
                tvWordingType.visibility = View.VISIBLE
                tv_Send.text = resources.getString(R.string.string_sendAct_8)
                tvWording.visibility = View.VISIBLE
                if (TextUtils.isEmpty(tagName)) {
                    tagName = "求配音"
                }
                tvWording.text = "#$tagName#"
                intent.getStringExtra("wording")?.let {
                    tvWordingType.text = it
                }
            }
            7 -> {
                tv_OpenGallery.visibility = View.GONE
                tvCheersHint.visibility = View.VISIBLE
            }
        }
        if (intent.getBooleanExtra("isComment", false)) {
            tv_Gallery.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(filePath)) {
            tv_Gallery.visibility = View.GONE
        }
        themeWave.setColor(resources.getColor(colorArray[randomIndex]))
        themeWave.setSpeed(1200)
        themeWave.setInitialRadius(AppTools.dp2px(this, 50).toFloat())
        themeWave.setMaxRadiusRate(1f)
        audioPlayer = AudioPlayer(this)
    }

    private fun mkdris() {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    private var clickTime = 0L

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        audioMessageHelper?.completeRecord(false)
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        relativeRule.setOnClickListener {
            startActivity(Intent(this, ActionActivity::class.java)
                    .putExtra("title", "声昔宿舍的“不不不”")
                    .putExtra("url", "32")
                    .putExtra("isHtml", true)
                    .putExtra("isVersion", false)
            )
        }
        iv_Close.setOnClickListener {
            if (recordType == 2) {
                if (recordState == RECORDING) {
                    mHandler.stop()
                    mHandler.removeCallbacks(mHandler)
                    audioMessageHelper?.completeRecord(true)
                } else {
                    finish()
                }
            } else
                DialogCancelEchoes(this).setOnCLickListener(View.OnClickListener {
                    if (recordState == RECORDING) {
                        mHandler.stop()
                        mHandler.removeCallbacks(mHandler)
                        audioMessageHelper?.completeRecord(true)
                    } else
                        finish()
                }).show()
        }
        iv_Record.setOnClickListener {
            if (!iv_Record.isSelected) {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime - clickTime < 500) {
                return@setOnClickListener
            }
            clickTime = System.currentTimeMillis()
            when (recordState) {
                IDEL -> {//空闲  点击请求权限,开始录制
                    checkPermission()
                }
                RECORDING -> {//停止录制
                    audioMessageHelper?.completeRecord(false)
                }
                RECORDED -> {//播放
                    play()
                }
                PLAYING -> {//播放状态时点击为暂停
                    pausePosition = audioPlayer.currentPosition
                    mHandler.stop()
                    audioPlayer.stop()
                    tv_Hint.text = resources.getString(R.string.string_recordAct_5)
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.btn_pause)
                    recordState = RECORDED
//                    tv_Time.text = "${voiceLength - Math.ceil(pausePosition / 1000.0).toInt()}S"
                }
//                else -> {
//
//                }
            }
        }
        tv_ReRecord.setOnClickListener {
            /*回复初始化状态 */
            if (linearOperate.alpha < 1) {
                return@setOnClickListener
            }
            if (recordType == 2) {
                iv_Close.visibility = View.VISIBLE
            } else
                iv_Close.visibility = View.GONE
            if (tv_Gallery.visibility != View.VISIBLE) {
                if (recordType == 1) {
                    tv_Gallery.visibility = View.VISIBLE
                }
            }
            pausePosition = 0
            audioPlayer.stop()
            //删除文件
            try {
                File(filePath).let {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            } catch (e: Exception) {
            }
            filePath = null
            mHandler.stop()
            restoreAnim()
            iv_Record.setImageResource(idleResource)
            recordState = IDEL
            voiceLength = 0
            tv_Hint.text = resources.getString(R.string.string_recordAct_1)
            tv_Time.text = "0S"
            waveView.stopImmediately()
        }
        tv_RePlay.setOnClickListener {
            if (linearOperate.alpha < 1) {
                return@setOnClickListener
            }
            pausePosition = 0
            tv_Time.text = "${voiceLength}S"
            play()
        }
        tv_Send.setOnClickListener {
            if (linearOperate.alpha < 1) {
                return@setOnClickListener
            }
            if (!TextUtils.isEmpty(intent.getStringExtra("original"))) {
                if (intent.getStringExtra("original") == filePath) {
                    finish()
                    return@setOnClickListener
                }
            }
            /**
             * 上传文件
             */
            if (isSend) {
                LocalLogUtils.writeLog("录制界面=>上传音频 : " + if (recordType == 3) "回声" else if (recordType == 5) "录制预设回复" else if (recordType == 6) "录制配音" else "声波", System.currentTimeMillis())
                startTime = System.currentTimeMillis()
                allLength = File(filePath).length()
                request(0)
            } else {
                val intent = Intent()
                intent.putExtra("voiceLength", calcMediaLength(filePath, voiceLength).toString())
                        .putExtra("original", filePath)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        viewColse.setOnClickListener {
            if (recordState == IDEL) {
                finish()
            } else {
                return@setOnClickListener
            }/* else {
                if (recordType == 3) {
                    if (!TextUtils.isEmpty(filePath)) {
                        DialogCancelEchoes(this).setOnCLickListener(View.OnClickListener {
                            finish()
                        }).show()
                    }
                }
            }*/
        }
        iv_Finish.setOnClickListener {
            /* audioMessageHelper?.let {
                 if (it.isRecording) {
                     it.completeRecord(true)
                 }
             }*/
            setResult(999)
            super.finish()
        }
        tv_AdvanceModel.setOnClickListener {
            themeWave.stopImmediately()
            val local = IntArray(2)
            relativeRecord.getLocationOnScreen(local)//终点坐标
            val templocal = IntArray(2)
            iv_Record_Type.getLocationOnScreen(templocal)//起始位置
            /**
             * 切换显示界面
             */
            val valueAnimator = ValueAnimator.ofObject(ParabolaTypeCopy(), PointF(templocal[0].toFloat(), templocal[1].toFloat()),
                    PointF(local[0].toFloat(), local[1].toFloat()))
            valueAnimator.duration = 520
            valueAnimator.addUpdateListener {
                val value = it.animatedValue as PointF
                iv_Record_Type.x = value.x
                iv_Record_Type.y = value.y
            }
            valueAnimator.start()
            ObjectAnimator.ofFloat(iv_Record_Type, "alpha", 1f, 0f).setDuration(520).start()
            val reveal = ViewAnimationUtils.createCircularReveal(relativeSimpleModel, local[0] + AppTools.dp2px(this, 58),
                    local[1] + AppTools.dp2px(this, 58),
                    relativeSimpleModel.height.toFloat(),
                    AppTools.dp2px(this, 48).toFloat())
            reveal.duration = 620
            reveal.start()
            reveal.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator?) {
                    super.onAnimationEnd(animation)
                    relativeSimpleModel.visibility = View.GONE
                }
            })
        }
        /* iv_Record_Type.setOnClickListener {
             if (!iv_Record.isSelected) {
                 Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
             }
             val currentTime = System.currentTimeMillis()
             if (currentTime - clickTime < 500) {
                 return@setOnClickListener
             }
             clickTime = System.currentTimeMillis()
             when (recordState) {
                 IDEL -> {//空闲  点击请求权限,开始录制
                     checkPermission()
                     themeWave.start()
                     tv_Record.visibility = View.GONE
                 }
                 RECORDING -> {//停止录制
                     //直接发送
 //                    changeModelAnim()
                     themeWave.stopImmediately()
                     audioMessageHelper?.completeRecord(false)
                 }
             }
         }*/
        tv_OpenGallery.setOnClickListener {
            /**
             * 打开相册模式
             * 加载本地图片
             */
            loadAlbum()
        }
        tv_Next.setOnClickListener {
            tv_Pre.visibility = View.VISIBLE
            if (current < photoList!!.size - 1) {
                current++
                ImageLoader.getInstance().displayImage("file://${photoList!![current]}", iv_Gallery, AppTools.options)
            }
            if (current == photoList!!.size - 1) {
                tv_Next.alpha = 0.5f
                tv_Next.isEnabled = false
            }
        }
        tv_Pre.setOnClickListener {
            tv_Next.alpha = 1f
            tv_Next.isEnabled = true
            if (current <= 1) {
                tv_Pre.visibility = View.GONE
            }
            if (current > 0) {
                current--
                ImageLoader.getInstance().displayImage("file://${photoList!![current]}", iv_Gallery, AppTools.options)
            }
        }
        iv_CloseGallery.setOnClickListener {
            current = 0
            relativeGallery.visibility = View.GONE
            tv_OpenGallery.visibility = View.VISIBLE
        }
        tvWordingAnonymous.setOnClickListener {
            tvWordingAnonymous.text = if (it.isSelected) {
                "匿名OFF"
            } else {
                "匿名ON"
            }
            it.isSelected = !it.isSelected
        }
    }

    /**
     * 计算音频的实际长度
     */
    private fun calcMediaLength(path: String?, voiceLength: Int = 0): Int {
        return voiceLength /*try {
            val media = MediaPlayer()
            media.setDataSource(path)
            media.prepare()
            var duration = (media.duration / 1000f + 0.5f).toInt()
            media.release()
            if (isMusic) {
                if (duration > 300)
                    300
                else
                    duration
            } else {
                if (duration > 120)
                    120
                else
                    duration
            }
        } catch (e: Exception) {
            voiceLength
        }*/
    }

    private fun changeModelAnim() {
        themeWave.stopImmediately()
        val local = IntArray(2)
        relativeRecord.getLocationOnScreen(local)//终点坐标
        val templocal = IntArray(2)
        iv_Record_Type.getLocationOnScreen(templocal)//起始位置
        /**
         * 切换显示界面
         */
        val valueAnimator = ValueAnimator.ofObject(ParabolaTypeCopy(), PointF(templocal[0].toFloat(), templocal[1].toFloat()),
                PointF(local[0] - AppTools.dp2px(this, 65).toFloat(), local[1].toFloat() - AppTools.dp2px(this, 15)))
        valueAnimator.duration = 520
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as PointF
            iv_Record_Type.x = value.x
            iv_Record_Type.y = value.y
        }
        valueAnimator.start()
        ObjectAnimator.ofFloat(iv_Record_Type, "alpha", 1f, 0f).setDuration(520).start()
        val reveal = ViewAnimationUtils.createCircularReveal(relativeSimpleModel, local[0] - AppTools.dp2px(this, 7),
                local[1] + AppTools.dp2px(this, 43),
                relativeSimpleModel.height.toFloat(),
                AppTools.dp2px(this, 48).toFloat())
        reveal.duration = 620
        reveal.start()
        reveal.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator?) {
                super.onAnimationEnd(animation)
                relativeSimpleModel.visibility = View.GONE
            }
        })
    }

    @SuppressLint("StaticFieldLeak")
    private fun loadAlbum() {
        if (photoList != null && photoList?.size!! > 0) {
            current = 0
            relativeGallery.visibility = View.VISIBLE
//            tv_Gallery.visibility = View.VISIBLE
            tv_OpenGallery.visibility = View.GONE
            ImageLoader.getInstance().displayImage("file://${photoList!![0]}", iv_Gallery, AppTools.options)
        } else {
            transLayout.showProgress()
            object : AsyncTask<Void, Void, ArrayList<String>>() {
                @SuppressLint("Recycle")
                override fun doInBackground(vararg params: Void?): ArrayList<String> {
                    val data = arrayListOf<String>()
                    val imageUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val resolver = this@RecordVoiceActivity.contentResolver
                    val cursor = resolver.query(imageUrl, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                            arrayOf("image/jpeg", "image/png", "image/gif"), MediaStore.Images.Media.DATE_MODIFIED)
                    if (null != cursor && cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Images.Media.DATA))
                            if (File(path).length() > 5000)//文件必须大于5kb
                                data.add(0, path)
                        }
                    }
                    cursor!!.close()
                    return data
                }

                override fun onPostExecute(result: ArrayList<String>?) {
                    super.onPostExecute(result)
                    transLayout.showContent()
                    /**
                     * 展示图片
                     */
                    if (result != null && result.size > 0) {
                        photoList = result
                        relativeGallery.visibility = View.VISIBLE
//                        tv_Gallery.visibility = View.VISIBLE
                        tv_OpenGallery.visibility = View.GONE
                        ImageLoader.getInstance().displayImage("file://${result[0]}", iv_Gallery, AppTools.options)
                        current = 0
                        if (result.size == 1) {
                            tv_Next.visibility = View.GONE
                            tv_Pre.visibility = View.GONE
                        }
                    } else {
                        showToast("相册中没有发现图片")
                    }
                }
            }.execute()
        }
    }

    private var allLength = 0L
    private var startTime = 0L
    private var aliLoad: AliLoadFactory? = null

    override fun request(flag: Int) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", resourceType)
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(filePath))}.aac")
                .add("needBaseUri", "1")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                val data = result as QiniuStringData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(result.data.bucket_id)) {
                        AppTools.bucketId = result.data.bucket_id
                    }
                    LocalLogUtils.writeLog("录制界面=>上传音频,Token获取成功 " + if (recordType == 3) "回声" else if (recordType == 5) "录制预设回复" else "声波", System.currentTimeMillis())
                    aliLoad = AliLoadFactory(this@RecordVoiceActivity, result.data.end_point, data.data.bucket, data.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {
                            if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                LocalLogUtils.writeLog("录制界面:上传音频:more than 30s   cancel all task", System.currentTimeMillis())
                                aliLoad?.cancel()
                            } else if (System.currentTimeMillis() - startTime >= 10000) {
                                if (tv_progress.visibility != View.VISIBLE) {
                                    tv_progress.post {
                                        tv_progress.visibility = View.VISIBLE
                                    }
                                }
                                tv_progress.post {
                                    tv_progress.text = "${((current * 1f / allLength) * 100).toInt()}%"
                                }
                            }
                        }

                        override fun success() {
                            if (recordType == 3 && !TextUtils.isEmpty(sendPath)) {
                                mHandler.postDelayed({
                                    EventBus.getDefault().post(SendMsgEvent(String.format(sendPath!!, data.data.resource_content, calcMediaLength(filePath, voiceLength).toString(), AppTools.bucketId)))
                                }, 200)
                                setResult(Activity.RESULT_CANCELED)
                            } else if (recordType == 6) {
                                /*
                                 发布闹钟配音
                                 */
                                sendDubbing(data.data.resource_content)
                            } else {
                                val intent = Intent()
                                intent.putExtra("voice", data.data.resource_content)
                                        .putExtra("voiceLength", calcMediaLength(filePath, voiceLength).toString())
                                        .putExtra("baseUri", result.data.baseUri)
                                        .putExtra("original", filePath)
                                setResult(RESULT_OK, intent)
                            }
                            //删除本地默认缓存文件
                            try {
                                File(filePath)?.let {
                                    if (it.exists()) {
                                        it.delete()
                                    }
                                }
                            } catch (e: java.lang.Exception) {

                            }
                            runOnUiThread {
                                finish()
                            }
                        }

                        override fun fail() {
                            runOnUiThread {
                                tv_progress.visibility = View.GONE
                                LocalLogUtils.writeLog("录制界面=>上传音频出错 :  ${if (AppTools.isNetOk(this@RecordVoiceActivity)) "网络正常" else "网络异常"}" + if (recordType == 3) "回声" else if (recordType == 5) "录制预设回复" else "声波", System.currentTimeMillis())
                                showToast(if (recordType == 2) "上传失败,当前网络不太稳定" else "发送失败,当前网络不太稳定")
                                transLayout.showContent()
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(data.data.resource_content, filePath))
                } else {
                    LocalLogUtils.writeLog("录制界面=>code=${result.code} : ${result.msg} ${if (AppTools.isNetOk(this@RecordVoiceActivity)) "网络正常" else "网络异常"}" + if (recordType == 3) "回声" else if (recordType == 5) "录制预设回复" else "声波", System.currentTimeMillis())
                }
            }

            override fun onFailure(any: Any?) {
                showToast(if (recordType == 2) "上传失败,当前网络不太稳定" else "发送失败,当前网络不太稳定")
                LocalLogUtils.writeLog("录制界面=>Token获取错误 : ${any.toString()} ${if (AppTools.isNetOk(this@RecordVoiceActivity)) "网络正常" else "网络异常"}" + if (recordType == 3) "回声" else if (recordType == 5) "录制预设回复" else "声波", System.currentTimeMillis())
                transLayout.showContent()
            }
        })
    }

    private fun sendDubbing(uri: String) {
        val formBody = FormBody.Builder().add("lineId", intent.getStringExtra("wordingId"))
                .add("bucketId", AppTools.bucketId)
                .add("dubbingUri", uri)
                .add("dubbingLen", voiceLength.toString())
                .add("isAnonymous", if (tvWordingAnonymous.isSelected) "1" else "0").build()
        OkClientHelper.post(this, "dubbings", formBody, IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    EventBus.getDefault().post(AlarmUpdateEvent(1, 1).apply { dubbingId = intent.getStringExtra("wordingId") })
                    finish()
                } else {
                    LocalLogUtils.writeLog("录制界面=> 配音发布${result.msg}", System.currentTimeMillis())
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.1")
    }

    private var voicePath: String? = null

    /**
     * zhijie 发送音频
     */
    private fun sendVoice(flag: Int) {
        when (flag) {
            1 -> {
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                        .add("resourceType", "2")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(filePath))}.aac")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        val data = result as QiniuStringData
                        if (result.code == 0) {
                            AliLoadFactory(this@RecordVoiceActivity, result.data.end_point, data.data.bucket, data.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    runOnUiThread {
                                        voicePath = result.data.resource_content
                                        sendVoice(2)
                                    }
                                }

                                override fun fail() {
                                    runOnUiThread {
                                        showToast("上传失败，请稍后重试")
                                        transLayout.showContent()
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {

                                }
                            }, UploadData(data.data.resource_content, filePath))
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (any is Exception) {
                            if (any is ConnectException) {
                                showToast("网络连接异常")
                            }
                        }
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                val buider = FormBody.Builder()
                        .add("voiceType", "2")
                        .add("voiceUri", voicePath)
                        .add("voiceLen", voiceLength.toString())
                OkClientHelper.post(this, "voices", buider.build(), SendVoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 发送通知 刷新界面
                         */
                        transLayout.showContent()
                        result as SendVoiceData
                        if (result.code == 0) {
                            EventBus.getDefault().post(UpdateSendVoice(1, if (null == result.data) null else result.data.voice_id))
                            EventBus.getDefault().post(OperatorVoiceListEvent(1))
                            if (null == result.data || TextUtils.isEmpty(result.data.voice_id)) {
                                showToast(resources.getString(R.string.string_sendAct_11))
                            }
                            setResult(999)
                            finish()
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@RecordVoiceActivity)) {
                            showToast("网络连接异常")
                        } else {
                            showToast(any.toString())
                        }
                    }
                })

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun play() {
        if (linearOperate.alpha < 1) {
            return
        }
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        mHandler.stop()
        audioPlayer.stop()
        recordState = PLAYING
        try {
            audioPlayer.setDataSource(filePath)
            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)

            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
                override fun onCompletion() {
                    pausePosition = 0
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.btn_pause)
                    recordState = RECORDED
                    mHandler.stop()
                    tv_Time.text = "${voiceLength}S"
                }

                override fun onInterrupt() {//异常停止
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.btn_pause)
                    recordState = RECORDED
                    mHandler.stop()
//                    tv_Time.text = "${voiceLength}S"
                }

                override fun onPrepared() {
                    audioPlayer.seekTo(pausePosition.toInt())
                    pausePosition = 0
                    tv_Hint.text = resources.getString(R.string.string_recordAct_6)
                    iv_Record.setImageResource(R.mipmap.btn_playing)
                    recordState = PLAYING
                    waveView.start()
                    mHandler.start()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUESTRECORD)
            } else {
                startRecording()
            }
        } else {
            startRecording()
        }
    }

    @Synchronized
    fun startRecording() {
        synchronized(RecordVoiceActivity::class.java) {
            if (recordState == RECORDING)
                return@synchronized
//            linear_Echoes.visibility = View.GONE
            recordState = RECORDING
            audioMessageHelper?.startRecord()
            tv_Hint.text = resources.getString(R.string.string_recordAct_7)
            mHandler.start()
            waveView.start()
            iv_Record.setImageResource(R.mipmap.btn_recording)
            iv_Record_Type.setImageResource(iconRecord[randomIndex])
            tv_RecordHint.visibility = View.VISIBLE
            tv_SimpleHint.text = "录制中"
            iv_Finish.visibility = View.GONE
            if (recordType == 6) {
                tvWording.visibility = View.GONE
                iv_Close.visibility = View.VISIBLE
            }
        }
    }

    override fun onRecordSuccess(p0: File?, p1: Long, p2: RecordType?) {

        /**
         * 录制完成
         *
         * 如果是高级模式 停止之后直接发送声兮
         */
        if (tv_Gallery.visibility == View.VISIBLE) {
            tv_Gallery.visibility = View.INVISIBLE
        }
        if (relativeSimpleModel.visibility == View.VISIBLE) {//如果是开屏模式 切换到高级模式
            changeModelAnim()
        }
        if (recordType == 3) {
            iv_Close.visibility = View.VISIBLE
        }
        waveView.stopImmediately()
        voiceLength = (p1.toFloat() / 1000 + 0.5f).toInt()
        if (!isMusic) {
            if (voiceLength > 120) {
                voiceLength = 120
            }
        } else {
            if (voiceLength > 300) {
                voiceLength = 300
            }
        }
        if (voiceLength < 1) {
            voiceLength = 1
        }
        mHandler.stop()
        tv_Time.text = "${voiceLength}S"
        tv_Hint.text = resources.getString(R.string.string_recordAct_5)
        filePath = p0?.absolutePath
        openAnim()
        iv_Record.setImageResource(R.mipmap.btn_pause)
        recordState = RECORDED
        if (relativeSimpleModel.visibility == View.VISIBLE) {
            sendVoice(1)
        }
    }

    override fun onRecordReachedMaxTime(p0: Int) {
        Toast.makeText(this, resources.getString(R.string.string_signRecordAct_6), Toast.LENGTH_SHORT).show()
        audioMessageHelper?.handleEndRecord(true, p0)
        if (relativeSimpleModel.visibility == View.VISIBLE) {//如果是开屏模式 切换到高级模式
            changeModelAnim()
        }
    }

    override fun onRecordReady() {
    }

    override fun onRecordCancel() {
        finish()
    }

    override fun onRecordStart(p0: File?, p1: RecordType?) {
        time = System.currentTimeMillis()
    }

    @SuppressLint("SetTextI18n")
    override fun onRecordFail() {
        /**
         * 需要初始化录制的界面
         */
        if (recordType == 2) {
            iv_Close.visibility = View.VISIBLE
        } else
            iv_Close.visibility = View.GONE
        audioPlayer.stop()
        filePath = null
        mHandler.stop()
//        restoreAnim()
        iv_Record.setImageResource(idleResource)
        recordState = IDEL
        voiceLength = 0
        tv_Hint.text = resources.getString(R.string.string_recordAct_1)
        tv_Time.text = "0S"
        waveView.stopImmediately()
        LocalLogUtils.writeLog("录制失败, 可能是权限未打开", System.currentTimeMillis())
        showToast("录制失败,请允许语音录制和文件读写权限")
    }

    fun openAnim() {
        val anim = AnimatorSet()
        anim.playTogether(ObjectAnimator.ofFloat(relativeRecord, "translationX", -AppTools.dp2px(this, 65).toFloat()).setDuration(520),
                ObjectAnimator.ofFloat(relative_Text, "translationY", -AppTools.dp2px(this, 15).toFloat()).setDuration(320),
                ObjectAnimator.ofFloat(linearOperate, "translationX", -AppTools.dp2px(this, 65).toFloat()).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "alpha", 0f, 1f).setDuration(320),
                ObjectAnimator.ofFloat(tv_Send, "alpha", 0f, 1f).setDuration(320),
                ObjectAnimator.ofFloat(relativeRecord, "translationY", -AppTools.dp2px(this, 15).toFloat()).setDuration(520))
        anim.start()
        tv_Send.visibility = View.VISIBLE
    }

    private fun restoreAnim() {
        tv_Send.visibility = View.GONE
        val anim = AnimatorSet()
        anim.playTogether(ObjectAnimator.ofFloat(relativeRecord, "translationX", -AppTools.dp2px(this, 65).toFloat(), 0f).setDuration(520),
                ObjectAnimator.ofFloat(relative_Text, "translationY", -AppTools.dp2px(this, 15).toFloat(), 0F).setDuration(320),
                ObjectAnimator.ofFloat(linearOperate, "translationX", -AppTools.dp2px(this, 65).toFloat(), 0f).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "alpha", 1f, 0f).setDuration(320),
                ObjectAnimator.ofFloat(tv_Send, "alpha", 1f, 0f).setDuration(320),
                ObjectAnimator.ofFloat(relativeRecord, "translationY", -AppTools.dp2px(this, 15).toFloat(), 0f).setDuration(520))
        anim.start()
    }

    private var isAnimationPlay = false

    override fun finish() {
        if (!isAnimationPlay) {
            if (TextUtils.isEmpty(filePath)) {
                setResult(100)
            }
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            audioMessageHelper?.let {
                if (it.isRecording) {
                    it.completeRecord(true)
                }
            }
            audioMessageHelper?.destroyAudioRecorder()
            audioMessageHelper = null
            mHandler.stop()
            mHandler.removeCallbacks(mHandler)
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            val animator = ObjectAnimator.ofFloat(linearRecord, "translationY", AppTools.dp2px(this@RecordVoiceActivity, 288).toFloat()).setDuration(320)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super@RecordVoiceActivity.finish()
                    overridePendingTransition(0, 0)
                }

                override fun onAnimationStart(animation: Animator?) {
                    isAnimationPlay = true
                }
            })
            animator.start()
            if (relativeGallery.visibility != View.GONE) {
                ObjectAnimator.ofFloat(relativeGallery, "alpha", 1f, 0f).setDuration(300).start()
            }
        }


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when (recordState) {
                IDEL -> {
                    if (relativeSimpleModel.visibility == View.VISIBLE) {
                        setResult(999)
                        super.finish()
                        return true
                    }
                    return super.onKeyDown(keyCode, event)
                }
                RECORDING -> return true
                else -> {
                    if (recordType == 3) {
                        if (!TextUtils.isEmpty(filePath)) {
                            DialogCancelEchoes(this).setOnCLickListener(View.OnClickListener {
                                finish()
                            }).show()
                            return true
                        }
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTRECORD) {
            if (grantResults.isNotEmpty())
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iv_Record.isSelected = true
                    startRecording()
                } else {
                    iv_Record.isSelected = false
                }
            else {
                iv_Record.isSelected = false
            }
        } else if (requestCode == REQUESTPERMSSIONCODE) {
            if (grantResults.isNotEmpty())
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iv_Record.isSelected = true
                    mkdris()
                } else {
                    iv_Record.isSelected = false
                }
            else {
                iv_Record.isSelected = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioMessageHelper?.destroyAudioRecorder()
    }
}