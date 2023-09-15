package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseSearchBean implements Parcelable {

    /**
     * book_author : 〔美〕格雷格·麦科尔森（Gregg Michaelsen）
     * book_cover : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/book/730b3999a3cb46f53a8f85243abc9996_cover.jpg
     * book_intro : 你的男友是否总是对你趾高气昂？你是否已经厌倦了那些迟迟不肯给你承诺的废柴？男人们经常跟你提出分手吗？或者，你仅仅是想知道，怎样做才能找到男朋友？本书用“致命”5招，教你如何让男人对你俯首称臣，每招每式都可现学现用。让这本美国亚马逊销量第一的社交指南，一次解决你所有恋爱难题！    格雷格·麦科尔森是美国波士顿顶尖恋爱顾问，他的作品是亚马逊恋爱建议类的销售冠军。他与成千上万对情侣、夫妻进行沟通，了解他们关系成功或失败的原因，并将这些分析用于实践，指导男人、女人如何更好地与彼此沟通相处，以维持持久的爱情。同时，他拥有最受欢迎的女性交友网站WhoHoldsTheCardsNow.com。（读读作者的人生故事，你会更了解他的说法）
     * book_isbn : 9781620962435
     * book_name : 牌在谁手上？五个绝招让他对你一见倾心，俯首称臣
     * book_publisher : Fiberead
     * book_score : 0
     * id : 12
     * published_date : 2017-04
     */

    private String book_author;
    private String book_cover;
    private String book_intro;
    private String book_isbn;
    private String book_name;
    private String book_publisher;
    private int book_score;
    private String id;
    private String published_date;
    /**
     * id : 926
     * movie_area : ["日本"]
     * movie_id : 926
     * movie_intro : 在东京都葛饰区小菅的某座公寓里，发现了一具被勒死的女性遗体。被害者是在清洁公司上班、住在滋贺县的押谷道子。而杀人现场的住户越川睦夫也下落不明。而以松宫（沟端淳平 饰）等警视厅搜查一科的刑警们查不出越川与押谷道子有何交集，案件调查陷入困境。尽管查明押谷道子是为了拜访她的同学，即导演浅居博美（松岛菜菜子 饰）才来东京的，但博美与越川之间也找不出什么交集。这时，案发地点附近发现了被烧死的尸体，松宫怀疑两者有关联。从遗物中，发现了写有日本桥周围12座桥名的文字。知道此事后加贺恭一郎（阿部宽 饰）非常激动，因为那东西与他的亡母大有关联。
     * movie_len : 119
     * movie_poster : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/movie/926_poster.jpg?x-oss-process=image/resize,m_lfit,w_160,limit_0/quality,q_90/format,webp
     * movie_score : 0
     * movie_starring : ["田中丽奈","阿部宽","小日向文世","沟端淳平"]
     * movie_title : 祈祷落幕时
     * movie_type : ["剧情"]
     * released_at : 1516982400
     */

    private int movie_id;
    private String movie_intro;
    private int movie_len;
    private String movie_poster;
    private int movie_score;
    private String movie_title;
    private int released_at;
    private String[] movie_area;
    private String[] movie_starring;
    private String[] movie_type;
    /**
     * album_cover : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/album/7ad063fb3d0b2c55b64edbe3e596b08d_poster.jpg
     * album_genre : 流行
     * album_name : 初恋
     * album_singer : 宇多田ヒカル
     * song_name : 10. 大空で抱きしめて
     */

    private String song_cover;//封面
    private String song_genre;//歌曲类型
    private String album_name;//专辑名称
    private String song_singer;//歌手
    private String song_name;//歌曲名

    private String rate_id;//最后一个评论的ID,rate_id
    private String released_date;
    private SubscribeBean subscription;
    private String song_id;
    private String book_id;

    private BaseBean first_voice;

    public BaseSearchBean() {

    }

    protected BaseSearchBean(Parcel in) {
        book_author = in.readString();
        book_cover = in.readString();
        book_intro = in.readString();
        book_isbn = in.readString();
        book_name = in.readString();
        book_publisher = in.readString();
        book_score = in.readInt();
        id = in.readString();
        published_date = in.readString();
        movie_id = in.readInt();
        movie_intro = in.readString();
        movie_len = in.readInt();
        movie_poster = in.readString();
        movie_score = in.readInt();
        movie_title = in.readString();
        released_at = in.readInt();
        movie_area = in.createStringArray();
        movie_starring = in.createStringArray();
        movie_type = in.createStringArray();
        song_cover = in.readString();
        song_genre = in.readString();
        album_name = in.readString();
        song_singer = in.readString();
        song_name = in.readString();
        rate_id = in.readString();
        released_date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(book_author);
        dest.writeString(book_cover);
        dest.writeString(book_intro);
        dest.writeString(book_isbn);
        dest.writeString(book_name);
        dest.writeString(book_publisher);
        dest.writeInt(book_score);
        dest.writeString(id);
        dest.writeString(published_date);
        dest.writeInt(movie_id);
        dest.writeString(movie_intro);
        dest.writeInt(movie_len);
        dest.writeString(movie_poster);
        dest.writeInt(movie_score);
        dest.writeString(movie_title);
        dest.writeInt(released_at);
        dest.writeStringArray(movie_area);
        dest.writeStringArray(movie_starring);
        dest.writeStringArray(movie_type);
        dest.writeString(song_cover);
        dest.writeString(song_genre);
        dest.writeString(album_name);
        dest.writeString(song_singer);
        dest.writeString(song_name);
        dest.writeString(rate_id);
        dest.writeString(released_date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseSearchBean> CREATOR = new Creator<BaseSearchBean>() {
        @Override
        public BaseSearchBean createFromParcel(Parcel in) {
            return new BaseSearchBean(in);
        }

        @Override
        public BaseSearchBean[] newArray(int size) {
            return new BaseSearchBean[size];
        }
    };

    public String getBook_author() {
        return book_author;
    }

    public void setBook_author(String book_author) {
        this.book_author = book_author;
    }

    public String getBook_cover() {
        return book_cover;
    }

    public void setBook_cover(String book_cover) {
        this.book_cover = book_cover;
    }

    public String getBook_intro() {
        return book_intro;
    }

    public void setBook_intro(String book_intro) {
        this.book_intro = book_intro;
    }

    public String getBook_isbn() {
        return book_isbn;
    }

    public void setBook_isbn(String book_isbn) {
        this.book_isbn = book_isbn;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_publisher() {
        return book_publisher;
    }

    public void setBook_publisher(String book_publisher) {
        this.book_publisher = book_publisher;
    }

    public int getBook_score() {
        return book_score;
    }

    public void setBook_score(int book_score) {
        this.book_score = book_score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublished_date() {
        return published_date;
    }

    public void setPublished_date(String published_date) {
        this.published_date = published_date;
    }

    public int getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }

    public String getMovie_intro() {
        return movie_intro;
    }

    public void setMovie_intro(String movie_intro) {
        this.movie_intro = movie_intro;
    }

    public int getMovie_len() {
        return movie_len;
    }

    public void setMovie_len(int movie_len) {
        this.movie_len = movie_len;
    }

    public String getMovie_poster() {
        return movie_poster;
    }

    public void setMovie_poster(String movie_poster) {
        this.movie_poster = movie_poster;
    }

    public int getMovie_score() {
        return movie_score;
    }

    public void setMovie_score(int movie_score) {
        this.movie_score = movie_score;
    }

    public String getMovie_title() {
        return movie_title;
    }

    public void setMovie_title(String movie_title) {
        this.movie_title = movie_title;
    }

    public int getReleased_at() {
        return released_at;
    }

    public void setReleased_at(int released_at) {
        this.released_at = released_at;
    }

    public String[] getMovie_area() {
        return movie_area;
    }

    public void setMovie_area(String[] movie_area) {
        this.movie_area = movie_area;
    }

    public String[] getMovie_starring() {
        return movie_starring;
    }

    public void setMovie_starring(String[] movie_starring) {
        this.movie_starring = movie_starring;
    }

    public String[] getMovie_type() {
        return movie_type;
    }

    public void setMovie_type(String[] movie_type) {
        this.movie_type = movie_type;
    }

    public String getSong_cover() {
        return song_cover;
    }

    public void setSong_cover(String album_cover) {
        this.song_cover = album_cover;
    }

    public String getSong_genre() {
        return song_genre;
    }

    public void setSong_genre(String album_genre) {
        this.song_genre = album_genre;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getSong_singer() {
        return song_singer;
    }

    public void setSong_singer(String album_singer) {
        this.song_singer = album_singer;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getRate_id() {
        return rate_id;
    }

    public void setRate_id(String rate_id) {
        this.rate_id = rate_id;
    }

    public String getReleased_date() {
        return released_date;
    }

    public void setReleased_date(String released_date) {
        this.released_date = released_date;
    }

    public SubscribeBean getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscribeBean subscription) {
        this.subscription = subscription;
    }

    public BaseBean getFirst_voice() {
        return first_voice;
    }

    public void setFirst_voice(BaseBean first_voice) {
        this.first_voice = first_voice;
    }

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }
}
