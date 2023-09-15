package org.xiaoxingqi.shengxi.model;

public class SystemNoticeData extends BaseRepData {

    /**
     * data : {"system":{"content":"","timestamp":"","unread":"0"},"friend_request":{"content":"","timestamp":"","unread":"0"},"other":{"content":"","timestamp":"","unread":"0"}}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * system : {"content":"","timestamp":"","unread":"0"}
         * friend_request : {"content":"","timestamp":"","unread":"0"}
         * other : {"content":"","timestamp":"","unread":"0"}
         */
        private MessageNoticeBean sys;
        private MessageNoticeBean frequest;
        private MessageNoticeBean other;
        private MessageNoticeBean chatpri;

        private int total;

        public MessageNoticeBean getOther() {
            return other;
        }

        public void setOther(MessageNoticeBean other) {
            this.other = other;
        }

        public MessageNoticeBean getSys() {
            return sys;
        }

        public void setSys(MessageNoticeBean sys) {
            this.sys = sys;
        }

        public MessageNoticeBean getFrequest() {
            return frequest;
        }

        public void setFrequest(MessageNoticeBean frequest) {
            this.frequest = frequest;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public MessageNoticeBean getChatpri() {
            return chatpri;
        }

        public void setChatpri(MessageNoticeBean chatpri) {
            this.chatpri = chatpri;
        }
    }

    public static class MessageNoticeBean {
        /**
         * content :
         * timestamp :
         * unread : 0
         */

        private String tips;
        private String time;
        private String num;

        public String getTips() {
            return tips;
        }

        public void setTips(String tips) {
            this.tips = tips;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
