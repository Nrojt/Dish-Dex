package com.nrojt.dishdex.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentTransaction;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
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

        // Replacing the current fragment with SettingsFragment
        activityScenario.onActivity(activity -> {
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
        });

        // Get the shared preferences object from the context
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);

        // Click the proUserToggleButton
        Espresso.onView(ViewMatchers.withId(R.id.proUserToggleButton)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.saveSettingsButton)).perform(ViewActions.click());


        // Check that the value was saved correctly
        boolean isProUser = sharedPreferences.getBoolean(SettingsFragment.IS_PRO_USER, false);
        Assert.assertTrue(isProUser);

        // Close the fragment scenario
        activityScenario.close();
    }
}
