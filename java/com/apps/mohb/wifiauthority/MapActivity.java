/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapActivity.java
 *  Last modified : 6/29/17 1:12 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;
import com.apps.mohb.wifiauthority.networks.NetworkAdditionalData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.ListIterator;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Bundle bundle;

    private String ssid;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bundle = getIntent().getExtras();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // If bundle has no SSID, i.e. no individual network was clicked, iterates over the list of
        // configured networks and, if available, put a marker on the location of the network
        if (bundle.getString(Constants.KEY_SSID).isEmpty()) {
            ConfiguredNetworks configuredNetworks = new ConfiguredNetworks(this);
            List<NetworkAdditionalData> networksData = configuredNetworks.getConfiguredNetworksData();

            ListIterator iterator = networksData.listIterator();

            while (iterator.hasNext()) {
                ssid = networksData.get(iterator.nextIndex()).getSSID();
                latitude = networksData.get(iterator.nextIndex()).getLatitude();
                longitude = networksData.get(iterator.nextIndex()).getLongitude();

                if ((latitude != Constants.DEFAULT_LATITUDE) && (longitude != Constants.DEFAULT_LONGITUDE)) {
                    LatLng networkPosition = new LatLng(latitude, longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(networkPosition).title(ssid));
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
                }

                iterator.next();
            }

            LatLng currentLocation = new LatLng(bundle.getDouble(Constants.KEY_LATITUDE),
                    bundle.getDouble(Constants.KEY_LONGITUDE));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, Constants.MAP_LOW_ZOOM_LEVEL));

        } else { // if an individual network was clicked, put a marker on its location

            ssid = bundle.getString(Constants.KEY_SSID);
            latitude = bundle.getDouble(Constants.KEY_LATITUDE);
            longitude = bundle.getDouble(Constants.KEY_LONGITUDE);

            LatLng networkPosition = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(networkPosition).title(ssid));
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(networkPosition, Constants.MAP_HIGH_ZOOM_LEVEL));

        }
    }
}
