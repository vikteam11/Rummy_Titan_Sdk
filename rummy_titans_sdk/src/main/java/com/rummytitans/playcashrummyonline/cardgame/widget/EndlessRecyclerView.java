package com.rummytitans.playcashrummyonline.cardgame.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public final class EndlessRecyclerView extends RecyclerView {

    private final Handler handler = new Handler();
    private EndlessScrollListener endlessScrollListener;
    private LayoutManagerWrapper layoutManagerWrapper;
    private AdapterWrapper adapterWrapper;
    private final Runnable notifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            adapterWrapper.notifyDataSetChanged();
        }
    };
    private View progressView;
    private boolean refreshing;
    private int threshold = 1;

    public EndlessRecyclerView(Context context) {
        this(context, null);
    }

    public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Adapter getAdapter() {
        return adapterWrapper.getAdapter();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        //noinspection unchecked
        adapterWrapper = new AdapterWrapper(adapter);
        super.setAdapter(adapterWrapper);
    }

    /**
     * @param layout instances of {@link LinearLayoutManager} only
     */
    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        layoutManagerWrapper = layout == null ? null : new LayoutManagerWrapper(layout);
        super.setLayoutManager(layout);
    }

    /**
     * Sets {@link Pager} to use with the view.
     *
     * @param pager pager to set or {@code null} to clear current pager
     */
    public void setPager(Pager pager) {
        if (pager != null) {
            endlessScrollListener = new EndlessScrollListener(pager);
            endlessScrollListener.setThreshold(threshold);
            addOnScrollListener(endlessScrollListener);
        } else if (endlessScrollListener != null) {
            removeOnScrollListener(endlessScrollListener);
            endlessScrollListener = null;
        }
    }

    /**
     * Sets threshold to use. Only positive numbers are allowed. This value is used to determine if
     * loading should start when user scrolls the view down. Default value is 1.
     *
     * @param threshold positive number
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
        if (endlessScrollListener != null) {
            endlessScrollListener.setThreshold(threshold);
        }
    }

    /**
     * Sets progress view to show on the bottom of the list when loading starts.
     *
     * @param layoutResId layout resource ID
     */
    public void setProgressView(int layoutResId) {
        setProgressView(LayoutInflater
                .from(getContext())
                .inflate(layoutResId, this, false));
    }

    /**
     * Sets progress view to show on the bottom of the list when loading starts.
     *
     * @param view the view
     */
    private void setProgressView(View view) {
        progressView = view;
    }

    /**
     * @return {@code true} if progress view is shown
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    /**
     * If async operation completed you may want to call this method to hide progress view.
     *
     * @param refreshing {@code true} if list is currently refreshing, {@code false} otherwise
     */
    public void setRefreshing(boolean refreshing) {
        if (this.refreshing == refreshing) {
            return;
        }
        this.refreshing = refreshing;
        new Handler().post(this::notifyDataSetChanged);
    }

    private void notifyDataSetChanged() {
        if (isComputingLayout()) {
            handler.post(notifyDataSetChangedRunnable);
        } else {
            adapterWrapper.notifyDataSetChanged();
        }
    }

    /**
     * Pager interface.
     */
    public interface Pager {
        /**
         * @return {@code true} if pager should load new page
         */
        boolean shouldLoad();

        /**
         * Starts loading operation.
         */
        void loadNextPage();
    }

    private static final class LayoutManagerWrapper {

        @NonNull
        final LayoutManager layoutManager;

        @NonNull
        private final LayoutManagerResolver resolver;

        LayoutManagerWrapper(@NonNull LayoutManager layoutManager) {
            this.layoutManager = layoutManager;
            this.resolver = getResolver(layoutManager);
        }

        @NonNull
        private static LayoutManagerResolver getResolver(@NonNull LayoutManager layoutManager) {
            if (layoutManager instanceof LinearLayoutManager) {
                return layoutManager1 -> ((LinearLayoutManager) layoutManager1).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                return layoutManager12 -> {
                    int[] lastVisibleItemPositions =
                            ((StaggeredGridLayoutManager) layoutManager12)
                                    .findLastVisibleItemPositions(null);
                    int lastVisibleItemPosition = lastVisibleItemPositions[0];
                    for (int i = 1; i < lastVisibleItemPositions.length; ++i) {
                        if (lastVisibleItemPosition < lastVisibleItemPositions[i]) {
                            lastVisibleItemPosition = lastVisibleItemPositions[i];
                        }
                    }
                    return lastVisibleItemPosition;
                };
            } else {
                throw new IllegalArgumentException("unsupported layout manager: " + layoutManager);
            }
        }

        int findLastVisibleItemPosition() {
            return resolver.findLastVisibleItemPosition(layoutManager);
        }

        private interface LayoutManagerResolver {
            int findLastVisibleItemPosition(@NonNull LayoutManager layoutManager);
        }
    }

    private final class EndlessScrollListener extends OnScrollListener {

        private final Pager pager;

        private int threshold = 3;

        EndlessScrollListener(Pager pager) {
            if (pager == null) {
                throw new NullPointerException("pager is null");
            }
            this.pager = pager;
        }

        @Override
        public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
            int lastVisibleItemPosition = layoutManagerWrapper.findLastVisibleItemPosition();
            assert getAdapter() != null;
            int listSize = getAdapter().getItemCount();

            if ((listSize - lastVisibleItemPosition) <= threshold) {
                if (pager.shouldLoad()){
                    setRefreshing(true);
                    pager.loadNextPage();
                }
            }
        }


        void setThreshold(int threshold) {
            if (threshold <= 0) {
                throw new IllegalArgumentException("illegal threshold: " + threshold);
            }
            this.threshold = threshold;
        }
    }

    private final class AdapterWrapper extends Adapter<ViewHolder> {

        private static final int PROGRESS_VIEW_TYPE = -1;

        private final Adapter<ViewHolder> adapter;

        private ProgressViewHolder progressViewHolder;

        AdapterWrapper(Adapter<ViewHolder> adapter) {
            if (adapter == null) {
                throw new NullPointerException("adapter is null");
            }
            this.adapter = adapter;
            setHasStableIds(adapter.hasStableIds());
        }

        @Override
        public int getItemCount() {
            return adapter.getItemCount() + (refreshing && progressView != null ? 1 : 0);
        }

        @Override
        public long getItemId(int position) {
            return position == adapter.getItemCount() ? NO_ID : adapter.getItemId(position);
        }

        @Override
        public int getItemViewType(int position) {
            return refreshing & position == adapter.getItemCount() ? PROGRESS_VIEW_TYPE :
                    adapter.getItemViewType(position);
        }

        @Override
        public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            if (position < adapter.getItemCount()) {
                adapter.onBindViewHolder(holder, position);
            }
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return viewType == PROGRESS_VIEW_TYPE ? progressViewHolder = new ProgressViewHolder() :
                    adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onDetachedFromRecyclerView(@NotNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public boolean onFailedToRecycleView(@NotNull ViewHolder holder) {
            return holder == progressViewHolder || adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void onViewAttachedToWindow(@NotNull ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NotNull ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewRecycled(holder);
        }

        @Override
        public void registerAdapterDataObserver(@NotNull AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(@NotNull AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
            adapter.unregisterAdapterDataObserver(observer);
        }

        Adapter<ViewHolder> getAdapter() {
            return adapter;
        }

        private final class ProgressViewHolder extends ViewHolder {
            ProgressViewHolder() {
                super(progressView);
            }
        }
    }
}