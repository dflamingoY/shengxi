package org.xiaoxingqi.shengxi.model;

public class CacheBean {

    private String url;//本地音频文件
    private long time;
    private int type;//此次保存的类型   1 普通动态   2 影评 3 话题 4唱回忆 5 看过的书
    private String typeId;//发声兮的type   发送书评, 影评, 唱回忆
    private String topicId;//话题id
    private String topicName;//话题name
    private String images;//json数组
    private String resourceId;//资源id  唱歌 影评书评
    private String userId;
    private String voiceLength;//语音长度
    private String score;//评分等级

    /*
    上传时使用
     */
    private String imgJson;
    private String upVoicePath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImgJson() {
        return imgJson;
    }

    public void setImgJson(String imgJson) {
        this.imgJson = imgJson;
    }

    public String getUpVoicePath() {
        return upVoicePath;
    }

    public void setUpVoicePath(String upVoicePath) {
        this.upVoicePath = upVoicePath;
    }

    public String getVoiceLength() {
        return voiceLength;
    }

    public void setVoiceLength(String voiceLength) {
        this.voiceLength = voiceLength;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
