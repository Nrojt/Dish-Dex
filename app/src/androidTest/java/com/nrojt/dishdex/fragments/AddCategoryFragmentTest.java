package com.nrojt.dishdex.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.fragment.app.FragmentTransaction;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class AddCategoryFragmentTest {
    private String randomCategoryName;

    @Before
    public void setUp() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        randomCategoryName = sb.toString();
    }

    @Test
    public void testAddCategoryToDatabase() {

        // Start an activity scenario for MainActivity
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);

        // Get a Context object from the activity
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();


        // Replacing the current fragment with AddCategoryFragment. Espresso doesn't seem to work nice with FAB buttons
        activityScenario.onActivity(activity -> {
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            AddCategoryFragment fragment = new AddCategoryFragment();
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
            fragmentTransaction.replace(R.id.fragmentContainer, fragment);
            fragmentTransaction.commit();
        });




        // Enter a category name and click the saveCategoryButton
        onView(withId(R.id.categoryNameEditText)).perform(typeText(randomCategoryName));
        onView(withId(R.id.saveCategoryButton)).perform(click());

        // Check that the category was added to the database
        MyDatabaseHelper db = MyDatabaseHelper.getInstance(context);
        assertTrue(db.checkIfCategoryExists(randomCategoryName));
        assertTrue(db.deleteCategory(randomCategoryName));

        // Close the activity scenario
        activityScenario.close();
    }
}

