/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ConfiguredNetworksListAdapter.java
 *  Last modified : 6/26/17 11:15 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
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

import java.io.IOException;
import java.util.List;


// Adapter to connect Array List to ListView

public class ConfiguredNetworksListAdapter extends ArrayAdapter {

    private WifiManager wifiManager;
    private List<ScanResult> wifiScannedNetworks;
    private ConfiguredNetworks configuredNetworks;
    private WifiConfiguration configuration;
    private String ssid;

    private SharedPreferences settings;

    public ConfiguredNetworksListAdapter(Context context, List<WifiConfiguration> list,
                                         ConfiguredNetworks configuredNetworks) {
        super(context, 0, list);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiScannedNetworks = wifiManager.getScanResults();
        this.configuredNetworks = configuredNetworks;
        try {
            this.configuredNetworks.getDataState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        configuration = (WifiConfiguration) getItem(position);
        ssid = configuredNetworks.getDataSSID(configuration.SSID);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_networks, parent, false);
        }

        TextView txtNetworkName = (TextView) convertView.findViewById(R.id.txtNetTitle);
        TextView txtNetworkDescription = (TextView) convertView.findViewById(R.id.txtNetSubtitle);

        // Get network description
        String description = configuredNetworks.getDescriptionBySSID(ssid);

        // If has no description use the SSID instead
        if (description.isEmpty()) {
            description = configuredNetworks.getCfgSSID(ssid);
            configuredNetworks.setDescriptionBySSID(ssid, description);
        }

        String header = settings.getString(getContext().getResources().getString(R.string.pref_key_header),
                getContext().getResources().getString(R.string.pref_def_header));

        // Check if option to show description first is selected
        if (header.matches(Constants.PREF_HEADER_DESCRIPTION)) {
            txtNetworkName.setText(description);
            txtNetworkDescription.setText(ssid);
        } else {
            txtNetworkName.setText(ssid);
            txtNetworkDescription.setText(description);
        }

        // Network security

        TextView txtNetworkSecurity = (TextView) convertView.findViewById(R.id.txtNetSecurity);
        String keyMng = configuration.allowedKeyManagement.toString();
        String authAlg = configuration.allowedAuthAlgorithms.toString();

        ImageView imgLocker = (ImageView) convertView.findViewById(R.id.imgLockerCfg);

        switch (keyMng) {

            case Constants.KEY_NONE_WEP:
                if (authAlg.contains(String.valueOf(WifiConfiguration.AuthAlgorithm.SHARED))) {
                    txtNetworkSecurity.setText(R.string.security_WEP);
                    imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                            R.drawable.ic_encryption_yellow_24dp));
                } else {
                    txtNetworkSecurity.setText(R.string.security_deactivated);
                    imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                            R.drawable.ic_no_encryption_red_24dp));
                }
                break;

            case Constants.KEY_WPA:
                txtNetworkSecurity.setText(R.string.security_WPA);
                imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_enhanced_encryption_green_24dp));
                break;

            case Constants.KEY_WPA2:
                txtNetworkSecurity.setText(R.string.security_WPA);
                imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_enhanced_encryption_green_24dp));
                break;

            case Constants.KEY_EAP:
                txtNetworkSecurity.setText(R.string.security_EAP);
                imgLocker.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_enhanced_encryption_green_24dp));
                break;

        }


        // Network signal level

        ImageView imgWiFi = (ImageView) convertView.findViewById(R.id.imgWiFiOk);

        try {

            if ((configuredNetworks.isAvailableBySSID(wifiScannedNetworks, ssid))
                    || (configuredNetworks.isAvailableByMacAddress(wifiScannedNetworks,
                    configuredNetworks.getMacAddressBySSID(ssid)))
                    || (configuredNetworks.isConnected(wifiManager.getConfiguredNetworks(), ssid))) {
                switch (wifiManager.calculateSignalLevel(
                        configuredNetworks.getScannedNetworkLevel(wifiScannedNetworks, ssid), Constants.LEVELS)) {

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

            } else {
                imgWiFi.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_signal_wifi_unavailable_red_24dp));
            }


            // Network status

            TextView txtNetworkStatus = (TextView) convertView.findViewById(R.id.txtNetStatus);
            txtNetworkStatus.setText(R.string.layout_net_out_of_reach);

            if (configuredNetworks.isConnected(wifiManager.getConfiguredNetworks(), ssid)) {
                txtNetworkName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
                txtNetworkStatus.setText(R.string.layout_net_connected);
            } else {
                txtNetworkName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlack));
                txtNetworkStatus.setText(R.string.layout_net_disconnected);
            }


            // Network hidden

            ImageView imgHidden = (ImageView) convertView.findViewById(R.id.imgHidden);

            if (configuration.hiddenSSID) {
                imgHidden.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.ic_visibility_off_grey_24dp));
            } else {
                imgHidden.setImageDrawable(null);
            }


        } catch (NullPointerException e) {
            wifiManager.setWifiEnabled(true);
            e.printStackTrace();
        }

        return convertView;

    }


}
