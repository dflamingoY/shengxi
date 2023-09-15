package org.xiaoxingqi.shengxi.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class SearchTopicData extends BaseRepData {
    private List<SearchTopicBean> data;

    public List<SearchTopicBean> getData() {
        return data;
    }

    public void setData(List<SearchTopicBean> data) {
        this.data = data;
    }

    public static class SearchTopicBean implements Parcelable {
        private String topic_id = "";
        private String topic_name;
        private String voice_num;
        private int created_at;
        private boolean isExtra;
        private int weight;//设置话题的权重
        private int id;//设置话题的id
        private String artwork_num;//画图数量

        public SearchTopicBean() {

        }

        public SearchTopicBean(String topic_name, boolean isExtra) {
            this.topic_name = topic_name;
            this.isExtra = isExtra;
        }


        protected SearchTopicBean(Parcel in) {
            topic_id = in.readString();
            topic_name = in.readString();
            voice_num = in.readString();
            created_at = in.readInt();
            isExtra = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(topic_id);
            dest.writeString(topic_name);
            dest.writeString(voice_num);
            dest.writeInt(created_at);
            dest.writeByte((byte) (isExtra ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SearchTopicBean> CREATOR = new Creator<SearchTopicBean>() {
            @Override
            public SearchTopicBean createFromParcel(Parcel in) {
                return new SearchTopicBean(in);
            }

            @Override
            public SearchTopicBean[] newArray(int size) {
                return new SearchTopicBean[size];
            }
        };

        public boolean isExtra() {
            return isExtra;
        }

        public void setExtra(boolean extra) {
            isExtra = extra;
        }

        public String getTopic_id() {
            return topic_id;
        }

        public void setTopic_id(String topic_id) {
            this.topic_id = topic_id;
        }

        public String getTopic_name() {
            return topic_name;
        }

        public void setTopic_name(String topic_name) {
            this.topic_name = topic_name;
        }

        public String getVoice_num() {
            return voice_num;
        }

        public void setVoice_num(String voice_num) {
            this.voice_num = voice_num;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getArtwork_num() {
            return artwork_num;
        }

        public void setArtwork_num(String artwork_num) {
            this.artwork_num = artwork_num;
        }
    }
}
