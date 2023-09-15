package org.xiaoxingqi.shengxi.wedgit.actionTabar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


/**
 * Created by DoctorKevin on 2017/5/8.
 */

public class CustomSeletcor extends LinearLayout implements SkinCompatSupportable {
    private Context mContext;
    private LayoutInflater mLayoutInfalt;
    private ImageView mTabImage;
    private TextView mTabName;
    private ImageView mTabDot;
    private OnButtonClickListener mButtonClickListener;
    private int normalId;
    private String name;
    private boolean isFlag;
    private int selectedId;
    private int selectedColor = 0;
    private int normalTextColor = 0;
    private AnimatorSet mSet;
    private int mTextColorHintResId = 0;
    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (null != mButtonClickListener) {
                mButtonClickListener.onDoubleClicl(CustomSeletcor.this);
            }
            return true;
        }
    });

    public CustomSeletcor(Context context) {
        this(context, null, 0);
    }

    public CustomSeletcor(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeletcor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomSeletcor);
        selectedColor = array.getColor(R.styleable.CustomSeletcor_customSelectedColor, selectedColor);
        normalTextColor = array.getColor(R.styleable.CustomSeletcor_customNormalColor, normalTextColor);
        mTextColorHintResId = array.getResourceId(R.styleable.CustomSeletcor_customNormalColor, 0);
        array.recycle();
        mContext = context;
        SkinCompatResources.init(mContext);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mLayoutInfalt = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInfalt.inflate(R.layout.tab_selector_layout, null);
        this.addView(view);
        //        this.setOnClickListener(mOnClickListener);
        mTabImage = view.findViewById(R.id.tab_image);
        mTabName = view.findViewById(R.id.tab_name);
        mTabDot = view.findViewById(R.id.tab_dot);
        applySkin();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 设置默认的视图
     */
    public void setDefaultView(int resId, String name, boolean isFlag) {
        this.normalId = resId;
        this.name = name;
        this.isFlag = isFlag;
        setTabName(name);
        setTabImage(resId);
        setNameColor(normalTextColor);
        setButtonFlag(isFlag);
    }


    /**
     * 设置选中的视图
     */
    public void setSelectView(int resId, int colorId) {
        this.selectedId = resId;
        selectedColor = colorId;
    }

    /**
     * 设置文字的颜色
     *
     * @param colorId
     */
    public void setNameColor(int colorId) {
        mTabName.setTextColor(colorId);
        //        mTabName.setTextAppearance(mContext, colorId);
    }

    /**
     * 设置按钮图片
     *
     * @param resId
     */
    private void setTabImage(int resId) {
        mTabImage.setImageResource(resId);
    }

    /**
     * 设置按钮文字
     *
     * @param name
     */
    private void setTabName(String name) {
        mTabName.setText(name);
    }

    /**
     * 回复默认设置
     */
    public void clearSelect() {
        setTabImage(normalId);
        setNameColor(normalTextColor);
        setSelected(false);
    }

    /**
     * 设置选中状态
     */
    public void setSelected() {
        setSelected(true);
        setNameColor(selectedColor);
        setTabImage(selectedId);
        //执行动画
        if (mSet != null) {
            if (mSet.isRunning()) {
                mSet.cancel();
            }
        } else {
            mSet = new AnimatorSet();
        }
        mSet.playTogether(ObjectAnimator.ofFloat(mTabImage, "ScaleX", 1f, 0.8f, 1f),
                ObjectAnimator.ofFloat(mTabImage, "ScaleY", 1f, 0.8f, 1f));
        mSet.setInterpolator(new BounceInterpolator());
        mSet.setDuration(520);
        mSet.start();
    }

    public void setButtonFlag(boolean isFlag) {
        if (isFlag) {
            mTabDot.setVisibility(VISIBLE);
        } else {
            mTabDot.setVisibility(GONE);
        }
    }

    @Override
    public void applySkin() {
        try {
            ColorStateList color = SkinCompatResources.getInstance().getColorStateList(mTextColorHintResId);
            normalTextColor = color.getDefaultColor();
            if (isSelected()) {
                setNameColor(selectedColor);
            } else {
                setNameColor(normalTextColor);
            }
            //            mView.setHintTextColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听点击回调；
     */
    public interface OnButtonClickListener {
        /**
         * 如果消費了點擊事件， 需要过滤掉双击事件
         * @param view
         */
        void onDoubleClicl(View view);
    }

    public void setOnButtonClickListener(OnButtonClickListener buttonClickListener) {
        mButtonClickListener = buttonClickListener;
    }

}
