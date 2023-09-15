package org.xiaoxingqi.shengxi.model;


public class RecommendCountData extends BaseRepData {
    private RecommendCountBean data;

    public RecommendCountBean getData() {
        return data;
    }

    public void setData(RecommendCountBean data) {
        this.data = data;
    }

    public static class RecommendCountBean {
        @Deprecated
        private int share_num;
        @Deprecated
        private int max_share_num = 0;
        private int can_share;//当前是否可以共享心情，1:是，0:否
        private int left_times;//当前剩余共享次数
        private int last_shared;//最后共享的时间
        private int nearby_at;//默认0，0:当下就可以分享，非0:最近可以分享的时间
        private String tips;//提示文案

        public int getShare_num() {
            return share_num;
        }

        public void setShare_num(int share_num) {
            this.share_num = share_num;
        }

        public int getMax_share_num() {
            return max_share_num;
        }

        public void setMax_share_num(int max_share_num) {
            this.max_share_num = max_share_num;
        }

        public int getCan_share() {
            return can_share;
        }

        public void setCan_share(int can_share) {
            this.can_share = can_share;
        }

        public int getLeft_times() {
            return left_times;
        }

        public void setLeft_times(int left_times) {
            this.left_times = left_times;
        }

        public int getLast_shared() {
            return last_shared;
        }

        public void setLast_shared(int last_shared) {
            this.last_shared = last_shared;
        }

        public int getNearby_at() {
            return nearby_at;
        }

        public void setNearby_at(int nearby_at) {
            this.nearby_at = nearby_at;
        }

        public String getTips() {
            return tips;
        }

        public void setTips(String tips) {
            this.tips = tips;
        }
    }

}
