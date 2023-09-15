package org.xiaoxingqi.shengxi.impl;

public class IUpdateTalkAlbumEvent {

    private int type;//操作的类型   1删除 2 名字  封面  3 add
    private String cover;
    private String name;
    private String id;

    public IUpdateTalkAlbumEvent(int type, String id) {
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

    public String getId() {
        return id;
    }
}
