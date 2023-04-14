package com.nrojt.dishdex.fragments;

import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
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

import com.nrojt.dishdex.MainActivity;
import com.nrojt.dishdex.R;
import com.nrojt.dishdex.utils.interfaces.FragmentReplacer;
import com.nrojt.dishdex.utils.interfaces.OnBackPressedListener;
import com.nrojt.dishdex.utils.internet.LoadWebsiteBlockList;
import com.nrojt.dishdex.utils.internet.WebScraper;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebBrowserFragment extends Fragment implements FragmentReplacer, FragmentManager.OnBackStackChangedListener, OnBackPressedListener {
    private OnBackPressedListener onBackPressedListener;
    private ArrayList<String> blockedUrls = new ArrayList<>();
    private WebView urlBrowser;
    private EditText currentBrowserUrl;
    private Button scrapeThisUrlButton;

    private FragmentManager fragmentManager;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WebBrowserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment webBrowserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebBrowserFragment newInstance(String param1, String param2) {
        WebBrowserFragment fragment = new WebBrowserFragment();
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
        fragmentManager = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_browser, container, false);
        urlBrowser = view.findViewById(R.id.urlBrowser);
        currentBrowserUrl = view.findViewById(R.id.currentBrowserUrl);
        scrapeThisUrlButton = view.findViewById(R.id.scrapeThisUrlButton);

        LoadWebsiteBlockList loadWebsiteBlockList = new LoadWebsiteBlockList(getContext());
        //creating a new thread for getting the blocked urls
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        //running the LoadBlockList in another thread
        service.execute(() -> {
            loadWebsiteBlockList.loadBlockList();
            handler.post(() -> blockedUrls = loadWebsiteBlockList.getAdUrls());
        });
        service.shutdown();


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
                return false;
            }

            //overriding shouldInterceptRequest to block certain urls
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // check if request is coming from an ad URL and return a fake resource if it is
                if (isBlocked(request.getUrl().toString())) {
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

        urlBrowser.getSettings().setJavaScriptEnabled(true);
        urlBrowser.loadUrl("https://www.google.com");


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

        //the button that will get the url and send it to the ScrapeFromUrlFragment
        scrapeThisUrlButton.setOnClickListener(view1 -> {
            String url = currentBrowserUrl.getText().toString();
            if (!url.isBlank()) {
                if (URLUtil.isValidUrl(url)) {
                    WebScraper wb = new WebScraper(url);
                    ExecutorService service1 = Executors.newSingleThreadExecutor();
                    Handler handler1 = new Handler(Looper.getMainLooper());
                    //running the WebScraper on a separate thread, so the ui thread doesn't lock
                    service1.execute(() -> {
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
                                Fragment showAndEditRecipeFragment = ShowAndEditRecipeFragment.newInstance(0, -1, wb, url);
                                replaceFragment(showAndEditRecipeFragment);
                            }
                        });
                    });
                    service1.shutdown();
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
        System.out.println("back pressed");
        if (urlBrowser != null && urlBrowser.canGoBack()) {
            urlBrowser.goBack();
            return true;
        }
        return false;
    }


    //trying to see if it is possible to block ads
    private boolean isBlocked(String url) {
        for (int i = 0; i < blockedUrls.size(); i++) {
            if (url.contains(blockedUrls.get(i))) {
                return true;
            }
        }
        return false;
    }


    //replacing the fragment
    @Override
    public void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackStackChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            fragmentManager.removeOnBackStackChangedListener(this);
        }
    }


}