package com.nrojt.dishdex.utils.internet;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebScraperTest {

    @Test
    void isNotSupported() {
        WebScraper webScraper = new WebScraper("https://www.tasteofhome.com/recipes/fontina-asparagus-tart/");
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotSupported());
    }

    @Test
    void isNotReachable() {
        WebScraper webScraper = new WebScraper("AAAAA");
        webScraper.scrapeWebsite();
        assertTrue(webScraper.isNotReachable());
    }

    @Test
    void getUrl() {
        WebScraper webScraper = new WebScraper("https://www.google.com");
        assertEquals("https://www.google.com", webScraper.getUrl());
    }

    @Test
    void getServings() {
        WebScraper webScraper = new WebScraper("https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren");
        webScraper.scrapeWebsite();
        assertEquals(8, webScraper.getServings());
    }

    @Test
    void getCookingTime() {
        WebScraper webScraper = new WebScraper("https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren");
        webScraper.scrapeWebsite();
        assertEquals(75, webScraper.getCookingTime());
    }

    @Test
    void getRecipeTitle() {
        WebScraper webScraper = new WebScraper("https://www.ah.nl/allerhande/recept/R-R1198109/kleurig-gevulde-eieren");
        webScraper.scrapeWebsite();
        assertEquals("Kleurig gevulde eieren", webScraper.getRecipeTitle());
    }
}