package org.xiaoxingqi.shengxi.impl;

/**
 * 心情簿用来 改变当前 传感器状态
 */
public class SensorChangeMoodEvent {
    private int type;

    public SensorChangeMoodEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
