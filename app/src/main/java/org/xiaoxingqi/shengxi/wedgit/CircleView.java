package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.xiaoxingqi.shengxi.R;

/**
 * Created by yzm on 2018/3/12.
 */

public class CircleView extends View {

    private Paint mPaint;
    private int mColor = 0xffffff;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = typedArray.getColor(R.styleable.CircleView_circle_bg, 0x000000);
        typedArray.recycle();
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = MeasureSpec.makeMeasureSpec(Math.min(width, height), MeasureSpec.EXACTLY);
        super.onMeasure(size, size);
//        setMeasuredDimension(size,size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight()/2);
        canvas.drawCircle(0, 0, getMeasuredHeight() / 2, mPaint);
        canvas.restore();
    }
}
