package org.xiaoxingqi.shengxi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xiaoxingqi.shengxi.R;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by AddBean on 2016/7/1.
 */
public class WXHelper {
    public static final String API_KEY = "306537af6a9d2ffd9f43becfd7851ec3";
    public static final String APP_ID = "wx1c7709f7121a6877";
    public static final String APP_SECRET = "ad861fb8d0c1d534b488b18b1057945d";
    public static final String PARTNER_ID = "1488455642";
    public static final String characterEncoding = "UTF-8";
    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private static final int THUMB_SIZE = 150;
    public static String sOrderNum = "";

    /**
     * 第三方登录；
     */
    public static boolean loginWechat(Context context) {
        //注册到微信
        final IWXAPI api = WXAPIFactory.createWXAPI(context, APP_ID);
        api.registerApp(APP_ID);
        //授权
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        boolean sendReq = api.sendReq(req);
        return sendReq;
    }

    /**
     * 微信支付
     *
     * @param context
     */
    public static boolean wechatPay(Context context, Object bean) {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, APP_ID);
        boolean isPaySupported = msgApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!isPaySupported) {
            Toast.makeText(context, "当前微信版本不支持！", Toast.LENGTH_SHORT).show();
            return false;
        }
       /* sOrderNum = bean.getOut_trade_no();
        msgApi.registerApp(WXHelper.APP_ID);
        PayReq request = new PayReq();
        request.appId = bean.getAppid();
        request.partnerId = bean.getMch_id();
        request.prepayId = bean.getPrepay_id();
        request.packageValue = "Sign=WXPay";
        request.nonceStr = bean.getNonce_str();
        request.timeStamp = bean.getTimestamp();
        SortedMap<Object, Object> signParams = new TreeMap<>();
        signParams.put("appid", request.appId);
        signParams.put("partnerid", request.partnerId);
        signParams.put("prepayid", request.prepayId);
        signParams.put("package", request.packageValue);
        signParams.put("noncestr", request.nonceStr);
        signParams.put("timestamp", request.timeStamp);
        request.sign = bean.getNew_sign();
        msgApi.sendReq(request);*/
        return true;
    }

    /**
     * 分享微信朋友圈；
     */
    public static void shareCircle(Context context, String url, String title, String des, Bitmap bmp) {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, APP_ID);
        WXWebpageObject web = new WXWebpageObject();
        web.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(web);
        msg.title = title;
        msg.description = des;
        if (bmp != null) {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        } else {
            Bitmap temp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(temp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "webpage" + System.currentTimeMillis();
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        msgApi.sendReq(req);
    }

    /**
     * Error:元素类型 "application" 必须后跟属性规范 ">" 或 "/>"。
     * 分享微信；
     */
    public static void shareWeChat(Context context, String url, String title, String des, Bitmap bmp) {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, APP_ID);
        WXWebpageObject web = new WXWebpageObject();
        web.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(web);
        msg.title = title;
        msg.description = des;
        if (bmp != null) {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        } else {
            Bitmap temp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(temp, THUMB_SIZE, THUMB_SIZE, true);
            temp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "webpage" + System.currentTimeMillis();
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        msgApi.sendReq(req);
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String createSign(String characterEncoding, SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//升序
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + API_KEY);
        String sign = MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }


    private static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString;
    }


    private static String getRandomString(int length) {
        String s = "";
        for (int i = 0; i < length; i++) {
            int index = new Random().nextInt(hexDigits.length - 1);
            s = s + hexDigits[index];
        }
        return s;
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
