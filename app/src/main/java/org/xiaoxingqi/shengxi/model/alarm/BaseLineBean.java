package org.xiaoxingqi.shengxi.model.alarm;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseLineBean implements Parcelable {

    private String id;
    private String dubbing_num;
    private String line_content;
    private String tag_id;
    private String tag_name;

    public BaseLineBean() {

    }

    protected BaseLineBean(Parcel in) {
        id = in.readString();
        dubbing_num = in.readString();
        line_content = in.readString();
        tag_id = in.readString();
        tag_name = in.readString();
    }

    public static final Creator<BaseLineBean> CREATOR = new Creator<BaseLineBean>() {
        @Override
        public BaseLineBean createFromParcel(Parcel in) {
            return new BaseLineBean(in);
        }

        @Override
        public BaseLineBean[] newArray(int size) {
            return new BaseLineBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDubbing_num() {
        return dubbing_num;
    }

    public void setDubbing_num(String dubbing_num) {
        this.dubbing_num = dubbing_num;
    }

    public String getLine_content() {
        return line_content;
    }

    public void setLine_content(String line_content) {
        this.line_content = line_content;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(dubbing_num);
        dest.writeString(line_content);
        dest.writeString(tag_id);
        dest.writeString(tag_name);
    }
}
