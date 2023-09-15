package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class TalkListData extends BaseRepData {
    private List<TalkListBean> data;

    public List<TalkListBean> getData() {
        return data;
    }

    public void setData(List<TalkListBean> data) {
        this.data = data;
    }

    public static class TalkListBean extends BaseAnimBean implements Parcelable {

        private int created_at;
        private String from_user_id;
        private int read_at;
        private String resource_len;
        private String resource_type;//资源类型，1:音频，2:图片
        private String resource_url;
        private String dialog_id;
        private int is_self;  //1自己
        private String avatar_url;
        private String user_avatar_url;
        private String chat_id;//仅用于推送消息时判断是否为当前聊天的对象
        private boolean isBusy = false;//是否是繁忙状态
        private boolean isRead = true;//消息已读的标记
        private String offline_prompt;//仅当与客服聊天，且之前收到了客服的离线提示时，返回
        private boolean isPause;
        private String id;
        private String nick_name;//管理员查看专有
        private String user_id;//管理员查看专有
        private String voice_id;//管理员查看专有
        private String to_user_id;//管理员查看专有 与之对话的用户ID
        private String voice_user_id;//管理专用
        private int identity_type;
        private int dialog_status;//对话状态，1:正常，2:用户删除，3:系统删除
        private String recognition_content;//语音转文字
        private boolean isReadTag;
        private boolean isCache;//是否是發送失敗的item
        private boolean isIllegal = true;//图片非法
        private int garbage_type;//0:非垃圾，1:色情垃圾
        private int chat_type;
        private String from_avatar_url;
        private String from_nick_name;
        private String resource_id;

        public TalkListBean() {

        }

        protected TalkListBean(Parcel in) {
            super(in);
            created_at = in.readInt();
            from_user_id = in.readString();
            read_at = in.readInt();
            resource_len = in.readString();
            resource_type = in.readString();
            resource_url = in.readString();
            dialog_id = in.readString();
            is_self = in.readInt();
            avatar_url = in.readString();
            user_avatar_url = in.readString();
            chat_id = in.readString();
            isBusy = in.readByte() != 0;
            isRead = in.readByte() != 0;
            offline_prompt = in.readString();
            isPause = in.readByte() != 0;
            id = in.readString();
            nick_name = in.readString();
            user_id = in.readString();
            voice_id = in.readString();
            to_user_id = in.readString();
            voice_user_id = in.readString();
            identity_type = in.readInt();
            dialog_status = in.readInt();
            recognition_content = in.readString();
            chat_type = in.readInt();
            resource_id = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(created_at);
            dest.writeString(from_user_id);
            dest.writeInt(read_at);
            dest.writeString(resource_len);
            dest.writeString(resource_type);
            dest.writeString(resource_url);
            dest.writeString(dialog_id);
            dest.writeInt(is_self);
            dest.writeString(avatar_url);
            dest.writeString(user_avatar_url);
            dest.writeString(chat_id);
            dest.writeByte((byte) (isBusy ? 1 : 0));
            dest.writeByte((byte) (isRead ? 1 : 0));
            dest.writeString(offline_prompt);
            dest.writeByte((byte) (isPause ? 1 : 0));
            dest.writeString(id);
            dest.writeString(nick_name);
            dest.writeString(user_id);
            dest.writeString(voice_id);
            dest.writeString(to_user_id);
            dest.writeString(voice_user_id);
            dest.writeInt(identity_type);
            dest.writeInt(dialog_status);
            dest.writeString(recognition_content);
            dest.writeInt(chat_type);
            dest.writeString(resource_id);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<TalkListBean> CREATOR = new Creator<TalkListBean>() {
            @Override
            public TalkListBean createFromParcel(Parcel in) {
                return new TalkListBean(in);
            }

            @Override
            public TalkListBean[] newArray(int size) {
                return new TalkListBean[size];
            }
        };

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public void setFrom_user_id(String from_user_id) {
            this.from_user_id = from_user_id;
        }

        public int getRead_at() {
            return read_at;
        }

        public void setRead_at(int read_at) {
            this.read_at = read_at;
        }

        public String getResource_len() {
            return resource_len;
        }

        public void setResource_len(String resource_len) {
            this.resource_len = resource_len;
            voice_len = resource_len;
        }

        public String getResource_type() {
            return resource_type;
        }

        public void setResource_type(String resource_type) {
            this.resource_type = resource_type;
        }

        public String getResource_url() {
            return resource_url;
        }

        public void setResource_url(String resource_url) {
            this.resource_url = resource_url;
        }

        public String getDialog_id() {
            return dialog_id;
        }

        public void setDialog_id(String dialog_id) {
            this.dialog_id = dialog_id;
        }

        public int getIs_self() {
            return is_self;
        }

        public void setIs_self(int is_self) {
            this.is_self = is_self;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getUser_avatar_url() {
            return user_avatar_url;
        }

        public void setUser_avatar_url(String user_avatar_url) {
            this.user_avatar_url = user_avatar_url;
            avatar_url = user_avatar_url;
        }

        public String getChat_id() {
            return chat_id;
        }

        public void setChat_id(String chat_id) {
            this.chat_id = chat_id;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }

        public boolean isBusy() {
            return isBusy;
        }

        public void setBusy(boolean busy) {
            isBusy = busy;
        }

        public String getOffline_prompt() {
            return offline_prompt;
        }

        public void setOffline_prompt(String offline_prompt) {
            this.offline_prompt = offline_prompt;
        }

        public boolean isPause() {
            return isPause;
        }

        public void setPause(boolean pause) {
            isPause = pause;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }

        public String getVoice_user_id() {
            return voice_user_id;
        }

        public void setVoice_user_id(String voice_user_id) {
            this.voice_user_id = voice_user_id;
        }

        public int getIdentity_type() {
            return identity_type;
        }

        public void setIdentity_type(int identity_type) {
            this.identity_type = identity_type;
        }

        public int getDialog_status() {
            return dialog_status;
        }

        public void setDialog_status(int dialog_status) {
            this.dialog_status = dialog_status;
        }

        public String getRecognition_content() {
            return recognition_content;
        }

        public void setRecognition_content(String recognition_content) {
            this.recognition_content = recognition_content;
        }

        public boolean isReadTag() {
            return isReadTag;
        }

        public void setReadTag(boolean readTag) {
            isReadTag = readTag;
        }

        public boolean isCache() {
            return isCache;
        }

        public void setCache(boolean cache) {
            isCache = cache;
        }

        public boolean isIllegal() {
            return isIllegal;
        }

        public void setIllegal(boolean illegal) {
            isIllegal = illegal;
        }

        public int getGarbage_type() {
            return garbage_type;
        }

        public void setGarbage_type(int garbage_type) {
            this.garbage_type = garbage_type;
        }

        public int getChat_type() {
            return chat_type;
        }

        public void setChat_type(int chat_type) {
            this.chat_type = chat_type;
        }

        public String getFrom_avatar_url() {
            return from_avatar_url;
        }

        public void setFrom_avatar_url(String from_avatar_url) {
            this.from_avatar_url = from_avatar_url;
        }

        public String getFrom_nick_name() {
            return from_nick_name;
        }

        public void setFrom_nick_name(String from_nick_name) {
            this.from_nick_name = from_nick_name;
        }

        public String getResource_id() {
            return resource_id;
        }

        public void setResource_id(String resource_id) {
            this.resource_id = resource_id;
        }
    }
}
