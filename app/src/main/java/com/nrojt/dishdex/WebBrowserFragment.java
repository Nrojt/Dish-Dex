package com.nrojt.dishdex;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebBrowserFragment extends Fragment {
    private WebView urlBrowser;
    private EditText currentBrowserUrl;
    private Button scrapeThisUrlButton;

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
        View view = inflater.inflate(R.layout.fragment_web_browser, container, false);
        urlBrowser = view.findViewById(R.id.urlBrowser);
        currentBrowserUrl = view.findViewById(R.id.currentBrowserUrl);
        scrapeThisUrlButton = view.findViewById(R.id.scrapeThisUrlButton);

        //Overriding the standard WebClient to update the currentBrowserUrl
        urlBrowser.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                currentBrowserUrl.setText(view.getUrl());
                super.onProgressChanged(view, newProgress);
            }
        });

        //overriding the  shouldOverrideUrlLoading so the websites open in the webview in stead of in an external browser
        urlBrowser.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                currentBrowserUrl.setText(request.getUrl().toString());
                return false;
            }
        });

        urlBrowser.getSettings().setJavaScriptEnabled(true);
        urlBrowser.loadUrl("https://www.google.com");



        //making it so the user can go to the previous website in the webview
        urlBrowser.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    if(keycode == KeyEvent.KEYCODE_BACK){
                        if(urlBrowser != null){
                            if (urlBrowser.canGoBack()){
                                urlBrowser.goBack();
                            } else {
                                getActivity().onBackPressed();
                            }
                        }
                    }
                }
                return true;
            }
        });

        //searching the web if the user presses the enter key in the edittext
        currentBrowserUrl.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    //checking to see if the userinput is an actual url, if not it will perform a google search
                    String userInput = currentBrowserUrl.getText().toString();
                    if (URLUtil.isValidUrl(userInput)) {
                        urlBrowser.loadUrl(userInput); //updating the webview
                    } else {
                        String googleSearchQuery = "https://www.google.com/search?q=" + userInput;
                        urlBrowser.loadUrl(googleSearchQuery);
                    }
                    return true;
                }
                return false;
            }
        });

        //the that will get the url and send it to the ScrapeFromUrlFragment
        scrapeThisUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = currentBrowserUrl.getText().toString();
                if (!url.isBlank()) {
                    WebScraper wb = new WebScraper(url);
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    service.execute(new Runnable() { //running the webscraper on a seperate thread, so the ui thread doesnt lock
                        @Override
                        public void run() {
                            wb.scrapeWebsite();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //checking if the website is supported
                                    if (wb.isNotSupported()) {
                                        Toast.makeText(getActivity().getApplicationContext(), "This site is unsupported", Toast.LENGTH_SHORT).show();
                                    } else if (wb.isNotConnected()) { //checking if the user is connected to the internet. This cannot be done on main thread, cause this will throw an error
                                        Toast.makeText(getActivity().getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                                    } else if(wb.isNotReachable()){
                                        Toast.makeText(getActivity().getApplicationContext(), "Cannot reach this site", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Fragment scrapeUrlfragment = new ScrapeFromUrlFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("WebScraper", wb);
                                        bundle.putString("Url", url);
                                        scrapeUrlfragment.setArguments(bundle);
                                        replaceFragment(scrapeUrlfragment);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No url given", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void hideAllViewsBeforeSwitchingFragments(){
        // Hiding all the other views that don't get replaced
        urlBrowser.setVisibility(View.GONE);
        currentBrowserUrl.setVisibility(View.GONE);
        scrapeThisUrlButton.setVisibility(View.GONE);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.webviewFragmentLinearLayout, fragment);
        hideAllViewsBeforeSwitchingFragments();
        fragmentTransaction.commit();
    }

}