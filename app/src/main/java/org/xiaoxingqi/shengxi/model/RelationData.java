package org.xiaoxingqi.shengxi.model;

public class RelationData extends BaseRepData {

    private RelationBean data;

    public RelationBean getData() {
        return data;
    }

    public void setData(RelationBean data) {
        this.data = data;
    }

    public static class RelationBean {
        private int friend_status;//用户关系，0=陌生人，1=待验证，2=已是好友，3：被对方拉黑，4：自己拉黑对方,5=自己
        private String strange_view;//用户隐私设置

        private WhiteListBean whitelist;//是否在白名单

        public int getFriend_status() {
            return friend_status;
        }

        public void setFriend_status(int friend_status) {
            this.friend_status = friend_status;
        }

        public String getStrange_view() {
            return strange_view;
        }

        public void setStrange_view(String strange_view) {
            this.strange_view = strange_view;
        }

        public WhiteListBean getWhitelist() {
            return whitelist;
        }

        public void setWhitelist(WhiteListBean whitelist) {
            this.whitelist = whitelist;
        }
    }

    public static class WhiteListBean {
        private int id;
        private int released_at;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getReleased_at() {
            return released_at;
        }

        public void setReleased_at(int released_at) {
            this.released_at = released_at;
        }
    }
}
