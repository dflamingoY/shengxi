package org.xiaoxingqi.shengxi.wedgit.customPlayMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import org.xiaoxingqi.shengxi.core.App;
import org.xiaoxingqi.shengxi.model.login.LoginData;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.PreferenceTools;
import org.xiaoxingqi.shengxi.utils.SPUtils;

public class CustomHoverRelative extends RelativeLayout {
    private float mStartX;
    private float mStartY;
    private float mParentWidth = 0;
    private float destX = 0;
    private boolean isClick = false;
    private static float floatX = -1f;//需要展示的坐标
    private static float floatY = -1f;
    private static float currentX = 0f;//溢出屏幕之外的坐标
    private static float currentY = -1f;
    private static String userId = null;

    public static void init() {
        //保存位置
        LoginData.LoginBean loginBean = PreferenceTools.getObj(App.context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        userId = loginBean.getUser_id();
        String position = SPUtils.getString(App.context, IConstant.PLAY_MENU_POSITION + userId, "-1,-1,0,-1");
        if (!TextUtils.isEmpty(position)) {
            String[] splits = position.split(",");
            floatX = Float.parseFloat(splits[0]);
            floatY = Float.parseFloat(splits[1]);
            try {
                currentX = Float.parseFloat(splits[2]);
                currentY = Float.parseFloat(splits[3]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (floatX == -1f && floatY == -1f) {//没有默认值
            //获取默认的高度到屏幕的右下角
            floatX = AppTools.getWindowsWidth(App.context) - AppTools.dp2px(App.context, 90 + 7);
            floatY = AppTools.getWindowsHeight(App.context) - AppTools.dp2px(App.context, 42 + 90 + 29 + 50) - AppTools.getStatusBarHeight(App.context);
            currentX= AppTools.getWindowsWidth(App.context);
            currentY=floatY;
        }
    }

    public CustomHoverRelative(Context context) {
        this(context, null, 0);
    }

    public CustomHoverRelative(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHoverRelative(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void parseClickable(boolean isClick) {
        for (int a = 0; a < getChildCount(); a++) {
            View child = getChildAt(a);
            if (child instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) child).getChildCount(); i++) {
                    View view = getChildAt(a);
                    if (view instanceof ViewGroup) {
                        ViewGroup childParent = ((ViewGroup) view);
                        for (int x = 0; x < childParent.getChildCount(); x++) {
                            View childAt = childParent.getChildAt(x);
                            if (childAt instanceof TouchImageView)
                                ((TouchImageView) childAt).setClickStatus(isClick);
                        }
                    } else if (view instanceof TouchImageView)
                        ((TouchImageView) view).setClickStatus(isClick);
                }
            } else {
                if (child instanceof TouchImageView)
                    ((TouchImageView) child).setClickStatus(isClick);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mParentWidth = ((View) getParent()).getMeasuredWidth();
    }

    private boolean isSlid = false;//是否拖动
    private float sX, sY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSlid = false;
                isClick = false;
                parseClickable(true);
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                sX = event.getRawX();
                sY = event.getRawY();
                this.clearAnimation();
                super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                parseClickable(false);
                isClick = (Math.abs(event.getRawX() - mStartX) < 5 && Math.abs(event.getRawY() - mStartY) < 5);
                isSlid = (Math.abs(event.getRawX() - sX) > 5 || Math.abs(event.getRawY() - sY) > 5);
                float dx = event.getRawX() - mStartX;
                float dy = event.getRawY() - mStartY;
                applyPosition(dx, dy);
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                startSideAnim();
                parseClickable(!isSlid);
                isSlid = false;
                if (isClick) {
                    super.dispatchTouchEvent(event);
                }
                if (isClick) {
                    return true;
                }
                isClick = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                parseClickable(true);
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                this.clearAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                isClick = (Math.abs(event.getRawX() - mStartX) < 5 && Math.abs(event.getRawY() - mStartY) < 5);
                requestDisallowInterceptTouchEvent(true);
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP: {
                if (!isClick) {
                    parseClickable(true);
                }
                if (isClick)
                    return true;
                else
                    return super.onInterceptTouchEvent(event);
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                this.clearAnimation();
                super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                requestDisallowInterceptTouchEvent(true);
                float dx = event.getRawX() - mStartX;
                float dy = event.getRawY() - mStartY;
                applyPosition(dx, dy);
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                startSideAnim();
                parseClickable(false);
                isClick = false;
                return true;
        }
        return true;
    }

    private void applyPosition(float dx, float dy) {
        parseClickable(false);
        setX((int) (getX() + dx));
        ViewGroup group = (ViewGroup) getParent();
        if (getY() + dy <= 0) {
            setY(0);
        } else if (getY() + dy + getMeasuredHeight() >= group.getHeight()) {
            setY(group.getHeight() - getMeasuredHeight());
        } else
            setY((int) (getY() + dy));
    }

    //改变坐标
    public void setFloatX(float x) {
        setX(x);
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    @Override
    public void setY(float y) {
        if (y < 0) {
            y = 0;
        } else if (y > ((ViewGroup) getParent()).getHeight() - getMeasuredHeight()) {
            y = ((ViewGroup) getParent()).getHeight() - getMeasuredHeight();
        }
        super.setY(y);
    }

    /**
     * y轴必须回归到高度线
     */
    private void startSideAnim() {
        if (this.getX() + getMeasuredWidth() >= mParentWidth)
            destX = mParentWidth + getMeasuredWidth();
        else if (this.getX() <= 0) {//(getX() < AppTools.dp2Px(getContext(), 50) || getX() < -getMeasuredWidth() / 2f)
            destX = -getMeasuredWidth();
        } else {
            //记录上一次的点
            floatX = getX();
            floatY = getY();
            //保存位置  可见位置, 隐藏位置
            currentX = getX();
            currentY = getY();
            SPUtils.setString(getContext(), IConstant.PLAY_MENU_POSITION + userId, floatX + "," + floatY + "," + destX + "," + getY());
            if (listener != null) {
                listener.onChange(floatX, floatY);
            }
            return;
        }
        currentX = destX;
        currentY = getY();
        SPUtils.setString(getContext(), IConstant.PLAY_MENU_POSITION + userId, floatX + "," + floatY + "," + destX + "," + getY());
        if (listener != null) {
            listener.onChange(destX, getY());
        }
        TransAnimation anim = new TransAnimation();
        anim.setDuration(220);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null)
                    listener.visible();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(anim);
    }

    public void setCurrent(float x, float y) {
        currentX = x;
        currentY = y;
    }

    public float getFloatX() {
        return floatX;
    }

    public float getFloatY() {
        return floatY;
    }

    public class TransAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            setX((int) (getX() + (destX - getX()) * interpolatedTime + 0.5f));
        }
    }

    private OnSlideChangeListener listener;

    public void setOnSlideChangeListener(OnSlideChangeListener listener) {
        this.listener = listener;
    }

    public interface OnSlideChangeListener {
        void onChange(float x, float y);

        void visible();
    }

}

