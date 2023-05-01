package com.nrojt.dishdex.backend.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<Boolean> darkModeLiveData = new MutableLiveData<>();

    public MutableLiveData<Boolean> getDarkModeLiveData() {
        return darkModeLiveData;
    }

    public void setDarkModeLiveData(boolean darkMode) {
        darkModeLiveData.setValue(darkMode);
    }

    public MainActivityViewModel() {
    }
}
