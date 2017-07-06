/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : DetailsActivity.java
 *  Last modified : 7/6/17 1:08 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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


public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String ssid;
    private String mac;
    private Double latitude;
    private Double longitude;

    private Bundle bundle;
    private GoogleMap mMap;
    private MapView map;

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private ConfiguredNetworks configuredNetworks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        configuredNetworks = new ConfiguredNetworks(this);


        TextView txtNetworkSSID = (TextView) findViewById(R.id.txtNetSSID);
        TextView txtNetworkMac = (TextView) findViewById(R.id.txtNetMac);
        TextView txtNetworkIpAddress = (TextView) findViewById(R.id.txtNetIpAddress);
        TextView txtNetworkLinkSpeed = (TextView) findViewById(R.id.txtNetSpeed);
        TextView txtNetworkLinkSpeedUnit = (TextView) findViewById(R.id.txtNetSpeedUnit);
        TextView txtNetworkSignalLevel = (TextView) findViewById(R.id.txtNetSignalLevel);
        TextView txtNetworkSignalLevelUnit = (TextView) findViewById(R.id.txtNetSignalLevelUnit);

        // create map and initialize it
        map = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        bundle = getIntent().getExtras();

        ssid = bundle.getString(Constants.KEY_SSID);
        mac = bundle.getString(Constants.KEY_BSSID).toUpperCase();

        txtNetworkSSID.setText(ssid);
        txtNetworkMac.setText(mac);

        txtNetworkLinkSpeedUnit.setText("");
        txtNetworkSignalLevelUnit.setText("");

        if (ssid.matches(configuredNetworks.getDataSSID(wifiInfo.getSSID()))) {
            txtNetworkIpAddress.setText(getIpAddressString(wifiInfo.getIpAddress()));
            txtNetworkLinkSpeed.setText(String.valueOf(wifiInfo.getLinkSpeed()));
            txtNetworkLinkSpeedUnit.setText(" " + WifiInfo.LINK_SPEED_UNITS);
            txtNetworkSignalLevel.setText(String.valueOf(wifiInfo.getRssi()));
            txtNetworkSignalLevelUnit.setText(" " + getString(R.string.layout_db));
            setNetworkFrequencyText(true, "");
        } else {
            txtNetworkIpAddress.setText(Constants.NO_INFO);
            txtNetworkLinkSpeed.setText(Constants.NO_INFO);
            txtNetworkSignalLevel.setText(Constants.NO_INFO);
            setNetworkFrequencyText(false, Constants.NO_INFO);
        }

    }

    private void setNetworkFrequencyText(boolean setValue, String noInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TextView txtNetworkFrequency = (TextView) findViewById(R.id.txtNetFrequency);
            TextView txtNetworkFrequencyUnit = (TextView) findViewById(R.id.txtNetFrequencyUnit);
            if (setValue) {
                txtNetworkFrequency.setText(String.valueOf(wifiInfo.getFrequency()));
                txtNetworkFrequencyUnit.setText(" " + WifiInfo.FREQUENCY_UNITS);
            } else {
                txtNetworkFrequency.setText(noInfo);
                txtNetworkFrequencyUnit.setText("");

            }
        }
    }

    private String getIpAddressString(int ipAddr) {
        byte[] ipAddressBytes = BigInteger.valueOf(ipAddr).toByteArray();
        String ipAddress = String.valueOf(ipAddressBytes[3]) + "."
                + String.valueOf(ipAddressBytes[2]) + "."
                + String.valueOf(ipAddressBytes[1]) + "."
                + String.valueOf(ipAddressBytes[0]);

        return ipAddress;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        latitude = bundle.getDouble(Constants.KEY_LATITUDE);
        longitude = bundle.getDouble(Constants.KEY_LONGITUDE);

        if ((latitude != Constants.DEFAULT_LATITUDE) && (longitude != Constants.DEFAULT_LONGITUDE)) {
            LatLng networkPosition = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(networkPosition).title(ssid));
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(networkPosition, Constants.MAP_HIGH_ZOOM_LEVEL));

        } else { // if no location data, show toast to inform this
            Toasts.showMissingInformation(getApplicationContext(), R.string.toast_no_location_information);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }
}
