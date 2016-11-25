package com.mobileproject.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OptionsMenuActivity extends AppCompatActivity {

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);
        setWebContent();
    }

    public void process(View view){
        this.finish();
        startActivity(new Intent(this,MainMenuActivity.class));
    }

    private void setWebContent(){
        webview = (WebView) findViewById(R.id.webViewInformation);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webview.loadUrl("file:///android_res/raw/information.html");
    }

}
