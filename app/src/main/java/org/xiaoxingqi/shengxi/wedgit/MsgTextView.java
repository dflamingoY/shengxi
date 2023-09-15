package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;


import org.xiaoxingqi.shengxi.model.SelectionBean;
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity;
import org.xiaoxingqi.shengxi.modules.listen.WebViewActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xiaoxingqi.shengxi.utils.AppTools.ALL;

/**
 * Created by yzm on 2017/12/5.
 * <p>
 * 消息对话 的textView
 */

public class MsgTextView extends AppCompatTextView {
    private Context mContext;
    private List<SelectionBean> selects = new ArrayList<>();

    public MsgTextView(Context context) {
        this(context, null, 0);
    }

    public MsgTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsgTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setTextData(String text) {
        selects.clear();
        text = text.replace("\n", "<br/>");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setTextIsSelectable(true);
        }
        try {
            setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString ssb = new SpannableString(Html.fromHtml(text));
            Pattern pattern = Pattern.compile(ALL);
            Matcher matcher = pattern.matcher(ssb);
            while (matcher.find()) {
                String topic = matcher.group(1);
                String urlPath = matcher.group(2);
                if (topic != null) {
                    int start = matcher.start(1);
                    int endSpoiler = start + topic.length();
                    selects.add(new SelectionBean(topic, topic, start, endSpoiler, 3));
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF46CDCF"));
                    ssb.setSpan(colorSpan, start, endSpoiler, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                if (urlPath != null) {
                    if (urlPath != null) {
                        int start = matcher.start();
                        int endUrl = start + urlPath.length();
                        selects.add(new SelectionBean(urlPath, start, endUrl, 4));
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF46CDCF"));
                        ssb.setSpan(colorSpan, start, endUrl, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }
            setText(ssb);
            BackgroundColorSpan span = new BackgroundColorSpan(Color.parseColor("#31000000"));
            int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            setOnTouchListener(new OnTouchListener() {

                int downX, downY;
                int id;
                SelectionBean downSection = null;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = MotionEventCompat.getActionMasked(event);
                    Layout layout = getLayout();
                    if (layout == null) {
                        return false;
                    }
                    int line = 0;
                    int index = 0;

                    switch (action) {
                        case MotionEvent.ACTION_DOWN://TODO 最后一行点击问题 网址链接
                            int actionIndex = event.getActionIndex();
                            id = event.getPointerId(actionIndex);
                            downX = (int) event.getX(actionIndex);
                            downY = (int) event.getY(actionIndex);
                            line = layout.getLineForVertical(getScrollY() + (int) event.getY());//计算出当前点击的第几行
                            int lineCount = layout.getLineCount();
                            index = layout.getOffsetForHorizontal(line, (int) event.getX());
                            int lastRight = (int) layout.getLineRight(line);
                            if (lastRight < downX) {  //文字最后为话题时，如果点击在最后一行话题之后，也会造成话题被选中效果
                                return false;
                            }
                            for (SelectionBean section : selects) {
                                if (index >= section.getStart() && index <= section.getEnd()) {
                                    ssb.setSpan(span, section.getStart(), section.getEnd(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    downSection = section;
                                    setText(ssb);
                                    getParent().requestDisallowInterceptTouchEvent(true);//不允许父view拦截
                                    return true;
                                }
                            }

                            return false;
                        case MotionEvent.ACTION_MOVE:
                            int indexMove = event.findPointerIndex(id);
                            int currentX = (int) event.getX(indexMove);
                            int currentY = (int) event.getY(indexMove);
                            if (Math.abs(currentX - downX) < slop && Math.abs(currentY - downY) < slop) {
                                if (downSection == null) {
                                    getParent().requestDisallowInterceptTouchEvent(false);//允许父view拦截
                                    return false;
                                }
                                break;
                            }
                            downSection = null;
                            getParent().requestDisallowInterceptTouchEvent(false);//允许父view拦截
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            int indexUp = event.findPointerIndex(id);
                            ssb.removeSpan(span);
                            setText(ssb);
                            int upX = (int) event.getX(indexUp);
                            int upY = (int) event.getY(indexUp);
                            if (Math.abs(upX - downX) < slop && Math.abs(upY - downY) < slop) {
                                try {
                                    if (null != downSection) {
                                        if (downSection.getType() == 3) {//跳转搜索
                                            try {
                                                mContext.startActivity(new Intent(mContext, TopicResultActivity.class)
                                                        .putExtra("tag", downSection.getName().replace("#", ""))
                                                        .putExtra("isTopic", true)
                                                );
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else if (downSection != null) {
                                            if (downSection.getType() == 4) {
                                                mContext.startActivity(new Intent(mContext, WebViewActivity.class).putExtra("url", downSection.getName()));
                                            }
                                            downSection = null;
                                        } else {
                                            return false;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            } else {
                                downSection = null;
                                return false;
                            }
                            break;
                    }
                    return true;
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
            setText(text);
        }
    }
}
