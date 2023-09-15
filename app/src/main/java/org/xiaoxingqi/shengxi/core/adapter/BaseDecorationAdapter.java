package org.xiaoxingqi.shengxi.core.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.DataTitleBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzm on 2017/11/28.
 */

public abstract class BaseDecorationAdapter<T extends DataTitleBean, H extends BaseAdapterHelper> extends RecyclerView.Adapter<BaseAdapterHelper> implements View.OnClickListener {
    private Context mContext;
    private List<T> data;
    private final int ITEM_TYPE_NORMAL = 0;
    private final int ITEM_TYPE_HORZONTAL = 1;
    private final int ITEM_TYPE_FOOT = 2;
    private final int ITEM_TYPE_HEAD = 3;
    private int layoutSpe;
    private int layoutNor;
    private boolean isHaveFoot = false;
    private View mFootView;
    private int mFootViewCount = 0;
    private View headView;
    private boolean hasHeadView = false;
    private int headCount = 0;

    public BaseDecorationAdapter(Context context, int layoutSpe, int normalLayoutID, List<T> data) {
        mContext = context;
        this.data = data == null ? new ArrayList<>() : data;
        this.layoutSpe = layoutSpe;
        layoutNor = normalLayoutID;//item_title_text
    }

    public BaseDecorationAdapter(Context context, int layoutSpe, int normalLayoutID, List<T> data, View headView) {
        this.headView = headView;
        mContext = context;
        this.data = data == null ? new ArrayList<>() : data;
        this.layoutSpe = layoutSpe;
        layoutNor = normalLayoutID;//item_title_text
        if (headView != null) {
            hasHeadView = true;
            headCount = 1;
        }
    }


    public BaseDecorationAdapter(Context context, int layoutSpe, int normalLayoutID, List<T> data, View headView, View footView) {
        this.headView = headView;
        mContext = context;
        this.data = data == null ? new ArrayList<>() : data;
        this.layoutSpe = layoutSpe;
        layoutNor = normalLayoutID;//item_title_text
        if (headView != null) {
            hasHeadView = true;
            headCount = 1;
        }
        if (footView != null) {
            this.mFootView = footView;
            isHaveFoot = true;
            mFootViewCount = 1;
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (hasHeadView) {
            if (isHeadView(position)) {
                return ITEM_TYPE_HEAD;
            }
        }
        if (isHaveFoot) {
            if (isFootView(position))
                return ITEM_TYPE_FOOT;
        }
        if (data.get(position - headCount).isSelect()) {
            return ITEM_TYPE_HORZONTAL;
        }
        return super.getItemViewType(position);
    }


    public boolean isHeadView(int position) {
        return position == 0;
    }

    /**
     * 判断是否尾部View
     */
    public boolean isFootView(int position) {
        if (data.size() == 0) {
            return position == data.size();
        }
        return position == data.size();
    }

    @Override
    public BaseAdapterHelper onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ITEM_TYPE_HEAD == viewType) {
            return new BaseAdapterHelper(headView);
        }
        if (ITEM_TYPE_FOOT == viewType) {
            return new BaseAdapterHelper(mFootView);
        }
        if (ITEM_TYPE_HORZONTAL == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutSpe, parent, false);
            BaseAdapterHelper vh = new BaseAdapterHelper(view);
            return vh;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutNor, parent, false);
            BaseAdapterHelper vh = new BaseAdapterHelper(view);
            view.setOnClickListener(this);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(BaseAdapterHelper helper, int position) {
        if (isHaveFoot && isFootView(position) || (hasHeadView && isHeadView(position))) {
            return;
        }
        helper.itemView.setTag(position - headCount);
        T item = getItem(position - headCount);
        convert((H) helper, item);
    }

    protected abstract void convert(H helper, T item);

    public boolean isHorzontal(int position) {
        if (data.size() == 0) {
            return false;
        }
        return data.get(position).isSelect();
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    private OnItemClickListener mItemClickListener;

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public T getItem(int position) {
        if (position >= data.size()) return null;
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size() + mFootViewCount + headCount;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public enum ELoadState {
        READY,//准备
        LOADING,//加载中
        EMPTY, //空数据
        GONE,//不可见
        NULLDATA//空数据
    }

    private void setFootView(RecyclerView.LayoutManager manager, View footView) {
        if (footView != null) {
            mFootViewCount = 1;
            mFootView = footView;
            isHaveFoot = true;
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager mManager = ((GridLayoutManager) manager);
                mManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int count;
                        if (isFootView(position) || isHorzontal(position)) {
                            count = mManager.getSpanCount();
                        } else {
                            count = 1;
                        }
                        return count;
                    }
                });
            }
        }
    }

    private ELoadState mLoadState = ELoadState.GONE;
    private int mLastVisibleItem;

    public void setLoadMoreEnable(RecyclerView recyclerView, final RecyclerView.LayoutManager manager, View footView) {
        setFootView(manager, footView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mLastVisibleItem + 1 == BaseDecorationAdapter.this.getItemCount()) {
                    if (mOnLoadListener == null) return;
                    if (mLoadState == ELoadState.READY) {
                        mOnLoadListener.onLoadMore();
                        setLoadStatue(ELoadState.LOADING);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager instanceof LinearLayoutManager || manager instanceof GridLayoutManager) {
                    mLastVisibleItem = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                }
            }
        });
    }

    private boolean enableLoadmore;

    public void enableLoadMore(boolean loadmore) {
        enableLoadmore = loadmore;
    }

    private BaseQuickAdapter.OnLoadListener mOnLoadListener;

    public void setOnLoadListener(BaseQuickAdapter.OnLoadListener mOnLoadListener) {
        this.mOnLoadListener = mOnLoadListener;
    }

    public interface OnLoadListener {
        void onLoadMore();
    }

    public void setLoadStatue(ELoadState loadStatue) {
        mLoadState = loadStatue;
        final TextView msg = mFootView.findViewById(R.id.text_msg);
        View mLinearProgress = mFootView.findViewById(R.id.linearProgress);
        View progress = mFootView.findViewById(R.id.progress);
        View emptyView = mFootView.findViewById(R.id.relative_Empty);
        switch (loadStatue) {
            case GONE:
                msg.setText(mContext.getString(R.string.string_load_more));
                mFootView.setVisibility(View.GONE);
                break;
            case LOADING:
                msg.setText(mContext.getString(R.string.string_loading_more));
                progress.setVisibility(View.VISIBLE);
                break;
            case READY:
                msg.setText(mContext.getString(R.string.string_load_more));
                mLinearProgress.setVisibility(View.VISIBLE);
                try {
                    emptyView.setVisibility(View.GONE);
                } catch (Exception e) {
                }
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                msg.setText(mContext.getString(R.string.string_load_empty));
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
            case NULLDATA:
                mLinearProgress.setVisibility(View.GONE);
                try {
                    emptyView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
        }
    }


}
