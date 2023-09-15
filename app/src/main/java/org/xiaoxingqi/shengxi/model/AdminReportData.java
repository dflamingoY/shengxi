package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class AdminReportData extends BaseRepData {

    private List<BaseAdminReportBean> data;

    public List<BaseAdminReportBean> getData() {
        return data;
    }

    public void setData(List<BaseAdminReportBean> data) {
        this.data = data;
    }
}
