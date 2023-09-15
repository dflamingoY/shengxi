package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class PhotoData extends BaseRepData {

    private List<BaseImg> data;

    public List<BaseImg> getData() {
        return data;
    }

    public void setData(List<BaseImg> data) {
        this.data = data;
    }
}
