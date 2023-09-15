package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;

import java.lang.reflect.Field;

public class StrokeTextView extends AppCompatTextView {
    private float spacing = Spacing.NORMAL;
    private CharSequence originalText = "";
    private Paint m_TextPaint;
    private float strokewidth;

    public StrokeTextView(Context context) {
        this(context, null, 0);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        strokewidth = array.getDimension(R.styleable.StrokeTextView_strokeWidth, 2);
        array.recycle();
        init();
    }

    private void init() {
        m_TextPaint = getPaint();
        m_TextPaint.setDither(true);
        m_TextPaint.setAntiAlias(true);
        m_TextPaint.setFilterBitmap(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setTextColorUseReflection(Color.parseColor("#FF151662"));
        m_TextPaint.setStrokeWidth(strokewidth);  // 描边宽度
        m_TextPaint.setStyle(Paint.Style.FILL_AND_STROKE); //描边种类
        m_TextPaint.setFakeBoldText(true); // 外层text采用粗体
        m_TextPaint.setShadowLayer(10, 0, 0, 0); //字体的阴影效果，可以忽略
        super.onDraw(canvas);
        // 描内层，恢复原先的画笔
        setTextColorUseReflection(Color.parseColor("#FFFEEF3D"));
        m_TextPaint.setStrokeWidth(0);
        m_TextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        m_TextPaint.setFakeBoldText(false);
        m_TextPaint.setShadowLayer(0, 0, 0, 0);
        super.onDraw(canvas);
    }

    private void setTextColorUseReflection(int color) {
        Field textColorField;
        try {
            textColorField = TextView.class.getDeclaredField("mCurTextColor");
            textColorField.setAccessible(true);
            textColorField.set(this, color);
            textColorField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        m_TextPaint.setColor(color);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        applySpacing();
    }

    @Override
    public CharSequence getText() {
        return originalText;
    }

    private void applySpacing() {
        if (this == null || this.originalText == null) return;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < originalText.length(); i++) {
            builder.append(originalText.charAt(i));
            if (i + 1 < originalText.length()) {
                //如果前后都是英文，则不添加空格，防止英文空格太大
                if (isEnglish(originalText.charAt(i) + "") && isEnglish(originalText.charAt(i + 1) + "")) {
                } else {
                    // \u00A0 不间断空格 碰见文字追加空格
                    builder.append("\u00A0");
                }
            }
        }
        // 通过SpannableString类，去设置空格
        SpannableString finalText = new SpannableString(builder.toString());
        // 如果当前TextView内容长度大于1，则进行空格添加
        if (builder.toString().length() > 1) {
            for (int i = 1; i < builder.toString().length(); i += 2) {
                // ScaleXSpan 基于x轴缩放  按照x轴等比例进行缩放 通过字间距+1除以10进行等比缩放
                finalText.setSpan(new ScaleXSpan((spacing + 1) / 10), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
    }

    /**
     * 判断是否是英语
     */
    public static boolean isEnglish(String charaString) {
        return charaString.matches("^[a-zA-Z]*");
    }

    public class Spacing {
        public final static float NORMAL = 0;
    }
}
