package org.xiaoxingqi.shengxi.wedgit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.xiaoxingqi.shengxi.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


/**
 * Created by Kevin on 2016/6/13.
 */
@SuppressLint("AppCompatCustomView")
public class CustomCheckImageView extends ImageView implements View.OnClickListener, SkinCompatSupportable {
    private boolean mIsSelected = false;
    private int mResIdOn = R.mipmap.icon_selector_check;
    private int mResIdOff = R.mipmap.icon_selector_normal;
    private Drawable resOn;
    private Drawable resOff;

    public CustomCheckImageView(Context context) {
        super(context);
        initView(null);
    }

    public CustomCheckImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public CustomCheckImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCheckImageView);
            if (typedArray.hasValue(R.styleable.CustomCheckImageView_checkState)) {
                mIsSelected = typedArray.getBoolean(R.styleable.CustomCheckImageView_checkState, false);
            }
            if (typedArray.hasValue(R.styleable.CustomCheckImageView_checkOn)) {
                mResIdOn = typedArray.getResourceId(R.styleable.CustomCheckImageView_checkOn, 0);
                resOn = typedArray.getDrawable(R.styleable.CustomCheckImageView_checkOn);
            }
            if (typedArray.hasValue(R.styleable.CustomCheckImageView_checkOff)) {
                mResIdOff = typedArray.getResourceId(R.styleable.CustomCheckImageView_checkOff, 0);
                resOff = typedArray.getDrawable(R.styleable.CustomCheckImageView_checkOff);
            }
            typedArray.recycle();
        }
        if (resOn == null) {
            resOn = getContext().getResources().getDrawable(mResIdOn);
        }
        if (resOff == null) {
            resOff = getContext().getResources().getDrawable(mResIdOff);
        }
        setSelected(mIsSelected);
        SkinCompatResources.init(getContext());
        applySkin();
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
        this.setImageDrawable(mIsSelected ? resOn : resOff);
    }

    @Override
    public void onClick(View v) {
        setSelected(!mIsSelected);
        if (this.mOnStateChangeListener != null)
            this.mOnStateChangeListener.onStateChange(mIsSelected);
    }

    private OnStateChangeListener mOnStateChangeListener;

    public void setOnStateChangeListener(OnStateChangeListener mOnStateChangeListener) {
        this.mOnStateChangeListener = mOnStateChangeListener;
    }

    @Override
    public void applySkin() {
        //主题变更
        Drawable drawOn = SkinCompatResources.getInstance().getMipmap(mResIdOn);
        Drawable drawOff = SkinCompatResources.getInstance().getMipmap(mResIdOff);
        if (drawOn != null) {
            resOn = drawOn;
        }
        if (drawOff != null) {
            resOff = drawOff;
        }
        setSelected(isSelected());
    }

    public interface OnStateChangeListener {
        void onStateChange(boolean selected);
    }
}
