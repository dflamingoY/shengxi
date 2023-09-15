package org.xiaoxingqi.shengxi.receiver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setSound(Uri.parse("android.resource://shengxi.xiaoxingqi.org.skin_red/raw1/beep"))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("sdasdads")
                .setTicker("haha ")
                .setContentText("xixi")
                .setVibrate(new long[]{0})
                .build();
        notificationManager.notify(1, notification);
    }


    /**
     * 是否是前台进程
     *
     * @param var0
     * @return
     */
    public boolean isAppRunningForeground(Context var0) {
        @SuppressLint("WrongConstant") ActivityManager var1 = (ActivityManager) var0.getSystemService("activity");
        try {
            List var2 = var1.getRunningTasks(1);
            if (var2 != null && var2.size() >= 1) {
                boolean var3 = var0.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) var2.get(0)).baseActivity.getPackageName());
                return var3;
            } else {
                return false;
            }
        } catch (SecurityException var4) {
            var4.printStackTrace();
            return false;
        }
    }

}
