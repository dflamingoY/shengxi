package org.xiaoxingqi.alarmService.model;

import android.database.Cursor;

/**
 * Created by Yuriy on 24.06.2017.
 */

public interface ContainerFactory {
    AlarmActiveRecord create();

    AlarmActiveRecord create(Cursor cursor);
}
