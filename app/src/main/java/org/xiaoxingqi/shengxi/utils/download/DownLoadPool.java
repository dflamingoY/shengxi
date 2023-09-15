package org.xiaoxingqi.shengxi.utils.download;

import org.xiaoxingqi.shengxi.core.http.OkClientHelper;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 下载失败之后 重试3次 失败之后不处理
 */
public class DownLoadPool {

    private ArrayList<String> loadThread = new ArrayList<>();
    private static DownLoadPool loadPool;
    private HashMap<String, Integer> map = new HashMap<>();

    private DownLoadPool() {

    }

    public static DownLoadPool getInstance() {
        synchronized (DownLoadPool.class) {
            if (loadPool == null) {
                synchronized (DownLoadPool.class) {
                    loadPool = new DownLoadPool();
                }
            }
        }
        return loadPool;
    }

    /**
     * 添加到下载池 异步下载  判断是否在下载池子里
     */
    public void addPool(String url) {
        if (loadThread.contains(url)) {
            return;
        }
        if (map.containsKey(url) && map.get(url) >= 2) {
            return;
        }
        map.put(url, null == map.get(url) ? 1 : map.get(url) + 1);
        loadThread.add(url);
        OkClientHelper.downFile(null, url, o -> {
            if (loadThread.contains(url))
                loadThread.remove(url);
        }, e -> {
            if (loadThread.contains(url))
                loadThread.remove(url);
            if (e instanceof SocketTimeoutException) {
                LocalLogUtils.writeLog("文件下载超时,再次下载", System.currentTimeMillis());
                addPool(url);
            }
        });
    }

    /**
     * 是否正在下载
     *
     * @return
     */
    public boolean isLoading(String url) {
        return loadThread.contains(url);
    }
}
