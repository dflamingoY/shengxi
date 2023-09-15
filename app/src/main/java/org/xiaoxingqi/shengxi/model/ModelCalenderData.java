package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class ModelCalenderData extends BaseRepData {

    private List<ModelCalenderBean> data;

    public List<ModelCalenderBean> getData() {
        return data;
    }

    public void setData(List<ModelCalenderBean> data) {
        this.data = data;
    }

    public static class ModelCalenderBean {
        private String voiceDay;

        public String getVoiceDay() {
            return voiceDay;
        }

        public void setVoiceDay(String voiceDay) {
            this.voiceDay = voiceDay;
        }
    }


}
