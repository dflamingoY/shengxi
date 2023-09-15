package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class SystemInfoData extends BaseRepData {

    private List<SystemInfoBean> data;

    public List<SystemInfoBean> getData() {
        return data;
    }

    public void setData(List<SystemInfoBean> data) {
        this.data = data;
    }

    public static class SystemInfoBean {

        private String id;
        private String push_at;
        private String title;
        private String content;
        private int created_at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getPush_at() {
            return push_at;
        }

        public void setPush_at(String push_at) {
            this.push_at = push_at;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }
    }
}
