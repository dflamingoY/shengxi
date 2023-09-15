package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class UserMoviesData extends BaseRepData {

    private UserMoviesBean data;

    public UserMoviesBean getData() {
        return data;
    }

    public void setData(UserMoviesBean data) {
        this.data = data;
    }

    public static class UserMoviesBean {
        private String total;
        private List<BaseSearchBean> list;

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<BaseSearchBean> getList() {
            return list;
        }

        public void setList(List<BaseSearchBean> list) {
            this.list = list;
        }
    }

}
