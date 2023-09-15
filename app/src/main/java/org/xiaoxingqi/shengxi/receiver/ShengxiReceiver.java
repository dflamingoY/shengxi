package org.xiaoxingqi.shengxi.receiver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.modules.login.SplashActivity;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.SPUtils;

import java.util.Iterator;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * {
 * "about_id": 14772,
 * "created_at": 1554879775,
 * "from_user_id": "4280",
 * "key": "newDialogPri",
 * "title": "嗨！ 有人给你发私信了~",
 * "to_user_id": "5461",
 * "type": 10,
 * "voice_id": "0",
 * "voice_user_id": 0
 * }
 */
public class ShengxiReceiver extends BroadcastReceiver {

    private static int ID_NOTIFICATION = "shengxi".hashCode();
    //    private NotificationManager notificationManager = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            Log.d("Mozator", "[MyReceiver] onReceive - " + intent.getAction()
                    + ", extras: " + printBundle(bundle));

            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {//第一次安装app 才有此回调
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                Log.d("Mozator", "[MyReceiver] 接收Registration Id : " + regId);
                if (TextUtils.isEmpty(regId)) {
                    //                    App.jPushDeviceId = regId;
                }
            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {//自定义的消息
                //收到推送的消息
                Log.d("Mozator", "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
                /**
                 * 手动推消息 打开主界面
                 */
            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                Log.d("Mozator", "[MyReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                Log.d("Mozator", "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
                String packageName = context.getApplicationInfo().packageName;
                //                Intent msgIntent = context.getPackageManager()
                //                        .getLaunchIntentForPackage(packageName);
                Intent msgIntent = new Intent();
                msgIntent.setClass(context, SplashActivity.class);
                msgIntent.addCategory(Intent.CATEGORY_DEFAULT);
                String stringExtra = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
                String message = intent.getStringExtra(JPushInterface.EXTRA_ALERT);
                Log.d("Mozator", "stringExtra  " + stringExtra);
                msgIntent.putExtra("stringExtra", stringExtra);
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                PackageManager packageManager = context.getPackageManager();
                String contentTitle = (String) packageManager
                        .getApplicationLabel(context.getApplicationInfo());
                Uri defaultSoundUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        notifactionId, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //当sdk版本大于26
                    String description = "CustomNotification";
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    NotificationChannel channel = new NotificationChannel(packageName, description, importance);
                    channel.enableLights(true);
                    if (SPUtils.getBoolean(context, IConstant.ISSOUND, true)) {
                        channel.setSound(defaultSoundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                    } else {
                        channel.setSound(null, null);
                    }
                    if (SPUtils.getBoolean(context, IConstant.ISVITAR, true)) {
                        channel.enableVibration(true);
                    } else {
                        channel.enableVibration(false);
                        channel.setVibrationPattern(new long[]{0});
                    }
                    notificationManager.createNotificationChannel(channel);
                    Notification notification = new Notification.Builder(context, channel.getId())
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(contentTitle)
                            .setContentText(message)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
                    notificationManager.notify(notifactionId, notification);
                } else {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(context, notifactionId + "")
                            .setSmallIcon(context.getApplicationInfo().icon)
                            .setSound(SPUtils.getBoolean(context, IConstant.ISSOUND, true) ? defaultSoundUri : null)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setPriority(NotificationManager.IMPORTANCE_LOW)
                            .setContentTitle(contentTitle)
                            .setTicker(message)
                            .setContentText(message)
                            .setVibrate(SPUtils.getBoolean(context, IConstant.ISVITAR, true) ? new long[]{500, 500, 500} : new long[]{0})
                            .setContentIntent(pendingIntent)
                            .setChannelId(notifactionId + "");
                    notificationManager.notify(notifactionId, notification.build());
                }
                //                notificationManager.cancel(notifactionId);
                //                JPushInterface.clearAllNotifications(context);
            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
                    .getAction())) {
            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
                Log.d("Mozator", "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
                Log.d("Mozator", "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
            } else {
                Log.d("Mozator", "[MyReceiver] Unhandled intent - " + intent.getAction());
            }
        } catch (Exception e) {
            Log.d("Mozator", "异常  " + e.getMessage());
        }
    }

    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle
                        .getString(JPushInterface.EXTRA_EXTRA))) {
                    Log.d("Mozator", "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(
                            bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" + myKey + " - "
                                + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Log.d("Mozator", "Get message extra JSON error!");
                }
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
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

    public boolean isSingleActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        String MY_PKG_NAME = context.getPackageName();
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(MY_PKG_NAME) || info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }
}
