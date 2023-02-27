package com.example.recipe_app__scraper;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScrapeFromUrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScrapeFromUrlFragment extends Fragment {

    private TextView recipeTextOnScreen;
    private TextView ingredientTextOnScreen;
    private TextInputEditText websiteTextInput;
    private TextView cookingTimeTextOnScreen;
    private TextView servingsTextOnScreen;
    private TextView recipeTitleTextOnScreen;
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

        recipeTextOnScreen = view.findViewById(R.id.recipeTextOnScreen);
        websiteTextInput = view.findViewById(R.id.websiteTextInput);
        getUrlButton = view.findViewById(R.id.getUrlButton);
        ingredientTextOnScreen = view.findViewById(R.id.ingredientTextOnScreen);
        cookingTimeTextOnScreen = view.findViewById(R.id.cookingTimeTextOnScreen);
        servingsTextOnScreen = view.findViewById(R.id.servingsTextOnScreen);
        recipeTitleTextOnScreen = view.findViewById(R.id.recipeTitleTextOnScreen);

        // Inflate the layout for this fragment
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

        return inflater.inflate(R.layout.fragment_scrape_from_url, container, false);
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
            if(InternetConnection.isNetworkAvailable()){
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

}