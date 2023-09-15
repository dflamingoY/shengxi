package org.xiaoxingqi.shengxi.model;

public class ConfessionsData extends BaseRepData {

    private ConfessionsBean data;

    public ConfessionsBean getData() {
        return data;
    }

    public void setData(ConfessionsBean data) {
        this.data = data;
    }

    public static class ConfessionsBean {
        private int confess_id;
        private String resource_id;

        public int getConfess_id() {
            return confess_id;
        }

        public void setConfess_id(int confess_id) {
            this.confess_id = confess_id;
        }

        public String getResource_id() {
            return resource_id;
        }

        public void setResource_id(String resource_id) {
            this.resource_id = resource_id;
        }
    }
}
