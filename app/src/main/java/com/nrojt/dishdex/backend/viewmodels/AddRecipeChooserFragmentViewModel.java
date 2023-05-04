package com.nrojt.dishdex.backend.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddRecipeChooserFragmentViewModel extends ViewModel {
    private MutableLiveData<String> urlInput;
    private MutableLiveData<String> bingSearchInput;

    public AddRecipeChooserFragmentViewModel() {
        urlInput = new MutableLiveData<>();
        bingSearchInput = new MutableLiveData<>();
    }

    public MutableLiveData<String> getUrlInput() {
        if(urlInput.getValue() == null) {
            urlInput.setValue("");
        }
        return urlInput;
    }

    public MutableLiveData<String> getBingSearchInput() {
        if(bingSearchInput.getValue() == null) {
            bingSearchInput.setValue("");
        }
        return bingSearchInput;
    }

    public void setUrlInput(String urlInput) {
        this.urlInput.setValue(urlInput);
    }

    public void setBingSearchInput(String bingSearchInput) {
        this.bingSearchInput.setValue(bingSearchInput);
    }

    public void clear(){
        urlInput.setValue("");
        bingSearchInput.setValue("");
    }
}
