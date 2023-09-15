package org.xiaoxingqi.shengxi.wedgit.flowereffects;


import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.Random;

public class FlowBean {

    public float startX, startY;
    public float endX, endY;
    public float currentX, currentY;
    public Matrix matrix;
    public float degrees;
    public float scale;
    public long duration;
    public int windowsWidth;
    public Random random;
    public float dx;
    public long delay = 0;
    public Bitmap bitmap;

    public FlowBean(float startX, float startY, float endX, float endY) {
        currentX = startX;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        matrix = new Matrix();
        random = new Random();
    }

}
