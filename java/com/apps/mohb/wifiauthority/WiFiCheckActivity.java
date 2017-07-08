/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : WiFiCheckActivity.java
 *  Last modified : 7/8/17 12:39 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class WiFiCheckActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Bundle bundle = getIntent().getExtras();

        // check if WiFi is enabled
        if (wifiManager.isWifiEnabled()) {

            // Opens the activity that was called
            switch (bundle.getInt(Constants.KEY_ACTIVITY)) {

                case Constants.ACTIVITY_MAIN:
                    intent = new Intent(this, MainActivity.class);
                    break;

                case Constants.ACTIVITY_SCAN:
                    intent = new Intent(this, ScanNetworksActivity.class);
                    break;

            }

        } else { // if is disabled show button to turn it on
            intent = new Intent(this, WiFiDisabledActivity.class);
        }

        // Call activity and finish this
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        if (!wifiManager.isWifiEnabled()) {
            super.onBackPressed();
        } else {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

}
