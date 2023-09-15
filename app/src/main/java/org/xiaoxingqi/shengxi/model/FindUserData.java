package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class FindUserData extends BaseRepData {

    private FindUserBean data;

    public FindUserBean getData() {
        return data;
    }

    public void setData(FindUserBean data) {
        this.data = data;
    }

    public static class FindUserBean {
        private List<String> user_tag;

        private BaseUserBean user;

        public List<String> getUser_tag() {
            return user_tag;
        }

        public void setUser_tag(List<String> user_tag) {
            this.user_tag = user_tag;
        }

        public BaseUserBean getUser() {
            return user;
        }

        public void setUser(BaseUserBean user) {
            this.user = user;
        }
    }
}
