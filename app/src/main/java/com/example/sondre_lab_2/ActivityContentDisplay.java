package com.example.sondre_lab_2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import java.net.URL;

public class ActivityContentDisplay extends AppCompatActivity {

    // Loads view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_display);
        startCode();
    }

    protected void startCode() {
        WebView session = findViewById(R.id.webView);
        session.loadUrl(getIntent().getStringExtra("link"));
    }
}
