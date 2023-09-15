package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatEditText;

/**
 * Created by yzm on 2018/3/19.
 * 边框宽度 44dp
 * 边框左右间距8dp
 */

public class BorderEditText extends SkinCompatEditText {

    private Paint mPaint;
    private Context mContext;
    private int count = 6;
    private int width = 44;//默认单位dp
    private int margin = 8;
    private int radius = 10;
    private int borderColorID = 0;
    private int borderColor = 0xffeeeeee;

    public BorderEditText(Context context) {
        this(context, null);
    }

    public BorderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BorderEditText);
        count = array.getInt(R.styleable.BorderEditText_border_Count, 6);
        width = array.getInt(R.styleable.BorderEditText_border_Width, 44);
        margin = array.getInt(R.styleable.BorderEditText_border_Margin, 8);
        radius = array.getInt(R.styleable.BorderEditText_border_radius, radius);
        if (array.hasValue(R.styleable.BorderEditText_border_color)) {
            borderColorID = array.getResourceId(R.styleable.BorderEditText_border_color, 0);
        }
        array.recycle();
        mContext = context;
        SkinCompatResources.init(getContext());
        initView();
        applySkin();
    }

    private void initView() {
        width = AppTools.dp2px(mContext, width);
        margin = AppTools.dp2px(mContext, margin);
        /**
         * 这个可以自定义属性 放在资源文件中设置 懒的加了
         */
        radius = AppTools.dp2px(mContext, radius);
        mPaint = new Paint();
        mPaint.setColor(borderColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int _width = width * count + (count - 1) * margin;
        super.onMeasure(MeasureSpec.makeMeasureSpec(_width, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(borderColor);
        /**
         * 此处可以自己绘制 想要的效果图
         */
        for (int a = 0; a < count; a++) {
            canvas.drawRoundRect(a * width + a * margin, 0, (a + 1) * width + a * margin, getMeasuredHeight(), radius, radius, mPaint);
        }
        mPaint.setColor(getTextColors().getDefaultColor());
        mPaint.setTextSize(getTextSize());
        mPaint.setStyle(Paint.Style.FILL);
        /**
         * 计算文字的宽高
         */
        Rect textRect = new Rect();
        float v = mPaint.measureText("9");
        mPaint.getTextBounds("9", 0, 1, textRect);
        String trim = getText().toString().trim();
        if (!TextUtils.isEmpty(trim)) {
            for (int a = 0; a < trim.length(); a++) {
                canvas.drawText(String.valueOf(trim.charAt(a)), a * width + a * margin + width / 2 - v / 2, getMeasuredHeight() / 2 + textRect.height() / 2, mPaint);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        invalidate();
    }

    @Override
    public void applySkin() {
        super.applySkin();
        try {
            ColorStateList colorButton = SkinCompatResources.getInstance().getColorStateList(borderColorID);
            borderColor = colorButton.getDefaultColor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
