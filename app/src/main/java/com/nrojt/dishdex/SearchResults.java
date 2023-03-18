package com.nrojt.dishdex;

import java.util.HashMap;

//SearchResults was provided on the Microsoft bing api v7 website
public class SearchResults{
    HashMap<String, String> relevantHeaders;
    String jsonResponse;
    SearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}
