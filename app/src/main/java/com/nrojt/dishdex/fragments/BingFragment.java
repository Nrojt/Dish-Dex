package com.nrojt.dishdex.fragments;

import static com.nrojt.dishdex.fragments.SettingsFragment.BING_API_KEY;
import static com.nrojt.dishdex.fragments.SettingsFragment.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.internet.SearchResults;
import com.nrojt.dishdex.utils.internet.WebScraper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BingFragment extends Fragment implements FragmentReplacer {
    // Enter a valid subscription key.
    private static String subscriptionKey;

    private static final ArrayList<String> bingReturnUrls = new ArrayList<>();
    private static final ArrayList<String> recipeTitles = new ArrayList<>();
    private static final ArrayList<Integer> recipeCookingTimes = new ArrayList<>();
    private static final ArrayList<Integer> recipeServings = new ArrayList<>();

    private RecyclerView bingRecyclerView;

    /*
     * If you encounter unexpected authorization errors, double-check these values
     * against the endpoint for your Bing Web search instance in your Azure
     * dashboard.
     */
    private static String host = "https://api.bing.microsoft.com";
    private static String path = "/v7.0/search";
    private String searchTerm = null;

    // the fragment initialization parameters
    private static final String SEARCH_QUERY = "param1";

    public BingFragment() {
        // Required empty public constructor
    }

    public static BingFragment newInstance(String searchTerm) {
        BingFragment fragment = new BingFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_QUERY, searchTerm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchTerm = getArguments().getString(SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bing, container, false);

        bingRecyclerView = view.findViewById(R.id.bingRecyclerView);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        subscriptionKey = sharedPreferences.getString(BING_API_KEY, "");

        if(searchTerm != null) {
            bingReturnUrls.clear();
            recipeTitles.clear();
            recipeCookingTimes.clear();
            recipeServings.clear();

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            // Run the search on a background thread.
            service.execute(() -> {
                runTheSearch();
                handler.post(() -> {
                    if(bingReturnUrls.size() > 0){
                        scrapeLink();
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                    //setting the search term back to null so that the search doesn't run again when the fragment is recreated
                    searchTerm = null;
                });
            });
            service.shutdown();


        }

        // Inflate the layout for this fragment
        return view;
    }

    //Just a proof of concept, should use a recyclerview instead to show all the results
    private void scrapeLink(){
        //TODO: recyclerview in stead of just using the first url
        String url = bingReturnUrls.get(0);
        WebScraper wb = new WebScraper(url);
        //creating a new thread for the WebScraper
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        service.execute(() -> {
            wb.scrapeWebsite();
            handler.post(() -> {
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
                    Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(0, -1, wb, url);
                    replaceFragment(showAndEditRecipeFragment);
                }
            });
        });
        service.shutdown();

    }

    private void runTheSearch() {
        JSONObject jsonObject;
        SearchResults results;
        JSONArray jsonArray;

        try {
            results = SearchWeb(searchTerm);
            jsonObject = new JSONObject(results.jsonResponse);

            if (jsonObject.has("webPages")) {
                jsonArray = jsonObject.getJSONObject("webPages").getJSONArray("value");
                for (int i = 0; i < jsonArray.length(); i++) {
                    bingReturnUrls.add(jsonArray.getJSONObject(i).getString("url"));
                }
            } else {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: No results found.", Toast.LENGTH_SHORT).show());
            }

        } catch (FileNotFoundException e) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: API key is invalid or the API endpoint is unreachable.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //SearchWeb was provided on the Microsoft bing api v7 website
    public SearchResults SearchWeb(String searchQuery) throws Exception {
        // Construct the URL.
        URL url = new URL(host + path + "?q=" + URLEncoder.encode(searchQuery, "UTF-8") + "&responseFilter=webpages&safeSearch=strict");

        // Open the connection.
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

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

    // Method for replacing the fragment (redirecting to main activity)
    @Override
    public void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }
}