package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import skin.support.widget.SkinCompatTextView;


public class TagTextView extends SkinCompatTextView {
    public TagTextView(Context context) {
        super(context);
    }

    public TagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(String data, String matcherChar) {
        try {
            Pattern pattern = Pattern.compile(matcherChar);
            Matcher matcher = pattern.matcher(data);
            SpannableString ssb = new SpannableString(data);
            while (matcher.find()) {
                String urlPath = matcher.group();
                if (urlPath != null) {
                    if (urlPath != null) {
                        int start = matcher.start();
                        int endUrl = start + urlPath.length();
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF46CDCF"));
                        ssb.setSpan(colorSpan, start, endUrl, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }
            setText(ssb);
        } catch (Exception e) {
            e.printStackTrace();
            setText(matcherChar);
        }
    }


}
