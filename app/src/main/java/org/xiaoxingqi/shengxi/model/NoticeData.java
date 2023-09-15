package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class NoticeData extends BaseRepData {
    public List<NoticeBean> getData() {
        return data;
    }

    public void setData(List<NoticeBean> data) {
        this.data = data;
    }

    private List<NoticeBean> data;

    public static class NoticeBean {
        private int type;//1=共鸣，2=表白，3=新好友
        private int created_at;
        private String from_user_id;
        private String nick_name;
        private String tips;
        private String id;
        private String voice_id;
        private int message_type;//1=系统消息，2=新好友请求，3=共鸣，4=表白，5=好友通知，6=新的会话，7=新的对话，8=系统回复 14心情关注  16台词配音 17下载配音 18绘画作品点赞  19 涂鸦作品 20涂鸦作品点赞 21 台词点赞 22:配音点赞
        private String html_id;
        private long open_at;//是否展示对方的名字
        private long release_at;
        private String line_id;//台词id
        private String dubbing_id;//配音ID
        private String artwork_id;//绘画作品ID，仅当message_type=18/19/20时返回
        private String graffiti_id;//涂鸦作品ID，仅当message_type=19/20时返回
        private int vote_option;//1 2 3 1:天使，2：魔鬼，3:神
        private int like_type;//1 私密  2 公开  绘画作品
        private int friend_from;//汇总消息类型，1=系统消息，2=新好友请求，3=共鸣/表白/好友通知/系统回复留言
        private int is_anonymous;//共鸣是否匿名展示

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public void setFrom_user_id(String from_user_id) {
            this.from_user_id = from_user_id;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getTips() {
            return tips;
        }

        public void setTips(String tips) {
            this.tips = tips;
        }

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getMessage_type() {
            return message_type;
        }

        public void setMessage_type(int message_type) {
            this.message_type = message_type;
        }

        public String getHtml_id() {
            return html_id;
        }

        public void setHtml_id(String html_id) {
            this.html_id = html_id;
        }

        public long getOpen_at() {
            return open_at;
        }

        public void setOpen_at(long open_at) {
            this.open_at = open_at;
        }

        public long getRelease_at() {
            return release_at;
        }

        public void setRelease_at(long release_at) {
            this.release_at = release_at;
        }

        public String getLine_id() {
            return line_id;
        }

        public void setLine_id(String line_id) {
            this.line_id = line_id;
        }

        public String getDubbing_id() {
            return dubbing_id;
        }

        public void setDubbing_id(String dubbing_id) {
            this.dubbing_id = dubbing_id;
        }

        public String getArtwork_id() {
            return artwork_id;
        }

        public void setArtwork_id(String artwork_id) {
            this.artwork_id = artwork_id;
        }

        public String getGraffiti_id() {
            return graffiti_id;
        }

        public void setGraffiti_id(String graffiti_id) {
            this.graffiti_id = graffiti_id;
        }

        public int getVote_option() {
            return vote_option;
        }

        public void setVote_option(int vote_option) {
            this.vote_option = vote_option;
        }

        public int getLike_type() {
            return like_type;
        }

        public void setLike_type(int like_type) {
            this.like_type = like_type;
        }

        public int getFriend_from() {
            return friend_from;
        }

        public void setFriend_from(int friend_from) {
            this.friend_from = friend_from;
        }

        public int getIs_anonymous() {
            return is_anonymous;
        }

        public void setIs_anonymous(int is_anonymous) {
            this.is_anonymous = is_anonymous;
        }
    }
}
