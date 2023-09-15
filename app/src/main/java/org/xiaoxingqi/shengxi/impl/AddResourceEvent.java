package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.BaseSearchBean;

public class AddResourceEvent {

    private BaseSearchBean bean;
    private int type;

    public AddResourceEvent(int type, BaseSearchBean bean) {
        this.type = type;
        this.bean = bean;
    }

    public BaseSearchBean getBean() {
        return bean;
    }

    public int getType() {
        return type;
    }
}
