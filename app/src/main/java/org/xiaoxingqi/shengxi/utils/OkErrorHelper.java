package org.xiaoxingqi.shengxi.utils;

import java.net.SocketTimeoutException;

public class OkErrorHelper {
    public static void getErrorMsg(Object obj) {
        if (obj instanceof Exception) {
            if (obj instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~

            }
        }
    }
}
