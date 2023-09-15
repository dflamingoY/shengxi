package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.SystemClock
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
import com.netease.nimlib.sdk.auth.ClientType
import com.netease.nimlib.sdk.rts.RTSCallback
import com.netease.nimlib.sdk.rts.RTSChannelStateObserver
import com.netease.nimlib.sdk.rts.RTSManager
import com.netease.nimlib.sdk.rts.constant.RTSEventType
import com.netease.nimlib.sdk.rts.constant.RTSTimeOutEvent
import com.netease.nimlib.sdk.rts.constant.RTSTunnelType
import com.netease.nimlib.sdk.rts.model.*
import kotlinx.android.synthetic.main.activity_magic_canvas.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
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
import org.xiaoxingqi.shengxi.wedgit.doodle.DoodleView
import org.xiaoxingqi.shengxi.wedgit.doodle.SupportActionType
import org.xiaoxingqi.shengxi.wedgit.doodle.TransactionCenter
import org.xiaoxingqi.shengxi.wedgit.doodle.action.MyPath
import org.xiaoxingqi.yunxin.rts.ActionTypeEnum
import skin.support.SkinCompatManager
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

/**
 * 魔法画板
 */
class MagicCanvasActivity : BaseNormalActivity() {
    private var sessionId: String? = null
    private var account: String? = null
    private var fromUserId: String? = null
    private var relation = -1//未给改字段赋值, 表示未连接成功过
    private var isCall = false//是否拨打
    private var cast: TickBroadCast? = null
    private var dialog: CallWaitDialog? = null
    private var time = 0L
    private var isConnecting = false//是否是连接状态
    private var connectTime = 0L
    private var isDiscard = false//是否放弃所有任务
    private val progressHelper = @SuppressLint("HandlerLeak")
    object : ProgressHelper() {
        override fun handleMessage(msg: Message?) {
            if (null != dialog) {
                if (dialog!!.isShowing)
                    dialog?.timeTick(((System.currentTimeMillis() - time) / 1000).toInt())
            }
            if (isConnecting) {
                var dTime = connectTime - System.currentTimeMillis()
                if (dTime < 0) {
                    dTime = 0
                }
                tv_Timer.text = resources.getString(R.string.string_canvas_1) + AppTools.parseTime2Str(dTime)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_magic_canvas
    }

    override fun initView() {
        window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        val statusBarHeight = AppTools.getStatusBarHeight(this)
        val params = view_status_bar.layoutParams
        params.height = statusBarHeight
        view_status_bar.layoutParams = params
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                || OsUtil.isMIUI()
                || OsUtil.isFlyme()) {
//            setStatusBarFontIconDark(true)
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                view_status_bar.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {
                view_status_bar.setBackgroundColor(Color.parseColor("#00ffffff"))
            }
        }
        (loading.drawable as AnimationDrawable).start()
        try {
            setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            tv_Desc.text = resources.getString(R.string.string_magic_canvas_e_1)
            tv_Desc1.text = resources.getString(R.string.string_magic_canvas_e_2)
        }
        initPage()
        /*progressHelper.postDelayed({
            linear_Match.visibility = View.VISIBLE
            relative_Wait.visibility = View.GONE
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

        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus(userStatusObserver, true)
//        request(1)
    }

    private fun initPage() {
        relative_Wait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        iv_card.visibility = View.VISIBLE
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        /**
         * 获取当前时间
         */
        calendar.set(Calendar.HOUR_OF_DAY, 21)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var dtime = calendar.timeInMillis - System.currentTimeMillis()
        if (dtime < 0) {
            dtime += 24 * 60 * 60 * 1000
        }
        countDown.start(dtime)
        /**
         * 當前時間是否在 22:59- 22:00 之間
         */
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        if (hour == 21 && min == 59 && second <= 58) {//当前是 22:59 分 仍可以匹配
            relative_Wait.visibility = View.GONE
            linear_Match.visibility = View.VISIBLE
            countTime = 60 - second
            tv_TimeTick.base = SystemClock.elapsedRealtime()
            tv_TimeTick.start()
        }
        btn_Back.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (linear_Match.visibility == View.VISIBLE) {
            if (countTime > 0) {
                countTime -= ((SystemClock.elapsedRealtime() - tv_TimeTick.base) / 1000).toInt()
            }
        }
        if (!isConnecting) {//没有接通的状态, 进来时都初始化界面
            initPage()
        }
        if (isMatch) {//正在匹配时,
            LocalLogUtils.writeLog("MagicCanvas:onResume 正在匹配中", System.currentTimeMillis())
            if (null != dialog && dialog!!.isShowing) {
                val difference = getDifference()
                LocalLogUtils.writeLog("MagicCanvas:onResume 正在匹配中  获取当前的时间差  $difference", System.currentTimeMillis())
                if (difference <= 0 && difference >= -10000) {
                    if (!isConnecting && isMatch) {
                        if (!running) {
                            LocalLogUtils.writeLog("MagicCanvas:onResume 查询结果", System.currentTimeMillis())
                            running = true
                            delayTask(difference)
                        }
                    }
                } else if (difference < -10000) {
                    LocalLogUtils.writeLog("MagicCanvas:onResume 超过10s 不在查询结果", System.currentTimeMillis())
                    isDiscard = true
                    dialog!!.dismiss()
                    progressHelper.stop()
                    if (!isConnecting) {//未接通
                        relative_Wait.visibility = View.VISIBLE
                        linear_Match.visibility = View.GONE
                        initPage()
                        DialogMatchEmpty(this@MagicCanvasActivity).setOnClickListener(View.OnClickListener {
                            request(5)
                        }).show()
                        LocalLogUtils.writeLog("MagicCanvas:匹配结束,onResume等待超时", System.currentTimeMillis())
                    }
                    request(1)
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
        cal.set(Calendar.HOUR_OF_DAY, 22)//23
        cal.set(Calendar.MINUTE, 0)//0
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis - System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        doodle_view.onPause()
        if (isConnecting) {
            LocalLogUtils.writeLog("MagicCanvas:匹配结束,onPause 连接过程中退到后台", System.currentTimeMillis())
        }
        tv_TimeTick.base = SystemClock.elapsedRealtime()
    }

    override fun initEvent() {
        tv_Match.setOnClickListener {
            transLayout.showProgress()
            request(0)
        }
        btn_close.setOnClickListener {
            DialogDismissConnect(this).setOnClickListener(View.OnClickListener {
                LocalLogUtils.writeLog("MagicCanvas:用户主动挂断 ", System.currentTimeMillis())
                request(1)
                endSession() // 挂断
            }).show()
        }
        btn_clear.setOnClickListener {
            try {
                LocalLogUtils.writeLog("MagicCanvas:用户点击了清屏", System.currentTimeMillis())
                doodle_view.clear()
            } catch (e: Exception) {
            }
        }
        btn_ReBack.setOnClickListener {
            try {
                doodle_view.paintBack()
            } catch (e: Exception) {

            }
        }
        btn_Back.setOnClickListener {
            finish()
        }
        tv_TimeTick.setOnChronometerTickListener {
            tv_TimeTick.text = "${countTime}秒"
            countTime--
            if (countTime < 0) {
                countTime = 0
            }
        }
        tv_change_set_1.setOnClickListener { startActivity(Intent(this, VoiceCallSetActivity::class.java).putExtra("type", 2)) }
        tv_change_set.setOnClickListener { startActivity(Intent(this, VoiceCallSetActivity::class.java).putExtra("type", 2)) }
    }

    private var countTime = 60
    private var isMatch = false
    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                transLayout.showProgress()
                isCall = false
                OkClientHelper.post(this, "personality/match/2", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        sendObserver()
                        result as BaseRepData
                        if (result.code == 0) {
                            running = false
                            /**
                             *  匹配
                             */
                            LocalLogUtils.writeLog("MagicCanvas:用户开始匹配", System.currentTimeMillis())
                            isDiscard = false
                            isMatch = true
                            if (null != dialog) {
                                if (dialog!!.isShowing)
                                    dialog!!.dismiss()
                            }
                            dialog = CallWaitDialog(this@MagicCanvasActivity).setCurrentType(1)
                            dialog?.show()
                            time = System.currentTimeMillis()
                            progressHelper.start()

                        } else {
                            showToast(result.msg)
                            DialogMatchEmpty(this@MagicCanvasActivity).setOnClickListener(View.OnClickListener {
                                finish()
                            }).show()
                            LocalLogUtils.writeLog("MagicCanvas:请求匹配失败, ${result.msg} 服务器拒绝请求", System.currentTimeMillis())
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.0")
            }
            1 -> {
                OkClientHelper.delete(this, "personality/match/2", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            isMatch = false
                            running = false
                            LocalLogUtils.writeLog("MagicCanvas:退出匹配队列", System.currentTimeMillis())
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
            2 -> {
                OkClientHelper.get(this, "users/$fromUserId", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            relation = result.data.relation_status
                            glideUtil.loadGlide(result.data.avatar_url, iv_Avatar, R.mipmap.icon_user_default, glideUtil.getLastModified(result.data.avatar_url))
                            tv_UserName.text = result.data.nick_name
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            3 -> {//添加好友
                val formBody = FormBody.Builder()
                        .add("toUserId", fromUserId)
                        .build()
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
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
                OkClientHelper.patch(this, "personality/match/2", FormBody.Builder().add("pingStatus", "1").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {

                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.5")
            }
            5 -> {//主动挂断
                OkClientHelper.patch(this, "personality/match/2", FormBody.Builder().add("pingStatus", "0").build(), BaseRepData::class.java, object : OkResponse {
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

    /**
     * 主接受的监听
     */
    private fun registerInComingObserver(register: Boolean) {
        if (!TextUtils.isEmpty(sessionId))
            RTSManager.getInstance().observeOnlineAckNotification(sessionId, onlineAckObserver, register)
    }

    /**
     * 主拨打的监听
     */
    private fun registerOutgoingObserver(register: Boolean) {
        if (!TextUtils.isEmpty(sessionId))
            RTSManager.getInstance().observeCalleeAckNotification(sessionId, calleeAckEventObserver, register)
    }

    /**
     * 必须是收到sessionId 时,触发监听
     */
    private fun registerCommonObserver(register: Boolean) {
        if (!TextUtils.isEmpty(sessionId)) {
            RTSManager.getInstance().observeChannelState(sessionId, channelStateObserver, register)
            RTSManager.getInstance().observeHangUpNotification(sessionId, endSessionObserver, register)
            RTSManager.getInstance().observeReceiveData(sessionId, receiveDataObserver, register)
            RTSManager.getInstance().observeTimeoutNotification(sessionId, timeoutObserver, register)
            RTSManager.getInstance().observeControlNotification(sessionId, controlObserver, register)
        }
    }

    /**
     * 被叫方监听在线其他端的接听响应
     */
    private val onlineAckObserver = Observer<RTSOnlineAckEvent> { rtsOnlineAckEvent ->
        if (rtsOnlineAckEvent.clientType.toInt() != ClientType.Android) {
            var client: String? = null
            when (rtsOnlineAckEvent.clientType.toInt()) {
                ClientType.Web -> client = "Web"
                ClientType.Windows -> client = "Windows"
                ClientType.MAC -> client = "Mac"
                else -> {
                }
            }
            if (client != null) {
                val option = if (rtsOnlineAckEvent.event == RTSEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE)
                    "接受"
                else
                    "拒绝"
            } else {
            }
            finish()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        // 这里需要重绘
        try {
            doodle_view.onResume()
        } catch (e: Exception) {
        }
    }

    /**
     * 检测云信是否登录在线
     */
    private var userStatusObserver: Observer<StatusCode> = Observer { code ->
        if (code.wontAutoLogin()) {
            Toast.makeText(this, "画板功能处于离线状态, 请退出重试", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * 主叫方监听被叫方的接受or拒绝会话的响应
     */
    private val calleeAckEventObserver = Observer<RTSCalleeAckEvent> { rtsCalleeAckEvent ->
        //        showToast("收到回拨")
        if (rtsCalleeAckEvent.event == RTSEventType.CALLEE_ACK_AGREE) {
            // 判断SDK自动开启通道是否成功
            if (!rtsCalleeAckEvent.isTunReady) {
                //Toast.makeText(this@MagicCanvasActivity, "通道开启失败!请查看LOG", Toast.LENGTH_SHORT).show()
                return@Observer
            }
            initDoodleView(true)
        } else if (rtsCalleeAckEvent.event == RTSEventType.CALLEE_ACK_REJECT) {
            //Toast.makeText(this@MagicCanvasActivity, "对方拒绝了请求", Toast.LENGTH_SHORT).show()
            LocalLogUtils.writeLog("MagicCanvas:对方拒绝了请求 ", System.currentTimeMillis())
            endSession()
        }
    }

    /**
     * @isReceived  是否是接收方
     */
    private fun initDoodleView(isReceived: Boolean) {
        linear_Match.visibility = View.GONE
        relative_Wait.visibility = View.GONE
        session_layout.visibility = View.VISIBLE
        btn_Back.visibility = View.GONE
        (loading.drawable as AnimationDrawable).stop()
        loading.visibility = View.GONE
        iv_card.visibility = View.GONE
        // add support ActionType
        SupportActionType.getInstance().addSupportActionType(ActionTypeEnum.Path.value, MyPath::class.java)

        doodle_view.init(sessionId, account, DoodleView.Mode.BOTH, Color.WHITE, this)
        doodle_view.isReceived(isReceived)
        doodle_view.setPaintSize(AppTools.dp2px(this, 2))
        doodle_view.setPaintType(ActionTypeEnum.Path.value)

        // adjust paint offset
        Handler(mainLooper).postDelayed(Runnable {
            val frame = Rect()
            window.decorView.getWindowVisibleDisplayFrame(frame)
            val statusBarHeight = frame.top
            Log.i("Doodle", "statusBarHeight =$statusBarHeight")

            val marginTop = doodle_view.top
            Log.i("Doodle", "doodle_view marginTop =$marginTop")

            val marginLeft = doodle_view.left
            Log.i("Doodle", "doodle_view marginLeft =$marginLeft")

            val offsetX = marginLeft.toFloat()
//            val offsetY = (statusBarHeight + marginTop).toFloat()
            val offsetY = AppTools.dp2px(this, 70) + AppTools.getStatusBarHeight(this)

            doodle_view.setPaintOffset(offsetX, offsetY.toFloat())
            Log.i("Doodle", "client1 offsetX = $offsetX, offsetY = $offsetY")
        }, 50)
    }

    @Synchronized
    private fun call(extendMessage: String) {
        if (!isCall)
            synchronized(this) {
                if (!isCall) {
                    request(4)
                    isCall = true
                    val notifyOption = RTSNotifyOption()
                    notifyOption.apnsContent = ""
                    notifyOption.extendMessage = extendMessage
                    tv_canvas_topic.text = resources.getString(R.string.string_canvas_topics_1) + "：" + extendMessage
                    sessionId = RTSManager.getInstance().start(account, arrayListOf(RTSTunnelType.DATA/*,RTSTunnelType.AUDIO*/), RTSOptions().setRecordAudioTun(false)
                            .setRecordDataTun(true), notifyOption, object : RTSCallback<RTSData> {
                        override fun onSuccess(rtsData: RTSData) {
                            LocalLogUtils.writeLog("MagicCanvas:白板拨打成功 ", System.currentTimeMillis())
                        }

                        override fun onFailed(code: Int) {
                            if (code == 11001) {
                                //Toast.makeText(this@MagicCanvasActivity, "无可送达的被叫方", Toast.LENGTH_SHORT).show()
                            } else {
                                //Toast.makeText(this@MagicCanvasActivity, "发起会话失败,code=$code", Toast.LENGTH_SHORT).show()
                            }
                            LocalLogUtils.writeLog("MagicCanvas:白板拨打失败$code ", System.currentTimeMillis())
                            exception()
                        }

                        override fun onException(exception: Throwable) {
                            //Toast.makeText(this@MagicCanvasActivity, "发起会话异常,e=$exception", Toast.LENGTH_SHORT).show()
                            LocalLogUtils.writeLog("MagicCanvas:白板拨打异常${exception.message} ", System.currentTimeMillis())
                            exception()
                        }
                    })
                    RTSManager.getInstance().setMute(sessionId, true)
                    registerOutgoingObserver(true)
                    registerCommonObserver(true)
                }
            }
    }

    /**
     * 出现云信的错误走此方法, 初始化界面
     *
     * 拨打失败, 拨打异常 接听失败, 接听异常
     */
    private fun exception() {
        if (null != dialog) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
        session_layout.visibility = View.GONE
        initPage()
    }

    /**
     * 监听控制消息
     */
    private val controlObserver = Observer<RTSControlEvent> { rtsControlEvent ->
        //Toast.makeText(this@MagicCanvasActivity, rtsControlEvent.commandInfo, Toast.LENGTH_SHORT).show()
        LocalLogUtils.writeLog("MagicCanvas:监听控制消息 controlObserver${rtsControlEvent.commandInfo}", System.currentTimeMillis())
    }
    /**
     * 超时监听
     */
    private val timeoutObserver = Observer<RTSTimeOutEvent> {
        LocalLogUtils.writeLog("MagicCanvas:对方延迟未接受请求,timeout", System.currentTimeMillis())
        if (null != dialog) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
        DialogMatchEmpty(this).setOnClickListener(View.OnClickListener {
            request(5)
        }).show()
        endSession()
    }

    /**
     * 监听对方挂断
     */
    private val endSessionObserver = Observer<RTSCommonEvent> {
        /**
         * 弹窗提示加好友
         */
        LocalLogUtils.writeLog("MagicCanvas:对方主动挂断,获取到回调", System.currentTimeMillis())
        relative_Wait.visibility = View.VISIBLE
        linear_Match.visibility = View.GONE
        btn_Back.visibility = View.VISIBLE
        session_layout.visibility = View.GONE
        loading.visibility = View.VISIBLE
        (loading.drawable as AnimationDrawable).start()
        if (relation == 0) {
            if (!isDestroyed)
                DialogAddFriends(this).setOnClickListener(View.OnClickListener {
                    /**
                     * 添加好友
                     */
                    request(3)

                }).show()
        }
        isConnecting = false
        initPage()
        RTSManager.getInstance().close(sessionId, null)
        doodle_view.end()
        isMatch = false
    }


    /**
     * 监听收到对方发送的通道数据
     */
    private val receiveDataObserver = Observer<RTSTunData> { rtsTunData ->
        progressHelper.post {
            var data = "[parse bytes error]"
            try {
                data = String(rtsTunData.data, 0, rtsTunData.length, Charset.forName("UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            TransactionCenter.getInstance().onReceive(sessionId, data)
        }
    }

    /**
     * 监听当前会话的状态
     */
    private val channelStateObserver = object : RTSChannelStateObserver {

        override fun onConnectResult(sessionId: String, tunType: RTSTunnelType, channelId: Long, code: Int, file: String) {
            try {
                //Toast.makeText(this@MagicCanvasActivity, "onConnectResult, tunType=" + tunType.toString() +
//                        ", channelId=" + channelId +
//                        ", code=" + code + ", file=" + file, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onChannelEstablished(sessionId: String, tunType: RTSTunnelType) {
            try {
//                Toast.makeText(this@MagicCanvasActivity, "onCallEstablished,tunType=$tunType", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (tunType == RTSTunnelType.AUDIO) {
                RTSManager.getInstance().setSpeaker(sessionId, true) // 默认开启扬声器
            }
        }

        override fun onUserJoin(sessionId: String, tunType: RTSTunnelType, account: String) {
            /**
             * 查询对方的信息
             */
            isConnecting = true
            try {
                fromUserId = account.substring(account.lastIndexOf("_") + 1)
            } catch (e: Exception) {
            }
            LocalLogUtils.writeLog("MagicCanvas:用户加入通道$fromUserId", System.currentTimeMillis())
            if (null != dialog) {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                    progressHelper.stop()
                }
            }
            request(2)
            connectTime = System.currentTimeMillis() + 300 * 1000
            progressHelper.start()
            progressHelper.postDelayed({
                if (isConnecting) {
                    LocalLogUtils.writeLog("MagicCanvas:连线时间到达最大,后台挂断", System.currentTimeMillis())
                    endSession()
                }
            }, 5 * 60 * 1000L)
        }

        override fun onUserLeave(sessionId: String, tunType: RTSTunnelType, account: String, event: Int) {
            //双人直接关闭会话
            LocalLogUtils.writeLog("MagicCanvas:用户离开了 $account", System.currentTimeMillis())
            endSession()
        }

        override fun onDisconnectServer(sessionId: String, tunType: RTSTunnelType) {
//            Toast.makeText(this@MagicCanvasActivity, "onDisconnectServer, tunType=$tunType", Toast
//                    .LENGTH_SHORT).show()
            if (tunType == RTSTunnelType.DATA) {
                // 如果数据通道断了，那么关闭会话
//                Toast.makeText(this@MagicCanvasActivity, "TCP通道断开，自动结束会话", Toast.LENGTH_SHORT).show()
                LocalLogUtils.writeLog("MagicCanvas:魔法画板断开连接onDisconnectServer", System.currentTimeMillis())
                endSession()
            } else if (tunType == RTSTunnelType.AUDIO) {
                // 如果音频通道断了，那么UI变换
            }
        }

        override fun onError(sessionId: String, tunType: RTSTunnelType, code: Int) {
//            Toast.makeText(this@MagicCanvasActivity, "onError, tunType=$tunType, error=$code",
//                    Toast.LENGTH_LONG).show()
            LocalLogUtils.writeLog("MagicCanvas:魔法画板断开连接onError 通道错误", System.currentTimeMillis())
            endSession()
        }

        override fun onNetworkStatusChange(sessionId: String, tunType: RTSTunnelType, value: Int) {
            // 网络信号强弱
            LocalLogUtils.writeLog("MagicCanvas:魔法画板网络信号差", System.currentTimeMillis())
        }
    }

    /**
     * 关闭会话
     */
    private fun endSession() {
        progressHelper.stop()
        if (!TextUtils.isEmpty(sessionId)) {
            RTSManager.getInstance().close(sessionId, object : RTSCallback<Void?> {

                override fun onSuccess(aVoid: Void?) {
//                    showToast("关闭会话")
                    if (relation == 0) {
                        if (!isDestroyed)
                            DialogAddFriends(this@MagicCanvasActivity).setOnClickListener(View.OnClickListener {
                                /**
                                 * 添加好友
                                 */
                                request(3)
                            }).show()
                    }
                }

                override fun onFailed(code: Int) {
                    LocalLogUtils.writeLog("MagicCanvas:魔法画板挂断错误 $code", System.currentTimeMillis())
                }

                override fun onException(exception: Throwable) {
                    LocalLogUtils.writeLog("MagicCanvas:魔法画板挂断异常", System.currentTimeMillis())
                }
            })
            /**
             * 弹窗提示加好友
             */
            relative_Wait.visibility = View.VISIBLE
            linear_Match.visibility = View.GONE
            session_layout.visibility = View.GONE
            loading.visibility = View.VISIBLE
            btn_Back.visibility = View.VISIBLE
            (loading.drawable as AnimationDrawable).start()
            initPage()
            isConnecting = false
        }
        doodle_view.end()
        isMatch = false
    }

    private fun acceptSession() {
        val options = RTSOptions().setRecordAudioTun(false).setRecordDataTun(true)
        RTSManager.getInstance().accept(sessionId, options, object : RTSCallback<Boolean> {
            override fun onSuccess(success: Boolean?) {
                // 判断开启通道是否成功
                if (success!!) {
                    initDoodleView(false)
                    LocalLogUtils.writeLog("MagicCanvas:魔法画板接听成功", System.currentTimeMillis())
                } else {
                }
            }

            override fun onFailed(code: Int) {
                LocalLogUtils.writeLog("MagicCanvas:魔法画板接听失败$code", System.currentTimeMillis())
                exception()
            }

            override fun onException(exception: Throwable) {
                LocalLogUtils.writeLog("MagicCanvas:魔法画板接听异常${exception.message}", System.currentTimeMillis())
                exception()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMatchEvent(event: OnMatchCanvas) {
        /**
         * 收到通知的时候主动拨打画板
         */
        isDiscard = true
        if (!TextUtils.isEmpty(event.bean.yunxin_id)) {
            account = event.bean.yunxin_id
            LocalLogUtils.writeLog("MagicCanvas:魔法画板收到匹配结果 socket $account", System.currentTimeMillis())
            call(event.bean.talking_topic.topic_name)
        } else {//没有匹配到用户
            if (dialog != null)
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                    progressHelper.stop()
                }
            relative_Wait.visibility = View.VISIBLE
            linear_Match.visibility = View.GONE
            initPage()
            LocalLogUtils.writeLog("MagicCanvas:本次没有匹配到人 socket", System.currentTimeMillis())
            DialogMatchEmpty(this).setOnClickListener(View.OnClickListener {
                request(5)
            }).show()
        }
    }

    class OnMatchCanvas {
        var bean: SocketData.SocketBean

        constructor(bean: SocketData.SocketBean) {
            this.bean = bean
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIncomingEvent(event: OnIncomingObserver) {
        /**
         * 收到白板的通知  主动接听
         */
        tv_canvas_topic.text = resources.getString(R.string.string_canvas_topics_1) + "：" + event.data.extra
        LocalLogUtils.writeLog("MagicCanvas:收到魔法画板的拨入", System.currentTimeMillis())
        account = event.data.account
        sessionId = event.data.localSessionId
        RTSManager.getInstance().setMute(sessionId, true)
        registerInComingObserver(true)
        registerCommonObserver(true)
        acceptSession()
    }

    class OnIncomingObserver {
        var data: RTSData

        constructor(data: RTSData) {
            this.data = data
        }
    }

    override fun onDestroy() {
        doodle_view.end()
        super.onDestroy()
        unregisterReceiver(cast)
        if (!TextUtils.isEmpty(sessionId)) {
            RTSManager.getInstance().close(sessionId, null)
        }
        registerOutgoingObserver(false)
        registerCommonObserver(false)
        registerInComingObserver(false)
        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus(userStatusObserver, false)
        progressHelper.removeCallbacksAndMessages(null)
    }

    /**
     * 整点过一秒,接口请求, 确保socket断线或者服务宕机， 推送延迟等潜在问题   只请求10s
     * 每次请求此接口 延迟1-2 s
     * 一旦socket 先请求到数据 则此请求作废 反之
     */
    private fun loopResult() {
        OkClientHelper.get(this, "personality/match/2", SocketData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SocketData
                LocalLogUtils.writeLog("MagicCanvas:${result.data?.let { result.data.yunxin_id + result.data.from_user_id }}", System.currentTimeMillis())
                if (result.code == 0 && !isDiscard) {
                    if (System.currentTimeMillis() - preQuestTime < 10000) {
                        if (null != result.data && !TextUtils.isEmpty(result.data.yunxin_id)) {
                            account = result.data.yunxin_id
                            if (!isCall)
                                call(result.data.talking_topic.topic_name)
                        } else {
                            if (!isCall)
                                progressHelper.postDelayed({
                                    if (!isDiscard) {
                                        if (null != dialog && dialog!!.isShowing) {
                                            LocalLogUtils.writeLog("MagicCanvas:重复请求匹配结果", System.currentTimeMillis())
                                            loopResult()
                                        }
                                    }
                                }, 2000)
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.0")
    }

    private var preQuestTime = 0L

    private inner class TickBroadCast : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            /**
             * 当前的时间
             */
            val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
            if (hour == 22 && min == 0) {
                try {
                    if (!isMatch) {
                        initPage()
                    }
                    if (session_layout.visibility != View.VISIBLE && isMatch) {
                        if (!running) {
                            running = true
                            delayTask(0)
                        }
                    }
                } catch (e: Exception) {
                }
            } else if (hour == 21 && min == 59) {
                isMatch = false
                countTime = 60
                relative_Wait.visibility = View.GONE
                linear_Match.visibility = View.VISIBLE
                // 开始一分钟的倒计时
                tv_TimeTick.base = SystemClock.elapsedRealtime()
                tv_TimeTick.start()
            }
            /* if (min.rem(5) == 0) {
                 try {
                     if (!isMatch) {
                         initPage()
                     }
                     if (session_layout.visibility != View.VISIBLE) {
                         if (!running) {
                             running = true
                             delayTask(0)
                         }
                     }
                 } catch (e: Exception) {
                 }
             } else if (min.rem(5) == 4) {
                 isMatch = false
                 countTime = 60
                 relative_Wait.visibility = View.GONE
                 linear_Match.visibility = View.VISIBLE
                 //开始一分钟的倒计时
                 tv_TimeTick.base = SystemClock.elapsedRealtime()
                 tv_TimeTick.start()
             }*/
        }
    }

    @Volatile
    private var running = false

    @Synchronized
    private fun delayTask(offsetTime: Long) {
        preQuestTime = System.currentTimeMillis() + offsetTime
        linear_Match.visibility = View.GONE
        relative_Wait.visibility = View.VISIBLE
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
            if (!isConnecting) {
                if (null != dialog) {
                    if (dialog!!.isShowing) {
                        dialog!!.dismiss()
                        progressHelper.stop()
                        relative_Wait.visibility = View.VISIBLE
                        linear_Match.visibility = View.GONE
                        session_layout.visibility = View.GONE
                        initPage()
                        if (!isDestroyed)
                            DialogMatchEmpty(this@MagicCanvasActivity).setOnClickListener(View.OnClickListener {
                                request(5)
                            }).show()
                    }
                    LocalLogUtils.writeLog("MagicCanvas:匹配结束,等待10s之后没有任何结果", System.currentTimeMillis())
                }
                request(1)
            }
            isMatch = false
            running = false
        }, 15000 + offsetTime)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isConnecting) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}