package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils;

public class MovieUserScoreView extends CardView {

    private ImageView mIvCover;
    private TextView mTvName;
    private TextView mTvScoreCount;
    private Drawable mDrawable;
    private String mRanking;
    private float size = 0;
    private float margin = 0;//13

    public MovieUserScoreView(@NonNull Context context) {
        this(context, null, 0);
    }

    public MovieUserScoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieUserScoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MovieUserScoreView);
        mDrawable = array.getDrawable(R.styleable.MovieUserScoreView_scoreResid);
        mRanking = array.getString(R.styleable.MovieUserScoreView_scoreRanking);
        size = array.getDimension(R.styleable.MovieUserScoreView_imgSize, size);
        margin = array.getDimension(R.styleable.MovieUserScoreView_margin_Bottom, margin);
        array.recycle();
        initView();
    }

    private void initView() {
        setVisibility(INVISIBLE);
        View.inflate(getContext(), R.layout.layout_movie_user_score_view, this);
        mIvCover = findViewById(R.id.round_img2);
        mTvName = findViewById(R.id.tv_UserName);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvCover.getLayoutParams();
        params.width = (int) size;
        params.height = (int) size;
        params.bottomMargin = (int) margin;
        params.addRule(RelativeLayout.ABOVE, R.id.tv_UserName);
        mTvScoreCount = findViewById(R.id.tv_ScoreCount);
        View relativeFlag = findViewById(R.id.relativeFlag);
        TextView tvFlag = findViewById(R.id.tvFlag);
        if (!TextUtils.isEmpty(mRanking)) {
            tvFlag.setText(mRanking);
        }
        if (mDrawable != null) {
            relativeFlag.setBackground(mDrawable);
        }
    }

    /**
     * 设置 显示的头像 昵称 数量
     *
     * @param url
     * @param title
     * @param count
     */
    public void setData(GlideJudeUtils glide, String url, String title, String count) {
        setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(url)) {
            glide.loadGlide(url, mIvCover, R.mipmap.icon_user_default, glide.getLastModified(url));
            //            GlideUtil.load((Activity) getContext(), url, mIvCover, R.mipmap.icon_user_default);
        }
        mTvName.setText(title);
        mTvScoreCount.setText(count + "条影评");
    }

}
