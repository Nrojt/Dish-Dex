package com.nrojt.dishdex;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private TextInputLayout bingApiKeyTextInput;
    private Button saveSettingsButton;

    private String bingApiKey;

    public static final String SHARED_PREFS = "SharedPrefs";
    public static final String BING_API_KEY = "bingApiKey";

    private String loadedApiKey;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton);

        //Saving the settings to shared preferences (local storage) when the save button is clicked
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bingApiKey = bingApiKeyTextInput.getEditText().getText().toString();
                System.out.println(bingApiKey);
                if(!bingApiKey.isBlank()){
                    if (bingApiKey.length() != 32) {
                        Toast.makeText(getActivity().getApplicationContext(), "Invalid Bing Search API key", Toast.LENGTH_SHORT).show();
                    }
                }
                //Saving the data
                saveData();
            }
        });

        //Loading the settings from shared preferences (local storage) when the fragment is created
        loadData();

        //Setting the text in the text input to the api key that was loaded in
        bingApiKeyTextInput.getEditText().setText(loadedApiKey);

        return view;
    }

    //Saving the data to shared preferences
    public void saveData(){
        //Creating a shared preferences object
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Saving the data, the first parameter is the key(name) and the second is the value(api key)
        editor.putString(BING_API_KEY, bingApiKey);
        editor.apply();
    }

    //Loading the data from shared preferences
    public void loadData(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        //If the key doesn't exist, the default value is an empty string
        loadedApiKey = sharedPreferences.getString(BING_API_KEY, "");

    }
}