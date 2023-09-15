package org.xiaoxingqi.shengxi.impl;

/**
 * 增刪声兮列表的时候发送此广播
 */
public class OperatorVoiceListEvent {
    private int type; // 1 发送无图声兮(影评) 2 发送有图声兮 3 删除声兮
    private String voice_id;

    public OperatorVoiceListEvent(int type) {
        this.type = type;
    }

    public OperatorVoiceListEvent(int type, String voice_id) {
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
