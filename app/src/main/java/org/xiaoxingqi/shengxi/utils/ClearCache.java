package org.xiaoxingqi.shengxi.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.math.BigDecimal;

public class ClearCache {
    //清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
    public void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    //清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
    public void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    //清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
    public void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/shared_prefs"));
    }

    //清除/data/data/com.xxx.xxx/files下的内容
    public void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }

    //清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
    public void cleanDatabases(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/databcleanFiles(context);ases"));
    }

    /**
     * 清除缓存
     */
    public void clearCache(Context context) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        //		cleanSharedPreference(context);

        //		cleanDatabases(context);
        //Toast.makeText(this, "清理成功", 0).show();
    }

    public double getCacheSize(Context context) throws Exception {
        double size = 0;
        size += getFolderSize(context.getCacheDir());
        size += getFolderSize(context.getExternalCacheDir());
        size += getFolderSize(context.getFilesDir());
        //		size+=getFolderSize(new File("/data/data/"
        //				+ context.getPackageName() + "/shared_prefs"));
        return size;
    }

    //获得本应用缓存的大小
    public String getCaCheSizeFormat(Context context) throws Exception {
        return getFormatSize(getCacheSize(context));
    }

    //格式化大小
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
    //获取指定文件大小

    /**
     * 递归遍历文件夹  获取到文件所有文件的大小
     */
    public long getFolderSize(File file) throws Exception {

        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                if (item.isDirectory()) {
                    deleteFilesByDirectory(item);
                } else
                    item.delete();
            }
        }
    }

    public static void deleteFileAndFile(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                if (item.isDirectory()) {
                    deleteFileAndFile(item);
                } else
                    item.delete();
            }
            directory.delete();
        }
    }

}
