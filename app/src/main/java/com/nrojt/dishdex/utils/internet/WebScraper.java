package com.nrojt.dishdex.utils.internet;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.schedulers.Schedulers;

//Serializable allows the object to be passed between fragments
public class WebScraper implements Parcelable {
    private boolean notConnected = false;
    private boolean notSupported = false;
    private boolean notReachable = false;
    private final String url;
    private final String openaiApiKey;
    private int servings = 0;
    private int cookingTime = 0;
    private String recipeTitle;
    private List<String> recipeTextList = new ArrayList<>();
    private List<String> ingredientTextList = new ArrayList<>();
    private int recipeCategoryID;

    private final StringBuilder recipeText = new StringBuilder();
    private final StringBuilder ingredientText = new StringBuilder();

    //constructor
    public WebScraper(String url, String openaiApiKey) {
        //if the url doesn't contain http or https, add https. This is to prevent the app from crashing.
        if (!(url.contains("http://") || url.contains("https://"))) {
            this.url = "https://" + url;
        } else {
            this.url = url;
        }
        this.openaiApiKey = openaiApiKey;
    }

    //Random stuff needed for parcelables
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
        openaiApiKey = in.readString();
    }

    //Random stuff needed for parcelables
    public static final Creator<WebScraper> CREATOR = new Creator<>() {
        @Override
        public WebScraper createFromParcel(Parcel in) {
            return new WebScraper(in);
        }

        @Override
        public WebScraper[] newArray(int size) {
            return new WebScraper[size];
        }
    };

    // getters
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

    public int getRecipeCategoryID() {
        return recipeCategoryID;
    }

    //method to check if the device is connected to the internet
    public boolean checkIfNotConnected() {
        boolean connected = InternetConnection.isNetworkAvailable();
        return !connected;
    }

    //method to scrape the website
    public void scrapeWebsite() {
        //check if the device is connected to the internet
        notConnected = checkIfNotConnected();
        if (!notConnected) {
            Document document = getDocument(url);
            if (document == null) {
                notReachable = true;
            } else {
                Elements instructionElements;
                Elements ingredientElements = null;
                Element servingsElement;
                Element cookingTimeElement;
                Element recipeTitleElement;
                Elements categoryElements = null;

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
                    categoryElements = document.getElementsByClass("breadcrumbs");
                } else if (url.contains("ohmyfoodness.nl")){
                    instructionElements = document.getElementsByClass("wprm-recipe-instruction-text");
                    ingredientElements = document.getElementsByClass("wprm-recipe-ingredient");

                    servingsElement = document.getElementsByClass("wprm-recipe-servings-with-unit").first();
                    cookingTimeElement = null;
                    recipeTitleElement = document.getElementsByClass("wprm-recipe-name wprm-block-text-bold").first();
                    categoryElements = document.getElementsByClass("cat-links");

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

                // guessing the category of the recipe based on the url
                // Define keywords that indicate the type of recipe
                //TODO maybe put the hashmap in a separate class or in its own method.
                //TODO add support for custom categories

                HashMap<String, Integer> categoryKeywords = new HashMap<>();
                categoryKeywords.put("breakfast", 1);
                categoryKeywords.put("lunch", 2);
                categoryKeywords.put("dinner", 3);
                categoryKeywords.put("main", 3);
                categoryKeywords.put("diner", 3);
                categoryKeywords.put("avond eten", 3);
                categoryKeywords.put("dessert", 4);
                categoryKeywords.put("toetje", 4);
                categoryKeywords.put("snack", 5);
                categoryKeywords.put("side dish", 6);


                String categoryText = "";
                if (categoryElements != null) {
                    categoryText = categoryElements.text();
                }

                // Loop through each keyword
                for (String keyword : categoryKeywords.keySet()){
                    // Check if the keyword is present in the URL
                    if (url.toLowerCase().contains(keyword.toLowerCase()) || categoryText.toLowerCase().contains(keyword.toLowerCase())) {
                        recipeCategoryID = categoryKeywords.get(keyword);
                        break;
                    }
                }

                //If the recipe category is still 0, use GPT to guess the category
                if (recipeCategoryID == 0 && !notSupported && openaiApiKey != null && !openaiApiKey.isBlank()) {
                    getRecipeCategoryFromGPT(categoryKeywords);
                }

                System.out.println("Recipe CategoryID: " + recipeCategoryID);
            }
        }
    }

    //Using GPT 3.5 to get the category of the recipe
    private void getRecipeCategoryFromGPT(HashMap<String, Integer> categoryKeywords){
        try {
            OpenAiService SERVICE = new OpenAiService(openaiApiKey);

            String[] keywords = categoryKeywords.keySet().toArray(new String[0]);

            String userMessageString = "What recipe type of recipe is this? Give one of the following answers in 1 word without punctuation: " + Arrays.toString(keywords) + url;
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessageString);
            messages.add(userMessage);

            StringBuilder responseBuilder = new StringBuilder();

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .n(1) // number of choices to return
                    .logitBias(new HashMap<>()) // bias towards certain tokens, idk how to implement this yet
                    .build();


            // Because we cannot run network requests on the main thread, we need to use a Scheduler and a completable future to wait for the response
            CompletableFuture<String> future = new CompletableFuture<>();

            SERVICE.streamChatCompletion(chatCompletionRequest) // get a stream of chat completions, the gpt api returns tokens one by one.
                    .observeOn(Schedulers.io()) // run on a background thread
                    .doOnError(e -> {
                        //TODO find out why this still crashes the app when an incorrect api key is used
                        if (e instanceof OpenAiHttpException) {
                            // Handle OpenAiHttpExceptions here
                            System.out.println("OpenAI HTTP Error: " + e.getMessage());
                            // Provide a user-friendly error message to the user
                            future.completeExceptionally(new RuntimeException("Unable to complete request. Please check your API key and try again."));
                        } else {
                            System.out.println("Error: " + e.getMessage());
                            future.completeExceptionally(e);
                        }
                    }) // print errors
                    .doOnComplete(() -> {
                        String gptResponse = responseBuilder.toString();
                        future.complete(gptResponse);
                    })
                    .subscribe(chatCompletionResponse -> {
                        String response = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
                        if (response != null) { //The beginning and the end of the response are null
                            responseBuilder.append(response);
                        }
                    });

            String gptResponseString;
            try {
                gptResponseString = future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (gptResponseString == null || gptResponseString.isEmpty()) {
                System.out.println("GPT response was null");
                return;
            }

            System.out.println("GPT response: " + gptResponseString);

            // Loop through each keyword
            for (String keyword : categoryKeywords.keySet()) {
                // Check if the keyword is present in the URL
                if (keyword.equalsIgnoreCase(gptResponseString)) {
                    recipeCategoryID = categoryKeywords.get(keyword);
                    break;
                }
            }

            SERVICE.shutdownExecutor(); // shutdown the executor to prevent memory leaks
        } catch (OpenAiHttpException e) {
            throw new RuntimeException(e);
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