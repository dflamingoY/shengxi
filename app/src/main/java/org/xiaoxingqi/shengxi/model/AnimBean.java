package org.xiaoxingqi.shengxi.model;

public class AnimBean {
    private int resourceId;
    private int status;//0 未读 1 已读
    private String name;

    public AnimBean(int resourceId) {
        this.resourceId = resourceId;
    }

    public AnimBean(int resourceId, String name) {
        this.resourceId = resourceId;
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

