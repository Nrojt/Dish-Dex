package com.nrojt.dishdex.fragments;

import static com.nrojt.dishdex.fragments.SettingsFragment.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.viewmodels.AddRecipeChooserFragmentViewModel;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.internet.WebScraper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddRecipeChooserFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener {
    private TextInputEditText urlInput;
    private TextInputEditText bingSearchInput;

    private FragmentManager fragmentManager;

    private AddRecipeChooserFragmentViewModel viewModel;


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public AddRecipeChooserFragment() {
        // Required empty public constructor
    }



    public static AddRecipeChooserFragment newInstance(String param1, String param2) {
        AddRecipeChooserFragment fragment = new AddRecipeChooserFragment();
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
        viewModel = new ViewModelProvider(requireActivity()).get(AddRecipeChooserFragmentViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getChildFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_recipe_chooser, container, false);

        //initializing the variables, assigning them to the corresponding on screen views
        urlInput = view.findViewById(R.id.urlInput);
        Button getRecipeFromUrlButton = view.findViewById(R.id.getRecipeFromUrlButton);
        Button bingSearchButton = view.findViewById(R.id.bingSearchButton);
        bingSearchInput = view.findViewById(R.id.bingSearchInput);
        Button browseWebButton = view.findViewById(R.id.browseWebButton);
        Button emptyRecipeButton = view.findViewById(R.id.emptyRecipeButton);

        //setting the text of the urlInput to the value of the urlInput in the viewModel, same for bingSearchInput
        urlInput.setText(viewModel.getUrlInput().getValue());
        bingSearchInput.setText(viewModel.getBingSearchInput().getValue());

        // TextChangedListeners for the urlInput and bingSearchInput, which update the values in the viewModel
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUrlInput(s.toString());
            }
        });

        bingSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setBingSearchInput(s.toString());
            }
        });


        //When the user clicks the button, the url is passed to the WebScraper class which checks if the site is supported and if the site is reachable
        getRecipeFromUrlButton.setOnClickListener(v -> {
            String url = viewModel.getUrlInput().getValue();
            if (url != null && !url.isBlank()) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                String openaiApiKey = sharedPreferences.getString(SettingsFragment.OPENAI_API_KEY, "");
                WebScraper wb = new WebScraper(url, openaiApiKey);
                //creating a new thread for the WebScraper
                ExecutorService service = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                service.execute(() -> {
                    wb.scrapeWebsite();
                    handler.post(() -> {
                        Log.i("Scraping","Scraping done");
                        //checking to see if the site is supported and if the site is reachable
                        if (wb.isNotConnected()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                        } else if (wb.isNotReachable()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                        } else {
                            if (wb.isNotSupported()) {
                                Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_SHORT).show();
                            }
                            //switching to the ShowAndEditRecipeFragment
                            Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(0, null, wb, url);
                            replaceFragment(showAndEditRecipeFragment);
                        }
                    });
                });
                service.shutdown();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No url given", Toast.LENGTH_SHORT).show();
            }
        });

        //When the user clicks this button, an in app browser is going to open in the WebBrowserFragment
        browseWebButton.setOnClickListener(view1 -> replaceFragment(WebBrowserFragment.newInstance("https://www.google.com",true)));

        //When the user clicks this button, the search query is passed to the BingSearch class
        bingSearchButton.setOnClickListener(v -> {
            String bingSearchQuery = viewModel.getBingSearchInput().getValue();
            if (bingSearchQuery != null && !bingSearchQuery.isBlank()) {
                Fragment bingFragment = BingFragment.newInstance(bingSearchQuery);
                replaceFragment(bingFragment);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No search query given", Toast.LENGTH_SHORT).show();
            }
        });

        //When the user clicks this button, the ShowAndEditRecipeFragment is opened with a new empty recipe
        emptyRecipeButton.setOnClickListener(v -> {
            Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(2, null, null, null);
            replaceFragment(showAndEditRecipeFragment);
        });

        return view;
    }


    //This method is used to replace the current fragment with a new fragment
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.clear();
    }
}