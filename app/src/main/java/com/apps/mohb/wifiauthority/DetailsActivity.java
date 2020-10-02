/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : DetailsActivity.java
 *  Last modified : 10/1/20 1:33 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigInteger;
import java.util.Objects;


public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String ssid;

    private Bundle bundle;
    private MapView map;

    private WifiInfo wifiInfo;
    private ConfiguredNetworks configuredNetworks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        configuredNetworks = new ConfiguredNetworks(this);

        // Views
        TextView txtNetworkSSID = findViewById(R.id.txtNetSSID);
        TextView txtNetworkMac = findViewById(R.id.txtNetMac);
        TextView txtNetworkIpAddressTitle = findViewById(R.id.txtNetIpAddressTitle);
        TextView txtNetworkIpAddress = findViewById(R.id.txtNetIpAddress);
        TextView txtNetworkLinkSpeedTitle = findViewById(R.id.txtNetSpeedTitle);
        TextView txtNetworkLinkSpeed = findViewById(R.id.txtNetSpeed);
        TextView txtNetworkLinkSpeedUnit = findViewById(R.id.txtNetSpeedUnit);
        TextView txtNetworkSignalLevelTitle = findViewById(R.id.txtNetSignalLevelTitle);
        TextView txtNetworkSignalLevel = findViewById(R.id.txtNetSignalLevel);
        TextView txtNetworkSignalLevelUnit = findViewById(R.id.txtNetSignalLevelUnit);

        // create map and initialize it
        map = findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        bundle = getIntent().getExtras();

        // Get SSID and MAC address of the network...
        assert bundle != null;
        ssid = bundle.getString(Constants.KEY_SSID);
        String mac = Objects.requireNonNull(bundle.getString(Constants.KEY_BSSID)).toUpperCase();

        // ... and show them
        txtNetworkSSID.setText(ssid);
        txtNetworkMac.setText(mac);

        // Set link speed and signal level units to empty in case
        // of they are not shown because network is not connected
        txtNetworkLinkSpeedUnit.setText(Constants.EMPTY);
        txtNetworkSignalLevelUnit.setText(Constants.EMPTY);

        // Check if the network is connected or connecting
        if ((ssid.matches(configuredNetworks.getDataSSID(wifiInfo.getSSID())))) {

            // Get IP address string in the format XXX.XXX.XXX.XXX
            String ipAddressString;
            try {
                ipAddressString = getIpAddressString(wifiInfo.getIpAddress());
            } catch (ArrayIndexOutOfBoundsException e) {
                ipAddressString = Constants.EMPTY;
                e.printStackTrace();
            }

            // Show IP address
            if (!ipAddressString.matches(Constants.EMPTY)) {
                txtNetworkIpAddress.setText(ipAddressString);
            } else {
                doNotShowView(txtNetworkIpAddressTitle);
                doNotShowView(txtNetworkIpAddress);
            }

            // Show link speed
            if (wifiInfo.getLinkSpeed() > Constants.NO_FREQ_SET) {
                txtNetworkLinkSpeed.setText(String.valueOf(wifiInfo.getLinkSpeed()));
                String speedUnits = Constants.SPACE + WifiInfo.LINK_SPEED_UNITS;
                txtNetworkLinkSpeedUnit.setText(speedUnits);
            } else {
                doNotShowView(txtNetworkLinkSpeedTitle);
                doNotShowView(txtNetworkLinkSpeed);
                doNotShowView(txtNetworkLinkSpeedUnit);
            }

            // Show signal level
            txtNetworkSignalLevel.setText(String.valueOf(wifiInfo.getRssi()));
            String levelUnit = Constants.SPACE + getString(R.string.layout_db);
            txtNetworkSignalLevelUnit.setText(levelUnit);

            // Show frequency
            setNetworkFrequencyText(true);

        } else {
            // There is no information to show
            doNotShowView(txtNetworkIpAddressTitle);
            doNotShowView(txtNetworkIpAddress);
            doNotShowView(txtNetworkLinkSpeedTitle);
            doNotShowView(txtNetworkLinkSpeed);
            doNotShowView(txtNetworkLinkSpeedUnit);
            doNotShowView(txtNetworkSignalLevelTitle);
            doNotShowView(txtNetworkSignalLevel);
            doNotShowView(txtNetworkSignalLevelUnit);
            setNetworkFrequencyText(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        double latitude = bundle.getDouble(Constants.KEY_LATITUDE);
        double longitude = bundle.getDouble(Constants.KEY_LONGITUDE);

        // Check if there is location data and show it on map
        if ((latitude != Constants.DEFAULT_LATITUDE) && (longitude != Constants.DEFAULT_LONGITUDE)) {
            LatLng networkPosition = new LatLng(latitude, longitude);
            Marker marker = googleMap.addMarker(new MarkerOptions().position(networkPosition));
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(networkPosition, Constants.MAP_DETAILS_ZOOM_LEVEL));

        } else { // if no location data, show toast to inform this
            Toasts.showNoDetailedInformation(getApplicationContext(), R.string.toast_no_location_information);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    // CLASS METHODS

    /*
         Show frequency of the network if build version is 21 or higher
         as this feature is not available in lower builds
    */
    private void setNetworkFrequencyText(boolean isActive) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TextView txtNetworkFrequencyTitle = (TextView) findViewById(R.id.txtNetFrequencyTitle);
            TextView txtNetworkFrequency = (TextView) findViewById(R.id.txtNetFrequency);
            TextView txtNetworkFrequencyUnit = (TextView) findViewById(R.id.txtNetFrequencyUnit);
            // Check if it is the current active network
            if (isActive) {
                if ((configuredNetworks.getFrequency(ssid) == Constants.NO_FREQ_SET)
                        || (configuredNetworks.getFrequency(ssid) != wifiInfo.getFrequency())) {
                    configuredNetworks.setFrequency(ssid, wifiInfo.getFrequency());
                }
            }
            if (configuredNetworks.getFrequency(ssid) > Constants.NO_FREQ_SET) {
                String netFreq;
                if (configuredNetworks.getFrequency(ssid) < Constants.FREQ_5GHZ) {
                    netFreq = Constants.SPACE + getResources().getString(R.string.layout_net_freq_2p4ghz);
                    txtNetworkFrequency.setText(netFreq);
                } else {
                    netFreq = Constants.SPACE + getResources().getString(R.string.layout_net_freq_5ghz);
                    txtNetworkFrequency.setText(netFreq);
                }
                txtNetworkFrequencyUnit.setText(getResources().getString(R.string.layout_net_freq_unit));
            } else {
                doNotShowView(txtNetworkFrequencyTitle);
                doNotShowView(txtNetworkFrequency);
                doNotShowView(txtNetworkFrequencyUnit);
            }
        }
    }

    /*
         The IP address information is an integer representing the bytes corresponding to each ip number
         This method gets the IP address to show on a human readable form
    */
    private String getIpAddressString(int ipAddressInteger) throws ArrayIndexOutOfBoundsException {

        // Get the bytes from the integer
        byte[] ipAddressBytes = BigInteger.valueOf(Integer.reverseBytes(ipAddressInteger)).toByteArray();

        // Convert signed integers to signed
        int ip1 = convertToUnsignedInteger(ipAddressBytes[0]);
        int ip2 = convertToUnsignedInteger(ipAddressBytes[1]);
        int ip3 = convertToUnsignedInteger(ipAddressBytes[2]);
        int ip4 = convertToUnsignedInteger(ipAddressBytes[3]);

        // Build and return the IP address string
        return ip1 + Constants.DOT + ip2 + Constants.DOT + ip3 + Constants.DOT + ip4;

    }

    /*
         Convert signed integers into unsigned
    */
    private int convertToUnsignedInteger(int signedInteger) {
        return signedInteger & 0xFF;
    }

    /*
         Set height of view to 0 to not show it
    */
    private void doNotShowView(TextView view) {
        view.setHeight(Constants.INVISIBLE);
    }


}
