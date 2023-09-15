package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;

/**
 * 显示表情和推荐 中立 不推荐的 筛选框
 * 文字颜色和图片资源
 */
public class SelectRecommendView extends LinearLayout implements SkinCompatSupportable {

    private ImageView mIv;
    private TextView mTv;
    private Drawable selectedId;
    private Drawable selectId;
    private boolean isSelectedView;
    private String content = "";
    private int normalColor = 0xff999999;
    private SkinCompatBackgroundHelper mBackgroundTintHelper;
    private int colorResId = 0;
    private int imgResId = 0;
    private int selectedColor = 0xFFF0BB4E;

    public SelectRecommendView(Context context) {
        this(context, null, 0);
    }

    public SelectRecommendView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectRecommendView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SelectRecommendView);
        selectedId = array.getDrawable(R.styleable.SelectRecommendView_selected_img_id);
        //        selectId = array.getDrawable(R.styleable.SelectRecommendView_select_img_id);
        isSelectedView = array.getBoolean(R.styleable.SelectRecommendView_isSelected_img, false);
        content = array.getString(R.styleable.SelectRecommendView_select_Title);
        normalColor = array.getColor(R.styleable.SelectRecommendView_normalColor, normalColor);
            colorResId = array.getResourceId(R.styleable.SelectRecommendView_normalColor, 0);
        imgResId = array.getResourceId(R.styleable.SelectRecommendView_select_img_id, 0);
        selectedColor = array.getColor(R.styleable.SelectRecommendView_selectedColors, selectedColor);
        array.recycle();
        init();
        SkinCompatResources.init(getContext());
        applySkin();
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);
        mIv = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(AppTools.dp2px(getContext(), 27), AppTools.dp2px(getContext(), 27));
        addView(mIv, params);
        mTv = new TextView(getContext());
        mTv.setTextSize(12);
        mTv.setTextColor(getResources().getColor(R.color.colorTextGray));
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.setMargins(AppTools.dp2px(getContext(), 10), 0, 0, 0);
        addView(mTv, params1);
        mTv.setText(content);
        mTv.setTextColor(normalColor);
        setSelected(isSelectedView);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        try {
            if (selected) {
                if (selectedId != null)
                    mIv.setImageDrawable(selectedId);
                mTv.setTextColor(selectedColor);
            } else {
                if (selectId != null)
                    mIv.setImageDrawable(selectId);
                mTv.setTextColor(normalColor);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }


    @Override
    public void applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySkin();
        }
        try {
            ColorStateList color = SkinCompatResources.getInstance().getColorStateList(colorResId);
            normalColor = color.getDefaultColor();
            mTv.setTextColor(normalColor);
            selectId = SkinCompatResources.getInstance().getMipmap(imgResId);
            if (selectId != null) {
                mIv.setImageDrawable(selectId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
