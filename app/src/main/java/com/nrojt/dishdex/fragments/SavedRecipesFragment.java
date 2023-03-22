package com.nrojt.dishdex.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.recycler.CustomAdapter;
import com.nrojt.dishdex.utils.recycler.ItemPaddingDecoration;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedRecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedRecipesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyDatabaseHelper db;

    private final ArrayList<String> recipeTitles = new ArrayList<>();
    private final ArrayList<Integer> recipeCookingTimes = new ArrayList<>();
    private final ArrayList<Integer> recipeServings = new ArrayList<>();
    private final ArrayList<Integer> recipeIDs = new ArrayList<>();


    public SavedRecipesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SavedRecipesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SavedRecipesFragment newInstance(String param1, String param2) {
        SavedRecipesFragment fragment = new SavedRecipesFragment();
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
        View view = inflater.inflate(R.layout.fragment_saved_recipes, container, false);

        db = new MyDatabaseHelper(getContext());

        RecyclerView savedRecipesRecyclerView = view.findViewById(R.id.savedRecipesRecyclerView);

        //Adding padding to the recyclerView and setting the adapter and layout manager
        savedRecipesRecyclerView.addItemDecoration(new ItemPaddingDecoration(20));
        CustomAdapter customAdapter = new CustomAdapter(getContext(), recipeTitles, recipeCookingTimes, recipeServings);
        savedRecipesRecyclerView.setAdapter(customAdapter);
        savedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        displayRecipes();

        return view;
    }

    //Adding the recipes to a recyclerView
    private void displayRecipes() {
        //Getting the data from the database
        Cursor cursor = db.readDataForSavedRecipesRecyclerView();

        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No saved recipes found.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                recipeIDs.add(cursor.getInt(0));
                recipeTitles.add(cursor.getString(1));
                recipeCookingTimes.add(cursor.getInt(2));
                recipeServings.add(cursor.getInt(3));
            }
        }
    }
}