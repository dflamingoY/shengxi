package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.TalkListData;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

public class EchoesProgress extends LinearLayout implements SkinCompatSupportable {

    private TextView mTvTime;
    private PlayWifiView mPlaywife;
    private TalkListData.TalkListBean mBean;
    private View mRelativeLeft;
    private View mRelativeRight;
    private PlayWifiView mPlay1;
    private TextView mTvTime1;
    private RelativeLayout frameEcho;
    private ImageView reStartSelf;
    private ImageView reStartOther;
    private Drawable mDrawable;
    private int reStartLeftId;
    private Drawable rightDrawable;
    private int reStartRightId;
    private ViewSeekProgress seekProgress;
    private View viewLayer;

    public EchoesProgress(@NonNull Context context) {
        this(context, null, 0);
    }

    public EchoesProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EchoesProgress(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VoiceProgress);
        mDrawable = array.getDrawable(R.styleable.VoiceProgress_reStartLeftId);
        rightDrawable = array.getDrawable(R.styleable.VoiceProgress_reStartRightId);
        reStartLeftId = array.getResourceId(R.styleable.VoiceProgress_reStartLeftId, 0);
        reStartRightId = array.getResourceId(R.styleable.VoiceProgress_reStartRightId, 0);
        array.recycle();
        applySkin();
        SkinCompatResources.init(context);
        initView(context);
    }

    private void initView(Context context) {
        setGravity(Gravity.CENTER_VERTICAL);
        reStartSelf = new ImageView(context);
        reStartSelf.setId(R.id.iv_click_reStart);
        if (null != rightDrawable)
            reStartSelf.setImageDrawable(rightDrawable);
        reStartSelf.setVisibility(GONE);
        LinearLayout.LayoutParams params = new LayoutParams(AppTools.dp2px(context, 24), AppTools.dp2px(context, 22));
        params.setMargins(0, 0, AppTools.dp2px(context, 10), 0);
        reStartSelf.setLayoutParams(params);
        addView(reStartSelf);

        View.inflate(context, R.layout.layout_echoes_progress, this);
        mTvTime = findViewById(R.id.tv_Time);
        mPlaywife = findViewById(R.id.play);
        mRelativeLeft = findViewById(R.id.relativeLeft);
        mRelativeRight = findViewById(R.id.relativeRight);
        mTvTime1 = findViewById(R.id.tv_Time1);
        mPlay1 = findViewById(R.id.play1);
        frameEcho = findViewById(R.id.relativeVoiceContainer);
        seekProgress = findViewById(R.id.viewSeekProgress);
        viewLayer = findViewById(R.id.viewLayer);

        LinearLayout.LayoutParams params1 = new LayoutParams(AppTools.dp2px(context, 24), AppTools.dp2px(context, 22));
        reStartOther = new ImageView(context);
        params1.setMargins(AppTools.dp2px(context, 10), 0, 0, 0);
        reStartOther.setLayoutParams(params1);
        reStartOther.setId(R.id.iv_click_reStart_other);
        if (null != mDrawable)
            reStartOther.setImageDrawable(mDrawable);
        reStartOther.setVisibility(GONE);
        addView(reStartOther);
    }

    public void setReOnClickListener(OnClickListener onClickListener) {
        reStartOther.setOnClickListener(onClickListener);
        reStartSelf.setOnClickListener(onClickListener);
    }

    /**
     * 中间空白处最短32dp
     * 最大132 dp
     *
     * @param bean
     */
    public void setData(TalkListData.TalkListBean bean) {
        if (null == bean) {
            this.setVisibility(GONE);
            return;
        }
        mBean = bean;
        /**
         * 设置位置设置长度
         */
        try {
            seekProgress.setProgress(0);
            seekProgress.setIsMove(false);
            if (!bean.isPlaying()) {
                finish();
            }
            int length = Integer.parseInt(bean.getVoice_len());
            mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            mTvTime1.setText(AppTools.parseTime2Str(length * 1000));
            int width = (int) (AppTools.dp2px(getContext(), 100 - 32) * (length * 1f / 120) + 0.5f) + AppTools.dp2px(getContext(), 32);
            setContentLength(width);
        } catch (NumberFormatException e) {
            LocalLogUtils.writeLog(bean.getVoice_len() + " : " + e.getMessage(), System.currentTimeMillis());
            mTvTime.setText("00:00");
            mTvTime1.setText("00:00");
            setContentLength(AppTools.dp2px(getContext(), 32));
        }
        if (bean.getIs_self() == 1) {
            mPlay1.setDirecation(bean.getIs_self() == 1, bean.getIs_self() == 1 ? 0xffffffff : 0xffaeaeae);
            mRelativeLeft.setVisibility(GONE);
            mRelativeRight.setVisibility(VISIBLE);
            GradientDrawable mask = new GradientDrawable();
            mask.setCornerRadius(AppTools.dp2px(getContext(), 17));
            mask.setColor(Color.parseColor("#46CDCF"));
            mask.setStroke(0, Color.WHITE);
            frameEcho.setBackground(mask);
            //                        frameEcho.setBackgroundResource(R.drawable.shape_right_voice_bg);
            viewLayer.setSelected(true);
            seekProgress.setMaskColor(getResources().getDrawable(R.drawable.shape_right_voice_bg), Color.parseColor("#46cdcf"), Color.parseColor("#33ffffff"), Color.parseColor("#4dffffff"));
        } else {
            mPlaywife.setDirecation(bean.getIs_self() == 1, bean.getIs_self() == 1 ? 0xffffffff : 0xffaeaeae);

            seekProgress.setMaskColor(getResources().getDrawable(R.drawable.shape_left_voice_bg), Color.parseColor("#ffffff"), Color.parseColor("#14000000"), Color.parseColor("#14000000"));
            GradientDrawable mask = new GradientDrawable();
            mask.setCornerRadius(AppTools.dp2px(getContext(), 17));
            mask.setColor(Color.WHITE);
            mask.setStroke(AppTools.dp2px(getContext(), 1), Color.parseColor("#AEAEAE"));
            frameEcho.setBackground(mask);
            //                        frameEcho.setBackgroundResource(R.drawable.shape_left_voice_bg);
            viewLayer.setSelected(false);
            mRelativeLeft.setVisibility(VISIBLE);
            mRelativeRight.setVisibility(GONE);
        }
        if (!bean.isPlaying()) {
            finish();
        }
    }

    private void setContentLength(int width) {
        LinearLayout.LayoutParams params = (LayoutParams) frameEcho.getLayoutParams();
        params.width = width + AppTools.dp2px(getContext(), 26 + 58);//动画长度 + 文字长度
        frameEcho.setLayoutParams(params);
    }

    public void changeProgress(int progress) {
        if (mBean.isPlaying()) {
            try {
                if (mBean.getIs_self() == 1) {
                    mPlay1.statr();
                    reStartOther.setVisibility(GONE);
                    reStartSelf.setVisibility(VISIBLE);
                } else {
                    mPlaywife.statr();
                    reStartOther.setVisibility(VISIBLE);
                    reStartSelf.setVisibility(GONE);
                }
                int length = Integer.parseInt(mBean.getVoice_len());
                seekProgress.setIsMove(true);
                seekProgress.setProgress((int) (progress * 1f / length));
                if (length * 1000 - progress < 0) {
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                    mTvTime1.setText(AppTools.parseTime2Str(length * 1000));
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(progress));
                    mTvTime1.setText(AppTools.parseTime2Str(progress));
                }
            } catch (NumberFormatException e) {
                reStartOther.setVisibility(GONE);
                reStartSelf.setVisibility(GONE);
            }
        } else {
            mPlaywife.stop();
            mPlay1.stop();
            seekProgress.setProgress(0);
            seekProgress.setIsMove(false);
            try {
                int length = Integer.parseInt(mBean.getVoice_len());
                mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                mTvTime1.setText(AppTools.parseTime2Str(length * 1000));
            } catch (NumberFormatException e) {
                LocalLogUtils.writeLog(mBean.getVoice_len() + " : " + e.getMessage(), System.currentTimeMillis());
                mTvTime.setText("00:00");
                mTvTime1.setText("00:00");
            }
            reStartOther.setVisibility(GONE);
            reStartSelf.setVisibility(GONE);
        }
    }

    public void start() {
        if (mBean.getIs_self() == 1) {
            mPlay1.statr();
        } else {
            mPlaywife.statr();
        }
    }

    /**
     * 如果当前是暂停状态 则显示暂停的进度条
     */
    public void finish() {
        mPlaywife.stop();
        mPlay1.stop();
        seekProgress.setProgress(0);
        seekProgress.setIsMove(false);
        try {
            if (mBean != null) {
                int length = Integer.parseInt(mBean.getVoice_len());
                if (mBean.getPasuePosition() > 0) {
                    mTvTime.setText(AppTools.parseTime2Str(/*length * 1000 -*/ mBean.getPasuePosition()));
                    mTvTime1.setText(AppTools.parseTime2Str(/*length * 1000 -*/ mBean.getPasuePosition()));
                   /* if (mBean.getIs_self() == 1) {
                        reStartSelf.setVisibility(VISIBLE);
                    } else {
                        reStartOther.setVisibility(VISIBLE);
                    }*/
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                    mTvTime1.setText(AppTools.parseTime2Str(length * 1000));
                }
                reStartOther.setVisibility(GONE);
                reStartSelf.setVisibility(GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    public TalkListData.TalkListBean getBean() {
        return mBean;
    }

    @Override
    public void applySkin() {
        try {
            mDrawable = SkinCompatResources.getInstance().getMipmap(reStartLeftId);
            rightDrawable = SkinCompatResources.getInstance().getMipmap(reStartRightId);
            if (null != mDrawable) {
                reStartOther.setImageDrawable(mDrawable);
            }
            if (null != rightDrawable) {
                reStartSelf.setImageDrawable(rightDrawable);
            }
        } catch (Exception e) {
        }
    }
}
