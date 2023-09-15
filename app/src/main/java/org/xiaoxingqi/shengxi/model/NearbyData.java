package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class NearbyData extends BaseRepData {
    private List<NearbyBean> data;

    public List<NearbyBean> getData() {
        return data;
    }

    public void setData(List<NearbyBean> data) {
        this.data = data;
    }


    public static class NearbyBean extends BaseAnimBean {

        private String user_id;
        private String nick_name;
        private String avatar_url;
        private String wave_url;
        private String self_intro;
        private int login_at;
        private String wave_len;
        private long distance;
        private int voice_total_len;

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
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

        public String getWave_url() {
            return wave_url;
        }

        public void setWave_url(String wave_url) {
            this.wave_url = wave_url;
        }

        public String getSelf_intro() {
            return self_intro;
        }

        public void setSelf_intro(String self_intro) {
            this.self_intro = self_intro;
        }

        public int getLogin_at() {
            return login_at;
        }

        public void setLogin_at(int login_at) {
            this.login_at = login_at;
        }

        public String getWave_len() {
            return wave_len;
        }

        public void setWave_len(String wave_len) {
            this.wave_len = wave_len;
            voice_len = wave_len;
        }

        public long getDistance() {
            return distance;
        }

        public void setDistance(long distance) {
            this.distance = distance;
        }

        public int getVoice_total_len() {
            return voice_total_len;
        }

        public void setVoice_total_len(int voice_total_len) {
            this.voice_total_len = voice_total_len;
        }
    }
}
