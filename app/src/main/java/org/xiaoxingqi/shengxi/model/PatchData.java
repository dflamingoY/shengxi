package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class PatchData extends BaseRepData {

    public PatchBean getData() {
        return data;
    }

    public void setData(PatchBean data) {
        this.data = data;
    }

    private PatchBean data;

    public static class PatchBean {
        private String user_id;
        private int chat_remind;//回声提醒开关，0=关闭提醒，1=开启提醒，默认开启
        private int chat_pri_remind;//私聊提醒开关，0=关闭提醒，1=开启提醒，默认开启
        private int new_friend_remind;//新好友请求通知提醒开关,0=关闭提醒，1=开启提醒，默认开启
        private int sys_remind;//系统消息通知提醒开关，0=关闭提醒，1=开启提醒，默认开启
        private int chat_with;//谁可以向我回声，0=所有人，1=仅限好友,默认所有人
        private int chat_pri_with;//谁可以和我私聊，0=所有人，1=仅限好友，默认所有人
        private int strange_view;//陌生人浏览我的声兮和相册 ，-1:全部 0=禁止，7=默认7天 30:浏览30天
        private int gps_switch;//距离功能，0=关闭，1=开启，默认关闭
        private int other_remind;//共鸣/表白/新好友通知
        private int auto_reply;//是否繁忙状态
        private FilmReview film_review;
        private FilmReview book_review;
        private FilmReview song_review;
        private int favorite_topic;//0:所有人可以查看，1:仅限好友

        private int join_sing_ranking;//是否展示唱歌幫
        private List<EchoTypesData.EchoTypesBean> chat_tips;//
        private EchoTypesData.EchoTypesBean chat_hobby;//
        private int bucket_id;
        private int looking_for;
        private ShakeBean shake;//摇一摇设置
        private int display_same_topic;//是否允许听见页话题里展示同话题心情  1:是，0：否

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public int getChat_remind() {
            return chat_remind;
        }

        public void setChat_remind(int chat_remind) {
            this.chat_remind = chat_remind;
        }

        public int getChat_pri_remind() {
            return chat_pri_remind;
        }

        public void setChat_pri_remind(int chat_pri_remind) {
            this.chat_pri_remind = chat_pri_remind;
        }

        public int getNew_friend_remind() {
            return new_friend_remind;
        }

        public void setNew_friend_remind(int new_friend_remind) {
            this.new_friend_remind = new_friend_remind;
        }

        public int getSys_remind() {
            return sys_remind;
        }

        public void setSys_remind(int sys_remind) {
            this.sys_remind = sys_remind;
        }

        public int getChat_with() {
            return chat_with;
        }

        public void setChat_with(int chat_with) {
            this.chat_with = chat_with;
        }

        public int getChat_pri_with() {
            return chat_pri_with;
        }

        public void setChat_pri_with(int chat_pri_with) {
            this.chat_pri_with = chat_pri_with;
        }

        public int getStrange_view() {
            return strange_view;
        }

        public void setStrange_view(int strange_view) {
            this.strange_view = strange_view;
        }

        public int getGps_switch() {
            return gps_switch;
        }

        public void setGps_switch(int gps_switch) {
            this.gps_switch = gps_switch;
        }

        public int getOther_remind() {
            return other_remind;
        }

        public void setOther_remind(int other_remind) {
            this.other_remind = other_remind;
        }

        public int getAuto_reply() {
            return auto_reply;
        }

        public void setAuto_reply(int auto_reply) {
            this.auto_reply = auto_reply;
        }

        public FilmReview getFilm_review() {
            return film_review;
        }

        public void setFilm_review(FilmReview film_review) {
            this.film_review = film_review;
        }

        public FilmReview getBook_review() {
            return book_review;
        }

        public void setBook_review(FilmReview book_review) {
            this.book_review = book_review;
        }

        public FilmReview getSong_review() {
            return song_review;
        }

        public void setSong_review(FilmReview song_review) {
            this.song_review = song_review;
        }

        public int getFavorite_topic() {
            return favorite_topic;
        }

        public void setFavorite_topic(int favorite_topic) {
            this.favorite_topic = favorite_topic;
        }

        public int getJoin_sing_ranking() {
            return join_sing_ranking;
        }

        public void setJoin_sing_ranking(int join_sing_ranking) {
            this.join_sing_ranking = join_sing_ranking;
        }

        public List<EchoTypesData.EchoTypesBean> getChat_tips() {
            return chat_tips;
        }

        public void setChat_tips(List<EchoTypesData.EchoTypesBean> chat_tips) {
            this.chat_tips = chat_tips;
        }

        public EchoTypesData.EchoTypesBean getChat_hobby() {
            return chat_hobby;
        }

        public void setChat_hobby(EchoTypesData.EchoTypesBean chat_hobby) {
            this.chat_hobby = chat_hobby;
        }

        public int getBucket_id() {
            return bucket_id;
        }

        public void setBucket_id(int bucket_id) {
            this.bucket_id = bucket_id;
        }

        public int getLooking_for() {
            return looking_for;
        }

        public void setLooking_for(int looking_for) {
            this.looking_for = looking_for;
        }

        public ShakeBean getShake() {
            return shake;
        }

        public void setShake(ShakeBean shake) {
            this.shake = shake;
        }

        public int getDisplay_same_topic() {
            return display_same_topic;
        }

        public void setDisplay_same_topic(int display_same_topic) {
            this.display_same_topic = display_same_topic;
        }
    }

    public static class ShakeBean {

        private int recent;
        private int movie;
        private int book;
        private int song;

        public int getSong() {
            return song;
        }

        public void setSong(int song) {
            this.song = song;
        }

        public int getBook() {
            return book;
        }

        public void setBook(int book) {
            this.book = book;
        }

        public int getMovie() {
            return movie;
        }

        public void setMovie(int movie) {
            this.movie = movie;
        }

        public int getRecent() {
            return recent;
        }

        public void setRecent(int recent) {
            this.recent = recent;
        }
    }


    public static class FilmReview {

        /**
         * homepage 在我和好友的首页是否显示,1：显示，0：隐藏
         * review  谁可以浏览我影评列表，0：所有人，1：仅限好友
         * machine  在心情簿-时光机是否显示 1：显示，0：隐藏
         * memory  在心情簿-记忆是否显示 1：显示，0：隐藏
         * square  在评电影广场是否显示 1：显示，0：隐藏
         */
        private int homepage;
        private int machine;
        private int memory;
        private int square;
        private int review;//评论列表，0：无限制，1：仅限好友
        private int entry;//词条页
        private int hobby;//同好
        private int list;//谁可以浏览我歌评列表，0：所有人，1：仅限好友
        private int subscribe;//1:显示，0:隐藏

        public int getHomepage() {
            return homepage;
        }

        public void setHomepage(int homepage) {
            this.homepage = homepage;
        }

        public int getMachine() {
            return machine;
        }

        public void setMachine(int machine) {
            this.machine = machine;
        }

        public int getMemory() {
            return memory;
        }

        public void setMemory(int memory) {
            this.memory = memory;
        }

        public int getSquare() {
            return square;
        }

        public void setSquare(int square) {
            this.square = square;
        }

        public int getReview() {
            return review;
        }

        public void setReview(int review) {
            this.review = review;
        }

        public int getEntry() {
            return entry;
        }

        public void setEntry(int entry) {
            this.entry = entry;
        }

        public int getHobby() {
            return hobby;
        }

        public void setHobby(int hobby) {
            this.hobby = hobby;
        }

        public int getList() {
            return list;
        }

        public void setList(int list) {
            this.list = list;
        }

        public int getSubscribe() {
            return subscribe;
        }

        public void setSubscribe(int subscribe) {
            this.subscribe = subscribe;
        }
    }
}
