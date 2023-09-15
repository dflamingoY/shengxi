package org.xiaoxingqi.shengxi.model;

public class QiniuStringData extends BaseRepData {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String baseUri;
        private String resource_content;
        private OssToken oss;
        private String bucket;
        private String end_point;
        private String bucket_id;

        public OssToken getOss() {
            return oss;
        }

        public void setOss(OssToken oss) {
            this.oss = oss;
        }

        public String getResource_content() {
            return resource_content;
        }

        public void setResource_content(String resource_content) {
            this.resource_content = resource_content;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getBaseUri() {
            return baseUri;
        }

        public void setBaseUri(String baseUri) {
            this.baseUri = baseUri;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "baseUri='" + baseUri + '\'' +
                    ", resource_content='" + resource_content + '\'' +
                    ", oss=" + oss +
                    ", bucket='" + bucket + '\'' +
                    '}';
        }

        public String getEnd_point() {
            return end_point;
        }

        public void setEnd_point(String end_point) {
            this.end_point = end_point;
        }

        public String getBucket_id() {
            return bucket_id;
        }

        public void setBucket_id(String bucket_id) {
            this.bucket_id = bucket_id;
        }
    }

    @Override
    public String toString() {
        return "QiniuStringData{" +
                "data=" + data.toString() +
                '}';
    }
}
