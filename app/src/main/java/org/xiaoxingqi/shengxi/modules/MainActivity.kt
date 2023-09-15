package org.xiaoxingqi.shengxi.modules

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.SensorEvent
import android.net.Uri
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.jpush.android.api.JPushInterface
import com.alibaba.fastjson.JSON
import com.huawei.android.hms.agent.HMSAgent
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.tencent.bugly.crashreport.CrashReport
import com.xiaomi.mipush.sdk.MiPushClient
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.BuildConfig
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.MODE_SPEAKER
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogNetChange
import org.xiaoxingqi.shengxi.dialog.DialogSystemWarning
import org.xiaoxingqi.shengxi.dialog.DialogVoluntaryUpdate
import org.xiaoxingqi.shengxi.dialog.DialogWaring
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.*
import org.xiaoxingqi.shengxi.modules.home.AnimGuideActivity
import org.xiaoxingqi.shengxi.modules.home.HomeFragment
import org.xiaoxingqi.shengxi.modules.listen.*
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.TalkGraffitiDetailsActivity
import org.xiaoxingqi.shengxi.modules.login.*
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.user.MeFragment
import org.xiaoxingqi.shengxi.modules.user.set.PwdActivity
import org.xiaoxingqi.shengxi.receiver.NetBroadCast
import org.xiaoxingqi.shengxi.service.SocketThread
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.CustomPlayMenuView
import org.xiaoxingqi.shengxi.wedgit.ThemeSetView
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import org.xiaoxingqi.shengxi.wedgit.actionTabar.ActionBarView
import org.xiaoxingqi.shengxi.wedgit.customPlayMenu.CustomHoverRelative
import org.xiaoxingqi.shengxi.wedgit.skinView.cardView.SkinCardViewInflater
import org.xiaoxingqi.yunxin.DemoCache
import skin.support.SkinCompatManager
import skin.support.design.SkinMaterialManager
import skin.support.design.app.SkinMaterialViewInflater
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

const val nightName = "night.skin"

class MainActivity : BaseNormalActivity() {

    private var dialogUpdate: DialogVoluntaryUpdate? = null

    companion object {
        @JvmStatic
        var sInstance: MainActivity? = null
        private const val INSTALL_PACKAGES_REQUEST_CODE = 0x77
    }

    override fun onSensorChanged(event: SensorEvent?) {
        /**
         * 收到处理 通知
         */
        val range = event?.values!![0]
        if (null != mSensor) {
            if (range >= mSensor!!.maximumRange && currentMode != MODE_SPEAKER) {//公放
                currentMode = MODE_SPEAKER
                EventBus.getDefault().post(SensorChangeEvent(1))
            } else if (range < mSensor!!.maximumRange && currentMode != MODE_EARPIECE) {//听筒
                EventBus.getDefault().post(SensorChangeEvent(2))
                currentMode = MODE_EARPIECE
            }
        }
    }

    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeEvent(type))
    }

    private val progressHelper = @SuppressLint("HandlerLeak")
    object : ProgressHelper(5 * 1000L) {
    }
    private val homeFragment = HomeFragment()
    private val echoFragment = EchoeFragment()
    private val listenFragment = ListenFragment()
    private val meFragment = MeFragment()
    private var mCast: NetBroadCast? = null
    private val fragCache = arrayOf(homeFragment, echoFragment, listenFragment, meFragment)
    private var currentFrag: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (SkinMaterialManager.getInstance() == null) {
            SkinMaterialManager.init(this)
            SkinCompatManager.init(this)                         // 基础控件换肤初始化
                    .addInflater(SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                    .addInflater(SkinCardViewInflater())
                    .loadSkin()
        }
        window.setFormat(PixelFormat.TRANSLUCENT)
        sInstance = this
        super.onCreate(null)
        CustomHoverRelative.init()
        try {
            setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancelAll()
            /**
             * SDK连接HMS
             */
            if (OsUtil.isHw()) {
                HMSAgent.connect(this) {}
                HMSAgent.Push.getToken {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        if (SPUtils.getInt(this, IConstant.HAS_CHECKED_CODE, 0) == 1) {
            startActivity(Intent(this, PwdActivity::class.java).putExtra("isVerify", false))
        }
        onParseIntent()
        paserNotifyThread()
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancelAll()
        } catch (e: Exception) {

        }
    }

    /**
     * 解析socket 推送的消息
     */
    private fun onParseIntent() {
        val stringExtra = intent.getStringExtra("socketInfo")
        if (!TextUtils.isEmpty(stringExtra)) {
            try {
                val socketData = JSON.parseObject(stringExtra, SocketData::class.java)
                paserIntent(socketData.data)
            } catch (e: Exception) {

            }
        }
    }

    /**
     * 网页唤醒app
     */
    private fun paserNotifyThread() {
        val jPushExtra = intent.getStringExtra("jpushExtra")
        if (!TextUtils.isEmpty(jPushExtra)) {
            try {
                val noticeData = JSON.parseObject(jPushExtra, SocketData.SocketBean::class.java)
                paserIntent(noticeData)
            } catch (e: Exception) {

            }
        }
        intent = Intent()
    }

    private fun paserIntent(data: SocketData.SocketBean) {
        if (SPUtils.getInt(this, IConstant.HAS_CHECKED_CODE, 0) == 1)
            return
        when (data.type) {
            1 -> {//系统消息
                startActivity(Intent(this, SystemInfoActivity::class.java))
            }
            2 -> {//新好友请求
                startActivity(Intent(this, NewFriendsActivity::class.java))
            }
            3 -> {//共鸣
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            4 -> {//表白
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            5 -> {//好友通知
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            14 -> {//收到新的关注通知
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            16 -> {//收到新的配音
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            17 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            18 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            19 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            20 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            21 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            22 -> {//收到新的下载
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            23 -> {
                startActivity(Intent(this, MsgNotifyActivity::class.java))
            }
            6 -> {
                startActivity(Intent(this, TalkListActivity::class.java)
                        .putExtra("voice_id", data.voice_id)
                        .putExtra("chat_id", data.about_id.toString())
                        .putExtra("uid", data.voice_user_id)
                        .putExtra("talkId", data.from_user_id)
                )
            }
            7 -> {
                startActivity(Intent(this, TalkListActivity::class.java)
                        .putExtra("voice_id", data.voice_id)
                        .putExtra("chat_id", data.about_id.toString())
                        .putExtra("uid", data.voice_user_id)
                        .putExtra("talkId", data.from_user_id)
                )
            }
            1001 -> {
                startActivity(Intent(this, TalkGraffitiDetailsActivity::class.java)
                        .putExtra("resourceId", data.resource_id)
                        .putExtra("uid", data.resource_user_id.toString())
                        .putExtra("from", data.from_user_id)
                        .putExtra("chatId", data.chat_id.toString())
                )
            }
            8 -> {//系统回复
                startActivity(Intent(this, ActionActivity::class.java)
                        .putExtra("isHtml", true)
                        .putExtra("isScroll", true)
                        .putExtra("url", data.about_id.toString()))
            }
            9 -> {
                startActivity(Intent(this, ChatActivity::class.java)
                        .putExtra("uid", data.from_user_id))
            }
            10 -> {
                startActivity(Intent(this, ChatActivity::class.java)
                        .putExtra("uid", data.from_user_id))
            }
            11 -> {//警告
                DialogSystemWarning(this).show()
            }
            else -> {//6 7 回声   9 10 私聊
                if (data.chat_type == 1) {
                    startActivity(Intent(this, TalkListActivity::class.java)
                            .putExtra("voice_id", data.voice_id)
                            .putExtra("chat_id", data.chat_id.toString())
                            .putExtra("uid", data.voice_user_id)
                            .putExtra("talkId", data.from_user_id)
                    )
                } else if (data.chat_type == 2) {
                    startActivity(Intent(this, ChatActivity::class.java)
                            .putExtra("uid", data.from_user_id))
                } else {//涂鸦对话
                    //TODO
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val preTime = SPUtils.getString(this, IConstant.ISREFRESHTOKEN, "")//上次刷新的时间
        val paserYyMm = TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000)
        if (paserYyMm == preTime) {
            request(5)
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentFrag is HomeFragment) {
            (currentFrag as HomeFragment).stop()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        switchFragment(TypeFragment.Home)
    }

    override fun initData() {
        if (intent.getBooleanExtra("isSign", false)) {
            startActivity<AnimGuideActivity>("name" to "guide")
        }
        val obj = PreferenceTools.getObj(this@MainActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        VoiceProgress.isHideText = SPUtils.getBoolean(this, IConstant.WORD_ENABLE_VOICE + obj.user_id, false)
        try {
            val file = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/cache")
            val file1 = File(Environment.getExternalStorageDirectory(), application.packageName)
            if (file1.exists()) {
                Thread {
                    ClearCache.deleteFileAndFile(file1)
                }.start()
            }
            if (file.exists()) {
                Thread {
                    ClearCache.deleteFileAndFile(file)
                }.start()
            }
            //删除云信log目录下的产生的大量垃圾文件,引用到3个版本
            val logFile = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/log")
            if (logFile.exists()) {
                //删除缓存文件
                if (logFile.listFiles().size >= 10)
                    ClearCache.deleteFileAndFile(logFile)
            }
        } catch (e: Exception) {
            LocalLogUtils.writeLog("MainPage: 删除遗留缓存文件出错", System.currentTimeMillis())
        }
        /**
         * 文件权限
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), INSTALL_PACKAGES_REQUEST_CODE)
            } else {
                createFile()
            }
        } else {
            createFile()
        }
        if ("start" == intent.getStringExtra("isEnterTest")) {//是否立即进入测一测性格
            startActivity(Intent(this, PersonalityActivity::class.java))
        }
        if (SPUtils.getInt(this, IConstant.HAS_CHECKED_CODE, 0) == 1) {
            startActivity(Intent(this, PwdActivity::class.java).putExtra("isVerify", false))
        }
        /* if (!SPUtils.getBoolean(this, IConstant.HOME_GUIDE_VISIBLE + obj.user_id, false)) {
             homeGuideView.visibility = View.VISIBLE
         }*/
        paserNotifyThread()
        val preTime = SPUtils.getString(this, IConstant.ISREFRESHTOKEN, "")//上次刷新的时间
        val parseYyMm = TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000)
        if (parseYyMm != preTime && !isLoading) {
            request(8)
        } else {
            if (intent.getBooleanExtra("isCheckIp", false)) {
                request(11)
            }
            request(0)
            try {
                if (SPUtils.getLong(this, IConstant.ISHINTUPDATE + obj.user_id, 0) + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                    request(12)
                }
            } catch (e: Exception) {
            }
            request(13)
            request(14)
        }
        LocalLogUtils.writeLog("MainPage: 当前用户的IP ${IPUtils.getIpAddress(this)}", System.currentTimeMillis())
        try {
            mCast = NetBroadCast()
            val filternet = IntentFilter()
            //                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filternet.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            filternet.addAction("android.net.wifi.WIFI_STATE_CHANGED")
            filternet.addAction("android.net.wifi.STATE_CHANGE")
            registerReceiver(mCast, filternet)
        } catch (e: Exception) {
        }
    }

    /**
     * 创建文件夹
     */
    private fun createFile() {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME)
        if (!file.exists())
            file.mkdir()
        val rootFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE)
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }
        val cacheAudio = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/audio")
        if (!cacheAudio.exists()) {
            cacheAudio.mkdirs()
        }
    }

    override fun initEvent() {
        actionbarView.setOnTabClickListener(object : ActionBarView.OnTabOnClickListener {
            override fun tabVlick(view: View?): Boolean {
                when (view?.id) {
                    R.id.tab_01 -> {
                        switchFragment(TypeFragment.Home)
                        return true
                    }
                    R.id.tab_02 -> {
                        switchFragment(TypeFragment.Echo)
                        return true
                    }
                    R.id.Iv_HomeButton -> {
                        if (!RecordTransparentActivity.isOnCreate) {
                            startActivity(Intent(this@MainActivity, RecordTransparentActivity::class.java)
                                    .putExtra("type", 1)
                                    .putExtra("isHome", true))
                            overridePendingTransition(0, 0)
                        }
                        return true
                    }
                    R.id.tab_03 -> {
                        switchFragment(TypeFragment.Listen)
                        return true
                    }
                    R.id.tab_04 -> {
                        switchFragment(TypeFragment.Me)
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }

            override fun doubleClick(view: View?) {
                //双击首页的按钮
                //if (view?.id == R.id.tab_01)
                //EventBus.getDefault().post(UpdateSendVoice(3))
            }
        })
        themeSetView.setOnDismissListener(object : ThemeSetView.OnDismissListener {
            override fun skinChange() {

            }

            override fun onDismiss() {
                /*
                 * 切换主题, 或者是切换氛围
                 */
                if (currentFrag is HomeFragment) {
                    (currentFrag as HomeFragment).setThemeChange()
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showThemeEvent(event: ThemeSetMainEvent) {
        /**
         * 点击通知, 切换界面
         */
        themeSetView.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pageChangeEvent(event: ImplPageChangeEvent) {
        if (event.page == 0) {
            switchFragment(TypeFragment.Home)
            actionbarView.setCurrentSelect(0)
        }
    }

    fun setEchoesFlag(isVisible: Boolean) {
        if (actionbarView != null) {
            actionbarView.setFlag(isVisible, 1)
        }
    }

    override fun request(flag: Int) {
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            0 -> {
                if (null != obj)
                    OkClientHelper.get(this, "users/${obj.user_id}", UserInfoData::class.java, object : OkResponse {
                        override fun success(result: Any?) {
                            result as UserInfoData
                            /**
                             *查询云信token
                             */
                            when (result.code) {
                                0 -> {
                                    LightUtils.initArray(applicationContext)
                                    SPUtils.setLong(this@MainActivity, IConstant.USERLIMITTIME, result.data.released_at)
                                    LocalLogUtils.writeLog("-----------current user id:${result.data.user_id} nickName: ${result.data.nick_name}----------", System.currentTimeMillis())
                                    CrashReport.setUserId(result.data.user_id)//设置bugly日志用户id
                                    SPUtils.setInt(this@MainActivity, IConstant.TOTALLENGTH + result.data.user_id, result.data.voice_total_len)
                                    PreferenceTools.saveObj(this@MainActivity, IConstant.USERCACHE, result)
                                    /*if (TextUtils.isEmpty(result.data.wave_url)) {
                                        if (result.data.interface_type_setted == "0") {
                                            startActivity(Intent(this@MainActivity, SelectCharacterTypeActivity::class.java)
                                                    .putExtra("illegalRecord", true)
                                                    .putExtra("isCanBack", false)
                                                    .putExtra("uid", result.data.user_id.toString()))
                                        }
                                    } else {
                                    }*/
                                    SPUtils.setString(this@MainActivity, IConstant.USERCHARACTERTYPE + result.data.user_id, result.data.interface_type)
                                    if (OsUtil.isMIUI()) {
                                        MiPushClient.setAlias(this@MainActivity, result.data.socket_id, null)
                                        request(10)
                                    } else if (!OsUtil.isHw()) {
                                        JPushInterface.setAlias(this@MainActivity, result.data.user_id.toInt(), result.data.socket_id)
                                    }
                                    App.uid = result.data.user_id
//                                    oreo {
//                                        startForegroundService(Intent(this@MainActivity, SocketServer::class.java))
//                                    }
//                                    preOreo {
//                                        startService(Intent(this@MainActivity, SocketServer::class.java))
//                                    }
                                    SocketThread.instances(applicationContext).onStartCommand()
                                    request(2)
                                    request(4)
                                    request(6)
                                    request(9)
                                }
                                412 -> {
                                    PreferenceTools.clear(this@MainActivity, IConstant.LOCALTOKEN)
                                    PreferenceTools.clear(this@MainActivity, IConstant.USERCACHE)
                                    DialogWaring(this@MainActivity).setOnClickListener(View.OnClickListener {
                                        startActivity(Intent(this@MainActivity, LoginActivity::class.java)
                                                .putExtra("isBan", true))
                                        finish()
                                    }).show()
                                }

                                //                                else -> showToast(result.msg)
                            }
                        }

                        override fun onFailure(any: Any?) {

                        }
                    })
            }
            2 -> {
                /**
                 * 更新极光地址
                 */
                val userInfoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                OkClientHelper.patch(this, "users/${userInfoData.data.user_id}", FormBody.Builder()
                        .add("jpushId", userInfoData.data.socket_id)
                        .add("platformId", "1")
                        .build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {

                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            3 -> {
                /**
                 * 检测新版本
                 */
                OkClientHelper.get(this, "apps/1/${AppTools.getVersion(this)}", VersionData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VersionData
                        if (result.code == 0 && result.data != null) {
                            dialogUpdate = DialogVoluntaryUpdate(this@MainActivity).setForce(result.data.forced_update, result.data.stop_at).setOnClickListener(View.OnClickListener {
                                if (result.data.forced_update == 0) {
                                    val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                    val uri = Uri.parse(result.data.app_url)
                                    val request = DownloadManager.Request(uri)
                                    request.setTitle(resources.getString(R.string.app_name) + "下载中...")
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)//在wifi下下载
                                    val name = "shengxi_${System.currentTimeMillis() / 1000}.apk"
                                    request.setDestinationInExternalPublicDir(IConstant.DOCNAME, name)
                                    request.setMimeType("application/vnd.android.package-archive")
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    val enqueue = manager.enqueue(request)
                                    listenerLoad(enqueue, name)
                                    dialogUpdate?.dismiss()
                                } else {
                                    load(result.data.app_url)
                                }
                            })
                            dialogUpdate?.show()
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
            4 -> {//获取云信token
                OkClientHelper.get(this, "users/${obj.user_id}/yxtoken", YunxinTokenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as YunxinTokenData
                        if (result.code == 0) {

                            val future = NIMClient.getService(AuthService::class.java).login(LoginInfo(result.data.yunxin_id, result.data.token))
                            future.setCallback(object : RequestCallback<LoginInfo> {
                                override fun onSuccess(loginInfo: LoginInfo) {
                                    DemoCache.setAccount(loginInfo.account)
                                }

                                override fun onFailed(code: Int) {
                                    if (302 == code) {
                                        request(7)
                                    }
                                }

                                override fun onException(exception: Throwable) {
                                }
                            })
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
            5 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                if (loginBean != null) {
                    OkClientHelper.get(this, "messages/${loginBean.user_id}", SystemNoticeData::class.java, object : OkResponse {
                        override fun success(result: Any?) {
                            if ((result as SystemNoticeData).code == 0) {
                                if (result.data.total > 0 || result.data.chatpri.num > "0") {//显示小红点
                                    setEchoesFlag(true)
                                } else {
                                    setEchoesFlag(false)
                                }
                            }
                        }

                        override fun onFailure(any: Any?) {

                        }
                    })
                }
            }
            6 -> {//获取最近一次的测试结果
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "personality/${loginBean.user_id}/lastLog", PersonnalityData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PersonnalityData
                        if (result.code == 0) {
                            if ("0" != result.data.personality_id) {
                                SPUtils.setString(this@MainActivity, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", result.data.personality_id)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
            7 -> {//强制更新云信的token
                OkClientHelper.get(this, "users/${obj.user_id}/yxtoken?forceUpdated=1", YunxinTokenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as YunxinTokenData
                        if (result.code == 0) {
                            val future = NIMClient.getService(AuthService::class.java).login(LoginInfo(result.data.yunxin_id, result.data.token))
                            future.setCallback(object : RequestCallback<LoginInfo> {
                                override fun onSuccess(loginInfo: LoginInfo) {
                                    DemoCache.setAccount(loginInfo.account)
                                }

                                override fun onFailed(code: Int) {
                                }

                                override fun onException(exception: Throwable) {
                                }
                            })
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
            8 -> {//请求是否刷新token
                val formBody = FormBody.Builder()
                        .add("appVersion", AppTools.getVersion(this))
                        .add("deviceInfo", PhoneUtil.getBrand() + "/" + PhoneUtil.getModel() + "/" + PhoneUtil.getVersion())
                        .add("platformId", "1")
                        .add("deviceId", if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) SPUtils.getString(this, IConstant.CACHEUUID, "") else {
                            if (TextUtils.isEmpty(PhoneUtil.getInstance(this).imei)) "" else PhoneUtil.getInstance(this).imei
                        })
                        .build()
                /**
                 * 开启强制刷新token
                 */
                OkClientHelper.setCancel(true)
                val data = SocketData.SocketBean()
                data.type = 115
                EventBus.getDefault().post(OnNewHintViewEvent(data))
                OkClientHelper.put(this, "users/refresh", formBody, LoginData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as LoginData
                        if (result.code == 0) {
                            PreferenceTools.saveObj(this@MainActivity, IConstant.LOCALTOKEN, result.data)
                            SPUtils.setString(this@MainActivity, IConstant.ISREFRESHTOKEN, TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000))
                        }
                        val socketData = SocketData.SocketBean()
                        socketData.type = 116
                        EventBus.getDefault().post(OnNewHintViewEvent(socketData))
                        LocalLogUtils.writeLog("MainPage: 重进入app,刷新token 刷新结果 : ${result.code}  ", System.currentTimeMillis())
                        OkClientHelper.setCancel(false)
                        isLoading = false
                    }

                    override fun onFailure(any: Any?) {
                        val socketData = SocketData.SocketBean()
                        socketData.type = 116
                        EventBus.getDefault().post(OnNewHintViewEvent(socketData))
                        LocalLogUtils.writeLog("MainPage: 重进入app,刷新token 刷新错误  ${any.toString()}", System.currentTimeMillis())
                        OkClientHelper.setCancel(false)
                        isLoading = false
                    }
                })
            }
            9 -> {
                OkClientHelper.get(this, "users/${obj.user_id}/setting/strangeView", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            SPUtils.setBoolean(this@MainActivity, IConstant.STRANGEVIEW + obj.user_id, result.data.strange_view == 0)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            10 -> {//更新小米或者华为设备id
                val loginBean = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
                OkClientHelper.post(this, "users/${loginBean.data.user_id}/pushDevice", FormBody.Builder()
                        .add("deviceToken", if (OsUtil.isHw()) "$hwToken" else "${loginBean.data.socket_id}")
                        .add("vendorId", if (OsUtil.isHw()) "1" else "2").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.6")
            }
            11 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "user/${loginBean.user_id}/ip", IpQuestData::class.java, object : OkResponse {

                    override fun success(result: Any?) {
                        result as IpQuestData
                        if (result.code == 0 && result.data != null) {
                            if (!result.data.country_id.equals("cn", true) || "香港" == result.data.region || "澳门" == result.data.region || "台湾" == result.data.region) {
                                DialogNetChange(this@MainActivity).show()
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }

                }, "V3.7")
            }
            12 -> {
                OkClientHelper.get(this, "apps/1", VersionData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VersionData
                        if (result.code == 0) {
                            val localVersion = AppTools.getVersion(this@MainActivity)
                            if (result.data.app_version > localVersion) {
                                request(3)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
            13 -> {//初始化进入项目,加载用户的封面图集合
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/covers?coverName=moodbook_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionCoverData
                        if (result.code == 0) {
                            result.data?.let {
                                SPUtils.setString(this@MainActivity, IConstant.USER_CACHE_COVER_LIST + loginBean.user_id, it.joinToString(",") { bean -> bean.coverUrl })
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.2")
            }
            14 -> {//获取用户的设置
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/settings?settingName=cover_efficacy&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        //默认可见
                        result.data?.let { data ->
                            if (data.setting_name == "cover_efficacy") {
                                SPUtils.setString(this@MainActivity, IConstant.USER_THEME_COVER_MODEL + loginBean.user_id, data.setting_value.toString())
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
        }
    }

    private var broadcastReceiver: BroadcastReceiver? = null
    private fun listenerLoad(id: Long, name: String) {
        try {
            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val downId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downId == id) {
                        /**
                         * 打开安装界面
                         */
                        val newIntent = Intent()
                        newIntent.action = "android.intent.action.VIEW"
                        newIntent.addCategory("android.intent.category.DEFAULT")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            val contentUri = FileProvider.getUriForFile(this@MainActivity, BuildConfig.APPLICATION_ID + ".fileprovider", File(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + name))
                            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            newIntent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                        } else {
                            newIntent.setDataAndType(Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + name)), "application/vnd.android.package-archive")
                        }
                        startActivity(newIntent)
                    }
                }
            }
            registerReceiver(broadcastReceiver, intentFilter)
        } catch (e: Exception) {

        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun load(path: String) {
        object : AsyncTask<Void, Float, File>() {
            override fun doInBackground(vararg params: Void?): File? {
                val url = URL(path)
                val connt = url.openConnection() as HttpURLConnection
                val allLength = connt.contentLength//文件总长度
                if (allLength <= 0) {
                    return null
                }
                val fileParent = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/${IConstant.CACHE_NAME}/${IConstant.VOICENAME}")
                if (!fileParent.exists()) {
                    fileParent.mkdirs()
                }
                val file = File(fileParent, "shengxi.apk")
                if (connt.responseCode == 200) {
                    val ins = connt.inputStream
                    val fos = FileOutputStream(file)
                    val bytes = ByteArray(1024)
                    var len: Int
                    var current = 0
                    ins.use {
                        fos.use {
                            while (ins.read(bytes).also { length ->
                                        len = length
                                    } != -1) {
                                it.write(bytes, 0, len)
                                it.flush()
                                current += len
                                publishProgress(current.toFloat() / allLength)
                            }
                        }
                    }
                    fos.close()
                    ins.close()
                }
                return file
            }

            override fun onProgressUpdate(vararg values: Float?) {
                dialogUpdate?.updateProgress(values[0]!!)
                super.onProgressUpdate(*values)
            }

            override fun onPostExecute(result: File?) {
                super.onPostExecute(result)
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                intent.addCategory("android.intent.category.DEFAULT")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val contentUri = FileProvider.getUriForFile(this@MainActivity, BuildConfig.APPLICATION_ID + ".fileprovider", result!!)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                } else {
                    intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive")
                }
                startActivity(intent)
            }
        }.execute()
    }

    private fun clearClick(frag: Fragment) {
        for (tab in fragCache) {
            if (tab === frag) {
                (tab as ITabClickCall).tabClick(true)
            } else {
                (tab as ITabClickCall).tabClick(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onThemeEvent(event: OnThemeEvent) {
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//设为夜间模式
            SkinCompatManager.getInstance().loadSkin()
            SkinCompatManager.getInstance().loadSkin(nightName, object : SkinCompatManager.SkinLoaderListener {
                override fun onStart() {

                }

                override fun onSuccess() {
                    setTranslucentStatus()
                    EventBus.getDefault().post(FragSkinUpdateTheme())
                }

                override fun onFailed(errMsg: String) {

                }
            })
        } else {//设为白天模式
            SkinCompatManager.getInstance().loadSkin("", object : SkinCompatManager.SkinLoaderListener {
                override fun onStart() {

                }

                override fun onSuccess() {
                    setTranslucentStatus()
                    setStatusBarFontIconDark(true)
                    EventBus.getDefault().post(FragSkinUpdateTheme())

                }

                override fun onFailed(errMsg: String) {

                }
            })
        }
        /**
         * 更新其他三个fragment
         */
        Handler().postDelayed({
            EventBus.getDefault().post(UpdateSendVoice(2))
        }, 1500)
    }

    @Synchronized
    private fun switchFragment(type: TypeFragment) {
        val transTemp = supportFragmentManager.beginTransaction()
        currentFrag?.let { transTemp.hide(currentFrag!!) }
        currentFrag = when (type) {
            TypeFragment.Home ->
                homeFragment
            TypeFragment.Echo ->
                echoFragment
            TypeFragment.Listen ->
                listenFragment
            TypeFragment.Me ->
                meFragment
        }
        currentFrag?.let {
            if (!currentFrag!!.isAdded) {
                transTemp.add(R.id.frame_container, currentFrag!!)
            }
        }
        transTemp.show(currentFrag!!)
        clearClick(currentFrag!!)
//        transTemp.commit()
        transTemp.commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
    }

    private var hwToken: String? = null

    /**
     * 华为手机获取到token之后注册
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hwToken(event: IHwTokenEvent) {
        if (TextUtils.isEmpty(event.token)) {
            val infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
            JPushInterface.setAlias(this@MainActivity, infoData.data.user_id.toInt(), infoData.data.socket_id)
        } else {
            hwToken = event.token
            request(10)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun socketMsg(event: SocketEvent) {// 再次发送数据之后可能出现数据校验错误: 数据有误，请刷新当前页面
        if (event.type == 1) {
            showToast("回声成功")
        } else if (event.type == 2) {
            request(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateTokenEvent(event: UpdateTokenEvent) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        LocalLogUtils.writeLog("MainPage: App in  foreground", System.currentTimeMillis())
        val preTime = SPUtils.getString(this, IConstant.ISREFRESHTOKEN, "")//上次刷新的时间
        val parseYyMm = TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000)
        if (parseYyMm != preTime && !isLoading) {//先于Fragment的onResume
            if (AppTools.isNetOk(this)) {
                OkClientHelper.setCancel(true)
                isLoading = true
                LocalLogUtils.writeLog("MainPage: 重进入app,刷新token  上一次$preTime  本地$parseYyMm", System.currentTimeMillis())
                request(8)
            }
        }
    }

    @Volatile
    private var isLoading = false

    private var preNetState = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun netEvent(event: NetUpdateEvent) {
        if (AppTools.isNetOk(this)) {
            LocalLogUtils.writeLog("MainPage: 网络状态发生变化->网络可用 ", System.currentTimeMillis())
            if (!preNetState) {
                val data = SocketData.SocketBean()
                data.type = 113
                EventBus.getDefault().post(OnNewHintViewEvent(data))
                SocketThread.instances(applicationContext).onStartCommand()
                //判断当天是否刷新token,悄悄咪咪的刷新token
                val preTime = SPUtils.getString(this, IConstant.ISREFRESHTOKEN, "")//上次刷新的时间
                val parseYyMm = TimeUtils.getInstance().paserYyMm(System.currentTimeMillis() / 1000)
                if (parseYyMm != preTime && !isLoading) {//先于Fragment的onResume
                    if (AppTools.isNetOk(this)) {
                        OkClientHelper.setCancel(true)
                        isLoading = true
                        LocalLogUtils.writeLog("MainPage: 网络状态监测后台刷新token  上一次$preTime  本地$parseYyMm", System.currentTimeMillis())
                        request(8)
                    }
                }
            }
            preNetState = true
        } else {
            LocalLogUtils.writeLog("MainPage: 网络状态发生变化->异常, 不可用网络 ", System.currentTimeMillis())
            if (preNetState) {
                if (!AppTools.isWifiConnected(this) && !AppTools.isMobileConnected(this)) {
                    val data = SocketData.SocketBean()
                    data.type = 112
                    EventBus.getDefault().post(OnNewHintViewEvent(data))
                }
            }
            preNetState = false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (themeSetView.isShow()) {
                themeSetView.show()
                return true
            }
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK// 注意
            intent.addCategory(Intent.CATEGORY_HOME)
            this.startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == INSTALL_PACKAGES_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFile()
            }
        }
    }

    class NetUpdateEvent

    override fun finish() {
        super.finish()
        unregisterReceiver(mCast)
        sInstance = null
        progressHelper.removeCallbacks(progressHelper)
        broadcastReceiver?.let {
            unregisterReceiver(it)
        }
        try {
            CustomPlayMenuView.clearList()
        } catch (e: Exception) {
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        LocalLogUtils.writeLog("程序被系统回收,onRestoreInstanceState", System.currentTimeMillis())

        /**
         * App退出进程, 重新从SplashActivity 打开
         */
        try {
            startActivity(Intent(this, SplashActivity::class.java))
        } catch (e: Exception) {

        }
        finish()
    }
}

enum class TypeFragment {
    Home, Echo, Listen, Me;
}
