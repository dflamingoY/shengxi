package org.xiaoxingqi.shengxi.model;

public class CommentCallBackData extends BaseRepData {


    /**
     * data : {"id":5124,"voice":"http://voice.gmdoc.com/1537455543017.aac","voice_length":"1","timestamp":1537455542000}
     */

    private TalkListData.TalkListBean data;


    public TalkListData.TalkListBean getData() {
        return data;
    }

    public void setData(TalkListData.TalkListBean data) {
        this.data = data;
    }
}
