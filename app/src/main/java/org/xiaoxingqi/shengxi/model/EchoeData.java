package org.xiaoxingqi.shengxi.model;


import java.util.List;

public class EchoeData extends BaseRepData {
    private List<EchoesBean> data;

    public List<EchoesBean> getData() {
        return data;
    }

    public void setData(List<EchoesBean> data) {
        this.data = data;
    }

    public static class EchoesBean {
        private int id;//  4.3 引入 相当于chat_id
        private int chat_id;
        private int un_read_num;
        private int updated_at;
        private int voice_id;
        private int with_user_id;
        private String with_user_name;
        private int created_at;
        private String with_user_avatar_url;
        private String voice_user_id;
        private int last_resource_type;//最新一条聊天内容的类型 1音频 2 图片
        private int notice_at;
        private int with_user_identity_type;
        private int topic_type;//1 忘记密码
        private int chat_type;// 1回声 2私聊 3绘画
        private int resource_id;//资源id
        private String last_dialog_id;
        private int dialog_num;//对话数量
        private String resource_user_id;

        public int getChat_id() {
            return chat_id;
        }

        public void setChat_id(int chat_id) {
            this.chat_id = chat_id;
        }

        public int getUn_read_num() {
            return un_read_num;
        }

        public void setUn_read_num(int un_read_num) {
            this.un_read_num = un_read_num;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
        }

        public int getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(int voice_id) {
            this.voice_id = voice_id;
        }

        public int getWith_user_id() {
            return with_user_id;
        }

        public void setWith_user_id(int with_user_id) {
            this.with_user_id = with_user_id;
        }

        public String getWith_user_name() {
            return with_user_name;
        }

        public void setWith_user_name(String with_user_name) {
            this.with_user_name = with_user_name;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public String getWith_user_avatar_url() {
            return with_user_avatar_url;
        }

        public void setWith_user_avatar_url(String with_user_avatar_url) {
            this.with_user_avatar_url = with_user_avatar_url;
        }

        public int getLast_resource_type() {
            return last_resource_type;
        }

        public void setLast_resource_type(int last_resource_type) {
            this.last_resource_type = last_resource_type;
        }

        public int getNotice_at() {
            return notice_at;
        }

        public void setNotice_at(int notice_at) {
            this.notice_at = notice_at;
        }

        public String getVoice_user_id() {
            return voice_user_id;
        }

        public void setVoice_user_id(String voice_user_id) {
            this.voice_user_id = voice_user_id;
        }

        public int getWith_user_identity_type() {
            return with_user_identity_type;
        }

        public void setWith_user_identity_type(int with_user_identity_type) {
            this.with_user_identity_type = with_user_identity_type;
        }

        public int getTopic_type() {
            return topic_type;
        }

        public void setTopic_type(int topic_type) {
            this.topic_type = topic_type;
        }

        public int getChat_type() {
            return chat_type;
        }

        public void setChat_type(int chat_type) {
            this.chat_type = chat_type;
        }

        public int getResource_id() {
            return resource_id;
        }

        public void setResource_id(int resource_id) {
            this.resource_id = resource_id;
        }

        public String getLast_dialog_id() {
            return last_dialog_id;
        }

        public void setLast_dialog_id(String last_dialog_id) {
            this.last_dialog_id = last_dialog_id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDialog_num() {
            return dialog_num;
        }

        public void setDialog_num(int dialog_num) {
            this.dialog_num = dialog_num;
        }

        public String getResource_user_id() {
            return resource_user_id;
        }

        public void setResource_user_id(String resource_user_id) {
            this.resource_user_id = resource_user_id;
        }
    }

}
