package org.xiaoxingqi.shengxi.model;

public class CheersData extends BaseRepData {
    private CheersDataEntity data;

    public CheersDataEntity getData() {
        return data;
    }

    public void setData(CheersDataEntity data) {
        this.data = data;
    }

    public static class CheersDataEntity {
        private UserCheersData.UserCheersBean recording;
        private BaseUserBean user;
        private String cover_url;

        public UserCheersData.UserCheersBean getRecording() {
            return recording;
        }

        public void setRecording(UserCheersData.UserCheersBean recording) {
            this.recording = recording;
        }

        public BaseUserBean getUser() {
            return user;
        }

        public void setUser(BaseUserBean user) {
            this.user = user;
        }

        public String getCover_url() {
            return cover_url;
        }

        public void setCover_url(String cover_url) {
            this.cover_url = cover_url;
        }
    }
}
