package org.xiaoxingqi.shengxi.core;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils;
import org.xiaoxingqi.shengxi.dialog.DialogCommitPwd;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;
import org.xiaoxingqi.shengxi.utils.OsUtil;
import org.xiaoxingqi.shengxi.utils.SPUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public abstract class BaseLoginAct extends AppCompatActivity {
    private FrameLayout mFrameLayoutContent;
    private View mViewStatusBarPlace;
    protected GlideJudeUtils glideUtil;
    protected DialogCommitPwd dialogPwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        super.setContentView(R.layout.activity_compat_normal_bar);
        glideUtil = new GlideJudeUtils(this);
        mViewStatusBarPlace = findViewById(R.id.view_status_bar_place);
        mFrameLayoutContent = findViewById(R.id.frame_layout_content_place);
        ViewGroup.LayoutParams params = mViewStatusBarPlace.getLayoutParams();
        params.height = getStatusBarHeight();
        mViewStatusBarPlace.setLayoutParams(params);
        setImmersiveStatusBar(0xffffffff);
        setContentView(getLayoutId());
        initView();
        initData();
        initEvent();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View contentView = LayoutInflater.from(this).inflate(layoutResID, mFrameLayoutContent, false);
        mFrameLayoutContent.addView(contentView);
    }

    protected void setImmersiveStatusBar(int statusBarPlaceColor) {
        setTranslucentStatus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                || OsUtil.isMIUI()
                || OsUtil.isFlyme()) {
            setStatusBarFontIconDark(true);
        } else {
            if (statusBarPlaceColor == Color.WHITE) {
                statusBarPlaceColor = 0xffcccccc;
            } else {

            }
        }
        setStatusBarPlaceColor(statusBarPlaceColor);
    }

    protected void setStatusBarPlaceColor(int statusColor) {
        if (mViewStatusBarPlace != null) {
            mViewStatusBarPlace.setBackgroundColor(statusColor);
        }
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }


    /**
     * 设置状态栏透明
     */
    private void setTranslucentStatus() {

        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    private void setStatusBarFontIconDark(boolean dark) {
        // 小米MIUI
        try {
            Window window = getWindow();
            Class clazz = getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
        }

        // 魅族FlymeUI
        try {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
        }
        // android6.0+系统
        // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initEvent();

    public void request(int flag) {

    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String str) {
        changeLanguage();
        recreate();//刷新界面
    }

    private void changeLanguage() {
        String sta = SPUtils.getString(this, IConstant.LANGUAGE, "");
        if (!TextUtils.isEmpty(sta)) {
            Locale myLocale = null;
            //            if ("US".equalsIgnoreCase(sta)) {
            //                myLocale = new Locale("en", sta);
            //            } else if ("JP".equalsIgnoreCase(sta)) {
            //                myLocale = new Locale("ja", sta);
            //            } else {
            //            }
            myLocale = new Locale("zh", sta);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            Configuration configuration = getResources().getConfiguration();
            configuration.locale = myLocale;
            getResources().updateConfiguration(configuration, displayMetrics);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LocalLogUtils.writeLog("App in background:${this.localClassName}", System.currentTimeMillis());
    }

}
