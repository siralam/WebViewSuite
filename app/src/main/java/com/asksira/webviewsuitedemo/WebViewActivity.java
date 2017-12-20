package com.asksira.webviewsuitedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.asksira.webviewsuite.WebViewSuite;

public class WebViewActivity extends AppCompatActivity {

    WebViewSuite webViewSuite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webViewSuite = findViewById(R.id.webViewSuite);
    }
}
