package org.xiaoxingqi.shengxi.wedgit.flowereffects.typeEvalutor;

import android.graphics.PointF;

import com.nineoldandroids.animation.TypeEvaluator;

/**
 * 树叶行驶轨迹计算
 * <p>
 * 正弦计算出 树叶左右飘逸的位置
 * y轴 渐进 x轴模n 计算正弦值
 */
public class LeavesEvaluator implements TypeEvaluator<PointF> {
    private static final int offset = 50;

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float y = startValue.y + (endValue.y - startValue.y) * fraction;
        float v = (float) (offset * Math.sin((y / 100) % 360));
        return new PointF(v, y);
    }
}
