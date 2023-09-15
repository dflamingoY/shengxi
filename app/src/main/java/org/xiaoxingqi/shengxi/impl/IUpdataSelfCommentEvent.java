package org.xiaoxingqi.shengxi.impl;

public class IUpdataSelfCommentEvent {
    private int type;//1 加1  减1
    private String voiceId;

    public IUpdataSelfCommentEvent(int type, String voiceId) {
        this.type = type;
        this.voiceId = voiceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}
