package com.nrojt.dishdex.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the initial schema of the database
        sqLiteDatabase.execSQL("CREATE TABLE 'category' ('categoryName'	TEXT, 'categoryID'	INTEGER, PRIMARY KEY('categoryID' AUTOINCREMENT))");
        sqLiteDatabase.execSQL("CREATE TABLE 'recipe_categories' ('recipeID' INTEGER, 'categoryID'	INTEGER, FOREIGN KEY('categoryID') REFERENCES 'category'('categoryID'), FOREIGN KEY ('recipeID') REFERENCES 'saved_recipes' ('recipeID') , PRIMARY KEY ('recipeID','categoryID'))");
        sqLiteDatabase.execSQL("CREATE TABLE 'saved_recipes' ('recipeID'	INTEGER, 'recipeName'	TEXT, 'cookingTime'	INTEGER, 'servings'	INTEGER, 'ingredients'	TEXT, 'instructions'	TEXT, 'notes'	TEXT, 'sourceURL'	TEXT, PRIMARY KEY('recipeID' AUTOINCREMENT))");
    }

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

    public boolean addRecipe(String recipeName, String ingredients, String instructions, int cookingTime, int servings,  String notes, String sourceURL){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("recipeName", recipeName);
        cv.put("cookingTime", cookingTime);
        cv.put("servings", servings);
        cv.put("ingredients", ingredients);
        cv.put("instructions", instructions);
        cv.put("notes", notes);
        cv.put("sourceURL", sourceURL);

        long result = db.insert( "saved_recipes", null, cv);
        db.close();
        if (result == -1){
            Toast.makeText(context, "Failed to save recipe", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(context, "Saved recipe successfully!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public Cursor readAllData(){
        Cursor cursor = null;
        String query = "SELECT * FROM saved_recipes";
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null){
           cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
