package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class ListenFriendData extends BaseRepData {

    private ListenFriendDataX data;

    public ListenFriendDataX getData() {
        return data;
    }

    public void setData(ListenFriendDataX data) {
        this.data = data;
    }

    public static class ListenFriendDataX {
        private FriendsListBean self;
        private List<FriendsListBean> other;

        public FriendsListBean getSelf() {
            return self;
        }

        public void setSelf(FriendsListBean self) {
            this.self = self;
        }

        public List<FriendsListBean> getOther() {
            return other;
        }

        public void setOther(List<FriendsListBean> other) {
            this.other = other;
        }
    }

    public static class FriendsListBean extends BaseAnimBean {
        private String id;//用户ID
        private String nick_name;
        private String avatar_url;
        private String self_intro;
        private String wave_url;
        private String wave_len;
        private int friend_num;//好友数量
        private String friend_card_url;
        private String friend_status;//好友关系，1:待验证，2:已经是好友，其他情况不返回该key
        private int is_on;
        private String tips;
        private int song_num;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getSelf_intro() {
            return self_intro;
        }

        public void setSelf_intro(String self_intro) {
            this.self_intro = self_intro;
        }

        public String getWave_url() {
            return wave_url;
        }

        public void setWave_url(String wave_url) {
            this.wave_url = wave_url;
        }

        public String getWave_len() {
            return wave_len;
        }

        public void setWave_len(String wave_len) {
            this.wave_len = wave_len;
        }

        public int getFriend_num() {
            return friend_num;
        }

        public void setFriend_num(int friend_num) {
            this.friend_num = friend_num;
        }

        public String getFriend_card_url() {
            return friend_card_url;
        }

        public void setFriend_card_url(String friend_card_url) {
            this.friend_card_url = friend_card_url;
        }

        public String getFriend_status() {
            return friend_status;
        }

        public void setFriend_status(String friend_status) {
            this.friend_status = friend_status;
        }

        public int getIs_on() {
            return is_on;
        }

        public void setIs_on(int is_on) {
            this.is_on = is_on;
        }

        public String getTips() {
            return tips;
        }

        public void setTips(String tips) {
            this.tips = tips;
        }

        public int getSong_num() {
            return song_num;
        }

        public void setSong_num(int song_num) {
            this.song_num = song_num;
        }
    }
}
