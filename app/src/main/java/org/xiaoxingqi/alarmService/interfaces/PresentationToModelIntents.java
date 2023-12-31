package org.xiaoxingqi.alarmService.interfaces;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.xiaoxingqi.alarmService.model.AlarmsReceiver;
import org.xiaoxingqi.shengxi.BuildConfig;

public class PresentationToModelIntents {

    public static final String ACTION_REQUEST_SNOOZE = BuildConfig.APPLICATION_ID + ".model.interfaces.ServiceIntents.ACTION_REQUEST_SNOOZE";
    public static final String ACTION_REQUEST_DISMISS = BuildConfig.APPLICATION_ID + ".model.interfaces.ServiceIntents.ACTION_REQUEST_DISMISS";

    public static PendingIntent createPendingIntent(Context context, String action, int id) {
        Intent intent = new Intent(action);
        intent.putExtra(Intents.EXTRA_ID, id);
        intent.setClass(context, AlarmsReceiver.class);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }
}
