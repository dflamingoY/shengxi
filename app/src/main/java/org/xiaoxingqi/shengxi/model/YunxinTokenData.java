package org.xiaoxingqi.shengxi.model;

public class YunxinTokenData extends BaseRepData {
    private YunxinTokenBean data;

    public YunxinTokenBean getData() {
        return data;
    }

    public void setData(YunxinTokenBean data) {
        this.data = data;
    }

    public static class YunxinTokenBean {

        private String token;
        private String yunxin_id;

        public String getYunxin_id() {
            return yunxin_id;
        }

        public void setYunxin_id(String yunxin_id) {
            this.yunxin_id = yunxin_id;
        }


        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
