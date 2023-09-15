package org.xiaoxingqi.shengxi.impl;

public class UpdateSendVoice {
    private int type;//1刷新  2  更新主题是的刷新 3 双击刷新
    private String voice_id;

    public UpdateSendVoice(int type) {
        this.type = type;
    }

    public UpdateSendVoice(int type, String voice_id) {
        this.type = type;
        this.voice_id = voice_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVoice_id() {
        return voice_id;
    }

    public void setVoice_id(String voice_id) {
        this.voice_id = voice_id;
    }
}
