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
    private Context context;
    private ArrayList<String> recipeTitles;
    private ArrayList<String> recipeIngredients;
    private ArrayList<String> recipeInstructions;
    private ArrayList<String> recipeNotes;
    private ArrayList<String> recipeUrls;

    private ArrayList<Integer> recipeCookingTimes;
    private ArrayList<Integer> recipeServings;
    private ArrayList<Integer> recipeIds;

    public CustomAdapter(Context context, ArrayList<String> recipeTitles, ArrayList<String> recipeIngredients, ArrayList<String> recipeInstructions, ArrayList<String> recipeNotes, ArrayList<String> recipeUrls, ArrayList<Integer> recipeCookingTimes, ArrayList<Integer> recipeServings, ArrayList<Integer> recipeIds) {
        this.context = context;
        this.recipeTitles = recipeTitles;
        this.recipeIngredients = recipeIngredients;
        this.recipeInstructions = recipeInstructions;
        this.recipeNotes = recipeNotes;
        this.recipeUrls = recipeUrls;
        this.recipeCookingTimes = recipeCookingTimes;
        this.recipeServings = recipeServings;
        this.recipeIds = recipeIds;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.saved_recipes_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recipeTitleText.setText(recipeTitles.get(position));
        holder.recipeCookingTimeText.setText(String.valueOf(recipeCookingTimes.get(position))+ " mins");
        holder.recipeServingsText.setText(String.valueOf(recipeServings.get(position))+ " servings");
    }

    @Override
    public int getItemCount() {
        return recipeTitles.size();
    }

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
