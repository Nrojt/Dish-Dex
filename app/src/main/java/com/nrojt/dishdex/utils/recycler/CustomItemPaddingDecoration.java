package com.nrojt.dishdex.utils.recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class CustomItemPaddingDecoration extends RecyclerView.ItemDecoration {
    private final int padding;

    public CustomItemPaddingDecoration(int padding) {
        this.padding = padding;
    }

    //This method is called for each item in the recycler view to add padding to the top and bottom of each item
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = padding;
        outRect.bottom = padding;
    }
}
