package org.xiaoxingqi.alarmService.model

import android.content.Context
import java.text.DateFormatSymbols
import java.util.*

/*
 * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
 * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
 * Sunday
 * 日期转换
 */
data class DaysOfWeek(val coded: Int) {
    // Returns days of week encoded in an array of booleans.
    val booleanArray = BooleanArray(7) { index -> index.isSet() }
    val isRepeatSet = coded != 0

    fun toString(context: Context, showNever: Boolean): String {
        return when {
            coded == 0 && showNever -> /*context.getText(R.string.never).toString()*/"从不"
            coded == 0 -> ""
            // every day
            coded == 0x7f -> return /*context.getText(R.string.every_day).toString()*/"每天"
            // count selected days
            else -> {
                val dayCount = (0..6).count { it.isSet() }
                // short or long form?
                val dayStrings = when {
                    dayCount > 1 -> DateFormatSymbols().shortWeekdays
                    else -> DateFormatSymbols().weekdays
                }
                //选择的日期
                val maps = (0..6).filter { it.isSet() }
                        .map { dayIndex -> DAY_MAP[dayIndex] }
                if (maps.size == 2 && maps.contains(Calendar.SATURDAY) && maps.contains(Calendar.SUNDAY)) {
                    "双休日"
                } else if (maps.size == 5 && maps.contains(Calendar.MONDAY) && maps.contains(Calendar.TUESDAY) && maps.contains(Calendar.WEDNESDAY) && maps.contains(Calendar.THURSDAY) && maps.contains(Calendar.FRIDAY)) {
                    "工作日"
                } else {
                    maps.map { calDay -> dayStrings[calDay] }
                            .joinToString(", ")
                }
            }
        }
    }

    private fun Int.isSet(): Boolean {
        return coded and (1 shl this) > 0
    }

    /**
     * returns number of days from today until next alarm
     */
    fun getNextAlarm(today: Calendar): Int {
        val todayIndex = (today.get(Calendar.DAY_OF_WEEK) + 5) % 7

        return (0..6).firstOrNull { dayCount ->
            val day = (todayIndex + dayCount) % 7
            day.isSet()
        } ?: -1
    }

    override fun toString(): String {
        return (if (0.isSet()) "m" else "_") +
                (if (1.isSet()) 't' else '_') +
                (if (2.isSet()) 'w' else '_') +
                (if (3.isSet()) 't' else '_') +
                (if (4.isSet()) 'f' else '_') +
                (if (5.isSet()) 's' else '_') +
                if (6.isSet()) 's' else '_'
    }

    companion object {
        private val DAY_MAP = intArrayOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    }
}