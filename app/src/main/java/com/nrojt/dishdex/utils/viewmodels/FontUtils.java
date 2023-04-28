package com.nrojt.dishdex.utils.viewmodels;

import android.content.Context;

import androidx.annotation.Nullable;

import com.nrojt.dishdex.backend.viewmodels.MainActivityViewModel;

public class FontUtils {
    private Context context;
    private static FontUtils instance;
    private MainActivityViewModel viewModel;

    private FontUtils(@Nullable Context context) {
        this.context = context;
    }

    //TODO see if I can make this work with observers
    public static float getTitleFontSize(MainActivityViewModel viewModel) {
        // return the font size from the view model
        return viewModel.getFontSizeTitle().getValue();
    }

    public static float getTextFontSize(MainActivityViewModel viewModel) {
        // return the font size from the view model
        return viewModel.getFontSizeText().getValue();
    }
}
