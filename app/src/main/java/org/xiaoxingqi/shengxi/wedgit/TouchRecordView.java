package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yzm on 2018/3/13.
 */

public class TouchRecordView extends RelativeLayout {

    private float mInitialRadius;   // 初始波纹半径
    private float mMaxRadius;   // 最大波纹半径
    private long mDuration = 2000; // 一个波纹从创建到消失的持续时间
    private int mSpeed = 500;   // 波纹的创建速度，每500ms创建一个
    private float mMaxRadiusRate = 1f;
    private boolean mMaxRadiusSet;

    private boolean mIsRunning;
    private long mLastCreateTime;
    private List<Circle> mCircleList = new ArrayList<>();

    public TouchRecordView(Context context) {
        super(context);
    }

    public TouchRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };

    private Interpolator mInterpolator = new DecelerateInterpolator();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public void setStyle(Paint.Style style) {
        mPaint.setStyle(style);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    public void setMaxRadiusRate(float maxRadiusRate) {
        mMaxRadiusRate = maxRadiusRate;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#3346CDCF"));
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            float radius = circle.getCurrentRadius();
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mPaint);
            } else {
                iterator.remove();
            }
        }
        if (mCircleList.size() > 0) {
            postInvalidateDelayed(10);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * 开始
     */
    public void start() {
        startView(0.6f);
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     * 缓慢停止
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * 立即停止
     */
    public void stopImmediately() {
        mIsRunning = false;
        mCircleList.clear();
        invalidate();
    }

    protected void onDraw(Canvas canvas) {

    }

    public void setInitialRadius(float radius) {
        mInitialRadius = radius;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setMaxRadius(float maxRadius) {
        mMaxRadius = maxRadius;
        mMaxRadiusSet = true;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        invalidate();
        mLastCreateTime = currentTime;
    }

    private class Circle {
        private long mCreateTime;

        Circle() {
            mCreateTime = System.currentTimeMillis();
        }

        int getAlpha() {
            float percent = (getCurrentRadius() - mInitialRadius) / (mMaxRadius - mInitialRadius);
            return (int) (255 - mInterpolator.getInterpolation(percent) * 255);
        }

        float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private boolean isTouchable = true;

    public void setOnTouchable(boolean isTouchable) {
        this.isTouchable = isTouchable;
    }

    public boolean isTouchable() {
        return isTouchable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouchable) {
            if (mOnStartRecord != null) {
                mOnStartRecord.permissDenial();
            }
            return false;
        }
        if (isRecording) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mOnStartRecord != null) {
                    mOnStartRecord.start();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (event.getY() < 0) {

                }

                if (mIsRunning) {
                    stopImmediately();
                    endView();
                    if (mOnStartRecord != null) {
                        mOnStartRecord.stop();
                    }
                }
                break;
        }
        return true;
    }

    /**
     *
     */
    private boolean isRecording = false;


    /**
     * 执行view动画
     */
    private void startView(float scale) {
        View childAt = getChildAt(0);
        childAt.clearAnimation();
        if (childAt != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(childAt, "scaleX", scale).setDuration(150);
            scaleX.start();
            scaleX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    isRecording = true;
                }
            });
            ObjectAnimator.ofFloat(childAt, "scaleY", scale).setDuration(150).start();
        }
    }

    private void endView() {
        View childAt = getChildAt(0);
        childAt.clearAnimation();
        if (childAt != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(childAt, "scaleX", 1f).setDuration(150);
            scaleX.start();
            scaleX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isRecording = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                }
            });
            ObjectAnimator.ofFloat(childAt, "scaleY", 1f).setDuration(150).start();
        }
    }


    private OnStartRecord mOnStartRecord;

    public void setOnStartListener(OnStartRecord record) {
        mOnStartRecord = record;
    }

    public interface OnStartRecord {
        void start();

        void stop();

        void permissDenial();
    }
}
