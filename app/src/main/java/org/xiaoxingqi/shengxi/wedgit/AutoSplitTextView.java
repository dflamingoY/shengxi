package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

public class AutoSplitTextView extends AppCompatTextView implements SkinCompatSupportable {
    private boolean mEnabled = true;
    private int colorResourceId = 0;

    public AutoSplitTextView(Context context) {
        this(context, null, 0);
    }

    public AutoSplitTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSplitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SkinTextAppearance);
        if (array.hasValue(R.styleable.SkinTextAppearance_android_textColor)) {
            colorResourceId = array.getResourceId(R.styleable.SkinTextAppearance_android_textColor, 0);
        }
        array.recycle();
        SkinCompatResources.init(getContext());
        applySkin();
    }

    private int tvWidth = 0;

    private String autoSplitText(String text) {
        final String rawText = text; //原始文本
        final Paint tvPaint = getPaint(); //paint，包含字体等信息
        //将原始文本按行拆分
        String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
        StringBuilder sbNewText = new StringBuilder();
        for (String rawTextLine : rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行宽度在控件可用宽度之内，就不处理了
                sbNewText.append(rawTextLine);
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                float lineWidth = 0;
                for (int cnt = 0; cnt < rawTextLine.length(); ++cnt) {
                    char ch = rawTextLine.charAt(cnt);
                    boolean isemojiCharacter = isEmojiCharacter(ch);//是否是表情
                    float charLength = tvPaint.measureText(String.valueOf(ch));
                    if (isemojiCharacter) {
                        float measureText = tvPaint.measureText(String.valueOf(ch) + rawTextLine.charAt(cnt + 1));
                        lineWidth += measureText;
                    } else {
                        lineWidth += charLength;
                    }
                    //                    lineWidth += tvPaint.measureText(String.valueOf(ch));
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch);
                        if (isemojiCharacter) {
                            ++cnt;
                            if (cnt < rawTextLine.length()) {
                                sbNewText.append(rawTextLine.charAt(cnt));
                            }
                        }
                    } else {
                        sbNewText.append("\n");
                        lineWidth = 0;
                        --cnt;
                    }
                }
            }
            sbNewText.append("\n");
        }

        //把结尾多余的\n去掉
        if (!rawText.endsWith("\n")) {
            sbNewText.deleteCharAt(sbNewText.length() - 1);
        }
        return sbNewText.toString();
    }

    private boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }

    public void setString(int width, String text) {
        tvWidth = width;
        setText(autoSplitText(text));
    }

    @Override
    public void applySkin() {
        try {
            ColorStateList color1 = SkinCompatResources.getInstance().getColorStateList(colorResourceId);
            setTextColor(color1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
