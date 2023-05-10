package com.nrojt.dishdex.utils.hashmap;

import java.util.HashMap;

public class CategoryKeywordsHashmap {
    private static HashMap<String, Integer> categoryKeywords = new HashMap<>();


    private static void populateHashMap(){
        categoryKeywords.put("breakfast", 1);
        categoryKeywords.put("lunch", 2);
        categoryKeywords.put("dinner", 3);
        categoryKeywords.put("main", 3);
        categoryKeywords.put("diner", 3);
        categoryKeywords.put("avond eten", 3);
        categoryKeywords.put("avondeten", 3);
        categoryKeywords.put("avondmaaltijd", 3);
        categoryKeywords.put("avond maaltijd", 3);
        categoryKeywords.put("hoofdgerecht", 3);
        categoryKeywords.put("dessert", 4);
        categoryKeywords.put("toetje", 4);
        categoryKeywords.put("snack", 5);
        categoryKeywords.put("side dish", 6);
        //TODO add support for custom categories
    }

    public void addCategoryKeyword(String keyword, int categoryID){
        categoryKeywords.put(keyword, categoryID);
    }

    public static HashMap<String, Integer> getCategoryKeywords(){
        if(categoryKeywords.isEmpty()){
            populateHashMap();
        }
        return categoryKeywords;
    }

    public static int getCategoryID(String keyword){
        if(categoryKeywords.isEmpty()){
            populateHashMap();
        }
        return categoryKeywords.get(keyword);
    }
}
