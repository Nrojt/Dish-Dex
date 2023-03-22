package com.nrojt.dishdex.utils.recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ItemPaddingDecoration extends RecyclerView.ItemDecoration {
    private int padding;

    public ItemPaddingDecoration(int padding) {
        this.padding = padding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = padding;
        outRect.bottom = padding;
        outRect.left = padding;
        outRect.right = padding;
    }
}
