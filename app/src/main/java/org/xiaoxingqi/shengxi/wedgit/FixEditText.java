package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import skin.support.widget.SkinCompatEditText;

public class FixEditText extends SkinCompatEditText {
    public FixEditText(Context context) {
        super(context);
        init();
    }

    public FixEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FixEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int paddingleft = 0;

    private void init() {
        paddingleft = getPaddingLeft();
        int left = (int) getPaint().measureText("#") + paddingleft;
        setPadding(left, getPaddingTop(), getPaddingBottom(), getPaddingRight());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("#", paddingleft, getBaseline(), getPaint());
    }
}
