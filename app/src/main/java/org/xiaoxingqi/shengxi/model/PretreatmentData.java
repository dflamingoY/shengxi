package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PretreatmentData extends BaseRepData {
    private List<PretreatmentBean> data;

    public List<PretreatmentBean> getData() {
        return data;
    }

    public void setData(List<PretreatmentBean> data) {
        this.data = data;
    }

    public static class PretreatmentBean extends BaseAnimBean implements Parcelable {

        private String resource_url;
        private String resource_type;
        private String resource_uri;
        private String resource_len;
        private int bucket_id;
        private String reply_remark;
        private String id;

        public PretreatmentBean() {

        }

        protected PretreatmentBean(Parcel in) {
            super(in);
            resource_url = in.readString();
            resource_type = in.readString();
            resource_uri = in.readString();
            resource_len = in.readString();
            bucket_id = in.readInt();
            reply_remark = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(resource_url);
            dest.writeString(resource_type);
            dest.writeString(resource_uri);
            dest.writeString(resource_len);
            dest.writeInt(bucket_id);
            dest.writeString(reply_remark);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PretreatmentBean> CREATOR = new Creator<PretreatmentBean>() {
            @Override
            public PretreatmentBean createFromParcel(Parcel in) {
                return new PretreatmentBean(in);
            }

            @Override
            public PretreatmentBean[] newArray(int size) {
                return new PretreatmentBean[size];
            }
        };

        public String getResource_url() {
            return resource_url;
        }

        public void setResource_url(String resource_url) {
            this.resource_url = resource_url;
        }

        public String getResource_type() {
            return resource_type;
        }

        public void setResource_type(String resource_type) {
            this.resource_type = resource_type;
        }

        public String getResource_uri() {
            return resource_uri;
        }

        public void setResource_uri(String resource_uri) {
            this.resource_uri = resource_uri;
        }

        public String getResource_len() {
            return resource_len;
        }

        public void setResource_len(String resource_len) {
            this.resource_len = resource_len;
            voice_len = resource_len;
        }

        public int getBucket_id() {
            return bucket_id;
        }

        public void setBucket_id(int bucket_id) {
            this.bucket_id = bucket_id;
        }

        public String getReply_remark() {
            return reply_remark;
        }

        public void setReply_remark(String reply_remark) {
            this.reply_remark = reply_remark;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
