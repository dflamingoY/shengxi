package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class UserAttentionData extends BaseRepData {
    private UserAttentionBean data;

    public UserAttentionBean getData() {
        return data;
    }

    public void setData(UserAttentionBean data) {
        this.data = data;
    }

    public static class UserAttentionBean {
        private String total;
        private List<SubscribeUserData.SubscribeUserBean> list;

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<SubscribeUserData.SubscribeUserBean> getList() {
            return list;
        }

        public void setList(List<SubscribeUserData.SubscribeUserBean> list) {
            this.list = list;
        }
    }
}
