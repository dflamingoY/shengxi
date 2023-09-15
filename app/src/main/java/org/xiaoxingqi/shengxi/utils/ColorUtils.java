package org.xiaoxingqi.shengxi.utils;

public class ColorUtils {
    public static int colorEmbedAlpha(int alpha, int color) {

        int startR = (color >> 16) & 0xff;
        int startG = (color >> 8) & 0xff;
        int startB = color & 0xff;
        return (alpha << 24) |
                (startR << 16) |
                (startG << 8) |
                startB;
    }


    /**
     * 计算2个颜色之间的渐变值
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static int getArgb(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24);
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24);
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                (startB + (int) (fraction * (endB - startB)));
    }

    public static int and0x(int color) {
        return color & 0x0000000;
    }

}
