package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.backend.viewmodels.HomePageFragmentViewModel;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePageFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener {
    private FragmentManager fragmentManager;

    private int timeCategoryID;
    private int timeRecipeID;

    private Recipe recipe;

    private TextView greetingsTextView;
    private TextView recipeForTimeTextView;

    private HomePageFragmentViewModel viewModel;


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomePageFragment.
     */


    public static HomePageFragment newInstance(String param1, String param2) {
        HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(HomePageFragmentViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        //inflating the views
        CardView recipeTimeCardView = view.findViewById(R.id.recipeTimeCardView);
        TextView recipeTimeTitleTextView = view.findViewById(R.id.recipeTimeTitleTextView);
        TextView recipeTimeCookingTimeTextView = view.findViewById(R.id.recipeTimeCookingTimeTextView);
        TextView recipeTimeServingsTextView = view.findViewById(R.id.recipeTimeServingsTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        TextView timeTextView = view.findViewById(R.id.timeTextView);
        TextView homePageFragmentContainerTextView = view.findViewById(R.id.homePageFragmentContainerTextView);
        FragmentContainerView fragmentContainerView = view.findViewById(R.id.savedRecipesFragmentContainerView);
        greetingsTextView = view.findViewById(R.id.greetingsTextView);
        recipeForTimeTextView = view.findViewById(R.id.recipeForTimeTextView);

        //Replacing the fragment for the saved recipes, so I can hide the floating action button
        SavedRecipesFragment savedRecipesFragment = SavedRecipesFragment.newInstance(true);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.savedRecipesFragmentContainerView, savedRecipesFragment);
        transaction.commit();



        //Setting the text sizes
        recipeTimeTitleTextView.setTextSize(FontUtils.getTitleFontSize());
        homePageFragmentContainerTextView.setTextSize(FontUtils.getTitleFontSize());
        greetingsTextView.setTextSize(FontUtils.getTitleFontSize());


        recipeForTimeTextView.setTextSize(FontUtils.getTextFontSize());
        dateTextView.setTextSize(FontUtils.getTextFontSize());
        timeTextView.setTextSize(FontUtils.getTextFontSize());
        recipeTimeCookingTimeTextView.setTextSize(FontUtils.getTextFontSize());
        recipeTimeServingsTextView.setTextSize(FontUtils.getTextFontSize());



        //Getting a random recipeID based on the time of day
        getRandomRecipeIDBasedOnTime();

        //Getting the recipe information from the database
        getInformationFromRecipe();


        fragmentManager = getChildFragmentManager();

        //Get the current day of the week and display it
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String currentDay = dow.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = formatter.format(LocalTime.now());


        dateTextView.setText("Today is: " + currentDay);

        timeTextView.setText("It is currently: " + currentTime);


        if(recipe != null) {
            recipeTimeTitleTextView.setText(recipe.getRecipeTitle());
            recipeTimeCookingTimeTextView.setText(recipe.getRecipeCookingTime() + " minutes");
            recipeTimeServingsTextView.setText("Servings: " + recipe.getRecipeServings());
        }



        //Setting the visibility and onclick listener for the recipe card
        if (timeRecipeID == -1) {
            recipeForTimeTextView.setText("You do not have any saved recipes for this time of the day.");
            homePageFragmentContainerTextView.setText("Maybe try one of your saved recipes:");
            recipeTimeCardView.setVisibility(View.GONE);
        } else {
            recipeTimeCardView.setVisibility(View.VISIBLE);
            homePageFragmentContainerTextView.setText("Don't like this one? Try one of your saved recipes:");
            recipeTimeCardView.setOnClickListener(v -> {
                Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(1, recipe , null, null);
                replaceFragment(showAndEditRecipeFragment);
            });
        }

        //TODO hide containertextview when there are no saved recipes
        if (savedRecipesFragment.noSavedRecipes) {
            fragmentContainerView.setVisibility(View.GONE);
            homePageFragmentContainerTextView.setVisibility(View.GONE);
        }

        return view;
    }

    //getting a random recipeID based on the time of day
    private void getRandomRecipeIDBasedOnTime() {
        LocalTime currentTime = LocalTime.now();
        LocalTime breakfastTime = LocalTime.of(9, 0);
        LocalTime lunchTime = LocalTime.of(12, 0);
        LocalTime dinnerTime = LocalTime.of(18, 0);
        LocalTime lateNightTime = LocalTime.of(22, 0);
        LocalTime earlyMorningTime = LocalTime.of(5, 0); //We dont want to suggest breakfast recipes after 0:00 and before 5:00

        if (currentTime.isBefore(breakfastTime) && currentTime.isAfter(earlyMorningTime)) {
            timeCategoryID = 1;
            greetingsTextView.setText("Good morning!");
            recipeForTimeTextView.setText("Let's try this breakfast recipe:");
        } else if (currentTime.isBefore(lunchTime)) {
            timeCategoryID = 2;
            greetingsTextView.setText("Good morning!");
            recipeForTimeTextView.setText("How about this for lunch:");
        } else if (currentTime.isBefore(dinnerTime)) {
            timeCategoryID = 3;
            greetingsTextView.setText("Good afternoon!");
            recipeForTimeTextView.setText("Your new favourite dinner:");
        } else if (currentTime.isBefore(lateNightTime)) {
            timeCategoryID = 5;
            greetingsTextView.setText("Good evening!");
            recipeForTimeTextView.setText("This is a great time for a snack:");
        } else {
            //if the time is after 22:00
            timeCategoryID = 5;
            greetingsTextView.setText("Good night!");
            recipeForTimeTextView.setText("Time for a little midnight snack:");
        }

        MyDatabaseHelper db = MyDatabaseHelper.getInstance(getContext());
        timeRecipeID = db.getRandomRecipeIDWhereCategoryID(timeCategoryID);
        db.close();
    }

    //Getting the recipe information from the database
    private void getInformationFromRecipe() {
        MyDatabaseHelper db = MyDatabaseHelper.getInstance(getContext());

        if (timeRecipeID != -1) {
            Cursor cursor = db.readAllDataFromSavedRecipesWhereRecipeID(timeRecipeID);
            cursor.moveToFirst();
            ArrayList<Category> categories = getSavedCategoryForRecipeFromDatabase(timeRecipeID);
            recipe = new Recipe(cursor.getString(1), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), true, categories);
            cursor.close();
        }
        db.close();
    }

    //Getting the saved categoryIDs from the database to show the user which categories are applied to the recipe
    private ArrayList<Category> getSavedCategoryForRecipeFromDatabase(int recipeID) {
        ArrayList<Category> savedCategories = new ArrayList<>();
        try (MyDatabaseHelper db = MyDatabaseHelper.getInstance(getContext())) {
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

    @Override
    public void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
        }
    }

    @Override
    public void onBackStackChanged() {
        ((MainActivity) getActivity()).onBackStackChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }
}