package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class SearchUserData extends BaseRepData {

    private List<BaseUserBean> data;

    public List<BaseUserBean> getData() {
        return data;
    }

    public void setData(List<BaseUserBean> data) {
        this.data = data;
    }
}
