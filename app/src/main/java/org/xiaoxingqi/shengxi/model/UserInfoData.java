package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfoData extends BaseRepData {


    /**
     * data : {"avatar_url":"http://pfcbgonq6.bkt.clouddn.com/","bg_photo_url":"","frequency_no":106435,"friend_num":0,"mobile":"13662693562","nick_name":"MISMAGIUS","self_intro":"","user_id":1,"voice_num":1,"wave_len":3,"wave_url":"http://pfcbgonq6.bkt.clouddn.com/1_5a4dd21f1c1b9da80bce7435965bfa43.aac"}
     */

    private UserBean data;

    public UserBean getData() {
        return data;
    }

    public void setData(UserBean data) {
        this.data = data;
    }

    public static class UserBean implements Parcelable {
        /**
         * avatar_url : http://pfcbgonq6.bkt.clouddn.com/
         * bg_photo_url :
         * frequency_no : 106435
         * friend_num : 0
         * mobile : 13662693562
         * nick_name : MISMAGIUS
         * self_intro :
         * user_id : 1
         * voice_num : 1
         * wave_len : 3
         * wave_url : http://pfcbgonq6.bkt.clouddn.com/1_5a4dd21f1c1b9da80bce7435965bfa43.aac
         */
        private String id;
        private String avatar_url;
        private String bg_photo_url;
        private String frequency_no;
        private int friend_num;
        private String mobile;
        private String nick_name;
        private String self_intro;
        private String user_id;
        private int voice_num;
        private String wave_len;
        private String wave_url;
        private int relation_status;//用户关系状态，0=不是好友且没拉黑，1=是好友，2=已拉黑，仅当获取不是自己的信息的时候才返回
        private int voice_total_len;//总时长
        private int created_at;
        private boolean isPlaying;
        private String html_id;
        //        private String cover_photo_url;//用户的背景图
        private String voice_cover_photo_url;//时光机的封面图
        private String socket_id;
        private String socket_token;
        private String main_cover_photo_url;
        private String chat_pri_id;//聊天的id
        private String interface_type;//用户的IE类型
        private String interface_type_setted;//仅当用户未主动设置界面风格时，返回该key，值为0
        private long released_at;//封禁释放时间；仅当查看自己的信息时返回
        private String flag = "0";//用户旗帜，1：仙人掌;仅当查看自己的信息时返回
        private String user_status;//用户状态 1:正常，2:用户注销，3:系统封禁/删除
        private int identity_type = 0;//用戶身份类型
        private int in_whitelist = 0;//是否在白名单   1:是，0:否
        private String nick_name_true;

        public UserBean() {

        }

        protected UserBean(Parcel in) {
            id = in.readString();
            avatar_url = in.readString();
            bg_photo_url = in.readString();
            frequency_no = in.readString();
            friend_num = in.readInt();
            mobile = in.readString();
            nick_name = in.readString();
            self_intro = in.readString();
            user_id = in.readString();
            voice_num = in.readInt();
            wave_len = in.readString();
            wave_url = in.readString();
            relation_status = in.readInt();
            country_code = in.readString();
            voice_total_len = in.readInt();
            created_at = in.readInt();
            released_at = in.readLong();
            flag = in.readString();
            user_status = in.readString();
            identity_type = in.readInt();
            in_whitelist = in.readInt();
            nick_name_true = in.readString();
        }

        public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
            @Override
            public UserBean createFromParcel(Parcel in) {
                return new UserBean(in);
            }

            @Override
            public UserBean[] newArray(int size) {
                return new UserBean[size];
            }
        };

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getRelation_status() {
            return relation_status;
        }

        public void setRelation_status(int relation_status) {
            this.relation_status = relation_status;
        }

        public String getCountry_code() {
            return country_code;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        private String country_code;

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getBg_photo_url() {
            return bg_photo_url;
        }

        public void setBg_photo_url(String bg_photo_url) {
            this.bg_photo_url = bg_photo_url;
        }

        public String getFrequency_no() {
            return frequency_no;
        }

        public void setFrequency_no(String frequency_no) {
            this.frequency_no = frequency_no;
        }

        public int getFriend_num() {
            return friend_num;
        }

        public void setFriend_num(int friend_num) {
            this.friend_num = friend_num;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getSelf_intro() {
            return self_intro;
        }

        public void setSelf_intro(String self_intro) {
            this.self_intro = self_intro;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public int getVoice_num() {
            return voice_num;
        }

        public void setVoice_num(int voice_num) {
            this.voice_num = voice_num;
        }

        public String getWave_len() {
            return wave_len;
        }

        public void setWave_len(String wave_len) {
            this.wave_len = wave_len;
        }

        public String getWave_url() {
            return wave_url;
        }

        public void setWave_url(String wave_url) {
            this.wave_url = wave_url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(avatar_url);
            dest.writeString(bg_photo_url);
            dest.writeString(frequency_no);
            dest.writeInt(friend_num);
            dest.writeString(mobile);
            dest.writeString(nick_name);
            dest.writeString(self_intro);
            dest.writeString(user_id);
            dest.writeInt(voice_num);
            dest.writeString(wave_len);
            dest.writeString(wave_url);
            dest.writeInt(relation_status);
            dest.writeString(country_code);
            dest.writeInt(voice_total_len);
            dest.writeInt(created_at);
            dest.writeLong(released_at);
            dest.writeString(flag);
            dest.writeString(user_status);
            dest.writeInt(identity_type);
            dest.writeInt(in_whitelist);
            dest.writeString(nick_name_true);
        }

        public int getVoice_total_len() {
            return voice_total_len;
        }

        public void setVoice_total_len(int voice_total_len) {
            this.voice_total_len = voice_total_len;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public boolean isPlaying() {
            return isPlaying;
        }

        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        public String getHtml_id() {
            return html_id;
        }

        public void setHtml_id(String html_id) {
            this.html_id = html_id;
        }

        public String getVoice_cover_photo_url() {
            return voice_cover_photo_url;
        }

        public void setVoice_cover_photo_url(String voice_cover_photo_url) {
            this.voice_cover_photo_url = voice_cover_photo_url;
        }

        public String getSocket_id() {
            return socket_id;
        }

        public void setSocket_id(String socket_id) {
            this.socket_id = socket_id;
        }

        public String getSocket_token() {
            return socket_token;
        }

        public void setSocket_token(String socket_token) {
            this.socket_token = socket_token;
        }

        public String getMain_cover_photo_url() {
            return main_cover_photo_url;
        }

        public void setMain_cover_photo_url(String main_cover_photo_url) {
            this.main_cover_photo_url = main_cover_photo_url;
        }

        public String getChat_pri_id() {
            return chat_pri_id;
        }

        public void setChat_pri_id(String chat_pri_id) {
            this.chat_pri_id = chat_pri_id;
        }

        public String getInterface_type() {
            return interface_type;
        }

        public void setInterface_type(String interface_type) {
            this.interface_type = interface_type;
        }

        public String getInterface_type_setted() {
            return interface_type_setted;
        }

        public void setInterface_type_setted(String interface_type_setted) {
            this.interface_type_setted = interface_type_setted;
        }

        public long getReleased_at() {
            return released_at;
        }

        public void setReleased_at(long released_at) {
            this.released_at = released_at;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getUser_status() {
            return user_status;
        }

        public void setUser_status(String user_status) {
            this.user_status = user_status;
        }

        public int getIdentity_type() {
            return identity_type;
        }

        public void setIdentity_type(int identity_type) {
            this.identity_type = identity_type;
        }

        public int getIn_whitelist() {
            return in_whitelist;
        }

        public void setIn_whitelist(int in_whitelist) {
            this.in_whitelist = in_whitelist;
        }

        public String getNick_name_true() {
            return nick_name_true;
        }

        public void setNick_name_true(String nick_name_true) {
            this.nick_name_true = nick_name_true;
        }
    }
}
