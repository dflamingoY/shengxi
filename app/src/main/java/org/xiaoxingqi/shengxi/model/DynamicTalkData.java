package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class DynamicTalkData extends BaseRepData {


    private List<DynamicTalkBean> data;

    public List<DynamicTalkBean> getData() {
        return data;
    }

    public void setData(List<DynamicTalkBean> data) {
        this.data = data;
    }

    public static class DynamicTalkBean extends BaseAnimBean {

        /**
         * chat_id : 9
         * created_at : 1540193931
         * dialog_num : 1
         * from_user_id : 8
         * user : {"avatar_url":"http://qiniu-dev.byebyetext.com/8_1f2b0bf3a9894c653f561a889c8475b2.png","id":8,"nick_name":"暗夜"}
         * voice_len : 1
         * voice_url : http://qiniu-dev.byebyetext.com/8_18a41bfded9f4a3f432bcdb341126db8.aac
         */

        private String chat_id;
        private int created_at;
        private int dialog_num;
        private String from_user_id;
        private BaseUserBean user;
        private String resource_len;
        private String resource_url;
        private String dialog_id;
        private int identity_type;

        public String getChat_id() {
            return chat_id;
        }

        public void setChat_id(String chat_id) {
            this.chat_id = chat_id;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getDialog_num() {
            return dialog_num;
        }

        public void setDialog_num(int dialog_num) {
            this.dialog_num = dialog_num;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public void setFrom_user_id(String from_user_id) {
            this.from_user_id = from_user_id;
        }

        public BaseUserBean getUser() {
            return user;
        }

        public void setUser(BaseUserBean user) {
            this.user = user;
        }

        public String getResource_len() {
            return resource_len;
        }

        public void setResource_len(String resource_len) {
            this.resource_len = resource_len;
            voice_len = resource_len;
        }

        public String getResource_url() {
            return resource_url;
        }

        public void setResource_url(String resource_url) {
            this.resource_url = resource_url;
        }

        public String getDialog_id() {
            return dialog_id;
        }

        public void setDialog_id(String dialog_id) {
            this.dialog_id = dialog_id;
        }

        public int getIdentity_type() {
            return identity_type;
        }

        public void setIdentity_type(int identity_type) {
            this.identity_type = identity_type;
        }
    }
}
