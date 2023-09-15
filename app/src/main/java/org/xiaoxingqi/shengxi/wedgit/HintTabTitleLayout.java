package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;


/**
 * Created by yzm on 2018/2/6.
 */

public class HintTabTitleLayout extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private CharSequence[] mTextArray;
    private int defalueColor = Color.parseColor("#888888");
    private int selectColor = Color.parseColor("#282828");
    private OnClickListener mClickListener;
    private int DP = 1;
    private int mStartX = 0;
    private int mEndX = 0;
    private int mStartFinalX = 0;//动画结束的点坐标
    private int slidWidth = 0;//滑块的宽度
    private SlideAnim mSlideAnim = new SlideAnim();
    private final int TIME = 200;
    private int currentChild = 0;
    private boolean isFirst = false;
    private float titleMagin = 0;//中建的间隔

    public HintTabTitleLayout(Context context) {
        this(context, null, 0);
    }

    public HintTabTitleLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HintTabTitleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BaseTabTitleLayout);
        mTextArray = array.getTextArray(R.styleable.BaseTabTitleLayout_arrayText);
        defalueColor = array.getColor(R.styleable.BaseTabTitleLayout_title_color, defalueColor);
        selectColor = array.getColor(R.styleable.BaseTabTitleLayout_title_select_color, defalueColor);
        titleMagin = array.getDimension(R.styleable.BaseTabTitleLayout_title_margin, titleMagin);
        array.recycle();
        mContext = context;
        initView();
    }

    private void initView() {
        this.setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        this.removeAllViews();
        if (mTextArray != null && mTextArray.length > 0) {
            for (int a = 0; a < mTextArray.length; a++) {
                TextViewHintView textView = new TextViewHintView(mContext);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                if (a != 0) {
                    params.setMarginStart((int) titleMagin);
                }
                textView.setText(mTextArray[a]);
                textView.setSelected(false);
                textView.setOnClickListener(this);
                this.addView(textView, params);
            }
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (getChildCount() > 0) {
                    View childAt = getChildAt(0);
                    int[] local = new int[2];
                    childAt.getLocationInWindow(local);
                    mStartX = local[0];
                    slidWidth = childAt.getWidth();
                    mStartFinalX = childAt.getWidth();
                    setCurrentSelect(0);
                }
            }
        });
    }


    /**
     * 代码设置子view
     *
     * @param arrays
     */
    public void setTitleArray(String[] arrays) {
        removeAllViews();
        mTextArray = arrays;
        for (CharSequence text : mTextArray) {
            TextViewHintView textView = new TextViewHintView(mContext);
            LayoutParams params = new LayoutParams(AppTools.getWindowsWidth(mContext) / mTextArray.length, LayoutParams.MATCH_PARENT);
            textView.setText(text);
            textView.setSelected(false);
            textView.setOnClickListener(this);
            this.addView(textView, params);
        }
        setCurrentSelect(0);
        invalidate();
    }

    private void selectedTitle(View view) {
        clearAll();
        view.setSelected(true);
        mStartFinalX = (int) view.getX() /*+ view.getWidth()*/;
        currentChild = indexOfChild(view);
        mSlideAnim.setDuration(TIME);
        view.startAnimation(mSlideAnim);
    }

    private void clearAll() {
        for (int a = 0; a < getChildCount(); a++) {
            TextViewHintView tv = (TextViewHintView) getChildAt(a);
            tv.setSelected(false);
        }
    }

    public void setOnClick(OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        onDefaultIndexDraw(canvas, mStartX, getMeasuredHeight() - DP, mEndX, getMeasuredHeight() - DP);
    }

    Paint paint = new Paint();

    public boolean onDefaultIndexDraw(Canvas canvas, int x1, int y1, int x2, int y2) {
        paint.setStrokeWidth(AppTools.dp2px(getContext(), 3));
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.colorIndecators));
        canvas.drawRoundRect(x1, y1 - AppTools.dp2px(getContext(), 5), x2, y2 - AppTools.dp2px(getContext(), 3), 5, 5, paint);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isFirst && getChildCount() > 0) {
            TextViewHintView tv = (TextViewHintView) getChildAt(0);
            tv.setSelected(false);
            //            slidWidth = DP * slidWidth;
            //            slidWidth = AppTools.getWindowsWidth(mContext) / mTextArray.length;
            //            mStartFinalX = AppTools.getWindowsWidth(mContext) / mTextArray.length / 2 - slidWidth / 2;
            //            mSlideAnim.setDuration(TIME);
            //            startAnimation(mSlideAnim);
            isFirst = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setFlagState(int position, boolean isVisiable) {

        try {
            TextViewHintView tv = (TextViewHintView) getChildAt(position);
            tv.setFlagVisible(isVisiable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置当前选中
     *
     * @param position
     */
    public void setCurrentSelect(int position) {
        selectedTitle(getChildAt(position));
    }

    private class SlideAnim extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mStartX = (int) (mStartX + (mStartFinalX - mStartX) * interpolatedTime);
            mEndX = mStartX + slidWidth;
            postInvalidate();
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextViewHintView) {
            selectedTitle(v);
            if (mClickListener != null) {
                mClickListener.onClick(v);
            }
        }
    }
}
