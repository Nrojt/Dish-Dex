package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private SearchView savedCategoriesSearchView;

    private MyDatabaseHelper db;

    private ArrayList<String> categoryNames = new ArrayList<>();
    private ArrayList<Integer> categoryIDs = new ArrayList<>();

    private String deletedCategoryName = null;
    private int deletedCategoryID = -1;

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
        fragmentManager = getActivity().getSupportFragmentManager();
        View view = inflater.inflate(R.layout.fragment_saved_categories, container, false);
        savedCategoriesRecyclerView = view.findViewById(R.id.savedCategoriesRecyclerView);
        savedCategoriesSearchView = view.findViewById(R.id.savedCategoriesSearchView);

        savedCategoriesRecyclerView.addItemDecoration(new CustomItemPaddingDecoration(20));
        SavedCategoriesCustomRecyclerAdapter savedCategoriesCustomRecyclerAdapter = new SavedCategoriesCustomRecyclerAdapter(getContext(), categoryIDs, categoryNames, this);
        savedCategoriesRecyclerView.setAdapter(savedCategoriesCustomRecyclerAdapter);
        savedCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new MyDatabaseHelper(getContext());


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
                    if (categoryIDs.get(position) > 6) {
                        deletedCategoryName = categoryNames.get(position);
                        deletedCategoryID = categoryIDs.get(position);

                        categoryNames.remove(position);
                        categoryIDs.remove(position);

                        savedCategoriesCustomRecyclerAdapter.notifyItemRemoved(position);

                        //This snackbar allows the user to undo the deletion
                        Snackbar snackbar = Snackbar.make(savedCategoriesRecyclerView, deletedCategoryName, Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    //recipeTitles.add(position, deletedRecipe);
                                    categoryNames.add(position, deletedCategoryName);
                                    categoryIDs.add(position, deletedCategoryID);
                                    savedCategoriesCustomRecyclerAdapter.notifyItemInserted(position);
                                });

                        //This callback is called when the snackbar is dismissed
                        snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    db.deleteCategory(deletedCategoryID);
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

        //This method displays the categories in the recycler view
        displayCategories();

        return view;
    }

    private void filter(String newText) {
        //TODO filter by category once that is implemented, not gonna make this before opt2 though
        ArrayList<String> filteredCategoryTitles = new ArrayList<>();
        ArrayList<Integer> filteredCategoryIDs = new ArrayList<>();


        for (String categoryTitle : categoryNames) {
            if (categoryTitle.toLowerCase().contains(newText.toLowerCase())) {
                filteredCategoryTitles.add(categoryTitle);
                filteredCategoryIDs.add(categoryIDs.get(categoryNames.indexOf(categoryTitle)));
            }
        }

        SavedCategoriesCustomRecyclerAdapter savedCategoriesCustomRecyclerAdapter = new SavedCategoriesCustomRecyclerAdapter(getContext(), filteredCategoryIDs, filteredCategoryTitles, this);
        savedCategoriesRecyclerView.setAdapter(savedCategoriesCustomRecyclerAdapter);
        savedCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void displayCategories() {
        //Getting the data from the database
        Cursor cursor = db.readDataForSavedCategoriesRecyclerView();

        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No categories found.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                categoryIDs.add(cursor.getInt(0));
                categoryNames.add(cursor.getString(1));
            }
        }
        cursor.close();
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    @Override
    public void onItemClick(int position) {
        //TODO Something here, maybe editing category name when clicking on the category.
    }

    @Override
    public void onBackStackChanged() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackStackChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }
}