package org.xiaoxingqi.alarmService.ui;


import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.xiaoxingqi.alarmService.configuration.EditedAlarm;

/**
 * Created by Yuriy on 11.08.2017.
 */

public interface UiStore {
    BehaviorSubject<EditedAlarm> editing();

    PublishSubject<String> onBackPressed();

    void createNewAlarm();

    /**
     * createNewAlarm was called -> list updates should be ignored
     */
    Subject<Boolean> transitioningToNewAlarmDetails();

    void edit(int id);

    void edit(int id, Object holder);

    void hideDetails();

    void hideDetails(Object holder);
}
