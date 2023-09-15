package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BaseBean extends BaseAnimBean implements Parcelable {

    private int chat_num;//回声数 ,自己的时候显示这个
    private int created_at;
    private String is_shared;//是否为自己的声兮才会存在是否共享
    private int played_num;//播放数量
    private String resource_id;
    private int resource_type;//资源类型，1=影片  2 书籍  3 唱歌
    private int topic_id;
    private BaseUserBean user;
    private int user_id;//该条声兮所属用户ID
    private String voice_id;//声兮ID
    private String voice_url;
    private int dialog_num;//对话次数, 非此条声兮的拥有者显示
    private int friend_status;//0=不是好友，1=待验证,2=已是好友
    private int is_collected;//是否共鸣   0否1是
    private String topic_name;
    private String chat_id;
    private String share_url;
    private int shared_at;
    private boolean isPause;//是否暂停播放
    private String share_id;
    private int is_private;//是否设置尽自己可见   1是 0否
    private String cover_photo;//声兮封面图
    private int may_interested;//1：可能感兴趣，0:普通，仅当是世界上的声兮，才会有该值返回
    private int user_gender;//0:未知，1:男，2:女,仅当是世界上的声兮，才会有该值返回
    private BaseSearchBean resource;
    private int user_score;//用户的评分等级
    private int note_num;
    private String first_share_voice;//首次分享到世界的心情返回此字段    1是  0否
    private int voice_status;//对话状态，1:正常，2:用户删除，3:系统删除
    private String id;
    private int hide_at;
    private boolean isReadTag;
    private int subscription_id;
    private boolean isNetStatus = false;//本地添加是否正正在网络请求的状态, 该状态下, 不可重复点击 只在取消点赞时做此处理
    private boolean isReDown;//是否再次请求数据
    private boolean isTop;//是否是小二置顶的数据
    private List<String> intersect_tags;//用户感兴趣的数据

    public BaseBean() {

    }

    protected BaseBean(Parcel in) {
        super(in);
        chat_num = in.readInt();
        created_at = in.readInt();
        is_shared = in.readString();
        played_num = in.readInt();
        resource_id = in.readString();
        resource_type = in.readInt();
        topic_id = in.readInt();
        user = in.readParcelable(BaseUserBean.class.getClassLoader());
        user_id = in.readInt();
        voice_id = in.readString();
        voice_url = in.readString();
        dialog_num = in.readInt();
        friend_status = in.readInt();
        is_collected = in.readInt();
        topic_name = in.readString();
        chat_id = in.readString();
        share_url = in.readString();
        shared_at = in.readInt();
        isPause = in.readByte() != 0;
        share_id = in.readString();
        is_private = in.readInt();
        cover_photo = in.readString();
        may_interested = in.readInt();
        user_gender = in.readInt();
        resource = in.readParcelable(BaseSearchBean.class.getClassLoader());
        user_score = in.readInt();
        note_num = in.readInt();
        first_share_voice = in.readString();
        voice_status = in.readInt();
        id = in.readString();
        hide_at = in.readInt();
        isReadTag = in.readByte() != 0;
        img_list = in.createStringArrayList();
        subscription_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(chat_num);
        dest.writeInt(created_at);
        dest.writeString(is_shared);
        dest.writeInt(played_num);
        dest.writeString(resource_id);
        dest.writeInt(resource_type);
        dest.writeInt(topic_id);
        dest.writeParcelable(user, flags);
        dest.writeInt(user_id);
        dest.writeString(voice_id);
        dest.writeString(voice_url);
        dest.writeInt(dialog_num);
        dest.writeInt(friend_status);
        dest.writeInt(is_collected);
        dest.writeString(topic_name);
        dest.writeString(chat_id);
        dest.writeString(share_url);
        dest.writeInt(shared_at);
        dest.writeByte((byte) (isPause ? 1 : 0));
        dest.writeString(share_id);
        dest.writeInt(is_private);
        dest.writeString(cover_photo);
        dest.writeInt(may_interested);
        dest.writeInt(user_gender);
        dest.writeParcelable(resource, flags);
        dest.writeInt(user_score);
        dest.writeInt(note_num);
        dest.writeString(first_share_voice);
        dest.writeInt(voice_status);
        dest.writeString(id);
        dest.writeInt(hide_at);
        dest.writeByte((byte) (isReadTag ? 1 : 0));
        dest.writeStringList(img_list);
        dest.writeInt(subscription_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseBean> CREATOR = new Creator<BaseBean>() {
        @Override
        public BaseBean createFromParcel(Parcel in) {
            return new BaseBean(in);
        }

        @Override
        public BaseBean[] newArray(int size) {
            return new BaseBean[size];
        }
    };

    public ArrayList<String> getImg_list() {
        return img_list;
    }

    public void setImg_list(ArrayList<String> img_list) {
        this.img_list = img_list;
    }

    private ArrayList<String> img_list;

    public int getChat_num() {
        return chat_num;
    }

    public void setChat_num(int chat_num) {
        this.chat_num = chat_num;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public String getIs_shared() {
        return is_shared;
    }

    public void setIs_shared(String is_shared) {
        this.is_shared = is_shared;
    }

    public int getPlayed_num() {
        return played_num;
    }

    public void setPlayed_num(int played_num) {
        this.played_num = played_num;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public int getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(int topic_id) {
        this.topic_id = topic_id;
    }

    public BaseUserBean getUser() {
        return user;
    }

    public void setUser(BaseUserBean user) {
        this.user = user;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getVoice_id() {
        return voice_id;
    }

    public void setVoice_id(String voice_id) {
        this.voice_id = voice_id;
    }


    public String getVoice_url() {
        return voice_url;
    }

    public void setVoice_url(String voice_url) {
        this.voice_url = voice_url;
        setVoicePath(voice_url);
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public int getDialog_num() {
        return dialog_num;
    }

    public void setDialog_num(int dialog_num) {
        this.dialog_num = dialog_num;
    }

    public int getFriend_status() {
        return friend_status;
    }

    public void setFriend_status(int friend_status) {
        this.friend_status = friend_status;
    }

    public int getIs_collected() {
        return is_collected;
    }

    public void setIs_collected(int is_collected) {
        this.is_collected = is_collected;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public int getShared_at() {
        return shared_at;
    }

    public void setShared_at(int shared_at) {
        this.shared_at = shared_at;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public String getShare_id() {
        return share_id;
    }

    public void setShare_id(String share_id) {
        this.share_id = share_id;
    }

    public int getIs_private() {
        return is_private;
    }

    public void setIs_private(int is_private) {
        this.is_private = is_private;
    }

    public String getCover_photo() {
        return cover_photo;
    }

    public void setCover_photo(String cover_photo) {
        this.cover_photo = cover_photo;
    }

    public int getMay_interested() {
        return may_interested;
    }

    public void setMay_interested(int may_interested) {
        this.may_interested = may_interested;
    }

    public int getUser_gender() {
        return user_gender;
    }

    public void setUser_gender(int user_gender) {
        this.user_gender = user_gender;
    }

    public BaseSearchBean getResource() {
        return resource;
    }

    public void setResource(BaseSearchBean resource) {
        this.resource = resource;
    }

    public int getUser_score() {
        return user_score;
    }

    public void setUser_score(int user_score) {
        this.user_score = user_score;
    }

    public int getNote_num() {
        return note_num;
    }

    public void setNote_num(int note_num) {
        this.note_num = note_num;
    }

    public String getFirst_share_voice() {
        return first_share_voice;
    }

    public void setFirst_share_voice(String first_share_voice) {
        this.first_share_voice = first_share_voice;
    }

    public int getVoice_status() {
        return voice_status;
    }

    public void setVoice_status(int voice_status) {
        this.voice_status = voice_status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isReadTag() {
        return isReadTag;
    }

    public void setReadTag(boolean readTag) {
        isReadTag = readTag;
    }

    public int getHide_at() {
        return hide_at;
    }

    public void setHide_at(int hide_at) {
        this.hide_at = hide_at;
    }

    public int getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(int subscription_id) {
        this.subscription_id = subscription_id;
    }

    public boolean isNetStatus() {
        return isNetStatus;
    }

    public void setNetStatus(boolean netStatus) {
        isNetStatus = netStatus;
    }

    public boolean isReDown() {
        return isReDown;
    }

    public void setReDown(boolean reDown) {
        isReDown = reDown;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public List<String> getIntersect_tags() {
        return intersect_tags;
    }

    public void setIntersect_tags(List<String> intersect_tags) {
        this.intersect_tags = intersect_tags;
    }
}
