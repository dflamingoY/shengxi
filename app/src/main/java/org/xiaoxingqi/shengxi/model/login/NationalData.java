package org.xiaoxingqi.shengxi.model.login;

import android.os.Parcel;
import android.os.Parcelable;

import org.xiaoxingqi.shengxi.model.BaseRepData;

import java.util.List;

public class NationalData extends BaseRepData {

    private NationalBean data;

    public NationalBean getData() {
        return data;
    }

    public void setData(NationalBean data) {
        this.data = data;
    }

    public static class NationalBean {
        private List<NationalEntity> hot;
        private List<NationalEntity> other;

        public List<NationalEntity> getHot() {
            return hot;
        }

        public void setHot(List<NationalEntity> hot) {
            this.hot = hot;
        }

        public List<NationalEntity> getOther() {
            return other;
        }

        public void setOther(List<NationalEntity> other) {
            this.other = other;
        }
    }

    public static class NationalEntity implements Parcelable {
        private String phone_code;//86
        private String area_name;//cn
        private String area_code;

        public NationalEntity() {

        }

        public NationalEntity(String phone_code, String area_code) {
            this.area_code = area_code;
            this.phone_code = phone_code;
        }

        protected NationalEntity(Parcel in) {
            phone_code = in.readString();
            area_name = in.readString();
            area_code = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(phone_code);
            dest.writeString(area_name);
            dest.writeString(area_code);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<NationalEntity> CREATOR = new Creator<NationalEntity>() {
            @Override
            public NationalEntity createFromParcel(Parcel in) {
                return new NationalEntity(in);
            }

            @Override
            public NationalEntity[] newArray(int size) {
                return new NationalEntity[size];
            }
        };

        public String getArea_code() {
            return area_code;
        }

        public void setArea_code(String area_code) {
            this.area_code = area_code;
        }

        public String getArea_name() {
            return area_name;
        }

        public void setArea_name(String area_name) {
            this.area_name = area_name;
        }

        public String getPhone_code() {
            return phone_code;
        }

        public void setPhone_code(String phone_code) {
            this.phone_code = phone_code;
        }
    }

}
