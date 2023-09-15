package org.xiaoxingqi.shengxi.utils;

import android.graphics.PointF;
import android.util.Log;

import com.nineoldandroids.animation.TypeEvaluator;

/**
 * Created by DoctorBigDiao on 2016/10/13.
 */

public class QuadType implements TypeEvaluator<PointF> {
    @Override//endValue  作为方程的顶点
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        Log.d("name", "start--> " + startValue.x + " <--> " + startValue.y + "   end  -> " + endValue.x + "  <--> " + endValue.y);
        PointF value = new PointF();
        float valuex = (endValue.x - startValue.x) * fraction + startValue.x;
        //解析二次函数的方程式
//        float a = (float) ((startValue.y - endValue.y) / (Math.pow(startValue.x, 2) - Math.pow(endValue.x, 2)));
//        float b = (float) (startValue.y - a * Math.pow(startValue.x, 2));
//        float valueY = (float) (a * Math.pow(valuex, 2) + b);
        float a = (float) ((startValue.y - endValue.y) / Math.pow((startValue.x - endValue.x), 2));
        float valueY = (float) (a * Math.pow((valuex - endValue.x), 2) + endValue.y);
        value.x = valuex;
        value.y = valueY;
        return value;
    }
}
