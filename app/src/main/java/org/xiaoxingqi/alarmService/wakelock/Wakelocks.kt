package org.xiaoxingqi.alarmService.wakelock

interface Wakelocks {
    fun acquireServiceLock()

    fun releaseServiceLock()
}