package org.xiaoxingqi.shengxi.model;

/*
成就bean 计算时间    连续天数=  结束时间-开始时间+1
last_at 小于昨天 或为0  ,表示已经断了
last_at 等于昨天 连续到昨天
last_at 等于今天, 今天已经达标
 */
public class AchievementBean {
    private int achievement_type;//成就类型， 1:心情，2:时光机，不传时，默认全部
    private String latest_at;
    private String started_at;

    public int getAchievement_type() {
        return achievement_type;
    }

    public void setAchievement_type(int achievement_type) {
        this.achievement_type = achievement_type;
    }

    public String getLatest_at() {
        return latest_at;
    }

    public void setLatest_at(String latest_at) {
        this.latest_at = latest_at;
    }

    public String getStarted_at() {
        return started_at;
    }

    public void setStarted_at(String started_at) {
        this.started_at = started_at;
    }
}
