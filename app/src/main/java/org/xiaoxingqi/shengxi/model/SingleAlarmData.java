package org.xiaoxingqi.shengxi.model;

import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean;

public class SingleAlarmData extends BaseRepData {

    private BaseAlarmBean data;

    public BaseAlarmBean getData() {
        return data;
    }

    public void setData(BaseAlarmBean data) {
        this.data = data;
    }
}
