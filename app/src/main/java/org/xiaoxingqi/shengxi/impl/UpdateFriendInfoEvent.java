package org.xiaoxingqi.shengxi.impl;

/**
 * 用户修改自己的头像昵称, 修改好友的备注
 */
public class UpdateFriendInfoEvent {
    private int type;//通知的类型  0:备注  1:昵称  2:简介 3:头像
    private String remark;
    private String nickName;
    private String userId;
    private String desc;

    public UpdateFriendInfoEvent(String userId, int type, String remark, String nickName, String desc) {
        this.type = type;
        this.userId = userId;
        this.remark = remark;
        this.nickName = nickName;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public String getNickName() {
        return nickName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDesc() {
        return desc;
    }
}
