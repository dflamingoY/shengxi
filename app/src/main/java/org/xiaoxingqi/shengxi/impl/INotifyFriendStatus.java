package org.xiaoxingqi.shengxi.impl;

public class INotifyFriendStatus {
    private int status;//0=陌生人，1=待验证，2=已是好友 3删除好友  4 拉黑  5 取消拉黑
    private String userId;

    public INotifyFriendStatus(int staus, String userId) {
        this.status = staus;
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
