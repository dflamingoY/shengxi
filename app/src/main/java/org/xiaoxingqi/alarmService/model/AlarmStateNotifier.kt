package org.xiaoxingqi.alarmService.model

import org.xiaoxingqi.alarmService.background.Event
import org.xiaoxingqi.alarmService.configuration.Store
import org.xiaoxingqi.alarmService.interfaces.Intents

/**
 * Broadcasts alarm state with an intent
 *
 * @author Yuriy
 */
class AlarmStateNotifier(private val store: Store) : AlarmCore.IStateNotifier {
    override fun broadcastAlarmState(id: Int, action: String) {
        val event = when (action) {
            Intents.ALARM_ALERT_ACTION -> Event.AlarmEvent(id)
            Intents.ALARM_PREALARM_ACTION -> Event.PrealarmEvent(id)
            Intents.ACTION_MUTE -> Event.MuteEvent()
            Intents.ACTION_DEMUTE -> Event.DemuteEvent()
            Intents.ACTION_SOUND_EXPIRED -> Event.Autosilenced(id)
            Intents.ALARM_SNOOZE_ACTION -> Event.SnoozedEvent(id)
            Intents.ACTION_CANCEL_SNOOZE -> Event.CancelSnoozedEvent(id)
            Intents.ALARM_DISMISS_ACTION -> Event.DismissEvent(id)
            else -> throw RuntimeException("Unknown action $action")
        }
        store.events.onNext(event)
    }
}
