package org.xiaoxingqi.shengxi.modules.adminManager

import android.media.AudioManager
import kotlinx.android.synthetic.main.activity_test_audio.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.utils.AudioPlayer
import org.xiaoxingqi.shengxi.utils.VoiceRecorder
import org.xiaoxingqi.shengxi.utils.audioRecorder.AudioRecorder

/*
 */
class TestAudioStatusActivity : BaseAct() {
    private var audioPlayer: AudioPlayer? = null
    private var file: String? = null
    private var audioRecorder: AudioRecorder? = null
    private var voiceRecorder: VoiceRecorder? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_test_audio
    }

    override fun initView() {

    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        voiceRecorder = VoiceRecorder(120, object : VoiceRecorder.OnRecorderListener {
            override fun onRecordSuccess(path: String?, length: Long) {
                file = path
            }

            override fun onRecordCancel() {

            }

            override fun onRecordStart() {

            }

            override fun onRecordFail() {

            }
        })
//        audioRecorder = AudioRecorder(this, 120, this)
    }

    override fun initEvent() {
        btnStart.setOnClickListener {
//            audioRecorder?.start()
            voiceRecorder?.start()
        }
        btnStop.setOnClickListener {
//            audioRecorder?.complete(false)
            voiceRecorder?.completeRecord()
        }
        btnPlay.setOnClickListener {
            audioPlayer?.setDataSource(file)
            audioPlayer?.start(AudioManager.STREAM_MUSIC)
        }
    }

    /*override fun onRecordReady() {

    }

    override fun onRecordStart(var1: String?) {

    }

    override fun onRecordSuccess(var1: String?, var2: Long) {
        file = var1
    }

    override fun onRecordFail(msg: String) {
        println(msg)
    }

    override fun onRecordCancel() {

    }*/

}