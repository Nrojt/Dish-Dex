package com.nrojt.dishdex.utils.internet;

import android.content.Context;

import androidx.annotation.Nullable;

import com.nrojt.dishdex.utils.hashmap.CategoryKeywordsHashmap;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.reactivex.schedulers.Schedulers;

public class OpenAIGPT {
    private OpenAiService service;
    private static OpenAIGPT instance = null;

    private static String openaiApiKey = "";

    //TODO switch to context and remove oepnaiapikey string
    private Context context;

    private OpenAIGPT(@Nullable String openaiApiKey) {
        OpenAIGPT.openaiApiKey = openaiApiKey;
    }

    public static OpenAIGPT getInstance(String openaiapikey){
        if(instance == null){
            instance = new OpenAIGPT(openaiapikey);
        }
        return instance;
    }

    //Using GPT 3.5 to get the category of the recipe
    public int getRecipeCategoryFromGPT(String url){
        int recipeCategoryID = 0;
        try {
            OpenAiService SERVICE = new OpenAiService(openaiApiKey);

            List<String> keywords = new ArrayList<>(CategoryKeywordsHashmap.getCategoryKeywords().keySet());

            String userMessageString = "What recipe type of recipe is this? Give one of the following answers in 1 word without punctuation:" + "[" + String.join(", ", keywords) + "]" + url;
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
                return recipeCategoryID;
            }

            System.out.println("GPT response: " + gptResponseString);

            if(keywords.contains(gptResponseString)){
                recipeCategoryID = CategoryKeywordsHashmap.getCategoryID(gptResponseString);
            }

            SERVICE.shutdownExecutor(); // shutdown the executor to prevent memory leaks
        } catch (OpenAiHttpException e) {
            throw new RuntimeException(e);
        }
        return recipeCategoryID;
    }

}
