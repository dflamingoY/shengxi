package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class NewVersionCoverData extends BaseRepData {
    private List<AddCoverData.AddCoverBean> data;

    public List<AddCoverData.AddCoverBean> getData() {
        return data;
    }

    public void setData(List<AddCoverData.AddCoverBean> data) {
        this.data = data;
    }
}
