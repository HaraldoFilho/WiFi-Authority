/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ConfiguredNetworks.java
 *  Last modified : 7/10/17 9:43 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.networks;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.apps.mohb.wifiauthority.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class ConfiguredNetworks {

    private static ArrayList<NetworkData> networksData;
    public static NetworkInfo.DetailedState supplicantNetworkState;
    public static NetworkInfo.DetailedState lastSupplicantNetworkState;
    public static String supplicantSSID;
    public static String lastSupplicantSSID;

    private SharedPreferences preferences;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;


    public ConfiguredNetworks(Context context) {
        if (networksData == null) {
            networksData = new ArrayList<>();
        }

        supplicantNetworkState = NetworkInfo.DetailedState.IDLE;
        lastSupplicantNetworkState = NetworkInfo.DetailedState.IDLE;
        supplicantSSID = "";
        lastSupplicantSSID = "";

        settings = PreferenceManager.getDefaultSharedPreferences(context);
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        editor = preferences.edit();

        try {
            getDataState();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void addNetworkData(String description, String ssid, String bssid,
                               String security, String password, double latitude, double longitude) {
        if (!settings.getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
            password = "";
        }
        NetworkData data = new NetworkData(description, getDataSSID(ssid), bssid,
                security, password, latitude, longitude);
        networksData.add(data);
        saveDataState();
    }

    public boolean removeNetworkData(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                networksData.remove(iterator.nextIndex());
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;

    }

    public void collectGarbage(List<WifiConfiguration> wifiConfiguredNetworks) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        String ssid;
        String listOfNetworks = "";

        for (int i = 0; i < wifiConfiguredNetworks.size(); i++) {
            ssid = wifiConfiguredNetworks.get(i).SSID;
            listOfNetworks = listOfNetworks.concat(ssid + " ");
        }

        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            ssid = data.getSSID();
            if (!listOfNetworks.contains(ssid)) {
                networksData.remove(iterator.nextIndex());
                saveDataState();
            }
            if (iterator.hasNext()) {
                iterator.next();
            }
        }

    }


    // GETTERS

    public List<NetworkData> getConfiguredNetworksData() {
        return networksData;
    }

    public String getDataSSID(String cfgSSID) {
        if (!cfgSSID.isEmpty()) {
            return cfgSSID.replace("\"", "");
        } else {
            return "";
        }
    }

    public String getCfgSSID(String ssid) {
        String cfgSSID = "\"" + ssid + "\"";
        return cfgSSID;
    }

    public boolean hasNetworkAdditionalData(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return true;
            }
            iterator.next();
        }
        return false;
    }

    public String getMacAddressBySSID(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getMacAddress();
            }
            iterator.next();
        }
        return "";
    }

    public String getNetworkSecurityBySSID(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getSecurity();
            }
            iterator.next();
        }
        return "";
    }

    public double getLatitudeBySSID(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getLatitude();
            }
            iterator.next();
        }
        return Constants.DEFAULT_LATITUDE;

    }

    public double getLongitudeBySSID(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getLongitude();
            }
            iterator.next();
        }
        return Constants.DEFAULT_LONGITUDE;

    }

    public String getDescriptionBySSID(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getDescription();
            }
            iterator.next();
        }
        return "";
    }

    public WifiConfiguration getConfigurationToAdd(
            List<WifiConfiguration> configuredNetworks, String mac, String ssid) throws NullPointerException {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (mac.matches(data.getMacAddress())) {
                ListIterator<WifiConfiguration> cfgIterator = configuredNetworks.listIterator();
                WifiConfiguration configuration;
                while (cfgIterator.hasNext()) {
                    configuration = configuredNetworks.get(cfgIterator.nextIndex());
                    if (data.getSSID().matches(getDataSSID(configuration.SSID))) {
                        WifiConfiguration updatedConfiguration = configuration;
                        updatedConfiguration.SSID = getCfgSSID(getDataSSID(ssid));
                        updatedConfiguration.status = WifiConfiguration.Status.DISABLED;
                        updatedConfiguration.preSharedKey = null;
                        updatedConfiguration.wepKeys[0] = null;
                        data.setSSID(getDataSSID(ssid));
                        saveDataState();
                        return updatedConfiguration;
                    }
                    cfgIterator.next();
                }
            }
            iterator.next();
        }
        return null;

    }

    public String getPassword(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getPassword();
            }
            iterator.next();
        }
        return "";

    }

    public int getScannedNetworkLevel(List<ScanResult> wifiScannedNetworks, String ssid)
            throws NullPointerException {

        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult network = wifiScannedNetworks.get(index);
            String scanSSID = getDataSSID(network.SSID);
            if (getDataSSID(ssid).matches(scanSSID)) {
                return network.level;
            }
            listIterator.next();
        }
        return 0;
    }

    public int getNetworkSecurity(String security) {

        if (security.contains(Constants.SCAN_WPA)) {
            return Constants.SET_WPA;
        } else if (security.contains(Constants.SCAN_EAP)) {
            return Constants.SET_EAP;
        } else if (security.contains(Constants.SCAN_WEP)) {
            return Constants.SET_WEP;
        } else return Constants.SET_OPEN;

    }


    // SETTERS

    public boolean setLocationBySSID(String ssid, double latitude, double longitude) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setLatitude(latitude);
                data.setLongitude(longitude);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;
    }

    public boolean setLocationByMacAddress(String bssid, double latitude, double longitude) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (bssid.matches(data.getMacAddress())) {
                data.setLatitude(latitude);
                data.setLongitude(longitude);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;
    }

    public boolean setMacAddressBySSID(String ssid, String mac) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setMacAddress(mac);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;

    }

    public boolean setDescriptionBySSID(String ssid, String description) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDescriptionBySSID(ssid).matches(data.getSSID())) {
                data.setDescription(description);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;

    }

    public boolean setPassword(String ssid, String password) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setPassword(password);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;

    }

    public WifiConfiguration setNetworkSecurity(WifiConfiguration configuration, int security, String password) {

        switch (security) {

            case Constants.SET_OPEN:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case Constants.SET_WEP:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.wepTxKeyIndex = 0;
                configuration.wepKeys[0] = password;
                break;

            case Constants.SET_WPA:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                configuration.preSharedKey = password;
                break;

            case Constants.SET_EAP:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                configuration.preSharedKey = password;
                break;

        }

        return configuration;

    }

    public WifiConfiguration setNetworkCiphers(WifiConfiguration configuration, String security) {

        if (security.contains(Constants.CFG_CCMP)) {
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        }
        if (security.contains(Constants.CFG_TKIP)) {
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        }
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        return configuration;

    }


    // UPDATERS

    public boolean updateNetworkDescription(String ssid, String description) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setDescription(description);
                saveDataState();
                return true;
            }
            iterator.next();
        }
        return false;

    }

    public WifiConfiguration updateSSIDbyMacAddress(
            List<WifiConfiguration> configuredNetworks, String mac, String ssid) throws NullPointerException {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;

        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (mac.matches(data.getMacAddress())) {
                ListIterator<WifiConfiguration> cfgIterator = configuredNetworks.listIterator();
                WifiConfiguration configuration;
                while (cfgIterator.hasNext()) {
                    configuration = configuredNetworks.get(cfgIterator.nextIndex());
                    if (data.getSSID().matches(getDataSSID(configuration.SSID))) {
                        WifiConfiguration updatedConfiguration = configuration;
                        updatedConfiguration.SSID = getCfgSSID(getDataSSID(ssid));
                        data.setSSID(getDataSSID(ssid));
                        saveDataState();
                        return updatedConfiguration;
                    }
                    cfgIterator.next();
                }
            }
            iterator.next();
        }
        return null;

    }

    public void saveDataState() {
        try {
            setDataState(networksData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // CHECKERS

    public boolean isConfiguredBySSID(
            List<WifiConfiguration> configuredNetworks, String ssid) throws NullPointerException {

        ListIterator<WifiConfiguration> listIterator = configuredNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            WifiConfiguration network = configuredNetworks.get(index);
            String ssidConfig = getDataSSID(network.SSID);
            if (ssidConfig.matches(getDataSSID(ssid))) {
                return true;
            }
            listIterator.next();
        }
        return false;

    }

    public boolean isConfiguredByMacAddress(String mac) {

        ListIterator<NetworkData> listIterator = networksData.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            String macConfig = networksData.get(index).getMacAddress();
            if (macConfig.matches(mac)) {
                return true;
            }
            listIterator.next();
        }
        return false;

    }

    public boolean isConnected(List<WifiConfiguration> configuredNetworks, String ssid) throws NullPointerException {

        ListIterator<WifiConfiguration> listIterator = configuredNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            WifiConfiguration network = configuredNetworks.get(index);
            String ssidConfig = getDataSSID(network.SSID);
            if ((ssidConfig.matches(getDataSSID(ssid))) && (network.status == WifiConfiguration.Status.CURRENT)) {
                return true;
            }
            listIterator.next();
        }
        return false;
    }


    public boolean isAvailable(
            List<ScanResult> wifiScannedNetworks, String ssid, String mac) throws NullPointerException {

        if (isAvailableBySSID(wifiScannedNetworks, ssid) || isAvailableByMacAddress(wifiScannedNetworks, mac)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAvailableBySSID(
            List<ScanResult> wifiScannedNetworks, String ssid) throws NullPointerException {

        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult network = wifiScannedNetworks.get(index);
            String scanSSID = getDataSSID(network.SSID);
            if (getDataSSID(ssid).matches(scanSSID)) {
                return true;
            }
            listIterator.next();
        }
        return false;
    }

    public boolean isAvailableByMacAddress(
            List<ScanResult> wifiScannedNetworks, String mac) throws NullPointerException {

        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult network = wifiScannedNetworks.get(index);
            String scanMac = network.BSSID;
            if (mac.matches(scanMac)) {
                return true;
            }
            listIterator.next();
        }
        return false;
    }


    // JSON

    public void setDataState(ArrayList<NetworkData> data) throws IOException {
        String jsonData = writeJsonString(data);
        editor.putString(Constants.DATA, jsonData);
        editor.commit();
    }

    // get bookmarks list from memory through a json string
    // if list was not saved yet creates a new array list
    public boolean getDataState() throws IOException {
        String jsonData = preferences.getString(Constants.DATA, null);
        if (jsonData != null) {
            networksData = readJsonString(jsonData);
            return true;
        } else {
            return false;
        }
    }

    // create a json string of a list of location items
    private String writeJsonString(ArrayList<NetworkData> dataItems) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");
        writeDataArrayList(jsonWriter, dataItems);
        jsonWriter.close();
        return stringWriter.toString();
    }

    // write all locations to json string
    private void writeDataArrayList(JsonWriter writer, ArrayList<NetworkData> dataItems) throws IOException {
        writer.beginArray();
        for (NetworkData dataItem : dataItems) {
            writeDataItem(writer, dataItem);
        }
        writer.endArray();
    }

    // write a single location to json string
    private void writeDataItem(JsonWriter writer, NetworkData dataItem) throws IOException {
        writer.beginObject();
        writer.name(Constants.JSON_DESCRIPTION).value(dataItem.getDescription());
        writer.name(Constants.JSON_SSID).value(dataItem.getSSID());
        writer.name(Constants.JSON_BSSID).value(dataItem.getMacAddress());
        writer.name(Constants.JSON_SECURITY).value(dataItem.getSecurity());
        writer.name(Constants.JSON_PASSWORD).value(dataItem.getPassword());
        writer.name(Constants.JSON_LATITUDE).value(dataItem.getLatitude());
        writer.name(Constants.JSON_LONGITUDE).value(dataItem.getLongitude());
        writer.endObject();
    }

    // read a json string containing a list of location items
    private ArrayList<NetworkData> readJsonString(String jsonString) throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        try {
            return readDataArrayList(jsonReader);
        } finally {
            jsonReader.close();
        }
    }

    // read a list of location items from a json string
    private ArrayList<NetworkData> readDataArrayList(JsonReader jsonReader) throws IOException {
        ArrayList<NetworkData> dataItems = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            dataItems.add(readDataItem(jsonReader));
        }
        jsonReader.endArray();
        return dataItems;
    }

    // read a single location item from a json string
    private NetworkData readDataItem(JsonReader jsonReader) throws IOException {
        String dataDescription = "";
        String dataSSID = "";
        String dataBSSID = "";
        String dataSecurity = "";
        String dataPassword = "";
        Double dataLatitude = Constants.DEFAULT_LATITUDE;
        Double dataLongitude = Constants.DEFAULT_LONGITUDE;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case Constants.JSON_DESCRIPTION:
                    dataDescription = jsonReader.nextString();
                    break;
                case Constants.JSON_SSID:
                    dataSSID = jsonReader.nextString();
                    break;
                case Constants.JSON_BSSID:
                    dataBSSID = jsonReader.nextString();
                    break;
                case Constants.JSON_SECURITY:
                    dataSecurity = jsonReader.nextString();
                    break;
                case Constants.JSON_PASSWORD:
                    dataPassword = jsonReader.nextString();
                    break;
                case Constants.JSON_LATITUDE:
                    dataLatitude = jsonReader.nextDouble();
                    break;
                case Constants.JSON_LONGITUDE:
                    dataLongitude = jsonReader.nextDouble();
                    break;
                default:
                    jsonReader.skipValue();
            }

        }
        jsonReader.endObject();
        NetworkData dataItem = new NetworkData(dataDescription, dataSSID, dataBSSID,
                dataSecurity, dataPassword, dataLatitude, dataLongitude);
        return dataItem;
    }

}