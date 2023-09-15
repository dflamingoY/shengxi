package org.xiaoxingqi.shengxi.model;

public class MatchSettingData extends BaseRepData {

    private MatchBean data;

    public MatchBean getData() {
        return data;
    }

    public void setData(MatchBean data) {
        this.data = data;
    }

    public static class MatchBean {

        private int match_new;
        private String first_user_id="0";
        private String first_avatar_url;
        private String first_nick_name;


        public int getMatch_new() {
            return match_new;
        }

        public void setMatch_new(int match_new) {
            this.match_new = match_new;
        }

        public String getFirst_user_id() {
            return first_user_id;
        }

        public void setFirst_user_id(String first_user_id) {
            this.first_user_id = first_user_id;
        }

        public String getFirst_avatar_url() {
            return first_avatar_url;
        }

        public void setFirst_avatar_url(String first_avatar_url) {
            this.first_avatar_url = first_avatar_url;
        }

        public String getFirst_nick_name() {
            return first_nick_name;
        }

        public void setFirst_nick_name(String first_nick_name) {
            this.first_nick_name = first_nick_name;
        }
    }

}
