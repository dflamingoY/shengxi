package org.xiaoxingqi.shengxi.model;

import java.util.HashMap;

public class SignCacheData {

    private HashMap<String, String> timeMap;

    public SignCacheData(HashMap<String, String> timeMap) {
        this.timeMap = timeMap;
    }

    public HashMap<String, String> getTimeMap() {
        return timeMap;
    }

    public void setTimeMap(HashMap<String, String> timeMap) {
        this.timeMap = timeMap;
    }
}
