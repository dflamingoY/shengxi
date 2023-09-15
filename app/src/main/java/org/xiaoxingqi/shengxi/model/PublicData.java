package org.xiaoxingqi.shengxi.model;

public class PublicData extends BaseRepData {

    public PublicBean getData() {
        return data;
    }

    public void setData(PublicBean data) {
        this.data = data;
    }

    private PublicBean data;

    public static class PublicBean {
        private String qrLink;

        public String getQrLink() {
            return qrLink;
        }

        public void setQrLink(String qrLink) {
            this.qrLink = qrLink;
        }
    }
}
