package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePageFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener {
    private TextView dateTextView, timeTextView, recipeTimeTitleTextView, recipeTimeCookingTimeTextView, recipeTimeServingsTextView;
    private CardView recipeTimeCardView;
    private FragmentManager fragmentManager;

    private int timeCategoryID;
    private int timeRecipeID;

    private String recipeName;
    private int recipeServings;
    private int recipeCookingTime;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);


        recipeTimeCardView = view.findViewById(R.id.recipeTimeCardView);
        recipeTimeTitleTextView = view.findViewById(R.id.recipeTimeTitleTextView);
        recipeTimeCookingTimeTextView = view.findViewById(R.id.recipeTimeCookingTimeTextView);
        recipeTimeServingsTextView = view.findViewById(R.id.recipeTimeServingsTextView);


        fragmentManager = getActivity().getSupportFragmentManager();

        //Get the current day of the week and display it
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);

        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String currentDay = dow.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = formatter.format(LocalTime.now());


        dateTextView.setText("Today is " + currentDay);
        dateTextView.setTextSize(MainActivity.fontSize);
        timeTextView.setText("And the time is " + currentTime);
        timeTextView.setTextSize(MainActivity.fontSize);

        //Getting a random recipeID based on the time of day
        getRandomRecipeIDBasedOnTime();

        //Getting the recipe information from the database
        getInformationFromRecipe();


        recipeTimeTitleTextView.setText(recipeName);
        recipeTimeTitleTextView.setTextSize(MainActivity.fontSizeTitles);

        recipeTimeCookingTimeTextView.setText(recipeCookingTime + " minutes");
        recipeTimeCookingTimeTextView.setTextSize(MainActivity.fontSize);
        recipeTimeServingsTextView.setText("Servings: " + recipeServings);
        recipeTimeServingsTextView.setTextSize(MainActivity.fontSize);


        if (timeRecipeID == -1) {
            recipeTimeCardView.setVisibility(View.GONE);
        } else {
            recipeTimeCardView.setVisibility(View.VISIBLE);
            recipeTimeCardView.setOnClickListener(v -> {
                Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(1, timeRecipeID, null, null);
                replaceFragment(showAndEditRecipeFragment);
            });
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

        if (currentTime.isBefore(breakfastTime)) {
            timeCategoryID = 1;
        } else if (currentTime.isBefore(lunchTime)) {
            timeCategoryID = 2;
        } else if (currentTime.isBefore(dinnerTime)) {
            timeCategoryID = 3;
        } else if (currentTime.isBefore(lateNightTime)) {
            timeCategoryID = 5;
        } else {
            timeCategoryID = 5;
        }

        MyDatabaseHelper db = new MyDatabaseHelper(getContext());
        timeRecipeID = db.getRandomRecipeIDWhereCategoryID(timeCategoryID);
        db.close();
    }

    //Getting the recipe information from the database
    private void getInformationFromRecipe() {
        MyDatabaseHelper db = new MyDatabaseHelper(getContext());
        System.out.println("timeRecipeID: " + timeRecipeID);

        if (timeRecipeID != -1) {
            Cursor cursor = db.readAllDataFromSavedRecipesWhereRecipeID(timeRecipeID);
            cursor.moveToFirst();
            recipeName = cursor.getString(1);
            recipeCookingTime = cursor.getInt(2);
            recipeServings = cursor.getInt(3);
            cursor.close();
        }

        db.close();
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
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