package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.SocketData;

/**
 * 删除撤销消息
 */
public class OnDeleteMsg {
    private SocketData data;

    public OnDeleteMsg(SocketData data) {
        this.data = data;
    }


    public SocketData getData() {
        return data;
    }

    public void setData(SocketData data) {
        this.data = data;
    }
}
