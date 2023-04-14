package com.nrojt.dishdex.utils.internet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebScraperTest {

    @Test
    void isNotSupported() {
        String url = "https://www.tasteofhome.com/recipes/fontina-asparagus-tart/";
        WebScraper webScraper = new WebScraper(url);
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotSupported());
    }

    @Test
    void isNotReachable() {
        String url = "AAAA";
        WebScraper webScraper = new WebScraper(url);
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotReachable());
    }

    @Test
    void getUrl() {
        String url = "https://www.google.com";
        WebScraper webScraper = new WebScraper(url);
        assertEquals(url, webScraper.getUrl());
    }

    @Test
    void getServings() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        int expectedServings = 8;
        WebScraper webScraper = new WebScraper(url);
        webScraper.scrapeWebsite();
        assertEquals(expectedServings, webScraper.getServings());
    }

    @Test
    void getCookingTime() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        int expectedCookingTime = 75;
        WebScraper webScraper = new WebScraper(url);
        webScraper.scrapeWebsite();
        assertEquals(expectedCookingTime, webScraper.getCookingTime());
    }

    @Test
    void getRecipeTitle() {
        String url = "https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren";
        String expectedTitle = "Kleurig gevulde eieren";
        WebScraper webScraper = new WebScraper(url);
        webScraper.scrapeWebsite();
        assertEquals(expectedTitle, webScraper.getRecipeTitle());
    }
}