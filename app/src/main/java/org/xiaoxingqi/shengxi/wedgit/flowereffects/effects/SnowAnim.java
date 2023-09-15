package org.xiaoxingqi.shengxi.wedgit.flowereffects.effects;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import org.jetbrains.annotations.NotNull;

import org.xiaoxingqi.shengxi.wedgit.flowereffects.BaseAnimFactory;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.FlowBean;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.typeEvalutor.SnowEvalutor;

public class SnowAnim extends BaseAnimFactory {

    public SnowAnim(@NotNull FlowBean bean) {
        super(bean);
    }

    @Override
    public void onDraw(@NotNull Canvas canvas) {
        try {
            if (null != getBean().bitmap && !getBean().bitmap.isRecycled()) {
                //            getBean().matrix.postRotate(getBean().degrees, bitmap.getWidth() * 1f / 2, bitmap.getHeight() * 1f / 2);
                getBean().matrix.postScale(getBean().scale, getBean().scale, getBean().bitmap.getWidth() * 1f / 2, getBean().bitmap.getHeight() * 1f / 2);
                getBean().matrix.postTranslate(getBean().currentX, getBean().currentY);
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
        ValueAnimator animator = ValueAnimator.ofObject(new SnowEvalutor(), new PointF(getBean().startX, getBean().startY), new PointF(getBean().endX, getBean().endY));
        animator.addUpdateListener(animation -> {
            PointF value = (PointF) animation.getAnimatedValue();
            getBean().currentX = value.x + getBean().dx;
            getBean().currentY = value.y;
            getBean().degrees = getBean().currentY / 10f % 360;
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                getBean().dx = getBean().random.nextInt((getBean().windowsWidth)) - getBean().startX;
                getBean().scale = (getBean().random.nextInt(3) + 3) / 10f;
            }
        });
        animator.setStartDelay(getBean().delay);
        animator.setDuration(getBean().duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }
}
