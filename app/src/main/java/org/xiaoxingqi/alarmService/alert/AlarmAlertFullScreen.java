/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xiaoxingqi.alarmService.alert;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.xiaoxingqi.alarmService.background.Event;
import org.xiaoxingqi.alarmService.interfaces.Alarm;
import org.xiaoxingqi.alarmService.interfaces.IAlarmsManager;
import org.xiaoxingqi.alarmService.interfaces.Intents;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import java.util.Calendar;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

import static org.xiaoxingqi.alarmService.configuration.Prefs.LONGCLICK_DISMISS_DEFAULT;
import static org.xiaoxingqi.alarmService.configuration.Prefs.LONGCLICK_DISMISS_KEY;
import static org.xiaoxingqi.shengxi.core.App.container;


/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm tone. This
 * activity is the full screen version which shows over the lock screen with the
 * wallpaper as the background.
 */
public class AlarmAlertFullScreen extends AppCompatActivity {
    protected static final String SCREEN_OFF = "screen_off";
    protected Alarm mAlarm;
    private final IAlarmsManager alarmsManager = container().alarms();
    private final SharedPreferences sp = container().sharedPreferences();
    private boolean longClickToDismiss;
    private Disposable subscription;
    //    private TickBroadCast cast;
    private TextView tvTime;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //如果息屏 里面亮起屏幕
        /*try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isInteractive()) {
                PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "shengxi:AlertServicePusher");
                wakeLock.acquire(10000); // 点亮屏幕
                wakeLock.release();
            }
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
            // 屏幕锁定
            keyguardLock.reenableKeyguard();
            keyguardLock.disableKeyguard();
        } catch (Exception e) {
            e.printStackTrace();
            LocalLogUtils.writeLog(e.getMessage(), System.currentTimeMillis());
        }*/
        LocalLogUtils.writeLog("AlarmAlertFullScreen :onCreate", System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final int id = getIntent().getIntExtra(Intents.EXTRA_ID, -1);
        try {
            mAlarm = alarmsManager.getAlarm(id);
            final Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            // Turn on the screen unless we are being launched from the
            // AlarmAlert
            // subclass as a result of the screen turning off.
           /* if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            }*/
            win.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
            updateLayout();
            // Register to get the alarm killed/snooze/dismiss intent.
            subscription = container().store()
                    .getEvents()
                    .filter(new Predicate<Event>() {
                        @Override
                        public boolean test(Event event) throws Exception {
                            return (event instanceof Event.SnoozedEvent && ((Event.SnoozedEvent) event).getId() == id)
                                    || (event instanceof Event.DismissEvent && ((Event.DismissEvent) event).getId() == id)
                                    || (event instanceof Event.Autosilenced && ((Event.Autosilenced) event).getId() == id);
                        }
                    }).subscribe(new Consumer<Event>() {
                        @Override
                        public void accept(Event event) throws Exception {
                            finish();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //解析当前时间
        try {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int week = cal.get(Calendar.DAY_OF_WEEK);
            int days = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            tvTime = findViewById(R.id.tvTime);
            tvTime.setText(hour + ":" + ((minute > 10) ? minute : "0" + minute));
            TextView tvDays = findViewById(R.id.tvDays);
            tvDays.setText((month + 1) + "月" + days + "日  星期" + parseWeeks(week));
        } catch (Exception e) {
            e.printStackTrace();
            LocalLogUtils.writeLog("时区获取错误: " + e.getMessage(), System.currentTimeMillis());
        }
        //        cast = new TickBroadCast();
        //        IntentFilter filter = new IntentFilter();
        //        filter.addAction(Intent.ACTION_TIME_TICK);
        //        registerReceiver(cast, filter);
    }

    private String parseWeeks(int week) {
        String name = "";
        switch (week) {
            case 1:
                name = "天";
                break;
            case 2:
                name = "一";
                break;
            case 3:
                name = "二";
                break;
            case 4:
                name = "三";
                break;
            case 5:
                name = "四";
                break;
            case 6:
                name = "五";
                break;
            case 7:
                name = "六";
                break;
        }
        return name;
    }

    private class TickBroadCast extends BroadcastReceiver {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            try {
                tvTime.setText(hour + ":" + ((minute >= 10) ? minute : "0" + minute));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setTitle() {
        final String titleText = mAlarm.getLabelOrDefault();
        setTitle(titleText);
        TextView textView = findViewById(R.id.alarm_alert_label);
        textView.setText(titleText);
    }

    protected int getLayoutResId() {
        return R.layout.alert_fullscreen;
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflater.inflate(getLayoutResId(), null));

        /*
         * snooze behavior: pop a snooze confirmation view, kick alarm manager.
         */
        final Button snooze = findViewById(R.id.alert_button_snooze);
        snooze.requestFocus();
        snooze.setOnClickListener(v -> snoozeIfEnabledInSettings());

        /* dismiss button: close notification */
        final Button dismissButton = findViewById(R.id.alert_button_dismiss);
        dismissButton.setOnClickListener(v -> dismiss());

        dismissButton.setOnLongClickListener(v -> {
            dismiss();
            return true;
        });

        /* Set the title from the passed in alarm */
        setTitle();
    }

    // Attempt to snooze this alert.
    private void snoozeIfEnabledInSettings() {
        if (isSnoozeEnabled()) {
            alarmsManager.snooze(mAlarm);
        }
    }

    // Dismiss the alarm.
    private void dismiss() {
        alarmsManager.dismiss(mAlarm);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private boolean isSnoozeEnabled() {
        return Integer.parseInt(sp.getString("snooze_duration", "-1")) != -1;
    }

    /**
     * this is called when a second alarm is triggered while a previous alert
     * window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = intent.getIntExtra(Intents.EXTRA_ID, -1);
        try {
            mAlarm = alarmsManager.getAlarm(id);
            setTitle();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalLogUtils.writeLog("AlarmAlertFullScreen :onResume", System.currentTimeMillis());
        longClickToDismiss = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(LONGCLICK_DISMISS_KEY,
                LONGCLICK_DISMISS_DEFAULT);
        Button snooze = findViewById(R.id.alert_button_snooze);
        View snoozeText = findViewById(R.id.alert_text_snooze);
        if (snooze != null) snooze.setEnabled(isSnoozeEnabled());
        if (snoozeText != null) snoozeText.setEnabled(isSnoozeEnabled());
        if (!snooze.isEnabled()) {
            snooze.setAlpha(0.1f);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalLogUtils.writeLog("AlarmAlertFullScreen :onDestroy", System.currentTimeMillis());
        // No longer care about the alarm being killed.
        subscription.dispose();
        //        unregisterReceiver(cast);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss
    }
}
