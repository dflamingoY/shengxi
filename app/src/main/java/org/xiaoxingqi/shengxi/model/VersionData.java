package org.xiaoxingqi.shengxi.model;


/**
 * Created by yzm on 2018/3/29.
 */

public class VersionData extends BaseRepData {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String id;
        private String app_version;
        private String app_url;
        private String app_content;
        private int forced_update;//0  1  强制更新
        private int stop_at;//听之维护的时间

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getApp_version() {
            return app_version;
        }

        public void setApp_version(String app_version) {
            this.app_version = app_version;
        }

        public String getApp_url() {
            return app_url;
        }

        public void setApp_url(String app_url) {
            this.app_url = app_url;
        }

        public String getApp_content() {
            return app_content;
        }

        public void setApp_content(String app_content) {
            this.app_content = app_content;
        }

        public int getForced_update() {
            return forced_update;
        }

        public void setForced_update(int forced_update) {
            this.forced_update = forced_update;
        }

        public int getStop_at() {
            return stop_at;
        }

        public void setStop_at(int stop_at) {
            this.stop_at = stop_at;
        }
    }
}
