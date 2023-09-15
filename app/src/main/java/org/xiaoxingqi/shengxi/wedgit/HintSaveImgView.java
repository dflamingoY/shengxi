package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

/**
 * Created by yzm on 2018/3/12.
 */

public class HintSaveImgView extends BaseLayout {

    private boolean isFirst = false;
    private LinearLayout mLinearSave;
    private Context mContext;
    private boolean isShow;
    private boolean isRun;

    public HintSaveImgView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        setAlpha(0);
        setBackgroundColor(Color.parseColor("#cc000000"));
        mLinearSave = findViewById(R.id.linear_save);
        View tvSave = findViewById(R.id.tv_Save);

        tvSave.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onClick(v);
            }
        });
        findViewById(R.id.tvCancel).setOnClickListener(v -> {
            show();
        });
    }

    private OnClickListener mClickListener;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mClickListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isFirst) {
            isFirst = true;
            ObjectAnimator.ofFloat(mLinearSave, "translationY", AppTools.getWindowsHeight(mContext)).setDuration(0).start();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_save_img;
    }

    public void show() {
        if (isRun) {
            return;
        }
        if (isShow) {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(mLinearSave, "translationY", AppTools.getWindowsHeight(mContext)).setDuration(400);
            translationY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isRun = false;
                    isShow = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    isRun = true;
                }
            });
            translationY.start();
            ObjectAnimator.ofFloat(this, "alpha", 0).setDuration(400).start();
        } else {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(mLinearSave, "translationY", 0).setDuration(400);
            translationY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isRun = false;
                    isShow = true;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    isRun = true;
                }
            });
            translationY.start();
            ObjectAnimator.ofFloat(this, "alpha", 1).setDuration(400).start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isShow) {
            show();
            return true;
        }
        return super.onTouchEvent(event);
    }
}


