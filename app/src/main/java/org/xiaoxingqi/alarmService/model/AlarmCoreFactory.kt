package org.xiaoxingqi.alarmService.model

import org.xiaoxingqi.alarmService.statemachine.HandlerFactory
import org.xiaoxingqi.alarmService.configuration.Prefs
import org.xiaoxingqi.alarmService.configuration.Store

/**
 * Created by Yuriy on 09.08.2017.
 */

class AlarmCoreFactory(
        private val alarmsScheduler: IAlarmsScheduler,
        private val broadcaster: AlarmCore.IStateNotifier,
        private val handlerFactory: HandlerFactory,
        private val prefs: Prefs,
        private val store: Store,
        private val calendars: Calendars
) {
    fun create(container: AlarmActiveRecord): AlarmCore {
        return AlarmCore(container, alarmsScheduler, broadcaster, handlerFactory, prefs, store, calendars)
    }
}
