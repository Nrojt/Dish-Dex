package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.internet.WebScraper;

import java.util.ArrayList;
import java.util.Objects;


public class ShowAndEditRecipeFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {
    private Button saveOrEditRecipeButton;
    private EditText instructionTextOnScreen;
    private EditText ingredientTextOnScreen;
    private TextView chooseCategoriesTextView;
    private EditText cookingTimeTextOnScreen;
    private EditText recipeTitleTextOnScreen;
    private EditText servingsTextOnScreen;

    private EditText noteTextOnScreen;
    private EditText urlTextOnScreen;

    private FragmentManager fragmentManager;

    // for getting and selecting categories
    private final ArrayList<Category> categories = new ArrayList<>();
    private boolean[] selectedCategories;
    private final ArrayList<Integer> savedCategoryIDs = new ArrayList<>();


    // the fragment initialization parameters
    private static final String MODE = "mode";
    private static final String RECIPE = "recipe";
    private static final String WEB_SCRAPER = "WebScraper";
    private static final String URL = "Url";


    private int mode;
    private String url = "";
    private WebScraper wb;
    private int recipeIDFromDatabase;
    private Recipe recipe;

    public ShowAndEditRecipeFragment() {
        // Required empty public constructor
    }

    public static ShowAndEditRecipeFragment newInstance(int mode, Recipe recipe, WebScraper wb, String url) {
        ShowAndEditRecipeFragment fragment = new ShowAndEditRecipeFragment();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putSerializable(RECIPE, recipe);
        args.putSerializable(WEB_SCRAPER, wb);
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getInt(MODE);
            recipe = (Recipe) getArguments().getSerializable(RECIPE);
            wb = (WebScraper) getArguments().getSerializable("WebScraper");
            url = getArguments().getString("Url");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_and_edit_recipe, container, false);

        //inflating the on screen elements
        instructionTextOnScreen = view.findViewById(R.id.recipeTextOnScreen);
        ingredientTextOnScreen = view.findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = view.findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = view.findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = view.findViewById(R.id.recipeTitleTextOnScreen);
        noteTextOnScreen = view.findViewById(R.id.noteTextOnScreen);
        saveOrEditRecipeButton = view.findViewById(R.id.saveOrEditRecipeButton);
        urlTextOnScreen = view.findViewById(R.id.urlTextOnScreen);
        chooseCategoriesTextView = view.findViewById(R.id.chooseCategoriesTextView);

        //setting the text size of the on screen elements
        instructionTextOnScreen.setTextSize(MainActivity.fontSize);
        ingredientTextOnScreen.setTextSize(MainActivity.fontSize);
        cookingTimeTextOnScreen.setTextSize(MainActivity.fontSize);
        servingsTextOnScreen.setTextSize(MainActivity.fontSize);
        recipeTitleTextOnScreen.setTextSize(MainActivity.fontSizeTitles);
        noteTextOnScreen.setTextSize(MainActivity.fontSize);
        urlTextOnScreen.setTextSize(MainActivity.fontSize);
        chooseCategoriesTextView.setTextSize(MainActivity.fontSize);

        getCategoriesFromDatabase();

        //Checking if the mode is 0, 1 or 2. 0 is for when the user is adding a recipe from a website, 1 is for when the user is editing a recipe and 2 is for when the user is adding a recipe from scratch.
        switch (mode) {
            case 0 -> {
                saveOrEditRecipeButton.setText("Save Recipe");
                if (wb != null) {
                    //setting the on screen elements to the values scraped from the url
                    instructionTextOnScreen.setText(wb.getRecipeText());
                    ingredientTextOnScreen.setText(wb.getIngredientText());
                    cookingTimeTextOnScreen.setText(String.valueOf(wb.getCookingTime()));
                    servingsTextOnScreen.setText(String.valueOf(wb.getServings()));
                    recipeTitleTextOnScreen.setText(wb.getRecipeTitle());
                    urlTextOnScreen.setText(url);
                } else {
                    Toast.makeText(getContext(), "No data on website", Toast.LENGTH_SHORT).show();
                }
            }
            case 1 -> {
                saveOrEditRecipeButton.setText("Change Recipe");
                recipeTitleTextOnScreen.setText(recipe.getRecipeTitle());
                cookingTimeTextOnScreen.setText(String.valueOf(recipe.getRecipeCookingTime()));
                servingsTextOnScreen.setText(String.valueOf(recipe.getRecipeServings()));
                ingredientTextOnScreen.setText(recipe.getRecipeIngredients());
                instructionTextOnScreen.setText(recipe.getRecipeInstructions());
                noteTextOnScreen.setText(recipe.getRecipeNotes());
                urlTextOnScreen.setText(recipe.getRecipeUrl());

                //getting the categories from the database and setting the selectedCategories array to the correct values;
                getSavedCategoryNamesFromDatabase();
            }
            case 2 -> {
                saveOrEditRecipeButton.setText("Save Recipe");
                getSavedCategoryNamesFromDatabase();
            }
            case 3 -> {
                saveOrEditRecipeButton.setText("Save Recipe");
                recipeTitleTextOnScreen.setText(recipe.getRecipeTitle());
                cookingTimeTextOnScreen.setText(String.valueOf(recipe.getRecipeCookingTime()));
                servingsTextOnScreen.setText(String.valueOf(recipe.getRecipeServings()));
                ingredientTextOnScreen.setText(recipe.getRecipeIngredients());
                instructionTextOnScreen.setText(recipe.getRecipeInstructions());
                noteTextOnScreen.setText(recipe.getRecipeNotes());
                urlTextOnScreen.setText(recipe.getRecipeUrl());
            }
            default -> {
                saveOrEditRecipeButton.setText("Change Recipe");
                getSavedCategoryNamesFromDatabase();
            }
        }

        //setting the saveRecipeButton to be invisible when the keyboard is up. It does this by checking the height of the screen and the height of the keyboard and if the difference is greater than a certain threshold, it hides the button.
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);
            int screenHeight = view.getRootView().getHeight();
            Handler handler = new Handler();

            // Calculate the difference between the visible screen height and the total screen height
            int heightDifference = screenHeight - (r.bottom - r.top);

            // Set an arbitrary threshold to determine if the keyboard is visible
            if (heightDifference > screenHeight / 4) {
                saveOrEditRecipeButton.setVisibility(View.GONE); // Hide the saveRecipeButton
            } else {
                handler.postDelayed(() -> {
                    saveOrEditRecipeButton.setVisibility(View.VISIBLE); // Show the saveRecipeButton
                }, 100); // Delay the showing of the saveRecipeButton to avoid flickering
            }
        });

        //setting the onClickListener for the saveOrEditRecipeButton
        saveOrEditRecipeButton.setOnClickListener(v -> {
            if(recipeTitleTextOnScreen.getText().toString().isBlank()){
                recipeTitleTextOnScreen.setError("Recipe title is required");
                Toast.makeText(getActivity().getApplicationContext(), "Recipe title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {

                String recipeTitle = recipeTitleTextOnScreen.getText().toString().trim();
                String ingredients = ingredientTextOnScreen.getText().toString().trim();
                String recipeSteps = instructionTextOnScreen.getText().toString().trim();
                String notes = noteTextOnScreen.getText().toString().trim();
                String url = urlTextOnScreen.getText().toString().trim();

                int cookingTime = 0;
                if (!cookingTimeTextOnScreen.getText().toString().isBlank()) {
                    // Convert cooking time input to integer
                    String cookingTimeStr = cookingTimeTextOnScreen.getText().toString().trim();
                    try {
                        cookingTime = Integer.parseInt(cookingTimeStr);
                    } catch (NumberFormatException e) {
                        // Handle invalid input
                        cookingTimeTextOnScreen.setError("Cooking time must be a number");
                        Toast.makeText(getActivity().getApplicationContext(), "Cooking time must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                int servings = 0;
                if (!servingsTextOnScreen.getText().toString().isBlank()) {
                    // Convert servings input to integer
                    String servingsStr = servingsTextOnScreen.getText().toString().trim();
                    try {
                        servings = Integer.parseInt(servingsStr);
                    } catch (NumberFormatException e) {
                        // Handle invalid input
                        servingsTextOnScreen.setError("Servings must be a number");
                        Toast.makeText(getActivity().getApplicationContext(), "Servings must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                switch (mode) {
                    case 0, 2, 3 -> {
                        // Add recipe to database
                        recipeIDFromDatabase = db.addRecipe(recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url);
                        for (int i = 0; i < selectedCategories.length; i++) {
                            System.out.println("selectedCategories[" + i + "] = " + selectedCategories[i]);
                            if (selectedCategories[i]) {
                                db.addRecipeCategory(recipeIDFromDatabase, categories.get(i).getCategoryID());
                            }
                        }
                        if (recipeIDFromDatabase != -1) {
                            fragmentManager.popBackStack();
                        }
                    }
                    default -> {
                        // Update recipe in database
                        db.updateRecipe(recipe.getRecipeID(), recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url);
                        for (int i = 0; i < selectedCategories.length; i++) {
                            System.out.println("selectedCategories[" + i + "] = " + selectedCategories[i] + " for " + recipe.getRecipeTitle() );

                            if (selectedCategories[i]) {
                                // The category is selected, so add it to the recipe
                                db.addRecipeCategory(recipe.getRecipeID(), categories.get(i).getCategoryID());
                            } else {
                                // The category is not selected, so remove it from the recipe
                                db.removeRecipeCategory(recipe.getRecipeID(), categories.get(i).getCategoryID());
                            }
                        }
                        if (recipeIDFromDatabase != -1) {
                            fragmentManager.popBackStack();
                        }
                    }
                }
            }
        });

        //setting the onClickListener for the chooseCategoriesTextView
        chooseCategoriesTextView.setOnClickListener(v -> {
            //creating a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose Categories");

            // Convert the categories ArrayList to an array of type CharSequence
            CharSequence[] categoriesArray = new CharSequence[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoriesArray[i] = categories.get(i).getCategoryName(); // Assuming each Category object has a getName() method
            }

            // Set the multi-choice items in the dialog builder
            builder.setMultiChoiceItems(categoriesArray, selectedCategories, (dialog, which, isChecked) -> selectedCategories[which] = isChecked);


            //setting the positive button to save the categories
            builder.setPositiveButton("Save", (dialog, which) -> {
                //creating a new ArrayList to store the selected categories
                ArrayList<Category> selectedCategoriesList = new ArrayList<>();
                for (int i = 0; i < selectedCategories.length; i++) {
                    if (selectedCategories[i]) {
                        selectedCategoriesList.add(categories.get(i));
                    }
                }

                //setting the text of the chooseCategoriesTextView to the selected categories
                ArrayList<String> selectedCategoryNames = new ArrayList<>();
                for (Category category : selectedCategoriesList) {
                    selectedCategoryNames.add(category.getCategoryName());
                }
                chooseCategoriesTextView.setText(TextUtils.join(", ", selectedCategoryNames));
            });

            //setting the negative button to cancel the dialog
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            //creating and showing the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return view;
    }

    private void getCategoriesFromDatabase() {
        try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {
            Cursor cursor = db.readAllDataFromCategories();
            if (cursor.getCount() == 0) {
                //This should never happen, since the database is created with default categories
                Toast.makeText(getContext(), "No Categories", Toast.LENGTH_SHORT).show();
            } else {
                while (cursor.moveToNext()) {
                    Category category = new Category(cursor.getInt(1), cursor.getString(0));
                    categories.add(category);
                }
            }
            selectedCategories = new boolean[categories.size()];
            cursor.close();
        }
    }

    //Getting the saved categoryNames and categoryIDs from the database
    private void getSavedCategoryNamesFromDatabase() {
        try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {
            Cursor cursor = db.getAllCategoriesWhereRecipeID(recipe.getRecipeID());
            while (cursor.moveToNext()) {
                savedCategoryIDs.add(cursor.getInt(0));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Setting the selectedCategories array to true for the categories that are already saved
        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < savedCategoryIDs.size(); j++) {
                if (Objects.equals(categories.get(i).getCategoryID(), savedCategoryIDs.get(j))) {
                    selectedCategories[i] = true;
                }
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackStackChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }
}