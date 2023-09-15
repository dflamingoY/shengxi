package org.xiaoxingqi.shengxi.model;

public class ResouceData extends BaseRepData {
    private ResourceBean data;

    public ResourceBean getData() {
        return data;
    }

    public void setData(ResourceBean data) {
        this.data = data;
    }


    public static class ResourceBean {
        private int id;
        private String resource_name;
        private String resource_content;
        private int resource_status;
        private String platform_id;
        private String created_at;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getResource_name() {
            return resource_name;
        }

        public void setResource_name(String resource_name) {
            this.resource_name = resource_name;
        }

        public int getResource_status() {
            return resource_status;
        }

        public void setResource_status(int resource_status) {
            this.resource_status = resource_status;
        }

        public String getPlatform_id() {
            return platform_id;
        }

        public void setPlatform_id(String platform_id) {
            this.platform_id = platform_id;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getResource_content() {
            return resource_content;
        }

        public void setResource_content(String resource_content) {
            this.resource_content = resource_content;
        }
    }
}
