package org.xiaoxingqi.shengxi.wedgit.userWaveView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.ValueAnimator;

public class LineFactoty {
    private LineBean bean1;
    private ValueAnimator animator;
    private LineBean drawBean;
    private SinaView sinaview;

    public LineFactoty(LineBean bean, SinaView view) {
        this.bean1 = bean;
        this.sinaview = view;
        drawBean = new LineBean();
        drawBean.setLeft(bean.getLeft());
        drawBean.setTop(bean.getTop());
        drawBean.setRight(bean.getRight());
        drawBean.setBottom(bean.getBottom());
        initAnim();
    }

    public void start() {
        if (animator == null) {
            initAnim();
        }
        animator.start();
    }

    private void initAnim() {
        drawBean.setLeft(bean1.getLeft());
        drawBean.setTop(bean1.getTop());
        drawBean.setRight(bean1.getRight());
        drawBean.setBottom(bean1.getBottom());
        animator = ValueAnimator.ofFloat(0, bean1.getMaxHeight() - bean1.getMinHeight()).setDuration(800);
        animator.addUpdateListener(animation -> {
            /**
             * 定向增量的移动
             */
            float value = (float) animation.getAnimatedValue();
            drawBean.setBottom(bean1.getBottom() + value);
            drawBean.setTop(bean1.getTop() - value);
            sinaview.invalidate();
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setStartDelay(bean1.getDelay());
    }

    public void end() {
        try {
            if (animator != null) {
                animator.end();
                animator.cancel();
            }
            animator = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void draw(Canvas canvas, Paint paint) {
        if (drawBean != null)
            canvas.drawRoundRect(drawBean.getLeft(), drawBean.getTop(), drawBean.getRight(), drawBean.getBottom(), 10f, 10f, paint);
    }
}
