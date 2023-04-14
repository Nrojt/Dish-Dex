package com.nrojt.dishdex.utils.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;

import java.util.ArrayList;

public class SavedCategoriesCustomRecyclerAdapter extends RecyclerView.Adapter<SavedCategoriesCustomRecyclerAdapter.MyViewHolder> {
    private final RecyclerViewInterface listener;
    private final Context context;
    private final ArrayList<String> categoryNames;
    private final ArrayList<Integer> categoryIDs;

    public SavedCategoriesCustomRecyclerAdapter(Context context, ArrayList<Integer> categoryIDs, ArrayList<String> categoryNames, RecyclerViewInterface listener) {
        this.listener = listener;
        this.context = context;
        this.categoryNames = categoryNames;
        this.categoryIDs = categoryIDs;
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
        holder.categoryTitleText.setText(categoryNames.get(position));
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
        return categoryNames.size();
    }
}
