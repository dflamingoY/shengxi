package org.xiaoxingqi.alarmService.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.xiaoxingqi.alarmService.NotificationImportance
import org.xiaoxingqi.alarmService.configuration.Prefs
import org.xiaoxingqi.alarmService.interfaces.Intents
import org.xiaoxingqi.alarmService.notificationBuilder
import org.xiaoxingqi.alarmService.oreo
import org.xiaoxingqi.shengxi.BuildConfig
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App.Companion.container

class AlertServiceWrapper : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private lateinit var alertService: AlertService
    private val tm = container().telephonyManager()
    private val inCall: Observable<Boolean> = Observable
            .create<Int> { emitter ->
                emitter.onNext(tm.callState)
                val listener = object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, ignored: String) {
                        emitter.onNext(state)
                    }
                }

                emitter.setCancellable {
                    // Stop listening for incoming calls.
                    tm.listen(listener, PhoneStateListener.LISTEN_NONE)
                }

                tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
            }
            .map { it != TelephonyManager.CALL_STATE_IDLE }
            .distinctUntilChanged()
            .replay(1)
            .refCount()

    override fun onCreate() {
        val fadeInTimeInMillis = container().rxPrefs()
                .getString(Prefs.KEY_FADE_IN_TIME_SEC, "30")
                .asObservable()
                .map { s -> Integer.parseInt(s) * 1000 }
        alertService = AlertService(
                wakelocks = container().wakeLocks(),
                alarms = container().alarms(),
                inCall = inCall,
                plugins = arrayOf(
                        KlaxonPlugin(
                                playerFactory = {
                                    PlayerWrapper(
                                            context = container().context(),
                                            resources = container().context.resources
                                    )
                                },
                                prealarmVolume = container().rxPrefs().getInteger(Prefs.KEY_PREALARM_VOLUME, Prefs.DEFAULT_PREALARM_VOLUME).asObservable(),
                                fadeInTimeInMillis = fadeInTimeInMillis,
                                inCall = inCall,
                                scheduler = AndroidSchedulers.mainThread()
                        ),
                        VibrationPlugin(
                                vibrator = container().vibrator(),
                                fadeInTimeInMillis = container().rxPrefs()
                                        .getString(Prefs.KEY_FADE_IN_TIME_SEC, "30")
                                        .asObservable()
                                        .map { s -> Integer.parseInt(s) * 1000 },
                                scheduler = AndroidSchedulers.mainThread(),
                                vibratePreference = container().rxPrefs().getBoolean("vibrate").asObservable()
                        ),
                        NotificationsPlugin(
                                mContext = this,
                                nm = container().notificationManager(),
                                startForeground = { id, notification -> this.startForeground(id, notification) },
                                prefs = container().prefs()
                        )
                ),
                handleUnwantedEvent = {
                    oreo {
                        val notification = notificationBuilder("${BuildConfig.APPLICATION_ID}.AlertServiceWrapper", NotificationImportance.LOW) {
                            setContentTitle("Background")
                            setContentText("Background")
                            setSmallIcon(R.drawable.stat_notify_alarm)
                            setOngoing(true)
                        }
                        this.startForeground(42, notification)
                    }
                    stopSelf()
                },
                stopSelf = {
                    stopSelf()
                }
        )
    }

    override fun onDestroy() {
        container().wakeLocks().releaseServiceLock()
        oreo {
            stopForeground(true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent == null) {
            // this also has to be delivered, because someone has to call startForeground()
            alertService.onStartCommand(Event.NullEvent())
            START_NOT_STICKY
        } else {
            alertService.onStartCommand(when (intent.action) {
                Intents.ALARM_ALERT_ACTION -> Event.AlarmEvent(intent.getIntExtra(Intents.EXTRA_ID, -1))
                Intents.ALARM_PREALARM_ACTION -> Event.PrealarmEvent(intent.getIntExtra(Intents.EXTRA_ID, -1))
                Intents.ACTION_MUTE -> Event.MuteEvent()
                Intents.ACTION_DEMUTE -> Event.DemuteEvent()
                Intents.ACTION_SOUND_EXPIRED -> Event.Autosilenced(intent.getIntExtra(Intents.EXTRA_ID, -1))
                Intents.ALARM_SNOOZE_ACTION -> Event.SnoozedEvent(intent.getIntExtra(Intents.EXTRA_ID, -1))
                Intents.ALARM_DISMISS_ACTION -> Event.DismissEvent(intent.getIntExtra(Intents.EXTRA_ID, -1))
                else -> throw RuntimeException("Unknown action ${intent.action}")
            })

            val ret = when (intent.action) {
                Intents.ALARM_ALERT_ACTION,
                Intents.ALARM_PREALARM_ACTION,
                Intents.ACTION_MUTE,
                Intents.ACTION_DEMUTE -> START_STICKY
                Intents.ALARM_SNOOZE_ACTION,
                Intents.ALARM_DISMISS_ACTION,
                Intents.ACTION_SOUND_EXPIRED -> START_NOT_STICKY
                else -> throw RuntimeException("Unknown action ${intent.action}")
            }

            container().wakeLocks().releaseTransitionWakeLock(intent)
            ret
        }
    }
}