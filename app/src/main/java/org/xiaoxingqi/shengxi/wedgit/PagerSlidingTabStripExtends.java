/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xiaoxingqi.shengxi.wedgit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.wedgit.helper.NavigatorHelper;

import java.util.Locale;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


public class PagerSlidingTabStripExtends extends HorizontalScrollView implements SkinCompatSupportable, NavigatorHelper.OnNavigatorScrollListener {
    private int normalId = 0;
    private int selectId = 0;
    private int indicatorColorId = 0;

    @Override
    public void applySkin() {
        try {
            tabTextColor = SkinCompatResources.getInstance().getColorStateList(normalId).getDefaultColor();
            selectedTabTextColor = SkinCompatResources.getInstance().getColorStateList(selectId).getDefaultColor();
            indicatorColor = SkinCompatResources.getInstance().getColorStateList(indicatorColorId).getDefaultColor();
            stateSelectedColor = SkinCompatResources.getInstance().getColorStateList(stateSelectedColorId).getDefaultColor();
            stateNormalColor = SkinCompatResources.getInstance().getColorStateList(stateNormalColorId).getDefaultColor();
            postInvalidate();
            updateTabStyles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface IconTabProvider {
        public int getPageIconResId(int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[]{android.R.attr.textSize,
            android.R.attr.textColor};
    // @formatter:on

    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private final PageListener pageListener = new PageListener();
    public OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    private ViewPager pager;

    private int tabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0xffffff;
    private int dividerColor = 0xffffff;

    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private int scrollOffset = 52;
    private int indicatorHeight = 8;
    private int underlineHeight = 2;
    private int dividerPadding = 0;
    private int tabPadding = 24;
    private int dividerWidth = 1;
    /*--------------- add begin  ---------------*/
    //默认的文字大小
    private int tabTextSize = 18;
    private int tabTextColor = 0xFFFF0000;
    //选中的文字大小
    private int selectedTabTextSize = 18;
    private int selectedTabTextColor = 0xFFFF0000;

    private int mSelectedPostion;                                        // 记录当前viewpager选中的postion
    private int mPstsMagin = 0;
    /*--------------- add end  ---------------*/

    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.NORMAL;

    private int lastScrollX = 0;

    private int tabBackgroundResId = R.drawable.background_tab;

    private Locale locale;
    private float mMinScale = 1f;
    private NavigatorHelper navigatorHelper;
    private int stateSelectedColor = 0;
    private int stateNormalColor = 0;
    private int stateSelectedColorId = -1;
    private int stateNormalColorId = -1;

    public PagerSlidingTabStripExtends(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStripExtends(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public PagerSlidingTabStripExtends(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(1, tabTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

        indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor);
        underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor);
        indicatorHeight = a
                .getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight);
        underlineHeight = a
                .getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight);
        dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, dividerPadding);
        tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding);
        tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackgroundResId);
        shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand);
        scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset);
        textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);

        /*--------------- add begin ---------------*/
        tabTextSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabTextSize, tabTextSize);
        tabTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsTabTextColor, tabTextColor);

        selectedTabTextSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsSelectedTabTextSize,
                selectedTabTextSize);
        selectedTabTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsSelectedTabTextColor,
                selectedTabTextColor);
        mPstsMagin = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsMargin, 0);

        /*--------------- add end ---------------*/
        //TODO
        if (a.hasValue(R.styleable.PagerSlidingTabStrip_stateSelectedTextColor)) {
            stateSelectedColor = a.getColor(R.styleable.PagerSlidingTabStrip_stateSelectedTextColor, 0);
            stateSelectedColorId = a.getResourceId(R.styleable.PagerSlidingTabStrip_stateSelectedTextColor, -1);
        }
        if (a.hasValue(R.styleable.PagerSlidingTabStrip_stateNormalTextColor)) {
            stateNormalColor = a.getColor(R.styleable.PagerSlidingTabStrip_stateNormalTextColor, 0);
            stateNormalColorId = a.getResourceId(R.styleable.PagerSlidingTabStrip_stateNormalTextColor, -1);
        }
        mMinScale = tabTextSize * 1f / selectedTabTextSize;

        if (a.hasValue(R.styleable.PagerSlidingTabStrip_pstsSelectedTabTextColor)) {
            selectId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsSelectedTabTextColor, 0);
        }
        if (a.hasValue(R.styleable.PagerSlidingTabStrip_pstsTabTextColor)) {
            normalId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabTextColor, 0);
        }
        if (a.hasValue(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor)) {
            indicatorColorId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, 0);
        }
        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
        SkinCompatResources.init(context);
        applySkin();
    }

    public void setViewPager(ViewPager pager) {
        this.pager = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        navigatorHelper = new NavigatorHelper();
        navigatorHelper.setTotalCount(pager.getAdapter().getCount());
        navigatorHelper.setNavigatorScrollListener(this);
        pager.addOnPageChangeListener(pageListener);
        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();

        if (scrollOffset == 0) {
            int width = getResources().getDisplayMetrics().widthPixels;
            scrollOffset = (int) (width / tabCount + 0.5f);
        }
        for (int i = 0; i < tabCount; i++) {
            if (pager.getAdapter() instanceof IconTabProvider) {
                addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
            } else {
                addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
            }
        }
        updateTabStyles();
        //布局文件家在完成  监听
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                currentPosition = pager.getCurrentItem();
                scrollToChild(currentPosition, 0);
                if (delegatePageListener != null) {
                    delegatePageListener.onPageSelected(0);
                }
            }
        });

    }

    //把指针添加到当前的容器
    private void addTextTab(final int position, String title) {
        FrameLayout frame = new FrameLayout(getContext());
        TextView tab = new TextView(getContext());
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTabTextSize);
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        frame.addView(tab);
        addTab(position, frame);
    }

    private void addIconTab(final int position, int resId) {
        ImageButton tab = new ImageButton(getContext());
        tab.setImageResource(resId);
        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(v -> pager.setCurrentItem(position, true));
        //设置均分滑块在屏幕的位置
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(scrollOffset, ViewGroup.LayoutParams.MATCH_PARENT);
        tab.setLayoutParams(params);
        tabsContainer.addView(tab, position);
    }

    /**
     * 修改样式
     */
    private void updateTabStyles() {

        for (int i = 0; i < tabCount; i++) {
            View v = tabsContainer.getChildAt(i);
            if (v instanceof FrameLayout) {
                TextView tab = (TextView) ((FrameLayout) v).getChildAt(0);
                tab.setTypeface(tabTypeface, tabTypefaceStyle);
                tab.setTextColor(isSelected() ? stateNormalColor : tabTextColor);
                // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                // pre-ICS-build
                if (textAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString().toUpperCase(locale));
                    }
                }
                // 如果选中
                if (mSelectedPostion == i) {
                    tab.setTextColor(isSelected() ? stateSelectedColor : selectedTabTextColor);
                    //                    tab.setScaleX(1f);
                    //                    tab.setScaleY(1f);
                } else {
                    //                    ViewHelper.setScaleX(tab, mMinScale);
                    //                    ViewHelper.setScaleY(tab, mMinScale);
                }
            }
        }
    }

    private void scrollToChild(int position, int offset) {

        if (tabCount == 0) {
            return;
        }

        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        updateTabStyles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount == 0) {
            return;
        }

        final int height = getHeight();

        // draw indicator line

        rectPaint.setColor(indicatorColor);

        // default: line below current tab
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

            View nextTab = tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
        }
        // 绘制indicator 下面的滑块
        canvas.drawRoundRect(lineLeft + dividerPadding, height - indicatorHeight - mPstsMagin, lineRight - dividerPadding, height - mPstsMagin, 5, 5, rectPaint);
        rectPaint.setColor(underlineColor);
        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);
        // draw divider 画分割线
        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
        }
    }

    private int getArgb(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24);
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24);
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                (startB + (int) (fraction * (endB - startB)));
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            navigatorHelper.onPageScrolled(position, positionOffset, positionOffsetPixels);
            currentPosition = position;
            currentPositionOffset = positionOffset;
            scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            invalidate();
            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            /*if (positionOffset > 0) {
                TextView left = (TextView) getChild(position).getChildAt(0);
                TextView right = (TextView) getChild(position + 1).getChildAt(0);
                if (null != left) {
                    float scale = mMinScale + (1 - mMinScale) * (1 - positionOffset);
                    if (scale > 1) scale = 1f;
                    if (scale < mMinScale) {
                        scale = mMinScale;
                    }
                    ViewHelper.setScaleX(left, scale);
                    ViewHelper.setScaleY(left, scale);
                    left.setTextColor(getArgb(1 - positionOffset, tabTextColor, selectedTabTextColor));
                }
                if (null != right) {
                    float scale = mMinScale + (1 - mMinScale) * (positionOffset);
                    if (scale > 1) scale = 1f;
                    if (scale < mMinScale) {
                        scale = mMinScale;
                    }
                    ViewHelper.setScaleX(right, scale);
                    ViewHelper.setScaleY(right, scale);
                    right.setTextColor(getArgb(positionOffset, tabTextColor, selectedTabTextColor));
                }
            }*/
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            navigatorHelper.onPageScrollStateChanged(state);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager.getCurrentItem(), 0);
            }
            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            navigatorHelper.onPageSelected(position);
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
            mSelectedPostion = position;
            updateTabStyles();
        }
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        float scale = mMinScale + (1 - mMinScale) * (enterPercent);
        if (scale > 1) scale = 1f;
        if (scale < mMinScale) {
            scale = mMinScale;
        }
        ViewHelper.setScaleX(getChild(index).getChildAt(0), scale);
        ViewHelper.setScaleY(getChild(index).getChildAt(0), scale);
        ((TextView) getChild(index).getChildAt(0)).setTextColor(getArgb(enterPercent, isSelected() ? stateNormalColor : tabTextColor, isSelected() ? stateSelectedColor : selectedTabTextColor));
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        float scale = mMinScale + (1 - mMinScale) * (1 - leavePercent);
        if (scale > 1) scale = 1f;
        if (scale < mMinScale) {
            scale = mMinScale;
        }
        ViewHelper.setScaleX(getChild(index).getChildAt(0), scale);
        ViewHelper.setScaleY(getChild(index).getChildAt(0), scale);
        ((TextView) getChild(index).getChildAt(0)).setTextColor(getArgb(1 - leavePercent, isSelected() ? stateNormalColor : tabTextColor, isSelected() ? stateSelectedColor : selectedTabTextColor));
    }

    @Override
    public void onSelected(int index, int totalCount) {

    }

    @Override
    public void onDeselected(int index, int totalCount) {//

    }

    private FrameLayout getChild(int index) {
        if (tabsContainer.getChildCount() > index) {
            return (FrameLayout) tabsContainer.getChildAt(index);
        }
        return null;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.dividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return dividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
        requestLayout();
    }

    public boolean getShouldExpand() {
        return shouldExpand;
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.tabTextSize = textSizePx;
        updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public void setTextColor(int textColor) {
        this.tabTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.tabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getTextColor() {
        return tabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.tabPadding = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
