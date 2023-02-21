package com.example.recipe_app__scraper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scrapeFromUrlButton = findViewById(R.id.scrapeFromUrlButton);
        Button bingButton = findViewById(R.id.bingButton);

        scrapeFromUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScrapeFromUrlActivity.class);
                startActivity(intent);
            }
        });

        bingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BingActivity.class);
                startActivity(intent);
            }
        });
    }

}