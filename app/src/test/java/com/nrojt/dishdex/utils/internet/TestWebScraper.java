package com.nrojt.dishdex.utils.internet;

import android.os.Parcel;

public class TestWebScraper extends WebScraper{
    public TestWebScraper(String url, String openaiApiKey) {
        super(url, openaiApiKey);
    }

    protected TestWebScraper(Parcel in) {
        super(in);
    }

    @Override
    public boolean checkIfNotConnected(){
        return !TestInternetConnection.isNetworkAvailable();
    }

}
