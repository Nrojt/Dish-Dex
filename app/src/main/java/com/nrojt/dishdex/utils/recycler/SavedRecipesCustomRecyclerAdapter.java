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

public class SavedRecipesCustomRecyclerAdapter extends RecyclerView.Adapter<SavedRecipesCustomRecyclerAdapter.MyViewHolder>{
    //Interface (polymorphism)
    private final RecyclerViewInterface listener;
    private final Context context;
    private final ArrayList<String> recipeTitles;
    private final ArrayList<Integer> recipeIDs;
    private final ArrayList<Integer> recipeCookingTimes;
    private final ArrayList<Integer> recipeServings;

    //Constructor
    public SavedRecipesCustomRecyclerAdapter(Context context, ArrayList<Integer> recipeIDs, ArrayList<String> recipeTitles, ArrayList<Integer> recipeCookingTimes, ArrayList<Integer> recipeServings, RecyclerViewInterface listener) {
        this.listener = listener;
        this.context = context;
        this.recipeTitles = recipeTitles;
        this.recipeCookingTimes = recipeCookingTimes;
        this.recipeServings = recipeServings;
        this.recipeIDs = recipeIDs;
    }

    //Inflate the view
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.saved_recipes_row, parent, false);
        return new MyViewHolder(view, listener);
    }

    //Setting the text for each recipe card
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recipeTitleText.setText(recipeTitles.get(position));
        holder.recipeCookingTimeText.setText(recipeCookingTimes.get(position) + " minutes");
        holder.recipeServingsText.setText(recipeServings.get(position) + " servings");
    }

    @Override
    public int getItemCount() {
        return recipeTitles.size();
    }

    //Inflate the layout for each recipe card
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView recipeServingsText, recipeTitleText, recipeCookingTimeText;
        CardView savedRecipesCardView;
        public MyViewHolder(@NonNull View itemView, final RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            recipeTitleText = itemView.findViewById(R.id.recipeTitleText);
            recipeCookingTimeText = itemView.findViewById(R.id.recipeCookingTimeText);
            recipeServingsText = itemView.findViewById(R.id.recipeServingsText);
            savedRecipesCardView = itemView.findViewById(R.id.savedRecipesCardView);

            savedRecipesCardView.setOnClickListener(new View.OnClickListener() {
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
}
