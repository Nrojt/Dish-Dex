package com.nrojt.dishdex.backend.viewmodels;

import android.content.Context;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;

import java.time.LocalTime;
import java.util.ArrayList;

public class HomePageFragmentViewModel extends ViewModel {
    private final MutableLiveData<Recipe> recipeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> timeCategoryIDLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> timeRecipeIDLiveData = new MutableLiveData<>();
    private Context context;

    public HomePageFragmentViewModel(Context context) {
        this.context = context;
        loadRecipe();
        System.out.println("HomePageFragmentViewModel created");
    }

    public void setRecipe(Recipe recipe){
        if(recipe == null){
            recipe = new Recipe();
        }
        recipeLiveData.setValue(recipe);
    }

    public void loadRecipe() {
        Recipe recipe = getRandomRecipeBasedOnTime();
        recipeLiveData.setValue(recipe);
    }

    public void setTimeCategoryID(){
        LocalTime currentTime = LocalTime.now();
        LocalTime breakfastTime = LocalTime.of(9, 0);
        LocalTime lunchTime = LocalTime.of(12, 0);
        LocalTime dinnerTime = LocalTime.of(18, 0);
        LocalTime lateNightTime = LocalTime.of(22, 0);
        LocalTime earlyMorningTime = LocalTime.of(5, 0); //We dont want to suggest breakfast recipes after 0:00 and before 5:00

        int timeCategoryID;

        if (currentTime.isBefore(breakfastTime) && currentTime.isAfter(earlyMorningTime)) {
            timeCategoryID = 1;
        } else if (currentTime.isBefore(lunchTime)) {
            timeCategoryID = 2;
        } else if (currentTime.isBefore(dinnerTime)) {
            timeCategoryID = 3;
        } else if (currentTime.isBefore(lateNightTime)) {
            timeCategoryID = 4;
        } else {
            //if the time is after 22:00
            timeCategoryID = 5;
        }
        timeCategoryIDLiveData.setValue(timeCategoryID);
    }

    public void setTimeRecipeID(int timeRecipeID){
        if(timeRecipeID <= 0){
            timeRecipeID = 1;
        }
        timeRecipeIDLiveData.setValue(timeRecipeID);
    }


    public LiveData<Recipe> getRecipeLiveData() {
        return recipeLiveData;
    }

    public LiveData<Integer> getTimeCategoryIDLiveData() {
        return timeCategoryIDLiveData;
    }

    //getting a random recipeID based on the time of day
    private Recipe getRandomRecipeBasedOnTime() {
        setTimeCategoryID();
        MyDatabaseHelper db = MyDatabaseHelper.getInstance(context);

        int timeRecipeID = db.getRandomRecipeIDWhereCategoryID(getTimeCategoryIDLiveData().getValue());

        Recipe recipe = null;

        if (timeRecipeID != -1) {
            Cursor cursor = db.readAllDataFromSavedRecipesWhereRecipeID(timeRecipeID);
            cursor.moveToFirst();
            ArrayList<Category> categories = getSavedCategoryForRecipeFromDatabase(timeRecipeID);
            recipe = new Recipe(cursor.getString(1), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), true, categories);
            cursor.close();
        }
        db.close();

        setTimeRecipeID(timeRecipeID);
        return recipe;
    }

    //Getting the saved categoryIDs from the database to show the user which categories are applied to the recipe
    private ArrayList<Category> getSavedCategoryForRecipeFromDatabase(int recipeID) {
        ArrayList<Category> savedCategories = new ArrayList<>();
        try (MyDatabaseHelper db = MyDatabaseHelper.getInstance(context)) {
            Cursor cursor = db.getAllCategoriesWhereRecipeID(recipeID);
            while (cursor.moveToNext()) {
                Category category = new Category( cursor.getInt(0), cursor.getString(1));
                savedCategories.add(category);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedCategories;
    }
}
