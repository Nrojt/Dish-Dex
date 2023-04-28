package com.nrojt.dishdex.backend;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Recipe implements Parcelable {
    private String recipeTitle = "";
    private String recipeIngredients = "";
    private String recipeInstructions = "";
    private String recipeNotes = "";
    private String recipeUrl = "";
    private int recipeID = -1;
    private int recipeCookingTime = 0;
    private int recipeServings = 0;
    private boolean isSupported = true;

    private ArrayList<Category> categories;

    //TODO: favorite recipes system

    public Recipe(String recipeTitle, String recipeIngredients, String recipeInstructions, String recipeNotes, String recipeUrl, int recipeID, int recipeCookingTime, int recipeServings, boolean isSupported, ArrayList<Category> categories) {
        this.recipeTitle = recipeTitle;
        this.recipeIngredients = recipeIngredients;
        this.recipeInstructions = recipeInstructions;
        this.recipeNotes = recipeNotes;
        this.recipeUrl = recipeUrl;
        this.recipeID = recipeID;
        this.recipeCookingTime = recipeCookingTime;
        this.recipeServings = recipeServings;
        this.isSupported = isSupported;
        this.categories = categories;
    }

    public Recipe(){}

    protected Recipe(Parcel in) {
        recipeTitle = in.readString();
        recipeIngredients = in.readString();
        recipeInstructions = in.readString();
        recipeNotes = in.readString();
        recipeUrl = in.readString();
        recipeID = in.readInt();
        recipeCookingTime = in.readInt();
        recipeServings = in.readInt();
        isSupported = in.readByte() != 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public String getRecipeInstructions() {
        return recipeInstructions;
    }

    public String getRecipeNotes() {
        return recipeNotes;
    }

    public String getRecipeUrl() {
        return recipeUrl;
    }

    public int getRecipeID() {
        return recipeID;
    }

    public int getRecipeCookingTime() {
        return recipeCookingTime;
    }

    public int getRecipeServings() {
        return recipeServings;
    }

    public boolean isSupported() {
        return isSupported;
    }
    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category category){
        categories.add(category);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(recipeTitle);
        dest.writeString(recipeIngredients);
        dest.writeString(recipeInstructions);
        dest.writeString(recipeNotes);
        dest.writeString(recipeUrl);
        dest.writeInt(recipeID);
        dest.writeInt(recipeCookingTime);
        dest.writeInt(recipeServings);
        dest.writeByte((byte) (isSupported ? 1 : 0));
    }
}
