package com.nrojt.utils.internet;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Serializable allows the object to be passed between fragments
public class WebScraper implements Serializable {
    private boolean notConnected = false;
    private boolean notSupported = false;
    private boolean notReachable = false;
    private String url;
    private int servings = 0;
    private int cookingTime = 0;
    private String recipeTitle = "Unknown";
    private List<String> recipeTextList = new ArrayList<>();
    private List<String> ingredientTextList = new ArrayList<>();

    private StringBuilder recipeText = new StringBuilder();
    private StringBuilder ingredientText = new StringBuilder();

    //constructor
    public WebScraper(String url) {
        //if the url doesn't contain http or https, add https. This is to prevent the app from crashing.
        if(!(url.contains("http://") || url.contains("https://"))){
            this.url = "https://"+url;
        } else{
            this.url = url;
        }
    }

    public boolean isNotConnected() {
        return notConnected;
    }

    public boolean isNotSupported() {
        return notSupported;
    }
    public boolean isNotReachable(){return notReachable;}

    public String getUrl() {
        return url;
    }

    public int getServings() {
        return servings;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public StringBuilder getRecipeText() {
        return recipeText;
    }

    public StringBuilder getIngredientText() {
        return ingredientText;
    }

    //method to scrape the website
    public void scrapeWebsite() {
        //Checking if the user is connected to the internet
        if (InternetConnection.isNetworkAvailable()) {
            Document document = getDocument(url);
            if (document == null) {
                notReachable = true;
            } else {
                Elements instructionElements = null;
                Elements ingredientElements = null;
                Element servingsElement = null;
                Element cookingTimeElement = null;
                Element recipeTitleElement = null;

                //checking the url to see what classes need to be scraped, don't think this can be done in a switch
                if (url.contains("ah.nl/allerhande/recept")) {
                    instructionElements = document.getElementsByClass("recipe-steps_step__FYhB8");
                    Elements ahIngredientAmount = document.getElementsByClass("typography_root__Om3Wh typography_variant-paragraph__T5ZAU typography_weight-strong__uEXiN typography_hasMargin__4EaQi ingredient_unit__-ptEq");
                    Elements ahIngredientNames = document.getElementsByClass("typography_root__Om3Wh typography_variant-paragraph__T5ZAU typography_hasMargin__4EaQi ingredient_name__WXu5R");

                    for (int i = 0; i < ahIngredientNames.size(); i++) {
                        ingredientTextList.add(ahIngredientAmount.eachText().get(i) + " " + ahIngredientNames.eachText().get(i));
                        if (i != ahIngredientNames.size() - 1) {
                            ingredientTextList.add("\n");
                        }
                    }

                    servingsElement = document.getElementsByClass("recipe-ingredients_servings__f8HXF").first();
                    cookingTimeElement = document.getElementsByClass("recipe-header-time_timeLine__nn84w").first();
                    recipeTitleElement = document.getElementsByClass("typography_root__Om3Wh typography_variant-superhero__239x3 typography_hasMargin__4EaQi recipe-header_title__tG0JE").first();
                } else if (url.contains("allrecipes.com/recipe")) {
                    instructionElements = document.getElementsByClass("comp mntl-sc-block-group--LI mntl-sc-block mntl-sc-block-startgroup");
                    ingredientElements = document.getElementsByClass("mntl-structured-ingredients__list-item ");

                    servingsElement = document.getElementsByClass("mntl-recipe-details__value").last();
                    cookingTimeElement = document.getElementsByClass("mntl-recipe-details__value").first();
                    recipeTitleElement = document.getElementById("article-heading_1-0");
                } else if (url.contains("food.com/recipe")) {
                    instructionElements = document.getElementsByClass("direction svelte-ovaflp");
                    Elements foodComIngredientNames = document.getElementsByClass("ingredient-text svelte-ovaflp");
                    Elements foodComIngredientAmount = document.getElementsByClass("ingredient-quantity svelte-ovaflp");


                    for (int i = 0; i < foodComIngredientAmount.eachText().size(); i++) {
                        ingredientTextList.add(foodComIngredientAmount.eachText().get(i) + " " + foodComIngredientNames.eachText().get(i));
                        if (i != foodComIngredientAmount.eachText().size() - 1) {
                            ingredientTextList.add("\n");
                        }
                    }

                    for (int i = foodComIngredientAmount.eachText().size(); i < foodComIngredientNames.size(); i++) {
                        ingredientTextList.add(foodComIngredientNames.eachText().get(i));
                        if (i != foodComIngredientNames.size() - 1) {
                            ingredientTextList.add("\n");
                        }
                    }


                    servingsElement = document.getElementsByClass("adjust svelte-1o10zxc").first();
                    cookingTimeElement = document.getElementsByClass("facts__item svelte-ovaflp").first();
                    recipeTitleElement = document.getElementsByClass("layout__item title svelte-ovaflp").first();
                } else if (url.contains("bellyfull.net")){
                    instructionElements = document.getElementsByClass("wprm-recipe-instruction-text");
                    ingredientElements = document.getElementsByClass("wprm-recipe-ingredient");

                    servingsElement = document.getElementsByClass("wprm-recipe-servings-with-unit").first();
                    cookingTimeElement = document.getElementsByClass("wprm-recipe-details wprm-recipe-details-minutes wprm-recipe-total_time wprm-recipe-total_time-minutes").get(0);
                    recipeTitleElement = document.getElementsByClass("wprm-recipe-name wprm-block-text-bold").first();
                } else {
                    notSupported = true;
                }

                //Converting the elements to text and adding them to the lists
                for (int i = 0; i < (instructionElements != null ? instructionElements.size() : 0); i++) {
                    recipeTextList.add(instructionElements.eachText().get(i));
                    if (i != instructionElements.size() - 1) {
                        recipeTextList.add("\n\n");
                    }
                }
                for (int i = 0; i < (ingredientElements != null ? ingredientElements.size() : 0); i++) {
                    ingredientTextList.add(ingredientElements.eachText().get(i));
                    if (i != ingredientElements.size() - 1) {
                        ingredientTextList.add("\n");
                    }
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

                for (int i = 0; i < recipeTextList.size(); i++) {
                    recipeText.append(recipeTextList.get(i));
                }
                for (int i = 0; i < ingredientTextList.size(); i++) {
                    ingredientText.append(ingredientTextList.get(i));
                }
            }
        } else {
            notConnected = true;
        }
    }

    //Method to get a Document from the website, this is part of Jsoup
    private static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url);
        Document document = null;
        //Setting the user agent as chrome since it is the most used browser so sites are more likely to support it
        conn.userAgent("Chrome");
        conn.followRedirects(true);
        try {
            document = conn.get();
        } catch (IOException ignored) {}
        return document;
    }
}