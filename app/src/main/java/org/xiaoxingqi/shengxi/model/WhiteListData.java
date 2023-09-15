package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class WhiteListData extends BaseRepData {
    private List<WhiteListBean> data;

    public List<WhiteListBean> getData() {
        return data;
    }

    public void setData(List<WhiteListBean> data) {
        this.data = data;
    }

    public static class WhiteListBean {
        private int id;
        private String to_user_id;
        private int released_at;
        private int identity_type;
        private String nick_name;
        private String avatar_url;
        private String user_id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }

        public int getReleased_at() {
            return released_at;
        }

        public void setReleased_at(int released_at) {
            this.released_at = released_at;
        }

        public int getIdentity_type() {
            return identity_type;
        }

        public void setIdentity_type(int identity_type) {
            this.identity_type = identity_type;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
    }

}
