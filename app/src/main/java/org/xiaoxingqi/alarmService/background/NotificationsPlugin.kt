/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
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

package org.xiaoxingqi.alarmService.background

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.xiaoxingqi.alarmService.NotificationImportance
import org.xiaoxingqi.alarmService.alert.AlarmAlertFullScreen
import org.xiaoxingqi.alarmService.notificationBuilder
import org.xiaoxingqi.alarmService.oreo
import org.xiaoxingqi.alarmService.preOreo
import org.xiaoxingqi.alarmService.configuration.Prefs
import org.xiaoxingqi.alarmService.interfaces.Intents
import org.xiaoxingqi.alarmService.interfaces.PresentationToModelIntents
import org.xiaoxingqi.shengxi.BuildConfig
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App.Companion.container

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert activity. Passes
 * through Alarm ID.
 */
class NotificationsPlugin(
        val mContext: Context,
        val nm: NotificationManager = container().notificationManager(),
        val startForeground: (Int, Notification) -> Unit,
        val prefs: Prefs = container().prefs()
) : AlertPlugin {
    override fun go(alarm: PluginAlarmData, prealarm: Boolean, targetVolume: Observable<TargetVolume>): Disposable {
        // our alarm fired again, remove snooze notification
        nm.cancel(alarm.id)

        /* Close dialogs and window shade */
        mContext.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

        // Trigger a notification that, when clicked, will show the alarm
        // alert dialog. No need to check for fullscreen since this will always
        // be launched from a user action.

        val notify = Intent(mContext, AlarmAlertFullScreen::class.java)

        notify.putExtra(Intents.EXTRA_ID, alarm.id)
        val pendingNotify = PendingIntent.getActivity(mContext, alarm.id, notify, 0)
        val pendingSnooze = PresentationToModelIntents.createPendingIntent(mContext,
                PresentationToModelIntents.ACTION_REQUEST_SNOOZE, alarm.id)
        val pendingDismiss = PresentationToModelIntents.createPendingIntent(mContext,
                PresentationToModelIntents.ACTION_REQUEST_DISMISS, alarm.id)

        val notification = mContext.notificationBuilder(CHANNEL_ID, NotificationImportance.HIGH) {
            setContentTitle(alarm.label)
            setContentText("暂停或关闭闹钟")
            setSmallIcon(R.drawable.stat_notify_alarm)
            // setFullScreenIntent to show the user AlarmAlert dialog at the same time
            // when the Notification Bar was created.
            setFullScreenIntent(pendingNotify, true)
            // setContentIntent to show the user AlarmAlert dialog
            // when he will click on the Notification Bar.
            setContentIntent(pendingNotify)
            setOngoing(true)
            //如果没有暂停再响, 则不添加此tab
            val isDuration = try {
                PreferenceManager.getDefaultSharedPreferences(container().context).getString("snooze_duration", "-1")
            } catch (e: Exception) {
                "-1"
            }
            if (isDuration != "-1")
                addAction(0, "暂停再响", pendingSnooze)
            addAction(0, "取消", pendingDismiss)
            setDefaults(Notification.DEFAULT_LIGHTS)
        }
        // Send the notification using the alarm id to easily identify the
        // correct notification.
        oreo {
            startForeground(alarm.id, notification)
        }
        preOreo {
            nm.notify(alarm.id, notification)
        }
        return Disposables.fromAction {
            oreo {
                // wrapper.stopForeground(true)
            }
            preOreo {
                nm.cancel(alarm.id)
            }
        }
    }

    private fun getString(id: Int) = mContext.getString(id)

    companion object {
        private const val CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.NotificationsPlugin"
    }
}
