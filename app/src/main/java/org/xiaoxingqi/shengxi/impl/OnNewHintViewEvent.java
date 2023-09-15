package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.SocketData;

public class OnNewHintViewEvent {
    private SocketData.SocketBean data;

    public OnNewHintViewEvent(SocketData.SocketBean data) {
        this.data = data;
    }

    public SocketData.SocketBean getData() {
        return data;
    }

    public void setData(SocketData.SocketBean data) {
        this.data = data;
    }
}
