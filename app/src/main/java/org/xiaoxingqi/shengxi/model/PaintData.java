package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PaintData extends BaseRepData {

    private List<PaintBean> data;

    public List<PaintBean> getData() {
        return data;
    }

    public void setData(List<PaintBean> data) {
        this.data = data;
    }

    public static class PaintBean implements Parcelable {
        private int id;
        private String user_id;
        private String nick_name;
        private String topic_id;
        private String avatar_url;
        private String topic_name;
        private String artwork_url;
        private int is_private;//是否设置了私密，1:是，0:否
        private int viewed_num;
        private int anonymous_like_num;//匿名
        private int publicly_like_num;//開放
        private int created_at;
        private int artwork_status;//状态，1：正常，2：用户删除，3：系统删除
        private int like_type;//点赞类型，0:没有点赞，1：匿名，2：公开
        private int identity_type;
        private boolean isLoading = false;
        private int hide_at;
        private String graffiti_url;
        private int graffiti_status;//涂鸦的状态 1:正常；2:归属用户删除，3:父级用户删除，4:系统删除
        private int vote_option_one;
        private int vote_option_two;
        private int vote_option;
        private int graffiti_num;
        private int graffiti_switch;//涂鸦开关，1：允许，0：关 禁止
        private int vote_id;//天使魔鬼选择的id
        private int being_graffiti;//是否已经涂鸦过此作品 1:是，0:否
        private String parent_user_id;//涂鸦所属作品的id
        private BaseUserBean user_info;
        private BaseUserBean user;//4.3 新版用户信息, 非自己返回改参数
        private String collection_id;//收藏id
        private int top_id;//设置为今日热门id
        private int dialog_num;//对话数量
        private int chat_num;//聊天数量 机主用
        private int isSelf;//0 否 1 是

        public PaintBean() {

        }

        protected PaintBean(Parcel in) {
            id = in.readInt();
            user_id = in.readString();
            nick_name = in.readString();
            topic_id = in.readString();
            avatar_url = in.readString();
            topic_name = in.readString();
            artwork_url = in.readString();
            is_private = in.readInt();
            viewed_num = in.readInt();
            anonymous_like_num = in.readInt();
            publicly_like_num = in.readInt();
            created_at = in.readInt();
            artwork_status = in.readInt();
            like_type = in.readInt();
            identity_type = in.readInt();
            isLoading = in.readByte() != 0;
            hide_at = in.readInt();
            graffiti_url = in.readString();
            graffiti_status = in.readInt();
            vote_option_one = in.readInt();
            vote_option_two = in.readInt();
            vote_option = in.readInt();
            graffiti_num = in.readInt();
            graffiti_switch = in.readInt();
            vote_id = in.readInt();
            being_graffiti = in.readInt();
            parent_user_id = in.readString();
        }

        public static final Creator<PaintBean> CREATOR = new Creator<PaintBean>() {
            @Override
            public PaintBean createFromParcel(Parcel in) {
                return new PaintBean(in);
            }

            @Override
            public PaintBean[] newArray(int size) {
                return new PaintBean[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
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

        public String getArtwork_url() {
            return artwork_url;
        }

        public void setArtwork_url(String artwork_url) {
            this.artwork_url = artwork_url;
        }

        public int getIs_private() {
            return is_private;
        }

        public void setIs_private(int is_private) {
            this.is_private = is_private;
        }

        public int getViewed_num() {
            return viewed_num;
        }

        public void setViewed_num(int viewed_num) {
            this.viewed_num = viewed_num;
        }

        public int getAnonymous_like_num() {
            return anonymous_like_num;
        }

        public void setAnonymous_like_num(int anonymous_like_num) {
            this.anonymous_like_num = anonymous_like_num;
        }

        public int getPublicly_like_num() {
            return publicly_like_num;
        }

        public void setPublicly_like_num(int publicly_like_num) {
            this.publicly_like_num = publicly_like_num;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getArtwork_status() {
            return artwork_status;
        }

        public void setArtwork_status(int artwork_status) {
            this.artwork_status = artwork_status;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
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

        public boolean isLoading() {
            return isLoading;
        }

        public void setLoading(boolean loading) {
            isLoading = loading;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(user_id);
            dest.writeString(nick_name);
            dest.writeString(topic_id);
            dest.writeString(avatar_url);
            dest.writeString(topic_name);
            dest.writeString(artwork_url);
            dest.writeInt(is_private);
            dest.writeInt(viewed_num);
            dest.writeInt(anonymous_like_num);
            dest.writeInt(publicly_like_num);
            dest.writeInt(created_at);
            dest.writeInt(artwork_status);
            dest.writeInt(like_type);
            dest.writeInt(identity_type);
            dest.writeByte((byte) (isLoading ? 1 : 0));
            dest.writeInt(hide_at);
            dest.writeString(graffiti_url);
            dest.writeInt(graffiti_status);
            dest.writeInt(vote_option_one);
            dest.writeInt(vote_option_two);
            dest.writeInt(vote_option);
            dest.writeInt(graffiti_num);
            dest.writeInt(graffiti_switch);
            dest.writeInt(vote_id);
            dest.writeInt(being_graffiti);
            dest.writeString(parent_user_id);
        }

        public int getHide_at() {
            return hide_at;
        }

        public void setHide_at(int hide_at) {
            this.hide_at = hide_at;
        }

        public String getGraffiti_url() {
            return graffiti_url;
        }

        public void setGraffiti_url(String graffiti_url) {
            this.graffiti_url = graffiti_url;
        }

        public int getGraffiti_status() {
            return graffiti_status;
        }

        public void setGraffiti_status(int graffiti_status) {
            this.graffiti_status = graffiti_status;
        }

        public int getVote_option_one() {
            return vote_option_one;
        }

        public void setVote_option_one(int vote_option_one) {
            this.vote_option_one = vote_option_one;
        }

        public int getVote_option_two() {
            return vote_option_two;
        }

        public void setVote_option_two(int vote_option_two) {
            this.vote_option_two = vote_option_two;
        }

        public int getVote_option() {
            return vote_option;
        }

        public void setVote_option(int vote_option) {
            this.vote_option = vote_option;
        }

        public int getGraffiti_num() {
            return graffiti_num;
        }

        public void setGraffiti_num(int graffiti_num) {
            this.graffiti_num = graffiti_num;
        }

        public int getGraffiti_switch() {
            return graffiti_switch;
        }

        public void setGraffiti_switch(int graffiti_switch) {
            this.graffiti_switch = graffiti_switch;
        }

        public int getVote_id() {
            return vote_id;
        }

        public void setVote_id(int vote_id) {
            this.vote_id = vote_id;
        }

        public int getBeing_graffiti() {
            return being_graffiti;
        }

        public void setBeing_graffiti(int being_graffiti) {
            this.being_graffiti = being_graffiti;
        }

        public String getParent_user_id() {
            return parent_user_id;
        }

        public void setParent_user_id(String parent_user_id) {
            this.parent_user_id = parent_user_id;
        }

        public BaseUserBean getUser_info() {
            return user_info;
        }

        public void setUser_info(BaseUserBean user_info) {
            this.user_info = user_info;
        }

        public BaseUserBean getUser() {
            return user;
        }

        public void setUser(BaseUserBean user) {
            this.user = user;
        }

        public String getCollection_id() {
            return collection_id;
        }

        public void setCollection_id(String collection_id) {
            this.collection_id = collection_id;
        }

        public int getTop_id() {
            return top_id;
        }

        public void setTop_id(int top_id) {
            this.top_id = top_id;
        }

        public int getDialog_num() {
            return dialog_num;
        }

        public void setDialog_num(int dialog_num) {
            this.dialog_num = dialog_num;
        }

        public int getChat_num() {
            return chat_num;
        }

        public void setChat_num(int chat_num) {
            this.chat_num = chat_num;
        }

        public int getIsSelf() {
            return isSelf;
        }

        public void setIsSelf(int isSelf) {
            this.isSelf = isSelf;
        }
    }
}
