package org.xiaoxingqi.shengxi.wedgit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.xiaoxingqi.shengxi.model.BaseSearchBean;
import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.utils.AppTools;

public class DynamicMovieView extends BaseLayout {

    private ImageView mIvMovieCover;
    private TextView mTvName;
    private TextView mTvYears;
    private TextView mTvDesc;
    private TextView mTvActors;
    private View cardLayout;

    public DynamicMovieView(@NonNull Context context) {
        this(context, null, 0);
    }

    public DynamicMovieView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicMovieView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mIvMovieCover = findViewById(R.id.ivMovieCover);
        mTvName = findViewById(R.id.tv_MovieName);
        mTvYears = findViewById(R.id.tv_Years);
        mTvDesc = findViewById(R.id.tv_MovieDesc);
        mTvActors = findViewById(R.id.tv_actors);
        cardLayout = findViewById(R.id.cardLayout);
    }

    public void setData(BaseSearchBean bean, int resourceType) {
        if (resourceType == 1 || resourceType == 2) {
            ViewGroup.LayoutParams params = cardLayout.getLayoutParams();
            params.height = AppTools.dp2px(getContext(), 133);
        } else {
            ViewGroup.LayoutParams params = cardLayout.getLayoutParams();
            params.height = AppTools.dp2px(getContext(), 96);
        }
        Glide.with(getContext())
                .applyDefaultRequestOptions(new RequestOptions().centerCrop().error(R.drawable.drawable_default_tmpry))
                .load(resourceType == 1 ? bean.getMovie_poster() : resourceType == 2 ? bean.getBook_cover() : bean.getSong_cover())
                .into(mIvMovieCover);
        if (resourceType == 1) {
            mTvName.setText(bean.getMovie_title());
            mTvDesc.setText(AppTools.array2String(bean.getMovie_area()) + "/" + AppTools.array2String(bean.getMovie_type()) + "/" + bean.getMovie_len() + "分钟");
            mTvActors.setText(AppTools.array2String(bean.getMovie_starring()));
        } else if (resourceType == 2) {
            mTvName.setText(bean.getBook_name());
            mTvDesc.setText(bean.getBook_author() + "/" + bean.getBook_publisher());
            mTvActors.setText(AppTools.array2String(bean.getMovie_starring()));
        } else {
            mTvName.setText(bean.getSong_name());
            mTvDesc.setText(bean.getSong_singer() + "/" + bean.getAlbum_name());
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_dynamic_movie;
    }
}
