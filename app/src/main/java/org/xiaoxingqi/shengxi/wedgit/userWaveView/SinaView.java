package org.xiaoxingqi.shengxi.wedgit.userWaveView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SinaView extends View {
    private Paint paint;
    private float margin;
    private float lineWidth;
    private List<LineFactoty> facts = new ArrayList<>();

    public SinaView(Context context) {
        super(context);
        initView();
    }

    public SinaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SinaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        margin = getContext().getResources().getDisplayMetrics().density * 4;
        lineWidth = getContext().getResources().getDisplayMetrics().density * 2.5f;
        paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float start = getMeasuredWidth() / 2f - (margin * 4 + lineWidth * 5) / 2f;//其实位置
        float height = 0f;
        height = getMeasuredHeight() * 1 / 7f;
        for (int a = 1; a < 6; a++) {
            LineBean bean = new LineBean();
            long delay = 0;
            switch (Math.abs(a - 3)) {
                case 1:
                    delay = 200;
                    break;
                case 2:
                    delay = 400;
                    break;
                case 0:
                    delay = 0;
                    break;
            }
            bean.setLeft(start + (a - 1) * (lineWidth + margin));
            bean.setTop(getMeasuredHeight() / 2f - height / 2f);
            bean.setRight(start + (a - 1) * (margin) + lineWidth * a);
            bean.setBottom(getMeasuredHeight() / 2f + height / 2);
            bean.setMaxHeight(getMeasuredHeight() * 3 / 7f / 2);
            bean.setMinHeight(getMeasuredHeight() * 1 / 7f / 2);
            bean.setDelay(delay);
            LineFactoty factoty = new LineFactoty(bean, this);
            facts.add(factoty);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, getMeasuredWidth() / 2f, paint);
        paint.setColor(Color.parseColor("#303030"));
        if (!isStart) {
            float start = getMeasuredWidth() / 2f - (margin * 4 + lineWidth * 5) / 2f;//其实位置
            float height = 0f;

            for (int a = 1; a < 6; a++) {
                switch (Math.abs(a - 3)) {
                    case 1:
                        height = getMeasuredHeight() * 2 / 7f;
                        break;
                    case 2:
                        height = getMeasuredHeight() * 1 / 7f;
                        break;
                    case 0:
                        height = getMeasuredHeight() * 3 / 7f;
                        break;
                }
                canvas.drawRoundRect(start + (a - 1) * (lineWidth + margin), getMeasuredHeight() / 2f - height / 2f, start + (a - 1) * (margin) + lineWidth * a, getMeasuredHeight() / 2f + height / 2, 10f, 10f, paint);
            }
        } else {
            for (LineFactoty factoty : facts) {
                factoty.draw(canvas, paint);
            }
        }
    }

    private boolean isStart = false;

    public void start() {
        isStart = true;
        for (LineFactoty factoty : facts) {
            factoty.start();
        }
        invalidate();
    }

    public void stop() {
        isStart = false;
        for (LineFactoty factoty : facts) {
            factoty.end();
        }
        invalidate();
    }

}
