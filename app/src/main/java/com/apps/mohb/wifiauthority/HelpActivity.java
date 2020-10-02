/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : HelpActivity.java
 *  Last modified : 10/1/20 1:33 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;


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
        webView.setWebViewClient(new WebViewClient());

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load options_help page in webView
        webView.loadUrl(getString(R.string.url_help_path) + bundle.getString(Constants.KEY_URL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Send question
        if (id == R.id.action_send_question) {
            String[] address = new String[Constants.QUESTION_ARRAY_SIZE];
            address[Constants.LIST_HEAD] = getString(R.string.info_feedback_email);
            composeEmail(address, getString(R.string.action_question) + Constants.SPACE + getString(R.string.action_about_application)
                    + Constants.SPACE + getString(R.string.info_app_name));
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPause() {
        super.onPause();
    }


    // CLASS METHOD

    /*
         Compose a e-mail to send a question
    */
    private void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(Constants.KEY_EMAIL)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}