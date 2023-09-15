package org.xiaoxingqi.shengxi.model;

public class DetailsBottomData extends BaseRepData {


    private BottomBean data;

    public BottomBean getData() {
        return data;
    }

    public void setData(BottomBean data) {
        this.data = data;
    }

    public static class BottomBean {

        private BottomStatusBean chatPri;//0=所有人可以私聊，1=仅限好友私聊
        private BottomStatusBean confession;
        private BottomStatusBean friend;

        public BottomStatusBean getChatPri() {
            return chatPri;
        }

        public void setChatPri(BottomStatusBean chatPri) {
            this.chatPri = chatPri;
        }

        public BottomStatusBean getConfession() {
            return confession;
        }

        public void setConfession(BottomStatusBean confession) {
            this.confession = confession;
        }

        public BottomStatusBean getFriend() {
            return friend;
        }

        public void setFriend(BottomStatusBean friend) {
            this.friend = friend;
        }
    }

    public static class BottomStatusBean {

        /**
         * status : 0
         * tipsName : 私聊
         */

        private int status;//状态，0=未加好友，1=加好友中，2=已是好友    私聊状态，0=所有人可以私聊，1=仅限好友私聊
        private String tipsName;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getTipsName() {
            return tipsName;
        }

        public void setTipsName(String tipsName) {
            this.tipsName = tipsName;
        }
    }

}
