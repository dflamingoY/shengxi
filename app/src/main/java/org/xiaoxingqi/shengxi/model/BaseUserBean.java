package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseUserBean implements Parcelable {
    protected BaseUserBean(Parcel in) {
        avatar_url = in.readString();
        id = in.readString();
        nick_name = in.readString();
        release_at = in.readInt();
        user_id = in.readString();
        movie_voice = in.readString();
        common_num = in.readString();
        isSelected = in.readByte() != 0;
        artwork_id = in.readInt();
        created_at = in.readInt();
        from_user_id = in.readString();
        like_type = in.readInt();
        identity_type = in.readInt();
        total = in.readString();
    }

    public BaseUserBean() {
    }

    public static final Creator<BaseUserBean> CREATOR = new Creator<BaseUserBean>() {
        @Override
        public BaseUserBean createFromParcel(Parcel in) {
            return new BaseUserBean(in);
        }

        @Override
        public BaseUserBean[] newArray(int size) {
            return new BaseUserBean[size];
        }
    };

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    private String avatar_url;
    private String id;
    private String nick_name;
    private int release_at;
    private String user_id;
    private String movie_voice;
    private String common_num;//共同爱好的数量
    private boolean isSelected;
    private int artwork_id;//专用绘图
    private int created_at;//专用绘图
    private String from_user_id;//专用绘图
    private int like_type;//专用绘图
    private int identity_type;
    private int renewed_at;
    private int renew_months;//连续好友天数
    private String total;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getRelease_at() {
        return release_at;
    }

    public void setRelease_at(int release_at) {
        this.release_at = release_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getMovie_voice() {
        return movie_voice;
    }

    public void setMovie_voice(String movie_voice) {
        this.movie_voice = movie_voice;
    }

    public String getCommon_num() {
        return common_num;
    }

    public void setCommon_num(String common_num) {
        this.common_num = common_num;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar_url);
        dest.writeString(id);
        dest.writeString(nick_name);
        dest.writeInt(release_at);
        dest.writeString(user_id);
        dest.writeString(movie_voice);
        dest.writeString(common_num);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeInt(artwork_id);
        dest.writeInt(created_at);
        dest.writeString(from_user_id);
        dest.writeInt(like_type);
        dest.writeInt(identity_type);
        dest.writeString(total);
    }

    public int getArtwork_id() {
        return artwork_id;
    }

    public void setArtwork_id(int artwork_id) {
        this.artwork_id = artwork_id;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(String from_user_id) {
        this.from_user_id = from_user_id;
    }

    public int getLike_type() {
        return like_type;
    }

    public void setLike_type(int like_type) {
        this.like_type = like_type;
    }

    public int getIdentity_type() {
        return identity_type;
    }

    public void setIdentity_type(int identity_type) {
        this.identity_type = identity_type;
    }

    public int getRenewed_at() {
        return renewed_at;
    }

    public void setRenewed_at(int renewed_at) {
        this.renewed_at = renewed_at;
    }

    public int getRenew_months() {
        return renew_months;
    }

    public void setRenew_months(int renew_months) {
        this.renew_months = renew_months;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
