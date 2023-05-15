package com.nrojt.dishdex.backend.viewmodels;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nrojt.dishdex.backend.Category;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.utils.database.MyDatabaseHelper;
import com.nrojt.dishdex.utils.internet.SearchResults;
import com.nrojt.dishdex.utils.internet.WebScraper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

//TODO savedinstance so it doesn't have to search again when opening a recipe and going back
//TODO fix network on main thread exception
public class BingFragmentViewModel extends ViewModel {
    private MutableLiveData<String> bingApiKey;
    private MutableLiveData<ArrayList<String>> bingReturnUrls;
    private MutableLiveData<ArrayList<Recipe>> recipes;
    private MutableLiveData<String> searchTerm;
    private MutableLiveData<String> openaiApiKey;
    private MutableLiveData<Integer> recipeChangeCounter;
     private boolean searchTermChanged = false;

    private static final String host = "https://api.bing.microsoft.com";
    private static final String path = "/v7.0/search";

    public BingFragmentViewModel() {
        bingApiKey = new MutableLiveData<>();
        openaiApiKey = new MutableLiveData<>();
        bingReturnUrls = new MutableLiveData<>();
        recipes = new MutableLiveData<>();
        searchTerm = new MutableLiveData<>();
        recipeChangeCounter = new MutableLiveData<>();

        bingReturnUrls.setValue(new ArrayList<>());
        recipes.setValue(new ArrayList<>());
    }

    public void setBingApiKey(String bingApiKey) {
        this.bingApiKey.setValue(bingApiKey);
    }

    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey.setValue(openaiApiKey);
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm.setValue(searchTerm);
        searchTermChanged = true;
    }

    public MutableLiveData<String> getSearchTermMutable() {
        return searchTerm;
    }

    public String getSearchTerm() {
        if(searchTerm.getValue() == null) {
            searchTerm.setValue("");
        }
        return searchTerm.getValue();
    }

    public ArrayList<Recipe> getRecipes() {
        return recipes.getValue();
    }

    //Observing a mutable live data arraylist seems to not be possible, so just using integer and changing it.
    public MutableLiveData<Integer> getRecipeChangeCounter() {
        return recipeChangeCounter;
    }

    public void searchForRecipes(Context context) {
        if(searchTerm.getValue() == null || searchTerm.getValue().equals("")) {
            Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        if(searchTermChanged) {
            bingReturnUrls.getValue().clear();
            recipes.getValue().clear();

            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> {
                try {
                    runTheSearch();
                    for (String url : bingReturnUrls.getValue()) {
                        System.out.println(url);
                        try {
                            recipes.getValue().add(scrapeLink(url, context));
                            recipeChangeCounter.postValue(recipes.getValue().size());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            searchTermChanged = false;
        }
    }

    private void runTheSearch() {
        JSONObject jsonObject;
        SearchResults results;
        JSONArray jsonArray;

        try {
            results = SearchWeb(searchTerm.getValue());
            jsonObject = new JSONObject(results.jsonResponse);

            if (jsonObject.has("webPages")) {
                jsonArray = jsonObject.getJSONObject("webPages").getJSONArray("value");
                for (int i = 0; i < jsonArray.length(); i++) {
                    bingReturnUrls.getValue().add(jsonArray.getJSONObject(i).getString("url"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //SearchWeb was provided on the Microsoft bing api v7 website
    private SearchResults SearchWeb(String searchQuery) throws Exception {
        // Construct the URL.
        URL url = new URL(host + path + "?q=" + URLEncoder.encode(searchQuery, "UTF-8") + "&responseFilter=webpages&safeSearch=strict");

        // Open the connection.
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", bingApiKey.getValue());

        // Receive the JSON response body.
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();

        // Construct the result object.
        SearchResults results = new SearchResults(new HashMap<>(), response);

        // Extract Bing-related HTTP headers.
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String header : headers.keySet()) {
            if (header == null) continue;      // may have null key
            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
                results.relevantHeaders.put(header, headers.get(header).get(0));
            }
        }
        stream.close();
        return results;
    }

    //getting the titles, cooking times and servings from the links
    private Recipe scrapeLink(String url, Context context){
        Recipe recipe;
        WebScraper wb = new WebScraper(url, openaiApiKey.getValue());
        wb.scrapeWebsite();
        //checking to see if the site is supported and if the site is reachable
        if (wb.isNotConnected() || wb.isNotReachable()) {
            recipe = new Recipe();
            Log.e("BingFragment", "Not connected to the internet or cannot reach this site: " + url);
        } else {
            ArrayList<Category> scrapedCategory = setCategoryFromScraper(wb, context);
            recipe = new Recipe(wb.getRecipeTitle(), wb.getIngredientText().toString(), wb.getRecipeText().toString(), "", wb.getUrl(), -1, wb.getCookingTime(), wb.getServings(), !wb.isNotSupported(), scrapedCategory); //if the site is supported, isNotSupported will return false. Here we need to know if the site is supported, so we invert the boolean
        }
        System.out.println(recipe.getRecipeTitle());
        return recipe;
    }

    //setting the category that got scraped by web scraper. Recipe requires an arraylist of categories, so we make an arraylist here containing 1 category
    private ArrayList<Category> setCategoryFromScraper(WebScraper wb, Context context){
        ArrayList<Category> scrapedCategory = new ArrayList<>();
        ArrayList<Category> allCategories = new ArrayList<>();

        try (MyDatabaseHelper db = MyDatabaseHelper.getInstance(context)) {
            Cursor cursor = db.readAllDataFromCategories();
            if (cursor.getCount() == 0) {
                //This should never happen, since the database is created with default categories
                Toast.makeText(context, "No Categories", Toast.LENGTH_SHORT).show();
            } else {
                while (cursor.moveToNext()) {
                    Category category = new Category(cursor.getInt(1), cursor.getString(0));
                    allCategories.add(category);
                }
            }

            cursor.close();
        } catch (SQLException e) {
            Log.e("BingFragment Database", e.getMessage());
        }

        int categoryIDFromScraper = wb.getRecipeCategoryID();
        if(categoryIDFromScraper > 0){
            for (int i = 0; i < allCategories.size(); i++) {
                if (allCategories.get(i).getCategoryID() == categoryIDFromScraper){
                    scrapedCategory.add(allCategories.get(i));
                }
            }
        }
        return scrapedCategory;
    }
}
