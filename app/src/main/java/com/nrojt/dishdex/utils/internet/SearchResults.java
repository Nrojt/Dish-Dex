package com.nrojt.dishdex.utils.internet;

import java.util.HashMap;

//SearchResults was provided on the Microsoft bing api v7 website
public class SearchResults {
    public HashMap<String, String> relevantHeaders;
    public String jsonResponse;

    public SearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}
