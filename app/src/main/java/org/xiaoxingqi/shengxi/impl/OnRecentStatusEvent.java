package org.xiaoxingqi.shengxi.impl;

/**
 * 操作私聊界面的拉黑 清空記錄  收到消息的通知
 */
public class OnRecentStatusEvent {
    private int type;//1 拉黑  2新的消息私聊消息
    private String userId;

    public OnRecentStatusEvent(int type) {
        this.type = type;
    }

    public OnRecentStatusEvent(int type, String userId) {
        this.type = type;
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
