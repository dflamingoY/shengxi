package org.xiaoxingqi.shengxi.wedgit.skinView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;

public class SkinStatueText extends AppCompatTextView implements SkinCompatSupportable {
    private int arrayResourceId = 0;
    private int selectTextColor = 0;
    private int colorResourceId = 0;
    private int normalColor = 0;
    private SkinCompatBackgroundHelper mBackgroundTintHelper;

    public SkinStatueText(Context context) {
        this(context, null, 0);
    }

    public SkinStatueText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinStatueText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SkinStatueText);
        if (array.hasValue(R.styleable.SkinStatueText_statusTextSelected)) {
            arrayResourceId = array.getResourceId(R.styleable.SkinStatueText_statusTextSelected, 0);
        }
        array.recycle();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkinTextAppearance);
        if (typedArray.hasValue(R.styleable.SkinTextAppearance_android_textColor)) {
            colorResourceId = typedArray.getResourceId(R.styleable.SkinTextAppearance_android_textColor, 0);
        }
        typedArray.recycle();
        SkinCompatResources.init(getContext());
        applySkin();
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
    }


    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setTextColor(selectTextColor);
        } else {
            setTextColor(normalColor);
        }

    }

    @Override
    public void applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySkin();
        }
        try {
            ColorStateList color = SkinCompatResources.getInstance().getColorStateList(arrayResourceId);
            selectTextColor = color.getDefaultColor();
            ColorStateList color1 = SkinCompatResources.getInstance().getColorStateList(colorResourceId);
            normalColor = color1.getDefaultColor();
            setSelected(isSelected());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
