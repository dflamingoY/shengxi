package org.xiaoxingqi.shengxi.utils;

import android.graphics.PointF;

import com.nineoldandroids.animation.TypeEvaluator;

public class StarTypeEvaluator implements TypeEvaluator<PointF> {
    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        PointF point = new PointF();
        point.x = (int) (endValue.x + (startValue.x - endValue.x) * (1 - fraction) + 0.5f);
        point.y = (int) (endValue.y + (startValue.y - endValue.y) * (1 - fraction) + 0.5f);
        return point;
    }
}
