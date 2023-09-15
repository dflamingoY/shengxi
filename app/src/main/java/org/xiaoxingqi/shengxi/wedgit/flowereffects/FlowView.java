package org.xiaoxingqi.shengxi.wedgit.flowereffects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.ValueAnimator;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.effects.LeavesAnim;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.effects.MapleAnim;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.effects.RainAnim;
import org.xiaoxingqi.shengxi.wedgit.flowereffects.effects.SnowAnim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 屏幕中最多出现5片叶子, 5片雪花, 5片花瓣,  允许旋转  允许倾斜
 * <p>
 * 雨滴不能旋转, 垂直降落
 */
public class FlowView extends View {

    private int[] snow_id = {R.mipmap.icon_effects_snow_1};
    private int[] flower_id = {R.mipmap.icon_effedts_flower_3};
    private int[] water_id = {R.mipmap.icon_secens_rain};
    private int[] leaves_id = {R.mipmap.icon_effedts_leaves_1};
    private int[] maple_id = {R.mipmap.icon_effects_maple, R.mipmap.icon_effects_maple_1, R.mipmap.icon_effects_maple_2};
    private List<BaseAnimFactory> factoryList = new ArrayList<>();
    private Random random = new Random();
    //    private ValueAnimator animator;
    private boolean isStart;

    public FlowView(Context context) {
        this(context, null, 0);
    }

    public FlowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        //        animator = ValueAnimator.ofFloat(0, 1);
        //        animator.setRepeatCount(ValueAnimator.INFINITE);
        //        animator.addUpdateListener(animation -> invalidate());
        //        animator.setInterpolator(new LinearInterpolator());
        //        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (BaseAnimFactory factory : factoryList) {
            factory.onDraw(canvas);
        }
        if (isStart)
            postInvalidateOnAnimation();
    }

    private String currentType = "";

    public void startView(String type) {
        if (!currentType.equalsIgnoreCase(type)) {
            stopView();
            initView();
            generator(type);
            currentType = type;
            isStart = true;
            postInvalidateOnAnimation();
        }
    }

    public void generator(String type) {
        if (IConstant.THEME_SNOW.equalsIgnoreCase(type)) {
            generatorSnow();
        } else {
            for (int a = 0; a < 7; a++) {
                float startX = random.nextInt(getWidth());
                FlowBean bean = new FlowBean(startX, 0 - random.nextInt(200) - 100, startX, getHeight());
                bean.windowsWidth = getWidth();
                switch (type) {
                    case IConstant.THEME_FLOWER:
                        bean.duration = (long) (5000 + random.nextFloat() * 4000);
                        bean.scale = (random.nextInt(2) + 5) / 10f;
                        bean.delay = a * 1300;
                        bean.currentY = bean.startY;
                        bean.bitmap = BitmapFactory.decodeResource(getResources(), flower_id[0]);
                        factoryList.add(new LeavesAnim(bean));
                        break;
                    case IConstant.THEME_LEAVES:
                        bean.duration = (long) (5000 + random.nextFloat() * 4000);
                        bean.scale = (random.nextInt(2) + 5) / 10f;
                        bean.delay = a * 1300;
                        bean.currentY = bean.startY;
                        bean.bitmap = BitmapFactory.decodeResource(getResources(), leaves_id[0]);
                        factoryList.add(new LeavesAnim(bean));
                        break;
                    case IConstant.THEME_RAIN:
                        bean.duration = (long) (1000 + random.nextFloat() * 1000);
                        bean.scale = (random.nextInt(5) + 1) / 10f;
                        bean.delay = (long) (random.nextInt(4) * 100);
                        bean.currentY = bean.startY;
                        bean.bitmap = BitmapFactory.decodeResource(getResources(), water_id[0]);
                        factoryList.add(new RainAnim(bean));
                        break;
                    case IConstant.THEME_MAPLE:
                        bean.duration = (long) (6000 + random.nextFloat() * 3000);
                        bean.scale = (random.nextInt(2) + 5) / 10f;
                        bean.delay = a * 1300;
                        bean.currentY = bean.startY;
                        factoryList.add(new MapleAnim(bean));
                        bean.bitmap = BitmapFactory.decodeResource(getResources(), maple_id[random.nextInt(maple_id.length - 1)]);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void generatorSnow() {
        for (int a = 0; a < 25; a++) {
            float startX = random.nextInt(getWidth());
            FlowBean bean = new FlowBean(startX, -random.nextInt(200) - 200, startX, getHeight());
            bean.windowsWidth = getWidth();
            bean.currentY = bean.startY;
            bean.duration = (long) (5000 + random.nextFloat() * 5000);
            bean.endX = bean.startX - 100;
            bean.delay = (long) (a * (100 + random.nextFloat() * 500));
            bean.bitmap = BitmapFactory.decodeResource(getResources(), snow_id[0]);
            bean.scale = (random.nextInt(3) + 3) / 10f;
            factoryList.add(new SnowAnim(bean));
        }
    }

    public void stopView() {
        try {
            currentType = "";
            /*if (null != animator) {
                animator.end();
                animator.cancel();
                animator = null;
            }*/
            isStart = false;
            if (factoryList != null && factoryList.size() > 0) {
                for (BaseAnimFactory bean : factoryList) {
                    bean.getBean().bitmap.recycle();
                    bean.stop();
                }
            }
            factoryList.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopView();
        super.onDetachedFromWindow();
    }
}
