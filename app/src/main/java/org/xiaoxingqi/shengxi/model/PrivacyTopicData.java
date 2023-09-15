package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class PrivacyTopicData extends BaseRepData {

    private List<SearchTopicData.SearchTopicBean> data;

    public List<SearchTopicData.SearchTopicBean> getData() {
        return data;
    }

    public void setData(List<SearchTopicData.SearchTopicBean> data) {
        this.data = data;
    }
}
