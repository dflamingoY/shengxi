package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class SubscribeUserData extends BaseRepData {

    private List<SubscribeUserBean> data;

    public List<SubscribeUserBean> getData() {
        return data;
    }

    public void setData(List<SubscribeUserBean> data) {
        this.data = data;
    }

    public static class SubscribeUserBean {
        private int id;
        private String log_id;
        private int resource_type;
        private BaseUserBean subscribeUser;
        private int renewed_at;
        private int established_at;
        private int released_at;
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLog_id() {
            return log_id;
        }

        public void setLog_id(String log_id) {
            this.log_id = log_id;
        }

        public int getResource_type() {
            return resource_type;
        }

        public void setResource_type(int resource_type) {
            this.resource_type = resource_type;
        }

        public BaseUserBean getSubscribeUser() {
            return subscribeUser;
        }

        public void setSubscribeUser(BaseUserBean subscribeUser) {
            this.subscribeUser = subscribeUser;
        }

        public int getRenewed_at() {
            return renewed_at;
        }

        public void setRenewed_at(int renewed_at) {
            this.renewed_at = renewed_at;
        }

        public int getEstablished_at() {
            return established_at;
        }

        public void setEstablished_at(int established_at) {
            this.established_at = established_at;
        }

        public int getReleased_at() {
            return released_at;
        }

        public void setReleased_at(int released_at) {
            this.released_at = released_at;
        }
    }

}
