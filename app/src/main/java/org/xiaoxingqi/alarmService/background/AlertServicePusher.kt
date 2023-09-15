package org.xiaoxingqi.alarmService.background

import android.content.Context
import android.content.Intent
import android.util.Log
import org.xiaoxingqi.alarmService.configuration.Store
import org.xiaoxingqi.alarmService.interfaces.Intents
import org.xiaoxingqi.alarmService.oreo
import org.xiaoxingqi.alarmService.preOreo
import org.xiaoxingqi.shengxi.core.App.Companion.container
import java.lang.RuntimeException

class AlertServicePusher(store: Store, context: Context) {
    init {
        val disposable = store.events
                .map {
                    when (it) {
                        is Event.AlarmEvent -> Intent(Intents.ALARM_ALERT_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
                        is Event.PrealarmEvent -> Intent(Intents.ALARM_PREALARM_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
                        is Event.DismissEvent -> Intent(Intents.ALARM_DISMISS_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
                        is Event.SnoozedEvent -> Intent(Intents.ALARM_SNOOZE_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
                        is Event.Autosilenced -> Intent(Intents.ACTION_SOUND_EXPIRED).apply { putExtra(Intents.EXTRA_ID, it.id) }
                        is Event.MuteEvent -> Intent(Intents.ACTION_MUTE)
                        is Event.DemuteEvent -> Intent(Intents.ACTION_DEMUTE)
                        is Event.CancelSnoozedEvent -> Intent(Intents.ACTION_CANCEL_SNOOZE)
                        is Event.NullEvent -> throw RuntimeException("NullEvent")
                    }.apply {
                        setClass(context, AlertServiceWrapper::class.java)
                    }
                }
                .filter { it.action != Intents.ACTION_CANCEL_SNOOZE }
                .subscribe { intent ->
                    container().wakeLocks().acquireTransitionWakeLock(intent)
                    oreo { context.startForegroundService(intent) }
                    preOreo { context.startService(intent) }
                }
    }
}