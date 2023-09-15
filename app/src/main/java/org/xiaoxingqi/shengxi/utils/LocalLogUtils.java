package org.xiaoxingqi.shengxi.utils;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;

/**
 * 本地日志路径 0/shengxi/cache/result/log
 */
public class LocalLogUtils {

    /**
     * 输入操作日志到本地
     */
    @SuppressLint("MissingPermission")
    public static void writeLog(String log, long time) {
        if (!TextUtils.isEmpty(log)) {
            new Thread(() -> {
                try {
                    File rootFile = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE);
                    if (!rootFile.exists()) {
                        rootFile.mkdirs();
                    }
                    FileWriter file = new FileWriter(new File(rootFile, "sxLog.log"), true);
                    file.write(TimeUtils.getInstance().parseLogTime(time) + "       ");
                    file.write(log);
                    file.write("\n");//换行
                    file.close();
                } catch (Exception e) {//可能会存在用户没有给与文件读写权限
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * 记录Ping发送心跳包的日志 测试包才做此日志
     */
    @SuppressLint("MissingPermission")
    public static void writePing(String log, long time) {
        if (!TextUtils.isEmpty(log)) {
            new Thread(() -> {
                try {
                    File rootFile = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE);
                    if (!rootFile.exists()) {
                        rootFile.mkdirs();
                    }
                    FileWriter file = new FileWriter(new File(rootFile, "sxPing.log"), true);
                    file.write(TimeUtils.getInstance().parseLogTime(time) + "       ");
                    file.write(log);
                    file.write("\n");//换行
                    file.close();
                } catch (Exception e) {//可能会存在用户没有给与文件读写权限
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
