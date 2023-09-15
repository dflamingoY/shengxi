package org.xiaoxingqi.shengxi.impl;

public class SensorChangeAdminEvent {
    private int type;

    public SensorChangeAdminEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
