package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.internet.WebScraper;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowAndEditRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowAndEditRecipeFragment extends Fragment {
    private String url = "";
    private WebScraper wb;
    private Button saveOrEditRecipeButton;
    private EditText recipeTextOnScreen;
    private EditText ingredientTextOnScreen;

    private EditText cookingTimeTextOnScreen;
    private EditText recipeTitleTextOnScreen;
    private EditText servingsTextOnScreen;

    private EditText noteTextOnScreen;
    private EditText urlTextOnScreen;


    // the fragment initialization parameters
    private static final String SCRAPE_FROM_URL = "scrapeFromUrl";
    private static final String RECIPE_ID = "recipeId";
    private static final String WEB_SCRAPER = "WebScraper";
    private static final String URL = "Url";


    private boolean scrapeFromUrl;
    private int recipeId;

    public ShowAndEditRecipeFragment() {
        // Required empty public constructor
    }

    public static ShowAndEditRecipeFragment newInstance(boolean scrapeFromUrl, int recipeId, WebScraper wb, String url) {
        ShowAndEditRecipeFragment fragment = new ShowAndEditRecipeFragment();
        Bundle args = new Bundle();
        args.putBoolean(SCRAPE_FROM_URL, scrapeFromUrl);
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
            scrapeFromUrl = getArguments().getBoolean(SCRAPE_FROM_URL);
            recipeId = getArguments().getInt(RECIPE_ID);
            wb = (WebScraper) getArguments().getSerializable("WebScraper");
            url = getArguments().getString("Url");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        if(scrapeFromUrl) {
            saveOrEditRecipeButton.setText("Save Recipe");
            if(wb != null) {
                //setting the on screen elements to the values scraped from the url
                recipeTextOnScreen.setText(wb.getRecipeText());
                ingredientTextOnScreen.setText(wb.getIngredientText());
                cookingTimeTextOnScreen.setText(String.valueOf(wb.getCookingTime()));
                servingsTextOnScreen.setText(String.valueOf(wb.getServings()));
                recipeTitleTextOnScreen.setText(wb.getRecipeTitle());
                urlTextOnScreen.setText(url);
            }

        } else {
            saveOrEditRecipeButton.setText("Update Recipe");
            Cursor cursor;
            try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {
                cursor = db.readAllDataFromSavedRecipesWhereRecipeID(recipeId);

                if (cursor.getCount() == 0) {
                    Toast.makeText(getContext(), "No data", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        recipeTextOnScreen.setText(cursor.getString(1));
                        ingredientTextOnScreen.setText(cursor.getString(2));
                        cookingTimeTextOnScreen.setText(cursor.getString(3));
                        servingsTextOnScreen.setText(cursor.getString(4));
                        recipeTitleTextOnScreen.setText(cursor.getString(5));
                        noteTextOnScreen.setText(cursor.getString(6));
                        urlTextOnScreen.setText(cursor.getString(7));
                    }
                }
            }
        }

        //setting the saveRecipeButton to be invisible when the keyboard is up. It does this by checking the height of the screen and the height of the keyboard and if the difference is greater than a certain threshold, it hides the button.
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
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
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveOrEditRecipeButton.setVisibility(View.VISIBLE); // Show the saveRecipeButton
                        }
                    }, 100); // Delay the showing of the saveRecipeButton to avoid flickering
                }
            }
        });

        saveOrEditRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try (MyDatabaseHelper db = new MyDatabaseHelper(getContext())) {

                    String recipeTitle = recipeTitleTextOnScreen.getText().toString().trim();
                    String ingredients = ingredientTextOnScreen.getText().toString().trim();
                    String recipeSteps = recipeTextOnScreen.getText().toString().trim();
                    String notes = noteTextOnScreen.getText().toString().trim();
                    String url = urlTextOnScreen.getText().toString().trim();

                    // Convert cooking time input to integer
                    String cookingTimeStr = cookingTimeTextOnScreen.getText().toString().trim();
                    int cookingTime;
                    try {
                        cookingTime = Integer.parseInt(cookingTimeStr);
                    } catch (NumberFormatException e) {
                        // Handle invalid input
                        Toast.makeText(getActivity().getApplicationContext(), "Cooking time must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Convert servings input to integer
                    String servingsStr = servingsTextOnScreen.getText().toString().trim();
                    int servings;
                    try {
                        servings = Integer.parseInt(servingsStr);
                    } catch (NumberFormatException e) {
                        // Handle invalid input
                        Toast.makeText(getActivity().getApplicationContext(), "Servings must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (scrapeFromUrl) {
                        // Add recipe to database
                        if (db.addRecipe(recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url)) {
                            replaceFragment(new AddRecipeChooserFragment());
                        }
                    } else {
                        if (db.updateRecipe(recipeId, recipeTitle, ingredients, recipeSteps, cookingTime, servings, notes, url)) {
                            replaceFragment(new SavedRecipesFragment());
                        }
                    }
                }
            }
        });

        return view;
    }

    //This method is used to replace the current fragment with a new fragment
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}