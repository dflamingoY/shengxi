package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserCheersData extends BaseRepData {
    private UserCheersBean data;

    public UserCheersBean getData() {
        return data;
    }

    public void setData(UserCheersBean data) {
        this.data = data;
    }

    public static class UserCheersBean extends BaseAnimBean implements Parcelable {
        private String id;
        private String user_id;
        private String recording_url;
        private String recording_len;
        private String recognition_content;

        public UserCheersBean() {

        }

        protected UserCheersBean(Parcel in) {
            id = in.readString();
            user_id = in.readString();
            recording_url = in.readString();
            recording_len = in.readString();
            recognition_content = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(user_id);
            dest.writeString(recording_url);
            dest.writeString(recording_len);
            dest.writeString(recognition_content);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<UserCheersBean> CREATOR = new Creator<UserCheersBean>() {
            @Override
            public UserCheersBean createFromParcel(Parcel in) {
                return new UserCheersBean(in);
            }

            @Override
            public UserCheersBean[] newArray(int size) {
                return new UserCheersBean[size];
            }
        };

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getRecording_url() {
            return recording_url;
        }

        public void setRecording_url(String recording_url) {
            this.recording_url = recording_url;
            setVoicePath(recording_url);
        }

        public String getRecording_len() {
            return recording_len;
        }

        public void setRecording_len(String recording_len) {
            this.recording_len = recording_len;
            setVoice_len(recording_len);
        }

        public String getRecognition_content() {
            return recognition_content;
        }

        public void setRecognition_content(String recognition_content) {
            this.recognition_content = recognition_content;
        }
    }
}
