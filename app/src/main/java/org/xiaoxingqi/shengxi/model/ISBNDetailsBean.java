package org.xiaoxingqi.shengxi.model;

public class ISBNDetailsBean extends ISBNBaseData {
    private ISBNResult result;

    public ISBNResult getResult() {
        return result;
    }

    public void setResult(ISBNResult result) {
        this.result = result;
    }

    public static class ISBNResult {
        private String levelNum;
        private String subtitle;
        private String author;
        private String pubdate;
        private String origin_title;
        private String binding;
        private String pages;
        private String images_medium;
        private String images_large;
        private String publisher;
        private String isbn10;
        private String isbn13;
        private String title;
        private String summary;
        private String price;

        public String getLevelNum() {
            return levelNum;
        }

        public void setLevelNum(String levelNum) {
            this.levelNum = levelNum;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPubdate() {
            return pubdate;
        }

        public void setPubdate(String pubdate) {
            this.pubdate = pubdate;
        }

        public String getOrigin_title() {
            return origin_title;
        }

        public void setOrigin_title(String origin_title) {
            this.origin_title = origin_title;
        }

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public String getPages() {
            return pages;
        }

        public void setPages(String pages) {
            this.pages = pages;
        }

        public String getImages_medium() {
            return images_medium;
        }

        public void setImages_medium(String images_medium) {
            this.images_medium = images_medium;
        }

        public String getImages_large() {
            return images_large;
        }

        public void setImages_large(String images_large) {
            this.images_large = images_large;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getIsbn10() {
            return isbn10;
        }

        public void setIsbn10(String isbn10) {
            this.isbn10 = isbn10;
        }

        public String getIsbn13() {
            return isbn13;
        }

        public void setIsbn13(String isbn13) {
            this.isbn13 = isbn13;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
