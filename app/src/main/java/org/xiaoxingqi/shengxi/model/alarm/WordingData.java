package org.xiaoxingqi.shengxi.model.alarm;

import org.xiaoxingqi.shengxi.model.BaseRepData;

import java.util.List;

public class WordingData extends BaseRepData {

    private List<BaseAlarmBean> data;

    public List<BaseAlarmBean> getData() {
        return data;
    }

    public void setData(List<BaseAlarmBean> data) {
        this.data = data;
    }
}
