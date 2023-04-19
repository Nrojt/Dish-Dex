package com.nrojt.dishdex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nrojt.dishdex.databinding.ActivityMainBinding;
import com.nrojt.dishdex.fragments.AddRecipeChooserFragment;
import com.nrojt.dishdex.fragments.HomePageFragment;
import com.nrojt.dishdex.fragments.SavedRecipesFragment;
import com.nrojt.dishdex.fragments.SettingsFragment;
import com.nrojt.dishdex.utils.interfaces.OnBackPressedListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, OnBackPressedListener {
    private AdView adView;

    private OnBackPressedCallback onBackPressedCallback;

    //Global variables basically
    public static boolean isProUser;
    public static final int MAX_CATEGORIES_FREE = 16;
    public static float fontSize;
    public static float fontSizeTitles;


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting the binding object
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setting the fragment that will show on app startup
        binding.bottomNavigationView.setSelectedItemId(R.id.homeButton);
        replaceFragment(new HomePageFragment());

        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        isProUser = sharedPreferences.getBoolean(SettingsFragment.IS_PRO_USER, false);
        fontSize = sharedPreferences.getInt(SettingsFragment.FONT_SIZE, 14);
        fontSizeTitles = sharedPreferences.getInt(SettingsFragment.FONT_SIZE_TITLES, 20);


        getSupportFragmentManager().addOnBackStackChangedListener(this);

        //Initialize the AdMob SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });

        //Setting onclicklistener for the bottom navigation bar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            navigateToFragment(item.getItemId());
            return true;
        });

        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the top and bottom constraints of the FrameLayout and disabling adview if the user is a pro user.
        if (isProUser) {
            // Remove the ad view
            ConstraintLayout adContainer = findViewById(R.id.mainPageConstraintLayout);
            View adView = findViewById(R.id.adView);
            adContainer.removeView(adView);

            // Set the top and bottom constraints of the FrameLayout
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(adContainer);
            constraintSet.connect(frameLayout.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            constraintSet.connect(frameLayout.getId(), ConstraintSet.BOTTOM, bottomNavigationView.getId(), ConstraintSet.TOP, 0);
            constraintSet.applyTo(adContainer);
        }

        //Method for handling the back button
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Get the fragment manager and the number of entries in the backstack
                FragmentManager fragmentManager = getSupportFragmentManager();
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                // If there is more than one fragment in the backstack, pop the backstack
                if (backStackEntryCount > 1) {
                    // Get the current fragment and check if it implements OnBackPressedListener
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
                    if (currentFragment instanceof OnBackPressedListener) {
                        // If it does, call handleOnBackPressed() on the fragment
                        OnBackPressedListener listener = (OnBackPressedListener) currentFragment;
                        if (listener.handleOnBackPressed()) {
                            // If the fragment handles the back button press, return without popping the backstack
                            return;
                        }
                    }
                    // Get the fragment to pop and check if it is null
                    Fragment fragmentToPop = fragmentManager.findFragmentById(R.id.frame_layout);
                    if (fragmentToPop != null) {
                        // If it is not null, pop the backstack and update the selected item in the bottom navigation view
                        fragmentManager.popBackStack();
                        updateBottomNavigationItem(fragmentManager.findFragmentById(R.id.frame_layout));
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
    public void replaceFragment(Fragment fragment) {
        Log.d("MainActivity", "replaceFragment called with fragment: " + fragment.getClass().getName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    //Method for when the backstack changes
    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);

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
            case "HomePageFragment":
                selectedItemId = R.id.homeButton;
                break;
            case "AddRecipeChooserFragment":
            case "BingFragment":
            case "ShowAndEditRecipeFragment":
            case "WebBrowserFragment":
                selectedItemId = R.id.addRecipeButton;
                break;
            case "SettingsFragment":
                selectedItemId = R.id.settingsButton;
                break;
            case "SavedRecipesFragment":
            case "AddCategoryFragment":
                selectedItemId = R.id.recipesButton;
                break;
            default:
                break;
        }

        // Check if the selected item in the bottom navigation view is already the same as the item you are going to set
        if (selectedItemId != -1 && binding.bottomNavigationView != null && binding.bottomNavigationView.getSelectedItemId() != selectedItemId) {
            binding.bottomNavigationView.setOnItemSelectedListener(null);
            binding.bottomNavigationView.setSelectedItemId(selectedItemId);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                navigateToFragment(item.getItemId());
                return true;
            });
        }
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
            replaceFragment(fragment);
        }
    }


    @Override
    public boolean handleOnBackPressed() {
        //This method needs to be implemented because of the interface, but it is not used in this class
        //The method is implemented in the fragments (currently only in the WebBrowserFragment)
        return false;
    }


}