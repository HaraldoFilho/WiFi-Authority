/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MainActivity.java
 *  Last modified : 10/1/20 9:49 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.apps.mohb.wifiauthority.adapters.ConfiguredNetworksListAdapter;
import com.apps.mohb.wifiauthority.fragments.dialogs.AddNetworkDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.DescriptionEditDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.LocationPermissionsAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkDeleteAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkManagementPolicyAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkNameChangedDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.PasswordChangeDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.RestoreNetworksAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.WifiDisabledAlertFragment;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;
import com.apps.mohb.wifiauthority.networks.NetworkData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        DescriptionEditDialogFragment.DescriptionEditDialogListener,
        AddNetworkDialogFragment.AddNetworkDialogListener,
        NetworkDeleteAlertFragment.NetworkDeleteDialogListener,
        WifiDisabledAlertFragment.WifiDisabledDialogListener,
        NetworkNameChangedDialogFragment.NetworkNameChangedDialogListener,
        PasswordChangeDialogFragment.PasswordChangeDialogListener,
        LocationPermissionsAlertFragment.LocationPermissionsDialogListener,
        NetworkManagementPolicyAlertFragment.NetworkManagementPolicyDialogListener,
        RestoreNetworksAlertFragment.RestoreNetworksListener {

    private WifiManager wifiManager;
    private List<ScanResult> wifiScannedNetworks;
    private List<WifiConfiguration> wifiConfiguredNetworks;
    private ListView networksListView;
    private FloatingActionButton fab;
    private DialogFragment wifiDisabledDialog;

    private ConfiguredNetworksListAdapter networksListAdapter;

    private ConfiguredNetworks configuredNetworks;
    protected WifiConfiguration network;

    private FusedLocationProviderClient fusedLocationClient;
    private double lastLatitude;
    private double lastLongitude;

    private SharedPreferences showNetworkManagementPolicyWarnPref;
    private boolean networkNameChangedDialogOpened;
    private String ssid;

    private SharedPreferences settings;


    /*
         Inner class to monitor network state changes
    */
    public class NetworkStateMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if ((!wifiManager.isWifiEnabled()) && (wifiDisabledDialog == null)) {
                try {
                    showWifiDisabledAlertDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (wifiManager.isWifiEnabled()) {
                updateListOfNetworks();
            }
        }

    }


    /*
         Inner class to receive WiFi scan results
    */
    private class WiFiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            wifiScannedNetworks = wifiManager.getScanResults();

            // Get saved state of networks additional data
            try {
                configuredNetworks.getDataState();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // Iterates over the list of the scanned networks to save the mac addresses
                // and location of configured networks that are on reach
                ListIterator<ScanResult> scanListIterator = wifiScannedNetworks.listIterator();

                while (scanListIterator.hasNext()) {

                    ScanResult scanResult = wifiScannedNetworks.get(scanListIterator.nextIndex());

                    // Check if network is configured using its ssid
                    if (configuredNetworks.isConfiguredBySSID(wifiConfiguredNetworks, scanResult.SSID)) {
                        // Check if the network is not configured using its mac address
                        if (!configuredNetworks.isConfiguredByMacAddress(scanResult.BSSID)) {
                            // Check if network already has additional data
                            if (configuredNetworks.hasNetworkAdditionalData(scanResult.SSID)) {
                                // Set its mac address using its ssid
                                configuredNetworks.setMacAddressBySSID(scanResult.SSID, scanResult.BSSID);
                                // Check if user's last known location has been acquired
                                if (isLastLocationKnown()) {
                                    // Save location as additional data
                                    // Note: while the network is on reach the location will be constantly updated,
                                    // but when the network turns out of reach the additional data will store the last
                                    // saved known location
                                    configuredNetworks.setLocationBySSID(scanResult.SSID, lastLatitude, lastLongitude);
                                }

                            } else { // If network doesn't have additional data
                                // Check if user's last known location has been acquired
                                if (isLastLocationKnown()) {
                                    // Create additional data for the network with the scanned SSID, Mac Address and the current location
                                    configuredNetworks.addNetworkData(Constants.EMPTY, scanResult.SSID, false, scanResult.BSSID,
                                            scanResult.capabilities, Constants.EMPTY, lastLatitude, lastLongitude);
                                } else { // If location has not been acquired create additional data for the network
                                    // with the scanned SSID, Mac Address and the default location (0,0)
                                    configuredNetworks.addNetworkData(Constants.EMPTY, scanResult.SSID, false, scanResult.BSSID,
                                            scanResult.capabilities, Constants.EMPTY, Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                                }
                            }

                        } else { // If network is already configured by Mac Address
                            // Check if user's last known location has been acquired
                            if (isLastLocationKnown()) {
                                // Save location using its mac address
                                configuredNetworks.setLocationByMacAddress(scanResult.BSSID,
                                        lastLatitude, lastLongitude);
                            }

                        }
                    }

                    scanListIterator.next();

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            // Get saved state of networks additional data
            // which could be changed on last iteration
            try {
                configuredNetworks.getDataState();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // Iterates over the list of configured networks...
                ListIterator<WifiConfiguration> wifiConfigurationListIterator
                        = wifiConfiguredNetworks.listIterator();

                while (wifiConfigurationListIterator.hasNext()) {

                    WifiConfiguration wifiConfiguration = wifiConfiguredNetworks
                            .get(wifiConfigurationListIterator.nextIndex());

                    ssid = wifiConfiguration.SSID;

                    // ... and on each configured network, iterates over the list of scanned networks results
                    ListIterator<ScanResult> scanResultListIterator = wifiScannedNetworks.listIterator();

                    while (scanResultListIterator.hasNext()) {

                        ScanResult scanResult = wifiScannedNetworks.get(scanResultListIterator.nextIndex());

                        // Check if ssid reconfiguration setting is on ...
                        if (settings.getBoolean(Constants.PREF_KEY_RECONFIG, true)
                                // ... and if Mac Address of configured network matches Mac Address of scanned network
                                && (configuredNetworks.getMacAddressBySSID(ssid).matches(scanResult.BSSID))) {

                            if (scanResult.SSID.isEmpty()) {
                                configuredNetworks.setHidden(ssid, true);

                            } else if (wifiConfiguration.status == WifiConfiguration.Status.DISABLED) {
                                configuredNetworks.setHidden(ssid, false);
                            }


                            // If network is not hidden ...
                            if (!configuredNetworks.isHidden(ssid)
                                    && !scanResult.SSID.isEmpty()
                                    // ... and SSID of the network has changed ...
                                    && !ssid.matches(configuredNetworks.getCfgSSID(scanResult.SSID))
                                    // ... and network is not connected ...
                                    && wifiConfiguration.status != WifiConfiguration.Status.CURRENT) {

                                // ... updates the SSID of the configured network
                                WifiConfiguration configuration = configuredNetworks
                                        .updateSSIDbyMacAddress(wifiConfiguredNetworks, scanResult.BSSID, scanResult.SSID);

                                // Update network with new configuration
                                int updateResult = wifiManager.updateNetwork(configuration);

                                // Check if update has failed and the name changed dialog is not opened
                                if ((updateResult == Constants.NETWORK_UPDATE_FAIL) && (!networkNameChangedDialogOpened)) {

                                    /*
                                     Opens name changed dialog with the following data of the changed network:
                                     - Description
                                     - Old name (SSID)
                                     - New name (SSID)
                                     - Security
                                     */
                                    DialogFragment networkNameChangedDialog = new NetworkNameChangedDialogFragment();

                                    Bundle bundle = new Bundle();
                                    bundle.putString(Constants.KEY_DESCRIPTION,
                                            configuredNetworks.getDescriptionBySSID(ssid));
                                    bundle.putString(Constants.KEY_OLD_NAME, ssid);
                                    bundle.putString(Constants.KEY_NEW_NAME, scanResult.SSID);
                                    bundle.putBoolean(Constants.KEY_HIDDEN, false);
                                    bundle.putString(Constants.KEY_SECURITY, scanResult.capabilities);
                                    bundle.putString(Constants.KEY_BSSID, scanResult.BSSID); // send also the Mac Address

                                    networkNameChangedDialog.setArguments(bundle);
                                    networkNameChangedDialog.show(getSupportFragmentManager(),
                                            "NetworkNameChangedDialogFragment");
                                    // Register that dialog is opened
                                    networkNameChangedDialogOpened = true;

                                }


                            } else {

                                // Updates hidden status of the configured network
                                WifiConfiguration configuration = configuredNetworks
                                        .updateSSIDbyMacAddress(wifiConfiguredNetworks, scanResult.BSSID, ssid);

                                configuration.hiddenSSID = true;

                                wifiManager.updateNetwork(configuration);

                            }

                        } else {
                            // If mac addresses doesn't match but SSIDs do, update mac address
                            // of the configured network (probably router was substituted)
                            if (ssid.matches(configuredNetworks.getCfgSSID(scanResult.SSID))) {
                                configuredNetworks.setMacAddressBySSID(scanResult.SSID, scanResult.BSSID);
                            }
                        }

                        scanResultListIterator.next();

                    }

                    wifiConfigurationListIterator.next();

                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            // If WiFi is enabled, refresh list of networks
            if (wifiManager.isWifiEnabled()) {
                updateListOfNetworks();
            }

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Create list header and footer, that will insert spaces on top and bottom of the
        // list to make material design effect elevation and shadow
        View listHeader = getLayoutInflater().inflate(R.layout.list_header, networksListView);
        View listFooter = getLayoutInflater().inflate(R.layout.list_footer, networksListView);

        networksListView = findViewById(R.id.networksList);

        // Insert header and footer if version is Lollipop (5.x) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networksListView.addHeaderView(listHeader);
            networksListView.addFooterView(listFooter);
            listHeader.setClickable(false);
            listFooter.setClickable(false);
        }

        registerForContextMenu(networksListView);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            wifiConfiguredNetworks = wifiManager.getConfiguredNetworks();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        configuredNetworks = new ConfiguredNetworks(this);

        // Create floating action button and handle clicks on it to add a network
        fab = (FloatingActionButton) findViewById(R.id.fab);
        // Make floating button translucent
        fab.setAlpha(Constants.FAB_TRANSLUCENT);
        // Monitor clicks on button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                startScanNetworksActivity();
            }
        });

        // Handle clicks on networks list items
        networksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                // Set default values for these variables
                String ssid;
                double latitude;
                double longitude;

                // Get the correct position of clicked item according to Android version
                // due to use of header and footer
                int correctPosition = getCorrectPosition(position);

                // Check if position is inside networks list size (not header or footer)
                if ((correctPosition >= Constants.LIST_HEAD) && (correctPosition < wifiConfiguredNetworks.size())) {

                    ssid = wifiConfiguredNetworks.get(correctPosition).SSID;
                    latitude = configuredNetworks.getLatitudeBySSID(ssid);
                    longitude = configuredNetworks.getLongitudeBySSID(ssid);

                    String mac = configuredNetworks.getMacAddressBySSID(ssid);

                    if (!mac.isEmpty() && !mac.matches(Constants.INVALID_MAC)) {
                        // Create bundle with the information needed to show more detailed information
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.KEY_SSID, configuredNetworks.getDataSSID(ssid));
                        bundle.putString(Constants.KEY_BSSID, mac);
                        bundle.putDouble(Constants.KEY_LATITUDE, latitude);
                        bundle.putDouble(Constants.KEY_LONGITUDE, longitude);
                        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        Toasts.showNoDetailedInformation(getApplicationContext(), R.string.toast_no_details);
                    }

                }

            }

        });


        networksListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Required empty method
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // If list is scrolling up hide the floating action button
                if (firstVisibleItem > Constants.LIST_HEAD) {
                    fab.hide();
                } else {
                    fab.show(new FloatingActionButton.OnVisibilityChangedListener() {
                        @Override
                        public void onShown(FloatingActionButton fab) {
                            // Make floating button translucent
                            fab.setAlpha(Constants.FAB_TRANSLUCENT);
                        }
                    });
                }
            }
        });

        // Set and register a scan receiver to get available networks
        WiFiScanReceiver wifiScanReceiver = new WiFiScanReceiver();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Shared preferences variable to control if Network Management Policy Dialog for Marshmallow (version 6.x)
        // or higher must be shown
        showNetworkManagementPolicyWarnPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

        wifiDisabledDialog = null;

        // Register a broadcast receiver to monitor changes on network state to update network status
        BroadcastReceiver wifiStateMonitor = new NetworkStateMonitor();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        this.registerReceiver(wifiStateMonitor, filter);

        // Create an instance of GoogleAPIClient to load maps
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // If Settings was opened from ScanActivity reopen it
        if (settings.getBoolean(Constants.PREF_KEY_SCAN_ACTIVITY, false)) {
            settings.edit().putBoolean(Constants.PREF_KEY_SCAN_ACTIVITY, false).apply();
            Intent intent = new Intent(this, ScanNetworksActivity.class);
            startActivity(intent);
        }

        if ((!wifiManager.isWifiEnabled()) && (wifiDisabledDialog == null)) {
            showWifiDisabledAlertDialog();
        }

        try {
            // Get configured networks additional data
            if (configuredNetworks == null) {
                configuredNetworks = new ConfiguredNetworks(this);
            }
            configuredNetworks.getDataState();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Add network data of networks that were configured outside application
            ListIterator<WifiConfiguration> wifiConfigurationListIterator = wifiConfiguredNetworks.listIterator();
            while (wifiConfigurationListIterator.hasNext()) {
                WifiConfiguration configuration = wifiConfiguredNetworks.get(wifiConfigurationListIterator.nextIndex());
                if (!configuredNetworks.hasNetworkAdditionalData(configuration.SSID)) {
                    configuredNetworks.addNetworkData(Constants.EMPTY, configuration.SSID, configuration.hiddenSSID,
                            Constants.EMPTY, configuredNetworks.getCapabilities(configuration), Constants.EMPTY,
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                }
                wifiConfigurationListIterator.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If all networks were removed outside application...
        try {
            if ((configuredNetworks.hasNetworksData()) && (wifiManager.getConfiguredNetworks().isEmpty())) {
                // ...show dialog to restore them
                DialogFragment restoreNetworksDialog = new RestoreNetworksAlertFragment();
                restoreNetworksDialog.show(getSupportFragmentManager(), "RestoreNetworksDialogListener");
            } else {

                // If restore networks settings option is enabled...
                if (settings.getBoolean(Constants.PREF_KEY_RESTORE_NETWORKS, false)) {
                    // ...restore networks that were removed outside application
                    configuredNetworks.restoreRemovedNetworks(getApplicationContext(), wifiManager.getConfiguredNetworks());
                } else {
                    // Delete data from networks that were removed by Android system
                    configuredNetworks.collectGarbage(wifiManager.getConfiguredNetworks());
                }
            }
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (wifiManager.isWifiEnabled()) {
                updateListOfNetworks();
                wifiManager.startScan();
            }
        } else {
            // Check if user already denied permission request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show dialog informing that location permissions are required to app properly work
                DialogFragment dialog = new LocationPermissionsAlertFragment();
                dialog.show(getSupportFragmentManager(), "LocationPermissionsAlertFragment");
            } else {
                // Request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.FINE_LOCATION_PERMISSION_REQUEST);
            }
        }

        // Set that Network Name Changed Dialog was not already been opened
        networkNameChangedDialogOpened = false;

        // Get the last user's none location. Most of the times
        // this corresponds to user's current location or very near
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lastLatitude = location.getLatitude();
                        lastLongitude = location.getLongitude();
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        Intent intent;
        Bundle bundle;

        switch (id) {

            // Map
            case R.id.action_map:
                // If there are configured networks, show all networks locations on a map
                if (!wifiConfiguredNetworks.isEmpty()) {
                    intent = new Intent(this, MapActivity.class);
                    startActivity(intent);
                } else {
                    Toasts.showNoDetailedInformation(getApplicationContext(), R.string.toast_no_configured_networks);
                }
                break;

            // Settings
            case R.id.action_main_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            // Help
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                bundle = new Bundle();
                bundle.putString(Constants.KEY_URL, getString(R.string.url_help_main));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            // About
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_main, menu);

        MenuItem itemConnect = menu.findItem(R.id.connect);
        MenuItem itemPassword = menu.findItem(R.id.changePassword);

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) itemConnect.getMenuInfo();
        WifiConfiguration network = wifiConfiguredNetworks.get(getCorrectPosition(menuInfo.position));

        // Disable connect item clicks
        itemConnect.setEnabled(false);

        if (wifiManager.isWifiEnabled()) {
            // Set text according to network state
            switch (ConfiguredNetworks.supplicantNetworkState) {
                // Disconnected
                case DISCONNECTED:
                    itemConnect.setTitle(R.string.context_connect);
                    break;
                // Connected
                case COMPLETED:
                    itemConnect.setTitle(R.string.context_disconnect);
                    break;
                // Connecting...
                case AUTHENTICATING:
                    itemConnect.setTitle(R.string.context_cancel);
                    break;
            }
        }

        // Disable password item if network is open
        if ((network.wepKeys[Constants.WEP_PASSWORD_KEY_INDEX] == null) && (network.preSharedKey == null)) {
            itemPassword.setEnabled(false);
        }

        List<ScanResult> wifiScannedNetworks = wifiManager.getScanResults();

        // If network is available to connect or is hidden enable item click
        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();
        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult scanResult = wifiScannedNetworks.get(index);
            try {
                if ((configuredNetworks.getDataSSID(network.SSID).matches(scanResult.SSID))
                        || (configuredNetworks.getMacAddressBySSID(network.SSID).matches(scanResult.BSSID))) {
                    itemConnect.setEnabled(true);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            listIterator.next();
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        network = wifiConfiguredNetworks.get(getCorrectPosition(menuInfo.position));
        ssid = network.SSID;
        boolean isHidden = network.hiddenSSID;

        switch (item.getItemId()) {

            // Connect
            case R.id.connect:
                if (configuredNetworks.getPassword(ssid).matches(Constants.WPA_DUMMY_PASSWORD)
                        || configuredNetworks.getPassword(ssid).matches(Constants.WEP_DUMMY_PASSWORD)) {
                    showChangePasswordDialog(network, ssid, isHidden);
                } else {
                    connectToNetwork(wifiManager, network);
                }
                return true;

            // Edit Description
            case R.id.editDescription:
                DialogFragment dialogEdit = new DescriptionEditDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_SSID, ssid);
                dialogEdit.setArguments(bundle);
                dialogEdit.show(getSupportFragmentManager(), "DescriptionEditDialogFragment");
                return true;

            // Change password
            case R.id.changePassword:
                showChangePasswordDialog(network, ssid, isHidden);
                return true;

            // Delete
            case R.id.delete:
                NetworkDeleteAlertFragment dialogDelete = new NetworkDeleteAlertFragment();
                dialogDelete.show(getSupportFragmentManager(), "NetworkDeleteAlertFragment");
                return true;

            default:
                return super.onContextItemSelected(item);

        }
    }


    @Override
    public void onBackPressed() {
        Toasts.cancelAllToasts();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST) {// if permission is granted create list of networks
            if (grantResults.length > Constants.RESULTS_EMPTY
                    && ((grantResults[Constants.RESULT_INDEX] == PackageManager.PERMISSION_GRANTED))) {
                updateListOfNetworks();
            } else {
                finish();
            }
        }
    }


    // CLASS METHODS

    /*
         List item position correction due to header
    */
    private int getCorrectPosition(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            position = position - Constants.LIST_HEADER_POSITION;
        }
        return position;
    }

    /*
         Check if user's last location has been acquired
    */
    private boolean isLastLocationKnown() {
        return (lastLatitude != Constants.DEFAULT_LATITUDE) && (lastLongitude != Constants.DEFAULT_LONGITUDE);
    }

    /*
         Connect to network
    */
    private void connectToNetwork(WifiManager wifiManager, WifiConfiguration network) {

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        // Get network id of the current active network
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int activeNetworkId = wifiInfo.getNetworkId();
        // Disconnect and disable the current network
        wifiManager.disconnect();
        wifiManager.disableNetwork(activeNetworkId);
        ConfiguredNetworks.lastSupplicantNetworkState = SupplicantState.DISCONNECTED;
        ConfiguredNetworks.supplicantNetworkState = SupplicantState.DISCONNECTED;
        // Check if it is trying to connect to a different network
        if (network.networkId != activeNetworkId) {
            // Enable and connect to the new network
            wifiManager.enableNetwork(network.networkId, true);
        }

    }

    /*
         Show change password dialog
    */
    private void showChangePasswordDialog(WifiConfiguration network, String ssid, boolean isHidden) {

        DialogFragment dialogPassword = new PasswordChangeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_NETWORK_ID, network.networkId);
        bundle.putString(Constants.KEY_SSID, ssid);
        bundle.putBoolean(Constants.KEY_HIDDEN, isHidden);
        bundle.putString(Constants.KEY_SECURITY, configuredNetworks.getSecurity(ssid));
        dialogPassword.setArguments(bundle);
        dialogPassword.show(getSupportFragmentManager(), "PasswordChangeDialogFragment");

    }

    /*
         Show WiFi dialog if wifi was disabled while activity is running
    */
    private void showWifiDisabledAlertDialog() {
        // Show dialog informing that wifi was disabled
        wifiDisabledDialog = new WifiDisabledAlertFragment();
        wifiDisabledDialog.show(getSupportFragmentManager(), "WifiDisabledDialogListener");
    }

    /*
         Start "Available Networks" activity
    */
    private void startScanNetworksActivity() {
        Intent intent = new Intent(getApplicationContext(), ScanNetworksActivity.class);
        startActivity(intent);
    }

    /*
         Refresh list of networks
    */
    private void updateListOfNetworks() {

        // Reset the configured networks list
        if (wifiConfiguredNetworks != null) {
            wifiConfiguredNetworks.clear();
        }
        try {
            wifiConfiguredNetworks = wifiManager.getConfiguredNetworks();
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        }

        wifiScannedNetworks = wifiManager.getScanResults();

        if (wifiManager.isWifiEnabled()) {

            // Get the state of the current active network
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ConfiguredNetworks.supplicantNetworkState = wifiInfo.getSupplicantState();
            ConfiguredNetworks.supplicantSSID = wifiInfo.getSSID();

            if (wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                // Set mac address of the supplicant network configuration
                configuredNetworks.setMacAddressBySSID(wifiInfo.getSSID(), wifiInfo.getBSSID());
            }

            // Get networks list sort mode from settings
            String sort = settings.getString(getResources().getString(R.string.pref_key_sort),
                    getResources().getString(R.string.pref_def_sort));

            // Get the title display mode from settings
            String header = settings.getString(getResources().getString(R.string.pref_key_header),
                    getResources().getString(R.string.pref_def_header));

            try {
                assert sort != null;
                switch (sort) {

                    // Automatic
                    case Constants.PREF_SORT_AUTO:

                        // Sorts according to title display mode preference
                        assert header != null;
                        if (header.matches(Constants.PREF_HEADER_DESCRIPTION)) {
                            sortByDescription();
                        } else {
                            sortByName();
                        }

                        // Sort list by decreasing order of signal level
                        Collections.sort(wifiConfiguredNetworks, new Comparator<WifiConfiguration>() {
                            @Override
                            public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {

                                int rhsLevel = Constants.OUT_OF_REACH;
                                int lhsLevel = Constants.OUT_OF_REACH;

                                ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();
                                while (listIterator.hasNext()) {
                                    int index = listIterator.nextIndex();
                                    ScanResult scanResult = wifiScannedNetworks.get(index);
                                    String mac = scanResult.BSSID;
                                    if (configuredNetworks.getMacAddressBySSID(rhs.SSID)
                                            .matches(mac)) {
                                        rhsLevel = scanResult.level;
                                    }
                                    if (configuredNetworks.getMacAddressBySSID(lhs.SSID)
                                            .matches(mac)) {
                                        lhsLevel = scanResult.level;
                                    }
                                    listIterator.next();
                                }

                                return WifiManager.compareSignalLevel(rhsLevel, lhsLevel);
                            }
                        });

                        // Move connected network to the beginning of the list
                        ListIterator<WifiConfiguration> listIterator = wifiConfiguredNetworks.listIterator();
                        while (listIterator.hasNext()) {
                            int index = listIterator.nextIndex();
                            WifiConfiguration wifiConfiguration = wifiConfiguredNetworks.get(index);
                            if (wifiConfiguration.status == WifiConfiguration.Status.CURRENT) {
                                wifiConfiguredNetworks.remove(index);
                                wifiConfiguredNetworks.add(Constants.LIST_HEAD, wifiConfiguration);
                            }
                            listIterator.next();
                        }

                        break;

                    // By description
                    case Constants.PREF_SORT_DESCRIPTION:
                        sortByDescription();
                        break;

                    // By network name
                    case Constants.PREF_SORT_NAME:
                        sortByName();
                        break;

                    // Unsorted
                    default:
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // Get network id of the current active network
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int activeNetworkId = wifiInfo.getNetworkId();
            // Disconnect and disable the current network
            wifiManager.disconnect();
            wifiManager.disableNetwork(activeNetworkId);
            ConfiguredNetworks.lastSupplicantNetworkState = SupplicantState.DISCONNECTED;
            ConfiguredNetworks.supplicantNetworkState = SupplicantState.DISCONNECTED;
        }

        // Create a list adapter if one was not created yet
        if (networksListAdapter == null) {
            networksListAdapter = new ConfiguredNetworksListAdapter(this, wifiConfiguredNetworks, configuredNetworks);
            networksListView.setAdapter(networksListAdapter);
        } else {
            try {
                // Refresh list
                networksListAdapter.clear();
                networksListAdapter.addAll(wifiConfiguredNetworks);
                networksListAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /*
         Sort networks by their descriptions
    */
    private void sortByDescription() throws NullPointerException, ConcurrentModificationException {

        if ((wifiConfiguredNetworks != null) && (configuredNetworks != null)) {
            // sort list by ascending order of network description
            Collections.sort(wifiConfiguredNetworks, new Comparator<WifiConfiguration>() {

                @Override
                public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
                    // Get description for each ssid and compare them
                    String lhsDescription = configuredNetworks.getDescriptionBySSID(lhs.SSID);
                    String rhsDescription = configuredNetworks.getDescriptionBySSID(rhs.SSID);
                    return lhsDescription.compareToIgnoreCase(rhsDescription);
                }
            });
        }
    }

    /*
         Sort the networks by their names
    */
    private void sortByName() throws NullPointerException, ConcurrentModificationException {

        if (wifiConfiguredNetworks != null) {
            // sort list by ascending order of network name
            Collections.sort(wifiConfiguredNetworks, new Comparator<WifiConfiguration>() {
                @Override
                public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
                    return lhs.SSID.compareToIgnoreCase(rhs.SSID);
                }
            });
        }
    }


    // EDIT DESCRIPTION DIALOG

    @Override // OK
    public void onDescriptionEditDialogPositiveClick(DialogFragment dialog) {
        updateListOfNetworks();
    }

    @Override // Cancel
    public void onDescriptionEditDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // PASSWORD CHANGE DIALOG

    @Override
    public void onPasswordChangeDialogPositiveClick(DialogFragment dialog) {
        updateListOfNetworks();
    }

    @Override
    public void onPasswordChangeDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // NETWORK DELETE DIALOG

    @Override // Yes
    public void onNetworkDeleteDialogPositiveClick(DialogFragment dialog) {

        // Check if network removal was successful
        if (wifiManager.removeNetwork(network.networkId)) {
            wifiConfiguredNetworks.remove(network);
            wifiManager.saveConfiguration();
            updateListOfNetworks();
            configuredNetworks.removeNetworkData(network.SSID);
        } else {
            // If version is Marshmallow (6.x) or higher show dialog explaining new networks manage,ent policy
            if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M))
                    && (showNetworkManagementPolicyWarnPref.getBoolean(Constants.NET_MNG_POLICY_WARN, true))) {
                DialogFragment dialogPolicy = new NetworkManagementPolicyAlertFragment();
                dialogPolicy.show(getSupportFragmentManager(), "NetworkManagementPolicyAlertFragment");
            } else {
                Toasts.showUnableRemoveNetwork(getApplicationContext());
            }
        }

    }

    @Override // No
    public void onNetworkDeleteDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // NETWORK NAME CHANGED DIALOG

    @Override // Ok
    public void onNetworkNameChangedDialogPositiveClick(DialogFragment dialog) {
        try {
            configuredNetworks.getDataState();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onResume();
        networkNameChangedDialogOpened = false;
    }

    @Override // Cancel
    public void onNetworkNameChangedDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
        networkNameChangedDialogOpened = false;
    }


    // REQUEST PERMISSION DIALOG

    @Override // Yes
    public void onAlertLocationPermDialogPositiveClick(DialogFragment dialog) {
        // request permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.FINE_LOCATION_PERMISSION_REQUEST);

    }

    @Override // No
    public void onAlertLocationPermDialogNegativeClick(DialogFragment dialog) {
        finish();
    }


    // NETWORK MANAGEMENT POLICY DIALOG

    @Override // OK
    public void onAlertNetworkManagementPolicyDialogPositiveClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }

    @Override // Tell me more
    public void onAlertNetworkManagementPolicyDialogNeutralClick(DialogFragment dialog) {
        // Open help page explaining change on network management policy
        Intent intent = new Intent(this, HelpActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_URL, getString(R.string.url_help_main) + getString(R.string.url_help_note_tag));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override // Do not show again
    public void onAlertNetworkManagementPolicyDialogNegativeClick(DialogFragment dialog) {
        showNetworkManagementPolicyWarnPref.edit().putBoolean(Constants.NET_MNG_POLICY_WARN, false).apply();
    }


    // ADD NETWORK DIALOG

    @Override // Connect
    public void onAddNetworkDialogPositiveClick(DialogFragment dialog) {
        // Start scanning for available networks
        wifiManager.startScan();
    }

    @Override // Cancel
    public void onAddNetworkDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // RESTORE NETWORKS DIALOG

    @Override // Yes
    public void onAlertDialogPositiveClick(DialogFragment dialog) {

        List<NetworkData> networksData = configuredNetworks.getConfiguredNetworksData();
        ListIterator<NetworkData> listIterator = networksData.listIterator();

        while (listIterator.hasNext()) {

            int index = listIterator.nextIndex();
            NetworkData data = networksData.get(index);

            // If no password is stored and network is not open set a dummy password
            if (data.getPassword().isEmpty()
                    && configuredNetworks.getNetworkSecurity(data.getSecurity()) != Constants.SET_OPEN) {
                data.setPassword(Constants.WPA_DUMMY_PASSWORD);
            }
            // Add network configuration
            configuredNetworks.addNetworkConfiguration(this, data.getSSID(), data.isHidden(),
                    data.getSecurity(), data.getPassword());

            // If store password settings option is disabled clear password
            if (!settings.getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
                data.setPassword(Constants.EMPTY);
            }
            listIterator.next();

        }

        configuredNetworks.saveDataState();

        updateListOfNetworks();

    }

    @Override // No
    public void onAlertDialogNegativeClick(DialogFragment dialog) {
        try {
            configuredNetworks.collectGarbage(wifiConfiguredNetworks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // WIFI DISABLED ALERT DIALOG

    @Override
    public void onAlertWifiDisabledDialogPositiveClick(DialogFragment dialog) {
        wifiDisabledDialog = null;
        wifiManager.setWifiEnabled(true);
    }

    @Override
    public void onAlertWifiDisabledDialogNegativeClick(DialogFragment dialog) {
        finish();
    }

}