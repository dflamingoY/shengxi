package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class PersonalityCommentData extends BaseRepData {

    private List<PersonalityCommentBean> data;

    public List<PersonalityCommentBean> getData() {
        return data;
    }

    public void setData(List<PersonalityCommentBean> data) {
        this.data = data;
    }

    public static class PersonalityCommentBean {

        /**
         * avatar_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-avatar/46_avatar.jpg
         * content : 哈哈哈
         * created_at : 1548744604
         * id : 12
         * nick_name : 王子
         * user_id : 46
         */

        private String avatar_url;
        private String content;
        private int created_at;
        private int id;
        private String nick_name;
        private String user_id;
        private String personality_no;//用户性格
        private String personality_title;//性格名称

        public PersonalityCommentBean() {
            
        }

        public PersonalityCommentBean(String avatar_url, String content) {
            this.content = content;
            this.avatar_url = avatar_url;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getPersonality_no() {
            return personality_no;
        }

        public void setPersonality_no(String personality_no) {
            this.personality_no = personality_no;
        }

        public String getPersonality_title() {
            return personality_title;
        }

        public void setPersonality_title(String personality_title) {
            this.personality_title = personality_title;
        }
    }
}
