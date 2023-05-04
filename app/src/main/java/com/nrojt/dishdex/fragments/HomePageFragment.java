package com.nrojt.dishdex.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.nrojt.dishdex.backend.viewmodels.HomePageFragmentViewModel;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;
import com.nrojt.dishdex.utils.viewmodel.HomePageFragmentViewModelFactory;

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
    private FragmentManager fragmentManager;

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
        viewModel = new ViewModelProvider(this, new HomePageFragmentViewModelFactory(requireActivity().getApplication())).get(HomePageFragmentViewModel.class);
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


        fragmentManager = getChildFragmentManager();

        //Get the current day of the week and display it
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String currentDay = dow.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = formatter.format(LocalTime.now());


        dateTextView.setText("Today is: " + currentDay);

        timeTextView.setText("It is currently: " + currentTime);


        Log.i("HomePageFragment", "noSavedRecipes " + savedRecipesFragment.getNoSavedRecipes());
        //TODO fix this
        if (savedRecipesFragment.getNoSavedRecipes()) {
            fragmentContainerView.setVisibility(View.GONE);
            homePageFragmentContainerTextView.setVisibility(View.GONE);
            homePageFragmentContainerTextView.setVisibility(View.GONE);
        }

        //Setting the greetings
        viewModel.getTimeCategoryIDLiveData().observe(getViewLifecycleOwner(), timeCategoryID -> {
            if (timeCategoryID != null) {
                //Setting the greeting text
                switch (timeCategoryID) {
                    case 1 -> {
                        greetingsTextView.setText("Good morning!");
                        recipeForTimeTextView.setText("Let's try this breakfast recipe:");
                    }
                    case 2 -> {
                        greetingsTextView.setText("Good morning!");
                        recipeForTimeTextView.setText("How about this for lunch:");
                    }
                    case 3 -> {
                        greetingsTextView.setText("Good afternoon!");
                        recipeForTimeTextView.setText("Your new favourite dinner:");
                    }
                    case 4 -> {
                        greetingsTextView.setText("Good evening!");
                        recipeForTimeTextView.setText("Want a desert:");
                    }
                    case 5 -> {
                        greetingsTextView.setText("Good night!");
                        recipeForTimeTextView.setText("Time for a little midnight snack:");
                    }
                }
            }
        });

        // Observe changes in the ViewModel data for the Recipe
        viewModel.getRecipeLiveData().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                //setting the text for the recipe card
                recipeTimeTitleTextView.setText(recipe.getRecipeTitle());
                recipeTimeCookingTimeTextView.setText(recipe.getRecipeCookingTime() + " minutes");
                recipeTimeServingsTextView.setText("Servings: " + recipe.getRecipeServings());

                //Setting the visibility and onclick listener for the recipe card
                recipeTimeCardView.setVisibility(View.VISIBLE);
                homePageFragmentContainerTextView.setText("Don't like this one? Try one of your saved recipes:");
                recipeTimeCardView.setOnClickListener(v -> {
                    Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(1, recipe , null, null);
                    replaceFragment(showAndEditRecipeFragment);
                });
            } else {
                recipeForTimeTextView.setText("You do not have any saved recipes for this time of the day.");
                homePageFragmentContainerTextView.setText("Maybe try one of your saved recipes:");
                recipeTimeCardView.setVisibility(View.GONE);
            }
        });

        return view;
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