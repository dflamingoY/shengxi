package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class NewVersionSetData extends BaseRepData {

    private List<VersionSetBean> data;

    public List<VersionSetBean> getData() {
        return data;
    }

    public void setData(List<VersionSetBean> data) {
        this.data = data;
    }

    //login_check_switch	false	1-2	登录验证是否打开，0:没设置，1:打开，2:关闭，默认0
    public static class VersionSetBean {
        private String setting_name;
        private int setting_value;

        public String getSetting_name() {
            return setting_name;
        }

        public void setSetting_name(String setting_name) {
            this.setting_name = setting_name;
        }

        public int getSetting_value() {
            return setting_value;
        }

        public void setSetting_value(int setting_value) {
            this.setting_value = setting_value;
        }
    }
}
