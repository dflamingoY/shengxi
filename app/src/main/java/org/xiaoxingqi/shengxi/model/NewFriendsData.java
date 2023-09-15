package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class NewFriendsData extends BaseRepData {

    private List<NewFriendBean> data;

    public List<NewFriendBean> getData() {
        return data;
    }

    public void setData(List<NewFriendBean> data) {
        this.data = data;
    }

    public static class NewFriendBean {

        /**
         * created_at : 1539314435
         * id : 2
         * log_status : 0
         * nick_name : 你妹夫
         * updated_at : 0
         * user_id : 1
         */

        private int created_at;
        private int id;
        private int log_status;//记录状态，处理状态，0=未处理，1=通过，2=忽略
        private String nick_name;
        private int updated_at;
        private String user_id;
        private String from_user_id;

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getLog_status() {
            return log_status;
        }

        public void setLog_status(int log_status) {
            this.log_status = log_status;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public void setFrom_user_id(String from_user_id) {
            this.from_user_id = from_user_id;
        }
    }
}
