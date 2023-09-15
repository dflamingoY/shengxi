package org.xiaoxingqi.shengxi.model.qiniu;

import org.xiaoxingqi.shengxi.model.BaseRepData;
import org.xiaoxingqi.shengxi.model.OssToken;

import java.util.List;

public class QiniuToken extends BaseRepData {


    /**
     * data : {"qiniu_token":"mU361QYU6HoN-GEUfyEXirtlPz4XXlUkG2LG7XF5:2ilx6CKWcgHJel6b8p5WB4bRfOQ=:eyJzY29wZSI6InN4LWxvY2FsIiwiZGVhZGxpbmUiOjE1Mzc4NzE0NzZ9","file_path":"sx-local","file_list":["1_c4ca4238a0b923820dcc509a6f75849b.jpg"]}
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
         * qiniu_token : mU361QYU6HoN-GEUfyEXirtlPz4XXlUkG2LG7XF5:2ilx6CKWcgHJel6b8p5WB4bRfOQ=:eyJzY29wZSI6InN4LWxvY2FsIiwiZGVhZGxpbmUiOjE1Mzc4NzE0NzZ9
         * file_list : ["1_c4ca4238a0b923820dcc509a6f75849b.jpg"]
         */

        private OssToken oss;
        private List<String> resource_content;
        private String bucket;
        private String end_point;
        private String bucket_id;

        public OssToken getOss() {
            return oss;
        }

        public void setOss(OssToken oss) {
            this.oss = oss;
        }

        public List<String> getResource_content() {
            return resource_content;
        }

        public void setResource_content(List<String> resource_content) {
            this.resource_content = resource_content;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
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

        @Override
        public String toString() {
            return "DataBean{" +
                    "oss=" + oss +
                    ", resource_content=" + resource_content.size() +
                    ", bucket='" + bucket + '\'' +
                    ", end_point='" + end_point + '\'' +
                    ", bucket_id='" + bucket_id + '\'' +
                    '}';
        }
    }
}
