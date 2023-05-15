package com.nrojt.dishdex.fragments;

import static com.nrojt.dishdex.fragments.SettingsFragment.BING_API_KEY;
import static com.nrojt.dishdex.fragments.SettingsFragment.OPENAI_API_KEY;
import static com.nrojt.dishdex.fragments.SettingsFragment.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.viewmodels.BingFragmentViewModel;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;
import com.nrojt.dishdex.utils.recycler.CustomItemPaddingDecoration;
import com.nrojt.dishdex.utils.recycler.SavedRecipesCustomRecyclerAdapter;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//TODO show which sites are supported

public class BingFragment extends Fragment implements RecyclerViewInterface, FragmentReplacer {
    private TextView bingNotificationTextView;

    private BingFragmentViewModel viewModel;


    // the fragment initialization parameters
    private static final String SEARCH_QUERY = "searchQuery";

    public BingFragment() {
        // Required empty public constructor
    }

    public static BingFragment newInstance(String searchTerm) {
        BingFragment fragment = new BingFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_QUERY, searchTerm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(BingFragmentViewModel.class);
        if (getArguments() != null) {
            String searchTerm = getArguments().getString(SEARCH_QUERY);
            if(!viewModel.getSearchTerm().equals(searchTerm)) {
                viewModel.setSearchTerm(getArguments().getString(SEARCH_QUERY));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bing, container, false);

        bingNotificationTextView = view.findViewById(R.id.bingNotificationTextView);

        //getting the bing api key from shared preferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        viewModel.setBingApiKey(sharedPreferences.getString(BING_API_KEY, ""));
        viewModel.setOpenaiApiKey(sharedPreferences.getString(OPENAI_API_KEY, ""));

        //Setting the font sizes
        bingNotificationTextView.setTextSize(FontUtils.getTitleFontSize());

        //Setting up the recycler view
        RecyclerView bingRecyclerView = view.findViewById(R.id.bingRecyclerView);
        bingRecyclerView.addItemDecoration(new CustomItemPaddingDecoration(20));
        SavedRecipesCustomRecyclerAdapter adapter = new SavedRecipesCustomRecyclerAdapter(getContext(), viewModel.getRecipes(), this, FontUtils.getTitleFontSize(), FontUtils.getTextFontSize());
        bingRecyclerView.setAdapter(adapter);
        bingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.getRecipeChangeCounter().observe(getViewLifecycleOwner(), integer -> {
            if(integer != null) {
                if(integer > 0) {
                    adapter.notifyItemInserted(integer - 1);
                    bingNotificationTextView.setText("Found " + integer + " recipes");
                }
            }
        });

        viewModel.getSearchTermMutable().observe(getViewLifecycleOwner(), s -> {
            if(s != null) {
                if(!s.isBlank()) {
                    bingNotificationTextView.setText("Searching for " + s + "...");
                    viewModel.searchForRecipes(getContext());
                } else {
                    bingNotificationTextView.setText("No search term provided");
                }
            }
        });

        //swipe to the right to open the recipe in the browser
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                replaceFragment(WebBrowserFragment.newInstance(viewModel.getRecipes().get(position).getRecipeUrl(), true));
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(bingRecyclerView);

        return view;
    }


    //Just a proof of concept, should use a recyclerview instead to show all the results
    private void openLink(int position){
        //switching to the ShowAndEditRecipeFragment
        Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(3, viewModel.getRecipes().get(position), null, viewModel.getRecipes().get(position).getRecipeUrl());
        replaceFragment(showAndEditRecipeFragment);
    }

    // Method for replacing the fragment (redirecting to main activity)
    @Override
    public void replaceFragment(Fragment fragment) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
    }

    @Override
    public void onItemClick(int position) {
        openLink(position);
    }
}