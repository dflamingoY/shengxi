package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

public class UserSeekBar extends AppCompatSeekBar implements SkinCompatSupportable {
    private int progressId = 0;
    private int thumbId = 0;

    public UserSeekBar(Context context) {
        this(context, null);
    }

    public UserSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressSeekBar);
        if (typedArray.hasValue(R.styleable.ProgressSeekBar_progress_skin_drawable)) {
            progressId = typedArray.getResourceId(R.styleable.ProgressSeekBar_progress_skin_drawable, 0);
        }
        if (typedArray.hasValue(R.styleable.ProgressSeekBar_progress_skin_thumb)) {
            thumbId = typedArray.getResourceId(R.styleable.ProgressSeekBar_progress_skin_thumb, 0);
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        SkinCompatResources.init(getContext());
        applySkin();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    @Override
    public void applySkin() {
        try {
            Drawable drawable = SkinCompatResources.getInstance().getDrawable(progressId);
            Drawable drawable1 = SkinCompatResources.getInstance().getMipmap(thumbId);
            if (drawable != null) {
                setProgressDrawable(drawable);
            }
            if (drawable1 != null) {
                setThumb(drawable1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
