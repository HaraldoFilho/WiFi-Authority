/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapActivity.java
 *  Last modified : 7/8/17 12:17 AM
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.ListIterator;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;

    private double latitude;
    private double minLatitude;
    private double maxLatitude;

    private double longitude;
    private double minLongitude;
    private double maxLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set the base minimum values for compare.
        // These are the maximum values they can have
        minLatitude = Constants.MAX_LATITUDE;
        minLongitude = Constants.MAX_LONGITUDE;

        // Set the base maximum values for compare.
        // These are the minimum values they can have
        maxLatitude = Constants.MIN_LATITUDE;
        maxLongitude = Constants.MIN_LONGITUDE;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        ConfiguredNetworks configuredNetworks = new ConfiguredNetworks(this);
        List<NetworkAdditionalData> networksData = configuredNetworks.getConfiguredNetworksData();

        ListIterator iterator = networksData.listIterator();

        while (iterator.hasNext()) {

            // Get latitude and longitude values for each configured network
            latitude = networksData.get(iterator.nextIndex()).getLatitude();
            longitude = networksData.get(iterator.nextIndex()).getLongitude();

            // Get the minimum and maximum values
            // for latitude and longitude
            // of all networks
            if (latitude < minLatitude) {
                minLatitude = latitude;
            }
            if (latitude > maxLatitude) {
                maxLatitude = latitude;
            }
            if (longitude < minLongitude) {
                minLongitude = longitude;
            }
            if (longitude > maxLongitude) {
                maxLongitude = longitude;
            }

            if ((latitude != Constants.DEFAULT_LATITUDE) && (longitude != Constants.DEFAULT_LONGITUDE)) {
                // Put a marker on the network position with its SSID as label
                LatLng networkPosition = new LatLng(latitude, longitude);
                String ssid = networksData.get(iterator.nextIndex()).getSSID();
                Marker marker = map.addMarker(new MarkerOptions().position(networkPosition).title(ssid));
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
            }

            iterator.next();

        }

        // Set the area where all networks are visible
        LatLngBounds allNetworksArea = new LatLngBounds(
                new LatLng(minLatitude, minLongitude), new LatLng(maxLatitude, maxLongitude));

        // Set the camera to the greatest possible zoom level that includes all networks
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(allNetworksArea,
                (int) getResources().getDimension(R.dimen.map_bounds_padding)));

    }

}

