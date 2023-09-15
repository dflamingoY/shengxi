package org.xiaoxingqi.shengxi.utils.audioRecorder

import android.content.Context
import android.media.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorder constructor(private val context: Context, val length: Int, private val listener: IAudioRecorderListener?) {
    //解码PCM
    //录制
    private var audioRecord: AudioRecord? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val isRecording = AtomicBoolean(false)
    private var pcmPath: String? = null
    private var recordThread: Thread? = null
    private var codecManager: MediaCodeCManager? = null
    private val mEventHandler = Handler(Looper.getMainLooper())//用于回调到主线程
    private val maxLength = length * 1000L
    private var startTime = 0L

    companion object {
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO//保证所有机型上正常运行
    }

    /**
     * 缓冲区大小
     */
    private var bufferSize = 0

    init {
        LocalLogUtils.writeLog("AudioRecorder:init $context", System.currentTimeMillis())
    }

    private fun init() {
        bufferSize = AudioRecord.getMinBufferSize(44100, CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
    }

    fun start() {
        if (isRecording.get()) {
            LocalLogUtils.writeLog("当前正在录制", System.currentTimeMillis())
            print("current state is isRecording ,please stop record")
            return
        }
        init()
        val rootFile = File(context.getExternalFilesDir(""), ".cache/audio")
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }
        pcmPath = File(rootFile, UUID.randomUUID().toString()).absolutePath
        codecManager = MediaCodeCManager(pcmPath!!)
        codecManager!!.init(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
        codecManager!!.startEncode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build())
        } else {
            audioManager.requestAudioFocus(null as AudioManager.OnAudioFocusChangeListener?, 0, 2)
        }
        if (isRecording.get()) {
            listener?.onRecordFail("录制失败, 资源被占用")
            return
        }
        listener?.onRecordReady()
        if (!isRecording.get() && audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            println("初始化成功, 开始录制")
            audioRecord?.startRecording()
            isRecording.set(true)
            recordThread = Thread(RecordThread())
            recordThread?.start()
        } else {
            listener?.onRecordFail("AudioRecord state " + audioRecord?.state)
        }
    }

    fun isRecording(): Boolean {
        return isRecording.get()
    }

    /**
     * @param isComplete true 取消录制,otherwise record finish
     */
    fun complete(isComplete: Boolean) {
        if (!isRecording.get()) {
            return
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioManager.abandonAudioFocusRequest(AudioFocusRequest.Builder())
//        } else
        audioManager.abandonAudioFocus(null)//释放焦点
        stop()
        if (isComplete) {
            //删除本地文件
            listener?.onRecordCancel()
            File(pcmPath).let {
                if (it.exists()) {
                    it.delete()
                }
            }
        } else {
            mEventHandler.post {
                if (File(pcmPath).length() < 1000) {
                    listener?.onRecordFail("录制失败, 可能是资源被占用")
                } else {
                    LocalLogUtils.writeLog("AudioRecorder:record success", System.currentTimeMillis())
                    listener?.onRecordSuccess(pcmPath, (System.currentTimeMillis() - startTime))
                }
            }
        }
    }

    /*
     * 停止录制
     */
    private fun stop() {
        isRecording.set(false)
        recordThread?.interrupt()
        //释放资源
        recordRelease()
        codecManager!!.stopEncode()
    }

    private fun recordRelease() {
        audioRecord?.let {
            if (it.state == AudioRecord.STATE_INITIALIZED) {
                it.stop()
            }
            it.release()
        }
        audioRecord = null
    }

    private fun handlerEvent(type: Int) {
        if (type == 1)
            mEventHandler.post {
                listener?.onRecordStart(pcmPath)
            }
    }

    private inner class RecordThread : Runnable {
        override fun run() {
            val buffer = ByteArray(bufferSize)
            handlerEvent(1)
            startTime = System.currentTimeMillis()
            while (isRecording.get()) {
                val audioSampleSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (audioSampleSize > 0) {
                    codecManager?.setPcmData(buffer, audioSampleSize)
                } else {
                    println(" read 为0 ")
                }
                //延迟写入 SystemClock  --  Android专用
                if (System.currentTimeMillis() - startTime > maxLength) {
                    complete(false)
                }
                SystemClock.sleep(10)
            }
        }
    }

    //移除所有资源
    fun destroyAudioRecorder() {
        if (isRecording.get()) {
            complete(true)
        }
        mEventHandler.removeCallbacksAndMessages(null)
        audioRecord = null
    }

}