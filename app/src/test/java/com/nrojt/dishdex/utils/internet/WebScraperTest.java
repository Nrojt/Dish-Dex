package com.nrojt.dishdex.utils.internet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebScraperTest {

    // https://www.simplyrecipes.com/recipes/chicken_cordon_bleu/
    // https://bellyfull.net/chicken-cordon-bleu/
    // https://www.allrecipes.com/recipe/8495/chicken-cordon-bleu-i/

    // https://www.allrecipes.com/recipe/10264/oatmeal-raisin-cookies-i/
    // https://bellyfull.net/oatmeal-raisin-cookies/
    // https://www.simplyrecipes.com/recipes/oatmeal_raisin_cookies/

    @Test
    void isNotSupported() {
        String url = "https://www.tasteofhome.com/recipes/fontina-asparagus-tart/";
        String openaiApiKey = "";

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotSupported());
    }

    @Test
    void isNotReachable() {
        String url = "AAAA";
        String openaiApiKey = "";

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotReachable());
    }

    @Test
    void getUrl() {
        String url = "https://www.google.com";
        String openaiApiKey = "";

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        assertEquals(url, webScraper.getUrl());
    }

    @Test
    void getServings() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        String openaiApiKey = "";
        int expectedServings = 8;

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();
        assertEquals(expectedServings, webScraper.getServings());
    }

    @Test
    void getCookingTime() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        String openaiApiKey = "";
        int expectedCookingTime = 75;

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();
        assertEquals(expectedCookingTime, webScraper.getCookingTime());
    }

    @Test
    void getRecipeTitle() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        String openaiApiKey = "";
        String expectedTitle = "Kleurig gevulde eieren";

        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();
        assertEquals(expectedTitle, webScraper.getRecipeTitle());
    }
}