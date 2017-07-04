/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : WiFiCheckActivity.java
 *  Last modified : 7/1/17 1:59 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.apps.mohb.wifiauthority.fragments.dialogs.WifiDisabledAlertFragment;


public class WiFiCheckActivity extends AppCompatActivity implements
        WifiDisabledAlertFragment.WifiDisabledDialogListener {

    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Bundle bundle = getIntent().getExtras();

        // check if WiFi is enabled
        if (wifiManager.isWifiEnabled()) { // if it is enabled set the networks list adapter

            switch (bundle.getInt(Constants.KEY_ACTIVITY)) {

                case Constants.ACTIVITY_MAIN:
                    startMainActivity();
                    break;

                case Constants.ACTIVITY_SCAN:
                    startScanActivity();
                    break;

            }

            finish();

        } else {

            switch (bundle.getInt(Constants.KEY_ACTIVITY)) {

                case Constants.ACTIVITY_MAIN:
                    // if is disabled show dialog to ask to enable it
                    DialogFragment dialog = new WifiDisabledAlertFragment();
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "WifiDisabledAlertFragment");
                    break;

                case Constants.ACTIVITY_SCAN:
                    wifiManager.setWifiEnabled(true);
                    startScanActivity();
                    finish();
                    break;

            }

        }

    }

    @Override
    public void onBackPressed() {
        if (!wifiManager.isWifiEnabled()) {
            super.onBackPressed();
        } else {
            startMainActivity();
        }
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void startScanActivity() {
        Intent intent = new Intent(this, ScanNetworksActivity.class);
        startActivity(intent);
    }

    // ENABLE WIFI DIALOG

    @Override  // YES
    public void onAlertWifiDialogPositiveClick(DialogFragment dialog) {
        wifiManager.setWifiEnabled(true);
        startMainActivity();
        finish();
    }

    @Override // NO
    public void onAlertWifiDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
        finish();
    }

}
