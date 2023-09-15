package org.xiaoxingqi.zxing.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.zxing.Result;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.BaseAct;
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddBookItemActivity;
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.zxing.encoding.RxQrBarTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ScanActivity extends BaseAct {
    private InactivityTimer inactivityTimer;
    private CaptureActivityHandler handler;//扫描处理
    private FrameLayout mContainer = null;//整体根布局
    private RelativeLayout mCropLayout = null;//扫描框根布局
    private int mCropWidth = 0;//扫描边界的宽度
    private int mCropHeight = 0;//扫描边界的高度
    private boolean hasSurface;//是否有预览
    private boolean vibrate = true;//扫描成功后是否震动
    private boolean mFlashing = true;//闪光灯开启状态
    private final static int REQUESTPERMISSCAMERA = 0x00;
    private final static int REQUEST_CODE_LOCAL = 0x01;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);//Camera初始化
        } else {
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (!hasSurface) {
                        hasSurface = true;
                        initCamera(holder);
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    hasSurface = false;

                }
            });
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    public void initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUESTPERMISSCAMERA);
            }
        }
        CameraManager.init(this);//初始化 CameraManager
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    public void initEvent() {
        findViewById(R.id.btn_Back).setOnClickListener(view -> finish());
    }

    @Override
    public void initView() {
        mContainer = findViewById(R.id.frame_boot);
        mCropLayout = findViewById(R.id.capture_crop_layout);
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    public int getCropWidth() {
        return mCropWidth;
    }

    public void setCropWidth(int cropWidth) {
        mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    public int getCropHeight() {
        return mCropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    public void btn(View view) {
        if (view.getId() == R.id.relative_Photo) {
            startActivityForResult(new Intent(this, AlbumActivity.class).putExtra("count", 1), REQUEST_CODE_LOCAL);
        }
    }

    public void open(View view) {
        light();
    }

    private void light() {
        if (mFlashing) {
            mFlashing = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            mFlashing = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            Point point = CameraManager.get().getCameraResolution();
            AtomicInteger width = new AtomicInteger(point.y);
            AtomicInteger height = new AtomicInteger(point.x);
            int cropWidth = mCropLayout.getWidth() * width.get() / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height.get() / mContainer.getHeight();
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this);
        }
    }
    //========================================打开本地图片识别二维码 end=================================

    //--------------------------------------打开本地图片识别二维码 start---------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_LOCAL) {
                ArrayList<String> gallery = (ArrayList<String>) data.getSerializableExtra("result");
                try {
                    if (null != gallery && gallery.size() > 0) {
                        Bitmap photo = rotaImageView(readPictureDegree(gallery.get(0)), BitmapFactory.decodeFile(gallery.get(0)));
                        // 开始对图像资源解码
                        Result rawResult = RxQrBarTool.decodeFromPhoto(photo);
                        if (rawResult == null) {
                            Toast.makeText(this, "未识别到图片中的ISBN", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String result = rawResult.getText();
                        if (!TextUtils.isEmpty(result) && result.matches(AppTools.ISBN)) {
                            startActivity(new Intent(ScanActivity.this, AddBookItemActivity.class).putExtra("isbn", result));
                        } else {
                            Toast.makeText(this, "未识别到图片中的ISBN", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "未识别到图片中的ISBN", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        }
    }

    private int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private Bitmap rotaImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    //==============================================================================================解析结果 及 后续处理 end

    public void handleDecode(Result result) {
        inactivityTimer.onActivity();
        RxBeepTool.playBeep(this, vibrate);//扫描成功之后的振动与声音提示
        String result1 = result.getText();
        if (!TextUtils.isEmpty(result1) && result1.matches(AppTools.ISBN)) {
            startActivity(new Intent(ScanActivity.this, AddBookItemActivity.class).putExtra("isbn", result1));
        } else {
            Toast.makeText(this, "未识别到图片中的ISBN", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTPERMISSCAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }
}
