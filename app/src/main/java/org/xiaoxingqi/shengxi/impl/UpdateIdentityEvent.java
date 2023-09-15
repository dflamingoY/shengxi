package org.xiaoxingqi.shengxi.impl;

public class UpdateIdentityEvent {
    private int type;

    public UpdateIdentityEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
