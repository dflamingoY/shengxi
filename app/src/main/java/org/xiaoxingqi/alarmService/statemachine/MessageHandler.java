package org.xiaoxingqi.alarmService.statemachine;


/**
 * Created by Yuriy on 07.03.2017.
 */
public interface MessageHandler {
    void handleMessage(Message msg);
}
