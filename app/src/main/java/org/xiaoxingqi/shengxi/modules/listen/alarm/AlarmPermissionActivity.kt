package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm_permission.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.utils.LocalLogUtils

class AlarmPermissionActivity : BaseAct() {
    private lateinit var manufacturer: String

    private var pendIntent: Intent? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_permission
    }

    override fun initView() {

    }

    override fun initData() {
        manufacturer = Build.MANUFACTURER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !manufacturer.equals("smartisan", true)) {//坚果手机无电池优化项
            setElectric.visibility = View.VISIBLE
        }
        //VIVO手机 后台显示,可以在I管理中设置 ,或者高版本可以在应用安装详情中设置
        pendIntent = autoIntent(manufacturer)
        when {//自启动
            manufacturer.equals("xiaomi", true) -> {
                if (packageManager.resolveActivity(pendIntent, 0) == null) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("huawei", true) -> {
                if (packageManager.resolveActivity(pendIntent, 0) == null) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("samsung", true) -> {
                if (packageManager.resolveActivity(pendIntent, 0) == null) {
                    pendIntent = Intent()
                    pendIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    pendIntent?.component = ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity")
                    if (packageManager.resolveActivity(pendIntent, 0) == null) {
                        setAutoOn.visibility = View.GONE
                    }
                }
            }
            manufacturer.equals("Meizu", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity")
                    if (pendIntent?.checkHas()!!) {
                        setAutoOn.visibility = View.GONE
                    }
                }
            }
            manufacturer.equals("oppo", true) -> {
                if (pendIntent.checkHas()) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("vivo", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager")
                    if (pendIntent.checkHas()) {
                        pendIntent?.component = ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.PurviewTabActivity")
                        if (pendIntent.checkHas()) {
                            setAutoOn.visibility = View.GONE
                        }
                    }
                }
            }
            manufacturer.equals("Letv", true) -> {
                if (pendIntent.checkHas()) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("YuLong", true) -> {
                if (pendIntent.checkHas()) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("ZTE", true) -> {
                if (pendIntent.checkHas()) {
                    setAutoOn.visibility = View.GONE
                }
            }
            manufacturer.equals("GIONEE", true) -> {
                if (pendIntent.checkHas())
                    setAutoOn.visibility = View.GONE
            }
            manufacturer.equals("smartisan", true) -> {
                if (pendIntent.checkHas())
                    setAutoOn.visibility = View.GONE

            }
            manufacturer.equals("360", true) -> {
                if (pendIntent.checkHas())
                    setAutoOn.visibility = View.GONE
            }
        }
        pendIntent = whiteList(manufacturer)
        setWhiteList.visibility = View.GONE
        when {
            manufacturer.equals("xiaomi", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("HUAWEI", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("Meizu", true) -> {
                if (!pendIntent.checkHas()) {
                    setWhiteList.visibility = View.VISIBLE
                } else {
                    pendIntent?.component = ComponentName.unflattenFromString("com.meizu.safe/.powerui.AppPowerManagerActivity")
                    if (!pendIntent.checkHas()) {
                        setWhiteList.visibility = View.VISIBLE
                    }
                }
            }
            manufacturer.equals("OPPO", true) -> {
                if (!pendIntent.checkHas()) {
                    setWhiteList.visibility = View.VISIBLE
                } else {
                    pendIntent?.component = ComponentName.unflattenFromString("com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
                    if (!pendIntent.checkHas()) {
                        setWhiteList.visibility = View.VISIBLE
                    }
                }
            }
            manufacturer.equals("vivo", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                } else {
                    pendIntent?.component = ComponentName.unflattenFromString("com.vivo.abeui/.highpower.ExcessivePowerManagerActivity")
                    if (pendIntent.checkHas()) {
                        setWhiteList.visibility = View.VISIBLE
                    }
                }
            }
            manufacturer.equals("Letv", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("LENOVO", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("ZTE", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("smartisan", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("360", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("oneplus", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
            manufacturer.equals("nokia", true) -> {
                if (!pendIntent?.checkHas()!!) {
                    setWhiteList.visibility = View.VISIBLE
                }
            }
        }

        if (manufacturer.equals("xiaomi", true) ||
                manufacturer.equals("Meizu", true) ||
                manufacturer.equals("vivo", true) ||
                manufacturer.equals("smartisan", true)
        ) {
            setBackground.visibility = View.VISIBLE
        }

    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        setAutoOn.setOnClickListener {
            openAuto()
        }
        setWhiteList.setOnClickListener {
            whiteList()
        }
        setElectric.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)) {
                    if (Build.MANUFACTURER.equals("HUAWEI", true)) {
                        try {
                            val paramView = Intent()
                            paramView.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            paramView.component = ComponentName("com.android.settings", "com.android.settings.Settings\$HighPowerApplicationsActivity")
                            startActivity(paramView)
                            return@setOnClickListener
                        } catch (paramView: Exception) {
                            paramView.printStackTrace()
                            return@setOnClickListener
                        }
                    }
                    try {
                        val paramView = Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")
                        val localStringBuilder = StringBuilder()
                        localStringBuilder.append("package:")
                        localStringBuilder.append(packageName)
                        paramView.data = Uri.parse(localStringBuilder.toString())
                        startActivity(paramView)
                        return@setOnClickListener
                    } catch (paramView: Exception) {
                        paramView.printStackTrace()
                        return@setOnClickListener
                    }
                }
                showToast("已经加入电池优化")
            }
        }
        setBackground.setOnClickListener {
            showBackground()
        }
    }

    /**
     * 是否存在跳转此界面
     */
    private fun Intent?.checkHas(): Boolean {
        return packageManager.resolveActivity(this, 0) == null
    }

    private fun check(paramIntent: Intent): Boolean {
        return packageManager.queryIntentActivities(paramIntent, PackageManager.MATCH_DEFAULT_ONLY).size > 0
    }


    /**
     * 自启动的Activity
     */
    private fun autoIntent(key: String): Intent {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        when {
            key.equals("xiaomi", true) -> intent.action = "miui.intent.action.OP_AUTO_START"
            key.equals("huawei", true) -> {
                intent.action = "huawei.intent.action.HSM_BOOTAPP_MANAGER"
                intent.addCategory("android.intent.category.DEFAULT")
            }
            key.equals("samsung", true) -> intent.component = ComponentName.unflattenFromString("com.samsung.memorymanager/.FragmentTabsActivity")
            key.equals("Meizu", true) -> intent.component = ComponentName.unflattenFromString("com.meizu.safe/.security.HomeActivity")
            key.equals("oppo", true) -> intent.component = ComponentName.unflattenFromString("com.coloros.safecenter/.startupapp.StartupAppListActivity")
            key.equals("vivo", true) -> intent.component = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity")
            key.equals("Letv", true) -> intent.action = "com.letv.android.permissionautoboot"
            key.equals("YuLong", true) -> intent.component = ComponentName("com.yulong.android.security", "com.yulong.android.seccenter.tabbarmain")
            key.equals("ZTE", true) -> intent.component = ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager")
            key.equals("GIONEE", true) -> intent.component = ComponentName("com.gionee.softmanager", "com.gionee.softmanager.MainActivity")
            key.equals("smartisan", true) -> intent.component = ComponentName.unflattenFromString("com.smartisanos.security/.PermissionsActivity")
            key.equals("360", true) -> intent.component = ComponentName.unflattenFromString("com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity")
        }
        return intent
    }

    //解析白名单的Intent
    private fun whiteList(key: String): Intent {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        when {
            key.equals("xiaomi", true) -> intent.component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity")
            key.equals("HUAWEI", true) -> intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            key.equals("Meizu", true) -> intent.component = ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity")
            key.equals("OPPO", true) -> intent.component = ComponentName.unflattenFromString("com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
            key.equals("vivo", true) -> intent.component = ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")
            key.equals("Letv", true) -> intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity")
            key.equals("LENOVO", true) -> intent.component = ComponentName("com.lenovo.powersetting", "com.lenovo.powersetting.ui.Settings\$HighPowerApplicationsActivity")
            key.equals("ZTE", true) -> intent.component = ComponentName("com.zte.heartyservice", "com.zte.heartyservice.setting.ClearAppSettingsActivity")
            key.equals("smartisan", true) -> intent.component = ComponentName.unflattenFromString("com.android.settings/.fuelgauge.appBatteryUseOptimization.AppBatteryUseOptimizationActivity")
            key.equals("360", true) -> intent.component = ComponentName.unflattenFromString("com.qiku.powerengine/.savepower.activity.MemoryDozeSettingActivity")
            key.equals("oneplus", true) -> intent.component = ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            key.equals("nokia", true) -> intent.component = ComponentName("com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity")
        }
        return intent
    }

    /*
     * 手机自启动
     */
    private fun openAuto() {
        pendIntent = autoIntent(manufacturer)
        when {
            manufacturer.equals("xiaomi", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:小米开启自启动界面错误 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:小米开启自启动界面配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("huawei", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:华为开启自启动界面错误 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:华为开启自启动界面配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("samsung", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent = Intent()
                    pendIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    pendIntent?.component = ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity")
                    if (!pendIntent.checkHas()) {
                        try {
                            startActivity(pendIntent)
                        } catch (e: Exception) {
                            LocalLogUtils.writeLog("AlarmPermission:三星备用方案自启动崩溃 ${e.message}", System.currentTimeMillis())
                        }
                    }
                } else {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:三星自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("Meizu", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity")
                    if (!pendIntent.checkHas()) {
                        try {
                            startActivity(pendIntent)
                        } catch (e: Exception) {
                            LocalLogUtils.writeLog("AlarmPermission:魅族备用方案自启动崩溃 ${e.message}", System.currentTimeMillis())
                            startDefaultPackageInfo()
                        }
                    } else {
                        LocalLogUtils.writeLog("AlarmPermission:无法打开魅族自启动设置", System.currentTimeMillis())
                    }
                } else {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:魅族自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("oppo", true) -> {//7.0  8.0 崩溃
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:OPPO自启动崩溃 ${e.message}", System.currentTimeMillis())
                        startDefaultPackageInfo()
                    }
                } else {
                    LocalLogUtils.writeLog("AlarmPermission:OPPO 自启动配置错误", System.currentTimeMillis())
                    startDefaultPackageInfo()
                }
            }
            manufacturer.equals("vivo", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager")
                    if (pendIntent.checkHas()) {
                        pendIntent?.component = ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.PurviewTabActivity")
                    }
                }
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                    try {
                        LocalLogUtils.writeLog("AlarmPermission:VIVO 自启动崩溃 ${e.message}", System.currentTimeMillis())
                        pendIntent = packageManager.getLaunchIntentForPackage("com.iqoo.secure")
                        pendIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                    }
                }
            }
            manufacturer.equals("Letv", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:乐视自启动崩溃 ${e.message}", System.currentTimeMillis())
                        startDefaultPackageInfo()
                    }
                } else {
                    LocalLogUtils.writeLog("AlarmPermission:乐视自启动配置异常", System.currentTimeMillis())
                }
            }
            manufacturer.equals("YuLong", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:酷派自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:酷派自启动配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("ZTE", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        startDefaultPackageInfo()
                        LocalLogUtils.writeLog("AlarmPermission:中兴自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:中兴自启动配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("GIONEE", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        startDefaultPackageInfo()
                        LocalLogUtils.writeLog("AlarmPermission:金立自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:金立自启动配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("smartisan", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        startDefaultPackageInfo()
                        LocalLogUtils.writeLog("AlarmPermission:坚果自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:坚果自启动配置异常", System.currentTimeMillis())
            }
            manufacturer.equals("360", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        startDefaultPackageInfo()
                        LocalLogUtils.writeLog("AlarmPermission:360自启动崩溃 ${e.message}", System.currentTimeMillis())
                    }
                } else LocalLogUtils.writeLog("AlarmPermission:360自启动配置异常", System.currentTimeMillis())
            }
            else -> {
                //跳到系统设置
                startDefaultPackageInfo()
            }
        }
    }

    private fun whiteList() {
        pendIntent = whiteList(manufacturer)
        when {
            manufacturer.equals("HUAWEI", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:华为白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("xiaomi", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:小米白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("Meizu", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.meizu.safe/.powerui.AppPowerManagerActivity")
                }
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                    LocalLogUtils.writeLog("AlarmPermission:Meizu白名单 ${e.message}", System.currentTimeMillis())
                }
            }
            manufacturer.equals("OPPO", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
                }
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                    LocalLogUtils.writeLog("AlarmPermission:OPPO白名单 ${e.message}", System.currentTimeMillis())
                }
            }
            manufacturer.equals("vivo", true) -> {
                if (pendIntent.checkHas()) {
                    pendIntent?.component = ComponentName.unflattenFromString("com.vivo.abeui/.highpower.ExcessivePowerManagerActivity")
                }
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                    pendIntent?.component = ComponentName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:VIVO白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("Letv", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:LETV白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("LENOVO", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:Lenovo白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("ZTE", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:ZTE白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("smartisan", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:smartiasn 白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("360", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:360白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("oneplus", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:oneplus白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
            manufacturer.equals("nokia", true) -> {
                if (!pendIntent.checkHas()) {
                    try {
                        startActivity(pendIntent)
                    } catch (e: Exception) {
                        LocalLogUtils.writeLog("AlarmPermission:nokia白名单 ${e.message}", System.currentTimeMillis())
                    }
                }
            }
        }
    }

    private fun showBackground() {
        when {
            manufacturer.equals("xiaomi", true) -> {
                startMiPermission()
            }
            manufacturer.equals("Meizu", true) -> {
                pendIntent = Intent("com.meizu.safe.security.SHOW_APPSEC")
                pendIntent?.addCategory("android.intent.category.DEFAULT")
                pendIntent?.putExtra("packageName", packageName)
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                }

            }
            manufacturer.equals("vivo", true) -> {
                pendIntent = packageManager.getLaunchIntentForPackage("com.iqoo.secure")
                pendIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (pendIntent.checkHas()) {
                    startDefaultPackageInfo()
                }
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            manufacturer.equals("smartisan", true) -> {
                pendIntent = autoIntent("smartisan")
                try {
                    startActivity(pendIntent)
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun startMiPermission() {
        val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
        localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
        localIntent.putExtra("extra_pkgname", packageName)
        localIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (check(localIntent)) {
            startActivity(localIntent)
        }
    }

    private fun startDefaultPackageInfo() {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        localIntent.data = Uri.fromParts("package", this.packageName, null)
        startActivity(localIntent)
    }

}