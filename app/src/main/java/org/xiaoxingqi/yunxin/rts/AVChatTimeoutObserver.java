package org.xiaoxingqi.yunxin.rts;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.netease.nimlib.sdk.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 音视频通话时超时监听处理
 * Created by weilv on 17/9/11.
 */

public class AVChatTimeoutObserver {
    private static final String TAG = "AVChatTimeoutObserver";
    private List<TimeoutObserver> timeoutObservers = new ArrayList<>();
    private List<Observer<Integer>> timeoutObserverLocal = new ArrayList<>(1); // 来电or呼出超时监听
    private Handler uiHandler;
    private final int OUTGOING_TIME_OUT = 10 * 1000;
    private final int INCOMING_TIME_OUT = 15 * 1000;
    private static Activity activity_;

    private static class InstanceHolder {
        public final static AVChatTimeoutObserver instance = new AVChatTimeoutObserver();
    }

    public static AVChatTimeoutObserver getInstance(Activity activity) {
        activity_ = activity;
        return InstanceHolder.instance;
    }

    private AVChatTimeoutObserver() {
        uiHandler = new Handler(activity_.getMainLooper());
    }

    // 通知APP观察者
    private <T> void notifyObservers(List<Observer<T>> observers, T result) {
        if (observers == null || observers.isEmpty()) {
            return;
        }

        // 创建副本，为了使得回调到app后，app如果立即注销观察者，会造成List异常。
        List<Observer<T>> copy = new ArrayList<>(observers.size());
        copy.addAll(observers);

        for (Observer<T> o : copy) {
            o.onEvent(result);
        }
    }

    // 注册注销APP观察者
    private <T> void registerObservers(List<Observer<T>> observers, final Observer<T> observer, boolean register) {
        if (observers == null || observer == null) {
            return;
        }

        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public void observeTimeoutNotification(Observer<Integer> observer, boolean register, boolean isIncoming) {
        registerObservers(timeoutObserverLocal, observer, register);
        if (register) {
            if (isIncoming) {
                addIncomingTimeout();
            } else {
                addOutgoingTimeout();
            }
        } else {
            removeAllTimeout();
        }
    }

    private class TimeoutObserver implements Runnable {

        TimeoutObserver() {

        }

        @Override
        public void run() {
            notifyObservers(timeoutObserverLocal, 0);
        }
    }

    private void addOutgoingTimeout() {
        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);
        uiHandler.postDelayed(timeoutObserver, OUTGOING_TIME_OUT);
    }

    private void removeAllTimeout() {
        try {
            if (null != uiHandler) {
                for (TimeoutObserver observer : timeoutObservers) {
                    uiHandler.removeCallbacks(observer);
                }
            }
            timeoutObservers.clear();
            activity_ = null;
            uiHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addIncomingTimeout() {
        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);
        uiHandler.postDelayed(timeoutObserver, INCOMING_TIME_OUT);
    }

}
