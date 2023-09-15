package org.xiaoxingqi.shengxi.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.BaseImg;
import org.xiaoxingqi.shengxi.model.login.LoginData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DoctorKevin on 2017/5/8.
 */

public class AppTools {
    public final static String telRegex = "[1][3456789]\\d{9}";
    public final static String emailRegex = "^[a-z0-9]+([._\\\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$";
    public static final String URLPATH = "((http|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    private static final String YEAR_MONTHREGEX = "\\d{4}[-]\\d{2}[-]\\d{2}";
    private static final String TOPIC = "#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#";// ##话题
    public static final String ALL = "(" + TOPIC + ")" + "|" + "(" + URLPATH + ")";
    private static ImageLoaderConfiguration config = null;
    public static DisplayImageOptions options = null;
    public static final String POWERACTION = "com.android.settings/.Settings$HighPowerApplicationsActivity";
    public static final String ISBN = "^(\\d{11}|\\d{13})$";

    public static void initDisplayImageOptions() {
        if (null != options) {
            return;
        }
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.drawable_default_tmpry)//加载过程中显示的图片
                .showImageForEmptyUri(R.drawable.drawable_default_tmpry)//加载内容为空显示的图片
                .showImageOnFail(R.drawable.drawable_default_tmpry)//加载失败显示的图片
                .cacheInMemory(true)//启用内存缓存
                .cacheOnDisk(true)//启用硬盘缓存
                .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
                //                .delayBeforeLoading(1000)//设置延迟部分时间再开始加载
                .bitmapConfig(Bitmap.Config.RGB_565)    //设置图片的解码类型
                //        ALPHA_8 代表8位Alpha位图
                //        ARGB_4444 代表16位ARGB位图，由4个4位组成
                //        ARGB_8888 代表32位ARGB位图，由4个8位组成
                //        RGB_565 代表16位RGB位图，R为5位，G为6位，B为5位
                .displayer(new FadeInBitmapDisplayer(1500))//是否图片加载好后渐入的动画时间
                //                .displayer(new RoundedBitmapDisplayer(20))//圆角
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(388))
                .build();
    }

    public static void initImageLoader(Context context) {
        if (null != config) {
            return;
        }
        config = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSizePercentage(13)
                .diskCacheSize(100 * 1024 * 1024)
                .diskCacheFileCount(200)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .threadPriority(Thread.MAX_PRIORITY)
                .imageDownloader(new BaseImageDownloader(context))
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
    }


    /**
     * @param context
     * @param value
     * @return
     */
    public static int dp2px(Context context, int value) {
        return (int) (context.getResources().getDisplayMetrics().density * value + 0.5f);
    }

    public static float dp2pxFloat(Context context, int value) {
        return context.getResources().getDisplayMetrics().density * value;
    }

    /**
     * @param context
     * @param value
     * @return
     */
    public static int px2dp(Context context, int value) {
        return (int) (value * 1f / context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int getWindowsWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getWindowsHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getPhoneDensity(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int getActionBarSize(Context context) {
        try {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            return context.getResources().getDimensionPixelSize(tv.resourceId);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取顶部状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断硬件是否具有导航栏
     *
     * @param activity
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context activity) {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }


    public static String getVersion(Activity activity) {
        PackageManager manager = activity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetOk(Context act) {
        try {
            ConnectivityManager manager = (ConnectivityManager) act
                    .getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            NetworkInfo networkinfo = manager.getActiveNetworkInfo();
            if (networkinfo == null || !networkinfo.isAvailable()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     *      * 判断WIFI网络是否可用
     *      * @param context
     *      * @return
     *      
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                //                return mWiFiNetworkInfo.isAvailable();
                return mWiFiNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    /**
     * 判断移动网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
                //                return  mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 匹配是否是手机号码
     *
     * @param number
     * @return
     */
    public static boolean isMobilePhone(String number) {
        return number.matches(telRegex);
    }

    /**
     * 匹配是否是邮箱地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(emailRegex, email);
    }

    public static String getPath(Uri data, Activity activity) {
        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.getContentResolver().query(data, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                cursor = null;
                if (picturePath == null || picturePath.equals("null")) {
                    Toast toast = Toast.makeText(activity, "图片路径不合法", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return "";
                }
                return picturePath;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 判断是否内存卡可用
     *
     * @return
     */
    public static boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void openGallery(Activity context, int request) {
        try {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            if (intent.resolveActivity(context.getPackageManager()) != null)
                context.startActivityForResult(intent, request);
            else {
                Toast.makeText(context, "相册访问异常", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {

        }
    }

    public static void openCamera(Activity activity, int code, File targetFile) throws Exception {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // 判断存储卡是否可以用，可用进行存储
        if (AppTools.hasSdcard()) {
            File file = new File(Environment.getExternalStorageDirectory(), "");
            if (!file.exists()) {
                file.mkdirs();
            }
            //                mFile = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + System.currentTimeMillis() + ".png");
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileprovider",
                        targetFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(targetFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            activity.startActivityForResult(intent, code);
        }
    }

    public static String getAndroidId(Activity activity) {
        return android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    /**
     * 判断EditText 是否为空或者是否满足字数的要求
     *
     * @param editText
     * @param count    字数限制要求
     * @return
     */
    public static boolean isEmptyEt(EditText editText, int count) {
        if (count == 0) {
            return TextUtils.isEmpty(editText.getText().toString().trim());
        } else {
            if (TextUtils.isEmpty(editText.getText().toString().trim()))
                return true;
            else {
                return editText.getText().toString().trim().length() < count;
            }
        }
    }


    /**
     * 转化html中 图片标签的宽度属性为全屏显示
     *
     * @param
     * @return
     */
/*    public static String getNewContent(String htmltext) {
        try {
            Document doc = Jsoup.parse(htmltext);
            Elements elements = doc.getElementsByTag("img");
            for (Element element : elements) {
                element.removeAttr("width").removeAttr("height");
            }
            Elements body = doc.getElementsByTag("body");
            String data = body.html();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return htmltext;
        }
    }*/
    public static String parseTime2Str(long length) {
        try {
            length = length / 1000;
            if (length < 0) length = 0;
            long minuts = length / 60;
            long rem = length % 60;
            if (rem < 10)
                return minuts + ":0" + rem;
            else
                return minuts + ":" + rem;
        } catch (Exception e) {
            return "0:00";
        }
    }

    public static String parseHMS(int time) {
        int hour = time / 60 / 60;
        int minutes = (time % 3600) / 60;
        int second = time - hour * 60 * 60 - minutes * 60;
        return (hour < 10 ? "0" + hour : hour) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (second < 10 ? "0" + second : second);
    }

    public static String decalToHex(int value) {
        return Integer.toHexString(value);
    }

    public static String getSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return System.currentTimeMillis() + ".aac1";
        }
        String endWith = path.substring(path.lastIndexOf("/") + 1);
        if ("wave.acc".equalsIgnoreCase(endWith)) {

        }
        return endWith + 1;
    }

    /*
    获取4.2.2 之前兼容的文件后缀
     */
    public static String getCompatibleSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return System.currentTimeMillis() + ".aac";
        }
        String endWith = path.substring(path.lastIndexOf("/") + 1);
        if ("wave.acc".equalsIgnoreCase(endWith)) {

        }
        return endWith;
    }

    public static ArrayList<BaseImg> paserString(String str) {
        if (TextUtils.isEmpty(str))
            return null;
        try {
            return (ArrayList) JSON.parseArray(str, BaseImg.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String distancePaser(long distance) {
        try {
            if (distance < 1000) {
                return distance + "m";
            } else {
                return distance / 1000 + "km";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0m";
        }
    }


    public static String getActors(String[] actors) {
        String actor = "";
        if (actors == null || actors.length == 0)
            return "";
        for (String str : actors) {
            actor += str + " ";
        }
        return "（" + actor + "）";
    }

    public static String getActorsDetails(String[] actors) {
        String actor = "";
        if (actors == null || actors.length == 0)
            return "";
        for (String str : actors) {
            actor += str + " ";
        }
        return actor;
    }

    public static String array2String(String[] actors) {
        String actor = "";
        if (actors == null)
            return "";
        for (int a = 0; a < actors.length; a++) {
            if (a != 0) {
                actor += "/" + actors[a];
            } else {
                actor += actors[a];
            }
        }
        return actor;
    }

    /**
     * 匹配时间的年月日
     *
     * @param text
     * @return
     */
    public static String getYearMonth(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return text;
        }
    }

    private static Pattern pattern = Pattern.compile(YEAR_MONTHREGEX);

    /**
     * 文件获取md5
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    /**
     * 避免 十六进制 0开头
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String encode64(String text) {
        try {
            String result = new String(Base64.encode(text.getBytes("utf-8"), Base64.NO_WRAP), "utf-8");
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return text;
        }
    }

    /**
     * 是否是前台进程
     *
     * @param var0
     * @return
     */
    public static boolean isAppRunningForeground(Context var0) {
        @SuppressLint("WrongConstant")
        ActivityManager manager = (ActivityManager) var0.getSystemService("activity");
        try {
            List var2 = manager.getRunningTasks(1);
            if (var2 != null && var2.size() >= 1) {
                boolean var3 = var0.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) var2.get(0)).baseActivity.getPackageName());
                return var3;
            } else {
                return false;
            }
        } catch (SecurityException var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public static String stringToUnicode(String string) {

        StringBuffer unicode = new StringBuffer();

        for (int i = 0; i < string.length(); i++) {

            // 取出每一个字符
            char c = string.charAt(i);

            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }


    public static String md5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(str.getBytes());
            return bytesToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String bucketId = "0";

    /**
     * @param userId    聊天对象的id
     * @param type      当前聊天的内容
     * @param voiceId   基于哪一条声兮的聊天 0 为私聊
     * @param voicePath 音频资源的路径
     * @param length    声音长度
     * @return 拼接的字符串
     */
    public static String fastJson(String userId, int type, String voiceId, String voicePath, String length) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"data\":{\"voiceId\":" + voiceId + ",\"dialogContentType\":" + type + ",\"dialogContentUri\":\"" + voicePath + "\",\"dialogContentLen\":" + length + ",\"bucketId\":" + bucketId + "}}";
    }

    /*
        忘记密码操作
        帮助跳转生成topicType
        "\"topicType\":1"
     */
    public static String fastJson(String userId, int type, String voiceId, String voicePath, String length, String extra) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"data\":{\"voiceId\":" + voiceId + ",\"dialogContentType\":" + type +
                ",\"dialogContentUri\":\"" + voicePath + "\",\"dialogContentLen\":" + length + ",\"bucketId\":" + bucketId + "," + extra + "}}";
    }

    /**
     * 跳转生成socket连接
     *
     * @param userId
     * @param type
     * @param voiceId
     * @return
     */
    public static String fastJson(String userId, int type, String voiceId) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"data\":{\"voiceId\":" + voiceId + ",\"dialogContentType\":" + type + ",\"dialogContentUri\":\"" + "%1$s" + "\",\"dialogContentLen\":" + "%2$s" + ",\"bucketId\":" + "%3$s" + "}}";
    }

    /**
     * 涂鸦会话
     *
     * @param userId
     * @param voicePath
     * @return
     */
    public static String fastJson(String userId, String voicePath, String resourceId) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"data\":{\"voiceId\":" + 0 + ",\"dialogContentType\":" + 2 + ",\"dialogContentUri\":\"" + voicePath + "\",\"dialogContentLen\":" + 0 + ",\"bucketId\":" + bucketId + ",\"resourceId\":" + resourceId + ",\"chatType\":3}}";
    }

    /**
     * 删除操作
     *
     * @param userId
     * @param voiceId
     * @param chatId
     * @param dialogId
     * @return
     */
    public static String fastJson(String userId, String voiceId, String chatId, String dialogId) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"action\":\"delete\",\"data\":{\"voiceId\":"
                + voiceId + ",\"chatId\":" + chatId + ",\"dialogId\":" + dialogId +
                ",\"chatType\":" + (voiceId == "0" ? "2" : voiceId == "-1" ? "3" : "1")
                + "}}";
    }

    public static String graffitiJson(String userId, String voiceId, String chatId, String dialogId, int resourceId) {
        return "{\"flag\":\"singleChat\",\"to\":\"" + IConstant.YUNXINPORT + userId + "\",\"action\":\"delete\",\"data\":{\"voiceId\":"
                + voiceId + ",\"chatId\":" + chatId + ",\"dialogId\":" + dialogId +
                ",\"chatType\":3" + ",\"resourceId\":" + resourceId + "}}";
    }

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    public static int getArgb(float fraction, int startValue, int endValue) {
        int startInt = startValue;
        int startA = (startInt >> 24);
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24);
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                (startB + (int) (fraction * (endB - startB)));
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查看当前网络类型
     *
     * @param var0
     * @return
     */
    @SuppressLint("WrongConstant")
    public static int netType(Context var0) {
        ConnectivityManager var1;
        NetworkInfo var2;
        if ((var1 = (ConnectivityManager) var0.getSystemService("connectivity")) != null && (var2 = var1.getActiveNetworkInfo()) != null) {
            if (var2.getType() == 0) {
                switch (var2.getSubtype()) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                    case 16:
                        return 1;
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                    case 17:
                        return 2;
                    case 13:
                        return 3;
                    default:
                        return 0;
                }
            }

            if (var2.getType() == 1) {
                return 10;
            }
        }

        return 0;
    }


    public static int getInteger(MediaFormat var0, String var1, int var2) {
        if (var0 != null && var1 != null) {
            return var0.containsKey(var1) ? var0.getInteger(var1) : var2;
        } else {
            return var2;
        }
    }

    //获取flyme 版本号
    public static String getSystemProperty() {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.build.display.id");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 获取当前设置的语言
     *
     * @param context
     * @return
     */
    public static String getLanguage(Context context) {
        try {
            LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
            String sta = (loginBean != null) ? SPUtils.getString(context, IConstant.LANGUAGE + loginBean.getUser_id(), "")
                    : SPUtils.getString(context, IConstant.LANGUAGE, "");
            return sta;
        } catch (Exception e) {
        }
        return null;
    }

}
