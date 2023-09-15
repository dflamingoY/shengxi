package org.xiaoxingqi.shengxi.wedgit.actionTabar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.wedgit.BaseLayout;


/**
 * Created by yzm on 2017/10/30.
 */

public class ActionBarView extends BaseLayout implements View.OnClickListener {
    private Context mContext;
    private ImageView mHomeButton;
    private CustomSeletcor mTab01;
    private CustomSeletcor mTab02;
    private CustomSeletcor mTab03;
    private CustomSeletcor mTab04;
    private boolean mIsFrist = true;

    //private Paint mPaint;
    public ActionBarView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ActionBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.home_tab_layout;
    }

    private void initView() {
        setClipChildren(false);
        mHomeButton = findViewById(R.id.Iv_HomeButton);
        mTab01 = findViewById(R.id.tab_01);
        mTab02 = findViewById(R.id.tab_02);
        mTab03 = findViewById(R.id.tab_03);
        mTab04 = findViewById(R.id.tab_04);

        mHomeButton.setOnClickListener(this);
        mTab01.setOnClickListener(this);
        mTab02.setOnClickListener(this);
        mTab03.setOnClickListener(this);
        mTab04.setOnClickListener(this);

        mTab01.setOnButtonClickListener(buttonClickListener);
        mTab02.setOnButtonClickListener(buttonClickListener);
        mTab03.setOnButtonClickListener(buttonClickListener);
        mTab04.setOnButtonClickListener(buttonClickListener);

        mTab01.setDefaultView(R.mipmap.icon_home_normal, mContext.getString(R.string.string_home), false);
        mTab01.setSelectView(R.mipmap.icon_home_selected, getResources().getColor(R.color.colorIndecators));

        mTab02.setDefaultView(R.mipmap.icon_tab_2, mContext.getString(R.string.string_echoes), false);
        mTab02.setSelectView(R.mipmap.icon_tab_2_select, getResources().getColor(R.color.colorIndecators));

        mTab03.setDefaultView(R.mipmap.icon_tab_3, mContext.getString(R.string.string_listen), false);
        mTab03.setSelectView(R.mipmap.icon_tab_3_select, getResources().getColor(R.color.colorIndecators));

        mTab04.setDefaultView(R.mipmap.icon_tab_4, mContext.getString(R.string.string_me), false);
        mTab04.setSelectView(R.mipmap.icon_tab_4_select, getResources().getColor(R.color.colorIndecators));
        mHomeButton.setOnLongClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.tabVlick(v);
            }
            return false;
        });
    }

    CustomSeletcor.OnButtonClickListener buttonClickListener = new CustomSeletcor.OnButtonClickListener() {
        @Override
        public void onDoubleClicl(View view) {
            if (mClickListener != null) {
                mClickListener.doubleClick(view);
            }
        }
    };


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mIsFrist) {
            mTab01.setSelected();
            mIsFrist = false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof CustomSeletcor) {
            if (mClickListener != null) {
                boolean tabVlick = mClickListener.tabVlick(view);
                if (tabVlick) {
                    selectedTitle((CustomSeletcor) view);
                }
            }
        } else if (view instanceof ImageView) {
            if (mClickListener != null) {
                mClickListener.tabVlick(view);
            }
        }
    }


    private OnTabOnClickListener mClickListener;

    public void setOnTabClickListener(OnTabOnClickListener clickListener) {

        this.mClickListener = clickListener;
    }

    private void selectedTitle(CustomSeletcor view) {
        clearAll();
        view.setSelected();
    }

    private void clearAll() {
        mTab04.clearSelect();
        mTab03.clearSelect();
        mTab02.clearSelect();
        mTab01.clearSelect();
    }


    public interface OnTabOnClickListener {
        boolean tabVlick(View view);

        void doubleClick(View view);
    }

    /**
     * 设置标记是否可见
     *
     * @param isFlag
     */
    public void setFlag(boolean isFlag, int child) {
        switch (child) {
            case 0:
                if (mTab01 != null) {
                    mTab01.setButtonFlag(isFlag);
                }
                break;
            case 1:
                if (mTab02 != null) {
                    mTab02.setButtonFlag(isFlag);
                }
                break;
            case 2:
                if (mTab03 != null) {
                    mTab03.setButtonFlag(isFlag);
                }
                break;
            case 3:
                if (mTab04 != null) {
                    mTab04.setButtonFlag(isFlag);
                }
                break;
        }
    }


    public void setCurrentSelect(int child) {
        if (child == 0) {
            selectedTitle(mTab01);
        } else if (child == 1) {
            selectedTitle(mTab02);
        }
    }

}
