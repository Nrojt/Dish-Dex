package com.nrojt.dishdex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener{
    private AdView adView;


    public static boolean isProUser;
    public static final int MAX_CATEGORIES_FREE = 16;

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
    }

    //Method for replacing the fragment from the main activity
    public void replaceFragment(Fragment fragment){
        Log.d("MainActivity", "replaceFragment called with fragment: " + fragment.getClass().getName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    //Method for popping the backstack
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

    private void updateBottomNavigationItem(Fragment fragment) {
        int selectedItemId = -1;
        switch (fragment.getClass().getName()) {
            case "com.nrojt.dishdex.fragments.HomePageFragment":
                selectedItemId = R.id.homeButton;
                break;
            case "com.nrojt.dishdex.fragments.AddRecipeChooserFragment":
            case "com.nrojt.dishdex.fragments.BingFragment":
            case "com.nrojt.dishdex.fragments.ShowAndEditRecipeFragment":
            case "com.nrojt.dishdex.fragments.WebBrowserFragment":
                selectedItemId = R.id.addRecipeButton;
                break;
            case "com.nrojt.dishdex.fragments.SettingsFragment":
                selectedItemId = R.id.settingsButton;
                break;
            case "com.nrojt.dishdex.fragments.SavedRecipesFragment":
            case "com.nrojt.dishdex.fragments.AddCategoryFragment":
                selectedItemId = R.id.recipesButton;
                break;
            default:
                break;
        }
        // Check if the selected item in the bottom navigation view is already the same as the item you are going to set
        if (selectedItemId != -1 && binding.bottomNavigationView.getSelectedItemId() != selectedItemId) {
            binding.bottomNavigationView.setOnItemSelectedListener(null);
            binding.bottomNavigationView.setSelectedItemId(selectedItemId);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                navigateToFragment(item.getItemId());
                return true;
            });
            //binding.bottomNavigationView.setSelectedItemId(selectedItemId);
        }
    }

    //Method for handling the back button
    @Override
    public void onBackPressed() {
        // Check if the back stack has any fragments other than the home fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();
        if (backStackCount > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void navigateToFragment(int itemId) {
        switch (itemId) {
            case R.id.homeButton:
                replaceFragment(new HomePageFragment());
                break;
            case R.id.addRecipeButton:
                replaceFragment(new AddRecipeChooserFragment());
                break;
            case R.id.settingsButton:
                replaceFragment(new SettingsFragment());
                break;
            case R.id.recipesButton:
                replaceFragment(new SavedRecipesFragment());
                break;
        }
    }

}