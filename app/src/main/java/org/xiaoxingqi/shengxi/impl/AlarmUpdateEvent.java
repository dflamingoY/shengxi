package org.xiaoxingqi.shengxi.impl;

public class AlarmUpdateEvent {
    private int type;//1 更新数据 2发布了台词  3 删除了本地文件 4删除配音文件 5 下载了新的配音文件 6 编辑可见性 7 更新了投票选项 8 删除了投票
    private int updateResource;//当前更新用于哪里//1 发布了新的配音  2 新台词  3删除文件
    private String dubbingId;
    private String deletePath;

    public AlarmUpdateEvent(int updateResource, int type) {
        this.type = type;
        this.updateResource = updateResource;
    }

    public int getType() {
        return type;
    }

    public int getUpdateResource() {
        return updateResource;
    }

    public String getDubbingId() {
        return dubbingId;
    }

    public void setDubbingId(String dubbingId) {
        this.dubbingId = dubbingId;
    }

    public String getDeletePath() {
        return deletePath;
    }

    public void setDeletePath(String deletePath) {
        this.deletePath = deletePath;
    }
}
