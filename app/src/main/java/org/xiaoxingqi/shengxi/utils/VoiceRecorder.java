package org.xiaoxingqi.shengxi.utils;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class VoiceRecorder {

    private boolean isRecording = false;
    private MediaRecorder recorder;
    private String endExt = ".m4a";
    private String recorderPath;
    private long startTime;
    private int maxTime;
    private RecorderDown timerDown;
    private OnRecorderListener listener;
    //是否是手动点击完成录制
    private boolean isClickComplete = false;

    public VoiceRecorder(int maxTime, OnRecorderListener listener) {
        this.maxTime = maxTime;
        this.listener = listener;
    }

    public void start() {
        try {
            if (null != recorder) {
                recorder.release();
                recorder = null;
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1); // MONO
            recorder.setAudioSamplingRate(44100); // 8000Hz
            recorder.setAudioEncodingBitRate(96000);// seems if change this to 128, still got same file size.  96000
            File files = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME);
            if (!files.exists())
                files.mkdirs();
            recorderPath = new File(files, fileName()).getAbsolutePath();
            Log.d("Mozator", "录音文件名 : " + recorderPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recorder.setOutputFile(new File(recorderPath));
            } else {
                recorder.setOutputFile(recorderPath);
            }
            recorder.prepare();
            isRecording = true;
            recorder.start();
            startTime = System.currentTimeMillis();
            delay();
            if (null != listener) {
                listener.onRecordStart();
            }
        } catch (IOException e) {
            if (null != listener) {
                listener.onRecordFail();
            }
        }
        new Thread(() -> {
            while (true) {
                try {
                    if (isRecording) {
                        float max = recorder.getMaxAmplitude() * 13 / 32767;
                        //                        Log.d("Mozator", "max :  " + max);
                        SystemClock.sleep(100L);
                        continue;
                    }
                } catch (Exception var2) {
                }
                return;
            }
        }).start();

    }

    /**
     * 延迟时间任务 定时停止
     */
    private void delay() {
        timerDown = new RecorderDown(maxTime * 1000, 500);
        timerDown.start();
    }

    /**
     * 创建文件名
     *
     * @return
     */
    private String fileName() {
        return UUID.randomUUID().toString() + endExt;
    }

    /**
     * 获取时间长度 ms
     *
     * @return
     */
    public synchronized void completeRecord() {
        if (timerDown != null) {
            isClickComplete = true;
            timerDown.onFinish();
        }
    }

    /**
     * 停止
     */
    private void stop() {
        try {
            if (recorder != null) {
                isRecording = false;
                recorder.stop();
                recorder.release();
                recorder = null;
                int seconds = (int) (System.currentTimeMillis() - startTime);
                if (listener != null) {
                    listener.onRecordSuccess(recorderPath, isClickComplete ? seconds : maxTime * 1000L);
                }
            }
        } catch (IllegalStateException e) {//报异常
            //录制失败
            LocalLogUtils.writeLog("VoiceRecorder : " + e.getMessage(), System.currentTimeMillis());
            if (null != listener) {
                listener.onRecordFail();
            }
        } finally {
            if (null != timerDown) {
                timerDown.cancel();
            }
            isClickComplete = false;
            timerDown = null;
            isRecording = false;
        }
    }

    /**
     * 取消录制
     */
    public void cancelRecorder() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                File file = new File(recorderPath);
                if (!TextUtils.isEmpty(recorderPath) && file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isRecording = false;
        }
        if (null != timerDown) {
            timerDown.cancel();
            timerDown = null;
        }
        if (null != listener) {
            listener.onRecordCancel();
        }

    }

    public boolean isRecording() {
        return isRecording;
    }

    public void onDestroy() {
        if (null != recorder) {
            if (isRecording) {
                recorder.stop();
            }
            recorder.release();
            recorder = null;
        }
        if (null != timerDown) {
            timerDown.cancel();
            timerDown = null;
        }
    }

    public interface OnRecorderListener {

        void onRecordFail();

        void onRecordSuccess(String path, long length);

        void onRecordCancel();

        void onRecordStart();

    }

    /**
     * 倒计时
     */
    private class RecorderDown extends CountDownTimer {

        public RecorderDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            //达到最大时间
            stop();
        }
    }

}
