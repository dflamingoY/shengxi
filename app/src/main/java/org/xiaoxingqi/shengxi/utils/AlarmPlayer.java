package org.xiaoxingqi.shengxi.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import org.xiaoxingqi.shengxi.impl.OnPlayListener;

import java.io.IOException;

public class AlarmPlayer {

    private Context context;
    private String path;
    private int stream;//音频输出源
    private OnPlayListener listener;

    public AlarmPlayer(Context context) {
        this.context = context;
    }

    private MediaPlayer player;

    public void stop() {
        if (player != null) {
            endPlay();
            if (listener != null) {
                listener.onInterrupt();
            }
        }
    }

    public void setResource(String path) {
        this.path = path;
    }

    public void start(int stream) {
        this.stream = stream;
        startPlay();
    }

    public void seekTo(int position) {
        player.seekTo(position);
    }

    public boolean isPlaying() {
        if (null != player) {
            return player.isPlaying();
        }
        return false;
    }

    private void startPlay() {
        endPlay();
        startInner();
    }

    private void endPlay() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void startInner() {
        player = new MediaPlayer();
        player.setLooping(false);
        player.setAudioStreamType(stream);
        //        if (stream == AudioManager.STREAM_MUSIC) {
        //            audioManager.setSpeakerphoneOn(true);
        //        } else {
        //            audioManager.setSpeakerphoneOn(false);
        //        }
        player.setOnPreparedListener(mp -> {
            if (null != listener) {
                listener.onPrepared();
            }
        });
        player.setOnCompletionListener(mp -> {
            if (null != listener) {
                listener.onCompletion();
            }
        });
        player.setOnErrorListener((mp, what, extra) -> {
            if (null != listener) {
                listener.onError(String.format("OnErrorListener what:%d extra:%d", what, extra));
            }
            return true;
        });
        try {
            if (path.startsWith("assets://")) {
                AssetFileDescriptor fileDescriptor = context.getAssets().openFd(path.replace("assets://", ""));
                player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            } else {
                player.setDataSource(path);
            }
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
            if (null != listener) {
                listener.onError(e.getMessage());
            }
        }
    }

    public void setOnPlayListener(OnPlayListener listener) {
        this.listener = listener;
    }

    public void onDestory() {
        endPlay();
        context = null;
    }
}
