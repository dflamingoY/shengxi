package org.xiaoxingqi.shengxi.model;

public class PersonnalityData extends BaseRepData {
    private PersonalityBean data;

    public PersonalityBean getData() {
        return data;
    }

    public void setData(PersonalityBean data) {
        this.data = data;
    }

    public static class PersonalityBean {
        private String personality_no;
        private String personality_id;
        private String interface_type;//I/E类型

        public String getPersonality_no() {
            return personality_no;
        }

        public void setPersonality_no(String personality_no) {
            this.personality_no = personality_no;
        }

        public String getPersonality_id() {
            return personality_id;
        }

        public void setPersonality_id(String personality_id) {
            this.personality_id = personality_id;
        }

        public String getInterface_type() {
            return interface_type;
        }

        public void setInterface_type(String interface_type) {
            this.interface_type = interface_type;
        }
    }
}
