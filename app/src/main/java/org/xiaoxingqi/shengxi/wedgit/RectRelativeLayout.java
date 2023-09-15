package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RectRelativeLayout extends RelativeLayout {
    public RectRelativeLayout(Context context) {
        super(context);
    }

    public RectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(size / 2, MeasureSpec.EXACTLY));
    }
}
