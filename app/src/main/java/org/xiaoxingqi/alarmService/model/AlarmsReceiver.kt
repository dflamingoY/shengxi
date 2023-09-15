package org.xiaoxingqi.alarmService.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.xiaoxingqi.alarmService.interfaces.PresentationToModelIntents
import org.xiaoxingqi.shengxi.core.App.Companion.container

class AlarmsReceiver : BroadcastReceiver() {
    private val alarms = container().rawAlarms()
    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("Mozator", "接收到新的广播  ${intent.action}")
        try {
            when (intent.action) {
                AlarmsScheduler.ACTION_FIRED -> {
                    val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                    val calendarType = CalendarType.valueOf(intent.extras!!.getString(AlarmsScheduler.EXTRA_TYPE))
                    alarms.getAlarm(id)?.let {
                        alarms.onAlarmFired(it, calendarType)
                    }
                }
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_LOCALE_CHANGED,
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    alarms.refresh()
                }
                Intent.ACTION_TIME_CHANGED -> alarms.onTimeSet()
                PresentationToModelIntents.ACTION_REQUEST_SNOOZE -> {
                    val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                    alarms.getAlarm(id)?.snooze()
                }
                PresentationToModelIntents.ACTION_REQUEST_DISMISS -> {
                    val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                    alarms.getAlarm(id)?.dismiss()
                }
            }
        } catch (e: Exception) {
        }
    }
}