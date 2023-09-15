package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.model.BaseAnimBean;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

/**
 * Created by yzm on 2018/3/7.
 *
 * @持续的动画效果,时间倒计时计算 最大长度183 最低长度 32  待进度条的进度动画
 */

public class VoiceAnimaProgress extends RelativeLayout implements SkinCompatSupportable {
    private Context mContext;
    private BaseAnimBean mBean;
    private PlayWifiView mIvPlay;
    private View mSpaceVie;
    private TextView mTvTime;
    private ImageView reStart;
    private Drawable mDrawable;
    private int reStartLeftId;
    private ViewSeekProgress seekProgress;

    public VoiceAnimaProgress(@NonNull Context context) {
        this(context, null, 0);
    }

    public VoiceAnimaProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceAnimaProgress(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VoiceProgress);
        mDrawable = array.getDrawable(R.styleable.VoiceProgress_reStartLeftId);
        reStartLeftId = array.getResourceId(R.styleable.VoiceProgress_reStartLeftId, 0);
        array.recycle();

        applySkin();
        SkinCompatResources.init(context);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.layout_voice_wife, this);
        mIvPlay = findViewById(R.id.play);
        mSpaceVie = findViewById(R.id.progress);
        mTvTime = findViewById(R.id.tv_Time);
        seekProgress = findViewById(R.id.viewSeekProgress);
        reStart = new ImageView(context);
        reStart.setId(R.id.iv_click_reStart);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(AppTools.dp2px(context, 24), AppTools.dp2px(context, 22));
        params.addRule(RelativeLayout.RIGHT_OF, R.id.relativeVoiceContainer);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMarginStart(AppTools.dp2px(context, 10));
        if (null != mDrawable)
            reStart.setImageDrawable(mDrawable);
        reStart.setLayoutParams(params);
        reStart.setVisibility(GONE);
        addView(reStart);
    }

    public void setData(BaseAnimBean bean) {
        mBean = bean;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSpaceVie.getLayoutParams();
        seekProgress.setProgress(0);
        seekProgress.setIsMove(false);
        try {
            int length = Integer.parseInt(bean.getVoice_len());
            int width = (int) (AppTools.dp2px(mContext, 100 - 50) * (length * 1f / 120) + 0.5f) + AppTools.dp2px(mContext, 32);
            params.width = width;
            mSpaceVie.setLayoutParams(params);
            mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            if (!bean.isPlaying())
                finish();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            params.width = AppTools.dp2px(mContext, 32);
            mSpaceVie.setLayoutParams(params);
            mTvTime.setText("00:00");
        }
    }

    public void changeProgress(int progress) {
        if (mBean.isPlaying()) {
            try {
                mIvPlay.statr();
                reStart.setVisibility(VISIBLE);
                int length = Integer.parseInt(mBean.getVoice_len());
                seekProgress.setIsMove(true);
                seekProgress.setProgress((int) (progress * 1f / length));
                if (length * 1000 - progress < 0) {
                    mTvTime.setText(AppTools.parseTime2Str(length));
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(progress));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                reStart.setVisibility(GONE);
            }
        } else {
            seekProgress.setProgress(0);
            seekProgress.setIsMove(false);
            mIvPlay.stop();
            reStart.setVisibility(GONE);
            try {
                int length = Integer.parseInt(mBean.getVoice_len());
                mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mTvTime.setText("00:00");
            }
        }
    }

    public BaseAnimBean getData() {
        return mBean;
    }

    public void setHintText(String text) {
        mTvTime.setText(text);
    }

    public void start() {
        mIvPlay.statr();
    }

    public void finish() {
        mIvPlay.stop();
        seekProgress.setProgress(0);
        seekProgress.setIsMove(false);
        try {
            if (mBean != null) {
                int length = Integer.parseInt(mBean.getVoice_len());
                if (mBean.getPasuePosition() > 0) {
                    //                    reStart.setVisibility(VISIBLE);
                    mTvTime.setText(AppTools.parseTime2Str(mBean.getPasuePosition()));
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                }
                reStart.setVisibility(GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            reStart.setVisibility(GONE);
        }

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
