package com.nrojt.dishdex.fragments;

import static com.nrojt.dishdex.fragments.SettingsFragment.BING_API_KEY;
import static com.nrojt.dishdex.fragments.SettingsFragment.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.RecyclerViewInterface;
import com.nrojt.dishdex.utils.internet.SearchResults;
import com.nrojt.dishdex.utils.internet.WebScraper;
import com.nrojt.dishdex.utils.recycler.CustomItemPaddingDecoration;
import com.nrojt.dishdex.utils.recycler.SavedRecipesCustomRecyclerAdapter;

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

//TODO show which sites are supported

public class BingFragment extends Fragment implements RecyclerViewInterface, FragmentReplacer {
    private static String subscriptionKey;

    private static final ArrayList<String> bingReturnUrls = new ArrayList<>();
    private static final ArrayList<String> recipeTitles = new ArrayList<>();
    private static final ArrayList<Integer> recipeCookingTimes = new ArrayList<>();
    private static final ArrayList<Integer> recipeServings = new ArrayList<>();
    private static final ArrayList<String> recipeAccessibleUrl = new ArrayList<>();
    private static final ArrayList<Boolean> recipeUrlSupported = new ArrayList<>();

    private RecyclerView bingRecyclerView;

    private TextView bingNotificationTextView;


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

        bingNotificationTextView = view.findViewById(R.id.bingNotificationTextView);
        bingNotificationTextView.setTextSize(MainActivity.fontSizeTitles);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        subscriptionKey = sharedPreferences.getString(BING_API_KEY, "");

        bingRecyclerView = view.findViewById(R.id.bingRecyclerView);
        bingRecyclerView.addItemDecoration(new CustomItemPaddingDecoration(20));
        SavedRecipesCustomRecyclerAdapter adapter = new SavedRecipesCustomRecyclerAdapter(getContext(), recipeTitles, recipeCookingTimes, recipeServings, this);
        bingRecyclerView.setAdapter(adapter);
        bingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        if(searchTerm != null) {
            bingNotificationTextView.setText("Searching for " + searchTerm + "...");
            bingReturnUrls.clear();
            recipeTitles.clear();
            recipeCookingTimes.clear();
            recipeServings.clear();
            recipeAccessibleUrl.clear();
            recipeUrlSupported.clear();

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            // Run the search on a background thread.
            service.execute(() -> {
                runTheSearch();
                if(bingReturnUrls.size() > 0){
                    for(int i = 0; i < bingReturnUrls.size(); i++){
                        scrapeLink(bingReturnUrls.get(i));
                    }
                } else {
                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                }
                handler.post(() -> {
                    //setting the search term back to null so that the search doesn't run again when the fragment is recreated
                    searchTerm = null;
                    adapter.notifyDataSetChanged();
                    bingNotificationTextView.setText("Swipe to the right to open the recipe in the browser");
                });
            });
            service.shutdown();
        }

        //swipe to the right to open the recipe in the browser
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if(recipeUrlSupported.get(position)){
                    replaceFragment(WebBrowserFragment.newInstance(recipeAccessibleUrl.get(position)));
                } else {
                    Toast.makeText(getContext(), "This site is not supported", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(bingRecyclerView);

        return view;
    }

    //getting the titles, cooking times and servings from the links
    private void scrapeLink(String url){
        WebScraper wb = new WebScraper(url);
        wb.scrapeWebsite();
        //checking to see if the site is supported and if the site is reachable
        if (wb.isNotConnected() || wb.isNotReachable()) {
            Log.e("BingFragment", "Not connected to the internet or cannot reach this site: " + url);
        } else {
            recipeTitles.add(wb.getRecipeTitle());
            recipeCookingTimes.add(wb.getCookingTime());
            recipeServings.add(wb.getServings());
            recipeAccessibleUrl.add(wb.getUrl());
            recipeUrlSupported.add(!wb.isNotSupported()); //if the site is supported, isNotSupported will return false. Here we need to know if the site is supported, so we invert the boolean
        }
    }

    //Just a proof of concept, should use a recyclerview instead to show all the results
    private void openLink(String url){
        WebScraper wb = new WebScraper(url);
        //creating a new thread for the WebScraper
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        service.execute(() -> {
            wb.scrapeWebsite();
            handler.post(() -> {
                //checking again to see if the site is reachable
                if (wb.isNotConnected()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                } else if (wb.isNotReachable()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                } else {
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
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());
        }
    }

    @Override
    public void onItemClick(int position) {
        openLink(recipeAccessibleUrl.get(position));
    }
}