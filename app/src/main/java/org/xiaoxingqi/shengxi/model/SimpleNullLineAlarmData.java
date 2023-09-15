package org.xiaoxingqi.shengxi.model;

import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmAdminBean;

public class SimpleNullLineAlarmData extends BaseRepData {

    private BaseAlarmAdminBean data;

    public BaseAlarmAdminBean getData() {
        return data;
    }

    public void setData(BaseAlarmAdminBean data) {
        this.data = data;
    }
}
