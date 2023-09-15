package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

import java.util.List;

import skin.support.SkinCompatManager;


/**
 * Created by yzm on 2018/3/2.
 * <p>
 * 2019.12.3 有之前的ViewPager 排版改成 横向并排
 */

public class ImageGroupView extends BaseLayout {

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private View layout1;
    private View layout2;
    private View layout3;


    public ImageGroupView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ImageGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageGroupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        img1 = findViewById(R.id.round_img1);
        img2 = findViewById(R.id.round_img2);
        img3 = findViewById(R.id.round_img3);
        layout1 = findViewById(R.id.cardlayout1);
        layout2 = findViewById(R.id.cardlayout2);
        layout3 = findViewById(R.id.cardlayout3);
        layout1.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onClick(0);
            }
        });
        layout2.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onClick(1);
            }
        });
        layout3.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onClick(2);
            }
        });
    }

    public void setOnClickViewListener(OnClickViewListener listener) {
        mClickListener = listener;
    }

    private OnClickViewListener mClickListener;

    public interface OnClickViewListener {
        void onClick(int position);
    }

    @Override
    public int getLayoutId() {
        return R.layout.img_group_layout;
    }

    public void setData(List<String> str) {
        if (str == null || str.size() == 0) {
            this.setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        showImg(img1, str.get(0));
        img1.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (str.size() == 1) {
            layout2.setVisibility(View.GONE);
            layout3.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout1.getLayoutParams();
            params.setMarginStart(AppTools.dp2px(getContext(), 15));
            params.width = AppTools.dp2px(getContext(), 163);
            params.height = AppTools.dp2px(getContext(), 163);
            layout1.setLayoutParams(params);
        } else if (str.size() == 2) {
            layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout1.getLayoutParams();
            params.setMarginStart(AppTools.dp2px(getContext(), 15));
            params.width = AppTools.dp2px(getContext(), 135);
            params.height = AppTools.dp2px(getContext(), 135);
            layout1.setLayoutParams(params);

            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) layout2.getLayoutParams();
            params2.setMarginStart(AppTools.dp2px(getContext(), 8));
            params2.width = AppTools.dp2px(getContext(), 135);
            params2.height = AppTools.dp2px(getContext(), 135);
            layout2.setLayoutParams(params2);
            showImg(img1, str.get(0));
            showImg(img2, str.get(1));

        } else if (str.size() == 3) {
            layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.VISIBLE);
            int width = AppTools.getWindowsWidth(getContext());
            float clipWidth = (width - AppTools.dp2px(getContext(), 46)) / 3f;

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout1.getLayoutParams();
            params.setMarginStart(AppTools.dp2px(getContext(), 15));
            params.width = (int) (clipWidth + 0.5f);
            params.height = (int) (clipWidth + 0.5f);
            layout1.setLayoutParams(params);

            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) layout2.getLayoutParams();
            params2.setMarginStart(AppTools.dp2px(getContext(), 8));
            params2.width = (int) (clipWidth + 0.5f);
            params2.height = (int) (clipWidth + 0.5f);
            layout2.setLayoutParams(params2);

            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) layout3.getLayoutParams();
            params3.setMarginStart(AppTools.dp2px(getContext(), 8));
            params3.width = (int) (clipWidth + 0.5f);
            params3.height = (int) (clipWidth + 0.5f);
            layout3.setLayoutParams(params3);
            showImg(img1, str.get(0));
            showImg(img2, str.get(1));
            showImg(img3, str.get(2));
        }
    }

    private void showImg(ImageView iv, String url) {
        Glide.with(getContext())
                .applyDefaultRequestOptions(new RequestOptions().placeholder(TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName()) ? R.drawable.drawable_default_tmpry : R.drawable.drawable_default_tmpry_night)
                        .error(TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName()) ? R.drawable.drawable_default_tmpry : R.drawable.drawable_default_tmpry_night)
                        .centerCrop()
                        .signature(new ObjectKey(url))
                )
                .asBitmap()
                .load(url)
                .error(Glide.with(getContext())
                        .applyDefaultRequestOptions(new RequestOptions().centerCrop()
                                .signature(new ObjectKey(url.contains("?") ? url.substring(0, url.indexOf("?")) : url)))
                        .asBitmap()
                        .load(url.contains("?") ? url.substring(0, url.indexOf("?")) : url))
                .into(iv);
    }

    /*
    * String url = str.get(position);
                if (url.contains(".gif")) {
                    if (url.contains("?")) {
                        url = url.substring(0, url.indexOf("?"));
                    }
                }
                Glide.with(getContext())
                        .applyDefaultRequestOptions(new RequestOptions().placeholder(TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName()) ? R.drawable.drawable_default_tmpry : R.drawable.drawable_default_tmpry_night)
                                .error(TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName()) ? R.drawable.drawable_default_tmpry : R.drawable.drawable_default_tmpry_night)
                                .centerCrop()
                                .signature(new ObjectKey(url))
                        )
                        .asBitmap()
                        .load(url)
                        .error(Glide.with(getContext())
                                .applyDefaultRequestOptions(new RequestOptions().centerCrop()
                                        .signature(new ObjectKey(url.contains("?") ? url.substring(0, url.indexOf("?")) : url)))
                                .asBitmap()
                                .load(url.contains("?") ? url.substring(0, url.indexOf("?")) : url))
                        .into(iv);
                iv.setOnClickListener(view -> {
                    if (mClickListener != null) {
                        mClickListener.onClick(position);
                    }
                });
    * */

    /*
     * 是否是白天模式
     * @return true 白天 else 夜間
     */
    private boolean isDay() {
        return TextUtils.isEmpty(SkinCompatManager.getInstance().getCurSkinName());
    }
}
