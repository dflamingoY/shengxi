package org.xiaoxingqi.shengxi.utils;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class SmoothLayoutManager extends LinearLayoutManager {
    public SmoothLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    public SmoothLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SmoothLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private float MILLISECONDS_PER_INCH = 0.03f;
    private Context mContext;

    public void setSpeedSlow() {
        MILLISECONDS_PER_INCH = mContext.getResources().getDisplayMetrics().density * 0.3f;
    }

    public void setSpeedFast() {
        MILLISECONDS_PER_INCH = mContext.getResources().getDisplayMetrics().density * 0.02f;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller SmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SmoothLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
            // 控制滑动速度

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                // 单位速度 25F/densityDpi
                // return 1F / displayMetrics.densityDpi;
                return MILLISECONDS_PER_INCH / displayMetrics.density;
            }

            //该方法计算滑动所需时间。在此处间接控制速度。
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx);
            }
        };
        SmoothScroller.setTargetPosition(position);
        startSmoothScroll(SmoothScroller);
    }
}
