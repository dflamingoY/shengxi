package org.xiaoxingqi.shengxi.wedgit.flowereffects.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.animation.FastOutLinearInInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import org.jetbrains.annotations.NotNull;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.FlowBean;

import org.xiaoxingqi.shengxi.wedgit.flowereffects.BaseAnimFactory;


public class RainAnim extends BaseAnimFactory {
    //    private Paint paint;

    public RainAnim(@NotNull FlowBean bean) {
        super(bean);
        //        paint = new Paint();
        //        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //        paint.setDither(true);
        //        paint.setAntiAlias(true);
        //        paint.setColor(Color.parseColor("#999999"));
    }

    @Override
    public void onDraw(@NotNull Canvas canvas) {
        //        canvas.drawRect(getBean().startX, getBean().currentY, getBean().startX + rainWidth, getBean().currentY + rainHeight, paint);
        try {
            if (null != getBean().bitmap && !getBean().bitmap.isRecycled()) {
                getBean().matrix.postScale(getBean().scale, getBean().scale, getBean().bitmap.getWidth() / 2f, getBean().bitmap.getHeight() / 2f);
                getBean().matrix.postTranslate(getBean().startX, getBean().currentY);
                canvas.drawBitmap(getBean().bitmap, getBean().matrix, null);
                getBean().matrix.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Animator createAnim() {
        ValueAnimator animator = ValueAnimator.ofFloat(getBean().startY, getBean().endY);
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            getBean().currentY = animatedValue;
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                getBean().scale = (getBean().random.nextInt(5) + 1) / 10f;
                getBean().startX = getBean().random.nextInt(getBean().windowsWidth);
            }
        });
        animator.setDuration(getBean().duration);
        animator.setStartDelay(getBean().delay);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new FastOutLinearInInterpolator());
        //        animator.setStartDelay(2000);
        return animator;
    }

    @Override
    public void stop() {
        super.stop();
        try {
            //            paint = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
