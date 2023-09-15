package org.xiaoxingqi.shengxi.model;

public class SendVoiceData extends BaseRepData {
    private SendVoiceBean data;

    public SendVoiceBean getData() {
        return data;
    }

    public void setData(SendVoiceBean data) {
        this.data = data;
    }

    public static class SendVoiceBean {
        private String voice_id;

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }
    }
}
