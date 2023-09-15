package org.xiaoxingqi.shengxi.impl;

/**
 * 删除好友的通知
 */
public class IDeleteFriends {
    private String userid;

    public IDeleteFriends(String userId) {
        userid = userId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
