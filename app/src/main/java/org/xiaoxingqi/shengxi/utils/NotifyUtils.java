package org.xiaoxingqi.shengxi.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.SocketData;
import org.xiaoxingqi.shengxi.modules.login.SplashActivity;
import org.xiaoxingqi.shengxi.utils.swipback.ActivityLifecycleHelper;

import java.util.List;

public class NotifyUtils {
    protected Vibrator vibrator;
    private Context context;
    //    private Ringtone ringtone = null;
    private static NotifyUtils notifyUtils;

    public static NotifyUtils getInstance(Context context) {
        synchronized (NotifyUtils.class) {
            if (notifyUtils == null) {
                synchronized (NotifyUtils.class) {
                    notifyUtils = new NotifyUtils(context);
                }
            }
        }
        return notifyUtils;
    }

    private NotifyUtils(Context context) {
        this.context = context;
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    private static int notifactionId = 0;

    public void notify(SocketData data) {
        if (!AppTools.isAppRunningForeground(context) || !ActivityLifecycleHelper.isTopActivity()) {
            Intent msgIntent = new Intent();
            msgIntent.setClass(context, SplashActivity.class);
            msgIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifactionId = data.getData().getCreated_at();
            String message = data.getData().getTitle();
            msgIntent.putExtra("socketData", data.getResouces());
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
                String channelName = "CustomNotification";
                String channelId =/* SPUtils.getString(context, IConstant.CHANNELNAME, context.getApplicationInfo().packageName)*/context.getApplicationInfo().packageName;
                //当sdk版本大于26
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.enableLights(true);
                AudioAttributes build = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();
                channel.setSound(defaultSoundUri, build);
                /*if (SPUtils.getBoolean(context, IConstant.ISSOUND, true)) {
                    channel.setSound(defaultSoundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                } else {
                    channel.setSound(null, null);
                }*/
                if (SPUtils.getBoolean(context, IConstant.ISVITAR, true)) {
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{500, 0, 500});
                } else {
                    channel.enableVibration(false);
                    channel.setVibrationPattern(new long[]{0});
                }
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(context, channel.getId())
                        .setCategory(Notification.CATEGORY_MESSAGE)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(contentTitle)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
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
                        .setWhen(System.currentTimeMillis())
                        .setVibrate(SPUtils.getBoolean(context, IConstant.ISVITAR, true) ? new long[]{500, 500, 500} : new long[]{0})
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setChannelId(notifactionId + "");
                notificationManager.notify(notifactionId, notification.build());
            }
        }
        //        if (ActivityLifecycleHelper.isAppPause)
        //            vibrateAndPlayTone();
    }

    protected long lastNotifiyTime;

    //应用内不震动没有提示音
    public void vibrateAndPlayTone() {
        if (System.currentTimeMillis() - lastNotifiyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }
        try {
            lastNotifiyTime = System.currentTimeMillis();
            // check if in silent mode
            if (SPUtils.getBoolean(context, IConstant.ISVITAR, true)) {
                long[] pattern = new long[]{0, 180, 80, 120};
                vibrator.vibrate(pattern, -1);
            }
            //            if (SPUtils.getBoolean(context, IConstant.ISSOUND, true)) {
            //                if (ringtone == null) {
            //                    Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)/*Uri.parse("android.resource://org.xiaoxingqi.shengxi/raw/msg")*/;
            //                    ringtone = RingtoneManager.getRingtone(context, notificationUri);
            //                    if (ringtone == null) {
            //                        return;
            //                    }
            //                    ringtone.setStreamType(AudioManager.STREAM_MUSIC);
            //                    //                    ringtone.setAudioAttributes(AudioAttributes.CONTENT_TYPE_MUSIC);
            //                }
            //                if (!ringtone.isPlaying()) {
            //                    String vendor = Build.MANUFACTURER;
            //                    ringtone.play();
            //                }
            //            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每一次更新铃声设置, 都必须删除之前的notify    api 支持android 8.0
     * <p>
     * 清除掉所有的通知
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void deleteNotify(Activity activity, String newChannelId) {
        NotificationManager nm = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        String notification = SPUtils.getString(activity, IConstant.CHANNELNAME, activity.getApplicationInfo().packageName);
        //替換新的channelName
        SPUtils.setString(activity, IConstant.CHANNELNAME, newChannelId);
        nm.cancelAll();
        List<NotificationChannel> notificationChannels = nm.getNotificationChannels();
        /*if (null == notificationChannels || notificationChannels.size() < 5) {
            return;
        }*/
        for (NotificationChannel channel : notificationChannels) {
            if (channel.getId().equals(notification)) {
                int notificationNumbers = getNotificationNumbers(nm, channel.getId());
                if (notificationNumbers == 0) {
                    nm.deleteNotificationChannel(channel.getId());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getNotificationNumbers(NotificationManager mNotificationManager, String channelId) {
        if (mNotificationManager == null || TextUtils.isEmpty(channelId)) {
            return -1;
        }
        int numbers = 0;
        StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification item : activeNotifications) {
            Notification notification = item.getNotification();
            if (notification != null) {
                if (channelId.equals(notification.getChannelId())) {
                    numbers++;
                }
            }
        }
        return numbers;
    }
}
