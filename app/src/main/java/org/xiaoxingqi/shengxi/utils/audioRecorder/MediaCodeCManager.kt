package org.xiaoxingqi.shengxi.utils.audioRecorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class MediaCodeCManager(private val targetPath: String) {
    private var audioCodec: MediaCodec? = null
    private var audioBuffer: MediaCodec.BufferInfo? = null
    private var sampleRate = 0
    private var channelCount = 0
    private val audioFormat = 16
    private var presentationTimeUs: Long = 0
    private val threadStarted = AtomicBoolean(false)
    private val encodeStart = AtomicBoolean(false)
    private var bos: BufferedOutputStream? = null
    private val encodeThread = CodeAudio()
    fun init(audioType: String, sampleRate: Int, channels: Int) {
        try {
            this.sampleRate = sampleRate
            this.channelCount = channels
            audioCodec = MediaCodec.createEncoderByType(audioType)
            val audioFormat = MediaFormat.createAudioFormat(audioType, sampleRate, channels)
            val BIT_RATE = 96000
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            val MAX_INOUT_SIZE = 8192
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INOUT_SIZE)
            audioCodec!!.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            audioBuffer = MediaCodec.BufferInfo()
        } catch (e: IOException) {
            LocalLogUtils.writeLog("MediaCodeCManager : 音频类型无效, 设备信息", System.currentTimeMillis())
        }
        bos = BufferedOutputStream(FileOutputStream(targetPath))
    }

    fun setPcmData(pcmBuffer: ByteArray, buffSize: Int) {
        if (audioCodec == null) {
            return
        }
        try {
            val buffIndex = audioCodec?.dequeueInputBuffer(0) ?: 0
            if (buffIndex < 0) {
                return
            }
            val byteBuffer: ByteBuffer?
            byteBuffer = audioCodec?.getInputBuffer(buffIndex)
            if (byteBuffer == null) {
                return
            }
            val position = byteBuffer.position()
            byteBuffer.clear()
            byteBuffer.put(pcmBuffer)
            //presentationTimeUs = 1000000L * (buffSize / 2) / sampleRate
            //一帧音频帧大小 int size = 采样率 x 位宽 x 采样时间 x 通道数
            // 1s时间戳计算公式  presentationTimeUs = 1000000L * (totalBytes / sampleRate/ audioFormat / channelCount / 8 )
            //totalBytes : 传入编码器的总大小
            //1000 000L : 单位为 微秒，换算后 = 1s,
            //除以8     : pcm原始单位是bit, 1 byte = 8 bit, 1 short = 16 bit, 用 Byte[]、Short[] 承载则需要进行换算
            presentationTimeUs += (1.0 * buffSize / (sampleRate * channelCount * (audioFormat / 16)) * 1000000L).toLong()
            Log.d("Mozator", "presentationTimeUs: $presentationTimeUs  buffSize: $buffSize ")
            var flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
            if (!threadStarted.get()) {
                flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
            }
            audioCodec?.queueInputBuffer(buffIndex, position, buffSize, presentationTimeUs, flags)
        } catch (e: IllegalStateException) {
            //audioCodec 线程对象已释放MediaCodec对象
        }
    }

    fun startEncode() {
        Thread(encodeThread).start()
    }

    fun stopEncode() {
        threadStarted.set(false)
        encodeStart.set(false)
    }

    //解码录音文件
    private inner class CodeAudio : Runnable {
        override fun run() {
            threadStarted.set(true)
            audioCodec?.start()
            while (true) {
                if (!threadStarted.get()) {
                    audioCodec?.stop()
                    audioCodec?.release()
                    audioCodec = null
                    encodeStart.set(false)
                    try {
                        bos?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    break
                }
                if (audioCodec == null) break
                var outputBufferIndex = audioCodec!!.dequeueOutputBuffer(audioBuffer, 0)
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    //开始录入数据
                    encodeStart.set(true)
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encodeStart.get()) {
                            SystemClock.sleep(10)
                            continue
                        }
                        val outputBuffer = audioCodec!!.getOutputBuffer(outputBufferIndex)
                        outputBuffer.position(audioBuffer!!.offset)
                        outputBuffer.limit(audioBuffer!!.offset + audioBuffer!!.size)
                        val chunkAudio = ByteArray(audioBuffer!!.size + 7)
                        val outPacketSize: Int = audioBuffer!!.size + 7 //7为ADTS头部的大小
                        ADTSUtils.addADTStoPacket(4, chunkAudio, outPacketSize)
                        //写入本地音频文件
                        try {
                            outputBuffer[chunkAudio, 7, audioBuffer!!.size]
                            bos!!.write(chunkAudio, 0, chunkAudio.size)
                            bos!!.flush()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        audioCodec!!.releaseOutputBuffer(outputBufferIndex, false)
                        outputBufferIndex = audioCodec!!.dequeueOutputBuffer(audioBuffer, 0)
                    }
                }
            }
        }
    }
}