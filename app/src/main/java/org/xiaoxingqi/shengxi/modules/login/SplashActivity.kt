package org.xiaoxingqi.shengxi.modules.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.core.App
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.listen.MagicCanvasActivity
import org.xiaoxingqi.shengxi.modules.listen.VoiceConnectActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper
import org.xiaoxingqi.shengxi.wedgit.skinView.cardView.SkinCardViewInflater
import skin.support.SkinCompatManager
import skin.support.design.SkinMaterialManager
import skin.support.design.app.SkinMaterialViewInflater
import java.lang.Exception

class SplashActivity : AppCompatActivity() {

    companion object {
        var instance: SplashActivity? = null
    }

    private var customSplash = false
    private var socketExtra: String? = null
    private var jpushExtra: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        SkinMaterialManager.init(this)
        SkinCompatManager.init(this)                         // 基础控件换肤初始化
                .addInflater(SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                .addInflater(SkinCardViewInflater())
                .loadSkin()
        var needFinish = false
        val activity = ActivityLifecycleHelper.getLatestActivity()
        if (activity != null && (activity is MagicCanvasActivity || activity is VoiceConnectActivity || activity is RecordVoiceActivity)) {
            needFinish = true
        }
        super.onCreate(savedInstanceState)
        if (needFinish) {
            finish()
            return
        }
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot) {
            val intent = intent
            if (intent != null) {
                val action = intent.action
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == action) {
                    finish()
                    return
                }
            }
        }
        instance = this
        if (intent != null) {
            //额外的内容
            jpushExtra = intent.getStringExtra("stringExtra")//透传极光通知
            socketExtra = intent.getStringExtra("socketData")
            if (!TextUtils.isEmpty(jpushExtra) || !TextUtils.isEmpty(socketExtra)) {
                ActivityLifecycleHelper.isSeepMsg = true
            }
        }
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//            MiitHelper(null).getDeviceIds(this.applicationContext)
//        }
    }

    private fun showSplashView() {
        // 首次进入，打开欢迎界面
        customSplash = true
    }

    /**
     * 切换Activity
     */
    private fun showMainActivity(intent: Intent, isDelay: Boolean = true) {
        /* var intent = intent
         if (intent == null) {
             intent = Intent(this, MainActivity::class.java)
         } else {
             intent.setClass(this, MainActivity::class.java)
         }
         intent.putExtra("socketInfo", socketExtra)
                 .putExtra("jpushExtra", jpushExtra)*/
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.sInstance != null) {
            showMainActivity(Intent(this, MainActivity::class.java)
                    .putExtra("socketInfo", socketExtra)
                    .putExtra("jpushExtra", jpushExtra), false)
        } else {
            showSplashView() // APP进程重新起来
            request(0)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    /**
     *  1.每天第一次打开app时,刷新token
     *  2.本地记录一个日期的  是否为当前
     */
    private fun request(flag: Int) {
        LocalLogUtils.writeLog("SplashActivity:判断token是否过期", System.currentTimeMillis())
        val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginData == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            LocalLogUtils.writeLog("SplashActivity:未登录", System.currentTimeMillis())
            finish()
        } else {
            App.uid = loginData.user_id
            val preTime = SPUtils.getString(this, IConstant.ISREFRESHTOKEN, "")//上次刷新的时间
            val paserYyMm = TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000)
            if (paserYyMm == preTime) {//已刷新
                showMainActivity(Intent(this, MainActivity::class.java)
                        .putExtra("socketInfo", socketExtra)
                        .putExtra("jpushExtra", jpushExtra))
//                finish()
            } else {
                if (paserYyMm != preTime && loginData.refresh_expire <= System.currentTimeMillis() / 1000) {//刷新token
                    /**
                     * 去登陆
                     */
                    LocalLogUtils.writeLog("SplashActivity:登录已过最长时效性", System.currentTimeMillis())
                    startActivity(Intent(this, LoginActivity::class.java))
                    PreferenceTools.clear(this, IConstant.LOCALTOKEN)
                    finish()
                } else {//刷新token
                    updateToken()
                }
            }
        }
    }

    private fun updateToken() {
        val formBody = FormBody.Builder()
                .add("appVersion", AppTools.getVersion(this))
                .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                .add("platformId", "1")
                .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else {
                    if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei
                })
                .build()
        OkClientHelper.put(this, "users/refresh", formBody, LoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as LoginData
                LocalLogUtils.writeLog("SplashActivity:刷新结果: code:${result.code} msg:${result.msg}", System.currentTimeMillis())
                if (result.code == 0) {
                    PreferenceTools.saveObj(this@SplashActivity, IConstant.LOCALTOKEN, result.data)
                    SPUtils.setString(this@SplashActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                    showMainActivity(Intent(this@SplashActivity, MainActivity::class.java)
                            .putExtra("socketInfo", socketExtra)
                            .putExtra("jpushExtra", jpushExtra), false)
//                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    PreferenceTools.clear(this@SplashActivity, IConstant.LOCALTOKEN)
                    finish()
                }
            }

            override fun onFailure(any: Any?) {
                /**
                 *出现错误的时候去登录
                 */
                try {
                    LocalLogUtils.writeLog("SplashActivity:token 刷新异常:${AppTools.isNetOk(this@SplashActivity)}  ${if (any is String) any.toString() else (any as Exception).message}", System.currentTimeMillis())
                } catch (e: Exception) {
                }
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                PreferenceTools.clear(this@SplashActivity, IConstant.LOCALTOKEN)
                finish()
            }
        })
    }

    private var isFrocessOut = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isFrocessOut = true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        PhoneUtil.getInstance(this).onDestroy()
        if (isFrocessOut) {
            Process.killProcess(Process.myPid())
        }
        PhoneUtil.getInstance(this).onDestroy()
        instance?.let {
            instance = null
        }
    }
}