/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ConfiguredNetworksListAdapter.java
 *  Last modified : 8/24/17 1:11 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
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
import com.apps.mohb.wifiauthority.Toasts;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;

import java.util.List;


// Adapter to connect Array List to ListView

public class ConfiguredNetworksListAdapter extends ArrayAdapter {

    private WifiManager wifiManager;
    private List<WifiConfiguration> wifiConfiguredNetworks;
    private List<ScanResult> wifiScannedNetworks;
    private ConfiguredNetworks configuredNetworks;
    private WifiConfiguration configuration;
    private String ssid;
    private String mac;
    private SharedPreferences settings;

    private String state;

    public ConfiguredNetworksListAdapter(Context context, List<WifiConfiguration> wifiConfiguredNetworks,
                                         ConfiguredNetworks configuredNetworks) {
        super(context, 0, wifiConfiguredNetworks);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguredNetworks = wifiConfiguredNetworks;
        this.configuredNetworks = configuredNetworks;
        try {
            this.configuredNetworks.getDataState();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        configuration = (WifiConfiguration) getItem(position);
        ssid = configuredNetworks.getDataSSID(configuration.SSID);
        mac = configuredNetworks.getMacAddressBySSID(ssid);

        wifiScannedNetworks = wifiManager.getScanResults();

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

            if (configuredNetworks.isAvailable(wifiScannedNetworks, ssid, mac)) {

                switch (wifiManager.calculateSignalLevel(
                        configuredNetworks.getScannedNetworkLevel(wifiScannedNetworks, mac), Constants.LEVELS)) {

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
                        R.drawable.ic_signal_wifi_out_of_reach_red_24dp));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        // Network state

        TextView txtNetworkState = (TextView) convertView.findViewById(R.id.txtNetStatus);
        txtNetworkName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlack));

        if (ssid.matches(configuredNetworks.getDataSSID(ConfiguredNetworks.supplicantSSID))) {

            switch (ConfiguredNetworks.supplicantNetworkState) {

                case DISCONNECTED:
                    wifiManager.disableNetwork(configuration.networkId);

                    switch (ConfiguredNetworks.lastSupplicantNetworkState) {
                        case AUTHENTICATING:
                            Toasts.showNetworkConnectionError(getContext(), R.string.toast_authentication_failed);
                            break;
                        case OBTAINING_IPADDR:
                            Toasts.showNetworkConnectionError(getContext(), R.string.toast_obt_ip_address_failed);
                            break;
                        default:
                            break;
                    }
                    state = getContext().getResources().getString(R.string.net_state_disconnected);
                    ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.DISCONNECTED;
                    break;

                case SCANNING:
                    state = getContext().getResources().getString(R.string.net_state_scannig);
                    ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.SCANNING;
                    break;

                case CONNECTING:
                    state = getContext().getResources().getString(R.string.net_state_connecting);
                    ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.CONNECTING;
                    break;

                case AUTHENTICATING:
                    state = getContext().getResources().getString(R.string.net_state_authenticating);
                    ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.AUTHENTICATING;
                    break;

                case OBTAINING_IPADDR:
                    state = getContext().getResources().getString(R.string.net_state_obt_ip_address);
                    ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.OBTAINING_IPADDR;
                    try {
                        if (configuredNetworks.isSSIDConnected(wifiConfiguredNetworks, ssid)) {
                            txtNetworkName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
                            state = getContext().getResources().getString(R.string.layout_net_connected);
                            ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.CONNECTED;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
            txtNetworkState.setText(state);
        } else {
            try {
                if (configuredNetworks.isAvailable(wifiScannedNetworks, ssid, mac)) {
                    txtNetworkState.setText(R.string.layout_net_disconnected);
                } else {
                    txtNetworkState.setText(R.string.layout_net_out_of_reach);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Hidden network

        ImageView imgHidden = (ImageView) convertView.findViewById(R.id.imgHidden);

        if ((configuredNetworks.isAvailableByMacAddress(wifiScannedNetworks, mac)
                && !configuredNetworks.isAvailableBySSID(wifiScannedNetworks, ssid))
                || (configuredNetworks.isHidden(ssid) && (!configuredNetworks.isAvailable(wifiScannedNetworks, ssid, mac)))) {
            imgHidden.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_visibility_off_grey_24dp));
        } else {
            imgHidden.setImageDrawable(null);
        }

        return convertView;

    }

}
