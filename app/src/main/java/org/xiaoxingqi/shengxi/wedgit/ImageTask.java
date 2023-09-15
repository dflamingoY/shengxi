package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class ImageTask extends AppCompatImageView {
    public ImageTask(Context context) {
        super(context);
    }

    public ImageTask(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageTask(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*private Drawable preDrawable;

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (null != getDrawable()) {
                super.onDraw(canvas);
                preDrawable = getDrawable();
            } else {
                if (preDrawable != null) {
                    try {
                        preDrawable.draw(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private String url;

    public void setImageUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
