package org.xiaoxingqi.alarmService.interfaces;

import android.net.Uri;


import java.util.Calendar;

import org.xiaoxingqi.alarmService.model.Alarmtone;

public interface Alarm {
    void enable(boolean enable);

    void snooze();

    void snooze(int hourOfDay, int minute);

    void dismiss();

    void delete();

    AlarmEditor edit();

    boolean isSilent();

    Uri getAlert();

    int getId();

    String getLabelOrDefault();

    Alarmtone getAlarmtone();

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    public Calendar getSnoozedTime();

}
