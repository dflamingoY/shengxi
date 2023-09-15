package org.xiaoxingqi.shengxi.receiver;

import android.content.Context;
import android.content.Intent;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.impl.SocKetOffLineEvent;
import org.xiaoxingqi.shengxi.modules.login.SplashActivity;


public class MiReceiver extends PushMessageReceiver {

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        //方法用来接收服务器向客户端发送的透传消息
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        //方法用来接收服务器向客户端发送的通知消息，
        //这个回调方法会在用户手动点击通知后触发。
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("stringExtra", message.getContent());
        context.startActivity(new Intent(context, SplashActivity.class));
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        //方法用来接收服务器向客户端发送的通知消息，
        //这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数
        try {
            EventBus.getDefault().post(new SocKetOffLineEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        //方法用来接收客户端向服务器发送命令后的响应结果。
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        //方法用来接收客户端向服务器发送注册命令后的响应结果
    }

    @Override
    public void onRequirePermissions(Context context, String[] permissions) {
        super.onRequirePermissions(context, permissions);
    }
}
