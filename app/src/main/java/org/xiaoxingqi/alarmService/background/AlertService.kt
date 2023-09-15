package org.xiaoxingqi.alarmService.background

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import org.xiaoxingqi.alarmService.interfaces.IAlarmsManager
import org.xiaoxingqi.alarmService.interfaces.Intents
import org.xiaoxingqi.alarmService.model.Alarmtone
import org.xiaoxingqi.alarmService.wakelock.Wakelocks

interface AlertPlugin {
    fun go(alarm: PluginAlarmData, prealarm: Boolean, targetVolume: Observable<TargetVolume>): Disposable
}

data class PluginAlarmData(val id: Int, val alarmtone: Alarmtone, val label: String)

enum class TargetVolume { MUTED, FADED_IN, FADED_IN_FAST }

sealed class Event {
    data class NullEvent(val actions: String = "null") : Event()
    data class AlarmEvent(val id: Int, val actions: String = Intents.ALARM_ALERT_ACTION) : Event()
    data class PrealarmEvent(val id: Int, val actions: String = Intents.ALARM_PREALARM_ACTION) : Event()
    data class DismissEvent(val id: Int, val actions: String = Intents.ALARM_DISMISS_ACTION) : Event()
    data class SnoozedEvent(val id: Int, val actions: String = Intents.ALARM_SNOOZE_ACTION) : Event()
    data class CancelSnoozedEvent(val id: Int, val actions: String = Intents.ACTION_CANCEL_SNOOZE) : Event()
    data class Autosilenced(val id: Int, val actions: String = Intents.ACTION_SOUND_EXPIRED) : Event()
    data class MuteEvent(val actions: String = Intents.ACTION_MUTE) : Event()
    data class DemuteEvent(val actions: String = Intents.ACTION_DEMUTE) : Event()
}

/**
 * Listens to all kinds of events, vibrates, shows notifications and so on.
 */
class AlertService(
        private val wakelocks: Wakelocks,
        private val alarms: IAlarmsManager,
        private val inCall: Observable<Boolean>,
        private val plugins: Array<AlertPlugin>,
        private val handleUnwantedEvent: () -> Unit,
        private val stopSelf: () -> Unit
) {
    private val wantedVolume: BehaviorSubject<TargetVolume> = BehaviorSubject.createDefault(TargetVolume.MUTED)

    private enum class Type { NORMAL, PREALARM }
    private data class CallState(val initial: Boolean, val inCall: Boolean)

    private var soundAlarmDisposable: CompositeDisposable = CompositeDisposable()

    init {
        wakelocks.acquireServiceLock()
    }

    private var playingAlarm = false
    fun onStartCommand(event: Event) {
        if (!playingAlarm) {
            when (event) {
                // we will start playing now
                is Event.AlarmEvent, is Event.PrealarmEvent -> {
                }
                else -> {
                    handleUnwantedEvent()
                }
            }
        }

        when (event) {
            is Event.AlarmEvent -> soundAlarm(event.id, Type.NORMAL)
            is Event.PrealarmEvent -> soundAlarm(event.id, Type.PREALARM)
            is Event.MuteEvent -> wantedVolume.onNext(TargetVolume.MUTED)
            is Event.DemuteEvent -> wantedVolume.onNext(TargetVolume.FADED_IN_FAST)
            is Event.DismissEvent, is Event.SnoozedEvent, is Event.Autosilenced -> {
                if (playingAlarm) {
                    playingAlarm = false
                    wantedVolume.onNext(TargetVolume.MUTED)
                    soundAlarmDisposable.dispose()
                    wakelocks.releaseServiceLock()
                    stopSelf()
                }
            }
        }
    }

    private fun soundAlarm(id: Int, type: Type) {
        // new alarm - dispose all current signals
        soundAlarmDisposable.dispose()
        playingAlarm = true

        wantedVolume.onNext(TargetVolume.FADED_IN)

        val targetVolumeAccountingForInCallState: Observable<TargetVolume> = Observable.combineLatest<TargetVolume, CallState, TargetVolume>(
                wantedVolume,
                inCall.zipWithIndex { callActive, index ->
                    CallState(index == 0, callActive)
                }, BiFunction { volume, callState ->
            when {
                !callState.initial && callState.inCall -> TargetVolume.MUTED
                !callState.initial && !callState.inCall -> TargetVolume.FADED_IN_FAST
                else -> volume
            }
        })

        val alarm = alarms.getAlarm(id)
        val alarmtone = alarm?.alarmtone ?: Alarmtone.Default()
        val label = alarm?.labelOrDefault ?: ""
        soundAlarmDisposable = CompositeDisposable(
                plugins.map {
                    it.go(PluginAlarmData(id, alarmtone, label), prealarm = type == Type.PREALARM, targetVolume = targetVolumeAccountingForInCallState)
                }
        )
    }

    private fun <U, D> Observable<U>.zipWithIndex(function: (U, Int) -> D): Observable<D> {
        return zipWith(generateSequence(0) { it + 1 }.asIterable()) { next, index -> function.invoke(next, index) }
    }
}