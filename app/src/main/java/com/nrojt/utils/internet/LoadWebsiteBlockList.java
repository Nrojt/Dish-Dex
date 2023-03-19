package com.nrojt.utils.internet;

import android.content.Context;

import com.nrojt.dishdex.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoadWebsiteBlockList {
    private ArrayList<String> adUrls = new ArrayList<>();
    private Context context;

    public LoadWebsiteBlockList(Context context){
        this.context = context;
    }

    public ArrayList<String> getAdUrls() {
        return adUrls;
    }

    //Loading in a list of ads and other unwanted websites from a local text file
    //downloaded from https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts
    public void loadBlockList(){
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.adblocklist);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                // Skip comments and lines that don't contain URLs
                if (!inputLine.startsWith("#") && inputLine.contains("0.0.0.0")) {
                    String[] parts = inputLine.split("\\s+");
                    // Add the URL to the list
                    adUrls.add(parts[1]);
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Failed to read the list file: " + e.getMessage());
        }
    }

}
