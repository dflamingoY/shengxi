package org.xiaoxingqi.shengxi.impl;

/**
 * 用于Fragment的当前状态发生变化  监听是否自动切换扬声器和听筒
 */
public class SensorChangeEvent {
    private int type;

    public SensorChangeEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
