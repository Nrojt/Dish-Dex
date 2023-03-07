package com.nrojt.dishdex;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipeChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRecipeChooserFragment extends Fragment {
    private TextInputEditText urlInput;
    private Button getUrlButton;

    private Button bingSearchButton;
    private TextInputEditText bingSearchInput;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddRecipeChooserFragment.
     */
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
        View view = inflater.inflate(R.layout.fragment_add_recipe_chooser, container, false);

        urlInput = view.findViewById(R.id.urlInput);
        getUrlButton = view.findViewById(R.id.getUrlButton);
        bingSearchButton = view.findViewById(R.id.bingSearchButton);
        bingSearchInput = view.findViewById(R.id.bingSearchInput);

        getUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String url = String.valueOf(urlInput.getText());
                    if (!url.isBlank()) {
                        WebScraper wb = new WebScraper(url);
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        service.execute(new Runnable() {
                            @Override
                            public void run() {
                                wb.scrapeWebsite();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (wb.isNotSupported()) {
                                            Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_SHORT).show();
                                        } else if (wb.isNotConnected()) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                                        } else if(wb.isNotReachable()){
                                            Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Fragment scrapeUrlfragment = new ScrapeFromUrlFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("WebScraper", wb);
                                            bundle.putString("Url", url);
                                            scrapeUrlfragment.setArguments(bundle);
                                            replaceFragment(scrapeUrlfragment);
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No url given", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        bingSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bingSearchQuery = bingSearchInput.getText().toString();
                if(!bingSearchQuery.isBlank()){

                } else{
                    Toast.makeText(getActivity().getApplicationContext(), "No search query given", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void hideAllViewsBeforeSwitchingFragments(){
        getUrlButton.setVisibility(View.GONE);
        urlInput.setVisibility(View.GONE);
        bingSearchButton.setVisibility(View.GONE);
        bingSearchInput.setVisibility(View.GONE);
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.framelayout, fragment, null);
        hideAllViewsBeforeSwitchingFragments();
        fragmentTransaction.commit();
    }
}