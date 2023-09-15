package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yzm on 2018/3/5.
 */

public class BaseImg extends DataTitleBean implements Parcelable {

    private String url;
    private String time;
    /**
     * 是否为标题
     */
    private String title;
    private String img_url;
    private String id;
    private BaseBean voiceBean;
    private String img_title;//查看别人相册的角标

    public BaseImg(boolean isSelect, String title) {
        this.isSelect = isSelect;
        this.title = title;
    }

    public BaseImg() {

    }

    public BaseImg(String url) {
        this.url = url;
    }

    protected BaseImg(Parcel in) {
        super(in);
        url = in.readString();
        time = in.readString();
        title = in.readString();
        img_url = in.readString();
        id = in.readString();
        img_title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(url);
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(img_url);
        dest.writeString(id);
        dest.writeString(img_title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseImg> CREATOR = new Creator<BaseImg>() {
        @Override
        public BaseImg createFromParcel(Parcel in) {
            return new BaseImg(in);
        }

        @Override
        public BaseImg[] newArray(int size) {
            return new BaseImg[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public BaseBean getVoiceBean() {
        return voiceBean;
    }

    public void setVoiceBean(BaseBean voiceBean) {
        this.voiceBean = voiceBean;
    }
}
