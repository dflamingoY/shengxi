package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.xiaoxingqi.shengxi.R;

public class SelectorScoreMovieView extends BaseLayout {

    private View mLienarGood;
    private View mLinearNormal;
    private View mLinearBad;

    public SelectorScoreMovieView(@NonNull Context context) {
        this(context, null, 0);
    }

    public SelectorScoreMovieView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectorScoreMovieView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLienarGood = findViewById(R.id.linearGood);
        mLinearNormal = findViewById(R.id.linearNormal);
        mLinearBad = findViewById(R.id.linearBad);
        mLienarGood.setOnClickListener(view -> {
            mLienarGood.setSelected(true);
            mLinearNormal.setSelected(false);
            mLinearBad.setSelected(false);
            if (mOnTabClick != null) {
                mOnTabClick.onClick();
            }
        });
        mLinearNormal.setOnClickListener(view -> {
            mLienarGood.setSelected(false);
            mLinearNormal.setSelected(true);
            mLinearBad.setSelected(false);
            if (mOnTabClick != null) {
                mOnTabClick.onClick();
            }
        });
        mLinearBad.setOnClickListener(view -> {
            mLienarGood.setSelected(false);
            mLinearNormal.setSelected(false);
            mLinearBad.setSelected(true);
            if (mOnTabClick != null) {
                mOnTabClick.onClick();
            }
        });
    }

    public String getScore() {
        if (mLienarGood.isSelected()) {
            return "100";
        }
        if (mLinearNormal.isSelected()) {
            return "50";
        }
        if (mLinearBad.isSelected()) {
            return "0";
        }
        return null;
    }

    public interface OnTabClick {
        void onClick();
    }

    private OnTabClick mOnTabClick;

    public void setOnTabClick(OnTabClick click) {
        mOnTabClick = click;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_score_selector;
    }
}
