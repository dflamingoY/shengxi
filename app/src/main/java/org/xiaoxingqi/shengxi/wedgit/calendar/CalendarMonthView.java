package org.xiaoxingqi.shengxi.wedgit.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


/**
 * 自定义日历View，可多选
 */
public class CalendarMonthView extends View implements SkinCompatSupportable {

    // 列的数量
    private static final int NUM_COLUMNS = 7;
    // 行的数量
    private static final int NUM_ROWS = 6;

    /**
     * 可选日期数据
     */
    private List<String> mOptionalDates = new ArrayList<>();

    /**
     * 以选日期数据
     */
    private List<String> mSelectedDates = new ArrayList<>();

    // 背景颜色
    private int circleBgColor = 0xff46CDCF;
    // 天数不可选颜色
    private int workDayColor = 0xff333333;
    private int weekendColor = 0xff333333;
    private int splitLineColor = 0xffdddddd;
    private int selectedTextColor = 0xffffffff;

    private int circleBgColorId;
    private int weekendColorId;
    private int workDayColorId;
    private int splitLineId;
    private int selectedTextColorId;

    // 天数字体大小
    private int mDayTextSize = 14;
    // 是否可以被点击状态
    private boolean mClickable = true;

    private DisplayMetrics mMetrics;
    private Paint mPaint;
    private int mCurYear;
    private int mCurMonth;
    private int mCurDate;

    private int mSelYear;
    private int mSelMonth = -1;
    private int mSelDate;
    private int mColumnSize;
    private int mRowSize;
    private int[][] mDays;

    // 当月一共有多少天
    private int mMonthDays;
    // 当月第一天位于周几
    private int mWeekNumber;
    private Paint mCirclePaint;
    private Context mContext;

    public CalendarMonthView(Context context) {
        this(context, null, 0);
    }

    public CalendarMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CalendarMonthView);
        selectedTextColor = array.getColor(R.styleable.CalendarMonthView_selected_color, 0);
        splitLineColor = array.getColor(R.styleable.CalendarMonthView_splitLine_color, 0);
        weekendColor = array.getColor(R.styleable.CalendarMonthView_weekend_color, 0);
        circleBgColor = array.getColor(R.styleable.CalendarMonthView_selectedCircle_color, circleBgColor);
        workDayColor = array.getColor(R.styleable.CalendarMonthView_workDay_color, workDayColor);
        //获取资源链接
        circleBgColorId = array.getResourceId(R.styleable.CalendarMonthView_selectedCircle_color, 0);
        workDayColorId = array.getResourceId(R.styleable.CalendarMonthView_workDay_color, 0);
        weekendColorId = array.getResourceId(R.styleable.CalendarMonthView_weekend_color, 0);
        splitLineId = array.getResourceId(R.styleable.CalendarMonthView_splitLine_color, 0);
        selectedTextColorId = array.getResourceId(R.styleable.CalendarMonthView_selected_color, 0);
        array.recycle();
        mContext = context;
        init();
        SkinCompatResources.init(getContext());
        applySkin();
    }

    private void init() {
        // 获取手机屏幕参数
        mMetrics = getResources().getDisplayMetrics();
        // 创建画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        //mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setDither(true);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setFilterBitmap(true);
        mCirclePaint.setColor(circleBgColor);
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        mCurYear = calendar.get(Calendar.YEAR);
        mCurMonth = calendar.get(Calendar.MONTH);
        mCurDate = calendar.get(Calendar.DATE);
        setSelYTD(mCurYear, mCurMonth, mCurDate);
        mDays = new int[6][7];
        // 设置绘制字体大小
        mPaint.setTextSize(mDayTextSize * mMetrics.scaledDensity);
        // 设置绘制字体颜色
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSelYear != 0 && mSelMonth != -1) {
            mMonthDays = DateUtils.getMonthDays(mSelYear, mSelMonth);
            mWeekNumber = DateUtils.getFirstDayWeek(mSelYear, mSelMonth);
            int count = (int) Math.ceil((mMonthDays + mWeekNumber - 1) / 7f);
            initSize();
            if (count > 6) {
                count = 6;
            }
            int viewHeight = mRowSize * count;
            setMeasuredDimension(widthMeasureSpec, viewHeight);
        } else {
            super.onMeasure(widthMeasureSpec, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initSize();
        // 绘制背景
        String dayStr;
        // 获取当月一共有多少天
        mMonthDays = DateUtils.getMonthDays(mSelYear, mSelMonth);
        // 获取当月第一天位于周几
        mWeekNumber = DateUtils.getFirstDayWeek(mSelYear, mSelMonth);
        for (int day = 0; day < mMonthDays; day++) {
            dayStr = String.valueOf(day + 1);
            int column = (day + mWeekNumber - 1) % 7;
            int row = (day + mWeekNumber - 1) / 7;
            mDays[row][column] = day + 1;
            int startX = (int) (mColumnSize * column + (mColumnSize - mPaint.measureText(dayStr)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            int left = mColumnSize * column;
            int top = mRowSize * row;
            mPaint.setColor(splitLineColor);
            if (row != 0) {
                canvas.drawLine(20, top, getWidth() - 20, top, mPaint);
                //                canvas.drawRect(20, top, getWidth() - 20, top + 1, mPaint);
            }
            if (mOptionalDates.contains(getSelData(mSelYear, mSelMonth, mDays[row][column]))) {
                canvas.drawCircle(left + mColumnSize / 2, top + mRowSize / 2, mColumnSize > mRowSize ? mRowSize / 2 - AppTools.dp2px(mContext, 2) : mColumnSize / 2 - AppTools.dp2px(mContext, 2), mCirclePaint);
                mPaint.setColor(selectedTextColor);
                canvas.drawText(dayStr, startX, startY, mPaint);
            } else {
                if (column % 7 == 0 || column % 7 == 6) {
                    mPaint.setColor(weekendColor);
                } else
                    mPaint.setColor(workDayColor);
                canvas.drawText(dayStr, startX, startY, mPaint);
            }
        }
    }

    private int downX = 0, downY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventCode = event.getAction();
        switch (eventCode) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (!mClickable) return true;
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (Math.abs(upX - downX) < 10 && Math.abs(upY - downY) < 10) {
                    performClick();
                    onClick((upX + downX) / 2, (upY + downY) / 2);
                }
                break;
        }
        return true;
    }

    /**
     * 点击事件
     */
    private void onClick(int x, int y) {
        try {
            int row = y / mRowSize;
            int column = x / mColumnSize;
            setSelYTD(mSelYear, mSelMonth, mDays[row][column]);



       /* // 判断是否点击过
        boolean isSelected = mSelectedDates.contains(getSelData(mSelYear, mSelMonth, mSelDate));
        // 判断是否可以添加
        boolean isCanAdd = mOptionalDates.contains(getSelData(mSelYear, mSelMonth, mSelDate));
        if (isSelected) {
            mSelectedDates.remove(getSelData(mSelYear, mSelMonth, mSelDate));
        } else if (isCanAdd) {
            mSelectedDates.add(getSelData(mSelYear, mSelMonth, mSelDate));
        }
        invalidate();*/
            if (mOptionalDates.contains(getSelData(mSelYear, mSelMonth, mSelDate))) {
                if (mListener != null) {
                    mListener.onClickDateListener(mSelYear, (mSelMonth + 1), mSelDate, getSelData(mSelYear, mSelMonth, mSelDate));
                }
                if (onTouchListener != null) {
                    onTouchListener.setRect(mColumnSize, mRowSize);
                    onTouchListener.position(column, row);
                }

            }
        } catch (Exception e) {
        }
    }

    private OnTouchPosition onTouchListener;

    public void onBindView(OnTouchPosition onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public interface OnTouchPosition {
        void position(int x, int y);

        void setRect(int x, int y);
    }


    /**
     * 初始化列宽和高
     */
    private void initSize() {
        // 初始化每列的大小
        mColumnSize = getWidth() / NUM_COLUMNS;
        // 初始化每行的大小
        mRowSize = (int) (35 * mMetrics.scaledDensity);
    }

    /**
     * 设置可选择日期
     *
     * @param dates 日期数据
     */
    public void setOptionalDate(String[] dates) {
        mOptionalDates.clear();
        if (null == dates)
            return;
        mOptionalDates.addAll(Arrays.asList(dates));
        //        if (!TextUtils.isEmpty(dates)) {
        //            String[] split = dates.split("_");
        //            for (int a = 0; a < split.length; a++) {
        //                if (!TextUtils.isEmpty(split[a])) {
        //                    mOptionalDates.add(split[a]);
        //                }
        //            }
        //        }
        //        invalidate();
    }

    /**
     * 设置已选日期数据
     */
    public void setSelectedDates(List<String> dates) {
        this.mSelectedDates = dates;
    }

    /**
     * 获取已选日期数据
     */
    public List<String> getSelectedDates() {
        return mSelectedDates;
    }

    /**
     * 设置日历是否可以点击
     */
    @Override
    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    /**
     * 设置年月日  月份没有加 1
     *
     * @param year  年
     * @param month 月
     * @param date  日
     */
    private void setSelYTD(int year, int month, int date) {
        this.mSelYear = year;
        this.mSelMonth = month;
        this.mSelDate = date;
    }

    /**
     * 设置上一个月日历
     */
    public void setLastMonth() {
        int year = mSelYear;
        int month = mSelMonth;
        int day = mSelDate;
        // 如果是1月份，则变成12月份
        if (month == 0) {
            year = mSelYear - 1;
            month = 11;
        } else if (DateUtils.getMonthDays(year, month) == day) {
            //　如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            month = month - 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            month = month - 1;
        }
        setSelYTD(year, month, day);
        invalidate();
    }

    /**
     * 设置下一个日历
     */
    public void setNextMonth() {
        int year = mSelYear;
        int month = mSelMonth;
        int day = mSelDate;
        // 如果是12月份，则变成1月份
        if (month == 11) {
            year = mSelYear + 1;
            month = 0;
        } else if (DateUtils.getMonthDays(year, month) == day) {//当前日期为本月最后一天
            //　如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            month = month + 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            month = month + 1;
        }
        setSelYTD(year, month, day);
        invalidate();
    }

    public void setDate(int year, int month) {
        mSelYear = year;
        mSelMonth = month - 1;
        int mMonthDays = DateUtils.getMonthDays(mSelYear, mSelMonth);
        int mWeekNumber = DateUtils.getFirstDayWeek(mSelYear, mSelMonth);
        int count = (int) Math.ceil((mMonthDays + mWeekNumber - 1) / 7f);
        if (count < 6) {
            invalidate();
        } else {
            requestLayout();
        }
    }

    /**
     * 获取当前展示的年和月份
     *
     * @return 格式：2016-06
     */
    public String getDate() {
        String data;
        if ((mSelMonth + 1) < 10) {
            data = mSelYear + "," + SolarUtil.matchingMonth(mSelMonth + 1) + "月";
        } else {
            data = mSelYear + "," + SolarUtil.matchingMonth(mSelMonth + 1) + "月";
        }
        return data;
    }

    /**
     * 获取当前展示的日期
     *
     * @return 格式：20160606
     */
    private String getSelData(int year, int month, int date) {
        String monty, day;
        month = (month + 1);
        // 判断月份是否有非0情况
        if ((month) < 10) {
            monty = "0" + month;
        } else {
            monty = String.valueOf(month);
        }

        // 判断天数是否有非0情况
        if ((date) < 10) {
            day = "0" + (date);
        } else {
            day = String.valueOf(date);
        }
        return year + monty + day;
    }

    /**
     * 获取当前的日期
     *
     * @return 20170608
     */
    private String getCurrentDate() {
        String result = "" + mCurYear;
        int month = mCurMonth + 1;
        if ((month) < 10) {
            result += "0" + month;
        } else {
            result += String.valueOf(month);
        }
        if ((mCurDate) < 10) {
            result += "0" + (mCurDate);
        } else {
            result += String.valueOf(mCurDate);
        }
        return result;
    }

    private OnClickListener mListener;

    @Override
    public void applySkin() {
        try {
            circleBgColor = SkinCompatResources.getInstance().getColorStateList(circleBgColorId).getDefaultColor();
            weekendColor = SkinCompatResources.getInstance().getColorStateList(weekendColorId).getDefaultColor();
            workDayColor = SkinCompatResources.getInstance().getColorStateList(workDayColorId).getDefaultColor();
            splitLineColor = SkinCompatResources.getInstance().getColorStateList(splitLineId).getDefaultColor();
            selectedTextColor = SkinCompatResources.getInstance().getColorStateList(selectedTextColorId).getDefaultColor();
            mCirclePaint.setColor(circleBgColor);
        } catch (Exception e) {
        }
    }

    public interface OnClickListener {
        void onClickDateListener(int year, int month, int day, String date);
    }

    /**
     * 设置点击回调
     */
    public void setOnClickDate(OnClickListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
