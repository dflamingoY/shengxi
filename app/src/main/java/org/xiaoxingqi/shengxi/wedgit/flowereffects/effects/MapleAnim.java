package org.xiaoxingqi.shengxi.wedgit.flowereffects.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import org.jetbrains.annotations.NotNull;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.BaseAnimFactory;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.FlowBean;

public class MapleAnim extends BaseAnimFactory {

    public MapleAnim(@NotNull FlowBean bean) {
        super(bean);
    }

    @Override
    public void onDraw(@NotNull Canvas canvas) {
        try {
            if (null != getBean().bitmap && !getBean().bitmap.isRecycled()) {
                getBean().matrix.postRotate(getBean().degrees, getBean().bitmap.getWidth() * 1f / 2, getBean().bitmap.getHeight() * 1f / 2);
                getBean().matrix.postScale(getBean().scale, getBean().scale, getBean().bitmap.getWidth() * 1f / 2, getBean().bitmap.getHeight() * 1f / 2);
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
            float fraction = animation.getAnimatedFraction();
            //            getBean().scale = 1 - fraction;
            Float animatedValue = (Float) animation.getAnimatedValue();
            getBean().degrees = animatedValue / 10f % 360;
            //            getBean().currentX = getBean().startX + animatedValue.x;
            getBean().currentY = animatedValue;
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                getBean().scale = (getBean().random.nextInt(5) + 5) / 10f;
                getBean().startX = getBean().random.nextInt(getBean().windowsWidth);
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
