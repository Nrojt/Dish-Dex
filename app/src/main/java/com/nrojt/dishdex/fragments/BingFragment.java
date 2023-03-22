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
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.internet.SearchResults;

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
public class BingFragment extends Fragment {
    // Enter a valid subscription key.
    static String subscriptionKey;

    ArrayList<String> bingReturnUrls;

    /*
     * If you encounter unexpected authorization errors, double-check these values
     * against the endpoint for your Bing Web search instance in your Azure
     * dashboard.
     */
    static String host = "https://api.bing.microsoft.com";
    static String path = "/v7.0/search";
    static String searchTerm;

    private EditText bingUrlTextOnScreen;
    //TODO fix issue where the on screen text doesn't update


    // the fragment initialization parameters
    private static final String SEARCH_QUERY = "param1";

    public BingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
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
        bingUrlTextOnScreen = view.findViewById(R.id.bingUrlTextOnScreen);
        bingUrlTextOnScreen.setText("Loading...");

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        subscriptionKey = sharedPreferences.getString(BING_API_KEY, "");

        Bundle bundle = getArguments();
        searchTerm = bundle.getString("SearchQuery", "");

        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        service.execute(new Runnable() {
            @Override
            public void run() {
                runTheSearch();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        System.out.println(bingReturnUrls);
                        for(String url : bingReturnUrls){
                            sb.append(url);
                            sb.append("\n");
                        }
                        bingUrlTextOnScreen.setText(sb.toString());
                    }
                });
            }
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bing, container, false);
    }

    private void runTheSearch(){
        JSONObject jsonObject;
        SearchResults results;
        JSONArray jsonArray;
        bingReturnUrls = new ArrayList<>();

        try {
            results = SearchWeb(searchTerm);
            jsonObject = new JSONObject(results.jsonResponse);

            if (jsonObject.has("webPages")) {
                jsonArray = jsonObject.getJSONObject("webPages").getJSONArray("value");
                for (int i = 0; i < jsonArray.length(); i++) {
                    bingReturnUrls.add(jsonArray.getJSONObject(i).getString("url"));
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error: No results found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (FileNotFoundException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Error: API key is invalid or the API endpoint is unreachable.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //SearchWeb was provided on the Microsoft bing api v7 website
    public SearchResults SearchWeb(String searchQuery) throws Exception {
        // Construct the URL.
        URL url = new URL(host + path + "?q=" +  URLEncoder.encode(searchQuery, "UTF-8") + "&responseFilter=webpages&safeSearch=strict");

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
}




/*
JSON Response example from the bing api v7:

{
  "_type": "SearchResponse",
  "queryContext": {
    "originalQuery": "Noodle recipe"
  },
  "webPages": {
    "webSearchUrl": "https://www.bing.com/search?q\u003dNoodle+recipe",
    "totalEstimatedMatches": 1680000,
    "value": [
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.0",
        "name": "Noodle recipes | BBC Good Food",
        "url": "https://www.bbcgoodfood.com/recipes/collection/noodle-recipes",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.bbcgoodfood.com/recipes/collection/noodle",
        "snippet": "Noodles make the perfect speedy, midweek supper. Flash fry some egg noodles for an Asian-style dish, or experiment with buckwheat, rice or soba noodles. Singapore noodles 46 ratings A low-fat, low-calorie stir-fry of pork and prawns, flavoured with teriyaki, madras and five-spice powder Peking-style noodles 20 ratings",
        "dateLastCrawled": "2023-03-15T01:33:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.1",
        "name": "Noodle Recipes",
        "url": "https://www.allrecipes.com/recipes/530/pasta-and-noodles/noodles/",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.allrecipes.com/recipes/530/pasta-and-noodles/noodles",
        "snippet": "Noodle Recipes Recipes Pasta and Noodles Noodles In a noodly kind of mood? Find recipes for egg noodles, udon, yakisoba, and rice noodles. Ramen Noodles Rice Noodles Chow Mein Noodles Egg Noodles Spicy Shrimp Ramen Noodles with Asparagus Chicken Teriyaki and Noodles Ground Beef Stroganoff Noodles 24 Ratings Lasagna Casserole 3 Ratings",
        "dateLastCrawled": "2023-03-15T19:31:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.2",
        "name": "The 20 best noodle recipes | Noodles | The Guardian",
        "url": "https://www.theguardian.com/food/2020/mar/23/observer-food-monthly-20-best-noodle-recipes",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.theguardian.com/food/2020/mar/23/observer-food-monthly-20-best-noodle-recipes",
        "snippet": "N oodles: the perfect lunch or dinner via Italy, Spain and China. All the best recipes are here, from Marcella’s spaghetti aio e oio to Nigella’s drunken noodles. There are delicious fragrant...",
        "dateLastCrawled": "2023-03-13T18:30:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.3",
        "name": "Chicken noodle recipes | BBC Good Food",
        "url": "https://www.bbcgoodfood.com/recipes/collection/chicken-noodle-recipes",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.bbcgoodfood.com/recipes/collection/chicken-noodle",
        "snippet": "A lean Asian-style rice noodle stir-fry flavoured with soy, chilli and coriander and packed with vegetable goodness Grilled chicken \u0026 noodles (Bun ga nuong) 7 ratings Serve spiced chicken thigh patties with a caramel glaze on a bed of rice noodles and julienned carrots for a classic Vietnamese dish bursting with flavour Chicken chow mein home made",
        "dateLastCrawled": "2023-03-14T20:08:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.4",
        "name": "25 Noodles Recipes | olivemagazine",
        "url": "https://www.olivemagazine.com/recipes/collection/best-ever-noodles-recipes/",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.olivemagazine.com/recipes/collection/best-ever-noodles-recipes",
        "snippet": "A quick and easy Thai noodle soup recipe. Beef, ginger and spring onion noodles Noodles are joined by succulent sirloin steak, fiery ginger and crunchy spring onions in this no-nonsense recipe. Ready in just 20 minutes, make this dish for a simple midweek meal for two. Yaki udon noodles",
        "dateLastCrawled": "2023-03-14T16:50:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.5",
        "name": "27 Easy Noodle Recipes - Insanely Good",
        "url": "https://insanelygoodrecipes.com/noodle-recipes/",
        "isFamilyFriendly": true,
        "displayUrl": "https://insanelygoodrecipes.com/noodle-recipes",
        "snippet": "Thai Peanut Noodles Recipe If you have peanut butter, soy sauce, sesame oil, chili paste, and brown sugar, you’ll have the makings of one tasty noodle dish! If you don’t have chili paste, you can use Sriracha instead. Just whisk it all together and toss it with the cooked noodles.",
        "dateLastCrawled": "2023-03-14T04:23:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.6",
        "name": "Noodles recipes - BBC Food",
        "url": "https://www.bbc.co.uk/food/noodle",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.bbc.co.uk/food/noodle",
        "snippet": "Cook this authentic Chinese chow mein noodle stir-fry in less than ten minutes, adding any crisp seasonal vegetables you fancy. Each serving provides 580 kcal, 49g protein, 67g carbohydrates (of...",
        "dateLastCrawled": "2023-03-13T18:28:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.7",
        "name": "25 Best Noodle Recipes From Around the World - The Spruce Eats",
        "url": "https://www.thespruceeats.com/best-noodle-recipes-from-around-the-world-4845735",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.thespruceeats.com/best-noodle-recipes-from-around-the-world-4845735",
        "snippet": "Our easy recipe delivers all the flavors of the traditional dish, combining savory beef and onions with a creamy sauce made with a roux, beef broth, and sour cream, for ladling over hot buttered egg noodles. It is one of those magical dishes that you can throw together in less than an hour, and tastes better than the sum of its parts. 15 of 25",
        "dateLastCrawled": "2023-03-13T21:49:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.8",
        "name": "Homemade Egg Noodles Recipe - The Spruce Eats",
        "url": "https://www.thespruceeats.com/homemade-egg-noodles-2215807",
        "isFamilyFriendly": true,
        "displayUrl": "https://www.thespruceeats.com/homemade-egg-noodles-2215807",
        "snippet": "Lay the noodles on a cooling or drying rack and let them sit until ready to cook. Repeat rolling and cutting with the remaining half of the dough. Boil the noodles in well-salted water until tender to the bite (2 to 10 minutes for fresh noodles depending on the thickness). Drain and use in your favorite recipe. Enjoy! Tips",
        "dateLastCrawled": "2023-03-15T06:11:00.0000000Z",
        "language": "en",
        "isNavigational": false
      },
      {
        "id": "https://api.bing.microsoft.com/api/v7/#WebPages.9",
        "name": "15 Popular Asian Noodle Recipes - Christie at Home",
        "url": "https://christieathome.com/blog/15-popular-asian-noodle-recipes/",
        "isFamilyFriendly": true,
        "displayUrl": "https://christieathome.com/blog/15-popular-asian-noodle-recipes",
        "snippet": "Savoury, flavourful flat wide rice noodles coated in a salty sweet soy sauce. Paired deliciously with pork belly, beansprouts, Chinese chives, garlic and egg. A perfect meal for dinner or lunch. This recipe only takes 30 minutes. These noodles are my ALL-time flavourite in the world.",
        "dateLastCrawled": "2023-03-15T10:38:00.0000000Z",
        "language": "en",
        "isNavigational": false
      }
    ]
  },
  "rankingResponse": {
    "mainline": {
      "items": [
        {
          "answerType": "WebPages",
          "resultIndex": 0,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.0"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 1,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.1"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 2,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.2"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 3,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.3"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 4,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.4"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 5,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.5"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 6,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.6"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 7,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.7"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 8,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.8"
          }
        },
        {
          "answerType": "WebPages",
          "resultIndex": 9,
          "value": {
            "id": "https://api.bing.microsoft.com/api/v7/#WebPages.9"
          }
        }
      ]
    }
  }
}
 */