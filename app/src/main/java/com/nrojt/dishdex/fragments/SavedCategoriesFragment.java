package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;
import com.nrojt.dishdex.utils.recycler.CustomItemPaddingDecoration;
import com.nrojt.dishdex.utils.recycler.SavedCategoriesCustomRecyclerAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedCategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedCategoriesFragment extends Fragment implements RecyclerViewInterface, FragmentReplacer, FragmentManager.OnBackStackChangedListener {
    private RecyclerView savedCategoriesRecyclerView;

    private MyDatabaseHelper db;

    private final ArrayList<Category> categories = new ArrayList<>();

    private Category deletedCategory;

    FragmentManager fragmentManager;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SavedCategoriesFragment() {
        // Required empty public constructor
    }


    public static SavedCategoriesFragment newInstance(String param1, String param2) {
        SavedCategoriesFragment fragment = new SavedCategoriesFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getChildFragmentManager();
        View view = inflater.inflate(R.layout.fragment_saved_categories, container, false);
        savedCategoriesRecyclerView = view.findViewById(R.id.savedCategoriesRecyclerView);
        SearchView savedCategoriesSearchView = view.findViewById(R.id.savedCategoriesSearchView);
        ImageButton addCategoryButton = view.findViewById(R.id.addCategoryButton);

        savedCategoriesRecyclerView.addItemDecoration(new CustomItemPaddingDecoration(20));
        SavedCategoriesCustomRecyclerAdapter savedCategoriesCustomRecyclerAdapter = new SavedCategoriesCustomRecyclerAdapter(getContext(), categories, this);
        savedCategoriesRecyclerView.setAdapter(savedCategoriesCustomRecyclerAdapter);
        savedCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = MyDatabaseHelper.getInstance(getContext());


        savedCategoriesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                    if (categories.get(position).getCategoryID() > 6) {
                        deletedCategory = categories.get(position);
                        categories.remove(deletedCategory);

                        savedCategoriesCustomRecyclerAdapter.notifyItemRemoved(position);

                        //This snackbar allows the user to undo the deletion
                        Snackbar snackbar = Snackbar.make(savedCategoriesRecyclerView, deletedCategory.getCategoryName(), Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    //recipeTitles.add(position, deletedRecipe);
                                    categories.add(position, deletedCategory);
                                    savedCategoriesCustomRecyclerAdapter.notifyItemInserted(position);
                                });

                        //This callback is called when the snackbar is dismissed
                        snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    db.deleteCategory(deletedCategory.getCategoryID());
                                }
                            }
                        });
                        snackbar.show();
                    } else {
                        Toast.makeText(getContext(), "You can't delete this category", Toast.LENGTH_SHORT).show();
                        savedCategoriesCustomRecyclerAdapter.notifyItemChanged(position);
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(savedCategoriesRecyclerView);

        //This method gets the categories in the recycler view
        getCategoriesFromDatabase();

        addCategoryButton.setOnClickListener(v -> {
            AddCategoryFragment addCategoryFragment = new AddCategoryFragment();
            replaceFragment(addCategoryFragment);
        });

        return view;
    }

    private void filter(String newText) {
        ArrayList<Category> filteredCategories = new ArrayList<>();


        for (Category category : categories) {
            if (category.getCategoryName().toLowerCase().contains(newText.toLowerCase())) {
                filteredCategories.add(category);
            }
        }

        SavedCategoriesCustomRecyclerAdapter savedCategoriesCustomRecyclerAdapter = new SavedCategoriesCustomRecyclerAdapter(getContext(), filteredCategories, this);
        savedCategoriesRecyclerView.setAdapter(savedCategoriesCustomRecyclerAdapter);
        savedCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void getCategoriesFromDatabase() {
        //Clearing the arraylist to avoid duplicates
        categories.clear();

        //Getting the data from the database
        Cursor cursor = db.readAllDataFromCategories();

        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No categories found.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                Category category = new Category(cursor.getInt(1), cursor.getString(0));
                categories.add(category);
            }
        }
        cursor.close();
    }

    @Override
    public void replaceFragment(Fragment fragment) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());

    }

    @Override
    public void onItemClick(int position) {
        //TODO Something here, maybe editing category name when clicking on the category.
    }

    @Override
    public void onBackStackChanged() {
            ((MainActivity) getActivity()).onBackStackChanged();

    }

    //This method is called when the fragment is visible to the user and actively running.
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }
}