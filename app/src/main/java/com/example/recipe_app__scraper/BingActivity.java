package com.example.recipe_app__scraper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class BingActivity extends AppCompatActivity {

    // Enter a valid subscription key.
    static String subscriptionKey;

    /*
     * If you encounter unexpected authorization errors, double-check these values
     * against the endpoint for your Bing Web search instance in your Azure
     * dashboard.
     */
    static String host = "https://api.bing.microsoft.com";
    static String path = "/v7.0/search";
    static String searchTerm;

    private TextView bingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bing);
        Button bingSearchButton = findViewById(R.id.bingSearchButton);
        TextInputEditText bingApiKeyInput = findViewById(R.id.bingApiKeyInput);
        TextInputEditText bingSearchInput = findViewById(R.id.bingSearchInput);
        bingText = findViewById(R.id.bingText);

        bingSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionKey = String.valueOf(bingApiKeyInput.getText());
                searchTerm = String.valueOf(bingSearchInput.getText());
                // Confirm the subscriptionKey is valid.
                if (subscriptionKey.length() != 32) {
                    bingText.setText("Invalid Bing Search API key!");
                } else if(searchTerm.isBlank()){
                    bingText.setText("No search query given");
                }
                else {
                    if(isNetworkAvailable()) {
                        BingWebSearch bong = new BingWebSearch();
                        bong.execute();
                    } else {
                        bingText.setText("Please connect to the internet");
                    }
                }
            }
        });
    }


    private class BingWebSearch extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Call the SearchWeb method and print the response.
            try {
                SearchResults result = SearchWeb(searchTerm);

            } catch (Exception e) {
                e.printStackTrace(System.out);
                System.exit(1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
        }
    }

        public static SearchResults SearchWeb (String searchQuery) throws Exception {
        // Construct the URL.
        URL url = new URL(host + path + "?q=" + URLEncoder.encode(searchQuery, "UTF-8"));

        // Open the connection.
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

        // Receive the JSON response body.
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();

        // Construct the result object.
        SearchResults results = new SearchResults(new HashMap<String, String>(), response);

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

class SearchResults{
    HashMap<String, String> relevantHeaders;
    String jsonResponse;
    SearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}