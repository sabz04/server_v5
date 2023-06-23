package com.template;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        webView = findViewById(R.id.webView);

        //get shared prefs
        String URL = sharedPreferences.getString(Keys.PREFS_NAME_LINK, "");


        if (savedInstanceState != null) {
            // Восстановление состояния WebView при повороте
            webView.restoreState(savedInstanceState.getBundle(Keys.KEY_WEB_VIEW));
        } else {
            // Загрузка URL-адреса в WebView
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            // Включение поддержки куков
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            // Настройка клиента WebView, чтобы переходы открывались в WebView
            webView.setWebViewClient(new WebViewClient());

            // Загрузка URL-адреса в WebView
            webView.loadUrl(URL);
        }




    }
    @Override
    public void onBackPressed() {
        // Проверка, возможно ли выполнить переход назад по страницам WebView
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Сохранение состояния WebView при повороте
        Bundle webViewState = new Bundle();
        webView.saveState(webViewState);
        outState.putBundle(Keys.KEY_WEB_VIEW, webViewState);
    }
}