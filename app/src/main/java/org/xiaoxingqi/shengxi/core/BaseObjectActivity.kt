package org.xiaoxingqi.shengxi.core

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.EarModeChange
import org.xiaoxingqi.shengxi.impl.OnNewHintViewEvent
import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.PopNewHintView
import org.xiaoxingqi.shengxi.wedgit.skinView.cardView.SkinCardViewInflater
import skin.support.SkinCompatManager
import skin.support.app.SkinCompatActivity
import skin.support.design.SkinMaterialManager
import skin.support.design.app.SkinMaterialViewInflater
import java.util.*

/**
 * @外放模式
 */
const val MODE_SPEAKER = 0

/**
 * @耳机模式
 */
const val MODE_HEADSET = 1

/**
 * @听筒模式
 */
const val MODE_EARPIECE = 2

open class BaseObjectActivity : SkinCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    protected var mSensor: Sensor? = null
    protected var isPlayed = false//此界面是否播放音频
    protected var isVisibleActivity = true//当前Activity 是否可见
    private var isPauseOnResume = false//界面从暂停到恢复

    companion object {
        var currentMode = MODE_SPEAKER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (SkinMaterialManager.getInstance() == null) {
            SkinMaterialManager.init(this)
            SkinCompatManager.init(this)                         // 基础控件换肤初始化
                    .addInflater(SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                    .addInflater(SkinCardViewInflater())
                    .loadSkin()
        }
        super.onCreate(savedInstanceState)
        LocalLogUtils.writeLog("onCreate: ${this.localClassName}", System.currentTimeMillis())
        initSensor()
        EventBus.getDefault().register(this)
        changeLanguage()
    }

    /**
     * 不能重载 单一
     */
    private fun initSensor() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    override fun onResume() {
        if (null != mSensor) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        super.onResume()
        isVisibleActivity = true
        volumeControlStream = if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) {//固定听筒模式
            AudioManager.STREAM_VOICE_CALL
        } else {
            AudioManager.STREAM_MUSIC
        }
    }

    override fun onPause() {
        mSensorManager.unregisterListener(this)
        super.onPause()
        isVisibleActivity = false
        isPauseOnResume = true
    }

    override fun onStop() {
        super.onStop()
        if (!ActivityLifecycleHelper.isAppPause) {
            sendObserver()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        /**
         *如果是耳机模式 直接return
         */
        try {
            if (writeHeadSet()) {
                currentMode = MODE_HEADSET
                return
            }
            if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) {//固定听筒模式
                currentMode = MODE_EARPIECE
                return
            }
            if (!isPauseOnResume) {
                if (null != mSensor) {
                    val range = event?.values!![0]
                    if (range >= mSensor!!.maximumRange && currentMode != MODE_SPEAKER) {//公放
                        currentMode = MODE_SPEAKER
                        changSpeakModel(1)
                        EventBus.getDefault().post(EarModeChange(1))
                    } else if (currentMode != MODE_EARPIECE && range < mSensor!!.maximumRange) {//听筒
                        currentMode = MODE_EARPIECE
                        changSpeakModel(2)
                        EventBus.getDefault().post(EarModeChange(2))
                    }
                    /**
                     * 如果其他界面还在播放, 需要发送切换焦点的广播
                     */
                }
            }
        } catch (e: Exception) {
        }
        isPauseOnResume = false
    }


    /**
     * 耳机或者蓝牙模式  有播放语音的子类必须复写该方法
     */
    open fun writeHeadSet(): Boolean {
        return false
    }

    /**
     * 自动切换播放的模式  1 公放  2 听筒
     */
    open fun changSpeakModel(type: Int) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changeType(event: EarModeChange) {
        if (!isVisibleActivity && currentMode != MODE_HEADSET)
            changSpeakModel(event.type)
    }

    protected fun sendObserver() {
        EventBus.getDefault().post(object : StopPlayInterFace {})
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(str: String) {
        when (str) {
            IConstant.REFRESHLANGUAGE -> {
                changeLanguage()
                recreate()//刷新界面
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewHintEvent(event: OnNewHintViewEvent) {
        try {
            if (isVisibleActivity) {
                if (event.data.type == 11) {
                    DialogSystemWarning(this).setShowContent(event.data.title).show()
                } else if (event.data.type == 112) {
                    if (dialog != null && dialog?.isShowing!!) {
                        dialog?.dismiss()
                    } else {
                        dialog = DialogOffline(this)
                    }
                    dialog?.show()
                } else if (event.data.type == 113) {
                    dialog?.let {
                        if (it.isShowing) {
                            it.dismiss()
                        }
                    }
                } else if (event.data.type == 114) {//初次发布声兮
                    //                checkShare(event.data.title)
                } else if (event.data.type == 115) {//刷新token的Dialog
                    loadingDialog = DialogProgress(this)
                    loadingDialog!!.show()
                } else if (event.data.type == 116) {//隐藏刷新token的Dialog
                    loadingDialog?.let {
                        it.dismiss()
                        loadingDialog = null
                    }
                    OkClientHelper.setCancel(false)
                } else if (event.data.type == 12) {

                } else if (event.data.type == 13) {//关禁闭
                    DialogLimitUser(this).show()
                } else {
                    if (SPUtils.getInt(this, IConstant.HAS_CHECKED_CODE, 0) == 1) {
                        return
                    }
                    if (this is DialogAddAlbumActivity) {
                        return
                    }
                    PopNewHintView.attach(this, event.data)
                }
            }
        } catch (e: Exception) {
        }
    }


    private var loadingDialog: DialogProgress? = null

    private fun checkShare(flag: String) {
        DialogHintToWorld(this).setOnClickListener {
            when (it.id) {
                R.id.tv_Commit -> {
                    shareWorld(flag)
                }
                R.id.tv_Cancel -> {
                    Toast.makeText(this, resources.getString(R.string.string_sendAct_11), Toast.LENGTH_SHORT)
                }
            }
        }.show()
    }

    private fun shareWorld(voiceId: String) {
        //共享到世界
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/voices/$voiceId/share", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    Toast.makeText(this@BaseObjectActivity, "分享成功，快去世界看看吧！", Toast.LENGTH_SHORT)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private var dialog: DialogOffline? = null

    private fun changeLanguage() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
//        if (!TextUtils.isEmpty(sta)) {
//        }
        var myLocale = Locale("zh", sta)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        PopNewHintView.dettach()
        LocalLogUtils.writeLog("onDestroy : ${this.localClassName}", System.currentTimeMillis())
    }

    /**
     * 设置状态栏透明
     */
    protected fun setTranslucentStatus() {
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//魅族6.0 的状态栏顶格,没有夜间
                try {
                    if (Build.DISPLAY.contains("flyme", true)) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    }
                } catch (e: Exception) {
                }
            }*/
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    protected fun setStatusBarFontIconDark(dark: Boolean) {
        // 小米MIUI
        try {
            val window = window
            val clazz = getWindow().javaClass
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag)
            }
        } catch (e: Exception) {
        }

        // 魅族FlymeUI
        try {
            val window = window
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (dark) {
                value or bit
            } else {
                value and bit.inv()
            }
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (e: Exception) {
        }

        // android6.0+系统
        // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        LocalLogUtils.writeLog("App in background:${this.localClassName}", System.currentTimeMillis())
        //只有当前是栈顶的时候, 执行到该方法,算是退出到后台,或者锁屏
        if (ActivityLifecycleHelper.getLatestActivity() == this) {
            ActivityLifecycleHelper.isAppPause = false
        }
    }
}