package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class BlackDeleteData extends BaseRepData {

    private List<DeleteBean> data;

    public List<DeleteBean> getData() {
        return data;
    }

    public void setData(List<DeleteBean> data) {
        this.data = data;
    }

    public static class DeleteBean {

        private String user_id;
        private String to_user_id;

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }
    }
}
