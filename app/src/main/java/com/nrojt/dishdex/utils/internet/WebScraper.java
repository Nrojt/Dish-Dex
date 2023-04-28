package com.nrojt.dishdex.utils.internet;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Serializable allows the object to be passed between fragments
public class WebScraper implements Parcelable {
    private boolean notConnected = false;
    private boolean notSupported = false;
    private boolean notReachable = false;
    private final String url;
    private int servings = 0;
    private int cookingTime = 0;
    private String recipeTitle;
    private List<String> recipeTextList = new ArrayList<>();
    private List<String> ingredientTextList = new ArrayList<>();

    private final StringBuilder recipeText = new StringBuilder();
    private final StringBuilder ingredientText = new StringBuilder();

    //constructor
    public WebScraper(String url) {
        //if the url doesn't contain http or https, add https. This is to prevent the app from crashing.
        if (!(url.contains("http://") || url.contains("https://"))) {
            this.url = "https://" + url;
        } else {
            this.url = url;
        }
    }

    protected WebScraper(Parcel in) {
        notConnected = in.readByte() != 0;
        notSupported = in.readByte() != 0;
        notReachable = in.readByte() != 0;
        url = in.readString();
        servings = in.readInt();
        cookingTime = in.readInt();
        recipeTitle = in.readString();
        recipeTextList = in.createStringArrayList();
        ingredientTextList = in.createStringArrayList();
    }

    public static final Creator<WebScraper> CREATOR = new Creator<WebScraper>() {
        @Override
        public WebScraper createFromParcel(Parcel in) {
            return new WebScraper(in);
        }

        @Override
        public WebScraper[] newArray(int size) {
            return new WebScraper[size];
        }
    };

    public boolean isNotConnected() {
        return notConnected;
    }

    public boolean isNotSupported() {
        return notSupported;
    }

    public boolean isNotReachable() {
        return notReachable;
    }

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
                Elements instructionElements;
                Elements ingredientElements = null;
                Element servingsElement;
                Element cookingTimeElement;
                Element recipeTitleElement;

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
                } else if (url.contains("bellyfull.net")) {
                    instructionElements = document.getElementsByClass("wprm-recipe-instruction-text");
                    ingredientElements = document.getElementsByClass("wprm-recipe-ingredient");

                    servingsElement = document.getElementsByClass("wprm-recipe-servings-with-unit").first();
                    cookingTimeElement = document.getElementsByClass("wprm-recipe-details wprm-recipe-details-minutes wprm-recipe-total_time wprm-recipe-total_time-minutes").get(0);
                    recipeTitleElement = document.getElementsByClass("wprm-recipe-name wprm-block-text-bold").first();
                } else {
                    //General recipe scraper
                    //Selectors are used to find the elements on the page

                    // Selectors for recipe instructions and ingredients
                    String instructionSelector = ".recipe-instructions, .instructions, .recipe-steps";
                    String ingredientSelector = ".recipe-ingredients, .ingredients, .recipe-ings";

                    // Selectors for recipe title, servings, and cooking time
                    String titleSelector = "h1, h2, .recipe-title, .title";
                    String servingsSelector = ".servings, .yield, .recipe-servings";
                    String cookingTimeSelector = ".time, .duration, .cook-time, .minutes";

                    // Find the recipe instructions and ingredients on the page
                    instructionElements = document.select(instructionSelector);
                    ingredientElements = document.select(ingredientSelector);

                    // Find the recipe title, servings, and cooking time on the page
                    recipeTitleElement = document.selectFirst(titleSelector);
                    servingsElement = document.selectFirst(servingsSelector);
                    cookingTimeElement = document.selectFirst(cookingTimeSelector);

                    notSupported = true;
                }

                //Converting the elements to text and adding them to the lists
                for (int i = 0; i < instructionElements.size(); i++) {
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

                // setting the cooking time, but some websites report time in hours and minutes, so we need to convert that to minutes. But for now it just adds all the numbers it can find together.
                if (cookingTimeElement != null) {
                    String text = cookingTimeElement.text().toLowerCase();
                    Pattern pattern = Pattern.compile("(\\d+)\\s*(hour|uur|min)?"); // Create a regex pattern to match numbers followed by an optional unit (hour, uur, or min)
                    Matcher matcher = pattern.matcher(text); // Matcher to find the numbers and the words in the text

                    int totalTime;
                    int hours = 0;
                    int minutes = 0;

                    // Iterate through all matches found
                    while (matcher.find()) {
                        int value = Integer.parseInt(matcher.group(1));
                        // Get the unit (uur, hour, min) (if any) from the match
                        String unit = matcher.group(2);

                        if (unit == null || unit.isEmpty()) {
                            minutes = value;
                        } else {
                            if (unit.contains("hour") || unit.contains("uur")) {
                                hours = value;
                                text = text.replaceFirst("\\d+", ""); // Remove the parsed number from the text
                                matcher = pattern.matcher(text); // Update the matcher with the new text
                            }
                            if (unit.contains("min")) {
                                minutes = value;
                            }
                        }
                    }

                    totalTime = hours * 60 + minutes;
                    cookingTime = totalTime;
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
    public static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url);
        Document document = null;
        //Setting the user agent as chrome since it is the most used browser so sites are more likely to support it
        conn.userAgent("Chrome");
        conn.followRedirects(true);
        try {
            document = conn.get();
        } catch (IOException ignored) {
        }
        return document;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte((byte) (notConnected ? 1 : 0));
        dest.writeByte((byte) (notSupported ? 1 : 0));
        dest.writeByte((byte) (notReachable ? 1 : 0));
        dest.writeString(url);
        dest.writeInt(servings);
        dest.writeInt(cookingTime);
        dest.writeString(recipeTitle);
        dest.writeStringList(recipeTextList);
        dest.writeStringList(ingredientTextList);
    }
}