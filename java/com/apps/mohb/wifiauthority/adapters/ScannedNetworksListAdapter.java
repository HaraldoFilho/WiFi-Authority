/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ScannedNetworksListAdapter.java
 *  Last modified : 1/2/17 9:56 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;

import java.util.List;


// Adapter to connect Array List to ListView

public class ScannedNetworksListAdapter extends ArrayAdapter {

    private WifiManager wifiManager;
    private ConfiguredNetworks configuredNetworks;

    public ScannedNetworksListAdapter(Context context, List<ScanResult> list) {
        super(context, 0, list);
        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        configuredNetworks = new ConfiguredNetworks(getContext());

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ScanResult result = (ScanResult) getItem(position);

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_scan, parent, false);

        TextView txtScanNetworkName = (TextView) convertView.findViewById(R.id.txtScanNetName);
        txtScanNetworkName.setText(result.SSID);

        ImageView imgCfg = (ImageView) convertView.findViewById(R.id.imgCfg);

        if (wifiManager.isWifiEnabled()) {

            // Check if network is already configured
            if (configuredNetworks.isConfiguredBySSID(wifiManager.getConfiguredNetworks(), result.SSID)) {
                imgCfg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_star_green_24dp));

                // Check if network is connected
                if (configuredNetworks.isConnected(wifiManager.getConfiguredNetworks(), result.SSID)) {
                    txtScanNetworkName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
                }

            }

        } else {
            wifiManager.setWifiEnabled(true);
        }


        // Network security

        TextView txtScanNetworkSecurity = (TextView) convertView.findViewById(R.id.txtScanNetSecurity);
        String capabilities = result.capabilities;

        ImageView imgLocker = (ImageView) convertView.findViewById(R.id.imgLocker);

        if (capabilities.contains(Constants.SCAN_WPA)) {
            txtScanNetworkSecurity.setText(getContext().getResources().getString(R.string.security_WPA));
            imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_enhanced_encryption_green_24dp));
        } else if (capabilities.contains(Constants.SCAN_EAP)) {
            txtScanNetworkSecurity.setText(getContext().getResources().getString(R.string.security_EAP));
            imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_enhanced_encryption_green_24dp));
        } else if (capabilities.contains(Constants.SCAN_WEP)) {
            txtScanNetworkSecurity.setText(getContext().getResources().getString(R.string.security_WEP));
            imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_encryption_yellow_24dp));
        } else {
            txtScanNetworkSecurity.setText(getContext().getResources().getString(R.string.security_deactivated));
            imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_no_encryption_red_24dp));
        }


        // Network signal level

        ImageView imgWiFi = (ImageView) convertView.findViewById(R.id.imgWiFi);

        TextView txtScanNetworkSignal = (TextView) convertView.findViewById(R.id.txtScanNetSignal);
        txtScanNetworkSignal.setText(String.valueOf(result.level));

        switch (wifiManager.calculateSignalLevel(result.level, Constants.LEVELS)) {

            case Constants.LEVEL_HIGH:
                imgWiFi.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_wifi_high_green_24dp));
                break;

            case Constants.LEVEL_LOW:
                imgWiFi.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_wifi_mid_yellow_24dp));
                break;

            case Constants.LEVEL_VERY_LOW:
                imgWiFi.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_wifi_low_red_24dp));
                break;

        }

        return convertView;

    }


}
