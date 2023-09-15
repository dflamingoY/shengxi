package org.xiaoxingqi.shengxi.modules.listen

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorEvent
import android.os.Build
import android.os.Message
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.avchat.AVChatCallback
import com.netease.nimlib.sdk.avchat.AVChatManager
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType
import com.netease.nimlib.sdk.avchat.constant.AVChatType
import com.netease.nimlib.sdk.avchat.model.*
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory
import kotlinx.android.synthetic.main.activity_voice_connect.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.BaseObjectActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.CallWaitDialog
import org.xiaoxingqi.shengxi.dialog.DialogAddFriends
import org.xiaoxingqi.shengxi.dialog.DialogDismissConnect
import org.xiaoxingqi.shengxi.dialog.DialogMatchEmpty
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.SocketData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.yunxin.rts.AVChatConfigs
import org.xiaoxingqi.yunxin.rts.PhoneCallStateObserver
import org.xiaoxingqi.yunxin.rts.SimpleAVChatStateObserver
import java.util.*

/**
 * 语音通话
 * 每晚九点59分开始
 */
class VoiceConnectActivity : BaseNormalActivity() {
    private val REQUESTRECORD = 0x1011
    private var account: String? = null
    private var state: Int = 0 // calltype 音频或视频
    private var mVideoCapturer: AVChatCameraCapturer? = null
    private var cast: BroadcastReceiver? = null
    private var dialog: CallWaitDialog? = null
    private var avChatData: AVChatData? = null // config for connect video server
    private var time = 0L
    private var avChatConfigs: AVChatConfigs? = null
    private var isCallEstablished = false // 电话是否接通
    private var mIsInComingCall = false// 來電或者拨打
    private var fromUsreId: String? = null
    private var relation = -1//未给改字段赋值, 表示未连接成功过
    private var iscall = false//是否拨打成功
    private var isDiscard = false//是否放弃所有任务
    private val progressHelper = @SuppressLint("HandlerLeak")
    object : ProgressHelper() {
        override fun handleMessage(msg: Message?) {
            if (null != dialog) {
                if (dialog!!.isShowing)
                    dialog?.timeTick(((System.currentTimeMillis() - time) / 1000).toInt())
            }
            if (isCallEstablished) {
                var dTime = callTime - System.currentTimeMillis()
                if (dTime < 0) {
                    dTime = 0
                }
                tv_Timer.text = AppTools.parseTime2Str(dTime)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_voice_connect
    }

    override fun initView() {
        // 禁止自动锁屏
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        tv_BanVoice.isSelected = false
    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            tv_Desc.text = resources.getString(R.string.string_voice_call_e_1)
            tv_Desc1.text = resources.getString(R.string.string_voice_call_e_2)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUESTRECORD)
            }
        }
        initPage()
        /*progressHelper.postDelayed({
            relativeWait.visibility = View.GONE
            linear_Match.visibility = View.VISIBLE
            // 开始一分钟的倒计时
            tv_TimeTick.base = SystemClock.elapsedRealtime()
            tv_TimeTick.start()
            progressHelper.postDelayed({
                tv_TimeTick.base = SystemClock.elapsedRealtime()
                tv_TimeTick.stop()
            }, 60000)

        }, 3000)*/
        cast = TickBroadCast()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        registerReceiver(cast, filter)
        registerObserves(true)
        avChatConfigs = AVChatConfigs(this)
//        request(1)
    }

    /**
     * 初始化界面
     */
    private fun initPage() {
        relativeWait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        val date = Date()
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        /**
         * 获取当前时间
         */
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var dtime = calendar.timeInMillis - System.currentTimeMillis() + 1000
        if (dtime < 0) {
            dtime += 24 * 60 * 60 * 1000
        }
        countDown.stop()
        countDown.start(dtime)
        /**
         * 當前時間是否在 20:59- 21:00 之間
         */
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        if (hour == 20 && min == 59 && second <= 58) {//当前是 21.59 分 仍可以匹配
            relativeWait.visibility = View.GONE
            linear_Match.visibility = View.VISIBLE
            countTime = 60 - second
            tv_TimeTick.base = SystemClock.elapsedRealtime()
            tv_TimeTick.start()
        }
        btn_Back.visibility = View.VISIBLE
//        AVChatManager.getInstance().disableRtc()
    }

    override fun onResume() {
        super.onResume()
        if (linear_Match.visibility == View.VISIBLE) {
            val base = tv_TimeTick.base
            val realtime = SystemClock.elapsedRealtime()
            if (countTime > 0) {
                countTime -= ((realtime - base) / 1000).toInt()
            }
        }
        if (!isCallEstablished) {
            initPage()
        }
        if (isMatch) {// 如果是点击了匹配 退出到后台, 即使休眠之后, 重新计算当前时间, 如果超出时间
            LocalLogUtils.writeLog("VoiceCall:onResume 正在匹配中", System.currentTimeMillis())
            if (null != dialog && dialog!!.isShowing) {
                val difference = getDifference()
                LocalLogUtils.writeLog("VoiceCall:onResume 正在匹配中  获取当前的时间差  $difference", System.currentTimeMillis())
                if (difference <= 0 && difference >= -10000) {
                    if (!isCallEstablished && isMatch) {
                        if (!running) {
                            LocalLogUtils.writeLog("VoiceCall:onResume 查询结果", System.currentTimeMillis())
                            running = true
                            delayTask(difference)
                        }
                    }
                } else if (difference < -10000) {
                    LocalLogUtils.writeLog("VoiceCall:onResume 超过10s 不在查询结果", System.currentTimeMillis())
                    isDiscard = true
                    dialog!!.dismiss()
                    progressHelper.stop()
                    if (!isCallEstablished) {//未接通
                        relativeWait.visibility = View.VISIBLE
                        linear_Match.visibility = View.GONE
                        initPage()
                        DialogMatchEmpty(this@VoiceConnectActivity).setOnClickListener(View.OnClickListener {
                            request(5)
                        }).show()
                        LocalLogUtils.writeLog("VoiceCall:匹配结束,onResume等待超时", System.currentTimeMillis())
                    }
                    request(1)
                    AVChatManager.getInstance().disableRtc()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tv_TimeTick.base = SystemClock.elapsedRealtime()
    }

    private var callTime = 0L
    private var allTime = 300
    private var countTime = 60

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        tv_TimeTick.setOnChronometerTickListener {
            tv_TimeTick.text = "${countTime}秒"
            countTime--
            if (countTime < 0) {
                countTime = 0
            }
        }

        btn_Back.setOnClickListener {
            if (!isCallEstablished) {
                finish()
            }
        }
        tv_Match.setOnClickListener {
            /**
             *  匹配
             */
            transLayout.showProgress()
            request(0)
        }
        /**
         * 公放
         */
        tv_Speaker.setOnClickListener {
            AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled())
            tv_Speaker.isSelected = AVChatManager.getInstance().speakerEnabled()
            if (AVChatManager.getInstance().speakerEnabled()) {
                tv_Speaker.text = resources.getString(R.string.string_voice_call_3)
                tv_Speaker.textSize = 12f
            } else {
                tv_Speaker.text = resources.getString(R.string.string_voice_call_4)
                tv_Speaker.textSize = 13f
            }
        }
        /**
         * 结束通话
         */
        tv_Close_Call.setOnClickListener {
            DialogDismissConnect(this).setOnClickListener(View.OnClickListener {
                LocalLogUtils.writeLog("VoiceCall:用户主动挂断了电话:${if (mIsInComingCall) "拨打方" else "接听方"}", System.currentTimeMillis())
                hangUp()
                request(1)
            }).show()
        }
        /**
         * 静音
         */
        tv_BanVoice.setOnClickListener {
            if (!AVChatManager.getInstance().isLocalAudioMuted) { // isMute是否处于静音状态
                // 关闭音频
                AVChatManager.getInstance().muteLocalAudio(true)
                tv_BanVoice.text = resources.getString(R.string.string_voice_call_1)
                tv_BanVoice.isSelected = true
                tv_BanVoice.textSize = 12f
            } else {
                // 打开音频
                AVChatManager.getInstance().muteLocalAudio(false)
                tv_BanVoice.text = resources.getString(R.string.string_voice_call_2)
                tv_BanVoice.textSize = 13f
                tv_BanVoice.isSelected = false
            }
        }
        tv_change.setOnClickListener {
            startActivity(Intent(this, VoiceCallSetActivity::class.java).putExtra("type", 1))
        }
        tv_change1.setOnClickListener { startActivity(Intent(this, VoiceCallSetActivity::class.java).putExtra("type", 1)) }
    }

    /**
     * 调用结束通话
     */
    private fun hangUp() {
        if (null != dialog) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
        AVChatManager.getInstance().disableRtc()
        if (avChatData != null)
            AVChatManager.getInstance().hangUp2(avChatData?.chatId!!, object : AVChatCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    LocalLogUtils.writeLog("VoiceCall:流星电话挂断成功", System.currentTimeMillis())
                }

                override fun onFailed(code: Int) {
                    LocalLogUtils.writeLog("VoiceCall:流星电话挂断失败 $code", System.currentTimeMillis())
                }

                override fun onException(exception: Throwable) {
                    LocalLogUtils.writeLog("VoiceCall:流星电话挂断异常 ${exception.message}", System.currentTimeMillis())
                }
            })
        relative_calling.visibility = View.GONE
        relativeWait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        if (!isDestroyed) {
            if (relation == 0 && !TextUtils.isEmpty(fromUsreId))
                DialogAddFriends(this).setOnClickListener(View.OnClickListener {
                    /**
                     * 添加好友
                     */
                    request(3)
                }).show()
        }
        btn_Back.visibility = View.VISIBLE
        isCallEstablished = false
        isMatch = false
        initPage()
    }

    /**
     * 只能拨打一次
     * @link mIsInComingCall  是否是拨打电话,
     */
    @Synchronized
    private fun call(extendMessage: String) {
        if (!mIsInComingCall)
            synchronized(this) {
                if (!mIsInComingCall) {
                    mIsInComingCall = true
                    AVChatManager.getInstance().enableRtc()
                    tv_call_topic.text = extendMessage
                    AVChatManager.getInstance().setParameters(avChatConfigs?.avChatParameters)
                    AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true)
                    val notifyOption = AVChatNotifyOption()
                    notifyOption.extendMessage = extendMessage
                    LocalLogUtils.writeLog("VoiceCall:尬聊话题  $extendMessage", System.currentTimeMillis())
                    AVChatManager.getInstance().call2(account, AVChatType.AUDIO, notifyOption, object : AVChatCallback<AVChatData> {
                        override fun onSuccess(data: AVChatData) {
                            avChatData = data
                            iscall = true
                            LocalLogUtils.writeLog("VoiceCall:流星电话拨打成功", System.currentTimeMillis())
                        }

                        override fun onFailed(code: Int) {
                            LocalLogUtils.writeLog("VoiceCall:流星电话拨打失败$code", System.currentTimeMillis())
                            exception()
                        }

                        override fun onException(exception: Throwable) {
                            LocalLogUtils.writeLog("VoiceCall:流星电话拨打出现异常${exception.message}", System.currentTimeMillis())
                            exception()
                        }
                    })
                    if (BaseObjectActivity.currentMode != 1 && BaseObjectActivity.currentMode != 3) {
                        AVChatManager.getInstance().setSpeaker(true)
                        tv_Speaker.text = resources.getString(R.string.string_voice_call_3)
                        tv_Speaker.isSelected = true
                        tv_Speaker.textSize = 12f
                    } else {
                        tv_Speaker.text = resources.getString(R.string.string_voice_call_4)
                        tv_Speaker.textSize = 13f
                        tv_Speaker.isSelected = false
                    }
                }
            }
    }

    /**
     * 返回当前距离匹配结束的时间差
     */
    private fun getDifference(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        cal.time = Date()
        var hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val oc = minute / 5
        var current = oc * 5
        //5之前用5  5之后也用5
        if (current < minute) {
            current += 5
        }
        if (current == 60) {
            hour += 1
            current = 0
        }
        cal.set(Calendar.HOUR_OF_DAY, 21)//21
        cal.set(Calendar.MINUTE, 0)//0
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis - System.currentTimeMillis()
    }

    /**
     * 出现异常的时候初始化到界面
     */
    private fun exception() {
        if (null != dialog) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
        AVChatManager.getInstance().disableRtc()
        relative_calling.visibility = View.GONE
        initPage()
    }

    private var isMatch = false

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                transLayout.showProgress()
                mIsInComingCall = false
                OkClientHelper.post(this, "personality/match/1", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            running = false
                            /**
                             *  匹配
                             */
                            relation = -1
                            LocalLogUtils.writeLog("VoiceCall:用户进行了流星电话匹配", System.currentTimeMillis())
                            isDiscard = false
                            isMatch = true
                            if (null != dialog) {
                                if (dialog!!.isShowing)
                                    dialog!!.dismiss()
                            }
                            dialog = CallWaitDialog(this@VoiceConnectActivity).setCurrentType(0)
                            dialog?.show()
                            time = System.currentTimeMillis()
                            progressHelper.start()
                        } else {
                            showToast(result.msg)
                            DialogMatchEmpty(this@VoiceConnectActivity).setOnClickListener(View.OnClickListener {
                                finish()
                            }).show()
                            LocalLogUtils.writeLog("VoiceCall:请求匹配失败, ${result.msg} 服务器拒绝请求", System.currentTimeMillis())
                        }
                        transLayout.showContent()
                        sendObserver()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.0")
            }
            1 -> {
                OkClientHelper.delete(this, "personality/match/1", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            iscall = false
                            running = false
                            isMatch = false
                            LocalLogUtils.writeLog("VoiceCall:退出匹配队列", System.currentTimeMillis())
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
            2 -> {//查詢用戶信息, 展示用戶頭像昵稱
                OkClientHelper.get(this, "users/$fromUsreId", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            relation = result.data.relation_status
                            glideUtil.loadGlide(result.data.avatar_url, custom_Avatar, R.mipmap.icon_user_default, glideUtil.getLastModified(result.data.avatar_url))
                            tv_UserName.text = result.data.nick_name
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            3 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                if (!TextUtils.isEmpty(fromUsreId))
                    OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", FormBody.Builder().add("toUserId", fromUsreId).build(), BaseRepData::class.java, object : OkResponse {
                        override fun success(result: Any?) {
                            if ((result as BaseRepData).code == 0) {

                            } else {

                            }
                        }

                        override fun onFailure(any: Any?) {

                        }
                    })
            }
            4 -> {//匹配成功
                OkClientHelper.patch(this, "personality/match/1", FormBody.Builder().add("pingStatus", "1").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {

                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.5")
            }
            5 -> {//主动挂断
                OkClientHelper.patch(this, "personality/match/1", FormBody.Builder().add("pingStatus", "0").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        finish()
                    }

                    override fun onFailure(any: Any?) {
                        finish()
                    }
                }, "V3.5")
            }
        }
    }

    private fun received() {
        AVChatManager.getInstance().enableRtc()
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(false, false)
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer)
            AVChatManager.getInstance().setParameters(avChatConfigs?.avChatParameters)
        }
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true)
        AVChatManager.getInstance().accept2(avChatData?.chatId!!, object : AVChatCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {
                LocalLogUtils.writeLog("VoiceCall:流星电话接听成功", System.currentTimeMillis())
            }

            override fun onFailed(code: Int) {
                LocalLogUtils.writeLog("VoiceCall:流星电话接听失败$code", System.currentTimeMillis())
                exception()
            }

            override fun onException(exception: Throwable) {
                LocalLogUtils.writeLog("VoiceCall:流星电话接听出现异常${exception.message}", System.currentTimeMillis())
                exception()
            }
        })
        if (BaseObjectActivity.currentMode != 1 && BaseObjectActivity.currentMode != 3) {
            AVChatManager.getInstance().setSpeaker(true)
            tv_Speaker.text = resources.getString(R.string.string_voice_call_3)
            tv_Speaker.isSelected = true
            tv_Speaker.textSize = 12f
        } else {
            tv_Speaker.text = resources.getString(R.string.string_voice_call_4)
            tv_Speaker.textSize = 13f
            tv_Speaker.isSelected = false
        }
    }

    private fun registerObserves(register: Boolean) {
        AVChatManager.getInstance().observeAVChatState(avchatStateObserver, register)
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register)
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register)
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register)
        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register)
        //放到所有UI的基类里面注册，所有的UI实现onKickOut接口
        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus(userStatusObserver, register)
    }

    /**
     * ****************************** 监听器 **********************************
     */
    //自动挂断本地电话观察器  当电话来临占线
    private var autoHangUpForLocalPhoneObserver: Observer<Int> = Observer {
        LocalLogUtils.writeLog("VoiceCall:外来电话侵入, 主动挂断流星电话", System.currentTimeMillis())
        AVChatManager.getInstance().disableRtc()
        relative_calling.visibility = View.GONE
        relativeWait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        isCallEstablished = false
        btn_Back.visibility = View.VISIBLE
        hangUp()
        initPage()
    }

    /**
     * 注册/注销同时在线的其他端对主叫方的响应
     */
    private var onlineAckObserver: Observer<AVChatOnlineAckEvent> = Observer { ackInfo ->
        if (state == AVChatType.AUDIO.value) {
//            avChatData = avChatController.getAvChatData()

        } else {

//            avChatData = avChatVideoUI.getAvChatData()
        }
//        if (avChatData != null && avChatData.getChatId() == ackInfo.chatId) {
//            finish()
//        }
    }

    /**
     * 用户是否在线的监听
     */
    private var userStatusObserver: Observer<StatusCode> = Observer { code ->
        if (code.wontAutoLogin()) {
            Toast.makeText(this, "流星电话处理离线状态, 请退出重试", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    // 通话过程状态监听
    private val avchatStateObserver = object : SimpleAVChatStateObserver() {
        override fun onAudioMixingProgressUpdated(p0: Long, p1: Long) {

        }

        override fun onAVRecordingCompletion(account: String?, filePath: String?) {
        }

        override fun onAudioRecordingCompletion(filePath: String?) {
        }

        override fun onLowStorageSpaceWarning(availableSize: Long) {
        }

        override fun onJoinedChannel(code: Int, audioFile: String, videoFile: String, i: Int) {
            //handleWithConnectServerResult(code)
        }

        /**
         * 用户加入, 建立连接
         */
        override fun onUserJoined(account: String) {
            LocalLogUtils.writeLog("VoiceCall:对方加入了流星电话,建立了通道帐号:$account", System.currentTimeMillis())
            if (state == AVChatType.VIDEO.value) {
            }
            if (null != dialog) {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                    progressHelper.stop()
                }
            }
            try {
                fromUsreId = account.substring(account.lastIndexOf("_") + 1)
            } catch (e: Exception) {
            }
            request(2)
        }

        /**
         * 用户离开, 或者是直接关掉app
         */
        override fun onUserLeave(account: String, event: Int) {
            LocalLogUtils.writeLog("VoiceCall:用户离开了通道 帐号:$account 操作的指令:$event", System.currentTimeMillis())
            hangUp()
        }

        /**
         * 接通的监听  双方都走此监听
         */
        override fun onCallEstablished() {
            //移除超时监听
            LocalLogUtils.writeLog("VoiceCall:双方接通的监听:onCallEstablished", System.currentTimeMillis())
            /**
             * 开始倒计时
             * 5分钟后自动断开
             */
            allTime = 300
            isCallEstablished = true
            callTime = System.currentTimeMillis() + allTime * 1000
            if (null != dialog) {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                }
            }
            relative_calling.visibility = View.VISIBLE
            linear_Match.visibility = View.GONE
            relativeWait.visibility = View.GONE
//            if (iscall)
            progressHelper.postDelayed({
                if (isCallEstablished) {
                    LocalLogUtils.writeLog("VoiceCall:时间到达5分钟, 主动挂断电话", System.currentTimeMillis())
                    hangUp()
                }
            }, 5 * 60 * 1000L)
            btn_Back.visibility = View.GONE
            if (null != dialog) {
                if (dialog?.isShowing!!) {
                    dialog?.dismiss()
                    progressHelper.stop()
                }
            }
            progressHelper.start()
        }

        override fun onVideoFrameFilter(frame: AVChatVideoFrame, maybeDualInput: Boolean): Boolean {

            return true
        }

        override fun onAudioFrameFilter(frame: AVChatAudioFrame): Boolean {
            return true
        }
    }

    // 通话过程中，收到对方挂断电话  恢复等待界面, 排队时间过期
    private var callHangupObserver: Observer<AVChatCommonEvent> = Observer { avChatHangUpInfo ->
        if (avChatData != null && avChatData?.chatId == avChatHangUpInfo.chatId) {
            LocalLogUtils.writeLog("VoiceCall:流星电话被对方挂断", System.currentTimeMillis())
            hangUp()
        }
    }

    // 呼叫时，被叫方的响应（接听、拒绝、忙）
    private var callAckObserver: Observer<AVChatCalleeAckEvent> = Observer { ackInfo ->
        val info = avChatData
        if (info != null && info.chatId == ackInfo.chatId) {
            when {
                ackInfo.event == AVChatEventType.CALLEE_ACK_BUSY -> {//繁忙 未接
                    AVChatManager.getInstance().disableRtc()
                    LocalLogUtils.writeLog("VoiceCall:呼叫时，被叫方的响应（忙）", System.currentTimeMillis())
                    hangUp()
                }
                ackInfo.event == AVChatEventType.CALLEE_ACK_REJECT -> { //拒绝
                    LocalLogUtils.writeLog("VoiceCall:呼叫时，被叫方的响应（拒绝）", System.currentTimeMillis())
                    AVChatManager.getInstance().disableRtc()
                    hangUp()
                }
                ackInfo.event == AVChatEventType.CALLEE_ACK_AGREE -> {//接听
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAvchatEvent(event: OnAvChatInComing) {
        try {
            avChatData = event.data
            tv_call_topic.text = event.data.extra
            LocalLogUtils.writeLog("VoiceCall:用户接收到了流星电话的拨入", System.currentTimeMillis())
            received()
        } catch (e: Exception) {

        }
    }

    class OnAvChatInComing {
        var data: AVChatData

        constructor(data: AVChatData) {
            this.data = data
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(cast)
        progressHelper.removeCallbacks(progressHelper)
        progressHelper.removeCallbacksAndMessages(null)
        /**
         * 注销观察者
         */
        registerObserves(false)
    }

    private var preQuestTime = 0L

    /**
     * 正确时间，21：59分
     */
    private inner class TickBroadCast : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            /**
             * 当前的时间
             */
            val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
            if (hour == 21 && min == 0) {
                try {
                    if (!isMatch) {
                        initPage()
                    }
                    if (!isCallEstablished && isMatch) {
                        if (!running) {
                            running = true
                            delayTask(0)
                        }
                    }
                } catch (e: Exception) {
                }
            } else if (hour == 20 && min == 59) {
                isMatch = false
                countTime = 60
                relativeWait.visibility = View.GONE
                linear_Match.visibility = View.VISIBLE
                // 开始一分钟的倒计时
                tv_TimeTick.base = SystemClock.elapsedRealtime()
                tv_TimeTick.start()
            }
            /*if (min.rem(5) == 0) {
                try {
                    if (!isMatch) {
                        initPage()
                    }
                    if (!isCallEstablished && isMatch) {
                        if (!running) {
                            running = true
                            LocalLogUtils.writeLog("VoiceCall:使用广播监听轮询结果", System.currentTimeMillis())
                            delayTask(0)
                        }
                    }
                } catch (e: Exception) {
                }
            } else if (min.rem(5) == 4) {
                isMatch = false
                countTime = 60
                relativeWait.visibility = View.GONE
                linear_Match.visibility = View.VISIBLE
                //开始一分钟的倒计时
                tv_TimeTick.base = SystemClock.elapsedRealtime()
                tv_TimeTick.start()
            }*/
        }
    }

    @Synchronized
    private fun delayTask(offsetTime: Long) {
        preQuestTime = System.currentTimeMillis() + offsetTime
        LocalLogUtils.writeLog("VoiceCall:开始接口轮询请求匹配结果", System.currentTimeMillis())
        relativeWait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        progressHelper.postDelayed({
            if (null != dialog) {
                if (dialog!!.isShowing) {
                    loopResult()
                }
            }
        }, 2000)
        tv_TimeTick.base = SystemClock.elapsedRealtime()
        tv_TimeTick.stop()
        progressHelper.postDelayed({
            /**
             *给10s 的延迟时间, 避免出现注册的监听获取不到
             */
            if (!isCallEstablished) {//未接通
                if (!isDestroyed) {
                    if (null != dialog) {
                        if (dialog!!.isShowing) {
                            dialog!!.dismiss()
                            progressHelper.stop()
                            if (!isCallEstablished) {//未接通
                                relativeWait.visibility = View.VISIBLE
                                linear_Match.visibility = View.GONE
                                initPage()
                                DialogMatchEmpty(this@VoiceConnectActivity).setOnClickListener(View.OnClickListener {
                                    request(5)
                                }).show()
                                LocalLogUtils.writeLog("VoiceCall:匹配结束,等待10s之后没有任何结果", System.currentTimeMillis())
                            }
                            request(1)
                            AVChatManager.getInstance().disableRtc()
                        }
                    }
                }
            }
            isMatch = false
            running = false
        }, 15000 + offsetTime)
    }


    /**
     * 整点过一秒,接口请求, 确保socket断线或者服务宕机， 推送延迟等潜在问题   只请求10s
     * 每次请求此接口 延迟1-2 s
     * 一旦socket 先请求到数据 则此请求作废 反之
     * 同步单线程
     */
    @Volatile
    private var running = false

    @Synchronized
    private fun loopResult() {
        OkClientHelper.get(this, "personality/match/1", SocketData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                Log.d("Mozator", "请求一次结果")
                result as SocketData
                LocalLogUtils.writeLog("VoiceCall:${result.code}+${result.data?.let { result.data.yunxin_id + result.data.from_user_id }}", System.currentTimeMillis())
                if (result.code == 0 && !isDiscard) {
                    if (System.currentTimeMillis() - preQuestTime <= 10000) {
                        if (null != result.data && !TextUtils.isEmpty(result.data.yunxin_id)) {
                            account = result.data.yunxin_id
                            if (!mIsInComingCall) {
                                call(result.data.talking_topic.topic_name)
                            }
                        } else {

                            if (!isCallEstablished) {
                                progressHelper.postDelayed({
                                    if (!isDiscard) {
                                        if (null != dialog && dialog!!.isShowing) {
                                            LocalLogUtils.writeLog("VoiceCall:重复请求匹配结果", System.currentTimeMillis())
                                            loopResult()
                                        }
                                    }
                                }, 2000)
                            }
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.0")
    }

    override fun finish() {
        super.finish()
        try {
            if (avChatData != null)
                AVChatManager.getInstance().hangUp2(avChatData?.chatId!!, object : AVChatCallback<Void?> {
                    override fun onSuccess(aVoid: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable) {

                    }
                })
            AVChatManager.getInstance().disableRtc()
        } catch (e: Exception) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMatcher(event: OnMatchUserEvent) {
        /**
         * 获取到匹配的用户,开始拨打电话
         */
        isDiscard = true
        if (!TextUtils.isEmpty(event.bean.yunxin_id)) {
            account = event.bean.yunxin_id
            call(event.bean.talking_topic.topic_name)
            LocalLogUtils.writeLog("VoiceCall:用户收到流星电话拨打通知 socket$account      ", System.currentTimeMillis())
        } else {//没有匹配到用户
            if (null != dialog) {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                    progressHelper.stop()
                }
            }
            relativeWait.visibility = View.VISIBLE
            linear_Match.visibility = View.GONE
            initPage()
            DialogMatchEmpty(this).setOnClickListener(View.OnClickListener {
                request(5)
            }).show()
            LocalLogUtils.writeLog("VoiceCall:本次流星电话没有匹配到人socket", System.currentTimeMillis())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTRECORD) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isCallEstablished) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    class OnMatchUserEvent(var bean: SocketData.SocketBean)
}