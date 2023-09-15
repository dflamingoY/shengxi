package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class PersonalityGroupData extends BaseRepData {
    private List<PersonalityGroupBean> data;

    public List<PersonalityGroupBean> getData() {
        return data;
    }

    public void setData(List<PersonalityGroupBean> data) {
        this.data = data;
    }

    public static class PersonalityGroupBean {

        /**
         * id : 22
         * role_from : 进击的巨人
         * role_name : 三笠·阿克曼
         * role_pic_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/personality/2_731f467619ad106bbe50514a9ae18635.jpg
         */

        private int id;
        private String role_from;
        private String role_name;
        private String role_pic_url;
        private String personality_id;//性格类型ID
        private String personality_title;
        private String role_intro;
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getRole_from() {
            return role_from;
        }

        public void setRole_from(String role_from) {
            this.role_from = role_from;
        }

        public String getRole_name() {
            return role_name;
        }

        public void setRole_name(String role_name) {
            this.role_name = role_name;
        }

        public String getRole_pic_url() {
            return role_pic_url;
        }

        public void setRole_pic_url(String role_pic_url) {
            this.role_pic_url = role_pic_url;
        }

        public String getPersonality_id() {
            return personality_id;
        }

        public void setPersonality_id(String personality_id) {
            this.personality_id = personality_id;
        }

        public String getPersonality_title() {
            return personality_title;
        }

        public void setPersonality_title(String personality_title) {
            this.personality_title = personality_title;
        }

        public String getRole_intro() {
            return role_intro;
        }

        public void setRole_intro(String role_intro) {
            this.role_intro = role_intro;
        }
    }
}
