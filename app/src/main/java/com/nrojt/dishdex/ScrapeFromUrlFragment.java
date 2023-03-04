package com.nrojt.dishdex;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScrapeFromUrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScrapeFromUrlFragment extends Fragment {

    private EditText recipeTextOnScreen;
    private EditText ingredientTextOnScreen;
    private TextInputEditText urlInput;
    private EditText cookingTimeTextOnScreen;
    private EditText servingsTextOnScreen;
    private EditText recipeTitleTextOnScreen;
    private Button getUrlButton;

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
        View view = inflater.inflate(R.layout.fragment_scrape_from_url, container, false);

        //inflating the on screen elements
        recipeTextOnScreen = view.findViewById(R.id.recipeTextOnScreen);
        urlInput = view.findViewById(R.id.urlInput);
        getUrlButton = view.findViewById(R.id.getUrlButton);
        ingredientTextOnScreen = view.findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = view.findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = view.findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = view.findViewById(R.id.recipeTitleTextOnScreen);

        //onclick listener for the getUrlButton
        getUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = String.valueOf(urlInput.getText());
                if(!url.isBlank()) {
                    Webscraper wb = new Webscraper(url);
                    wb.execute();
                }
            }
        });

        return view;
    }

    //Class for scraping the websites
    private class Webscraper extends AsyncTask<Void, Void, Void> {
        boolean notConnected = false;
        boolean notSupported = false;
        String url;
        int servings = 0;
        int cookingTime = 0;
        String recipeTitle = "Unknown";
        List<String> recipeTextList = new ArrayList<>();
        List<String> ingredientTextList = new ArrayList<>();

        public Webscraper(String url) {
            if(!(url.contains("http://") || url.contains("https://"))){
                this.url = "https://"+url;
            } else{
                this.url = url;
            }
        }

        //async
        @Override
        protected Void doInBackground(Void... voids) {
            if(InternetConnection.isNetworkAvailable()){
                Document document = getDocument(url);
                Elements instructionElements = null;
                Elements ingredientElements = null;
                Element servingsElement = null;
                Element cookingTimeElement = null;
                Element recipeTitleElement = null;

                //checking the url to see what classes need to be scraped, don't think this can be done in a switch
                if (url.contains("ah.nl/allerhande/recept")) {
                    instructionElements = document.getElementsByClass("recipe-steps_step__FYhB8");
                    Elements ahIngredientAmount = document.getElementsByClass("typography_root__Om3Wh typography_variant-paragraph__T5ZAU typography_weight-strong__uEXiN typography_hasMargin__4EaQi ingredient_unit__-ptEq");
                    Elements ahIngredientNames = document.getElementsByClass("typography_root__Om3Wh typography_variant-paragraph__T5ZAU typography_hasMargin__4EaQi ingredient_name__WXu5R");

                    for(int i = 0; i < ahIngredientNames.size(); i++){
                        ingredientTextList.add(ahIngredientAmount.eachText().get(i) + " " + ahIngredientNames.eachText().get(i));
                        ingredientTextList.add("\n");
                    }

                    servingsElement = document.getElementsByClass("recipe-ingredients_servings__f8HXF").get(0);
                    cookingTimeElement = document.getElementsByClass("recipe-header-time_timeLine__nn84w").get(0);
                    recipeTitleElement = document.getElementsByClass("typography_root__Om3Wh typography_variant-superhero__239x3 typography_hasMargin__4EaQi recipe-header_title__tG0JE").get(0);
                } else if(url.contains("allrecipes.com/recipe")){
                    instructionElements = document.getElementsByClass("comp mntl-sc-block-group--LI mntl-sc-block mntl-sc-block-startgroup");
                    ingredientElements = document.getElementsByClass("mntl-structured-ingredients__list-item ");

                    servingsElement = document.getElementsByClass("mntl-recipe-details__value").get(3);
                    cookingTimeElement = document.getElementsByClass("mntl-recipe-details__value").get(0);
                    recipeTitleElement = document.getElementById("article-heading_1-0");
                } else if (url.contains("food.com/recipe")){
                    instructionElements = document.getElementsByClass("direction svelte-ovaflp");
                    Elements foodComIngredientNames = document.getElementsByClass("ingredient-text svelte-ovaflp");
                    Elements foodComIngredientAmount = document.getElementsByClass("ingredient-quantity svelte-ovaflp");


                    for(int i = 0; i < foodComIngredientAmount.eachText().size(); i++){
                        ingredientTextList.add(foodComIngredientAmount.eachText().get(i) + " " + foodComIngredientNames.eachText().get(i));
                        ingredientTextList.add("\n");
                    }

                    for(int i = foodComIngredientAmount.eachText().size(); i < foodComIngredientNames.size(); i++){
                        ingredientTextList.add(foodComIngredientNames.eachText().get(i));
                        ingredientTextList.add("\n");
                    }


                    servingsElement = document.getElementsByClass("adjust svelte-1o10zxc").get(0);
                    cookingTimeElement = document.getElementsByClass("facts__item svelte-ovaflp").get(0);
                    recipeTitleElement = document.getElementsByClass("layout__item title svelte-ovaflp").get(0);
                }
                else {
                    notSupported = true;
                }

                for(int i = 0; i < (instructionElements != null ? instructionElements.size() : 0); i++){
                    recipeTextList.add(instructionElements.eachText().get(i));
                    recipeTextList.add("\n\n");
                }
                for(int i = 0; i < (ingredientElements != null ? ingredientElements.size() : 0); i++){
                    ingredientTextList.add(ingredientElements.eachText().get(i));
                    ingredientTextList.add("\n");
                }


                if (servingsElement != null) {
                    servings = Integer.parseInt(servingsElement.text().replaceAll("[^0-9]", ""));
                }
                if (cookingTimeElement != null) {
                    cookingTime = Integer.parseInt(cookingTimeElement.text().replaceAll("[^0-9]", ""));
                }

                if (recipeTitleElement != null) {
                    recipeTitle = recipeTitleElement.text();
                }
            } else{
                notConnected = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(notSupported){
                Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_LONG).show();
            } else if(notConnected){
                Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_LONG).show();
            } else {
                StringBuilder recipeText = new StringBuilder();
                StringBuilder ingredientText = new StringBuilder();
                for (int i = 0; i < recipeTextList.size(); i++) {
                    recipeText.append(recipeTextList.get(i));
                }
                for (int i = 0; i < ingredientTextList.size(); i++) {
                    ingredientText.append(ingredientTextList.get(i));
                }
                recipeTextOnScreen.setText(recipeText);
                ingredientTextOnScreen.setText(ingredientText);
                servingsTextOnScreen.setText("Servings: " + servings);
                cookingTimeTextOnScreen.setText("Cooking time: " + cookingTime + " min");
                recipeTitleTextOnScreen.setText(recipeTitle);
            }
        }

    }
    private static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url);
        Document document = null;
        conn.userAgent("Chrome");
        conn.followRedirects(true);
        try {
            document = conn.get();
        } catch (IOException e) {
            e.printStackTrace();
            // handle error
        }
        return document;
    }

}