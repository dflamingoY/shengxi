package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;


/**
 * Created by yzm on 2017/11/22.
 */

public class ViewScoreSetting extends BaseLayout {

    private TextView mMTvTypeName;
    private ImageView mMIvSelected;
    private String mString;
    private Context mContext;
    private boolean mSelected;
    private int textSize = 143;
    private int color;

    public ViewScoreSetting(@NonNull Context context) {
        this(context, null);
    }

    public ViewScoreSetting(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewMoreGroupView);
        mString = array.getString(R.styleable.ViewMoreGroupView_title_name);
        mSelected = array.getBoolean(R.styleable.ViewMoreGroupView_title_selected, false);
        textSize = array.getInt(R.styleable.ViewMoreGroupView_title_size, 13);
        color = array.getColor(R.styleable.ViewMoreGroupView_second_color, 0xff999999);
        array.recycle();
        mContext = context;
        initView();
    }

    private void initView() {
        mMTvTypeName = findViewById(R.id.tv_TypeName);
        mMIvSelected = findViewById(R.id.iv_Selected);
        if (mString != null) {
            mMTvTypeName.setText(mString);
        }
        mMTvTypeName.setTextSize(textSize);
        //        mMTvTypeName.setTextColor(color);
        if (mSelected) {
            //            mMTvTypeName.setTextColor(getResources().getColor(R.color.colorNormalIndecator));
            mMIvSelected.setVisibility(VISIBLE);
        }
    }

    public void changeStatus(boolean isSelected) {
        if (isSelected) {
            mMIvSelected.setVisibility(VISIBLE);
        } else {
            mMIvSelected.setVisibility(GONE);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            //            mMTvTypeName.setTextColor(getResources().getColor(R.color.colorNormalIndecator));
            mMIvSelected.setVisibility(VISIBLE);
        } else {
            //            mMTvTypeName.setTextColor(getResources().getColor(R.color.colorTextGray));
            mMIvSelected.setVisibility(GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_user_score_setting;
    }

    public void setTitle(String title) {
        mMTvTypeName.setText(title);
    }
}
