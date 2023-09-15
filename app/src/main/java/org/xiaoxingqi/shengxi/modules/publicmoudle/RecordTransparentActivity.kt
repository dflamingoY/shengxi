package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import android.widget.FrameLayout
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import kotlinx.android.synthetic.main.activity_record_transparent.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import java.io.File

/*
    非默认状态宽度为 58
    外部为89
 */
class RecordTransparentActivity : BaseNormalActivity(), IAudioRecordCallback {
    companion object {
        private const val REQUEST_CORD = 0x00

        @JvmStatic
        var isOnCreate = false
    }

    private var audioMessageHelper: AudioRecorder? = null
    private var length = 120
    private var time: Long = 0
    private var isMusic = false
    private val handler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(500) {
        override fun handleMessage(msg: Message?) {
            var dTime = ((System.currentTimeMillis() - time) / 1000).toInt()
            if (!isMusic) {
                if (dTime > 120) {
                    dTime = 120
                }
            } else {
                if (dTime > 300) {
                    dTime = 300
                }
            }
            tv_Time.text = "${dTime}S"
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_record_transparent
    }

    override fun initView() {
        isOnCreate = true
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        sendObserver()//停止所有的播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTPERMSSIONCODE)
            } else {
                mkdris()
            }
        } else {
            mkdris()
        }
        //移动View 的位置
        if (intent.getBooleanExtra("isHome", false)) {
            //更换 relativeRecord 的位置
            iv_Record.setImageResource(R.mipmap.icon_transparent_recording_home)
            waveView.setColor(resources.getColor(R.color.color_blue_indicator))
            waveView.setInitialRadius(AppTools.dp2px(this, 29).toFloat())
            val param3 = relativeRecord.layoutParams as FrameLayout.LayoutParams
            param3.bottomMargin = AppTools.dp2px(this, -2)
            relativeRecord.layoutParams = param3
        } else {
            waveView.setColor(Color.parseColor("#FFB650"))
            val params = iv_Record.layoutParams
            params.width = AppTools.dp2px(this, 58)
            params.height = AppTools.dp2px(this, 58)
            val params1 = waveView.layoutParams
            params1.width = AppTools.dp2px(this, 89)
            params1.height = AppTools.dp2px(this, 89)
            val param3 = relativeRecord.layoutParams as FrameLayout.LayoutParams
            param3.bottomMargin = AppTools.dp2px(this, 50)
            relativeRecord.layoutParams = param3
            waveView.setInitialRadius(AppTools.dp2px(this, 25).toFloat())
        }
        tvRecordMaxLength.text = resources.getString(R.string.string_recordAct_2) + "120秒"
        if (intent.getBooleanExtra("isMusic", false)) {
            isMusic = true
            length = 300
            tvRecordMaxLength.text = resources.getString(R.string.string_recordAct_2) + "300秒"
        }
        //直接初始化开始录制
        audioMessageHelper = AudioRecorder(this, RecordType.AAC, length, this)
        waveView.setSpeed(1200)
        waveView.setMaxRadiusRate(1f)
        checkPermission()
    }

    //检测是否开启录音权限
    @Synchronized
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CORD)
            } else {
                startRecord()
            }
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        waveView.start()
        audioMessageHelper?.startRecord()
    }

    private fun mkdris() {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    override fun initEvent() {
        iv_Record.setOnClickListener {
            if (System.currentTimeMillis() - time < 600)
                return@setOnClickListener
            audioMessageHelper?.completeRecord(false)
        }
        tvRecordDetails.setOnClickListener {
            audioMessageHelper?.completeRecord(true)
            startActivity(Intent(this@RecordTransparentActivity, SendAct::class.java)
                    .putExtra("type", intent.getIntExtra("type", 1))
                    .putExtra("topicName", intent.getStringExtra("topicName"))
                    .putExtra("topicId", intent.getStringExtra("topicId"))
                    .putExtra("isHomeRecord", intent.getBooleanExtra("isHomeRecord", false))
                    .putExtra("isHome", intent.getBooleanExtra("isHome", false))
                    .putExtra("albumId", intent.getStringExtra("albumId"))
                    .putExtra("data", intent.getParcelableExtra<BaseSearchBean>("data"))
                    .putExtra("isOpenRecord", true)
            )
            finish()
        }
        ivClose.setOnClickListener {
            audioMessageHelper?.completeRecord(true)
            finish()
        }
    }

    override fun onRecordSuccess(p0: File?, p1: Long, p2: RecordType?) {
        handler.stop()
        //结束录制
        startActivity(Intent(this@RecordTransparentActivity, SendAct::class.java)
                .putExtra("type", intent.getIntExtra("type", 1))
                .putExtra("topicName", intent.getStringExtra("topicName"))
                .putExtra("topicId", intent.getStringExtra("topicId"))
                .putExtra("isHomeRecord", intent.getBooleanExtra("isHomeRecord", false))
                .putExtra("isHome", intent.getBooleanExtra("isHome", false))
                .putExtra("originalPath", p0!!.absolutePath)
                .putExtra("albumId", intent.getStringExtra("albumId"))
                .putExtra("data", intent.getParcelableExtra<BaseSearchBean>("data"))
                .putExtra("length", if (isMusic) {
                    if ((p1.toFloat() / 1000 + 0.5f).toInt() > 300) "300"
                    else (p1.toFloat() / 1000 + 0.5f).toInt().toString()
                } else {
                    if ((p1.toFloat() / 1000 + 0.5f).toInt() > 120) "120"
                    else (p1.toFloat() / 1000 + 0.5f).toInt().toString()
                }))
        finish()

    }

    override fun onRecordReachedMaxTime(p0: Int) {
        audioMessageHelper?.handleEndRecord(true, p0)
    }

    override fun onRecordReady() {
    }

    override fun onRecordCancel() {
        finish()
    }

    override fun onRecordStart(p0: File?, p1: RecordType?) {
        time = System.currentTimeMillis()
        handler.start()
    }

    override fun onRecordFail() {
        showToast("录音失败")
        finish()
    }

    override fun finish() {
        super.finish()
        audioMessageHelper?.destroyAudioRecorder()
        audioMessageHelper = null
        overridePendingTransition(R.anim.operate_enter, 0)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        //停止录音
        audioMessageHelper?.completeRecord(true)
    }

    override fun onBackPressed() {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CORD) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //开始录制
                    startRecord()
                } else {//拒绝权限不能关闭界面
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isOnCreate = false
    }

}