package org.xiaoxingqi.shengxi.utils.audioRecorder

interface IAudioRecorderListener {

    fun onRecordReady()

    fun onRecordStart(var1: String?)

    fun onRecordSuccess(var1: String?, var2: Long)

    fun onRecordFail(msg:String)

    fun onRecordCancel()
}