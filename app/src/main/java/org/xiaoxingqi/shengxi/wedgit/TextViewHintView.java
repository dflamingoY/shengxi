package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;

/**
 * Created by yzm on 2018/2/6.
 */

public class TextViewHintView extends BaseLayout {
    private Context mContext;
    private TextView mTvName;
    private View mViewFlag;


    public TextViewHintView(@NonNull Context context) {
        this(context, null, 0);
    }

    public TextViewHintView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TextViewHintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }


    private void initView() {
        mTvName = findViewById(R.id.tv_Name);
        mViewFlag = findViewById(R.id.view_Flag);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_hint_slide;
    }

    public void setText(CharSequence text) {
        mTvName.setText(text);
    }

    public void setFlagVisible(boolean isVisible) {
        mViewFlag.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mTvName.setSelected(selected);
    }

    public void setTextColor(int defalueColor) {
        mTvName.setTextColor(defalueColor);
    }
}
