package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SubscribeBean implements Parcelable {
    private int id;
    private int subscription_type;

    public SubscribeBean() {

    }

    protected SubscribeBean(Parcel in) {
        id = in.readInt();
        subscription_type = in.readInt();
    }

    public static final Creator<SubscribeBean> CREATOR = new Creator<SubscribeBean>() {
        @Override
        public SubscribeBean createFromParcel(Parcel in) {
            return new SubscribeBean(in);
        }

        @Override
        public SubscribeBean[] newArray(int size) {
            return new SubscribeBean[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubscription_type() {
        return subscription_type;
    }

    public void setSubscription_type(int subscription_type) {
        this.subscription_type = subscription_type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(subscription_type);
    }
}
