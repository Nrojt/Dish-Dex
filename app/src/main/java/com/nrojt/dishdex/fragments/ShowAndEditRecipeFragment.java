package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.internet.WebScraper;

import java.util.ArrayList;
import java.util.Objects;


public class ShowAndEditRecipeFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {
    private Button saveOrEditRecipeButton;
    private EditText recipeTextOnScreen;
    private EditText ingredientTextOnScreen;
    private TextView chooseCategoriesTextView;
    private EditText cookingTimeTextOnScreen;
    private EditText recipeTitleTextOnScreen;
    private EditText servingsTextOnScreen;

    private EditText noteTextOnScreen;
    private EditText urlTextOnScreen;

    private FragmentManager fragmentManager;

    // for getting and selecting categories
    private ArrayList<String> categoryNames = new ArrayList<>();
    private ArrayList<Integer> categoryIDs = new ArrayList<>();
    private ArrayList<Integer> savedCategoryIDs = new ArrayList<>();
    private boolean[] selectedCategories;


    // the fragment initialization parameters
    private static final String MODE = "mode";
    private static final String RECIPE_ID = "recipeId";
    private static final String WEB_SCRAPER = "WebScraper";
    private static final String URL = "Url";


    private int mode;
    private int recipeId;
    private String url = "";
    private WebScraper wb;
    private int recipeIDFromDatabase;

    public ShowAndEditRecipeFragment() {
        // Required empty public constructor
    }

    public static ShowAndEditRecipeFragment newInstance(int mode, int recipeId, WebScraper wb, String url) {
        ShowAndEditRecipeFragment fragment = new ShowAndEditRecipeFragment();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putInt(RECIPE_ID, recipeId);
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
            recipeId = getArguments().getInt(RECIPE_ID);
            wb = (WebScraper) getArguments().getSerializable("WebScraper");
            url = getArguments().getString("Url");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ShowAndEditRecipes", "onCreateView");
        fragmentManager = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_and_edit_recipe, container, false);

        //inflating the on screen elements
        recipeTextOnScreen = view.findViewById(R.id.recipeTextOnScreen);
        ingredientTextOnScreen = view.findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = view.findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = view.findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = view.findViewById(R.id.recipeTitleTextOnScreen);
        noteTextOnScreen = view.findViewById(R.id.noteTextOnScreen);
        saveOrEditRecipeButton = view.findViewById(R.id.saveOrEditRecipeButton);
        urlTextOnScreen = view.findViewById(R.id.urlTextOnScreen);
        chooseCategoriesTextView = view.findViewById(R.id.chooseCategoriesTextView);

        getCategoriesFromDatabase();

        //Checking if the mode is 0, 1 or 2. 0 is for when the user is adding a recipe from a website, 1 is for when the user is editing a recipe and 2 is for when the user is adding a recipe from scratch.
        switch (mode) {
            case 0:
                saveOrEditRecipeButton.setText("Save Recipe");
                if (wb != null) {
                    //setting the on screen elements to the values scraped from the url
                    recipeTextOnScreen.setText(wb.getRecipeText());
                    ingredientTextOnScreen.setText(wb.getIngredientText());
                    cookingTimeTextOnScreen.setText(String.valueOf(wb.getCookingTime()));
                    servingsTextOnScreen.setText(String.valueOf(wb.getServings()));
                    recipeTitleTextOnScreen.setText(wb.getRecipeTitle());
                    urlTextOnScreen.setText(url);
                } else {
                    Toast.makeText(getContext(), "No data on website", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                saveOrEditRecipeButton.setText("Change Recipe");
                Cursor cursor;
                try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {
                    cursor = db.readAllDataFromSavedRecipesWhereRecipeID(recipeId);

                    if (cursor.getCount() == 0) {
                        Toast.makeText(getContext(), "No data", Toast.LENGTH_SHORT).show();
                    } else {
                        while (cursor.moveToNext()) {
                            recipeTitleTextOnScreen.setText(cursor.getString(1));
                            cookingTimeTextOnScreen.setText(cursor.getString(2));
                            servingsTextOnScreen.setText(cursor.getString(3));
                            ingredientTextOnScreen.setText(cursor.getString(4));
                            recipeTextOnScreen.setText(cursor.getString(5));
                            noteTextOnScreen.setText(cursor.getString(6));
                            urlTextOnScreen.setText(cursor.getString(7));
                        }
                    }
                }
                cursor.close();
                //getting the categories from the database and setting the selectedCategories array to the correct values;
                getSavedCategoryNamesFromDatabase();
                break;
            case 2:
                saveOrEditRecipeButton.setText("Save Recipe");
                getSavedCategoryNamesFromDatabase();
                break;
            default:
                saveOrEditRecipeButton.setText("Change Recipe");
                getSavedCategoryNamesFromDatabase();
                break;
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
            try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {

                String recipeTitle = recipeTitleTextOnScreen.getText().toString().trim();
                String ingredients = ingredientTextOnScreen.getText().toString().trim();
                String recipeSteps = recipeTextOnScreen.getText().toString().trim();
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
                    case 0:
                    case 2:
                        // Add recipe to database
                        recipeIDFromDatabase = db.addRecipe(recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url);
                        db.deleteRecipeCategories(recipeIDFromDatabase);

                        for (int i = 0; i < selectedCategories.length; i++) {
                            if (selectedCategories[i]) {
                                System.out.println("Adding category " + categoryIDs.get(i));
                                db.addRecipeCategory(recipeIDFromDatabase, categoryIDs.get(i));
                            }
                        }

                        if (recipeIDFromDatabase != -1) {
                            fragmentManager.popBackStack();
                        }

                        break;
                    default:
                        // Update recipe in database
                        db.updateRecipe(recipeId, recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url);
                        db.deleteRecipeCategories(recipeId);

                        for (int i = 0; i < selectedCategories.length; i++) {
                            if (selectedCategories[i]) {
                                // The category is selected, so add it to the recipe
                                db.addRecipeCategory(recipeId, categoryIDs.get(i));
                            } else {
                                // The category is not selected, so remove it from the recipe
                                db.removeRecipeCategory(recipeId, categoryIDs.get(i));
                            }
                        }

                        if (recipeIDFromDatabase != -1) {
                            fragmentManager.popBackStack();
                        }
                        break;
                }
            }
        });

        //setting the onClickListener for the chooseCategoriesTextView
        chooseCategoriesTextView.setOnClickListener(v -> {
            //creating a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose Categories");

            //setting the multiChoiceItems to the categories in the database
            builder.setMultiChoiceItems(categoryNames.toArray(new String[0]), selectedCategories, (dialog, which, isChecked) -> selectedCategories[which] = isChecked);

            //setting the positive button to save the categories
            builder.setPositiveButton("Save", (dialog, which) -> {
                //creating a new ArrayList to store the selected categories
                ArrayList<String> selectedCategoriesList = new ArrayList<>();
                for (int i = 0; i < selectedCategories.length; i++) {
                    if (selectedCategories[i]) {
                        selectedCategoriesList.add(categoryNames.get(i));
                        selectedCategories[i] = true;
                    } else {
                        selectedCategories[i] = false;
                    }
                }

                //setting the text of the chooseCategoriesTextView to the selected categories
                chooseCategoriesTextView.setText(TextUtils.join(", ", selectedCategoriesList));
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
                    categoryIDs.add(cursor.getInt(1));
                    categoryNames.add(cursor.getString(0));
                }
            }
            selectedCategories = new boolean[categoryIDs.size()];
            cursor.close();
        }
    }

    //Getting the saved categoryNames and categoryIDs from the database
    private void getSavedCategoryNamesFromDatabase() {
        try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {
            Cursor cursor = db.getAllCategoriesWhereRecipeID(recipeId);
            while (cursor.moveToNext()) {
                savedCategoryIDs.add(cursor.getInt(0));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Setting the selectedCategories array to true for the categories that are already saved
        for (int i = 0; i < categoryIDs.size(); i++) {
            for (int j = 0; j < savedCategoryIDs.size(); j++) {
                if (Objects.equals(categoryIDs.get(i), savedCategoryIDs.get(j))) {
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