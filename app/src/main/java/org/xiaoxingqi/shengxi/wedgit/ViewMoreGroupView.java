package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


/**
 * Created by yzm on 2017/11/17.
 */

public class ViewMoreGroupView extends BaseLayout implements SkinCompatSupportable {
    private Context mContext;
    private Drawable mDrawable;
    private String mTitleName;
    private TextView mTvMsgCount;
    private int mPixelSize;
    private int secondColor = Color.parseColor("#626569");
    private int secondSize = 13;
    private int textColor = Color.parseColor("#c4c9e2");
    private boolean mIsShowArrow = true;
    private String mSecondTitle;
    private View mViewHint;
    private TextView mTvPrivate;
    private String mPrivateTitle;
    private TextView tvTitleName;
    private View mArrow;
    private int mDrawId;
    private int secondColorId = 0;

    private ImageView mIvTitleSrc;
    private int titleIcon = 0;
    private ImageView ivTitleIcon;
    private Drawable mTitleIconDraw;

    public ViewMoreGroupView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ViewMoreGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewMoreGroupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewMoreGroupView);
        mDrawable = array.getDrawable(R.styleable.ViewMoreGroupView_title_img);
        mTitleName = array.getString(R.styleable.ViewMoreGroupView_title_name);
        mPixelSize = array.getInt(R.styleable.ViewMoreGroupView_title_size, 13);
        secondColor = array.getColor(R.styleable.ViewMoreGroupView_second_color, secondColor);
        secondSize = array.getInt(R.styleable.ViewMoreGroupView_second_Size, 13);
        mIsShowArrow = array.getBoolean(R.styleable.ViewMoreGroupView_show_arrow, true);
        mSecondTitle = array.getString(R.styleable.ViewMoreGroupView_second_title);
        mPrivateTitle = array.getString(R.styleable.ViewMoreGroupView_privaty_Title);
        mTitleIconDraw = array.getDrawable(R.styleable.ViewMoreGroupView_title_icon);
        if (array.hasValue(R.styleable.ViewMoreGroupView_title_img)) {
            mDrawId = array.getResourceId(R.styleable.ViewMoreGroupView_title_img, 0);
        }
        if (array.hasValue(R.styleable.ViewMoreGroupView_second_color)) {
            secondColorId = array.getResourceId(R.styleable.ViewMoreGroupView_second_color, 0);
        }
        if (array.hasValue(R.styleable.ViewMoreGroupView_title_icon)) {
            titleIcon = array.getResourceId(R.styleable.ViewMoreGroupView_title_icon, 0);
        }
        array.recycle();
        initView();
        SkinCompatResources.init(getContext());
        applySkin();
    }

    private void initView() {
        mTvMsgCount = findViewById(R.id.tv_MsgCount);
        mTvPrivate = findViewById(R.id.tv_Private);
        //        mTvMsgCount.setTextColor(secondColor);
        mTvMsgCount.setTextSize(secondSize);
        if (!TextUtils.isEmpty(mSecondTitle)) {
            mTvMsgCount.setText(mSecondTitle);
        }
        tvTitleName = findViewById(R.id.tv_TitleName);
        mIvTitleSrc = findViewById(R.id.iv_titleSrc);
        mViewHint = findViewById(R.id.viewHint);
        tvTitleName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPixelSize);
        mArrow = findViewById(R.id.iv_Arrow);
        mArrow.setVisibility(mIsShowArrow ? VISIBLE : INVISIBLE);
        ivTitleIcon = findViewById(R.id.ivIcon);
        if (mDrawable != null)
            mIvTitleSrc.setImageDrawable(mDrawable);
        else {
            mIvTitleSrc.setVisibility(GONE);
        }
        if (!TextUtils.isEmpty(mTitleName)) {
            tvTitleName.setText(mTitleName);
        }
        if (!TextUtils.isEmpty(mPrivateTitle)) {
            mTvPrivate.setText(mPrivateTitle);
        }
        if (null != mTitleIconDraw) {
            ivTitleIcon.setVisibility(VISIBLE);
            ivTitleIcon.setImageDrawable(mTitleIconDraw);
        }
    }


    @Override
    public int getLayoutId() {
        return R.layout.layout_more_group_view;
    }

    public void setTitle(String title) {
        try {
            if (!TextUtils.isEmpty(title))
                tvTitleName.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMsgCount(String count) {
        if (TextUtils.isEmpty(count)) {
            mTvMsgCount.setText("");
        } else
            mTvMsgCount.setText(count);
    }

    public void setChildTextStatus(boolean isSelected) {
        mTvMsgCount.setSelected(isSelected);
    }

    public String getText() {
        if (mTvMsgCount != null)
            return mTvMsgCount.getText().toString().trim();
        else return "";
    }

    /**
     * 设置提醒是否显示
     *
     * @param isVisible
     */
    public void setHintVisible(boolean isVisible) {
        if (mViewHint != null) {
            mViewHint.setVisibility(isVisible ? VISIBLE : GONE);
        }
    }

    /**
     * 设置箭头是否可见
     *
     * @param visible
     */
    public void setArrowVisible(int visible) {
        mArrow.setVisibility(visible);
    }

    @Override
    public void applySkin() {
        try {
            if (titleIcon != 0) {
                mTitleIconDraw = SkinCompatResources.getInstance().getMipmap(titleIcon);
                if (null != mTitleIconDraw)
                    ivTitleIcon.setImageDrawable(mTitleIconDraw);
            }
            mDrawable = SkinCompatResources.getInstance().getMipmap(mDrawId);
            if (secondColorId != 0) {
                ColorStateList stateList = SkinCompatResources.getInstance().getColorStateList(secondColorId);
                mTvMsgCount.setTextColor(stateList.getDefaultColor());
            }
            if (mIvTitleSrc != null) {
                if (mDrawable != null) {
                    mIvTitleSrc.setImageDrawable(mDrawable);
                }
            }
        } catch (Exception e) {
        }
    }
}
