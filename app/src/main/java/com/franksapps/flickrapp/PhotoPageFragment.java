package com.franksapps.flickrapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by frankbrosnan on 16/07/2016.
 */
public class PhotoPageFragment extends VisibleFragment {

    private static final String TAG="PhotoPageFragment";

    private String mUrl;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mUrl = getActivity().getIntent().getData().toString();

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_page,container,false);
        mWebView = (WebView)view.findViewById(R.id.webView);
        mProgressBar = (ProgressBar)view.findViewById(R.id.fragment_photo_page_progress_bar);
        mProgressBar.setMax(100);


        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient( new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView webView, int newProgress){
                if (newProgress == 100){
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView webView, String title){
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }

        });


        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view,String url){
                return false;
            }
        }
        );
        Log.i(TAG,"mUrl= "+ mUrl);

        mWebView.loadUrl(mUrl);

        return view;
    }
}
