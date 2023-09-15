package org.xiaoxingqi.shengxi.impl;

public class EchoesUpdateEvent {
    private int type;//1

    public EchoesUpdateEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
