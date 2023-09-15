package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.impl.ProgressBindTrackStateListener;
import org.xiaoxingqi.shengxi.model.BaseAnimBean;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.SPUtils;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

/**
 * 最長185 -80  105
 * 最短96  -80 16
 * 35dp 播放暂停的宽度
 * 10dp 文字间距
 * 35dp 文字宽度
 * 拖动过程中 文本显示,实时更新进度
 */
public class VoiceProgress extends RelativeLayout implements SkinCompatSupportable {
    public static boolean isHideText = false;

    private ImageView mIvStatus;
    private MarqueeTextView mLengthView;
    private TextView mTvTime;
    private BaseAnimBean bean;
    private ImageView reStart;
    private Drawable mDrawable;
    private int reStartLeftId;
    private ViewSeekProgress seekProgress;
    private OnClickListener onClickListener;

    public VoiceProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VoiceProgress);
        mDrawable = array.getDrawable(R.styleable.VoiceProgress_reStartLeftId);
        reStartLeftId = array.getResourceId(R.styleable.VoiceProgress_reStartLeftId, 0);
        array.recycle();
        applySkin();
        SkinCompatResources.init(context);
        init(context);
    }

    void init(Context context) {
        View.inflate(context, R.layout.layout_voice_anim, this);
        mIvStatus = findViewById(R.id.play);
        mLengthView = findViewById(R.id.viewlength);
        mTvTime = findViewById(R.id.tv_Time);
        reStart = new ImageView(context);
        reStart.setId(R.id.iv_click_reStart);
        seekProgress = findViewById(R.id.viewSeekProgress);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(AppTools.dp2px(context, 24), AppTools.dp2px(context, 22));
        params.addRule(RelativeLayout.RIGHT_OF, R.id.linearVoiceContent);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMarginStart(AppTools.dp2px(context, 10));
        if (null != mDrawable) {
            reStart.setImageDrawable(mDrawable);
            reStart.setLayoutParams(params);
            reStart.setVisibility(GONE);
            addView(reStart);
        }
        seekProgress.setOnTrackStateListener((progress) -> {//开始拖动
            if (mLengthView.getVisibility() != VISIBLE) {
                mLengthView.setVisibility(VISIBLE);
            }
            int length = Integer.parseInt(bean.getVoice_len());
            mLengthView.update((int) (length * 1000 * progress));
            try {
                mTvTime.setText(AppTools.parseTime2Str((long) (length * 1000 * (1 - progress) + 0.5f)));
            } catch (NumberFormatException e) {
                mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            }
        });
        mIvStatus.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(view);
            }
        });
        seekProgress.setOnClickListener(view -> {
            //为播放时,点击播放
            if (onClickListener != null) {
                if (bean != null && !bean.isPlaying())
                    onClickListener.onClick(view);
            }
        });
    }

    public void setData(BaseAnimBean bean) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLengthView.getLayoutParams();
        seekProgress.setIsMove(false);
        seekProgress.setProgress(0);
        try {
            if (!TextUtils.isEmpty(bean.getRecognition_content()) && !isHideText)
                mLengthView.setText(bean.getRecognition_content());
            else {
                mLengthView.setText("");
            }
            this.bean = bean;
            int length = Integer.parseInt(bean.getVoice_len());
            if (length > 120) {
                length = 120;
            }
            int width = (int) (AppTools.dp2px(getContext(), 105 - 16) * (length * 1f / 120) + 0.5f) + AppTools.dp2px(getContext(), 50);
            params.width = width;
            mLengthView.setLayoutParams(params);
            mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            mLengthView.calcDx(length * 1000);
            if (!bean.isPlaying()) {
                finish();
            }
        } catch (NumberFormatException e) {
            mTvTime.setText("0:00");
            int width = AppTools.dp2px(getContext(), 16);
            params.width = width;
            mLengthView.setText("");
        }
    }

    public BaseAnimBean getData() {
        return bean;
    }

    public void updateProgress(int progress) {
        if (bean.isPlaying()) {
            mLengthView.update(progress);
            if (mLengthView.getVisibility() != View.VISIBLE) {
                mLengthView.setVisibility(View.VISIBLE);
            }
            mIvStatus.setSelected(true);
            reStart.setVisibility(VISIBLE);
            try {
                int length = Integer.parseInt(bean.getVoice_len());
                seekProgress.setIsMove(true);
                seekProgress.setProgress((int) (progress * 1f / length));
                if (length * 1000 - progress < 0) {
                    mTvTime.setText(AppTools.parseTime2Str(0));
                    //                    finish();
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000 - progress));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            seekProgress.setIsMove(false);
            if (mLengthView.getScrollX() != 0) {//初始化位置
                mLengthView.setScrollX(0);
            }
            //            if (mLengthView.getVisibility() != View.INVISIBLE) {
            //                mLengthView.setVisibility(View.INVISIBLE);
            //            }
            seekProgress.setProgress(0);
            int length = Integer.parseInt(bean.getVoice_len());
            mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            mIvStatus.setSelected(false);
            reStart.setVisibility(GONE);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mIvStatus.setSelected(selected);
    }

    public void finish() {
        mIvStatus.setSelected(false);
        //        if (mLengthView.getVisibility() != INVISIBLE) {
        //            mLengthView.setVisibility(INVISIBLE);
        //        }
        try {
            seekProgress.setIsMove(false);
            seekProgress.setProgress(0);
            if (bean != null) {
                int length = Integer.parseInt(bean.getVoice_len());
                if (bean.getPasuePosition() > 0) {
                    //                    reStart.setVisibility(VISIBLE);
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000 - bean.getPasuePosition()));
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                }
                reStart.setVisibility(GONE);
            }
            if (mLengthView.getScrollX() != 0) {//初始化位置
                mLengthView.setScrollX(0);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            reStart.setVisibility(GONE);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    @Override
    public void applySkin() {
        try {
            mDrawable = SkinCompatResources.getInstance().getMipmap(reStartLeftId);
            if (mDrawable != null) {
                reStart.setImageDrawable(mDrawable);
            }
        } catch (Exception e) {
        }
    }
}
