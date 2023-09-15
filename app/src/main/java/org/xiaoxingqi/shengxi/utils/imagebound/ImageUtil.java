package org.xiaoxingqi.shengxi.utils.imagebound;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.IOException;

public class ImageUtil {
    public static class ImageSize {
        public int width = 0;
        public int height = 0;

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public final static float MAX_IMAGE_RATIO = 5f;

    public static final Bitmap getBitmapImmutableCopy(Resources res, int id) {
        return getBitmap(res.getDrawable(id)).copy(Config.RGB_565, false);
    }

    public static final Bitmap getBitmap(Drawable dr) {
        if (dr == null) {
            return null;
        }

        if (dr instanceof BitmapDrawable) {
            return ((BitmapDrawable) dr).getBitmap();
        }

        return null;
    }

    public static Bitmap rotateBitmapInNeeded(String path, Bitmap srcBitmap) {
        if (TextUtils.isEmpty(path) || srcBitmap == null) {
            return null;
        }

        ExifInterface localExifInterface;
        try {
            localExifInterface = new ExifInterface(path);
            int rotateInt = localExifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            float rotate = getImageRotate(rotateInt);
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                Bitmap dstBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                        srcBitmap.getWidth(), srcBitmap.getHeight(), matrix,
                        false);
                if (dstBitmap == null) {
                    return srcBitmap;
                } else {
                    if (srcBitmap != null && !srcBitmap.isRecycled()) {
                        srcBitmap.recycle();
                    }
                    return dstBitmap;
                }
            } else {
                return srcBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return srcBitmap;
        }
    }

    /**
     * 获得旋转角度
     *
     * @param rotate
     * @return
     */
    public static float getImageRotate(int rotate) {
        float f;
        if (rotate == 6) {
            f = 90.0F;
        } else if (rotate == 3) {
            f = 180.0F;
        } else if (rotate == 8) {
            f = 270.0F;
        } else {
            f = 0.0F;
        }

        return f;
    }


    public static ImageSize getThumbnailDisplaySize(float srcWidth, float srcHeight, float dstMaxWH, float dstMinWH) {
        if (srcWidth <= 0 || srcHeight <= 0) { // bounds check
            return new ImageSize((int) dstMinWH, (int) dstMinWH);
        }

        float shorter;
        float longer;
        boolean widthIsShorter;

        //store
        if (srcHeight < srcWidth) {
            shorter = srcHeight;
            longer = srcWidth;
            widthIsShorter = false;
        } else {
            shorter = srcWidth;
            longer = srcHeight;
            widthIsShorter = true;
        }

        if (shorter < dstMinWH) {
            float scale = dstMinWH / shorter;
            shorter = dstMinWH;
            if (longer * scale > dstMaxWH) {
                longer = dstMaxWH;
            } else {
                longer *= scale;
            }
        } else if (longer > dstMaxWH) {
            float scale = dstMaxWH / longer;
            longer = dstMaxWH;
            if (shorter * scale < dstMinWH) {
                shorter = dstMinWH;
            } else {
                shorter *= scale;
            }
        }

        //restore
        if (widthIsShorter) {
            srcWidth = shorter;
            srcHeight = longer;
        } else {
            srcWidth = longer;
            srcHeight = shorter;
        }

        return new ImageSize((int) srcWidth, (int) srcHeight);
    }


    public static boolean isInvalidPictureFile(String mimeType) {
        String lowerCaseFilepath = mimeType.toLowerCase();
        return (lowerCaseFilepath.contains("jpg") || lowerCaseFilepath.contains("jpeg")
                || lowerCaseFilepath.toLowerCase().contains("png") || lowerCaseFilepath.toLowerCase().contains("bmp") || lowerCaseFilepath
                .toLowerCase().contains("gif"));
    }

    public static boolean isGif(String extension) {
        return !TextUtils.isEmpty(extension) && extension.contains("gif");
    }
}
