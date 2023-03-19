package com.nrojt.dishdex.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.nrojt.dishdex.R;
import com.nrojt.utils.internet.WebScraper;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScrapeFromUrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScrapeFromUrlFragment extends Fragment {
    private String url = "";
    private WebScraper wb;
    private Button saveRecipeButton;
    private EditText recipeTextOnScreen;
    private EditText ingredientTextOnScreen;

    private EditText cookingTimeTextOnScreen;
    private EditText recipeTitleTextOnScreen;
    private EditText servingsTextOnScreen;

    private EditText noteTextOnScreen;
    private EditText urlTextOnScreen;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ScrapeFromUrlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScrapeFromUrlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScrapeFromUrlFragment newInstance(String param1, String param2) {
        ScrapeFromUrlFragment fragment = new ScrapeFromUrlFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scrape_from_url, container, false);

        //inflating the on screen elements
        recipeTextOnScreen = view.findViewById(R.id.recipeTextOnScreen);
        ingredientTextOnScreen = view.findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = view.findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = view.findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = view.findViewById(R.id.recipeTitleTextOnScreen);
        noteTextOnScreen = view.findViewById(R.id.noteTextOnScreen);
        saveRecipeButton = view.findViewById(R.id.saveRecipeButton);
        urlTextOnScreen = view.findViewById(R.id.urlTextOnScreen);

        //getting the url and WebScraper from the bundle
        Bundle bundle = getArguments();
        wb = (WebScraper) bundle.getSerializable("WebScraper");
        url = bundle.getString("Url");

        //setting the on screen elements
        recipeTextOnScreen.setText(wb.getRecipeText());
        ingredientTextOnScreen.setText(wb.getIngredientText());
        cookingTimeTextOnScreen.setText(String.valueOf(wb.getCookingTime()));
        servingsTextOnScreen.setText(String.valueOf(wb.getServings()));
        recipeTitleTextOnScreen.setText(wb.getRecipeTitle());
        urlTextOnScreen.setText(url);

        //setting the saveRecipeButton to be invisible when the keyboard is up. It does this by checking the height of the screen and the height of the keyboard and if the difference is greater than a certain threshold, it hides the button.
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                view.getWindowVisibleDisplayFrame(r);
                int screenHeight = view.getRootView().getHeight();
                Handler handler = new Handler();

                // Calculate the difference between the visible screen height and the total screen height
                int heightDifference = screenHeight - (r.bottom - r.top);

                // Set an arbitrary threshold to determine if the keyboard is visible
                if (heightDifference > screenHeight / 4) {
                    saveRecipeButton.setVisibility(View.GONE); // Hide the saveRecipeButton
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveRecipeButton.setVisibility(View.VISIBLE); // Show the saveRecipeButton
                        }
                    }, 100); // Delay the showing of the saveRecipeButton to avoid flickering
                }
            }
        });

        return view;
    }

}