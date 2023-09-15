package org.xiaoxingqi.shengxi.model;

public class JpushNoticeData {


    /**
     * comment_id : 5136
     * type : comment
     * voice_id : 5009
     */

    private String type;//1=系统消息，2=新好友请求，3=共鸣,4=表白,5=新好友通知，6=会话,7=警告，8=封号
    private String voice_id;
    private String user_id;
    private String chat_id;
    private String dialog_id;
    private String from_user_id;
    private String html_id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVoice_id() {
        return voice_id;
    }

    public void setVoice_id(String voice_id) {
        this.voice_id = voice_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getDialog_id() {
        return dialog_id;
    }

    public void setDialog_id(String dialog_id) {
        this.dialog_id = dialog_id;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(String from_user_id) {
        this.from_user_id = from_user_id;
    }

    public String getHtml_id() {
        return html_id;
    }

    public void setHtml_id(String html_id) {
        this.html_id = html_id;
    }
}
