/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapActivity.java
 *  Last modified : 7/7/17 12:54 AM
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
    private double minLatitude;
    private double minLongitude;
    private double maxLatitude;
    private double maxLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        minLatitude = Constants.MAX_LATITUDE;
        maxLatitude = Constants.MIN_LATITUDE;
        minLongitude = Constants.MAX_LONGITUDE;
        maxLongitude = Constants.MIN_LONGITUDE;

        bundle = getIntent().getExtras();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ConfiguredNetworks configuredNetworks = new ConfiguredNetworks(this);
        List<NetworkAdditionalData> networksData = configuredNetworks.getConfiguredNetworksData();

        ListIterator iterator = networksData.listIterator();

        while (iterator.hasNext()) {
            ssid = networksData.get(iterator.nextIndex()).getSSID();
            latitude = networksData.get(iterator.nextIndex()).getLatitude();
            longitude = networksData.get(iterator.nextIndex()).getLongitude();

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
                LatLng networkPosition = new LatLng(latitude, longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(networkPosition).title(ssid));
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wifi_marker_blue_36dp));
            }

            iterator.next();
        }

        double deltaLatitude = maxLatitude - minLatitude;
        double deltaLongitude = maxLongitude - minLongitude;

        double maxDelta;

        if (deltaLatitude > deltaLongitude) {
            maxDelta = deltaLatitude;
        } else {
            maxDelta = deltaLongitude;
        }

        maxDelta = absoluteValue(maxDelta);

        if (maxDelta > Constants.MAX_MAX_DELTA) {
            maxDelta = Constants.MAX_MAX_DELTA;
        }

        maxDelta /= Constants.MAP_MAX_ZOOM_LEVEL;
        maxDelta -= Constants.MAP_MIN_ZOOM_LEVEL;

        maxDelta = absoluteValue(maxDelta);

        int zoomLevel = (int) absoluteValue(
                (Constants.MAP_MAX_ZOOM_LEVEL - Constants.MAP_MIN_ZOOM_LEVEL) - maxDelta);

        LatLng currentLocation = new LatLng(bundle.getDouble(Constants.KEY_LATITUDE),
                bundle.getDouble(Constants.KEY_LONGITUDE));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

    }

    private double absoluteValue(double value) {
        if (value < 0) {
            value *= -1;
        }
        return value;
    }

}

