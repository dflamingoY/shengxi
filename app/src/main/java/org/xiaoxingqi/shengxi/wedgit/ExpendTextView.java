package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;


/**
 * Created by yzm on 2017/11/3.
 * 最多显示两行
 */

public class ExpendTextView extends BaseLayout {
    private TextView mTvShowText;
    private ImageView mTvExpend;
    private boolean isCollsped = true;

    public ExpendTextView(Context context) {
        this(context, null);
    }

    public ExpendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mTvShowText = findViewById(R.id.tv_ShowText);
        mTvExpend = findViewById(R.id.tv_Expend);
        mTvExpend.setOnClickListener(v -> {
            if (isCollsped) {//折叠状态
                mTvShowText.setMaxLines(Integer.MAX_VALUE);
                mTvExpend.setRotation(180);
                isCollsped = false;
            } else {
                mTvShowText.setMaxLines(3);
                isCollsped = true;
                mTvExpend.setRotation(0);
            }
            mTvShowText.requestLayout();
            if (mListener != null) {
                mListener.expend(isCollsped);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_expend_textview;
    }

    public void setTvShowText(String text) {
        mTvShowText.setText(text);
        mTvShowText.setMaxLines(3);
        mTvExpend.setRotation(0);
        isCollsped = true;
    }

    private OnExpendListener mListener;

    public void stOnExpendListener(OnExpendListener listener) {
        mListener = listener;
    }

    public interface OnExpendListener {
        void expend(boolean isExpend);
    }

}
