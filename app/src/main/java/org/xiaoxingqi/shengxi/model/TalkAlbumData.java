package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class TalkAlbumData extends BaseRepData {

    private List<BaseTalkVoiceBean> data;

    public List<BaseTalkVoiceBean> getData() {
        return data;
    }

    public void setData(List<BaseTalkVoiceBean> data) {
        this.data = data;
    }
}
