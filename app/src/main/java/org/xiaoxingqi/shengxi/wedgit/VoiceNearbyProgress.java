package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.BaseAnimBean;
import org.xiaoxingqi.shengxi.utils.AppTools;

public class VoiceNearbyProgress extends RelativeLayout {
    private BaseAnimBean mBean;
    private PlayWifiView mIvPlay;
    private View mSpaceVie;
    private TextView mTvTime;
    private ImageView reStart;
    private ViewSeekProgress seekProgress;

    public VoiceNearbyProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.layout_voice_nearby_wifi, this);

        mIvPlay = findViewById(R.id.play);
        mSpaceVie = findViewById(R.id.progress);
        mTvTime = findViewById(R.id.tv_Time);
        seekProgress = findViewById(R.id.viewSeekProgress);
        reStart = new ImageView(context);
        reStart.setId(R.id.iv_click_reStart);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(AppTools.dp2px(context, 21), AppTools.dp2px(context, 21));
        params.addRule(RelativeLayout.RIGHT_OF, R.id.relativeVoiceContainer);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMarginStart(AppTools.dp2px(context, 8));
        //        reStart.setImageResource(R.mipmap.btn_camera);
        reStart.setLayoutParams(params);
        reStart.setVisibility(GONE);
        addView(reStart);
    }

    public void setData(BaseAnimBean bean) {
        mBean = bean;
        seekProgress.setProgress(0);
        seekProgress.setIsMove(false);
        try {
            int length = Integer.parseInt(bean.getVoice_len());
            mTvTime.setText(AppTools.parseTime2Str(length * 1000));
            if (!bean.isPlaying()) {
                finish();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
                    mTvTime.setText(AppTools.parseTime2Str(0));
                } else {
                    mTvTime.setText(AppTools.parseTime2Str(progress));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                reStart.setVisibility(GONE);
            }
        } else {
            mIvPlay.stop();
            reStart.setVisibility(GONE);
            seekProgress.setProgress(0);
            seekProgress.setIsMove(false);
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
                    reStart.setVisibility(VISIBLE);
                    mTvTime.setText(AppTools.parseTime2Str(mBean.getPasuePosition()));
                } else {
                    reStart.setVisibility(GONE);
                    mTvTime.setText(AppTools.parseTime2Str(length * 1000));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }
}
