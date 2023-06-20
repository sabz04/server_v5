package com.template;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = findViewById(R.id.webView);

        //get shared prefs

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PREFS", MODE_PRIVATE);

        String URL = sharedPreferences.getString("URL", "");

    }
}