package com.nrojt.dishdex.utils.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.viewmodels.MainActivityViewModel;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;

import java.util.ArrayList;

public class SavedCategoriesCustomRecyclerAdapter extends RecyclerView.Adapter<SavedCategoriesCustomRecyclerAdapter.MyViewHolder> {
    private final RecyclerViewInterface listener;
    private final Context context;
    private final ArrayList<Category> categories;
    private int fontSizeTitle;

    public SavedCategoriesCustomRecyclerAdapter(Context context, ArrayList<Category> categories, RecyclerViewInterface listener, int fontSizeTitle) {
        this.listener = listener;
        this.context = context;
        this.categories = categories;
        this.fontSizeTitle = fontSizeTitle;
    }

    @NonNull
    @Override
    public SavedCategoriesCustomRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.saved_categories_row, parent, false);
        return new SavedCategoriesCustomRecyclerAdapter.MyViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedCategoriesCustomRecyclerAdapter.MyViewHolder holder, int position) {
        holder.categoryTitleText.setText(categories.get(position).getCategoryName());
        holder.categoryTitleText.setTextSize(fontSizeTitle);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitleText;
        CardView savedCategoriesCardView;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            categoryTitleText = itemView.findViewById(R.id.categoryTitleText);
            savedCategoriesCardView = itemView.findViewById(R.id.savedCategoriesCardView);

            savedCategoriesCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
