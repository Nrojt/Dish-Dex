package com.nrojt.dishdex;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.nrojt.dishdex.backend.viewmodels.MainActivityViewModel;
import com.nrojt.dishdex.databinding.ActivityMainBinding;
import com.nrojt.dishdex.fragments.AddRecipeChooserFragment;
import com.nrojt.dishdex.fragments.HomePageFragment;
import com.nrojt.dishdex.fragments.SavedRecipesFragment;
import com.nrojt.dishdex.fragments.SettingsFragment;
import com.nrojt.dishdex.utils.interfaces.OnBackPressedListener;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting the binding object
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MainActivityViewModel mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        //Setting the font size
        FontUtils.setTextFontSize(sharedPreferences.getInt(SettingsFragment.FONT_SIZE, 14));
        FontUtils.setTitleFontSize(sharedPreferences.getInt(SettingsFragment.FONT_SIZE_TITLES, 20));

        //Setting the dark mode
        // Get the current system theme
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // Check if the system is in night mode
        boolean defaultDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        mainActivityViewModel.setDarkModeLiveData(sharedPreferences.getBoolean(SettingsFragment.DARK_MODE, defaultDarkMode));

        //setting an observer for the dark mode
        mainActivityViewModel.getDarkModeLiveData().observe(this, darkMode -> {
            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            //recreate();
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);


        //Setting onclicklistener for the bottom navigation bar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            navigateToFragment(item.getItemId());
            return true;
        });


        //Method for handling the back button
        // Get the fragment manager and the number of entries in the backstack
        // If there is more than one fragment in the backstack, pop the backstack
        // Get the current fragment and check if it implements OnBackPressedListener
        // If it does, call handleOnBackPressed() on the fragment
        // If the fragment handles the back button press, return without popping the backstack
        // Get the fragment to pop and check if it is null
        // If it is not null, pop the backstack and update the selected item in the bottom navigation view
        // If there is only one fragment in the backstack, minimize the app
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Get the fragment manager and the number of entries in the backstack
                FragmentManager fragmentManager = getSupportFragmentManager();
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                // If there is more than one fragment in the backstack, pop the backstack
                if (backStackEntryCount > 0) {
                    // Get the current fragment and check if it implements OnBackPressedListener
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

                    //If the current fragment is the homepage fragment, minimize the app
                    if(currentFragment.getClass().getSimpleName().equals("HomePageFragment")){
                        moveTaskToBack(true);
                        return;
                    }

                    if (currentFragment instanceof OnBackPressedListener listener) {
                        // If it does, call handleOnBackPressed() on the fragment
                        if (listener.handleOnBackPressed()) {
                            // If the fragment handles the back button press, return without popping the backstack
                            return;
                        }
                    }
                    // Get the fragment to pop and check if it is null
                    Fragment fragmentToPop = fragmentManager.findFragmentById(R.id.fragmentContainer);
                    if (fragmentToPop != null) {
                        // If it is not null, pop the backstack and update the selected item in the bottom navigation view
                        fragmentManager.popBackStack();
                        updateBottomNavigationItem(fragmentManager.findFragmentById(R.id.fragmentContainer));
                    }
                } else {
                    // If there is only one fragment in the backstack, minimize the app
                    moveTaskToBack(true);
                }
            }
        };

        // Add the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    //Method for replacing the fragment from the main activity
    public void replaceFragment(Fragment fragment, Class<?> callingClass) {
        Log.d("MainActivity", "replaceFragment called from " + callingClass.getName() + " with fragment: " + fragment.getClass().getName());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    //Method for when the backstack changes
    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        // Check if the current fragment is already in the back stack
        boolean isFragmentInBackStack = false;
        int backStackCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            String fragmentName = entry.getName();
            if (currentFragment != null && currentFragment.getClass().getName().equals(fragmentName)) {
                isFragmentInBackStack = true;
                break;
            }
        }

        // Add the current fragment to the back stack if it's not already in the back stack
        if (!isFragmentInBackStack && currentFragment != null) {
            String fragmentName = currentFragment.getClass().getName();
            fragmentManager.beginTransaction().addToBackStack(fragmentName).commit();
        }

        updateBottomNavigationItem(currentFragment);
    }


    //Updating the selected button based on the current fragment
    private void updateBottomNavigationItem(Fragment fragment) {
        //if the fragment is null, return
        if (fragment == null) {
            return;
        }

        int selectedItemId = -1;
        switch (fragment.getClass().getSimpleName()) {
            case "HomePageFragment" -> selectedItemId = R.id.homeButton;
            case "AddRecipeChooserFragment", "BingFragment", "ShowAndEditRecipeFragment", "WebBrowserFragment" ->
                    selectedItemId = R.id.addRecipeButton;
            case "SettingsFragment" -> selectedItemId = R.id.settingsButton;
            case "SavedRecipesFragment", "AddCategoryFragment" ->
                    selectedItemId = R.id.recipesButton;
            default -> {
            }
        }

        // Check if the selected item in the bottom navigation view is already the same as the item you are going to set
        if (selectedItemId != -1 && binding.bottomNavigationView.getSelectedItemId() != selectedItemId) {
            //temporarily remove the listener from the bottom navigation view so that the listener doesn't get called when I set the selected item (to update the ui)
            binding.bottomNavigationView.setOnItemSelectedListener(null);
            binding.bottomNavigationView.setSelectedItemId(selectedItemId);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                navigateToFragment(item.getItemId());
                return true;
            });
        }
    }

    public void popBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
    }


    //Method for navigating to the correct fragment when the user taps on a bottom navigation item
    //using a hash map, since switch statements aren't supported starting gradle 8.0. Hash maps are better than if else statements
    private void navigateToFragment(int itemId) {
        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.homeButton, new HomePageFragment());
        fragmentMap.put(R.id.addRecipeButton, new AddRecipeChooserFragment());
        fragmentMap.put(R.id.settingsButton, new SettingsFragment());
        fragmentMap.put(R.id.recipesButton, new SavedRecipesFragment());

        Fragment fragment = fragmentMap.get(itemId);

        if (fragment != null) {
            Log.d("Mainactivity", "navigateToFragment");
            replaceFragment(fragment, getClass());
        }
    }
}