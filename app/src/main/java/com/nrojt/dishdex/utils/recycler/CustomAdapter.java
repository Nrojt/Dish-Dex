package com.nrojt.dishdex.utils.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.R;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>{
    private final Context context;
    private final ArrayList<String> recipeTitles;

    private final ArrayList<Integer> recipeCookingTimes;
    private final ArrayList<Integer> recipeServings;

    //Constructor
    public CustomAdapter(Context context, ArrayList<String> recipeTitles, ArrayList<Integer> recipeCookingTimes, ArrayList<Integer> recipeServings) {
        this.context = context;
        this.recipeTitles = recipeTitles;
        this.recipeCookingTimes = recipeCookingTimes;
        this.recipeServings = recipeServings;
    }

    //Inflate the view
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.saved_recipes_row, parent, false);
        return new MyViewHolder(view);
    }

    //Setting the text for each recipe card
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recipeTitleText.setText(recipeTitles.get(position));
        holder.recipeCookingTimeText.setText(recipeCookingTimes.get(position) + " mins");
        holder.recipeServingsText.setText(recipeServings.get(position) + " servings");
    }

    @Override
    public int getItemCount() {
        return recipeTitles.size();
    }

    //Inflate the layout for each recipe card
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView recipeServingsText, recipeTitleText, recipeCookingTimeText;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTitleText = itemView.findViewById(R.id.recipeTitleText);
            recipeCookingTimeText = itemView.findViewById(R.id.recipeCookingTimeText);
            recipeServingsText = itemView.findViewById(R.id.recipeServingsText);
        }
    }
}
