package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.SocketData;

public class OnChatNewMsgEvent {
    private SocketData bean;

    public OnChatNewMsgEvent(SocketData bean) {
        this.bean = bean;
    }

    public SocketData getBean() {
        return bean;
    }

    public void setBean(SocketData bean) {
        this.bean = bean;
    }
}
