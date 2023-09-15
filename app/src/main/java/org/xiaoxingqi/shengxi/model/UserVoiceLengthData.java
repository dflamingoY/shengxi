package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class UserVoiceLengthData extends BaseRepData {

    private List<UserVoiceLengthBean> data;

    public List<UserVoiceLengthBean> getData() {
        return data;
    }

    public void setData(List<UserVoiceLengthBean> data) {
        this.data = data;
    }

    public static class UserVoiceLengthBean {
        private String id;//1：心情簿记忆，2：心情簿相册，3：心情簿时光机，4：心情簿电影，5：心情簿书籍，6：心情簿唱回忆，7：心情簿绘画，8：心情簿配音
        private int total;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }


}
