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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrapeFromUrlActivity extends AppCompatActivity {
    private TextView recipeTextOnScreen;
    private TextInputEditText websiteTextInput;
    private Button getUrlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrape_from_url);

        recipeTextOnScreen = findViewById(R.id.recipeTextOnScreen);
        websiteTextInput = findViewById(R.id.websiteTextInput);
        getUrlButton = findViewById(R.id.getUrlButton);


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
                Elements recipeElements;
                Elements ingredientElements;
                if (url.contains("ah.nl/allerhande/recept")) {
                    recipeElements = document.getElementsByClass("recipe-steps_step__FYhB8");
                    ingredientElements = document.getElementsByClass("recipe-ingredients_ingredientsList__thXVo");
                    recipeTextList = recipeElements.eachText();
                    ingredientTextList = ingredientElements.eachText();
                } else {
                    recipeTextList.add("This site is unsupported");
                }
            } else{
                recipeTextList.add("Device not connected to the internet");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            StringBuilder recipeText = new StringBuilder();
            for(int i = 0; i < recipeTextList.size(); i++){
                recipeText.append(recipeTextList.get(i));
                recipeText.append("\n");
            }
            recipeTextOnScreen.setText(recipeText);

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}