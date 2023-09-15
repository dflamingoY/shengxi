package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MovieData extends BaseRepData {

    private List<MovieBean> data;

    public List<MovieBean> getData() {
        return data;
    }

    public void setData(List<MovieBean> data) {
        this.data = data;
    }

    public static class MovieBean implements Parcelable {
        private String movie_id;
        private String movie_title;
        private String[] movie_type;
        private int movie_len;
        private String movie_poster;
        private int movie_score;
        private String[] movie_starring;//主演
        private String released_at; //上映时间
        private String[] movie_area;
        private String movie_intro;
        private String id;

        public MovieBean() {

        }

        public String getMovie_id() {
            return movie_id;
        }

        public void setMovie_id(String movie_id) {
            this.movie_id = movie_id;
        }

        public String getMovie_title() {
            return movie_title;
        }

        public void setMovie_title(String movie_title) {
            this.movie_title = movie_title;
        }

        public String[] getMovie_type() {
            return movie_type;
        }

        public void setMovie_type(String[] movie_type) {
            this.movie_type = movie_type;
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

        public String[] getMovie_starring() {
            return movie_starring;
        }

        public void setMovie_starring(String[] movie_starring) {
            this.movie_starring = movie_starring;
        }

        public String getReleased_at() {
            return released_at;
        }

        public void setReleased_at(String released_at) {
            this.released_at = released_at;
        }

        public String[] getMovie_area() {
            return movie_area;
        }

        public void setMovie_area(String[] movie_area) {
            this.movie_area = movie_area;
        }

        protected MovieBean(Parcel in) {
            movie_id = in.readString();
            movie_title = in.readString();
            movie_type = in.createStringArray();
            movie_len = in.readInt();
            movie_poster = in.readString();
            movie_score = in.readInt();
            movie_starring = in.createStringArray();
            released_at = in.readString();
            movie_area = in.createStringArray();
            movie_intro = in.readString();
            id = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(movie_id);
            dest.writeString(movie_title);
            dest.writeStringArray(movie_type);
            dest.writeInt(movie_len);
            dest.writeString(movie_poster);
            dest.writeInt(movie_score);
            dest.writeStringArray(movie_starring);
            dest.writeString(released_at);
            dest.writeStringArray(movie_area);
            dest.writeString(movie_intro);
            dest.writeString(id);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<MovieBean> CREATOR = new Creator<MovieBean>() {
            @Override
            public MovieBean createFromParcel(Parcel in) {
                return new MovieBean(in);
            }

            @Override
            public MovieBean[] newArray(int size) {
                return new MovieBean[size];
            }
        };

        public String getMovie_intro() {
            return movie_intro;
        }

        public void setMovie_intro(String movie_intro) {
            this.movie_intro = movie_intro;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }


}
