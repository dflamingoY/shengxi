package org.xiaoxingqi.alarmService.configuration

import org.xiaoxingqi.alarmService.model.AlarmData
import org.xiaoxingqi.alarmService.utils.Optional


/**
 * Created by Yuriy on 09.08.2017.
 */
data class EditedAlarm(val isNew: Boolean = false,
                       val id: Int = -1,
                       val value: Optional<AlarmData> = Optional.absent()
                       /*val holder: Optional<RowHolder> = Optional.absent()*/) {
    fun id() = id
    val isEdited: Boolean = value.isPresent()
}
