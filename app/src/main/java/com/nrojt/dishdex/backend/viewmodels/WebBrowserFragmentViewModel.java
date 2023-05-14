package com.nrojt.dishdex.backend.viewmodels;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nrojt.dishdex.utils.internet.LoadWebsiteBlockList;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebBrowserFragmentViewModel extends ViewModel {
    private MutableLiveData<String> openUrl;
    private MutableLiveData<ArrayList<String>> blockedUrls;

    public WebBrowserFragmentViewModel() {
        openUrl = new MutableLiveData<>();
        blockedUrls = new MutableLiveData<>();
        if(blockedUrls.getValue() == null) {
            blockedUrls.setValue(new ArrayList<>());
        }
    }

    public void setOpenUrl(String url) {
        openUrl.setValue(url);
    }

    public String getOpenUrl() {
        return openUrl.getValue();
    }

    public MutableLiveData<ArrayList<String>> getBlockedUrls() {
        return blockedUrls;
    }

    public void loadBlockedUrls(Context context) {
        LoadWebsiteBlockList loadWebsiteBlockList = new LoadWebsiteBlockList(context);
        //creating a new thread for getting the blocked urls
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        //running the LoadBlockList in another thread
        service.execute(() -> {
            loadWebsiteBlockList.loadBlockList();
            handler.post(() -> blockedUrls.setValue(loadWebsiteBlockList.getAdUrls()));
        });
        service.shutdown();
    }

    public boolean isBlockedUrl(String url) {
        for (String blockedUrl : blockedUrls.getValue()) {
            if (url.contains(blockedUrl)) {
                return true;
            }
        }
        return false;
    }
}
