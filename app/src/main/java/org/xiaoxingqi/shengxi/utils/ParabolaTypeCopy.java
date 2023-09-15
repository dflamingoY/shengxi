package org.xiaoxingqi.shengxi.utils;

import android.graphics.PointF;

import com.nineoldandroids.animation.TypeEvaluator;

/**
 * Created by DoctorBigDiao on 2016/9/28.
 */

public class ParabolaTypeCopy implements TypeEvaluator<PointF> {
    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        PointF value = new PointF();
        float valuex = (endValue.x - startValue.x) * fraction + startValue.x;
        //解析二次函数的方程式 y=ax^2+b;
        float a = (float) ((startValue.y - endValue.y) / (Math.pow(startValue.x, 2) - Math.pow(endValue.x, 2)));
        float b = (float) (startValue.y - a * Math.pow(startValue.x, 2));
//        Log.d("name", "  a " + a + "  b  " + b);
        float valueY = (float) (a * Math.pow(valuex, 2) + b);
        value.x = valuex;
        value.y = valueY;
        return value;
    }
}
