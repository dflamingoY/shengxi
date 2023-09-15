package org.xiaoxingqi.shengxi.impl;

/**
 * 非对话界面 回声成功更新UI
 */
public class SendMsgSocketEvent {
    private String voiceId;
    private String chatId;
    private int sendType;//0 失败

    public SendMsgSocketEvent(String voiceId, String chatId) {
        this.voiceId = voiceId;
        this.chatId = chatId;
    }

    public SendMsgSocketEvent(int sendType) {
        this.sendType = sendType;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }
}
