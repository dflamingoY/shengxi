package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class SearchResData extends BaseRepData {

    private List<BaseSearchBean> data;

    public List<BaseSearchBean> getData() {
        return data;
    }

    public void setData(List<BaseSearchBean> data) {
        this.data = data;
    }
}
