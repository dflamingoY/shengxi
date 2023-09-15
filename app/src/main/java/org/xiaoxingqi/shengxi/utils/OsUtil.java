package org.xiaoxingqi.shengxi.utils;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Created by XiaoJianjun on 2017/3/7.
 * os相关的工具类.
 */
public class OsUtil {
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";

    public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean isHw() {
        return !TextUtils.isEmpty(getSystemProperty(KEY_VERSION_EMUI));
    }

    public static boolean isOppo() {
        return TextUtils.isEmpty(getSystemProperty(KEY_VERSION_OPPO));
    }

    public static boolean isVivo() {
        return TextUtils.isEmpty(getSystemProperty(KEY_VERSION_VIVO));
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    public static boolean isMeizu() {
        return Build.MANUFACTURER.equalsIgnoreCase("Meizu");
    }
}
