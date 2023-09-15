package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class UserHomeData extends BaseRepData {

    private UserHomeBean data;

    public UserHomeBean getData() {
        return data;
    }

    public void setData(UserHomeBean data) {
        this.data = data;
    }

    public static class UserHomeBean {

        private List<BaseBean> list;
        private UserInfoData.UserBean user;

        public List<BaseBean> getList() {
            return list;
        }

        public void setList(List<BaseBean> list) {
            this.list = list;
        }

        public UserInfoData.UserBean getUser() {
            return user;
        }

        public void setUser(UserInfoData.UserBean user) {
            this.user = user;
        }
    }

}
