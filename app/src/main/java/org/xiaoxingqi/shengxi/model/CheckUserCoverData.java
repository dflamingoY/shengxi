package org.xiaoxingqi.shengxi.model;

public class CheckUserCoverData extends BaseRepData {

    private CheckUserCoverBean data;

    public CheckUserCoverBean getData() {
        return data;
    }

    public void setData(CheckUserCoverBean data) {
        this.data = data;
    }

    public static class CheckUserCoverBean {
        private String id;
        private String user_id;
        private String cover_tag;
        private String cover_name;
        private String cover_url;
        private String cover_status;
        private int created_at;
        private int updated_at;
        private int deleted_at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getCover_tag() {
            return cover_tag;
        }

        public void setCover_tag(String cover_tag) {
            this.cover_tag = cover_tag;
        }

        public String getCover_name() {
            return cover_name;
        }

        public void setCover_name(String cover_name) {
            this.cover_name = cover_name;
        }

        public String getCover_url() {
            return cover_url;
        }

        public void setCover_url(String cover_url) {
            this.cover_url = cover_url;
        }

        public String getCover_status() {
            return cover_status;
        }

        public void setCover_status(String cover_status) {
            this.cover_status = cover_status;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
        }

        public int getDeleted_at() {
            return deleted_at;
        }

        public void setDeleted_at(int deleted_at) {
            this.deleted_at = deleted_at;
        }
    }

}
