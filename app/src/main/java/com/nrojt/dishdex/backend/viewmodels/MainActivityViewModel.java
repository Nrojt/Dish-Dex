package com.nrojt.dishdex.backend.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<Integer> mFontSizeText;
    private MutableLiveData<Integer> mFontSizeTitle;

    public LiveData<Integer> getFontSizeText() {
        if (mFontSizeText == null) {
            mFontSizeText = new MutableLiveData<>();
            mFontSizeText.setValue(14); // default value
        }
        return mFontSizeText;
    }

    public LiveData<Integer> getFontSizeTitle() {
        if (mFontSizeTitle == null) {
            mFontSizeTitle = new MutableLiveData<>();
            mFontSizeTitle.setValue(20); // default value
        }
        return mFontSizeTitle;
    }

    public void setFontSizeText(int size) {
        if (mFontSizeText != null) {
            mFontSizeText.setValue(size);
        } else {
            mFontSizeText = new MutableLiveData<>();
            mFontSizeText.setValue(size);
        }
    }

    public void setFontSizeTitle(int size) {
        if (mFontSizeTitle != null) {
            mFontSizeTitle.setValue(size);
        } else {
            mFontSizeTitle = new MutableLiveData<>();
            mFontSizeTitle.setValue(size);
        }
    }
}
