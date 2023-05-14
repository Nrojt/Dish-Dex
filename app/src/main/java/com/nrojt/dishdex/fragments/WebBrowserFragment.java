package com.nrojt.dishdex.fragments;

import static com.nrojt.dishdex.fragments.SettingsFragment.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.backend.Recipe;
import com.nrojt.dishdex.backend.viewmodels.WebBrowserFragmentViewModel;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.OnBackPressedListener;
import com.nrojt.dishdex.utils.internet.WebScraper;
import com.nrojt.dishdex.utils.viewmodel.FontUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO when opening a link from a saved recipe, allow that recipe to be updated/saved when pressing the saveThisRecipe button, instead of creating a new one
public class WebBrowserFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener, OnBackPressedListener {
    private WebView urlBrowser;
    private EditText currentBrowserUrl;

    private FragmentManager fragmentManager;

    private WebBrowserFragmentViewModel viewModel;


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String URL = "url";
    private static final String SHOW_SAVE_BUTTON = "showSaveButton";

    private Boolean showSaveButton;

    public WebBrowserFragment() {
        // Required empty public constructor
    }


    public static WebBrowserFragment newInstance(String url, boolean showSaveButton) {
        WebBrowserFragment fragment = new WebBrowserFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putBoolean(SHOW_SAVE_BUTTON, showSaveButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WebBrowserFragmentViewModel.class);

        if (getArguments() != null) {
            viewModel.setOpenUrl(getArguments().getString(URL));
            showSaveButton = getArguments().getBoolean(SHOW_SAVE_BUTTON);
        } else {
            if (viewModel.getOpenUrl() == null) {
                viewModel.setOpenUrl("https://www.google.com");
            }
            showSaveButton = true;
        }

        if(viewModel.getBlockedUrls().getValue().isEmpty()){
            viewModel.loadBlockedUrls(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getChildFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_browser, container, false);
        urlBrowser = view.findViewById(R.id.urlBrowser);
        currentBrowserUrl = view.findViewById(R.id.currentBrowserUrl);
        Button scrapeThisUrlButton = view.findViewById(R.id.scrapeThisUrlButton);

        currentBrowserUrl.setTextSize(FontUtils.getTitleFontSize());




        //Overriding the standard ChromeClient to update the currentBrowserUrl
        urlBrowser.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                currentBrowserUrl.setText(view.getUrl());
                super.onProgressChanged(view, newProgress);
            }
        });


        //Overriding the standard WebClient since I need different functionality
        urlBrowser.setWebViewClient(new WebViewClient() {
            //overriding shouldOverrideUrlLoading to update the currentBrowserUrl
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                currentBrowserUrl.setText(request.getUrl().toString());
                viewModel.setOpenUrl(request.getUrl().toString());
                return false;
            }

            //overriding shouldInterceptRequest to block certain urls
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // check if request is coming from an ad URL and return a fake resource if it is
                if (viewModel.isBlockedUrl(request.getUrl().toString())) {
                    Log.i("WebView", "blocked url: " + request.getUrl().toString());
                    return new WebResourceResponse("text/plain", "utf-8", null);
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }

            //overriding onReceivedError to log the error
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e("WebView", error.getDescription().toString());
            }
        });

        //enabling javascript, since without it many websites won't work
        urlBrowser.getSettings().setJavaScriptEnabled(true);
        urlBrowser.loadUrl(viewModel.getOpenUrl());


        //searching the web if the user presses the enter key in the edittext
        currentBrowserUrl.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                //checking to see if the user input is an actual url, if not it will perform a google search
                String userInput = currentBrowserUrl.getText().toString();
                if (URLUtil.isValidUrl(userInput)) {
                    urlBrowser.loadUrl(userInput); //updating the WebView
                } else {
                    String googleSearchQuery = "https://www.google.com/search?q=" + userInput;
                    urlBrowser.loadUrl(googleSearchQuery);
                }
                return true;
            }
            return false;
        });

        //hiding or showing the save this button
        if(showSaveButton){
            scrapeThisUrlButton.setVisibility(View.VISIBLE);
        } else {
            scrapeThisUrlButton.setVisibility(View.GONE);
        }

        //the button that will get the url and send it to the ScrapeFromUrlFragment
        scrapeThisUrlButton.setOnClickListener(view1 -> {
            String url = currentBrowserUrl.getText().toString();
            if (!url.isBlank()) {
                if (URLUtil.isValidUrl(url)) {
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                    String openaiApiKey = sharedPreferences.getString(SettingsFragment.OPENAI_API_KEY, "");
                    WebScraper wb = new WebScraper(url, openaiApiKey);
                    ExecutorService webScraperService = Executors.newSingleThreadExecutor();
                    Handler handler1 = new Handler(Looper.getMainLooper());
                    //running the WebScraper on a separate thread, so the ui thread doesn't lock
                    webScraperService.execute(() -> {
                        wb.scrapeWebsite();

                        handler1.post(() -> {
                            //checking if the website is supported
                            if (wb.isNotConnected()) { //checking if the user is connected to the internet. This cannot be done on main thread, cause this will throw an error
                                Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                            } else if (wb.isNotReachable()) {
                                Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                            } else {
                                if (wb.isNotSupported()) {
                                    Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_SHORT).show();
                                }
                                //switching to the showAndEditRecipeFragment
                                Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(0, new Recipe(), wb, url);
                                replaceFragment(showAndEditRecipeFragment);
                            }
                        });
                    });
                    webScraperService.shutdown();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "This site is blocked", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No url given", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    //overriding the back button
    @Override
    public boolean handleOnBackPressed() {
        if (urlBrowser != null && urlBrowser.canGoBack()) {
            urlBrowser.goBack();
            return true;
        }
        return false;
    }

    //replacing the fragment
    @Override
    public void replaceFragment(Fragment fragment) {
            ((MainActivity) getActivity()).replaceFragment(fragment, getClass());

    }

    @Override
    public void onBackStackChanged() {
            ((MainActivity) getActivity()).onBackStackChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }
}