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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianghejie on 15/8/8.
 */
public abstract class BaseQuickAdapter<T, H extends BaseAdapterHelper> extends RecyclerView.Adapter<BaseAdapterHelper> implements View.OnClickListener {
    protected static final String TAG = BaseQuickAdapter.class.getSimpleName();

    protected final Context context;

    protected final int layoutResId;

    protected final List<T> data;

    protected boolean displayIndeterminateProgress = false;

    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongClickListener mOnItemLongClickListener = null;

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    /**
     * Create a QuickAdapter.
     *
     * @param context     The context.
     * @param layoutResId The layout resource id of each item.
     */
    public BaseQuickAdapter(Context context, int layoutResId) {
        this(context, layoutResId, null);
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param context     The context.
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public BaseQuickAdapter(Context context, int layoutResId, List<T> data) {
        this.data = data == null ? new ArrayList<T>() : data;
        this.context = context;
        this.layoutResId = layoutResId;
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param context     The context.
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     * @param headView    头布局
     */
    private View mHeadView;
    private boolean isHeadView;
    private boolean isHaveHeadView = false;
    //是添加的头布局
    private static final int ITEM_VIEW_TYPE_HEADER = 1;
    //是正常显示的View
    private static final int ITEM_VIEW_TYPE_ITEM = 0;
    private static final int ITEM_VIEW_TYPE_BOTTOM = 2;
    private boolean isHaveFoot = false;
    private View mFootView;

    public void setIsHeadView(boolean isHeadView) {
        this.isHeadView = isHeadView;
    }

    private int mHeadViewCount = 0;
    private int mFootViewCount = 0;

    public void setIsHaveHeadView(boolean isHaveHeadView) {
        this.isHaveHeadView = isHaveHeadView;
        if (isHaveHeadView) {
            mHeadViewCount = 1;
        }
    }

    public void setIsHaveFoot(boolean view) {
        if (view) {
            mFootViewCount = 1;
            isHaveFoot = true;
        } else {
            mFootViewCount = 0;
            isHaveFoot = false;
        }
    }

    /**
     * 判断是否尾部View
     */
    public boolean isFootView(int position) {
        if (data.size() == 0) {
            return position == mHeadViewCount + data.size();
        }
        return position == data.size() + mHeadViewCount;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHaveHeadView) {
            if (isHeader(position)) {
                return ITEM_VIEW_TYPE_HEADER;
            }
        }
        if (isHaveFoot) {
            if (isFootView(position))
                return ITEM_VIEW_TYPE_BOTTOM;
        }
        return super.getItemViewType(position);
    }

    public BaseQuickAdapter(Context context, int layoutResId, List<T> data, View headView) {
        this.data = data == null ? new ArrayList<T>() : data;
        this.context = context;
        this.layoutResId = layoutResId;
        this.mHeadView = headView;
        setIsHaveHeadView(true);
    }

    public BaseQuickAdapter(Context context, int layoutResId, List<T> data, View headView, View footView) {
        this.data = data == null ? new ArrayList<T>() : data;
        this.context = context;
        this.layoutResId = layoutResId;
        mFootView = footView;
        if (footView != null) {
            setIsHaveFoot(true);
        }
        if (headView != null) {
            this.mHeadView = headView;
            setIsHaveHeadView(true);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + mHeadViewCount + mFootViewCount;
    }

    public T getItem(int position) {
        if (position >= data.size()) return null;
        return data.get(position);
    }

    @Override
    public BaseAdapterHelper onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            return new BaseAdapterHelper(mHeadView);
        }
        if (viewType == ITEM_VIEW_TYPE_BOTTOM) {
            return new BaseAdapterHelper(mFootView);
        }

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        view.setOnClickListener(this);
        BaseAdapterHelper vh = new BaseAdapterHelper(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(BaseAdapterHelper helper, int position) {
        if (isHaveHeadView && isHeader(position)) {
            return;
        }
        if (isHaveFoot && isFootView(position))
            return;
        helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(v, (int) v.getTag());
                    return true;
                } else {
                    return false;
                }
            }
        });
        helper.itemView.setTag(position - mHeadViewCount);
        T item = getItem(position - mHeadViewCount);
        convert((H) helper, item);
    }

    /**
     * 移除头部View
     */
    public void removeHeard(boolean isAttach) {
        if (mHeadView == null)
            return;
        if (isAttach) {//显示
            isHaveHeadView = true;
            mHeadViewCount = 1;
        } else {//移除
            isHaveHeadView = false;
            mHeadViewCount = 0;
        }
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    protected abstract void convert(H helper, T item);

    public void notifyBFootView(boolean isAttach) {
        setIsHaveFoot(isAttach);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
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
                        int size = isFootView(position) ? mManager.getSpanCount() : 1;
                        return size;
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
                if (mLastVisibleItem + 1 == BaseQuickAdapter.this.getItemCount()) {
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
                if (manager instanceof LinearLayoutManager /*|| mLayoutManager instanceof GridLayoutManager*/) {
                    mLastVisibleItem = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                }
            }
        });
    }

    private boolean enableLoadmore;

    public void enableLoadMore(boolean loadmore) {
        enableLoadmore = loadmore;
    }

    private OnLoadListener mOnLoadListener;

    public void setOnLoadListener(OnLoadListener mOnLoadListener) {
        this.mOnLoadListener = mOnLoadListener;
    }

    public interface OnLoadListener {
        void onLoadMore();
    }

    public ELoadState getLoadState() {
        return mLoadState;
    }

    public void setLoadStatue(ELoadState loadStatue) {
        mLoadState = loadStatue;
        final TextView msg = mFootView.findViewById(R.id.text_msg);
        View mLinearProgress = mFootView.findViewById(R.id.linearProgress);
        View progress = mFootView.findViewById(R.id.progress);
        View emptyView = mFootView.findViewById(R.id.relative_Empty);
        try {
            emptyView.setVisibility(View.GONE);
        } catch (Exception e) {
        }
        switch (loadStatue) {
            case GONE:
                msg.setText(context.getString(R.string.string_load_more));
                mFootView.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                break;
            case LOADING:
                msg.setText(context.getString(R.string.string_loading_more));
                progress.setVisibility(View.VISIBLE);
                break;
            case READY:
                msg.setText(context.getString(R.string.string_load_more));
                mLinearProgress.setVisibility(View.VISIBLE);
                try {
                    emptyView.setVisibility(View.GONE);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                msg.setText("");
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
            case NULLDATA:
                mLinearProgress.setVisibility(View.GONE);
                try {
                    emptyView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                progress.setVisibility(View.GONE);
                mFootView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
