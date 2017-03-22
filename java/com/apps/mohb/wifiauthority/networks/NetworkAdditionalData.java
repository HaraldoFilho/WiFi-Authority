/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : NetworkAdditionalData.java
 *  Last modified : 1/2/17 9:56 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.networks;


public class NetworkAdditionalData {

    private String ssid;
    private String mac;
    private String description;
    private double latitude;
    private double longitude;


    public NetworkAdditionalData() {
        // empty constructor
    }

    public NetworkAdditionalData(String ssid, String bssid, String description, double latitude, double longitude) {
        this.ssid = ssid;
        this.mac = bssid;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getSSID() {
        return ssid;
    }

    public String getMacAddress() {
        return mac;
    }

    public String getDescription() {
        return description;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public void setMacAddress(String mac) {
        this.mac = mac;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
