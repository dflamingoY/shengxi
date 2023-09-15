package org.xiaoxingqi.shengxi.utils;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzm on 2018/1/24.
 */

public class DataHelper<T> {

    private static ArrayList sImgBeans = new ArrayList<>();
    private static DataHelper sDataHelper;

    public DataHelper() {

    }

    public static DataHelper getInstance() {
        synchronized (DataHelper.class) {
            if (sDataHelper == null) {
                synchronized (DataHelper.class) {
                    sDataHelper = new DataHelper();
                }
            }
        }
        return sDataHelper;
    }

    public void addData(List<T> data) {
        sImgBeans.clear();
        sImgBeans.addAll(data);
    }

    public ArrayList<T> getImgBeans() {
        return sImgBeans;
    }

    public void clearData() {
        try {
            sImgBeans.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
