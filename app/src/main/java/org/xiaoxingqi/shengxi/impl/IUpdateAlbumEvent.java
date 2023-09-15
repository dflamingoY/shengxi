package org.xiaoxingqi.shengxi.impl;

public class IUpdateAlbumEvent {
    private int type;//操作的类型   1删除 2 转移可见性  名字  封面  3 add
    private String cover;
    private String name;
    private int visibleType;//操作之后的可见性
    private int originSort;//原始可见性
    private String id;

    public IUpdateAlbumEvent(int type, String id) {
        this.type = type;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVisibleType() {
        return visibleType;
    }

    public void setVisibleType(int visibleType) {
        this.visibleType = visibleType;
    }

    public int getOriginSort() {
        return originSort;
    }

    public void setOriginSort(int originSort) {
        this.originSort = originSort;
    }

    public String getId() {
        return id;
    }
}
