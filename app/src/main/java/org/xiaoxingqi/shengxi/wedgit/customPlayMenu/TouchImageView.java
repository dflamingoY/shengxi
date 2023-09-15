package org.xiaoxingqi.shengxi.wedgit.customPlayMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import skin.support.widget.SkinCompatImageView;

public class TouchImageView extends SkinCompatImageView {
    public TouchImageView(Context context) {
        super(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean click = false;

    public void setClickStatus(boolean isClickState) {
        click = isClickState;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (click) {
                performClick();
            }
        }
        return true;
    }
}
