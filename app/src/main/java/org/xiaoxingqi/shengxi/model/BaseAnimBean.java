package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Created by yzm on 2018/3/15.
 */

public class BaseAnimBean implements Parcelable {
    protected String voice_len;
    protected boolean isPlaying;
    protected int pasuePosition;//暂停的进度条
    protected long allDuration = 0;
    private boolean isHeapTop = false;
    private String voicePath;
    private String recognition_content;

    public int getPasuePosition() {
        return pasuePosition;
    }

    public void setPasuePosition(int pasuePosition) {
        this.pasuePosition = pasuePosition;
    }

    public BaseAnimBean() {

    }

    public BaseAnimBean(String voice_len) {
        this.voice_len = voice_len;
    }

    protected BaseAnimBean(Parcel in) {
        voice_len = in.readString();
        isPlaying = in.readByte() != 0;
        allDuration = in.readLong();
        recognition_content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(voice_len);
        dest.writeByte((byte) (isPlaying ? 1 : 0));
        dest.writeLong(allDuration);
        dest.writeString(recognition_content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseAnimBean> CREATOR = new Creator<BaseAnimBean>() {
        @Override
        public BaseAnimBean createFromParcel(Parcel in) {
            return new BaseAnimBean(in);
        }

        @Override
        public BaseAnimBean[] newArray(int size) {
            return new BaseAnimBean[size];
        }
    };

    public String getVoice_len() {
        return voice_len;
    }

    public void setVoice_len(String voice_len) {
        this.voice_len = voice_len;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public long getAllDuration() {
        return allDuration;
    }

    public void setAllDuration(long allDuration) {
        if (allDuration != 0)
            this.allDuration = allDuration;
    }

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public boolean isHeapTop() {
        return isHeapTop;
    }

    public void setHeapTop(boolean heapTop) {
        isHeapTop = heapTop;
    }

    public String getRecognition_content() {
        return recognition_content;
    }

    public void setRecognition_content(String recognition_content) {
        this.recognition_content = recognition_content;
    }
}
