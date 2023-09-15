package org.xiaoxingqi.shengxi.model;

/**
 * 用户参与评论的电影 书籍 音乐的数量摘要
 */
public class AboutUserCommentData extends BaseRepData {

    private AboutCommentBean data;

    public AboutCommentBean getData() {
        return data;
    }

    public void setData(AboutCommentBean data) {
        this.data = data;
    }

    public static class AboutCommentBean {
        private int film_num;
        private int book_num;
        private int song_num;

        public int getFilm_num() {
            return film_num;
        }

        public void setFilm_num(int film_num) {
            this.film_num = film_num;
        }

        public int getBook_num() {
            return book_num;
        }

        public void setBook_num(int book_num) {
            this.book_num = book_num;
        }

        public int getSong_num() {
            return song_num;
        }

        public void setSong_num(int song_num) {
            this.song_num = song_num;
        }
    }


    public static class CreateTopicData extends BaseRepData {

        private SearchTopicData.SearchTopicBean data;


        public SearchTopicData.SearchTopicBean getData() {
            return data;
        }

        public void setData(SearchTopicData.SearchTopicBean data) {
            this.data = data;
        }
    }
}
