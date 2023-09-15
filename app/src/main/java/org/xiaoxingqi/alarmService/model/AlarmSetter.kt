package org.xiaoxingqi.alarmService.model

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.xiaoxingqi.shengxi.modules.login.SplashActivity
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import java.util.*

/**
 * Created by Yuriy on 24.06.2017.
 */

interface AlarmSetter {

    fun removeRTCAlarm()

    fun setUpRTCAlarm(id: Int, typeName: String, calendar: Calendar)

    fun fireNow(id: Int, typeName: String)

    class AlarmSetterImpl(private val am: AlarmManager, private val mContext: Context) : AlarmSetter {
        private val setAlarmStrategy: ISetAlarmStrategy

        init {
            this.setAlarmStrategy = initSetStrategyForVersion()
        }

        override fun removeRTCAlarm() {
            val pendingAlarm = PendingIntent.getBroadcast(
                    mContext,
                    pendingAlarmRequestCode,
                    Intent(ACTION_FIRED).apply {
                        // must be here, otherwise replace does not work
                        setClass(mContext, AlarmsReceiver::class.java)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            am.cancel(pendingAlarm)
        }

        override fun setUpRTCAlarm(id: Int, typeName: String, calendar: Calendar) {
            LocalLogUtils.writeLog("用户设置了闹钟", System.currentTimeMillis())
            val pendingAlarm = Intent(ACTION_FIRED)
                    .apply {
                        setClass(mContext, AlarmsReceiver::class.java)
                        putExtra(EXTRA_ID, id)
                        putExtra(EXTRA_TYPE, typeName)
                    }
                    .let { PendingIntent.getBroadcast(mContext, pendingAlarmRequestCode, it, PendingIntent.FLAG_UPDATE_CURRENT) }
            setAlarmStrategy.setRTCAlarm(calendar, pendingAlarm)
        }

        override fun fireNow(id: Int, typeName: String) {
            val intent = Intent(ACTION_FIRED).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TYPE, typeName)
            }
            mContext.sendBroadcast(intent)
        }

        private fun initSetStrategyForVersion(): ISetAlarmStrategy {
            return when {
                Build.VERSION.SDK_INT >= 26 -> OreoSetter()
                Build.VERSION.SDK_INT >= 23 -> MarshmallowSetter()
                Build.VERSION.SDK_INT >= 19 -> KitKatSetter()
                else -> IceCreamSetter()
            }
        }

        private inner class IceCreamSetter : ISetAlarmStrategy {
            override fun setRTCAlarm(calendar: Calendar, pendingIntent: PendingIntent) {
                am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private inner class KitKatSetter : ISetAlarmStrategy {
            override fun setRTCAlarm(calendar: Calendar, pendingIntent: PendingIntent) {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }

        @TargetApi(23)
        private inner class MarshmallowSetter : ISetAlarmStrategy {
            override fun setRTCAlarm(calendar: Calendar, pendingIntent: PendingIntent) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }

        /** 8.0  */
        @TargetApi(Build.VERSION_CODES.O)
        private inner class OreoSetter : ISetAlarmStrategy {
            override fun setRTCAlarm(calendar: Calendar, pendingAlarm: PendingIntent) {
                val pendingShowList = PendingIntent.getActivity(
                        mContext,
                        100500,
                        //TODO 设置当前Activity
                        Intent(mContext, SplashActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
                am.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingShowList), pendingAlarm)
            }
        }

        private interface ISetAlarmStrategy {
            fun setRTCAlarm(calendar: Calendar, pendingIntent: PendingIntent)
        }

        companion object {
            private val pendingAlarmRequestCode = 0
        }
    }

    companion object {
        const val ACTION_FIRED = AlarmsScheduler.ACTION_FIRED
        const val EXTRA_ID = AlarmsScheduler.EXTRA_ID
        const val EXTRA_TYPE = AlarmsScheduler.EXTRA_TYPE
    }
}
