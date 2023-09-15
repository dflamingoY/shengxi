package org.xiaoxingqi.shengxi.wedgit.flowereffects.typeEvalutor;

import android.graphics.PointF;

import com.nineoldandroids.animation.TypeEvaluator;

/**
 * 雨行驶轨迹
 */
public class SnowEvalutor implements TypeEvaluator<PointF> {
    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float rate = (startValue.y - endValue.y) / (startValue.x - endValue.x);
        float b = startValue.y - rate * startValue.x;
        float currentX = startValue.x + (endValue.x - startValue.x) * fraction;
        float currentY = b + rate * currentX;
        return new PointF(currentX, currentY);
    }
}
