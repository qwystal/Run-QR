package com.crystalpixl.runqr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.net.MalformedURLException;

public class WebPlayer extends AppCompatActivity{


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webplayer);

        MaterialButton backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view -> {
            Intent i = new Intent(WebPlayer.this, MainActivity.class);
            startActivity(i);
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webPlayer() throws MalformedURLException {
        Bundle extras = getIntent().getExtras();
        String url = null;
        //The key argument here must match that used in the other activity
        if (extras != null) {
            url = extras.getString("key");
        }

        WebView webPlayer = findViewById(R.id.webPlayer);
        WebSettings webSettings = webPlayer.getSettings();
        webPlayer.setEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);
        webPlayer.requestFocus();
        webPlayer.setSoundEffectsEnabled(true);
        webPlayer.setVisibility(View.VISIBLE);
        webPlayer.getSettings().setUseWideViewPort(false);
        webPlayer.getSettings().setLoadWithOverviewMode(true);
        webPlayer.setInitialScale(210);
        webPlayer.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webPlayer.loadUrl(url);

    }

    @Override
    protected void onStart()
    {
        try {
            webPlayer();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        super.onStart();
    }


}