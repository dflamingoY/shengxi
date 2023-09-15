package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

/**
 * Created by yzm on 2018/3/7.
 */

public class PlayWifiView extends View implements Runnable, SkinCompatSupportable {
    private Context mContext;
    private Paint mPaint;
    public boolean isPlaying = false;
    private boolean isRotation = false;
    private int defaultColor = 0xffffffff;
    private int defaultId = 0;

    public PlayWifiView(Context context) {
        this(context, null);
    }

    public PlayWifiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RectPlayView);
        defaultId = array.getResourceId(R.styleable.RectPlayView_playColor, 0);
        array.recycle();
        mContext = context;
        SkinCompatResources.init(context);
        applySkin();
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(AppTools.getPhoneDensity(mContext) * 1.5f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(defaultColor);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }

    int width, height;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w * 2;
        height = h;
    }

    private int count = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int degrees = 0;
        if (isRotation) {
            degrees = 180;
        }
        canvas.rotate(degrees, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        mPaint.setColor(defaultColor);
        float margin = width / 12f;

        if (isPlaying) {
            if (count % 3 == 0) {
                canvas.drawArc(new RectF(-width / 9f, height / 2f - width / 9f, width / 9f, height / 2f + width / 9f), -50, 100, false, mPaint);
            } else if (count % 3 == 1) {
                canvas.drawArc(new RectF(-width / 9f, height / 2f - width / 9f, width / 9f, height / 2f + width / 9f), -50, 100, false, mPaint);
                canvas.drawArc(new RectF(-width / 4.5f, height / 2f - width / 4.5f, width / 4.5f, height / 2f + width / 4.5f), -50, 100, false, mPaint);
            } else if (count % 3 == 2) {
                canvas.drawArc(new RectF(-width / 9f, height / 2f - width / 9f, width / 9f, height / 2f + width / 9f), -50, 100, false, mPaint);
                canvas.drawArc(new RectF(-width / 4.5f, height / 2f - width / 4.5f, width / 4.5f, height / 2f + width / 4.5f), -50, 100, false, mPaint);
                canvas.drawArc(new RectF(-width / 3f, height / 2f - width / 3f, width / 3f, height / 2f + width / 3f), -50, 100, false, mPaint);
            }
        } else {
            canvas.drawArc(new RectF(-width / 9f, height / 2f - width / 9f, width / 9f, height / 2f + width / 9f), -50, 100, false, mPaint);
            canvas.drawArc(new RectF(-width / 4.5f, height / 2f - width / 4.5f, width / 4.5f, height / 2f + width / 4.5f), -50, 100, false, mPaint);
            canvas.drawArc(new RectF(-width / 3f, height / 2f - width / 3f, width / 3f, height / 2f + width / 3f), -50, 100, false, mPaint);
        }
        canvas.restore();
        count++;
    }

    /**
     * 設置是否旋转
     *
     * @param isLefe
     */
    public void setDirecation(boolean isLefe, int color) {
        isRotation = isLefe;
        defaultColor = color;
        invalidate();
    }

    public void statr() {
        if (!isPlaying) {
            isPlaying = true;
            new Thread(this).start();
        }
    }

    public void stop() {
        if (isPlaying) {
            isPlaying = false;
            postInvalidate();
        }
    }

    @Override
    public void run() {
        count = 0;
        while (isPlaying) {
            SystemClock.sleep(300);
            postInvalidateDelayed(0);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void applySkin() {
        try {
            if (defaultId != 0)
                defaultColor = SkinCompatResources.getInstance().getColorStateList(defaultId).getDefaultColor();
        } catch (Exception e) {

        }
    }
}
