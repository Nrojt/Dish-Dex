package com.nrojt.dishdex.utils.internet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class WebScraperTest {

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


    /*
    @ParameterizedTest
    @CsvSource({
            "https://bellyfull.net/chicken-cordon-bleu/, Chicken Cordon Bleu, true, 3",
            "https://www.allrecipes.com/recipe/8495/chicken-cordon-bleu-i/, Chicken Cordon Bleu, true, 3",
            "https://www.simplyrecipes.com/recipes/chicken_cordon_bleu/, Chicken Cordon Bleu, false, 3",
            "https://bellyfull.net/oatmeal-raisin-cookies/, Oatmeal Raisin Cookies, true, 4",
            "https://www.allrecipes.com/recipe/10264/oatmeal-raisin-cookies-i/, Oatmeal Raisin Cookies, true, 4",
            "https://www.simplyrecipes.com/recipes/oatmeal_raisin_cookies/, Oatmeal Raisin Cookies, false, 4"
    })
    void testPairwise(String url, String expectedTitle, boolean expectedSupported, int expectedCategoryID) {

        String openaiApiKey = "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"; //This needs to be filled in with a valid API key


        WebScraper webScraper = new WebScraper(url, openaiApiKey);
        webScraper.scrapeWebsite();

        assertEquals(expectedTitle, webScraper.getRecipeTitle());
        assertEquals(expectedSupported, !webScraper.isNotSupported()); // !isNotSupported() == isSupported()
        assertEquals(expectedCategoryID, webScraper.getRecipeCategoryID()); // 0 means no category

        if(expectedSupported){
            assertFalse(webScraper.getIngredientText().toString().isEmpty());
            assertFalse(webScraper.getRecipeText().toString().isEmpty());
        }
        else{
            assertTrue(webScraper.getIngredientText().toString().isEmpty());
            assertTrue(webScraper.getRecipeText().toString().isEmpty());
        }
    }

     */


    private final String supportedWebsite = "https://bellyfull.net/chicken-cordon-bleu/";
    private final String supportedWebsiteUnreachable = "https://bellyfull.net/chicken-aqwerdrsdfgsdefrzg/";
    private final String unsupportedWebsite = "https://www.simplyrecipes.com/recipes/chicken_cordon_bleu/";

    @Test
    public void testMCDCNoInternet(){

        //Using a TestWebScraper since there is no way to turn off internet access in android.
        //TestWebScraper is a copy of WebScraper that uses TestInternetConnection instead of InternetConnection
        //TestInternetConnection has the same code as InternetConnection, but it is trying to connect to a non-existing host. This will always fail, but is an accurate representation of what happens when there is no internet connection.
        WebScraper wb = new TestWebScraper(supportedWebsite, "");

        wb.scrapeWebsite();

        assertTrue(wb.isNotConnected());
        assertFalse(wb.isNotReachable());
        assertFalse(wb.isNotSupported());
    }

    @Test
    public void testMCDCSupportedWebsiteWithInternet() {
        WebScraper wb = new WebScraper(supportedWebsite, "");
        wb.scrapeWebsite();

        assertFalse(wb.isNotSupported());
        assertFalse(wb.isNotReachable());
        assertFalse(wb.isNotConnected());

        wb = new WebScraper(supportedWebsiteUnreachable, "");
        wb.scrapeWebsite();

        assertFalse(wb.isNotSupported());
        assertTrue(wb.isNotReachable());
        assertFalse(wb.isNotConnected());
    }

    @Test
    public void testMCDCUnsupportedWebsite() {
        WebScraper wb = new WebScraper(unsupportedWebsite, "");
        wb.scrapeWebsite();

        assertTrue(wb.isNotSupported());
        assertFalse(wb.isNotReachable());
        assertFalse(wb.isNotConnected());
    }
}