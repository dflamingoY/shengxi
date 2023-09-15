package org.xiaoxingqi.shengxi.core.http;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by yzm on 2017/11/17.
 * 七牛核心上传类
 */

public class QIniuUpLoadManager {

    public static void cancelUp() {

    }

    public static byte[] comoress(String file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 1024) {//图片最大为1M
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            if (options <= 10) {
                break;
            }
        }
        byte[] array = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;

    }


}
