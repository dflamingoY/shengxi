package org.xiaoxingqi.shengxi.model;

import java.io.Serializable;
import java.util.List;

public class VoiceAlbumData extends BaseRepData {

    private List<AlbumDataBean> data;

    public List<AlbumDataBean> getData() {
        return data;
    }

    public void setData(List<AlbumDataBean> data) {
        this.data = data;
    }

    public static class AlbumDataBean implements Serializable {
        /**
         * album_cover_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/voice-album/596/20190813_184308_6dae0419ba628c1bbe5eb3c5091452b6.png
         * album_name : 我摸www啦啦啦啦借口
         * album_sort : 1565692989
         * album_type : 2
         * created_at : 1565692989
         * id : 1
         * played_num : 0
         * user_id : 596
         * voice_num : 0
         * voice_total_len : 0
         */

        private String album_cover_url;
        private String album_name;
        private int album_sort;
        private int album_type;//1 所有人  2 好友 3 私密
        private int created_at;
        private String id;
        private int played_num;
        private String user_id;
        private int voice_num;
        private int voice_total_len;
        private int updated_at;
        private int started_at;
        private int ended_at;
        private int voice_total_len_o;//其他人可见心情总时长，仅当用户浏览自己的非私密心情专辑时返回
        private int dialog_num;
        private int latest_at;
        public String getAlbum_cover_url() {
            return album_cover_url;
        }

        public void setAlbum_cover_url(String album_cover_url) {
            this.album_cover_url = album_cover_url;
        }

        public String getAlbum_name() {
            return album_name;
        }

        public void setAlbum_name(String album_name) {
            this.album_name = album_name;
        }

        public int getAlbum_sort() {
            return album_sort;
        }

        public void setAlbum_sort(int album_sort) {
            this.album_sort = album_sort;
        }

        public int getAlbum_type() {
            return album_type;
        }

        public void setAlbum_type(int album_type) {
            this.album_type = album_type;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getPlayed_num() {
            return played_num;
        }

        public void setPlayed_num(int played_num) {
            this.played_num = played_num;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public int getVoice_num() {
            return voice_num;
        }

        public void setVoice_num(int voice_num) {
            this.voice_num = voice_num;
        }

        public int getVoice_total_len() {
            return voice_total_len;
        }

        public void setVoice_total_len(int voice_total_len) {
            this.voice_total_len = voice_total_len;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
        }

        public int getStarted_at() {
            return started_at;
        }

        public void setStarted_at(int started_at) {
            this.started_at = started_at;
        }

        public int getEnded_at() {
            return ended_at;
        }

        public void setEnded_at(int ended_at) {
            this.ended_at = ended_at;
        }

        public int getVoice_total_len_o() {
            return voice_total_len_o;
        }

        public void setVoice_total_len_o(int voice_total_len_o) {
            this.voice_total_len_o = voice_total_len_o;
        }

        public int getDialog_num() {
            return dialog_num;
        }

        public void setDialog_num(int dialog_num) {
            this.dialog_num = dialog_num;
        }

        public int getLatest_at() {
            return latest_at;
        }

        public void setLatest_at(int latest_at) {
            this.latest_at = latest_at;
        }
    }
}
