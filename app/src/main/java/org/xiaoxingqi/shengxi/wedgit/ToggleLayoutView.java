package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;


/**
 * Created by yzm on 2018/3/16.
 */

public class ToggleLayoutView extends BaseLayout {

    private Context mContext;
    private String mName;
    private boolean mState;
    private int color = Color.parseColor("#c4c9e2");
    private SwitchButton mToggleButton;
    private TextView mTvName;
    private TextView tvSecondTitle;
    private String secondTitle;
    private View clickView;

    public ToggleLayoutView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ToggleLayoutView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleLayoutView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ToggleLayoutView);
        mName = array.getString(R.styleable.ToggleLayoutView_typeName);
        mState = array.getBoolean(R.styleable.ToggleLayoutView_toggleState, false);
        color = array.getColor(R.styleable.ToggleLayoutView_text_color, color);
        secondTitle = array.getString(R.styleable.ToggleLayoutView_toggle_SecondTitle);
        array.recycle();
        mContext = context;
        initView();
    }

    private void initView() {
        mTvName = findViewById(R.id.tv_TypeName);
        mToggleButton = findViewById(R.id.toggle_Button);
        tvSecondTitle = findViewById(R.id.tv_Private);
        clickView = findViewById(R.id.viewClick);
        //        mToggleButton.setEnabled(false);
        //        mTvName.setTextColor(color);
        mToggleButton.setClickable(false);
        mTvName.setText(mName);
        if (mState) {
            mToggleButton.setChecked(mState);
        }
        if (!TextUtils.isEmpty(secondTitle)) {
            tvSecondTitle.setText(secondTitle);
        }
        mToggleButton.setOnCheckedChangeListener((view, isChecked) -> {
            if (null != toggleChangeListener) {
                toggleChangeListener.onChange();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_toggle;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        //        super.setOnClickListener(l);
        if (l != null) {
            clickView.setOnClickListener(l);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mToggleButton.setChecked(selected);
    }

    @Override
    public boolean isSelected() {
        return mToggleButton.isChecked();
    }

    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTvName.setText(text);
        }
    }

    public boolean getToggleState() {
        return mToggleButton.isChecked();
    }

    public void setToggle() {
        mToggleButton.setChecked(true);
    }

    private OnToggleChangeListener toggleChangeListener;

    public void setOnToggleListener(OnToggleChangeListener listener) {
        toggleChangeListener = listener;
    }

    public interface OnToggleChangeListener {
        void onChange();
    }

}
