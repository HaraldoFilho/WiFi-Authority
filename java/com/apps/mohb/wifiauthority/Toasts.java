/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Toasts.java
 *  Last modified : 12/22/16 8:31 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


// This class manages all the toasts in the application

public class Toasts {

    private static Toast scanningNetworks;
    private static Toast unableRemoveNetwork;
    private static Toast noLocationInformation;
    private static Toast networkIsConfigured;
    private static Toast wifiDisabled;
    private static Toast legalNotices;
    private static Toast helpPage;

    private static Context context;


    public static void setContext(Context c) {
        context = c;
    }


    // Toast to notify that it is scanning for available networks
    public static void showScanningNetworks(Context c) {
        scanningNetworks = Toast.makeText((c), R.string.toast_scan_networks, Toast.LENGTH_SHORT);
        scanningNetworks.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        scanningNetworks.show();
    }

    public static void cancelScanningNetworks() {
        if (scanningNetworks != null) {
            scanningNetworks.cancel();
        }
    }

    // Toast to notify that there is no location information
    public static void showNoLocationInformation(Context c, int textId) {
        noLocationInformation = Toast.makeText((c), textId, Toast.LENGTH_SHORT);
        noLocationInformation.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        noLocationInformation.show();
    }

    public static void cancelNoLocationInformation() {
        if (noLocationInformation != null) {
            noLocationInformation.cancel();
        }
    }

    // Toast to notify that it is unable to remove network
    public static void showUnableRemoveNetwork(Context c) {
        unableRemoveNetwork = Toast.makeText((c), R.string.toast_unable_remove_network, Toast.LENGTH_SHORT);
        unableRemoveNetwork.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        unableRemoveNetwork.show();
    }

    public static void cancelUnableRemoveNetwork() {
        if (unableRemoveNetwork != null) {
            unableRemoveNetwork.cancel();
        }
    }

    // Toast to notify that wifi is disabled
    public static void showWiFiDisabled(Context c) {
        wifiDisabled = Toast.makeText((c), R.string.toast_wifi_disabled, Toast.LENGTH_SHORT);
        wifiDisabled.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        wifiDisabled.show();
    }

    public static void cancelWiFiDisabled() {
        if (wifiDisabled != null) {
            wifiDisabled.cancel();
        }
    }

    // Toast to notify that network is already configured
    public static void showNetworkIsConfigured(Context c) {
        networkIsConfigured = Toast.makeText((c), R.string.toast_network_is_configured, Toast.LENGTH_SHORT);
        networkIsConfigured.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        networkIsConfigured.show();
    }

    public static void cancelNetworkIsConfigured() {
        if (networkIsConfigured != null) {
            networkIsConfigured.cancel();
        }
    }

    // Toast to notify that is getting Legal Notices text from the internet

    public static void createLegalNotices() {
        legalNotices = Toast.makeText((context), "", Toast.LENGTH_SHORT);
        legalNotices.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
    }

    public static void setLegalNoticesText(int textId) {
        legalNotices.setText(textId);
    }

    public static void showLegalNotices() {
        legalNotices.show();
    }

    public static void cancelLegalNotices() {
        if (legalNotices != null) {
            legalNotices.cancel();
        }
    }


    // Toast to notify that is getting a options_help page from the internet

    public static void createHelpPage() {
        helpPage = Toast.makeText((context), R.string.toast_get_help_page, Toast.LENGTH_SHORT);
        helpPage.setGravity(Gravity.CENTER, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
    }

    public static void showHelpPage() {
        helpPage.show();
    }

    public static void cancelHelpPage() {
        if (helpPage != null) {
            helpPage.cancel();
        }
    }


    // Cancel all toasts

    public static void cancelAllToasts() {
        cancelScanningNetworks();
        cancelUnableRemoveNetwork();
        cancelNetworkIsConfigured();
        cancelWiFiDisabled();
        cancelNoLocationInformation();
        cancelLegalNotices();
        cancelHelpPage();
    }

}