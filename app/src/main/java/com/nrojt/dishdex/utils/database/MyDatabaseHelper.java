package com.nrojt.dishdex.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;


    private static final String DATABASE_NAME = "dishdex.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    // Handle database creation
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the initial schema of the database
        sqLiteDatabase.execSQL("CREATE TABLE 'category' ('categoryName'	TEXT, 'categoryID'	INTEGER, PRIMARY KEY('categoryID' AUTOINCREMENT))");
        sqLiteDatabase.execSQL("CREATE TABLE 'recipe_categories' ('recipeID' INTEGER, 'categoryID'	INTEGER, FOREIGN KEY('categoryID') REFERENCES 'category'('categoryID') ON DELETE CASCADE, FOREIGN KEY ('recipeID') REFERENCES 'saved_recipes' ('recipeID') ON DELETE CASCADE, PRIMARY KEY ('recipeID','categoryID'))");
        sqLiteDatabase.execSQL("CREATE TABLE 'saved_recipes' ('recipeID'	INTEGER, 'recipeName'	TEXT, 'cookingTime'	INTEGER, 'servings'	INTEGER, 'ingredients'	TEXT, 'instructions'	TEXT, 'notes'	TEXT, 'sourceURL'	TEXT, PRIMARY KEY('recipeID' AUTOINCREMENT))");
        // Adding standard categories to the database
        sqLiteDatabase.execSQL("INSERT INTO category (categoryName) VALUES ('Breakfast'), ('Lunch'), ('Dinner'), ('Dessert'), ('Snack'), ('Side Dish')");
    }

    // Handle database upgrades and changes, currently dummy code
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades from one version of the database to the next
        if (oldVersion < 2) {
            // Upgrade from version 1 to version 2
            db.execSQL("ALTER TABLE mytable ADD COLUMN age INTEGER");
        }
        if (oldVersion < 3) {
            // Upgrade from version 2 to version 3
            db.execSQL("CREATE TABLE myothertable (id INTEGER PRIMARY KEY, description TEXT)");
        }
    }

    // Add a recipe to the database
    public int addRecipe(String recipeName, String ingredients, String instructions, int cookingTime, int servings, String notes, String sourceURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        int recipeID = -1;

        cv.put("recipeName", recipeName);
        cv.put("cookingTime", cookingTime);
        cv.put("servings", servings);
        cv.put("ingredients", ingredients);
        cv.put("instructions", instructions);
        cv.put("notes", notes);
        cv.put("sourceURL", sourceURL);

        long result = db.insert("saved_recipes", null, cv);
        db.close();
        if (result == -1) {
            Toast.makeText(context, "Failed to save recipe", Toast.LENGTH_SHORT).show();
            Log.e("Database", "Failed to save recipe");
        } else {
            Toast.makeText(context, "Saved recipe successfully!", Toast.LENGTH_SHORT).show();
            recipeID = (int) result;
        }
        return recipeID;
    }

    //Reading all data from a table
    public Cursor readAllDataFromTable(String tableName) {
        Cursor cursor = null;
        String query = "SELECT * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        return cursor;
    }

    //Getting the data from the database for the saved recipes recycler view
    public Cursor readDataForSavedRecipesRecyclerView() {
        Cursor cursor = null;
        String query = "SELECT recipeID, recipeName, cookingTime, servings FROM saved_recipes";
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        return cursor;
    }

    //Getting the data from the database for the saved categories recycler view
    public Cursor readDataForSavedCategoriesRecyclerView() {
        Cursor cursor = null;
        String query = "SELECT categoryID, categoryName FROM category";
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        return cursor;
    }

    //Getting the data from a specific recipe for ShowAndEditRecipeActivity to show a saved recipe
    public Cursor readAllDataFromSavedRecipesWhereRecipeID(int recipeID) {
        Cursor cursor = null;
        String query = "SELECT * FROM saved_recipes WHERE recipeID = " + recipeID;
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Failed to read data from saved_recipes");
        }
        return cursor;
    }

    //Updating a recipe in the database
    public void updateRecipe(int recipeId, String recipeName, String ingredients, String instructions, int cookingTime, int servings, String notes, String sourceURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("recipeName", recipeName);
        cv.put("cookingTime", cookingTime);
        cv.put("servings", servings);
        cv.put("ingredients", ingredients);
        cv.put("instructions", instructions);
        cv.put("notes", notes);
        cv.put("sourceURL", sourceURL);

        long result = db.update("saved_recipes", cv, "recipeID = ?", new String[]{String.valueOf(recipeId)});
        db.close();
        if (result == -1) {
            Toast.makeText(context, "Failed to update recipe", Toast.LENGTH_SHORT).show();
            Log.e("Database", "Failed to update recipe");
        } else {
            Toast.makeText(context, "Updated recipe successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    //Deleting a recipe from the database
    public boolean deleteRecipe(int recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("saved_recipes", "recipeID = ?", new String[]{String.valueOf(recipeId)});
        db.close();
        if (result == -1) {
            Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
            Log.e("Database", "Failed to delete recipe");
            return false;
        } else {
            return true;
        }
    }

    //Deleting a category from the database
    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("category", "categoryID = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        if (result == -1) {
            Toast.makeText(context, "Failed to delete category", Toast.LENGTH_SHORT).show();
            Log.e("Database", "Failed to delete category");
            return false;
        } else {
            return true;
        }
    }

    //Adding a category to the database
    public boolean addCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("categoryName", categoryName);

        long result = db.insert("category", null, cv);
        db.close();
        if (result == -1) {
            Toast.makeText(context, "Failed to update recipe", Toast.LENGTH_SHORT).show();
            Log.e("Database", "Failed to update recipe");
            return false;
        } else {
            Toast.makeText(context, "Updated recipe successfully!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    //Reading all data from the category table
    public Cursor readAllDataFromCategories() {
        Cursor cursor = null;
        String query = "SELECT * FROM category";

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        return cursor;
    }

    //Adding data to the recipe_category table
    public boolean addRecipeCategory(int recipeID, int categoryID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("recipeID", recipeID);
        cv.put("categoryID", categoryID);

        // Check if the combination of recipeID and categoryID already exists in the recipe_categories table
        String[] columns = {"recipeID", "categoryID"};
        String selection = "recipeID = ? AND categoryID = ?";
        String[] selectionArgs = {Integer.toString(recipeID), Integer.toString(categoryID)};
        Cursor cursor = db.query("recipe_categories", columns, selection, selectionArgs, null, null, null);

        if (cursor.getCount() > 0) {
            System.out.println("The combination of recipeID and categoryID already exists in the recipe_categories table");
            // The combination of recipeID and categoryID already exists in the recipe_categories table, so don't insert a new row
            cursor.close();
            db.close();
            return false;
        } else {
            // The combination of recipeID and categoryID doesn't exist yet in the recipe_categories table, so insert a new row
            long result = db.insert("recipe_categories", null, cv);
            cursor.close();
            db.close();
            return result != -1;
        }
    }

    //Deleting all recipeCategories from a recipe
    public void deleteRecipeCategories(int recipeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "recipeID = ?";
        String[] whereArgs = { String.valueOf(recipeID) };
        db.delete("recipe_categories", whereClause, whereArgs);
        db.close();
    }


    //Deleting the combination of recipeID and categoryID from the recipe_categories table
    public boolean removeRecipeCategory(int recipeID, int categoryID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the combination of recipeID and categoryID exists in the recipe_categories table
        String selection = "recipeID = ? AND categoryID = ?";
        String[] selectionArgs = {Integer.toString(recipeID), Integer.toString(categoryID)};
        Cursor cursor = db.query("recipe_categories", null, selection, selectionArgs, null, null, null);

        if (cursor.getCount() == 0) {
            // The combination of recipeID and categoryID does not exist in the recipe_categories table
            cursor.close();
            db.close();
            return false;
        } else {
            // The combination of recipeID and categoryID exists in the recipe_categories table, so remove it
            int result = db.delete("recipe_categories", selection, selectionArgs);
            cursor.close();
            db.close();
            return result != 0;
        }
    }




    //checking the count of the category table
    public int getCategoryCount() {
        Cursor cursor = null;
        String query = "SELECT COUNT(*) FROM category";
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        db.close();
        cursor.close();
        return count;
    }

    //getting the categories from recipe_categories where recipeID = recipeID
    public Cursor getAllCategoriesWhereRecipeID(int recipeID) {
        Cursor cursor = null;
        String query = "SELECT categoryID FROM recipe_categories WHERE recipeID = " + recipeID;
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e("Database", "Database is null");
        }
        return cursor;
    }

}
