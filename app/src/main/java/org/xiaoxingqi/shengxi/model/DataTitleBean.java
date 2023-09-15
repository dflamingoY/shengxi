package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DataTitleBean implements Parcelable {
    /**
     * 是否为标题
     */
    protected boolean isSelect;

    public DataTitleBean() {

    }

    protected DataTitleBean(Parcel in) {
        isSelect = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelect ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DataTitleBean> CREATOR = new Creator<DataTitleBean>() {
        @Override
        public DataTitleBean createFromParcel(Parcel in) {
            return new DataTitleBean(in);
        }

        @Override
        public DataTitleBean[] newArray(int size) {
            return new DataTitleBean[size];
        }
    };

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
