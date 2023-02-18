package com.example.recipe_app__scraper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView mainActivityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityText = findViewById(R.id.mainActivityText);
        mainActivityText.setText("hello");
        Webscraper wb = new Webscraper("https://www.ah.nl/allerhande/recept/R-R1197911/erwtensoep-met-geitenkaas-en-komijn");
        wb.execute();
    }

    private class Webscraper extends AsyncTask<Void, Void, Void> {
        String url;
        String recipeText;

        public Webscraper(String url) {
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println(url);
            Document document = getDocument(url);

            Elements recipeElements = document.getElementsByClass("recipe-steps_step__FYhB8");

            recipeText = recipeElements.text();

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            mainActivityText.setText(recipeText);
        }

    }
    public static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url);
        Document document = null;
        conn.userAgent("Chrome");
        conn.followRedirects(true);
        try {
            document = conn.get();
        } catch (IOException e) {
            e.printStackTrace();
            // handle error
        }
        return document;
    }

}