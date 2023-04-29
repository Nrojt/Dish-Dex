package com.nrojt.dishdex.utils.viewmodels;


import androidx.annotation.Nullable;
import com.nrojt.dishdex.backend.viewmodels.MainActivityViewModel;

public class FontUtils {

    private static FontUtils instance;
    private static MainActivityViewModel viewModel;


    private FontUtils(@Nullable MainActivityViewModel viewModel) {
        FontUtils.viewModel = viewModel;
    }

    public static FontUtils getInstance(@Nullable MainActivityViewModel viewModel) {
        if (instance == null) {
            instance = new FontUtils(viewModel);
        }
        return instance;
    }

    public static float getTitleFontSize() {
        if(viewModel == null) {
            return 20;
        }
        // return the font size from the view model
        return viewModel.getFontSizeTitle().getValue();
    }

    public static float getTextFontSize() {
        if(viewModel == null) {
            return 14;
        }
        // return the font size from the view model
        return viewModel.getFontSizeText().getValue();
    }
}
