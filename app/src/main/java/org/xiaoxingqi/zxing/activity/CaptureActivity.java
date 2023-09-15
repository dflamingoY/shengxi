//package org.xiaoxingqi.zxing.activity;
//
//import android.Manifest;
//import android.content.ContentResolver;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.res.AssetFileDescriptor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Vibrator;
//import android.provider.MediaStore;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.SurfaceHolder;
//import android.view.SurfaceHolder.Callback;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.ChecksumException;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.FormatException;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.Result;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.qrcode.QRCodeReader;
//
//import org.xiaoxingqi.shengxi.R;
//import org.xiaoxingqi.shengxi.modules.listen.ActionActivity;
//import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity;
//import org.xiaoxingqi.zxing.camera.CameraManager;
//import org.xiaoxingqi.zxing.decoding.CaptureActivityHandler;
//import org.xiaoxingqi.zxing.decoding.InactivityTimer;
//import org.xiaoxingqi.zxing.decoding.RGBLuminanceSource;
//import org.xiaoxingqi.zxing.encoding.RxQrBarTool;
//import org.xiaoxingqi.zxing.view.ViewfinderView;
//
//import java.io.IOException;
//import java.util.Hashtable;
//import java.util.Vector;
//
//
///**
// * Initial the camera
// *
// * @author Ryan.Tang
// */
//public class CaptureActivity extends AppCompatActivity implements Callback {
//
//    private static final int REQUEST_CODE_LOCAL = 0;
//    private CaptureActivityHandler handler;
//    private ViewfinderView viewfinderView;
//    private boolean hasSurface;
//    private Vector<BarcodeFormat> decodeFormats;
//    private String characterSet;
//    private InactivityTimer inactivityTimer;
//    private MediaPlayer mediaPlayer;
//    private boolean playBeep;
//    private static final float BEEP_VOLUME = 0.10f;
//    private boolean vibrate;
//
//    /**
//     * Called when the activity is first created.
//     */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED) {
//                //申请WRITE_EXTERNAL_STORAGE权限
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                        0);//自定义的code
//            }
//        }
//        setContentView(R.layout.activity_camera);
//        CameraManager.init(getApplication());
//        viewfinderView = findViewById(R.id.viewfinder_view);
//        View relativePhoto = findViewById(R.id.relative_Photo);
//        Toolbar toobar = findViewById(R.id.tintToolbar);
//        setSupportActionBar(toobar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//        hasSurface = false;
//        inactivityTimer = new InactivityTimer(this);
//        relativePhoto.setOnClickListener(v -> {
//            Intent intent;
//            if (Build.VERSION.SDK_INT < 19) {
//                intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//            } else {
//                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            }
//            startActivityForResult(intent, REQUEST_CODE_LOCAL);
//        });
//    }
//
//
//    /**
//     * 判断Android系统版本是否 >= M(API23)
//     */
//    private boolean isM() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
//        SurfaceHolder surfaceHolder = surfaceView.getHolder();
//        if (hasSurface) {
//            initCamera(surfaceHolder);
//        } else {
//            surfaceHolder.addCallback(this);
//            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        }
//        decodeFormats = null;
//        characterSet = null;
//
//        playBeep = true;
//        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
//        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
//            playBeep = false;
//        }
//        initBeepSound();
//        vibrate = true;
//        //quit the scan view
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (handler != null) {
//            handler.quitSynchronously();
//            handler = null;
//        }
//        CameraManager.get().closeDriver();
//    }
//
//    @Override
//    protected void onDestroy() {
//        inactivityTimer.shutdown();
//        super.onDestroy();
//    }
//
//    /**
//     * Handler scan result
//     *
//     * @param result
//     * @param barcode
//     */
//    public void handleDecode(Result result, Bitmap barcode) {
//        inactivityTimer.onActivity();
//        playBeepSoundAndVibrate();
//        String resultString = result.getText();
//        if (!TextUtils.isEmpty(resultString)) {
//            if (resultString.contains("https://h5.byebyetext.com/#/share/uid/")) {
//                startActivity(new Intent(this, UserDetailsActivity.class).putExtra("Uid", resultString.substring(resultString.lastIndexOf("/") + 1)));
//            } else {
//                startActivity(new Intent(this, ActionActivity.class).putExtra("url", resultString));
//            }
//        }
//        Log.d("Mozator", "结果 " + resultString);
//        //FIXME
//        CaptureActivity.this.finish();
//    }
//
//    private void initCamera(SurfaceHolder surfaceHolder) {
//        try {
//            CameraManager.get().openDriver(surfaceHolder);
//        } catch (IOException ioe) {
//            return;
//        } catch (RuntimeException e) {
//            return;
//        }
//        if (handler == null) {
//            handler = new CaptureActivityHandler(this, decodeFormats,
//                    characterSet);
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width,
//                               int height) {
//
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        if (!hasSurface) {
//            hasSurface = true;
//            initCamera(holder);
//        }
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        hasSurface = false;
//
//    }
//
//    public ViewfinderView getViewfinderView() {
//        return viewfinderView;
//    }
//
//    public Handler getHandler() {
//        return handler;
//    }
//
//    public void drawViewfinder() {
//        viewfinderView.drawViewfinder();
//
//    }
//
//    private void initBeepSound() {
//        if (playBeep && mediaPlayer == null) {
//            // The volume on STREAM_SYSTEM is not adjustable, and users found it
//            // too loud,
//            // so we now play on the music stream.
//            setVolumeControlStream(AudioManager.STREAM_MUSIC);
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setOnCompletionListener(beepListener);
//
//            AssetFileDescriptor file = getResources().openRawResourceFd(
//                    R.raw.beep);
//            try {
//                mediaPlayer.setDataSource(file.getFileDescriptor(),
//                        file.getStartOffset(), file.getLength());
//                file.close();
//                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
//                mediaPlayer.prepare();
//            } catch (IOException e) {
//                mediaPlayer = null;
//            }
//        }
//    }
//
//    private static final long VIBRATE_DURATION = 200L;
//
//    private void playBeepSoundAndVibrate() {
//        if (playBeep && mediaPlayer != null) {
//            mediaPlayer.start();
//        }
//        if (vibrate) {
//            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            vibrator.vibrate(VIBRATE_DURATION);
//        }
//    }
//
//    /**
//     * When the beep has finished playing, rewind to queue up another one.
//     */
//    private final OnCompletionListener beepListener = new OnCompletionListener() {
//        public void onCompletion(MediaPlayer mediaPlayer) {
//            mediaPlayer.seekTo(0);
//        }
//    };
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (data != null) {
//            if (requestCode == REQUEST_CODE_LOCAL) {
//                ContentResolver resolver = getContentResolver();
//                Uri selectedImage = data.getData();
//                try {
//                    Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, selectedImage);
//                    Result codeResult = RxQrBarTool.decodeFromPhoto(photo);
//                    if (codeResult == null) {
//                        Toast.makeText(this, "未识别到图片中的二维码", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    String result = codeResult.getText();
//                    if (!TextUtils.isEmpty(result)) {
//                        if (result.contains("https://h5.byebyetext.com/#/share/uid/")) {
//                            startActivity(new Intent(this, UserDetailsActivity.class).putExtra("Uid", result.substring(result.lastIndexOf("/") + 1)));
//                        } else {
//                            startActivity(new Intent(this, ActionActivity.class).putExtra("url", result));
//                        }
//                    }
//                } catch (IOException e) {
//                    Toast.makeText(this, "未识别到图片中的二维码", Toast.LENGTH_SHORT).show();
//                    finish();
//                } finally {
//                    finish();
//                }
//            }
//        }
//    }
//
//    private String paserBitmap(String path) {
//        if (TextUtils.isEmpty(path)) {
//            return null;
//        }
//        // DecodeHintType 和EncodeHintType
//        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
//        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true; // 先获取原大小
//        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
//        options.inJustDecodeBounds = false; // 获取新的大小
//        int sampleSize = (int) (options.outHeight / (float) 200);
//        if (sampleSize <= 0)
//            sampleSize = 1;
//        options.inSampleSize = sampleSize;
//        scanBitmap = BitmapFactory.decodeFile(path, options);
//        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
//        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
//        QRCodeReader reader = new QRCodeReader();
//        try {
//            return reader.decode(bitmap1, hints).getText();
//        } catch (NotFoundException e) {
//
//            e.printStackTrace();
//
//        } catch (ChecksumException e) {
//
//            e.printStackTrace();
//
//        } catch (FormatException e) {
//
//            e.printStackTrace();
//
//        }
//
//        return null;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                break;
//        }
//        return true;
//    }
//}