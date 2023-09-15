package org.xiaoxingqi.shengxi.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import org.xiaoxingqi.shengxi.impl.OnPlayListener;
import org.xiaoxingqi.shengxi.model.BaseAnimBean;

import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    public static final String TAG = "AudioPlayer";
    private MediaPlayer mPlayer;
    private String mAudioFile;
    private AudioManager audioManager;
    private OnPlayListener mListener;
    private int audioStreamType;
    private Context context;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    public AudioPlayer(Context var1) {
        this(var1, null, null);
    }

    @SuppressLint("WrongConstant")
    public AudioPlayer(Context context, String var2, OnPlayListener var3) {
        this.context = context;
        this.audioStreamType = 0;
        this.onAudioFocusChangeListener = var1 -> {
            switch (var1) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (isPlaying()) {
                        mPlayer.setVolume(0.1F, 0.1F);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS:
                    stop();
                    return;
                case 0:
                default:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (isPlaying()) {
                        mPlayer.setVolume(1.0F, 1.0F);
                    }
            }

        };
        audioManager = (AudioManager) context.getSystemService("audio");
        mAudioFile = var2;
        mListener = var3;
    }

    public final void setDataSource(String path) {
        if (!TextUtils.equals(path, mAudioFile)) {
            mAudioFile = path;
        }
    }

    public String getmAudioFile() {
        return mAudioFile;
    }

    private BaseAnimBean bean;

    public void setBean(BaseAnimBean bean) {
        this.bean = bean;
    }

    public BaseAnimBean getBean() {
        return bean;
    }

    public final void setOnPlayListener(OnPlayListener var1) {
        mListener = var1;
    }

    public final OnPlayListener getOnPlayListener() {
        return mListener;
    }

    public final void start(int var1) {
        audioStreamType = var1;
        startPlay();
    }

    public final void stop() {
        if (mPlayer != null) {
            endPlay();
            if (mListener != null) {
                mListener.onInterrupt();
            }
        }

    }

    public final boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public final long getDuration() {
        return mPlayer != null ? (long) mPlayer.getDuration() : 0L;
    }

    public final long getCurrentPosition() {
        return mPlayer != null ? (long) mPlayer.getCurrentPosition() : 0L;
    }

    public final void seekTo(int var1) {
        mPlayer.seekTo(var1);
    }

    private void startPlay() {
        this.endPlay();
        this.startInner();
    }

    private void endPlay() {
        if (audioManager != null)
            audioManager.abandonAudioFocus(this.onAudioFocusChangeListener);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private void startInner() {
        mPlayer = new MediaPlayer();
        mPlayer.setLooping(false);
        mPlayer.setAudioStreamType(this.audioStreamType);
        if (audioManager != null) {
            if (audioStreamType == AudioManager.STREAM_MUSIC) {
                audioManager.setSpeakerphoneOn(true);
            } else {
                audioManager.setSpeakerphoneOn(false);
            }
            audioManager.requestAudioFocus(this.onAudioFocusChangeListener, audioStreamType, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        mPlayer.setOnPreparedListener(var1 -> {
            if (mListener != null) {
                mListener.onPrepared();
            }
        });
        mPlayer.setOnCompletionListener(var1 -> {
            endPlay();
            if (mListener != null) {
                mListener.onCompletion();
            }

        });
        mPlayer.setOnErrorListener((var1, var2, var3) -> {
            endPlay();
            if (mListener != null) {
                mListener.onError(String.format("OnErrorListener what:%d extra:%d", var2, var3));
            }
            return true;
        });
        try {
            if (mAudioFile != null) {
                mPlayer.setDataSource(mAudioFile);
                mPlayer.prepare();
                mPlayer.start();
            } else {
                if (mListener != null) {
                    mListener.onError("no datasource");
                }
            }
        } catch (Exception var2) {
            try {
                File file = new File(mAudioFile);
                if (file.length() == 0) {
                    LocalLogUtils.writeLog("删除文件 :" + mAudioFile, System.currentTimeMillis());
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            var2.printStackTrace();
            if (var2 instanceof IOException) {
                File file = new File(mAudioFile);
                file.delete();
                LocalLogUtils.writeLog("delete file  :reason by IOException " + mAudioFile + "  " + var2.getMessage(), System.currentTimeMillis());
            }
            endPlay();
            if (mListener != null) {
                mListener.onError("Exception\n" + var2.toString());
            }
        }
    }

    private void deleteOnExit() {
        File var1;
        if ((var1 = new File(mAudioFile)).exists()) {
            var1.deleteOnExit();
        }
    }
}
