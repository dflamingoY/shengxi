package org.xiaoxingqi.shengxi.impl;

/**
 * 单条声兮数据发生变化 : 隐私 回声数  点赞 共享到世界
 */
public class EventVoiceBean {
    private String voiceId;
    private int type;//1 共享  2 点赞 3 私密  4 回声数增加
    private String isShare;
    private int dialogNum;
    private int isCollected;
    private int isPrivacy;
    private int create_at;//创建时间 方便快速定位更改的数据

    public EventVoiceBean(String voiceId, int type, int create_at) {
        this.voiceId = voiceId;
        this.type = type;
        this.create_at = create_at;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public int getType() {
        return type;
    }

    public String getIsShare() {
        return isShare;
    }

    public void setIsShare(String isShare) {
        this.isShare = isShare;
    }

    public int getDialogNum() {
        return dialogNum;
    }

    public void setDialogNum(int dialogNum) {
        this.dialogNum = dialogNum;
    }

    public int getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(int isCollected) {
        this.isCollected = isCollected;
    }

    public int getIsPrivacy() {
        return isPrivacy;
    }

    public void setIsPrivacy(int isPrivacy) {
        this.isPrivacy = isPrivacy;
    }

    public int getCreate_at() {
        return create_at;
    }
}
