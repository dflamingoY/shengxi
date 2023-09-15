package org.xiaoxingqi.shengxi.impl;

//话题管理界面 对话题有增删改查的通知
public class ITopicChangeEvent {
    private String userId;

    public ITopicChangeEvent(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
