package org.xiaoxingqi.shengxi.impl;

public class IUpdateEvent {
    private String action;
    private int state;
    private int bt;

    public IUpdateEvent(String str, int state) {
        action = str;
        this.state = state;
    }

    public IUpdateEvent(String str, int state, int bt) {
        action = str;
        this.state = state;
        this.bt = bt;
    }

    public String getAction() {
        return action;
    }

    public int getState() {
        return state;
    }

    public int getBt() {
        return bt;
    }
}
