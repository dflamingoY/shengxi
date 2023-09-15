/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 * Copyright (C) 2019 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xiaoxingqi.alarmService.alert

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.preference.PreferenceManager
import android.text.format.DateFormat
import org.xiaoxingqi.alarmService.NotificationImportance
import org.xiaoxingqi.alarmService.background.Event
import org.xiaoxingqi.alarmService.interfaces.Alarm
import org.xiaoxingqi.alarmService.interfaces.Intents
import org.xiaoxingqi.alarmService.interfaces.PresentationToModelIntents
import org.xiaoxingqi.alarmService.notificationBuilder
import org.xiaoxingqi.shengxi.BuildConfig
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App.Companion.container
import java.util.*

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert activity. Passes
 * through Alarm ID.
 */
class BackgroundNotifications {
    private var mContext = container().context()
    private val nm = container().notificationManager()
    private val alarmsManager = container().alarms()
    private val prefs = container().prefs()

    init {
        val subscribe = container().store.events.subscribe { event ->
            when (event) {
                is Event.AlarmEvent -> nm.cancel(event.id + BACKGROUND_NOTIFICATION_OFFSET)
                is Event.PrealarmEvent -> nm.cancel(event.id + BACKGROUND_NOTIFICATION_OFFSET)
                is Event.DismissEvent -> nm.cancel(event.id + BACKGROUND_NOTIFICATION_OFFSET)
                is Event.CancelSnoozedEvent -> nm.cancel(event.id + BACKGROUND_NOTIFICATION_OFFSET)
                is Event.SnoozedEvent -> onSnoozed(event.id)
                is Event.Autosilenced -> onSoundExpired(event.id)
            }
        }
    }

    private fun onSnoozed(id: Int) {
        // When button Reschedule is clicked, the TransparentActivity with
        // TimePickerFragment to set new alarm time is launched
        val pendingReschedule = Intent().apply {
            //            setClass(mContext, TransparentActivity::class.java)
            putExtra(Intents.EXTRA_ID, id)
        }.let {
            PendingIntent.getActivity(mContext, id, it, 0)
        }

        val pendingDismiss = PresentationToModelIntents.createPendingIntent(mContext,
                PresentationToModelIntents.ACTION_REQUEST_DISMISS, id)

        val label = alarmsManager.getAlarm(id)?.labelOrDefault ?: ""

        //@formatter:off
        val contentText: String = alarmsManager.getAlarm(id)
                ?.let { mContext.getString(R.string.alarm_notify_snooze_text, it.formatTimeString()) }
                ?: ""

        val status = mContext.notificationBuilder(CHANNEL_ID, NotificationImportance.NORMAL) {
            // Get the display time for the snooze and update the notification.
            setContentTitle(getString(R.string.alarm_notify_snooze_label, label))
            setContentText(contentText)
            setSmallIcon(R.drawable.stat_notify_alarm)
            setContentIntent(pendingDismiss)
            setOngoing(true)
//            addAction(R.drawable.ic_action_reschedule_snooze, getString(R.string.alarm_alert_reschedule_text), pendingReschedule)
            addAction(R.drawable.ic_action_dismiss, getString(R.string.alarm_alert_dismiss_text), pendingDismiss)
            setDefaults(Notification.DEFAULT_LIGHTS)
        }
        //@formatter:on

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        nm.notify(id + BACKGROUND_NOTIFICATION_OFFSET, status)
    }

    private fun getString(id: Int, vararg args: String) = mContext.getString(id, *args)
    private fun getString(id: Int) = mContext.getString(id)

    private fun Alarm.formatTimeString(): String {
        val format = if (prefs.is24HoutFormat().blockingGet()) DM24 else DM12
        val calendar = snoozedTime
        return DateFormat.format(format, calendar) as String
    }

    private fun onSoundExpired(id: Int) {
        val pendingDismiss = PresentationToModelIntents.createPendingIntent(mContext,
                PresentationToModelIntents.ACTION_REQUEST_DISMISS, id)
        // Update the notification to indicate that the alert has been
        // silenced.
        val alarm = alarmsManager.getAlarm(id)
        val label: String = alarm?.labelOrDefault ?: ""
        val autoSilenceMinutes = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                "auto_silence", "10"))
        val text = mContext.getString(R.string.alarm_alert_alert_silenced, autoSilenceMinutes)

        val notification = mContext.notificationBuilder(CHANNEL_ID, NotificationImportance.NORMAL) {
            setAutoCancel(true)
            setSmallIcon(R.drawable.stat_notify_alarm)
            setWhen(Calendar.getInstance().timeInMillis)
            setContentIntent(pendingDismiss)
            setContentTitle(label)
            setContentText(text)
            setTicker(text)
        }

        nm.notify(BACKGROUND_NOTIFICATION_OFFSET + id, notification)
    }

    companion object {
        private const val DM12 = "E h:mm aa"
        private const val DM24 = "E kk:mm"
        private const val BACKGROUND_NOTIFICATION_OFFSET = 1000
        private const val CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.BackgroundNotifications"
    }
}
