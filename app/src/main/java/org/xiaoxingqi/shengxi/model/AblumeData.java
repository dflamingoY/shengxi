package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class AblumeData extends BaseRepData {
    private List<AlbumBean> data;

    public List<AlbumBean> getData() {
        return data;
    }

    public void setData(List<AlbumBean> data) {
        this.data = data;
    }

    public static class AlbumBean {

        private List<String> img_list;
        private String time;

        public List<String> getImg_list() {
            return img_list;
        }

        public void setImg_list(List<String> img_list) {
            this.img_list = img_list;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
