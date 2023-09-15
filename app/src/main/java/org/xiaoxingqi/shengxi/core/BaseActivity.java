package org.xiaoxingqi.shengxi.core;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.impl.OnThemeEvent;
import org.xiaoxingqi.shengxi.utils.OsUtil;

import skin.support.SkinCompatManager;

import static org.xiaoxingqi.shengxi.modules.MainActivityKt.nightName;

public class BaseActivity extends SuperActivity {
    private FrameLayout mFrameLayoutContent;
    private View mViewStatusBarPlace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_compat_status_bar);
        mViewStatusBarPlace = findViewById(R.id.view_status_bar_place);
        mFrameLayoutContent = findViewById(R.id.frame_layout_content_place);

        ViewGroup.LayoutParams params = mViewStatusBarPlace.getLayoutParams();
        params.height = getStatusBarHeight();
        mViewStatusBarPlace.setLayoutParams(params);

        try {
            setImmersiveStatusBar(TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName()), 0xffffffff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View contentView = LayoutInflater.from(this).inflate(layoutResID, mFrameLayoutContent, false);
        mFrameLayoutContent.addView(contentView);
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param fontIconDark 状态栏字体和图标颜色是否为深色
     */
    protected void setImmersiveStatusBar(boolean fontIconDark, int statusBarPlaceColor) {
        setTranslucentStatus();
        if (fontIconDark) {
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
        }
        setStatusBarPlaceColor(statusBarPlaceColor);
    }

    protected void setStatusBarPlaceColor(int statusColor) {
        if (mViewStatusBarPlace != null) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName())) {//白天模式
                mViewStatusBarPlace.setBackgroundColor(statusColor);
            } else {//夜间模式
                mViewStatusBarPlace.setBackground(((ViewGroup) getWindow().getDecorView()).getChildAt(0).getBackground());
            }
        }
    }

    public int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThemeEvent(OnThemeEvent event) {
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName())) {//设为夜间模式
            SkinCompatManager.getInstance().loadSkin();
            SkinCompatManager.getInstance().loadSkin(nightName, new SkinCompatManager.SkinLoaderListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess() {
                    setImmersiveStatusBar(false, Color.parseColor("#00ffffff"));
                }

                @Override
                public void onFailed(String errMsg) {

                }
            });
        } else {//设为白天模式
            SkinCompatManager.getInstance().loadSkin("", new SkinCompatManager.SkinLoaderListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess() {
                    setImmersiveStatusBar(true, Color.parseColor("#ffffff"));

                }

                @Override
                public void onFailed(String errMsg) {

                }
            });
        }
    }

}
