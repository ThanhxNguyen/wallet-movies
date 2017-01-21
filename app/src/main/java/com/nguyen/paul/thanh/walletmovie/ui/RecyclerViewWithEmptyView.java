package com.nguyen.paul.thanh.walletmovie.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custome implementation of recyclerview. it will display a placeholder view when the list is empty
 */

public class RecyclerViewWithEmptyView extends RecyclerView {

    private View mPlaceholderView;

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            updateViewIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            updateViewIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            updateViewIfEmpty();
        }
    };

    public RecyclerViewWithEmptyView(Context context) {
        super(context);
    }

    public RecyclerViewWithEmptyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewWithEmptyView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateViewIfEmpty() {
        if(mPlaceholderView != null && getAdapter() != null) {
            //get number of items in the list, if empty set to true, otherwise false
            boolean shouldDisplayPlaceholderView = getAdapter().getItemCount() == 0;

            //if the list is empty, display placeholder view or hide it otherwise
            mPlaceholderView.setVisibility( shouldDisplayPlaceholderView ? VISIBLE : GONE );
            setVisibility( shouldDisplayPlaceholderView ? GONE : VISIBLE );
        }
    }

    @Override
    public void setAdapter(Adapter newAdapter) {
        Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(newAdapter);
        if (newAdapter != null) {
            newAdapter.registerAdapterDataObserver(observer);
        }

        updateViewIfEmpty();
    }

    public void setPlaceholderView(View view) {
        mPlaceholderView = view;
        updateViewIfEmpty();
    }
}
