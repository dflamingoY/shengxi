package org.xiaoxingqi.shengxi.wedgit;

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

/**
 * 展示几种颜色值
 */
public class SkinColorsTextView extends AppCompatTextView implements SkinCompatSupportable {

    private int normalColor = 0;
    private int selectedColor = 0;
    private int normalColorId = 0;
    private int selectedColorId = 0;
    private SkinCompatBackgroundHelper mBackgroundTintHelper;

    public SkinColorsTextView(Context context) {
        this(context, null, 0);
    }

    public SkinColorsTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinColorsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LinearStatusText);
        normalColor = array.getColor(R.styleable.LinearStatusText_textNormalColor, 0);
        selectedColor = array.getColor(R.styleable.LinearStatusText_textSelectedColor, 0);
        normalColorId = array.getResourceId(R.styleable.LinearStatusText_textNormalColor, 0);
        selectedColorId = array.getResourceId(R.styleable.LinearStatusText_textSelectedColor, 0);
        array.recycle();
        SkinCompatResources.init(context);
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
            setTextColor(selectedColor);
        } else {
            setTextColor(normalColor);
        }
    }

    @Override
    public void applySkin() {
        try {
            if (mBackgroundTintHelper != null) {
                mBackgroundTintHelper.applySkin();
            }
            ColorStateList stateList = SkinCompatResources.getInstance().getColorStateList(normalColorId);
            ColorStateList stateList1 = SkinCompatResources.getInstance().getColorStateList(selectedColorId);
            normalColor = stateList.getDefaultColor();
            selectedColor = stateList1.getDefaultColor();
            setSelected(isSelected());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
