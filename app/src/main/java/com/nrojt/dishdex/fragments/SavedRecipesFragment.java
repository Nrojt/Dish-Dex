package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;
import com.nrojt.dishdex.utils.recycler.CustomItemPaddingDecoration;
import com.nrojt.dishdex.utils.recycler.SavedRecipesCustomRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedRecipesFragment extends Fragment implements RecyclerViewInterface, FragmentReplacer, FragmentManager.OnBackStackChangedListener {

    private FloatingActionButton savedRecipesFab;

    private static final String ARG_PARAM1 = "hideFab";

    // TODO: Rename and change types of parameters
    private boolean hideFab;

    private MyDatabaseHelper db;

    private final ArrayList<Recipe> recipes = new ArrayList<>();

    private RecyclerView savedRecipesRecyclerView;

    private Recipe deletedRecipe;

    private FragmentManager fragmentManager;


    public SavedRecipesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param hideFab Parameter 1.
     * @return A new instance of fragment SavedRecipesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SavedRecipesFragment newInstance(boolean hideFab) {
        SavedRecipesFragment fragment = new SavedRecipesFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, hideFab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hideFab = getArguments().getBoolean(ARG_PARAM1, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        View view = inflater.inflate(R.layout.fragment_saved_recipes, container, false);

        db = MyDatabaseHelper.getInstance(getContext());

        savedRecipesRecyclerView = view.findViewById(R.id.savedRecipesRecyclerView);
        SearchView savedRecipesSearchView = view.findViewById(R.id.savedRecipesSearchView);
        savedRecipesFab = view.findViewById(R.id.savedRecipesFab);

        savedRecipesFab.setOnClickListener(v -> showFabMenu());

        //Adding padding to the recyclerView and setting the adapter and layout manager
        savedRecipesRecyclerView.addItemDecoration(new CustomItemPaddingDecoration(20));
        SavedRecipesCustomRecyclerAdapter savedRecipesCustomRecyclerAdapter = new SavedRecipesCustomRecyclerAdapter(getContext(), recipes, this);
        savedRecipesRecyclerView.setAdapter(savedRecipesCustomRecyclerAdapter);
        savedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(hideFab){
            savedRecipesFab.setVisibility(View.GONE);
            savedRecipesSearchView.setVisibility(View.GONE);
        }

        //Adding the search functionality
        savedRecipesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });


        //Adding swipe to delete functionality
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    deletedRecipe = recipes.get(position);
                    recipes.remove(position);


                    savedRecipesCustomRecyclerAdapter.notifyItemRemoved(position);

                    //This snackbar allows the user to undo the deletion
                    Snackbar snackbar = Snackbar.make(savedRecipesRecyclerView, deletedRecipe.getRecipeTitle(), Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                //recipeTitles.add(position, deletedRecipe);
                                recipes.add(position, deletedRecipe);
                                savedRecipesCustomRecyclerAdapter.notifyItemInserted(position);
                            });

                    //This callback is called when the snackbar is dismissed
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                db.deleteRecipe(deletedRecipe.getRecipeID());
                                db.deleteRecipeCategories(deletedRecipe.getRecipeID());
                            }
                        }
                    });
                    snackbar.show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(savedRecipesRecyclerView);

        //This method gets the recipes in the database
        getRecipesFromDatabase();

        return view;
    }

    //This method is called when the user enters text into the search bar and it filters the recipes based on their titles
    private void filter(String newText) {
        //TODO filter by category once that is implemented, not gonna make this before opt2 though
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (recipe.getRecipeTitle().toLowerCase().contains(newText.toLowerCase())) {
                filteredRecipes.add(recipe);
            }
        }

        SavedRecipesCustomRecyclerAdapter savedRecipesCustomRecyclerAdapter = new SavedRecipesCustomRecyclerAdapter(getContext(), filteredRecipes, this);
        savedRecipesRecyclerView.setAdapter(savedRecipesCustomRecyclerAdapter);
        savedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    //Adding the recipes to a recyclerView
    private void getRecipesFromDatabase() {

        //Clearing the lists so that the data is not duplicated
        recipes.clear();

        //Getting the data from the database
        Cursor cursor = db.readAllDataFromSavedRecipes();

        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No saved recipes found.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                Recipe recipe = new Recipe(cursor.getString(1), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), true);
                recipes.add(recipe);
            }
        }
        cursor.close();
    }

    //This method displays the FAB menu
    private void showFabMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), savedRecipesFab);
        popupMenu.getMenuInflater().inflate(R.menu.saved_recipes_fab_menu, popupMenu.getMenu());

        // Create a HashMap mapping menu item IDs to Runnables
        Map<Integer, Runnable> actionMap = new HashMap<>();
        actionMap.put(R.id.savedRecipesFabAddCategory, () -> {
            if (!MainActivity.isProUser && db.getCategoryCount() >= MainActivity.MAX_CATEGORIES_FREE) {
                Toast.makeText(getContext(), "You have reached the maximum amount of categories for the free version of the app. Please upgrade to the pro version to create more categories.", Toast.LENGTH_LONG).show();
            } else {
                replaceFragment(new AddCategoryFragment());
            }
        });
        actionMap.put(R.id.savedRecipesFabBrowser, () -> replaceFragment(new WebBrowserFragment()));
        actionMap.put(R.id.savedRecipesFabAddEmptyRecipe, () -> replaceFragment(ShowAndEditRecipeFragment.newInstance(2, new Recipe(), null, null)));
        actionMap.put(R.id.savedRecipesFabAllCategories, () -> replaceFragment(new SavedCategoriesFragment()));

        // Set the click listener for menu items
        popupMenu.setOnMenuItemClickListener(item -> {
            Runnable action = actionMap.get(item.getItemId());
            if (action != null) {
                action.run();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }


    //This code runs when a recipe is clicked
    @Override
    public void onItemClick(int position) {
        Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(1, recipes.get(position), null, null);
        replaceFragment(showAndEditRecipeFragment);
    }

    //replacing the fragment
    @Override
    public void replaceFragment(Fragment fragment) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
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