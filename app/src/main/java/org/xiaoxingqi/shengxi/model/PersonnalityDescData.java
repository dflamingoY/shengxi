package org.xiaoxingqi.shengxi.model;

public class PersonnalityDescData extends BaseRepData {

    private PersonalityDescBean data;

    public PersonalityDescBean getData() {
        return data;
    }

    public void setData(PersonalityDescBean data) {
        this.data = data;
    }

    public static class PersonalityDescBean {

        /**
         * id : 2
         * personality_desc :
         * personality_no : ISTJ
         * personality_title : 绝对理性
         * saying_content :
         * saying_from :
         * share_url : ?userId=46&shareId=c81e728d9d4c2f636f067f89cc14862c
         * today_join : 0
         */

        private int id;
        private String personality_desc;
        private String personality_no;
        private String personality_title;
        private String saying_content;
        private String saying_from;
        private String share_url;
        private int today_join;
        private String personality_feature;//强项
        private String personality_desc_long;
        private String img_url;//截图地址

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPersonality_desc() {
            return personality_desc;
        }

        public void setPersonality_desc(String personality_desc) {
            this.personality_desc = personality_desc;
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

        public String getSaying_content() {
            return saying_content;
        }

        public void setSaying_content(String saying_content) {
            this.saying_content = saying_content;
        }

        public String getSaying_from() {
            return saying_from;
        }

        public void setSaying_from(String saying_from) {
            this.saying_from = saying_from;
        }

        public String getShare_url() {
            return share_url;
        }

        public void setShare_url(String share_url) {
            this.share_url = share_url;
        }

        public int getToday_join() {
            return today_join;
        }

        public void setToday_join(int today_join) {
            this.today_join = today_join;
        }

        public String getPersonality_feature() {
            return personality_feature;
        }

        public void setPersonality_feature(String personality_feature) {
            this.personality_feature = personality_feature;
        }

        public String getPersonality_desc_long() {
            return personality_desc_long;
        }

        public void setPersonality_desc_long(String personality_desc_long) {
            this.personality_desc_long = personality_desc_long;
        }

        public String getImg_url() {
            return img_url;
        }

        public void setImg_url(String img_url) {
            this.img_url = img_url;
        }
    }
}
