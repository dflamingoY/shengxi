package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;

/**
 * Created by yzm on 2017/12/8.
 */

public class CartoonProgress extends View implements SkinCompatSupportable {

    private Paint mPaint;
    private float progress = 0;
    private ProgressAnim mAnim = new ProgressAnim();
    private float mCurrentX;
    private static final int BASE_TIME = 1500;
    private float offsetProgress = 0;//初始偏移量
    private boolean isEnd = false;
    private SkinCompatBackgroundHelper mBackgroundTintHelper;
    private int progressColorId = 0;
    private int progressColor = Color.RED;

    public CartoonProgress(Context context) {
        this(context, null, 0);
    }

    public CartoonProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartoonProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CartoonProgress);
        progressColorId = array.getResourceId(R.styleable.CartoonProgress_cartoon_color, 0);
        array.recycle();
        SkinCompatResources.init(context);
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        applySkin();
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setColor(progressColor);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isEnd = true;
                offsetProgress = progress;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void applySkin() {
        try {
            progressColor = SkinCompatResources.getInstance().getColorStateList(progressColorId).getDefaultColor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  /*  @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LinearGradient linear = new LinearGradient(0, 0,
                w, h
                , Color.parseColor("#BFE271"), Color.parseColor("#BFE271")
                , Shader.TileMode.MIRROR);
        mPaint.setShader(linear);
    }*/

    /**
     * 绘制一层白色的层
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(0, 0, mCurrentX, getMeasuredHeight(), getMeasuredHeight() / 2f, getMeasuredHeight() / 2f, mPaint);
    }

    /**
     * 创建的动画执行绘制
     */
    private class ProgressAnim extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (!isEnd) {
                mCurrentX = getMeasuredWidth() * progress * interpolatedTime + offsetProgress * getMeasuredWidth();
                postInvalidateOnAnimation();
                if (mChangeListener != null) {
                    mChangeListener.valueChange((progress * interpolatedTime + offsetProgress));
                }
            }
        }
    }

    private OnValueChangeListener mChangeListener;

    public void setOnValueChange(OnValueChangeListener listener) {
        mChangeListener = listener;
    }

    public interface OnValueChangeListener {
        void valueChange(float value);
    }

    /**
     * 设置当前的进度条
     *
     * @param progress 0~1
     */
    public void setProgress(float progress) {
        this.progress = progress - offsetProgress;
        mAnim.setDuration((long) ((progress - offsetProgress) * BASE_TIME));
        /*getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startAnimation(mAnim);
            }
        });*/
        clearAnimation();
        isEnd = false;
        startAnimation(mAnim);
    }
}
