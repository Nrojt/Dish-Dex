package com.nrojt.dishdex.backend;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {
    private String recipeTitle = "";
    private String recipeIngredients = "";
    private String recipeInstructions = "";
    private String recipeNotes = "";
    private String recipeUrl = "";
    private int recipeID = -1;
    private int recipeCookingTime = 0;
    private int recipeServings = 0;
    private boolean isSupported;

    private ArrayList<Category> categories;

    //TODO: Add a list of categories to the recipe class

    public Recipe(String recipeTitle, String recipeIngredients, String recipeInstructions, String recipeNotes, String recipeUrl, int recipeID, int recipeCookingTime, int recipeServings, boolean isSupported) {
        this.recipeTitle = recipeTitle;
        this.recipeIngredients = recipeIngredients;
        this.recipeInstructions = recipeInstructions;
        this.recipeNotes = recipeNotes;
        this.recipeUrl = recipeUrl;
        this.recipeID = recipeID;
        this.recipeCookingTime = recipeCookingTime;
        this.recipeServings = recipeServings;
        this.isSupported = isSupported;
    }

    public Recipe(){}

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
}
