package com.example.recipe_app__scraper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrapeFromUrlActivity extends AppCompatActivity {
    private TextView recipeTextOnScreen;
    private TextView ingredientTextOnScreen;
    private TextInputEditText websiteTextInput;
    private TextView cookingTimeTextOnScreen;
    private TextView servingsTextOnScreen;
    private TextView recipeTitleTextOnScreen;
    private Button getUrlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrape_from_url);

        recipeTextOnScreen = findViewById(R.id.recipeTextOnScreen);
        websiteTextInput = findViewById(R.id.websiteTextInput);
        getUrlButton = findViewById(R.id.getUrlButton);
        ingredientTextOnScreen = findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = findViewById(R.id.recipeTitleTextOnScreen);


        getUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = websiteTextInput.getText().toString();
                if(!url.isBlank()){
                    Webscraper wb = new Webscraper(url);
                    wb.execute();
                }
            }
        });
    }
    private class Webscraper extends AsyncTask<Void, Void, Void> {
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

        @Override
        protected Void doInBackground(Void... voids) {
            if(isNetworkAvailable()) {
                Document document = getDocument(url);
                Elements recipeElements = null;
                Elements ingredientElements = null;
                Element servingsElement = null;
                Element cookingTimeElement = null;
                Element recipeTitleElement = null;

                //checking the url to see what classes need to be scraped, don't think this can be done in a switch
                if (url.contains("ah.nl/allerhande/recept")) {
                    recipeElements = document.getElementsByClass("recipe-steps_step__FYhB8");
                    ingredientElements = document.getElementsByClass("recipe-ingredients_ingredientsList__thXVo");

                    servingsElement = document.getElementsByClass("recipe-ingredients_servings__f8HXF").get(0);
                    cookingTimeElement = document.getElementsByClass("recipe-header-time_timeLine__nn84w").get(0);
                    recipeTitleElement = document.getElementsByClass("typography_root__Om3Wh typography_variant-superhero__239x3 typography_hasMargin__4EaQi recipe-header_title__tG0JE").get(0);
                } else if(url.contains("allrecipes.com/recipe")){
                    recipeElements = document.getElementsByClass("comp recipe__steps-content mntl-sc-page mntl-block");
                    ingredientElements = document.getElementsByClass("comp mntl-structured-ingredients");

                    servingsElement = document.getElementsByClass("mntl-recipe-details__value").get(3);
                    cookingTimeElement = document.getElementsByClass("mntl-recipe-details__value").get(0);
                    recipeTitleElement = document.getElementById("article-heading_1-0");
                }
                else {
                    recipeTextList.add("This site is unsupported");
                    ingredientTextList.add("This site is unsupported");
                }

                for(int i = 0; i < (recipeElements != null ? recipeElements.size() : 0); i++){
                    recipeTextList.add(recipeElements.eachText().get(i));
                    recipeTextList.add("\n\n");
                }
                for(int i = 0; i < (ingredientElements != null ? ingredientElements.size() : 0); i++){
                    ingredientTextList.add(ingredientElements.eachText().get(i));
                    ingredientTextList.add("\n\n");
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
                recipeTextList.add("Device not connected to the internet");
                ingredientTextList.add("Device not connected to the internet");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            StringBuilder recipeText = new StringBuilder();
            StringBuilder ingredientText = new StringBuilder();
            for(int i = 0; i < recipeTextList.size(); i++){
                recipeText.append(recipeTextList.get(i));
            }
            for(int i = 0; i < ingredientTextList.size(); i++){
                ingredientText.append(ingredientTextList.get(i));
            }
            recipeTextOnScreen.setText(recipeText);
            ingredientTextOnScreen.setText(ingredientText);
            servingsTextOnScreen.setText("Servings: "+servings);
            cookingTimeTextOnScreen.setText("Cooking time: "+cookingTime + " min");
            recipeTitleTextOnScreen.setText(recipeTitle);

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

    //Checking if the user is connected to the internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}