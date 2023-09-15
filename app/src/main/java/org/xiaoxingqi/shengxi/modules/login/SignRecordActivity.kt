package org.xiaoxingqi.shengxi.modules.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Environment
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_sign_record.*
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAudioPermission
import org.xiaoxingqi.shengxi.dialog.DialogEnterSystemSet
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.utils.*
import skin.support.observe.SkinObservable
import java.io.File
import java.io.IOException

class SignRecordActivity : BaseNormalActivity(), IAudioRecordCallback {
    private lateinit var audioMessageHelper: AudioRecorder
    private var time: Long = 0
    private var mVoicePath: String? = ""
    private var mLength: Int = 0
    private var resultVoicePath: String? = ""
    private var nickName: String? = null
    private var isCanBack = false
    private val IDEL = 1
    private val RECORDING = 2
    private val RECORDED = 3
    private val PLAYING = 4
    private var currentStatus = IDEL
    private lateinit var audioPlayer: AudioPlayer
    private var pausePosition = 0L
    private var dialog: DialogEnterSystemSet? = null
    private var dialogAudio: DialogAudioPermission? = null

    companion object {
        private const val REQUEST_PERMISSION_STORAGE = 0x00
        private const val REQUEST_PERMISSION_RECORD = 0x01
    }

    override fun updateSkin(observable: SkinObservable?, o: Any?) {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_record
    }

    val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper() {
        override fun handleMessage(msg: Message?) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                var progress = (mLength * 1000 - currentPosition) / 1000
                if (progress > 120) {
                    progress = 120
                }
                tv_Time.text = "${progress}S"
            } else {
                var dtime = ((System.currentTimeMillis() - time) / 1000).toInt()
                if (dtime > 120) {
                    dtime = 120
                }
                tv_Time.text = "${dtime}S"
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

    override fun initView() {
        iv_Record.isSelected = true
        tvRecordMaxLength.append("120秒")
        iv_rePlay.setImageResource(R.mipmap.icon_record_replay)
        iv_delete.setImageResource(R.mipmap.icon_record_delete)
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        LocalLogUtils.writeLog("SignRecordActivity: user force record wave", System.currentTimeMillis())
        /* val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
         val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
         if (IConstant.USER_EXTROVERT.equals(character, true)) {
             tv_character_signRecord1.text = resources.getString(R.string.string_sign_record_e_1)
             iv_sign_record3.setImageResource(R.mipmap.icon_sign_record_e_1)
             tv_Send.text = resources.getString(R.string.string_sign_record_start_e)
             tv_character_signRecord2.text = resources.getString(R.string.string_signRecordAct_2_e)
         }*/
        nickName = intent.getStringExtra("name")
        isCanBack = intent.getBooleanExtra("isCanBack", false)
//        btn_Back.visibility = if (isCanBack) View.VISIBLE else View.INVISIBLE
        audioMessageHelper = AudioRecorder(this, RecordType.AAC, 120, this)
        waveView.setColor(resources.getColor(R.color.colorIndecators))
        waveView.setSpeed(1200)
        waveView.setInitialRadius(AppTools.dp2px(this, 40).toFloat())
        waveView.setMaxRadiusRate(1f)
    }

    private fun mkdris() {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//如果没有文件权限, 立即
                dialog = DialogEnterSystemSet(this).setLoginPermission(false, isWriteStorage = true)
                dialog!!.show()
            } else {
                mkdris()
                if (checkPermissionState(Manifest.permission.RECORD_AUDIO)) {
                    if (SPUtils.getBoolean(this, IConstant.PERMISSION_DENIED_AUDIO, false)) {
                        dialog = DialogEnterSystemSet(this).setAudio(true)
                        dialog!!.show()
                    } else {
                        dialogAudio = DialogAudioPermission(this).setOnClickListener(View.OnClickListener {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_RECORD)
                        })
                        dialogAudio!!.show()
                    }
                } else {
                    iv_Record.isSelected = true
                    dialogAudio?.let {
                        if (it.isShowing)
                            it.dismiss()
                    }
                }
            }
        } else {
            mkdris()
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.let {
            if (it.isShowing)
                it.dismiss()
        }
        dialogAudio?.let {
            if (it.isShowing)
                it.dismiss()
        }
    }

    /**
     * @return true 表示需要申请权限, false otherWise
     */
    private fun checkPermissionState(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }

    override fun initEvent() {
        iv_Record.setOnClickListener {
            if (!iv_Record.isSelected) {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime - clickTime < 500) {
                return@setOnClickListener
            }
            when (currentStatus) {
                IDEL -> {//空閑點擊錄製
                    startRecord()
                }
                RECORDING -> {//点击录制完成
                    audioMessageHelper.completeRecord(false)
                }
                RECORDED -> {//点击播放
                    play()
                }
                PLAYING -> {//点击暂停
                    pausePosition = audioPlayer.currentPosition
                    handler.stop()
                    audioPlayer.stop()
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.icon_sign_record_3)
                    currentStatus = RECORDED
                }
            }
        }
        btn_Back.setOnClickListener {
            if (btn_Back.visibility != View.VISIBLE) {
                return@setOnClickListener
            }
            finish()
        }
        tv_ReRecord.setOnClickListener {
            if (linearOperate.alpha < 1) {
                return@setOnClickListener
            }
            pausePosition = 0
            audioPlayer.stop()
            mVoicePath = null
            handler.stop()
            restoreAnim()
            iv_Record.setImageResource(R.mipmap.icon_sign_record_1)
            mLength = 0
            tv_Time.text = "0S"
            currentStatus = IDEL
            waveView.stopImmediately()
            tv_start_record.visibility = View.VISIBLE
        }
        tv_Send.setOnClickListener {
            if (audioPlayer.isPlaying) {
                handler.stop()
                audioPlayer.stop()
            }
            if (it.alpha == 1.0f) {
//                request(0)
                startActivity<ThirdLoginBindActivity>("openId" to intent.getStringExtra("openId"),
                        "unionId" to intent.getStringExtra("unionId"),
                        "authType" to intent.getIntExtra("authType", 1),
                        "name" to intent.getStringExtra("name"),
                        "answers" to intent.getStringExtra("answers"),
                        "path" to mVoicePath,
                        "length" to mLength,
                        "third" to intent.getBooleanExtra("third", false),
                        "identity" to intent.getStringExtra("identity"),
                        "phone" to intent.getStringExtra("phone")
                )
                finish()
            }
        }
        tv_RePlay.setOnClickListener {
            if (linearOperate.alpha < 1) {
                return@setOnClickListener
            }
            pausePosition = 0
            tv_Time.text = "${if (mLength > 120) 120 else mLength}S"
            play()
        }
    }

    private var clickTime = 0L

    private fun play() {
        if (linearOperate.alpha < 1) {
            return
        }
        if (TextUtils.isEmpty(mVoicePath)) {
            return
        }
        handler.stop()
        audioPlayer.stop()
        currentStatus = PLAYING
        try {
            audioPlayer.setDataSource(mVoicePath)
            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
                override fun onCompletion() {
                    pausePosition = 0
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.icon_sign_record_3)
                    currentStatus = RECORDED
                    handler.stop()
                    tv_Time.text = "${mLength}S"
                }

                override fun onInterrupt() {//异常停止
                    waveView.stopImmediately()
                    iv_Record.setImageResource(R.mipmap.icon_sign_record_3)
                    currentStatus = RECORDED
                    handler.stop()
                }

                override fun onPrepared() {
                    audioPlayer.seekTo(pausePosition.toInt())
                    pausePosition = 0
                    iv_Record.setImageResource(R.mipmap.icon_sign_record_4)
                    currentStatus = PLAYING
                    waveView.start()
                    handler.start()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startRecord() {
        /* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {//申请录音的权限
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                 //申请WRITE_EXTERNAL_STORAGE权限
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                         REQUEST_PERMISSION_RECORD)//自定义的code
             } else {
                 initRecord()
             }
         } else {
         }*/
        initRecord()
    }

    private fun initRecord() {
        audioMessageHelper.startRecord()
        iv_Record.setImageResource(R.mipmap.icon_sign_record_2)
        waveView.start()
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                        .add("resourceType", "2")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(mVoicePath))}.aac")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            result.data.bucket_id?.let {
                                AppTools.bucketId = it
                            }
                            AliLoadFactory(this@SignRecordActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    runOnUiThread {
                                        LocalLogUtils.writeLog("SignRecordActivity:上传成功,开始接口请求", System.currentTimeMillis())
                                        resultVoicePath = result.data.resource_content
                                        request(1)
                                    }
                                }

                                override fun fail() {
                                    LocalLogUtils.writeLog("SignRecordActivity:upload fail by alibaba oss", System.currentTimeMillis())
                                    runOnUiThread { transLayout.showContent() }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {
                                }
                            }, UploadData(result.data.resource_content, mVoicePath))
                        } else {
                            LocalLogUtils.writeLog("SignRecordActivity:upload fail interface error code:${result.code} msg:${result.msg}", System.currentTimeMillis())
                            showToast(resources.getString(R.string.string_sign_record_hint))
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                val builder = FormBody.Builder()
                        .add("voiceType", "2")
                        .add("voiceUri", resultVoicePath)
                        .add("voiceLen", mLength.toString())
                        .add("bucketId", AppTools.bucketId)
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.post(this, "voices", builder.build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            LocalLogUtils.writeLog("SignRecordActivity:user wave setting success", System.currentTimeMillis())
                            SPUtils.setBoolean(this@SignRecordActivity, IConstant.WORLD_SHOW_TAB + loginBean.user_id, true)
                            LoginActivity.instance?.finish()
                            VerifyActivity.instance?.finish()
                            SignNameActivity.instance?.finish()
                            SignActivity.instance?.finish()
                            startActivity(Intent(this@SignRecordActivity, MainActivity::class.java)
                                    .putExtra("isSign", true))
                            finish()
                        } else {
                            LocalLogUtils.writeLog("SignRecordActivity:user wave setting fail ${result.msg}", System.currentTimeMillis())
                            showToast(result.msg)
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        LocalLogUtils.writeLog("SignRecordActivity: 接口错误 ${any.toString()}", System.currentTimeMillis())
                        transLayout.showContent()
                        if (any is Exception) {
                            showToast(any.message)
                        } else {
                            showToast(any.toString())
                        }
                    }
                })
            }
        }
    }

    private fun openAnim() {
        val anim = AnimatorSet()
        anim.playTogether(ObjectAnimator.ofFloat(relativeRecord, "translationX", -AppTools.dp2px(this, 65).toFloat()).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "translationX", -AppTools.dp2px(this, 65).toFloat()).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "alpha", 0f, 1f).setDuration(320),
                ObjectAnimator.ofFloat(tv_Send, "alpha", 0f, 1f).setDuration(320)
                /* ObjectAnimator.ofFloat(relativeRecord, "translationY", -AppTools.dp2px(this, 15).toFloat()).setDuration(520)*/)
        anim.start()
        tv_Send.visibility = View.VISIBLE
    }

    private fun restoreAnim() {
        tv_Send.visibility = View.GONE
        val anim = AnimatorSet()
        anim.playTogether(ObjectAnimator.ofFloat(relativeRecord, "translationX", -AppTools.dp2px(this, 65).toFloat(), 0f).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "translationX", -AppTools.dp2px(this, 65).toFloat(), 0f).setDuration(520),
                ObjectAnimator.ofFloat(linearOperate, "alpha", 1f, 0f).setDuration(320),
                ObjectAnimator.ofFloat(tv_Send, "alpha", 1f, 0f).setDuration(320)
                /*ObjectAnimator.ofFloat(relativeRecord, "translationY", -AppTools.dp2px(this, 15).toFloat(), 0f).setDuration(520)*/)
        anim.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mkdris()
            } else {
//                iv_Record.isSelected = (false)
            }
            REQUEST_PERMISSION_RECORD -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                initRecord()
            } else {
//                iv_Record.isSelected = (false)
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    SPUtils.setBoolean(this, IConstant.PERMISSION_DENIED_AUDIO, true)
                }
            }
        }
    }


    override fun onRecordSuccess(p0: File?, p1: Long, p2: RecordType?) {
        waveView.stopImmediately()
        LocalLogUtils.writeLog("SignRecordActivity:record success start upload", System.currentTimeMillis())
        mLength = (p1 * 1f / 1000 + 0.5f).toInt()
        if (mLength > 120) mLength = 120
        if (mLength < 1) {
            mLength = 1
        }
        openAnim()
        iv_Record.setImageResource(R.mipmap.icon_sign_record_3)
        handler.stop()
        tv_Time.text = "${mLength}S"
        mVoicePath = p0!!.absolutePath
        currentStatus = RECORDED
    }

    override fun onRecordReachedMaxTime(p0: Int) {
        showToast(resources.getString(R.string.string_signRecordAct_6))
        audioMessageHelper.handleEndRecord(true, p0)
    }

    override fun onRecordReady() {
        tv_start_record.visibility = View.INVISIBLE
    }

    override fun onRecordCancel() {

    }

    override fun onRecordStart(p0: File?, p1: RecordType?) {
        tv_start_record.visibility = View.INVISIBLE
        LocalLogUtils.writeLog("SignRecordActivity:start record", System.currentTimeMillis())
        currentStatus = RECORDING
        time = System.currentTimeMillis()
        handler.start()
    }

    override fun onRecordFail() {
        pausePosition = 0
        audioPlayer.stop()
        mVoicePath = null
        handler.stop()
        iv_Record.setImageResource(R.mipmap.icon_sign_record_1)
        mLength = 0
        tv_Time.text = "0S"
        currentStatus = IDEL
        waveView.stopImmediately()
        tv_start_record.visibility = View.VISIBLE
        showToast("录制失败,请在设置中打开语音录制权限")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isCanBack) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        handler.stop()
        handler.removeCallbacks(handler)
        audioMessageHelper.destroyAudioRecorder()
    }
}