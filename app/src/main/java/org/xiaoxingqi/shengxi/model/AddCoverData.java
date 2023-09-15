package org.xiaoxingqi.shengxi.model;

public class AddCoverData extends BaseRepData {
    private AddCoverBean data;

    public AddCoverBean getData() {
        return data;
    }

    public void setData(AddCoverBean data) {
        this.data = data;
    }

    public static class AddCoverBean {
        private String id;
        private String coverUrl;

        public AddCoverBean() {

        }

        public AddCoverBean(String coverUrl) {
            this.coverUrl = coverUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public void setCoverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
        }
    }
}
