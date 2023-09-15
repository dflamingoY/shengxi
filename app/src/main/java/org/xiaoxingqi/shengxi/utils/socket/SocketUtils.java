package org.xiaoxingqi.shengxi.utils.socket;


import android.app.Activity;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.core.http.OkClientHelper;
import org.xiaoxingqi.shengxi.impl.SocKetOffLineEvent;
import org.xiaoxingqi.shengxi.impl.SocketEvent;
import org.xiaoxingqi.shengxi.model.UserInfoData;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;
import org.xiaoxingqi.shengxi.utils.PreferenceTools;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by yzm on 2017/12/5.
 */

public class SocketUtils {
    private static WebSocket socket;

    public synchronized static WebSocket initSocket(Activity activity) {
        synchronized (SocketUtils.class) {
            try {
                if (socket != null) {
                    socket.close(1000, "close");
                    socket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LocalLogUtils.writeLog("SocketUtils: 开始创建socket对象", System.currentTimeMillis());
            UserInfoData infoData = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData.class);
            if (infoData != null && infoData.getData() != null) {
                OkHttpClient client = OkClientHelper.getClient();
                Request request = new Request.Builder()
                        .url(IConstant.SOCKETPORT)
                        .addHeader("socket-id", infoData.getData().getSocket_id())
                        .addHeader("socket-signature", AppTools.md5(infoData.getData().getSocket_token() + infoData.getData().getSocket_id()))
                        .build();
                socket = client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        super.onOpen(webSocket, response);
                        webSocket.send("{\"flag\":\"init\"}");
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        super.onMessage(webSocket, text);
                        //                        Log.d("Socket", text);
                        LocalLogUtils.writeLog("socket Response : " + text, System.currentTimeMillis());
                        EventBus.getDefault().post(new SocketEvent(text));
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        super.onMessage(webSocket, bytes);
                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {
                        super.onClosing(webSocket, code, reason);
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        super.onClosed(webSocket, code, reason);
                        LocalLogUtils.writeLog("socket onClosed " + code, System.currentTimeMillis());
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        super.onFailure(webSocket, t, response);
                        LocalLogUtils.writeLog("socket onFailure " + t.getMessage(), System.currentTimeMillis());
                        /*
                         * 断线重连
                         */
                        EventBus.getDefault().post(new SocKetOffLineEvent());
                    }
                });
                socket.request();
                return socket;
            }
            return null;
        }
    }
}
