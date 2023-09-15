package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class NoteVoiceData extends BaseRepData {
    private List<NoteVoiceBean> data;

    public List<NoteVoiceBean> getData() {
        return data;
    }

    public void setData(List<NoteVoiceBean> data) {
        this.data = data;
    }

    public static class NoteVoiceBean extends BaseAnimBean {
        private String id;
        private String note_len;
        private int created_at;
        private String note_url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNote_len() {
            return note_len;
        }

        public void setNote_len(String note_len) {
            this.note_len = note_len;
            voice_len = note_len;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public String getNote_url() {
            return note_url;
        }

        public void setNote_url(String note_url) {
            this.note_url = note_url;
        }
    }
}
