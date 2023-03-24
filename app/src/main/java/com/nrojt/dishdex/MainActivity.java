package com.nrojt.dishdex;

import android.content.SharedPreferences;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {
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

        //Onclick listener for the bottom navigation bar, which will replace the fragment when one of the buttons is clicked.
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
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
            return true;
        });

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

    //Method for replacing the fragment
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}