package org.xiaoxingqi.shengxi.utils;

/**
 * Created by Administrator on 2015/9/11.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * 获取手机信息工具类
 *
 * @author linin
 */
public class PhoneUtil {

    //使用单例模式获取管理手机对象
    private static PhoneUtil instance;
    private TelephonyManager tm;
    private Activity act;
    private String Tag = "PhoneUtil";

    /**
     * 国际移动用户识别码
     */
    private String IMSI;

    /**
     * 摧毁引用
     */
    public void onDestroy() {
        if (null != tm) {
            tm = null;
        }
        if (null != act) {
            act = null;
        }
        if (null != instance) {
            instance = null;
        }
    }

    private PhoneUtil(Activity act) {
        tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        this.act = act;
    }

    public static PhoneUtil getInstance(Activity act) {
        if (instance == null) {
            instance = new PhoneUtil(act);
        } else if (instance.act != act) {
            instance = new PhoneUtil(act);
        }
        return instance;
    }

    /**
     * 获取RAM总大小
     *
     * @param context
     * @return
     */
    public String getSdTotalSize(Context context) {
        StatFs sf = new StatFs("/mnt/sdcard");
        long blockSize = sf.getBlockSize();
        long totalBlocks = sf.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }


    /**
     * 检查是否有前置摄像头
     *
     * @param facing
     * @return
     */
    private boolean checkCameraFacing(int facing) {

        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {

            return false;

        }

        final int cameraCount = Camera.getNumberOfCameras();

        Camera.CameraInfo info = new Camera.CameraInfo();

        for (int i = 0; i < cameraCount; i++) {

            Camera.getCameraInfo(i, info);

            if (facing == info.facing) {

                return true;

            }

        }

        return false;

    }

    /**
     * 检查是否有后置摄像头
     *
     * @return
     */
    public boolean hasBackFacingCamera() {

        final int CAMERA_FACING_BACK = 0;

        return checkCameraFacing(CAMERA_FACING_BACK);

    }

    public boolean hasFrontFacingCamera() {

        final int CAMERA_FACING_BACK = 1;

        return checkCameraFacing(CAMERA_FACING_BACK);

    }

    /**
     * 获取系统sdk的版本
     *
     * @return
     */
    public static int getSdkVersion() {

        return Build.VERSION.SDK_INT;

    }


    /**
     * 是否处于飞行模式
     */
    public boolean isAirModeOpen() {
        return (Settings.System.getInt(act.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1 ? true : false);
    }

    /**
     * 获取后置摄像头支持的像素
     *
     * @return
     */
    public int Pixels() {
        int CammeraIndex = FindBackCamera();
        Camera camera = Camera.open(CammeraIndex);
        Camera.Parameters parameters = camera.getParameters();
        parameters.set("camera-id", 1);
        int newPixels = 0;
        List<Camera.Size> psizelist = parameters.getSupportedPictureSizes();
        if (null != psizelist && 0 < psizelist.size()) {
            int heights[] = new int[psizelist.size()];
            int widths[] = new int[psizelist.size()];
            for (int i = 0; i < psizelist.size(); i++) {
                Camera.Size size = (Camera.Size) psizelist.get(i);
                int sizehieght = size.height;
                int sizewidth = size.width;
                heights[i] = sizehieght;
                widths[i] = sizewidth;
            }
            int Height_Pixels = heights[0];
            int width_Pixels = widths[0];
            newPixels = Height_Pixels * width_Pixels;
            System.out.println(newPixels);
        }
        return newPixels;
    }

    ;

    private int FindBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return camIdx;
            }
        }
        return -1;
    }
  /*  public int getCameraId(int type) {

        int numberOfCameras = Camera.getNumberOfCameras();
               CameraInfo cameraInfo = new CameraInfo();
              for (int i = 0; i < numberOfCameras; i++) {
                       Camera.getCameraInfo(i, cameraInfo);
                      if (cameraInfo.facing == type) {
                              return i;
                          }
                  }
               return 2;
            }*/


    /**
     * 获取手机号码
     */
    @SuppressLint("MissingPermission")
    public String getPhoneNumber() {
        return tm == null ? null : tm.getLine1Number();
    }

    /**
     * 获取网络类型（暂时用不到）
     */
    public int getNetWorkType() {
        return tm == null ? 0 : tm.getNetworkType();
    }

    /**
     * 获取手机IMEI
     */
    @SuppressLint("MissingPermission")
    public String getIMEI() {
        try {
            return null == tm ? "" : tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取手机型号
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机品牌
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机系统版本
     */
    public static String getVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机制造商名称
     */
    public static String getMakerName() {
        return Build.MANUFACTURER;
    }

    /**
     * 获得手机系统总内存
     */
    public String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(act, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }


    /**
     * 获取手机屏幕密度DPI
     */
    public int getScreenDensityDpi() {


        DisplayMetrics metric = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(metric);

        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）


        return densityDpi;
    }

    /**
     * 获取手机屏幕宽
     */
    public int getScreenWidth() {
        return act.getWindowManager().getDefaultDisplay().getWidth();
    }

    /**
     * 获取手机屏高宽
     */
    public int getScreenHeight() {
        return act.getWindowManager().getDefaultDisplay().getHeight();
    }

    /**
     * 获取应用包名
     */
    public String getPackageName() {
        return act.getPackageName();
    }

    /* *//**
     * 获取手机MAC地址
     * 只有手机开启wifi才能获取到mac地址
     *//*
    public String getMacAddress(){
        String result = "";
        WifiManager wifiManager = (WifiManager) act.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();
        return result;
    }*/

    /**
     * 获取手机CPU信息 //1-cpu型号  //2-cpu频率
     */
    public String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""};  //1-cpu型号  //2-cpu频率
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return cpuInfo;
    }

    /* *//**获取Application中的meta-data内容*//*
    public String getMetaData(String name){
        String result = "";
        try {
            ApplicationInfo appInfo = act.getPackageManager()
                    .getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
            result = appInfo.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    */


}