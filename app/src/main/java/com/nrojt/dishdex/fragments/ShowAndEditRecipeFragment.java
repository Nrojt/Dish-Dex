package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.lifecycle.ViewModelProvider;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.backend.viewmodels.ShowAndEditRecipeFragmentViewModel;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.internet.WebScraper;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ShowAndEditRecipeFragment extends Fragment implements FragmentManager.OnBackStackChangedListener, FragmentReplacer {
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

    private ShowAndEditRecipeFragmentViewModel viewModel;

    public ShowAndEditRecipeFragment() {
        // Required empty public constructor
    }

    public static ShowAndEditRecipeFragment newInstance(int mode, Recipe recipe, WebScraper wb, String url) {
        ShowAndEditRecipeFragment fragment = new ShowAndEditRecipeFragment();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putParcelable(RECIPE, recipe);
        args.putParcelable(WEB_SCRAPER, wb);
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("DEPRECATION")
    // getParcelable is deprecated in API 33 (Android 13) so we use getParcelableExtra for API 33 and above, but we still need getParcelable for API 32 and below. This is why we use @SuppressWarnings("DEPRECATION") to suppress the warning about getParcelable being deprecated.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getInt(MODE);
            url = getArguments().getString(URL);

            recipe = getArguments().getParcelable(RECIPE);
            wb = getArguments().getParcelable(WEB_SCRAPER);

        }
        viewModel = new ViewModelProvider(requireActivity()).get(ShowAndEditRecipeFragmentViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getChildFragmentManager();
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
        TextView isUrlSupportedTextView = view.findViewById(R.id.isUrlSupportedTextView);

        //setting the text size of the on screen elements
        recipeTitleTextOnScreen.setTextSize(FontUtils.getTitleFontSize());

        instructionTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        ingredientTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        cookingTimeTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        servingsTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        noteTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        urlTextOnScreen.setTextSize(FontUtils.getTextFontSize());
        chooseCategoriesTextView.setTextSize(FontUtils.getTextFontSize());
        isUrlSupportedTextView.setTextSize(FontUtils.getTextFontSize());


        getCategoriesFromDatabase();

        //Checking the mode. 0 is for when the user is adding a recipe from a website, 1 is for when the user is editing a recipe and 2 is for when the user is adding a recipe from scratch. 3 is for adding a recipe via BingFragment
        switch (mode) {
            case 0 -> {
                saveOrEditRecipeButton.setText("Save Recipe");
                if (wb != null) {
                    isUrlSupportedTextView.setVisibility(View.VISIBLE);
                    //setting the on screen elements to the values scraped from the url
                    instructionTextOnScreen.setText(wb.getRecipeText());
                    ingredientTextOnScreen.setText(wb.getIngredientText());
                    cookingTimeTextOnScreen.setText(String.valueOf(wb.getCookingTime()));
                    servingsTextOnScreen.setText(String.valueOf(wb.getServings()));
                    recipeTitleTextOnScreen.setText(wb.getRecipeTitle());
                    urlTextOnScreen.setText(url);
                    setCategoryFromScraper();
                    if(wb.isNotSupported()){
                        isUrlSupportedTextView.setText("This website is not supported");
                    } else {
                        isUrlSupportedTextView.setText("This website is supported");
                    }
                } else {
                    Toast.makeText(getContext(), "Error, no data on website", Toast.LENGTH_SHORT).show();
                }
            }
            case 1 -> {
                isUrlSupportedTextView.setVisibility(View.GONE);
                saveOrEditRecipeButton.setText("Change Recipe");
                recipeTitleTextOnScreen.setText(recipe.getRecipeTitle());
                cookingTimeTextOnScreen.setText(String.valueOf(recipe.getRecipeCookingTime()));
                servingsTextOnScreen.setText(String.valueOf(recipe.getRecipeServings()));
                ingredientTextOnScreen.setText(recipe.getRecipeIngredients());
                instructionTextOnScreen.setText(recipe.getRecipeInstructions());
                noteTextOnScreen.setText(recipe.getRecipeNotes());
                urlTextOnScreen.setText(recipe.getRecipeUrl());

                //getting the categories from the database and setting the selectedCategories array to the correct values;
                getSavedCategoryForRecipe();
            }
            case 2 -> {
                isUrlSupportedTextView.setVisibility(View.GONE);
                saveOrEditRecipeButton.setText("Save Recipe");
            }
            case 3 -> {
                isUrlSupportedTextView.setVisibility(View.VISIBLE);
                saveOrEditRecipeButton.setText("Save Recipe");
                recipeTitleTextOnScreen.setText(recipe.getRecipeTitle());
                cookingTimeTextOnScreen.setText(String.valueOf(recipe.getRecipeCookingTime()));
                servingsTextOnScreen.setText(String.valueOf(recipe.getRecipeServings()));
                ingredientTextOnScreen.setText(recipe.getRecipeIngredients());
                instructionTextOnScreen.setText(recipe.getRecipeInstructions());
                noteTextOnScreen.setText(recipe.getRecipeNotes());
                urlTextOnScreen.setText(recipe.getRecipeUrl());
                if(recipe.isSupported()){
                    isUrlSupportedTextView.setText("This website is supported");
                } else {
                    isUrlSupportedTextView.setText("This website is not supported");
                }

                //getting the categories from the database and setting the selectedCategories array to the correct values;
                getSavedCategoryForRecipe();
            }
            default -> {
                isUrlSupportedTextView.setVisibility(View.GONE);
                saveOrEditRecipeButton.setText("Change Recipe");
                getSavedCategoryForRecipe();
            }
        }

        //setting the saveRecipeButton to be invisible when the keyboard is up. It does this by checking the height of the screen and the height of the keyboard and if the difference is greater than a certain threshold, it hides the button.
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);
            int screenHeight = view.getRootView().getHeight();

            Handler handler = new Handler(Looper.getMainLooper());

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

            try (MyDatabaseHelper db = MyDatabaseHelper.getInstance(getContext())) {

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

                            if (selectedCategories[i]) {
                                db.addRecipeCategory(recipeIDFromDatabase, categories.get(i).getCategoryID());
                            }
                        }
                        if (recipeIDFromDatabase != -1) {
                            ((MainActivity) getActivity()).popBackStack();
                        } else{
                            Toast.makeText(getActivity().getApplicationContext(), "Error adding recipe", Toast.LENGTH_SHORT).show();
                        }
                    }
                    default -> {
                        // Update recipe in database
                        db.updateRecipe(recipe.getRecipeID(), recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url);
                        for (int i = 0; i < selectedCategories.length; i++) {

                            if (selectedCategories[i]) {
                                // The category is selected, so add it to the recipe
                                db.addRecipeCategory(recipe.getRecipeID(), categories.get(i).getCategoryID());
                            } else {
                                // The category is not selected, so remove it from the recipe
                                db.removeRecipeCategory(recipe.getRecipeID(), categories.get(i).getCategoryID());
                            }
                        }
                        if (recipeIDFromDatabase != -1) {
                            ((MainActivity) getActivity()).popBackStack();
                        } else{
                            Toast.makeText(getActivity().getApplicationContext(), "Error adding recipe", Toast.LENGTH_SHORT).show();
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

            // Putting the category names to a String array
            String[] categoriesArray = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoriesArray[i] = categories.get(i).getCategoryName(); // Assuming each Category object has a getName() method
            }

            // Set the multi-choice items in the dialog builder
            builder.setMultiChoiceItems(categoriesArray, selectedCategories, (dialog, which, isChecked) -> selectedCategories[which] = isChecked);


            //setting the positive button to save the categories
            builder.setPositiveButton("Save", (dialog, which) -> updateSelectedCategories());

            //setting the negative button to cancel the dialog
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            //creating and showing the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        urlTextOnScreen.setOnLongClickListener(v -> {
            if (urlTextOnScreen.getText().toString().isBlank()) {
                Toast.makeText(getContext(), "No URL to open", Toast.LENGTH_SHORT).show();
            } else {
                replaceFragment(WebBrowserFragment.newInstance(urlTextOnScreen.getText().toString(), false));
            }
            return true; // indicate that the event is consumed
        });

        return view;
    }

    private void getCategoriesFromDatabase() {
        try (MyDatabaseHelper db = MyDatabaseHelper.getInstance(getContext())) {
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

    //Getting the saved categoryIDs from the database to show the user which categories are applied to the recipe
    private void getSavedCategoryForRecipe() {
        //Setting the selectedCategories array to true for the categories that are already saved

        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < recipe.getCategories().size(); j++) {
                if (categories.get(i).getCategoryID() == recipe.getCategories().get(j).getCategoryID()){
                    selectedCategories[i] = true;
                }
            }
        }
        updateSelectedCategories();
    }

    //Updating the selected categories text view
    private void updateSelectedCategories() {
        // filter selected categories and map to their names
        String selectedCategoryNames = IntStream.range(0, selectedCategories.length)
                .filter(i -> selectedCategories[i])
                .mapToObj(categories::get)
                .map(Category::getCategoryName)
                .collect(Collectors.joining(", "));

        // set text of chooseCategoriesTextView to selected categories
        if(selectedCategoryNames.isEmpty() || selectedCategoryNames.isBlank()) {
            chooseCategoriesTextView.setText("Categories");
        }
        else {
            chooseCategoriesTextView.setText(selectedCategoryNames);
        }
    }

    //setting the category that got scraped by web scraper
    private void setCategoryFromScraper(){
        int categoryIDFromScraper = wb.getRecipeCategoryID();
        if(categoryIDFromScraper > 0){
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCategoryID() == categoryIDFromScraper){
                    selectedCategories[i] = true;
                }
            }
            updateSelectedCategories();
        }
    }

    @Override
    public void onBackStackChanged() {
        ((MainActivity) getActivity()).onBackStackChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentManager.removeOnBackStackChangedListener(this);
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
    }
}