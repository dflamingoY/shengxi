package org.xiaoxingqi.shengxi.model;

import org.xiaoxingqi.shengxi.model.alarm.LoaderBoardData;

import java.util.List;

public class AlarmBangDubbingData extends BaseRepData {

    private List<LoaderBoardData.LoaderTypeBean> data;

    public List<LoaderBoardData.LoaderTypeBean> getData() {
        return data;
    }

    public void setData(List<LoaderBoardData.LoaderTypeBean> data) {
        this.data = data;
    }
}
