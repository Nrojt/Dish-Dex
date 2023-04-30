package com.nrojt.dishdex.utils.viewmodel;


import androidx.annotation.Nullable;

public class FontUtils {

    private static float fontSizeTitle = 20;
    private static float fontSizeText = 14;


    private FontUtils(@Nullable Float fontSizeTitle, @Nullable Float fontSizeText) {
        if(fontSizeTitle != null) {
            FontUtils.fontSizeTitle = fontSizeTitle;
        }
        if(fontSizeText != null) {
            FontUtils.fontSizeText = fontSizeText;
        }
    }


    public static float getTitleFontSize() {
        // return the font size for titles
        return fontSizeTitle;
    }

    public static float getTextFontSize() {
        // return the font size for text
        return fontSizeText;
    }

    public static void setTitleFontSize(float fontSizeTitle) {
        // set the font size for titles
        FontUtils.fontSizeTitle = fontSizeTitle;
    }

    public static void setTextFontSize(float fontSizeText) {
        // set the font size for text
        FontUtils.fontSizeText = fontSizeText;
    }
}
