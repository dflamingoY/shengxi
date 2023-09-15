package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class H5CommentData extends BaseRepData {

    private List<H5CommentBean> data;

    public List<H5CommentBean> getData() {
        return data;
    }

    public void setData(List<H5CommentBean> data) {
        this.data = data;
    }

    public static class H5CommentBean {
        /**
         * content : 噢噢噢哦哦⊙∀⊙！
         * created_at : 1542800115
         * from_user_id : 3070
         * id : 21
         * to_user_id : 0
         */

        private String content;
        private int created_at;
        private String from_user_id;
        private int id;
        private String to_user_id;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

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

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }
    }
}
