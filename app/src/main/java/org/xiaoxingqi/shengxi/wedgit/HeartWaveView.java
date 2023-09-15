package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.App;
import org.xiaoxingqi.shengxi.model.BaseBean;
import org.xiaoxingqi.shengxi.modules.LightUtils;

import skin.support.SkinCompatManager;
import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;
import skin.support.widget.SkinCompatTextView;

public class HeartWaveView extends View implements SkinCompatSupportable {
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.03f;
    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_WAVE_LENGTH_RATIO = 1.0f;
    private static final float DEFAULT_WAVE_SHIFT_RATIO = 0.0f;
    private boolean mShowWave = true;
    private BitmapShader mWaveShader;
    private Matrix mShaderMatrix;
    private Paint mViewPaint;
    private float mDefaultAmplitude;
    private float mDefaultWaterLevel;
    private double mDefaultAngularFrequency;
    private float mAmplitudeRatio = DEFAULT_AMPLITUDE_RATIO;
    private float mWaveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO;
    private float mWaterLevelRatio = 0f;
    private float mWaveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO;
    private int mBehindWaveColor = Color.parseColor("#fc6158");
    private BaseBean bean;
    public static Bitmap bitmap;
    public Drawable bgDraw;
    private int drawableId;
    private int waveColorId = 0;

    static {
        bitmap = BitmapFactory.decodeResource(App.context.getResources(), R.mipmap.icon_empty_heart_empty);
    }

    public HeartWaveView(Context context) {
        this(context, null, 0);
    }

    public HeartWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HeartWaveView);
        drawableId = array.getResourceId(R.styleable.HeartWaveView_heart_background, 0);
        waveColorId = array.getResourceId(R.styleable.HeartWaveView_heart_color, 0);
        array.recycle();
        SkinCompatResources.init(getContext());
        applySkin();
        init();
    }

    private void init() {
        mShaderMatrix = new Matrix();
        mViewPaint = new Paint();
        mViewPaint.setAntiAlias(true);
        mViewPaint.setFilterBitmap(true);
        mViewPaint.setDither(true);
    }

    @Override
    public void applySkin() {
        try {
            Log.d("Mozator", "width " + getWidth() + " getHeight " + getHeight());
            mBehindWaveColor = SkinCompatResources.getInstance().getColorStateList(waveColorId).getDefaultColor();
            bgDraw = SkinCompatResources.getInstance().getMipmap(drawableId);
            if (getWidth() > 0 && getHeight() > 0)
                createShader();
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachBean(BaseBean bean) {
        this.bean = bean;
        setSelected(bean.getIs_collected() == 1);
        if (bean.getIs_collected() == 1) {
            mWaterLevelRatio = 1f;
        } else {
            mWaterLevelRatio = 0f;
        }
        invalidate();
    }

    public float getWaveShiftRatio() {
        return mWaveShiftRatio;
    }

    //波浪角度
    public void setWaveShiftRatio(float waveShiftRatio) {
        if (bean != null && bean.isPlaying() && bean.getIs_collected() != 1) {
            if (mWaveShiftRatio != waveShiftRatio) {
                mWaveShiftRatio = waveShiftRatio;
                postInvalidateOnAnimation();
            }
        }
    }

    public float getWaterLevelRatio() {
        return mWaterLevelRatio;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (isSelected() && bean.getIs_collected() == 1) {
            mWaterLevelRatio = 1f;
        } else {
            mWaterLevelRatio = 0f;
        }
        postInvalidateOnAnimation();
    }

    /**
     * 前10%时间 快速跑满60%的进度
     * 跟总时间成比例
     */
    public void setWaterLevelRatio(float waterLevelRatio) {
        if (bean != null && bean.isPlaying() && bean.getIs_collected() != 1 && LightUtils.Companion.contains(bean.getVoice_id())) {
            setSelected(true);
            if (mWaterLevelRatio != waterLevelRatio) {
                int length = Integer.parseInt(bean.getVoice_len());
                if (waterLevelRatio <= 0.1f) {
                    mWaterLevelRatio = waterLevelRatio * (length * 0.6f / (length * 0.1f));
                    if (mWaterLevelRatio > 0.6) {
                        mWaterLevelRatio = 0.6f;
                    }
                } else if (waterLevelRatio > 0.1f && waterLevelRatio < 0.6f) {
                    mWaterLevelRatio = 0.6f;
                } else {
                    if (waterLevelRatio > 0.8)
                        mWaterLevelRatio = 0.8f;
                    else
                        mWaterLevelRatio = waterLevelRatio;
                }
                postInvalidateOnAnimation();
            }
        } else {
            if (null != bean && bean.getIs_collected() == 1) {
                mWaterLevelRatio = 1f;
            } else {
                mWaterLevelRatio = 0f;
            }
        }
    }

    public float getWaveLengthRatio() {
        return mWaveLengthRatio;
    }

    public void setWaveLengthRatio(float waveLengthRatio) {
        mWaveLengthRatio = waveLengthRatio;
    }

    public void setAmplitudeRatio(float amplitudeRatio) {
        if (mAmplitudeRatio != amplitudeRatio) {
            mAmplitudeRatio = amplitudeRatio;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createShader();
    }

    private void createShader() {
        mDefaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / getWidth();
        mDefaultAmplitude = getHeight() * DEFAULT_AMPLITUDE_RATIO;
        mDefaultWaterLevel = getHeight() * DEFAULT_WATER_LEVEL_RATIO;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);
        final int endX = getWidth() + 1;
        final int endY = getHeight() + 1;
        float[] waveY = new float[endX];
        wavePaint.setColor(mBehindWaveColor);
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * mDefaultAngularFrequency;
            float beginY = (float) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);
            waveY[beginX] = beginY;
        }
        mWaveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        mViewPaint.setShader(mWaveShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWaterLevelRatio == 0 && !isSelected()) {
            if (bgDraw != null) {
                bgDraw.setBounds(0, 0, getWidth(), getHeight());
                bgDraw.draw(canvas);
            }
        } else {
            if (mShowWave && mWaveShader != null) {
                if (mViewPaint.getShader() == null) {
                    mViewPaint.setShader(mWaveShader);
                }
                mShaderMatrix.setScale(
                        mWaveLengthRatio / DEFAULT_WAVE_LENGTH_RATIO,
                        mAmplitudeRatio / DEFAULT_AMPLITUDE_RATIO,
                        0,
                        mDefaultWaterLevel);
                mShaderMatrix.postTranslate(
                        mWaveShiftRatio * getWidth(),
                        (DEFAULT_WATER_LEVEL_RATIO - mWaterLevelRatio) * getHeight());
                mWaveShader.setLocalMatrix(mShaderMatrix);
                float radius = getWidth() / 2f;
                int count = canvas.saveLayer(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()), mViewPaint);
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, mViewPaint);
                mViewPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                canvas.drawBitmap(bitmap, null, new RectF(0, 0, getWidth(), getHeight()), mViewPaint);
                mViewPaint.setXfermode(null);
                canvas.restoreToCount(count);
            } else {
                mViewPaint.setShader(null);
            }
        }
    }

    public void end() {
        if (bean.getIs_collected() != 1) {
            setSelected(false);
            mWaterLevelRatio = 0f;
        } else {
            mWaterLevelRatio = 1f;
        }
        invalidate();
    }
}
