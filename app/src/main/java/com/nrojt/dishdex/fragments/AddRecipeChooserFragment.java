package com.nrojt.dishdex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.internet.WebScraper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipeChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRecipeChooserFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener{
    private TextInputEditText urlInput;
    private Button getRecipeFromUrlButton;
    private Button browseWebButton;
    private Button emptyRecipeButton;
    private Button bingSearchButton;
    private TextInputEditText bingSearchInput;

    private FragmentManager fragmentManager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddRecipeChooserFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_recipe_chooser, container, false);

        //initializing the variables, assigning them to the corresponding on screen views
        urlInput = view.findViewById(R.id.urlInput);
        getRecipeFromUrlButton = view.findViewById(R.id.getRecipeFromUrlButton);
        bingSearchButton = view.findViewById(R.id.bingSearchButton);
        bingSearchInput = view.findViewById(R.id.bingSearchInput);
        browseWebButton = view.findViewById(R.id.browseWebButton);
        emptyRecipeButton = view.findViewById(R.id.emptyRecipeButton);

        //setting the visibility of the bing search button and input to invisible, because they're not at a working state yet
        bingSearchButton.setVisibility(View.INVISIBLE);
        bingSearchInput.setVisibility(View.INVISIBLE);

        //When the user clicks the button, the url is passed to the WebScraper class which checks if the site is supported and if the site is reachable
        getRecipeFromUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    System.out.println("Button clicked");
                    String url = urlInput.getText().toString();
                    if (!url.isBlank()) {
                        WebScraper wb = new WebScraper(url);
                        //creating a new thread for the WebScraper
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        service.execute(new Runnable() {
                            @Override
                            public void run() {
                                wb.scrapeWebsite();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("Scraping done");
                                        //checking to see if the site is supported and if the site is reachable
                                        if (wb.isNotSupported()) {
                                            Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_SHORT).show();
                                        } else if (wb.isNotConnected()) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                                        } else if(wb.isNotReachable()){
                                            Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            //switching to the ShowAndEditRecipeFragment
                                            Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(0, -1, wb, url);
                                            replaceFragment(showAndEditRecipeFragment);
                                        }
                                    }
                                });
                            }
                        });
                        service.shutdown();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No url given", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        //When the user clicks this button, an in app browser is going to open in the WebBrowserFragment
        browseWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new WebBrowserFragment());
            }
        });

        //When the user clicks this button, the search query is passed to the BingSearch class
        bingSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bingSearchQuery = bingSearchInput.getText().toString();
                if(!bingSearchQuery.isBlank()){
                    Fragment bingFragment = BingFragment.newInstance(bingSearchQuery);
                    replaceFragment(bingFragment);
                } else{
                    Toast.makeText(getActivity().getApplicationContext(), "No search query given", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emptyRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(2, -1, null, null);
                replaceFragment(showAndEditRecipeFragment);
            }
        });

        return view;
    }


    //This method is used to replace the current fragment with a new fragment
    @Override
    public void replaceFragment(Fragment fragment){
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
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