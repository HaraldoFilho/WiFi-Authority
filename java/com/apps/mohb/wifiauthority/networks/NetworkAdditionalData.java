/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : NetworkAdditionalData.java
 *  Last modified : 6/25/17 2:33 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.networks;


public class NetworkAdditionalData {

    private String ssid;
    private String mac;
    private String security;
    private String description;
    private double latitude;
    private double longitude;


    public NetworkAdditionalData() {
        // empty constructor
    }

    public NetworkAdditionalData(String ssid, String bssid, String security, String description, double latitude, double longitude) {
        this.ssid = ssid;
        this.mac = bssid;
        this.security = security;
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

    public String getSecurity() {
        return security;
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

    public void setSecurity(String security) {
        this.security = security;
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
