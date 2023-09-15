package org.xiaoxingqi.alarmService.background

import android.content.Context
import android.content.res.Resources
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import org.xiaoxingqi.alarmService.model.Alarmtone
import org.xiaoxingqi.shengxi.utils.IConstant
import java.io.File
import java.util.*

class PlayerWrapper(
        val resources: Resources,
        val context: Context
) : Player {
    override fun setDataSource(alarmtone: Alarmtone) {
        // Fall back on the default alarm if the database does not have an
        // alarm stored.
        val uri = when (alarmtone) {
            is Alarmtone.Silent -> throw RuntimeException("alarm is silent")
            is Alarmtone.Default -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            is Alarmtone.Sound -> Uri.parse(alarmtone.uriString)
        }
        val playUrl = uri.toString().split(",").filter {
            !TextUtils.isEmpty(it) && it != ","
        }.let {
            it[Random().nextInt(it.size)]
        }
        if (playUrl.contains("assets://")) {
            val openFd = context.assets.openFd(playUrl.replace("assets://", ""))
            player?.setDataSource(openFd.fileDescriptor, openFd.startOffset, openFd.length)
        } else {
            if (File(playUrl).exists()) {
                player?.setDataSource(playUrl)
            } else {
                val openFd = context.assets.openFd(IConstant.LOCAL_DEFAULT_ALARM.replace("assets://", ""))
                player?.setDataSource(openFd.fileDescriptor, openFd.startOffset, openFd.length)
            }
        }
    }

    private var player: MediaPlayer? = MediaPlayer().apply {
        setOnErrorListener { mp, _, _ ->
            mp.stop()
            mp.release()
            player = null
            true
        }
    }

    override fun startAlarm() {
        player?.runCatching {
            setAudioStreamType(AudioManager.STREAM_ALARM)
            isLooping = true
            prepare()
            start()
        }
    }

    override fun setDataSourceFromResource(res: Int) {
        resources.openRawResourceFd(res)?.run {
            player?.setDataSource(fileDescriptor, startOffset, length)
            close()
        }
    }

    override fun setPerceivedVolume(perceived: Float) {
        val volume = perceived.squared()
        player?.setVolume(volume, volume)
    }

    /**
     * Stops alarm audio
     */
    override fun stop() {
        try {
            player?.run {
                if (isPlaying) stop()
                release()
            }
        } finally {
            player = null
        }
    }

    override fun reset() {
        player?.reset()
    }

    private fun Float.squared() = this * this
}