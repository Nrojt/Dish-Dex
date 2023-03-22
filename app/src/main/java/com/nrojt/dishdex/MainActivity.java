package com.nrojt.dishdex;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nrojt.dishdex.databinding.ActivityMainBinding;
import com.nrojt.dishdex.fragments.AddRecipeChooserFragment;
import com.nrojt.dishdex.fragments.HomePageFragment;
import com.nrojt.dishdex.fragments.SavedRecipesFragment;
import com.nrojt.dishdex.fragments.SettingsFragment;


public class MainActivity extends AppCompatActivity {

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
    }

    //Method for replacing the fragment
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}