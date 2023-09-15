package org.xiaoxingqi.shengxi.model;

public class AdminLoginData extends BaseRepData {
    private AdminLoginBean data;

    public AdminLoginBean getData() {
        return data;
    }

    public void setData(AdminLoginBean data) {
        this.data = data;
    }

    public static class AdminLoginBean {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
