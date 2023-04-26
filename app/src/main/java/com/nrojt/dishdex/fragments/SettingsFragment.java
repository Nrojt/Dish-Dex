package com.nrojt.dishdex.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;


public class SettingsFragment extends Fragment {

    private TextInputLayout bingApiKeyTextInput;
    private TextInputLayout fontSizeTextInput;
    private TextInputLayout fontSizeTitleTextInput;

    private ToggleButton proUserToggleButton;

    private String bingApiKey;
    private boolean isProUser;
    private int fontSize;
    private int fontSizeTitles;

    public static final String SHARED_PREFS = "SharedPrefs";
    public static final String BING_API_KEY = "bingApiKey";
    public static final String IS_PRO_USER = "isProUser";
    public static final String FONT_SIZE = "fontSize";
    public static final String FONT_SIZE_TITLES = "fontSizeTitles";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //Getting the views
        bingApiKeyTextInput = view.findViewById(R.id.bingKeyTextInput);
        Button saveSettingsButton = view.findViewById(R.id.saveSettingsButton);
        proUserToggleButton = view.findViewById(R.id.proUserToggleButton);
        fontSizeTextInput = view.findViewById(R.id.fontSizeTextInput);
        fontSizeTitleTextInput = view.findViewById(R.id.fontSizeTitleTextInput);


        //Saving the settings to shared preferences (local storage) when the save button is clicked
        saveSettingsButton.setOnClickListener(v -> {
            isProUser = proUserToggleButton.isChecked();
            bingApiKey = bingApiKeyTextInput.getEditText().getText().toString();
            fontSize = Integer.parseInt(fontSizeTextInput.getEditText().getText().toString());
            fontSizeTitles = Integer.parseInt(fontSizeTitleTextInput.getEditText().getText().toString());

            if(fontSize > 32){
                Toast.makeText(getActivity().getApplicationContext(), "Font size too large", Toast.LENGTH_SHORT).show();
                fontSizeTextInput.getEditText().setText("32");
                return;
            }

            if(fontSize <= 0){
                Toast.makeText(getActivity().getApplicationContext(), "Font size too small", Toast.LENGTH_SHORT).show();
                fontSizeTextInput.getEditText().setText("32");
                return;
            }

            if(fontSizeTitles > 48){
                Toast.makeText(getActivity().getApplicationContext(), "Font size too large", Toast.LENGTH_SHORT).show();
                fontSizeTitleTextInput.getEditText().setText("48");
                return;
            }

            if(fontSizeTitles <= 0){
                Toast.makeText(getActivity().getApplicationContext(), "Font size too small", Toast.LENGTH_SHORT).show();
                fontSizeTitleTextInput.getEditText().setText("48");
                return;
            }

            if (!bingApiKey.isBlank()) {
                if (bingApiKey.length() != 32) {
                    Toast.makeText(getActivity().getApplicationContext(), "Invalid Bing Search API key", Toast.LENGTH_SHORT).show();
                }
            }

            //Saving the data
            saveData();
        });

        //Loading the settings from shared preferences (local storage) when the fragment is created
        loadData();

        return view;
    }

    //Saving the data to shared preferences
    public void saveData() {
        //Creating a shared preferences object
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Saving the data, the first parameter is the key(name) and the second is the value(api key)
        editor.putString(BING_API_KEY, bingApiKey);
        editor.putBoolean(IS_PRO_USER, isProUser);
        editor.putInt(FONT_SIZE, fontSize);
        editor.putInt(FONT_SIZE_TITLES, fontSizeTitles);
        editor.apply();

        //Setting the static variables in MainActivity to the new values so the user doesn't have to restart the app to apply a different font size
        MainActivity.fontSizeText = fontSize;
        MainActivity.fontSizeTitles = fontSizeTitles;
    }

    //Loading the data from shared preferences
    public void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        //If the key doesn't exist, the default value is an empty string
        String loadedApiKey = sharedPreferences.getString(BING_API_KEY, "");
        boolean loadedIsProUser = sharedPreferences.getBoolean(IS_PRO_USER, false);
        int loadedFontSize = sharedPreferences.getInt(FONT_SIZE, 14);
        int loadedFontSizeTitles = sharedPreferences.getInt(FONT_SIZE_TITLES, 20);

        //Setting the text in the text input to the api key that was loaded in
        bingApiKeyTextInput.getEditText().setText(loadedApiKey);
        proUserToggleButton.setChecked(loadedIsProUser);
        fontSizeTextInput.getEditText().setText(String.valueOf(loadedFontSize));
        fontSizeTitleTextInput.getEditText().setText(String.valueOf(loadedFontSizeTitles));

        //setting the text sizes
        bingApiKeyTextInput.getEditText().setTextSize(loadedFontSize);
        fontSizeTextInput.getEditText().setTextSize(loadedFontSize);
        fontSizeTitleTextInput.getEditText().setTextSize(loadedFontSize);
        fontSizeTitleTextInput.getEditText().setTextSize(loadedFontSize);
    }
}