package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class ListenData extends BaseRepData {

    private List<ListenBean> data;

    public List<ListenBean> getData() {
        return data;
    }

    public void setData(List<ListenBean> data) {
        this.data = data;
    }

    public static class ListenBean {

        private String html_id;
        private String html_title;
        private String banner_url;
        private String redirect_url;
        private String html_content;
        private int is_open;  //是否开放可以评论的权限  1是 0否

        public String getHtml_id() {
            return html_id;
        }

        public void setHtml_id(String html_id) {
            this.html_id = html_id;
        }

        public String getHtml_title() {
            return html_title;
        }

        public void setHtml_title(String html_title) {
            this.html_title = html_title;
        }

        public String getBanner_url() {
            return banner_url;
        }

        public void setBanner_url(String banner_url) {
            this.banner_url = banner_url;
        }

        public String getRedirect_url() {
            return redirect_url;
        }

        public void setRedirect_url(String redirect_url) {
            this.redirect_url = redirect_url;
        }

        public String getHtml_content() {
            return html_content;
        }

        public void setHtml_content(String html_content) {
            this.html_content = html_content;
        }

        public int getIs_open() {
            return is_open;
        }

        public void setIs_open(int is_open) {
            this.is_open = is_open;
        }
    }
}
