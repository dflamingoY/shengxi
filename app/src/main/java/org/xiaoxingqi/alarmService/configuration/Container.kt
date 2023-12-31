package org.xiaoxingqi.alarmService.configuration

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.PowerManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import org.xiaoxingqi.alarmService.model.Alarms
import org.xiaoxingqi.alarmService.interfaces.IAlarmsManager
import org.xiaoxingqi.alarmService.wakelock.WakeLockManager

data class Container(val context: Context,
                     val sharedPreferences: SharedPreferences,
                     val rxPrefs: RxSharedPreferences,
                     val prefs: Prefs,
                     val store: Store,
                     val rawAlarms: Alarms) {

    private val wlm: WakeLockManager = WakeLockManager(powerManager())

    fun context(): Context = context

    fun sharedPreferences(): SharedPreferences = sharedPreferences

    fun rxPrefs(): RxSharedPreferences = rxPrefs

    fun prefs(): Prefs = prefs

    fun store(): Store = store

    fun rawAlarms(): Alarms = rawAlarms

    fun alarms(): IAlarmsManager {
        return rawAlarms()
    }

    fun wakeLocks(): WakeLockManager {
        return wlm
    }

    fun vibrator(): Vibrator {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun powerManager(): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    fun telephonyManager(): TelephonyManager {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    fun notificationManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun audioManager(): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

}