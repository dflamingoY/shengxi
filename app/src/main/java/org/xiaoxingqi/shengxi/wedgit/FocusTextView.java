package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.util.AttributeSet;

import skin.support.widget.SkinCompatTextView;

/**
 * Created by yzm on 2018/3/28.
 */

public class FocusTextView extends SkinCompatTextView {
    public FocusTextView(Context context) {
        super(context);
    }

    public FocusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
