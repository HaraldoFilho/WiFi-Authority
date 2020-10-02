/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ConfiguredNetworks.java
 *  Last modified : 10/1/20 9:31 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.networks;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.preference.PreferenceManager;

import com.apps.mohb.wifiauthority.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;


public class ConfiguredNetworks {

    private static ArrayList<NetworkData> networksData;
    public static SupplicantState supplicantNetworkState;
    public static SupplicantState lastSupplicantNetworkState;
    public static String supplicantSSID;
    public static String lastSupplicantSSID;

    private SharedPreferences preferences;
    private SharedPreferences settings;


    public ConfiguredNetworks(Context context) {
        if (networksData == null) {
            networksData = new ArrayList<>();
        }

        supplicantNetworkState = SupplicantState.DISCONNECTED;
        lastSupplicantNetworkState = SupplicantState.DISCONNECTED;
        supplicantSSID = Constants.EMPTY;
        lastSupplicantSSID = Constants.EMPTY;

        settings = PreferenceManager.getDefaultSharedPreferences(context);
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

        try {
            getDataState();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void addNetworkData(String description, String ssid, boolean hidden, String bssid,
                               String security, String password, double latitude, double longitude) {
        if (!settings.getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
            password = Constants.EMPTY;
        }
        NetworkData data = new NetworkData(description, getDataSSID(ssid), hidden, bssid,
                security, password, latitude, longitude);
        networksData.add(data);
        saveDataState();
    }

    public void removeNetworkData(String ssid) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                networksData.remove(iterator.nextIndex());
                saveDataState();
                return;
            }
            iterator.next();
        }
    }

    public void collectGarbage(List<WifiConfiguration> wifiConfiguredNetworks)
            throws ConcurrentModificationException, NullPointerException {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        String ssid;
        String listOfNetworks = Constants.EMPTY;

        for (int i = Constants.LIST_HEAD; i < wifiConfiguredNetworks.size(); i++) {
            ssid = wifiConfiguredNetworks.get(i).SSID;
            listOfNetworks = listOfNetworks.concat(ssid + Constants.SPACE);
        }

        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            ssid = data.getSSID();
            if (!listOfNetworks.contains(ssid)) {
                networksData.remove(iterator.nextIndex());
                saveDataState();
                collectGarbage(wifiConfiguredNetworks);
            } else {
                iterator.next();
            }
        }

    }

    public void restoreRemovedNetworks(Context context, List<WifiConfiguration> wifiConfiguredNetworks)
            throws ConcurrentModificationException, NullPointerException {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        String ssid;
        String listOfNetworks = Constants.EMPTY;

        for (int i = Constants.LIST_HEAD; i < wifiConfiguredNetworks.size(); i++) {
            ssid = wifiConfiguredNetworks.get(i).SSID;
            listOfNetworks = listOfNetworks.concat(ssid + Constants.SPACE);
        }

        while (iterator.hasNext()) {

            data = networksData.get(iterator.nextIndex());
            ssid = data.getSSID();

            if (!listOfNetworks.contains(ssid)) {

                int security = getNetworkSecurity(data.getSecurity());
                String password = data.getPassword();

                // If no password is stored and network is not open set a dummy password
                if (password.isEmpty() && security != Constants.SET_OPEN) {
                    if (security == Constants.SET_WEP) {
                        password = Constants.WEP_DUMMY_PASSWORD;
                    } else {
                        password = Constants.WPA_DUMMY_PASSWORD;
                    }
                }

                addNetworkConfiguration(context, data.getSSID(), data.isHidden(), data.getSecurity(), password);

                // If store password settings option is disabled clear password
                if (!settings.getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
                    password = Constants.EMPTY;
                }

                data.setPassword(password);
                saveDataState();

            }

            iterator.next();
        }

    }

    public void clearAllPasswords() {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        while (iterator.hasNext()) {
            NetworkData data = networksData.get(iterator.nextIndex());
            data.setPassword(Constants.EMPTY);
            iterator.next();
        }
        saveDataState();
    }

    public void addNetworkConfiguration(Context context, String ssid, boolean isHidden, String security, String password) {

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration = setNetworkCiphers(wifiConfiguration, security);

        if (isHidden) {
            wifiConfiguration.hiddenSSID = true;
        }
        wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
        wifiConfiguration.SSID = getCfgSSID(ssid);
        wifiConfiguration.priority = Constants.CFG_PRIORITY;
        wifiConfiguration = setNetworkSecurity(wifiConfiguration, getNetworkSecurity(security), getCfgPassword(password));

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int netId = wifiManager.addNetwork(wifiConfiguration);

        wifiManager.disableNetwork(netId);

    }


    // GETTERS

    public List<NetworkData> getConfiguredNetworksData() {
        return networksData;
    }

    public String getDataSSID(String ssid) {
        return ssid.replace(Constants.QUOTE, Constants.EMPTY);
    }

    public String getCfgSSID(String ssid) {
        return Constants.QUOTE + ssid + Constants.QUOTE;
    }

    public String getCfgPassword(String password) {
        return Constants.QUOTE + password + Constants.QUOTE;
    }

    public String getMacAddressBySSID(String ssid) throws NullPointerException {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getMacAddress();
            }
            iterator.next();
        }
        return Constants.EMPTY;
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
        return Constants.EMPTY;
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
        return Constants.EMPTY;

    }

    public int getFrequency(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getFrequency();
            }
            iterator.next();
        }
        return Constants.NO_FREQ_SET;

    }

    public String getSecurity(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.getSecurity();
            }
            iterator.next();
        }
        return Constants.EMPTY;

    }

    public int getScannedNetworkLevel(List<ScanResult> wifiScannedNetworks, String mac)
            throws NullPointerException {

        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult network = wifiScannedNetworks.get(index);
            String bssid = network.BSSID;
            if (mac.matches(bssid)) {
                return network.level;
            }
            listIterator.next();
        }
        return Constants.LEVEL_VERY_LOW;
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

    public String getCapabilities(WifiConfiguration configuration) {

        String capabilities = Constants.EMPTY;

        switch (configuration.allowedKeyManagement.toString()) {

            case Constants.KEY_NONE_WEP:
                if (configuration.allowedAuthAlgorithms.toString().contains(Constants.ALLOW_1)) {
                    capabilities = capabilities.concat(Constants.CAP_WEP);
                }
                break;

            case Constants.KEY_WPA:
                capabilities = capabilities.concat(Constants.CAP_WPA);
                break;

            case Constants.KEY_WPA2:
                capabilities = capabilities.concat(Constants.CAP_WPA2);
                break;

            case Constants.KEY_EAP:
                capabilities = capabilities.concat(Constants.CAP_EAP);
                break;

        }

        if (configuration.allowedPairwiseCiphers.toString().contains(Constants.ALLOW_1)) {
            capabilities = capabilities.concat(Constants.CAP_TKTIP);
        }
        if (configuration.allowedPairwiseCiphers.toString().contains(Constants.ALLOW_2)) {
            capabilities = capabilities.concat(Constants.CAP_CCMP);
        }

        return capabilities;

    }


    // SETTERS

    public void setLocationBySSID(String ssid, double latitude, double longitude) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setLatitude(latitude);
                data.setLongitude(longitude);
                saveDataState();
                return;
            }
            iterator.next();
        }
    }

    public void setLocationByMacAddress(String bssid, double latitude, double longitude) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (bssid.matches(data.getMacAddress())) {
                data.setLatitude(latitude);
                data.setLongitude(longitude);
                saveDataState();
                return;
            }
            iterator.next();
        }
    }

    public void setMacAddressBySSID(String ssid, String mac) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setMacAddress(mac);
                saveDataState();
                return;
            }
            iterator.next();
        }

    }

    public void setDescriptionBySSID(String ssid, String description) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDescriptionBySSID(ssid).matches(data.getSSID())) {
                data.setDescription(description);
                saveDataState();
                return;
            }
            iterator.next();
        }

    }

    public void setHidden(String ssid, boolean hidden) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setHidden(hidden);
                saveDataState();
                return;
            }
            iterator.next();
        }

    }

    public void setFrequency(String ssid, int frequency) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setFrequency(frequency);
                saveDataState();
                return;
            }
            iterator.next();
        }

    }

    public void setPassword(String ssid, String password) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                if (!settings.getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
                    password = Constants.EMPTY;
                }
                data.setPassword(password);
                saveDataState();
                return;
            }
            iterator.next();
        }

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
                configuration.wepTxKeyIndex = Constants.WEP_PASSWORD_KEY_INDEX;
                configuration.wepKeys[Constants.WEP_PASSWORD_KEY_INDEX] = password;
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

    public void updateNetworkDescription(String ssid, String description) {
        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                data.setDescription(description);
                saveDataState();
                return;
            }
            iterator.next();
        }

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
                        configuration.SSID = getCfgSSID(getDataSSID(ssid));
                        data.setSSID(getDataSSID(ssid));
                        saveDataState();
                        return configuration;
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

    public boolean isSSIDConnected(List<WifiConfiguration> configuredNetworks, String ssid) throws NullPointerException {

        ListIterator<WifiConfiguration> listIterator = configuredNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            WifiConfiguration network = configuredNetworks.get(index);
            String ssidCfg = getDataSSID(network.SSID);
            if ((ssidCfg.matches(ssid)) && (network.status == WifiConfiguration.Status.CURRENT)) {
                return true;
            }
            listIterator.next();
        }
        return false;
    }

    public boolean isMacAddressConnected(List<WifiConfiguration> configuredNetworks, String mac) throws NullPointerException {

        ListIterator<WifiConfiguration> listIterator = configuredNetworks.listIterator();

        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            WifiConfiguration network = configuredNetworks.get(index);
            String macConfig = getMacAddressBySSID(getDataSSID(network.SSID));
            if ((macConfig.matches(mac)) && (network.status == WifiConfiguration.Status.CURRENT)) {
                return true;
            }
            listIterator.next();
        }
        return false;
    }


    public boolean isAvailable(
            List<ScanResult> wifiScannedNetworks, String ssid, String mac) throws NullPointerException {

        return isAvailableBySSID(wifiScannedNetworks, ssid) || isAvailableByMacAddress(wifiScannedNetworks, mac);
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

    public boolean hasNetworksData() {

        return !networksData.isEmpty();

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

    public boolean isHidden(String ssid) {

        ListIterator<NetworkData> iterator = networksData.listIterator();
        NetworkData data;
        while (iterator.hasNext()) {
            data = networksData.get(iterator.nextIndex());
            if (getDataSSID(ssid).matches(data.getSSID())) {
                return data.isHidden();
            }
            iterator.next();
        }
        return false;

    }

    public boolean isValidPassword(int securityOption, String password) {

        return (((securityOption == Constants.SET_WPA) || (securityOption == Constants.SET_EAP))
                && ((password.length() >= Constants.WPA_PASSWORD_MIN_LENGTH)
                && (password.length() <= Constants.WPA_PASSWORD_MAX_LENGTH)))
                || ((securityOption == Constants.SET_WEP)
                && ((password.length() == Constants.WEP_PASSWORD_64BIT_LENGTH)
                || (password.length() == Constants.WEP_PASSWORD_128BIT_LENGTH)))
                || (securityOption == Constants.SET_OPEN);

    }


    // JSON

    public void setDataState(ArrayList<NetworkData> data) throws IOException, IllegalStateException {
        String jsonData = writeJsonString(data);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.DATA, jsonData);
        editor.apply();
    }

    // get a network data list from memory through a json string
    // if list was not saved yet creates a new array list
    public void getDataState() throws IOException, IllegalStateException {
        String jsonData = preferences.getString(Constants.DATA, null);
        if (jsonData != null) {
            networksData = readJsonString(jsonData);
        }
    }

    // create a json string of a list of network data items
    private String writeJsonString(ArrayList<NetworkData> dataItems) throws IOException, IllegalStateException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent(Constants.SPACE);
        writeDataArrayList(jsonWriter, dataItems);
        jsonWriter.close();
        return stringWriter.toString();
    }

    // write all network data to json string
    private void writeDataArrayList(JsonWriter writer, ArrayList<NetworkData> dataItems)
            throws IOException, IllegalStateException {
        writer.beginArray();
        for (NetworkData dataItem : dataItems) {
            writeDataItem(writer, dataItem);
        }
        writer.endArray();
    }

    // write a single network data to json string
    private void writeDataItem(JsonWriter writer, NetworkData dataItem) throws IOException, IllegalStateException {
        writer.beginObject();
        writer.name(Constants.JSON_DESCRIPTION).value(dataItem.getDescription());
        writer.name(Constants.JSON_SSID).value(dataItem.getSSID());
        writer.name(Constants.JSON_HIDDEN).value(dataItem.isHidden());
        writer.name(Constants.JSON_BSSID).value(dataItem.getMacAddress());
        writer.name(Constants.JSON_SECURITY).value(dataItem.getSecurity());
        writer.name(Constants.JSON_FREQUENCY).value(dataItem.getFrequency());
        writer.name(Constants.JSON_PASSWORD).value(dataItem.getPassword());
        writer.name(Constants.JSON_LATITUDE).value(dataItem.getLatitude());
        writer.name(Constants.JSON_LONGITUDE).value(dataItem.getLongitude());
        writer.endObject();
    }

    // read a json string containing a list of network items
    private ArrayList<NetworkData> readJsonString(String jsonString) throws IOException, IllegalStateException {
        try (JsonReader jsonReader = new JsonReader(new StringReader(jsonString))) {
            return readDataArrayList(jsonReader);
        }
    }

    // read a list of network data items from a json string
    private ArrayList<NetworkData> readDataArrayList(JsonReader jsonReader) throws IOException, IllegalStateException {
        ArrayList<NetworkData> dataItems = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            dataItems.add(readDataItem(jsonReader));
        }
        jsonReader.endArray();
        return dataItems;
    }

    // read a single network data item from a json string
    private NetworkData readDataItem(JsonReader jsonReader) throws IOException, IllegalStateException {
        String dataDescription = Constants.EMPTY;
        String dataSSID = Constants.EMPTY;
        boolean dataHidden = false;
        String dataBSSID = Constants.EMPTY;
        String dataSecurity = Constants.EMPTY;
        int dataFrequency = Constants.NO_FREQ_SET;
        String dataPassword = Constants.EMPTY;
        double dataLatitude = Constants.DEFAULT_LATITUDE;
        double dataLongitude = Constants.DEFAULT_LONGITUDE;

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
                case Constants.JSON_HIDDEN:
                    dataHidden = jsonReader.nextBoolean();
                    break;
                case Constants.JSON_BSSID:
                    dataBSSID = jsonReader.nextString();
                    break;
                case Constants.JSON_SECURITY:
                    dataSecurity = jsonReader.nextString();
                    break;
                case Constants.JSON_FREQUENCY:
                    dataFrequency = jsonReader.nextInt();
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
        NetworkData dataItem = new NetworkData(dataDescription, dataSSID, dataHidden, dataBSSID,
                dataSecurity, dataPassword, dataLatitude, dataLongitude);
        dataItem.setFrequency(dataFrequency);
        return dataItem;
    }

}