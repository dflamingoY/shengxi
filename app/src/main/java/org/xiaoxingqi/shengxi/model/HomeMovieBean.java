package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class HomeMovieBean implements Parcelable {


    /**
     * movie_area : ["列支敦士登"]
     * movie_id : 2
     * movie_intro : 脱衣舞女安娜对一个神秘的女客人一见钟情,辞随其回到别墅,之后安娜开始不停做噩梦,在梦中她毫无目的地杀着人.神秘女人,精神病医生,还有隔壁监视着他们一举一动的一男一女......安娜怀疑自己发了疯,精神陷入一片混乱之中......
     * movie_len : 85
     * movie_poster : http://qiniu-dev.byebyetext.com/2_poster.jpg
     * movie_score : 0
     * movie_starring : ["ColetteGiacobine","杰克·泰勒","保罗·穆勒","黛安娜·洛瑞斯"]
     * movie_title : 噩梦连连
     * movie_type : ["剧情","惊悚","恐怖"]
     * released_at : 0
     * user_score : 50
     */

    private String movie_id;
    private String movie_intro;
    private int movie_len;
    private String movie_poster;
    private int movie_score;
    private String movie_title;
    private int released_at;
    private int user_score;
    private String[] movie_area;
    private String[] movie_starring;
    private String[] movie_type;

    public HomeMovieBean() {

    }


    protected HomeMovieBean(Parcel in) {
        movie_id = in.readString();
        movie_intro = in.readString();
        movie_len = in.readInt();
        movie_poster = in.readString();
        movie_score = in.readInt();
        movie_title = in.readString();
        released_at = in.readInt();
        user_score = in.readInt();
        movie_area = in.createStringArray();
        movie_starring = in.createStringArray();
        movie_type = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movie_id);
        dest.writeString(movie_intro);
        dest.writeInt(movie_len);
        dest.writeString(movie_poster);
        dest.writeInt(movie_score);
        dest.writeString(movie_title);
        dest.writeInt(released_at);
        dest.writeInt(user_score);
        dest.writeStringArray(movie_area);
        dest.writeStringArray(movie_starring);
        dest.writeStringArray(movie_type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HomeMovieBean> CREATOR = new Creator<HomeMovieBean>() {
        @Override
        public HomeMovieBean createFromParcel(Parcel in) {
            return new HomeMovieBean(in);
        }

        @Override
        public HomeMovieBean[] newArray(int size) {
            return new HomeMovieBean[size];
        }
    };

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
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

    public int getUser_score() {
        return user_score;
    }

    public void setUser_score(int user_score) {
        this.user_score = user_score;
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
}
