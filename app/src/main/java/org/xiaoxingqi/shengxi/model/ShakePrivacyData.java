package org.xiaoxingqi.shengxi.model;

public class ShakePrivacyData extends BaseRepData {

    private ShakePrivacyBean data;

    public ShakePrivacyBean getData() {
        return data;
    }

    public void setData(ShakePrivacyBean data) {
        this.data = data;
    }

    public static class ShakePrivacyBean {
        private int be_from_share;
        private int be_from_movie;
        private int be_from_book;
        private int be_from_song;

        public int getBe_from_share() {
            return be_from_share;
        }

        public void setBe_from_share(int be_from_share) {
            this.be_from_share = be_from_share;
        }

        public int getBe_from_movie() {
            return be_from_movie;
        }

        public void setBe_from_movie(int be_from_movie) {
            this.be_from_movie = be_from_movie;
        }

        public int getBe_from_book() {
            return be_from_book;
        }

        public void setBe_from_book(int be_from_book) {
            this.be_from_book = be_from_book;
        }

        public int getBe_from_song() {
            return be_from_song;
        }

        public void setBe_from_song(int be_from_song) {
            this.be_from_song = be_from_song;
        }
    }
}
