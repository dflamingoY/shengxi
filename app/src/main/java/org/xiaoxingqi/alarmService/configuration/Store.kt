package org.xiaoxingqi.alarmService.configuration


import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.xiaoxingqi.alarmService.background.Event
import org.xiaoxingqi.alarmService.model.AlarmValue
import org.xiaoxingqi.alarmService.utils.Optional

/**
 * Created by Yuriy on 10.06.2017.
 */

data class Store(
        val alarmsSubject: BehaviorSubject<List<AlarmValue>>,
        val next: BehaviorSubject<Optional<Next>>,
        val sets: Subject<AlarmSet>,
        val events: Subject<Event>
) {
    fun alarms(): Observable<List<AlarmValue>> {
        return alarmsSubject().distinctUntilChanged()
    }

    fun alarmsSubject(): BehaviorSubject<List<AlarmValue>> = alarmsSubject

    fun next(): BehaviorSubject<Optional<Next>> = next

    fun sets(): Subject<AlarmSet> = sets

    data class Next(
            val isPrealarm: Boolean,

            val alarm: AlarmValue,

            val nextNonPrealarmTime: Long

    ) {
        fun alarm(): AlarmValue = alarm

        fun nextNonPrealarmTime(): Long = nextNonPrealarmTime
    }

    data class AlarmSet(
            val alarm: AlarmValue,

            val millis: Long

    ) {
        fun alarm(): AlarmValue = alarm

        fun millis(): Long = millis
    }
}

