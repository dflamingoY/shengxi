package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class AdminCheckListData extends BaseRepData {
    private List<AdminCheckBean> data;

    public List<AdminCheckBean> getData() {
        return data;
    }

    public void setData(List<AdminCheckBean> data) {
        this.data = data;
    }

    public static class AdminCheckBean implements Parcelable {
        /**
         * created_at : 1547086776
         * data_from : 0
         * data_id : 22
         * data_type : 2
         * id : 1
         * operation_note :
         * operation_type : 1
         * operator_id : 0
         * updated_at : 0
         * user : {"avatar_url":"https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-avatar/4_avatar.jpg?x-oss-process=style/thumb_90_90","id":4,"nick_name":"卡哇伊"}
         * user_id : 4
         */
        private int created_at;
        private int data_from;// 1心情音频 2 对话音频 3声波音频 4 Cheers录音 5配音 31头像 32 心情图片 33对话图片 34 心情簿封面图 35 心情日历主题封面
        // 36心情专辑封面 37 绘画作品 38词条封面图  39时光机封面图 40登录密码处封面图 101 用户昵称 102 话题名称 103 台词  104 心情专辑名称
        private int data_id;
        private int data_type;//1:用户，2：声兮，3：对话，4：台词，5：配音，6：绘画
        private int id;
        private String operation_note;
        private int operation_type;//1：待处理,2：确认没有问题，3：删除，4：删除并警告，5：删除并封号，6：删除并封号且替换信息，
        private int operator_id;
        private int updated_at;
        private BaseUserBean user;
        private int user_id;
        private String chat_id;
        private String voice_id;
        private String image_url;
        private int entry_type;//1 2 3 影 书 音
        private String movie_id;
        private String song_id;
        private String book_id;

        public AdminCheckBean() {

        }

        protected AdminCheckBean(Parcel in) {
            created_at = in.readInt();
            data_from = in.readInt();
            data_id = in.readInt();
            data_type = in.readInt();
            id = in.readInt();
            operation_note = in.readString();
            operation_type = in.readInt();
            operator_id = in.readInt();
            updated_at = in.readInt();
            user = in.readParcelable(BaseUserBean.class.getClassLoader());
            user_id = in.readInt();
            chat_id = in.readString();
            voice_id = in.readString();
            image_url = in.readString();
            entry_type = in.readInt();
            movie_id = in.readString();
            book_id = in.readString();
            song_id = in.readString();
        }

        public static final Creator<AdminCheckBean> CREATOR = new Creator<AdminCheckBean>() {
            @Override
            public AdminCheckBean createFromParcel(Parcel in) {
                return new AdminCheckBean(in);
            }

            @Override
            public AdminCheckBean[] newArray(int size) {
                return new AdminCheckBean[size];
            }
        };

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getData_from() {
            return data_from;
        }

        public void setData_from(int data_from) {
            this.data_from = data_from;
        }

        public int getData_id() {
            return data_id;
        }

        public void setData_id(int data_id) {
            this.data_id = data_id;
        }

        public int getData_type() {
            return data_type;
        }

        public void setData_type(int data_type) {
            this.data_type = data_type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOperation_note() {
            return operation_note;
        }

        public void setOperation_note(String operation_note) {
            this.operation_note = operation_note;
        }

        public int getOperation_type() {
            return operation_type;
        }

        public void setOperation_type(int operation_type) {
            this.operation_type = operation_type;
        }

        public int getOperator_id() {
            return operator_id;
        }

        public void setOperator_id(int operator_id) {
            this.operator_id = operator_id;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(created_at);
            dest.writeInt(data_from);
            dest.writeInt(data_id);
            dest.writeInt(data_type);
            dest.writeInt(id);
            dest.writeString(operation_note);
            dest.writeInt(operation_type);
            dest.writeInt(operator_id);
            dest.writeInt(updated_at);
            dest.writeParcelable(user, flags);
            dest.writeInt(user_id);
            dest.writeString(chat_id);
            dest.writeString(voice_id);
            dest.writeString(image_url);
            dest.writeInt(entry_type);
            dest.writeString(movie_id);
            dest.writeString(book_id);
            dest.writeString(song_id);
        }

        public String getChat_id() {
            return chat_id;
        }

        public void setChat_id(String chat_id) {
            this.chat_id = chat_id;
        }

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public int getEntry_type() {
            return entry_type;
        }

        public void setEntry_type(int entry_type) {
            this.entry_type = entry_type;
        }

        public String getMovie_id() {
            return movie_id;
        }

        public void setMovie_id(String movie_id) {
            this.movie_id = movie_id;
        }

        public String getSong_id() {
            return song_id;
        }

        public void setSong_id(String song_id) {
            this.song_id = song_id;
        }

        public String getBook_id() {
            return book_id;
        }

        public void setBook_id(String book_id) {
            this.book_id = book_id;
        }
    }

}
