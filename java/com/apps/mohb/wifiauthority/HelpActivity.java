/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : HelpActivity.java
 *  Last modified : 6/27/17 1:49 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class HelpActivity extends AppCompatActivity {

    private WebView webView;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getExtras();

        // create webView that will show options_help page
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

    }

    @Override
    protected void onStart() {
        super.onStart();
        // show toast to inform that will
        // get options_help page from internet
        Toasts.setContext(getApplicationContext());
        Toasts.createHelpPage();
        Toasts.showHelpPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load options_help page in webView
        webView.loadUrl(getString(R.string.url_website) + bundle.getString("url"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // cancel toast if page if exit options_help screen
        Toasts.cancelHelpPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            // Send question
            case R.id.action_send_question:
                String[] address = new String[Constants.QUESTION_ARRAY_SIZE];
                address[Constants.LIST_HEAD] = getString(R.string.info_feedback_email);
                composeEmail(address, getString(R.string.action_question) + " " + getString(R.string.action_about_application)
                        + " " + getString(R.string.info_app_name));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Compose a e-mail to send a question
    private void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            // If can, go back
            // to the previous page
            webView.goBack();
        } else {
            super.onBackPressed();
        }

    }

}