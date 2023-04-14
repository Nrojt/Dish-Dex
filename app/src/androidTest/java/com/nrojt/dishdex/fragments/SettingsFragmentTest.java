package com.nrojt.dishdex.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;

import org.junit.Assert;
import org.junit.Test;

public class SettingsFragmentTest {
    @Test
    public void testSaveProUserSetting() {
        // Launch the SettingsFragment
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);

        // Get a Context object from the activity
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Code for switching to the correct fragment, not needed anymore since just clicking the settings button also works.
        /*
        // Replacing the current fragment with SettingsFragment
        activityScenario.onActivity(activity -> {
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
        });
         */

        // Going to the settings fragment
        onView(withId(R.id.settingsButton)).perform(ViewActions.click());

        // Get the shared preferences object from the context and saving the value
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
        boolean savedIsProUser = sharedPreferences.getBoolean(SettingsFragment.IS_PRO_USER, false);

        // Click the proUserToggleButton
        onView(withId(R.id.proUserToggleButton)).perform(ViewActions.click());
        onView(withId(R.id.saveSettingsButton)).perform(ViewActions.click());


        // Check that the value was saved correctly
        boolean isProUser = sharedPreferences.getBoolean(SettingsFragment.IS_PRO_USER, false);

        // Assert that the value was saved correctly
        Assert.assertEquals(!savedIsProUser, isProUser);

        // Close the fragment scenario
        activityScenario.close();
    }
}
