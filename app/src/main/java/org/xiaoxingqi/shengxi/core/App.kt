package org.xiaoxingqi.shengxi.core

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Process
import android.preference.PreferenceManager
import android.support.multidex.MultiDex
import android.text.TextUtils
import android.util.Log
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import com.bun.miitmdid.core.JLibrary
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.huawei.android.hms.agent.HMSAgent
import com.mob.MobSDK
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.util.NIMUtil
import com.netease.nrtc.monitor.b.Q
import com.tencent.bugly.crashreport.CrashReport
import com.xiaomi.mipush.sdk.MiPushClient
import io.reactivex.Maybe
import io.reactivex.MaybeOnSubscribe
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.xiaoxingqi.alarmService.alert.BackgroundNotifications
import org.xiaoxingqi.alarmService.background.AlertServicePusher
import org.xiaoxingqi.alarmService.background.Event
import org.xiaoxingqi.alarmService.configuration.Container
import org.xiaoxingqi.alarmService.configuration.Prefs
import org.xiaoxingqi.alarmService.configuration.Store
import org.xiaoxingqi.alarmService.model.*
import org.xiaoxingqi.alarmService.oreo
import org.xiaoxingqi.alarmService.persistance.DatabaseQuery
import org.xiaoxingqi.alarmService.persistance.PersistingContainerFactory
import org.xiaoxingqi.alarmService.preOreo
import org.xiaoxingqi.alarmService.utils.Optional
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.model.SignCacheData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.service.SocketServer
import org.xiaoxingqi.shengxi.service.SocketThread
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.yunxin.DemoCache
import org.xiaoxingqi.yunxin.NIMInitManager
import org.xiaoxingqi.yunxin.NimSDKOptionConfig
import org.xiaoxingqi.yunxin.UserPreferences
import java.util.*

class App : Application() {

    companion object {
        private lateinit var sContainer: Container
        lateinit var oaid: String
        lateinit var context: Context
        var uid: String? = null

        @JvmStatic
        fun container(): Container {
            return sContainer
        }
    }

    override fun onCreate() {
        super.onCreate()
        SocketThread.instances(applicationContext)
        context = this.applicationContext
        OkClientHelper.init()
        MobSDK.init(this, "24db510d970e8", "b44d9a0cebdd724f86047123c87789b0")
        when {
            OsUtil.isMIUI() -> MiPushClient.registerPush(this, "2882303761517764309", "5661776422309")
            OsUtil.isHw() -> HMSAgent.init(this)
            else -> {
                JPushInterface.init(this)
                JPushInterface.getRegistrationID(this)
                val builder = BasicPushNotificationBuilder(this)
                builder.statusBarDrawable = R.mipmap.ic_launcher
                builder.notificationFlags = Notification.FLAG_AUTO_CANCEL   //设置为自动消失和呼吸灯闪烁
                builder.notificationDefaults = Notification.DEFAULT_LIGHTS
                JPushInterface.setDefaultPushNotificationBuilder(builder)
            }
        }
        CrashReport.initCrashReport(applicationContext, "dcdaf1e7d5", false)
        AppTools.initDisplayImageOptions()
        AppTools.initImageLoader(applicationContext)
        TimeUtils.initTimeUtils(applicationContext)
        DemoCache.setContext(this)
        NIMClient.init(this, null, NimSDKOptionConfig.getSDKOptions(this.applicationContext))
        if (NIMUtil.isMainProcess(this)) {
            NIMClient.toggleNotification(false)
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true)
            try {
                val config = UserPreferences.getStatusConfig()
                config.vibrate = false
                config.ring = false
                UserPreferences.setStatusConfig(config)
                NIMClient.updateStatusBarNotificationConfig(config)
            } catch (e: Exception) {
            }
        }
        val cacheData = PreferenceTools.getObj(this, IConstant.SIGNCACHE, SignCacheData::class.java)
        if (cacheData != null) {
            if (cacheData.timeMap != null) {
                GlideJudeUtils.timeMap.putAll(cacheData.timeMap)
            }
        }
        changeLanguage()
        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build())
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        am.runningAppProcesses?.let { arr ->
//            for (info in arr) {
//                if (info.pid == Process.myPid()) {//当前进程
//                    if (packageName == info.processName) {
//                        pushService()
//                        break
//                    }
//                }
//            }
//        }
//        LeakCanary.install(this)
    }

    private fun changeLanguage() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
        if (!TextUtils.isEmpty(sta)) {
            val myLocale: Locale?
            myLocale = Locale("zh", sta.toLowerCase())
            val res = resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.locale = myLocale
            res.updateConfiguration(conf, dm)
        } else {//设置默认的语言到系统
            val res = resources
            val config = res.configuration
            val locale = config.locale.country
            /**
             * 保存默认的国家  如果不是中日美 默认美国
             */
            if (IConstant.CN != locale) {
                SPUtils.setString(this, IConstant.DEFAULTLANGUAGE, IConstant.CN)
            } else {
                SPUtils.setString(this, IConstant.DEFAULTLANGUAGE, locale.toUpperCase())
            }
        }
        syncLoadTypeface()
    }

    private var rxPreferences: RxSharedPreferences? = null

    private var is24hoursFormatOverride = Optional.absent<Boolean>()

    /**
     * 异步加载字体库,避免字体库重复加载耗费时间
     */
    private fun syncLoadTypeface() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        rxPreferences = RxSharedPreferences.create(preferences)
        val dateFormat = Maybe
                .create(MaybeOnSubscribe<Boolean> { e ->
                    if (is24hoursFormatOverride.isPresent()) {
                        e.onSuccess(is24hoursFormatOverride.get())
                    } else {
                        e.onComplete()
                    }
                })
                .switchIfEmpty(Maybe.create { e -> e.onSuccess(android.text.format.DateFormat.is24HourFormat(applicationContext)) })
                .toSingle()
        val parseInt = Function<String, Int> { s -> Integer.parseInt(s) }
        val prefs = Prefs(dateFormat,
                rxPreferences!!.getString("prealarm_duration", "-1").asObservable().map(parseInt),
                rxPreferences!!.getString("snooze_duration", "5").asObservable().map(parseInt),
                rxPreferences!!.getString(Prefs.LIST_ROW_LAYOUT, Prefs.LIST_ROW_LAYOUT_COMPACT).asObservable(),
                rxPreferences!!.getString("auto_silence", "5").asObservable().map(parseInt))
        val store = Store(
                // alarmsSubject
                BehaviorSubject.createDefault<List<AlarmValue>>(ArrayList()),
                // next
                BehaviorSubject.createDefault<Optional<Store.Next>>(Optional.absent()),
                // sets
                PublishSubject.create<Store.AlarmSet>(),
                PublishSubject.create<Event>())

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val setter = AlarmSetter.AlarmSetterImpl(alarmManager, applicationContext)
        val calendars = Calendars { Calendar.getInstance() }
        val alarmsScheduler = AlarmsScheduler(setter, store, prefs, calendars)
        val broadcaster = AlarmStateNotifier(store)
        val handlerFactory = ImmediateHandlerFactory()
        val containerFactory = PersistingContainerFactory(calendars, applicationContext)
        val alarms = Alarms(alarmsScheduler, DatabaseQuery(contentResolver, containerFactory), AlarmCoreFactory(
                alarmsScheduler,
                broadcaster,
                handlerFactory,
                prefs,
                store,
                calendars),
                containerFactory)

        sContainer = Container(
                applicationContext,
                preferences,
                rxPreferences!!,
                prefs,
                store,
                alarms)
        /**
         * 启动后台服务
         */
        try {
            AlertServicePusher(store, applicationContext)
            BackgroundNotifications()
            alarms.start()
            alarmsScheduler.start()
        } catch (e: Exception) {
        }
    }

    @Synchronized
    private fun pushService() {
        try {
            oreo {
                applicationContext.startForegroundService(Intent(this, SocketServer::class.java))
            }
            preOreo {
                applicationContext.startService(Intent(this, SocketServer::class.java))
            }
        } catch (e: Exception) {
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        //分包
        MultiDex.install(this)
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)//10的设备加载此包
//            JLibrary.InitEntry(base)
    }

}