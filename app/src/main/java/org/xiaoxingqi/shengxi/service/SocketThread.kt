package org.xiaoxingqi.shengxi.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.SocketData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.listen.MagicCanvasActivity
import org.xiaoxingqi.shengxi.modules.listen.VoiceConnectActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.utils.download.DownLoadPool
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper

/**
 * 创建socket服务
 * 发送心跳包
 * 解析socket数据
 * 发送EventBus
 * 更改服务的了解对象
 * startForegroundService  启动失败, 要设置为前台服务 anr
 * 设置为 startForeground
 */
class SocketThread private constructor() {

    companion object {
        private var context: Context? = null

        private val instance by lazy { SocketThread() }
        fun instances(context: Context): SocketThread {
            this.context = context
            return instance
        }
    }

    private var webSocket: WebSocket? = null
    private var retryCount = 0
    private val progress = @SuppressLint("HandlerLeak")
    object : ProgressHelper(5000) {
        override fun handleMessage(msg: Message?) {
            webSocket?.send("{\"flag\":\"ping\"}")
        }
    }
    private val retryTask = Retry()
    private var cacheMsg: String? = null
    private var isCircleSend = false//是否验证消息发送成功
    private var checkMsgTask = SocketSendBecomeSuccess()
    private var checkCacheMsg: String? = null
    private var checkStatus = SocketCheckStatus()

    init {
        EventBus.getDefault().register(this)
        onStartCommand()
    }

    fun onStartCommand() {
        initSocket()
    }

    //    private fun initSocket() {}
    private fun initSocket() {
        progress.stop()
        webSocket?.let {
            it.close(1000, "close")
            webSocket = null
        }
        LocalLogUtils.writeLog("SocketServer: initSocket", System.currentTimeMillis())
        if (AppTools.isNetOk(context)) {
            try {
                val infoData = PreferenceTools.getObj(context, IConstant.USERCACHE, UserInfoData::class.java)
                val loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                if (loginBean.user_id == infoData.data.user_id) {
                    val client = OkClientHelper.getClient()
                    val request = Request.Builder()
                            .url(IConstant.SOCKETPORT)
                            .addHeader("socket-id", infoData.data.socket_id)
                            .addHeader("socket-signature", AppTools.md5(infoData.data.socket_token + infoData.data.socket_id))
                            .build()
                    webSocket = client.newWebSocket(request, object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket?, response: Response?) {
//                            Log.d("Socket", "onOpen")
                            webSocket!!.send("{\"flag\":\"init\"}")
                        }

                        override fun onMessage(webSocket: WebSocket?, text: String?) {
                            //查看是否有匹配的响应 可能存在发送失败
//                            Log.d("Socket", "$text")
                            LocalLogUtils.writeLog("socket Response : $text", System.currentTimeMillis())
                            parse(text!!)
                        }

                        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                            LocalLogUtils.writeLog("socket onClosed $code", System.currentTimeMillis())
                        }

                        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                            LocalLogUtils.writeLog("socket onFailure " + t!!.message, System.currentTimeMillis())
                            /*
                             * 断线重连  后台时间太长 其他原因 网络不可用不在重连,只当再次重复调用socket时,再激活
                             */
                            if (AppTools.isNetOk(context)) {
                                retry()
                            }
                        }
                    })
                    webSocket?.request()
                }
            } catch (e: Exception) {
            }
        }
    }

    private inner class Retry : Runnable {
        override fun run() {
            retryCount++
            initSocket()
        }
    }

    /**
     * 校验消息发送是否成功
     */
    private inner class SocketSendBecomeSuccess : Runnable {
        override fun run() {
            LocalLogUtils.writeLog("SocketServer: start runnable 延时1500s 未收到response,断开socket重新连接", System.currentTimeMillis())
            if (isCircleSend) {
                LocalLogUtils.writeLog("SocketServer: 延时未响应, 手动断开socket,并再次发送msg", System.currentTimeMillis())
                cacheMsg = checkCacheMsg
                checkCacheMsg = null
                checkOffline()
            }
        }
    }

    /**
     * 检测是否离线
     */
    private fun checkOffline() {
        webSocket?.send("{\"flag\":\"init\"}")
        //消息发送失败, socket未断线,但是通道关闭
        //定时2s 如果消息未收到响应,判断socket断线重连
        progress.postDelayed(checkStatus, 3000)
    }

    //检测发送初始化消息后是否有respones
    private inner class SocketCheckStatus : Runnable {
        override fun run() {
            LocalLogUtils.writeLog("SocketServer: SocketCheckStatus :run  缓存:$checkCacheMsg", System.currentTimeMillis())
            if (!TextUtils.isEmpty(checkCacheMsg)) {
                cacheMsg = checkCacheMsg
            }
            initSocket()
        }
    }

    /*
     * 重连服务
     */
    @Synchronized
    private fun retry() {
        if (ActivityLifecycleHelper.isClearActivities()) {//服务进程则Activity未开启, 不请求
            return
        }
        if (AppTools.isNetOk(context)) {
            //检测socket掉线
            LocalLogUtils.writeLog("SocketServer:retry App is background ${!ActivityLifecycleHelper.isAppPause}", System.currentTimeMillis())
            //当App长期处于后台的时候,停止重连Socket
            if (System.currentTimeMillis() - ActivityLifecycleHelper.build().startTime < 60 * 30 * 1000) {//是启动后的后台进程,判断是否长时间处于后台 大于半小时, 则不再重连
                checkOffline()
                progress.postDelayed(retryTask, if (retryCount < 3) 4000L else 60 * 1000L)
            }
        }
    }

    private fun beat() {
        //隔5s发送心跳包
        progress.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun checkNetState(event: SocKetOffLineEvent) {
        retry()
    }

    //发送socket消息
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun msgEvent(event: SendMsgEvent) {
        LocalLogUtils.writeLog("SocketServer: 发送socket信息:${event.msg}", System.currentTimeMillis())
        val isSend = webSocket?.send(event.msg)
        LocalLogUtils.writeLog("SocketServer: sendMsg : $isSend ", System.currentTimeMillis())
        if (isSend != null) {
            if (isSend) { //检测消息是否发送成功
                if (event.isCircle) {
                    checkCacheMsg = event.msg
                    progress.postDelayed(checkMsgTask, 2000)
                    isCircleSend = true
                }
            } else {
                checkCacheMsg = event.msg
                checkOffline()
            }
        } else {
            checkCacheMsg = event.msg
            checkOffline()
        }
    }

    private fun parse(result: String) {
        try {
            //移除监听任务
            progress.removeCallbacks(checkStatus)
            val socketData = JSON.parseObject(result, SocketData::class.java)
            if (socketData.code == 1 || (!TextUtils.isEmpty(socketData.msg) && ("success" == socketData.msg || "failure" == socketData.msg))) {
                if (socketData.code == 1) {
                    LocalLogUtils.writeLog("SocketServer:socketData.code=1 清除消息  $cacheMsg", System.currentTimeMillis())
                    cacheMsg = null
                }
                isCircleSend = false
                progress.removeCallbacks(checkMsgTask)
                if (isCircleSend && socketData.msg.contains("数据有误")) {//有重发的消息且重发消息已发成功不处理
                    socketData.msg = ""
                    LocalLogUtils.writeLog("SocketServer:可能是重发消息已存在,清除缓存消息", System.currentTimeMillis())
                }
                EventBus.getDefault().post(OnDeleteMsg(socketData))
            } else {
                socketData.resouces = result
                when (socketData.flag) {
                    "init" -> {//初始化
                        progress.removeCallbacks(retryTask)
                        beat()
                        LocalLogUtils.writeLog("SocketServer: socket 初始化成功", System.currentTimeMillis())
                        retryCount = 0
                        if (!TextUtils.isEmpty(cacheMsg)) {
                            webSocket?.send(cacheMsg!!)
                            LocalLogUtils.writeLog("SocketServer: socket 再次发送缓存的未成功消息: $cacheMsg", System.currentTimeMillis())
                            cacheMsg = null
                        }
                        isCircleSend = false
                    }
                    "singleChat" -> {//私聊或者回声
                        if (!TextUtils.isEmpty(socketData.action)) {
                            if ("delete" == socketData.action) {
                                when (socketData.data.chatType) {
                                    1 -> {//撤回回声信息
                                        EventBus.getDefault().post(TalkListActivity.OnNewMsgEvent(socketData))
                                    }
                                    2 -> {////撤回私聊信息
                                        EventBus.getDefault().post(OnChatNewMsgEvent(socketData))
                                        EventBus.getDefault().post(OnArtNewMsgEvent(socketData))
                                    }
                                    3 -> {//撤回涂鸦作品
                                        EventBus.getDefault().post(OnArtNewMsgEvent(socketData))
                                    }
                                }
                                /*if ("0" == socketData.data.voiceId) {

                                } else {

                                }*/
                            } else if ("interactive" == socketData.action) {
                                EventBus.getDefault().post(UpdateInteraction(socketData.data.actionType))
                            }
                        } else {
                            val loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                            if (socketData.data.dialog_content_type == 1) {
                                if (socketData.data.from_user_id != loginBean.user_id)
                                    DownLoadPool.getInstance().addPool(socketData.data.dialog_content_url)
                            }
                            //当是自己发送消息的时候才需要 清除所有标记
                            if (socketData.data.from_user_id == loginBean.user_id) {
                                checkCacheMsg = null
                                progress.removeCallbacks(checkMsgTask)
                                isCircleSend = false
                            }
                            when (socketData.data.chat_type) {
                                1 -> {//回声
                                    EventBus.getDefault().post(TalkListActivity.OnNewMsgEvent(socketData))
                                    EventBus.getDefault().post(EchoesUpdateEvent(1))
                                    EventBus.getDefault().post(SendMsgSocketEvent(socketData.data.voice_id, socketData.data.chat_id.toString()))
                                }
                                3 -> {//涂鸦对话
                                    //刷新回声界面
                                    EventBus.getDefault().post(EchoesUpdateEvent(1))
                                    EventBus.getDefault().post(OnArtNewMsgEvent(socketData))
                                }
                                else -> {//私聊
                                    EventBus.getDefault().post(EchoesUpdateEvent(2))
                                    EventBus.getDefault().post(OnChatNewMsgEvent(socketData))
                                    EventBus.getDefault().post(OnRecentStatusEvent(2))
                                }
                            }
                            if (!TextUtils.isEmpty(socketData.from)) {//收到的消息
                                if (socketData.data.chat_type == 1) {
                                    socketData.data.type = 7
                                    socketData.data.title = "有人给你发回声了"
                                } else if (socketData.data.chat_type == 3) {
                                    //画
                                    socketData.data.title = "有人给你发涂鸦对话"
                                    socketData.data.type = 1001
                                } else {
                                    socketData.data.type = 9
                                    socketData.data.title = "有人给你发了一" + if (socketData.data.dialog_content_type == 1) "条语音" else "张图片"
                                }
                                EventBus.getDefault().post(OnNewHintViewEvent(socketData.data))
                                NotifyUtils.getInstance(context).notify(socketData)
                            } else {//自己发送消息
                                if (socketData.data.chat_type == 1) {
                                    // 弹出 回声成功
                                    EventBus.getDefault().post(SocketEvent(1))
                                }
                            }
                        }
                    }
                    "systemMsg" -> {//系统通知
                        if (socketData.data.type == 15) {
                            EventBus.getDefault().post(UpdateInteraction(socketData.data))
                        } else {
                            EventBus.getDefault().post(EchoesUpdateEvent(2))
                            if (socketData.data.type == 12) {
                                if (socketData.data.match_type == 1) {
                                    EventBus.getDefault().post(VoiceConnectActivity.OnMatchUserEvent(socketData.data))
                                } else {
                                    EventBus.getDefault().post(MagicCanvasActivity.OnMatchCanvas(socketData.data))
                                }
                            } else {
                                if (socketData.data.type == 13) {
                                    //用户被限制
                                    EventBus.getDefault().post(SocketEvent(2))
                                } else if (socketData.data.type == 14) {
                                    if (TextUtils.isEmpty(socketData.data.title))
                                        socketData.data.title = "有人关注了你的心情"
                                } else if (socketData.data.type == 16) {
                                    if (TextUtils.isEmpty(socketData.data.title))
                                        socketData.data.title = "有人为你的闹钟台词配音了~"
                                } else if (socketData.data.type == 17) {
                                    if (TextUtils.isEmpty(socketData.data.title))
                                        socketData.data.title = "有人下载了你的闹钟配音"
                                }
                                NotifyUtils.getInstance(context).notify(socketData)
                                EventBus.getDefault().post(OnNewHintViewEvent(socketData.data))
                            }
                            if (socketData.data.type == 5) {
                                EventBus.getDefault().post(INotifyFriendStatus(2, socketData.data.from_user_id))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    fun onDisConnect() {
        LocalLogUtils.writeLog("SocketServer: onDestroy", System.currentTimeMillis())
        try {
            progress.stop()
            webSocket?.let {
                it.close(1000, "Intercept")
                webSocket = null
            }
        } catch (e: Exception) {
        }
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
    }
}