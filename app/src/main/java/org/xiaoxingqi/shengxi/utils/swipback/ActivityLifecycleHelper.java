package org.xiaoxingqi.shengxi.utils.swipback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.impl.SocKetOffLineEvent;
import org.xiaoxingqi.shengxi.impl.UpdateTokenEvent;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by fhf11991 on 2016/7/18.
 */

public class ActivityLifecycleHelper implements Application.ActivityLifecycleCallbacks {

    private static ActivityLifecycleHelper singleton;
    private static final Object lockObj = new Object();
    private static List<Activity> activities;
    public long startTime = 0;//app 进入后台时间

    private ActivityLifecycleHelper() {
        activities = new LinkedList<>();
    }

    public static ActivityLifecycleHelper build() {
        synchronized (lockObj) {
            if (singleton == null) {
                singleton = new ActivityLifecycleHelper();
            }
            return singleton;
        }
    }

    private static boolean isProcessTop = true;

    public static boolean isTopActivity() {
        return isProcessTop;
    }

    public static boolean isSeepMsg = false;

    private static int resumeActivity = 0;
    public static boolean isAppPause = true;//app是否在前台

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumeActivity++;
        /*long currentTime = System.currentTimeMillis();
        if (!isSeepMsg) {
            if (startTime > 0)
                if (currentTime - startTime > 300 * 1000) {
                    //  开启录制的界面
                    if (AppTools.isAppRunningForeground(activity)) {
                        if (null != PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfo.class)) {
                            if (AppTools.isNetOk(activity)) {
                                activity.startActivity(new Intent(activity, SendAct.class).
                                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra("isOpenRecord", true));
                            }
                        }
                    }
                }
        } else {
            isSeepMsg = false;
        }*/
        /**
         * 再次resume时, 判断app 是否进入到后台,  如果进入需检测socket是否存活发送心跳包
         * 检测App 从锁屏状态进入
         */
        if (!isAppPause) {
            LocalLogUtils.writeLog("从后台恢复到前台 " + activity.getLocalClassName(), System.currentTimeMillis());
            EventBus.getDefault().post(new UpdateTokenEvent());
        }
        isProcessTop = true;
        isAppPause = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        resumeActivity--;
        if (resumeActivity == 0) {
            if (!AppTools.isAppRunningForeground(activity)) {
                isAppPause = false;
            }

        }
        startTime = System.currentTimeMillis();
        isProcessTop = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
        }
        if (activities.size() == 0) {
            activities = null;
        }
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activities == null) {
            activities = new LinkedList<>();
        }
        activities.add(activity);
    }

    /**
     * 获取集合中当前Activity
     *
     * @return
     */
    public static Activity getLatestActivity() {
        ActivityLifecycleHelper adapter = build();
        if (null == adapter.activities) {
            return null;
        }
        int count = adapter.activities.size();
        if (count == 0) {
            return null;
        }
        return adapter.activities.get(count - 1);
    }

    /**
     * @return true 未打开Activity  false 后台进程
     */
    public static boolean isClearActivities() {
        try {
            return activities.size() == 0;
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * 获取集合中上一个Activity
     *
     * @return
     */
    public static Activity getPreviousActivity() {
        ActivityLifecycleHelper adapter = build();
        int count = adapter.activities.size();
        if (count < 2) {
            return null;
        }
        return adapter.activities.get(count - 2);
    }
}
