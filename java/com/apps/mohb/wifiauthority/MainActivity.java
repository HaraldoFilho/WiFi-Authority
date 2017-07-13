/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MainActivity.java
 *  Last modified : 7/12/17 11:55 PM
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
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apps.mohb.wifiauthority.adapters.ConfiguredNetworksListAdapter;
import com.apps.mohb.wifiauthority.fragments.dialogs.AddNetworkDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.DescriptionEditDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.LocationPermissionsAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkDeleteAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkManagementPolicyAlertFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.NetworkNameChangedDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.PasswordChangeDialogFragment;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity implements
        DescriptionEditDialogFragment.DescriptionEditDialogListener,
        AddNetworkDialogFragment.AddNetworkDialogListener,
        NetworkDeleteAlertFragment.NetworkDeleteDialogListener,
        NetworkNameChangedDialogFragment.NetworkNameChangedDialogListener,
        PasswordChangeDialogFragment.PasswordChangeDialogListener,
        LocationPermissionsAlertFragment.LocationPermissionsDialogListener,
        NetworkManagementPolicyAlertFragment.NetworkManagementPolicyDialogListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private WifiManager wifiManager;
    private WiFiScanReceiver wifiScanReceiver;
    private List<ScanResult> wifiScannedNetworks;
    private List<WifiConfiguration> wifiConfiguredNetworks;
    private ListView networksListView;
    private View listHeader;
    private View listFooter;
    private FloatingActionButton fab;

    private ConfiguredNetworksListAdapter networksListAdapter;
    private AdapterView.AdapterContextMenuInfo menuInfo;

    private ConfiguredNetworks configuredNetworks;
    protected WifiConfiguration network;

    private GoogleApiClient googleApiClient;
    private Location location;
    private double lastLatitude;
    private double lastLongitude;

    private SharedPreferences showNetworkManagementPolicyWarnPref;
    private boolean networkNameChangedDialogOpened;
    private String ssid;

    private SharedPreferences settings;


    // Inner class to monitor network state changes
    public class NetworkStateMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("DEBUG_WIFI", "Stated changed!");
            // Refresh list of networks when connection state changes
            updateListOfNetworks();

        }

    }


    // Inner class to receive WiFi scan results
    private class WiFiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // If WiFi is enabled, refresh list of networks
            if (wifiManager.isWifiEnabled()) {
                updateListOfNetworks();
            } else {
                return;
            }

            wifiScannedNetworks = wifiManager.getScanResults();


            // Get the last user's none location. Most of the times
            // this corresponds to user's current location or very near
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    lastLatitude = location.getLatitude();
                    lastLongitude = location.getLongitude();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

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
                        // Check if the network is configured using its mac address
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
                                    configuredNetworks.addNetworkData("", scanResult.SSID, scanResult.BSSID,
                                            scanResult.capabilities, "", lastLatitude, lastLongitude);
                                } else { // If location has not been acquired create additional data for the network
                                    // with the scanned SSID, Mac Address and the default location (0,0)
                                    configuredNetworks.addNetworkData("", scanResult.SSID, scanResult.BSSID,
                                            scanResult.capabilities, "", Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
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

                        SharedPreferences settings
                                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        // Check if ssid reconfiguration setting is on ...
                        if (settings.getBoolean(Constants.PREF_KEY_RECONFIG, true)
                                // ... if Mac Address of configured network matches Mac Address of scanned network...
                                && (configuredNetworks.getMacAddressBySSID(ssid).matches(scanResult.BSSID))
                                // ... if network is not hidden...
                                && (!scanResult.SSID.isEmpty())
                                // ... and if SSID of the network has been changed
                                && (!ssid.matches(configuredNetworks.getCfgSSID(scanResult.SSID)))) {

                            // Updates the SSID of the configured network
                            WifiConfiguration configuration = configuredNetworks
                                    .updateSSIDbyMacAddress(wifiConfiguredNetworks, scanResult.BSSID, scanResult.SSID);

                            int updateResult = wifiManager.updateNetwork(configuration);

                            // Check if update has failed and the name changed dialog is not opened
                            if ((updateResult == Constants.NETWORK_UPDATE_FAIL) && (!networkNameChangedDialogOpened)) {

                                // Check if network is not hidden
                                if (!scanResult.SSID.isEmpty()) {
                                    /**
                                     Opens name changed dialog with the following data of the changed network:
                                     - Description
                                     - Old name (SSID)
                                     - New name (SSID)
                                     - Security
                                     **/
                                    DialogFragment networkNameChangedDialog = new NetworkNameChangedDialogFragment();

                                    Bundle bundle = new Bundle();
                                    bundle.putString(Constants.KEY_DESCRIPTION,
                                            configuredNetworks.getDescriptionBySSID(ssid));
                                    bundle.putString(Constants.KEY_OLD_NAME, ssid);
                                    bundle.putString(Constants.KEY_NEW_NAME, scanResult.SSID);
                                    bundle.putString(Constants.KEY_SECURITY, scanResult.capabilities);
                                    bundle.putString(Constants.KEY_BSSID, scanResult.BSSID); // send also the Mac Address

                                    networkNameChangedDialog.setArguments(bundle);
                                    networkNameChangedDialog.show(getSupportFragmentManager(),
                                            "NetworkNameChangedDialogFragment");
                                    // Register that dialog is opened
                                    networkNameChangedDialogOpened = true;

                                }
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

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create list header and footer, that will insert spaces on top and bottom of the
        // list to make material design effect elevation and shadow
        listHeader = getLayoutInflater().inflate(R.layout.list_header, networksListView);
        listFooter = getLayoutInflater().inflate(R.layout.list_footer, networksListView);

        networksListView = (ListView) findViewById(R.id.networksList);

        // Insert header and footer if version is Lollipop (5.x) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networksListView.addHeaderView(listHeader);
            networksListView.addFooterView(listFooter);
            listHeader.setClickable(false);
            listFooter.setClickable(false);
        }

        registerForContextMenu(networksListView);

        // Create floating action button and handle clicks on it to add a network
        fab = (FloatingActionButton) findViewById(R.id.fab);
        // Make floating button translucent
        fab.setAlpha(Constants.FAB_TRANSLUCENT);
        // Monitor clicks on button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
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
                String ssid = "";
                Double latitude = Constants.DEFAULT_LATITUDE;
                Double longitude = Constants.DEFAULT_LONGITUDE;

                // Get the correct position of clicked item according to Android version
                // due to use of header and footer
                int correctPosition = getCorrectPosition(position);

                // Check if position is inside networks list size (not header or footer)
                if ((correctPosition >= 0) && (correctPosition < wifiConfiguredNetworks.size())) {
                    ssid = wifiConfiguredNetworks.get(correctPosition).SSID;
                    latitude = configuredNetworks.getLatitudeBySSID(ssid);
                    longitude = configuredNetworks.getLongitudeBySSID(ssid);
                }

                String mac = configuredNetworks.getMacAddressBySSID(ssid);

                // Create bundle with the information needed to show more detailed information
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_SSID, configuredNetworks.getDataSSID(ssid));
                bundle.putString(Constants.KEY_BSSID, mac);
                bundle.putDouble(Constants.KEY_LATITUDE, latitude);
                bundle.putDouble(Constants.KEY_LONGITUDE, longitude);
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

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
                if (firstVisibleItem > 0) {
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
        wifiScanReceiver = new WiFiScanReceiver();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Create an instance of GoogleAPIClient to load maps
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Shared preferences variable to control if Network Management Policy Dialog for Marshmallow (version 6.x)
        // or higher must be shown
        showNetworkManagementPolicyWarnPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

        // Register a broadcast receiver to monitor changes on network state to update network status
        BroadcastReceiver wifiStateMonitor = new NetworkStateMonitor();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        this.registerReceiver(wifiStateMonitor, filter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeApplication();
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
                    Toasts.showMissingInformation(getApplicationContext(),
                            R.string.toast_no_configured_networks);
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
                bundle.putString("url", getString(R.string.url_help_main));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            // About
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                bundle = new Bundle();
                bundle.putString("url", getString(R.string.url_help_main));
                intent.putExtras(bundle);
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

        // Disable connect item clicks and set text to connect by default
        itemConnect.setTitle(R.string.context_connect);
        itemConnect.setEnabled(false);

        if (wifiManager.isWifiEnabled()) {
            // Set text according to network state
            switch (network.status) {
                // Connected
                case WifiConfiguration.Status.CURRENT:
                    itemConnect.setTitle(R.string.context_disconnect);
                    break;
                // Connecting...
                case WifiConfiguration.Status.ENABLED:
                    itemConnect.setTitle(R.string.context_cancel);
                    break;
                // Disconnected
                default:
                    break;
            }
        }

        // Disable password item if network is open
        if ((network.wepKeys[0] == null) && (network.preSharedKey == null)) {
            itemPassword.setEnabled(false);
        }


        List<ScanResult> wifiScannedNetworks = wifiManager.getScanResults();

        // If network is available to connect or is hidden enable item click
        ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();
        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            ScanResult scanResult = wifiScannedNetworks.get(index);
            if ((configuredNetworks.getDataSSID(network.SSID).matches(scanResult.SSID))
                    || (configuredNetworks.getMacAddressBySSID(network.SSID).matches(scanResult.BSSID))) {
                itemConnect.setEnabled(true);
            }
            listIterator.next();
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        network = wifiConfiguredNetworks.get(getCorrectPosition(menuInfo.position));
        ssid = network.SSID;

        Bundle bundle;

        switch (item.getItemId()) {

            // Connect
            case R.id.connect:
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                // Get network id of the current active network
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int activeNetworkId = wifiInfo.getNetworkId();
                // Disconnect and disable the current network
                wifiManager.disconnect();
                wifiManager.disableNetwork(activeNetworkId);
                ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.DISCONNECTED;
                ConfiguredNetworks.supplicantNetworkState = NetworkInfo.DetailedState.DISCONNECTED;
                // Check if it is trying to connect to a different network
                if (network.networkId != activeNetworkId) {
                    // Enable and connect to the new network
                    wifiManager.enableNetwork(network.networkId, true);
                }
                return true;

            // Edit Description
            case R.id.editDescription:
                DialogFragment dialogEdit = new DescriptionEditDialogFragment();
                bundle = new Bundle();
                bundle.putString(Constants.KEY_SSID, ssid);
                dialogEdit.setArguments(bundle);
                dialogEdit.show(getSupportFragmentManager(), "DescriptionEditDialogFragment");
                return true;

            // Change password
            case R.id.changePassword:
                DialogFragment dialogPassword = new PasswordChangeDialogFragment();
                bundle = new Bundle();
                bundle.putInt(Constants.KEY_NETWORK_ID, network.networkId);
                bundle.putString(Constants.KEY_SSID, ssid);
                bundle.putString(Constants.KEY_SECURITY, network.allowedKeyManagement.toString());
                dialogPassword.setArguments(bundle);
                dialogPassword.show(getSupportFragmentManager(), "PasswordChangeDialogFragment");
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

    // List item position correction due to header
    private int getCorrectPosition(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            position = position - Constants.LIST_HEADER_POSITION;
        }
        return position;
    }

    // Check if user's last location has been acquired
    private boolean isLastLocationKnown() {
        if ((lastLatitude != Constants.DEFAULT_LATITUDE) && (lastLongitude != Constants.DEFAULT_LONGITUDE)) {
            return true;
        } else {
            return false;
        }
    }

    // Resume application after being closed
    private void resumeApplication() {

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Get configured networks additional data
        try {
            if (configuredNetworks == null) {
                configuredNetworks = new ConfiguredNetworks(this);
            }
            configuredNetworks.getDataState();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete data from networks that were removed by Android system
        try {
            configuredNetworks.collectGarbage(wifiManager.getConfiguredNetworks());
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (wifiManager.isWifiEnabled()) {
                updateListOfNetworks();
                // If GoogleApiClient is connected start scanning for available networks
                if (googleApiClient.isConnected()) {
                    wifiManager.startScan();
                }
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

    }


    private void startScanNetworksActivity() {
        Intent intent = new Intent(getApplicationContext(), ScanNetworksActivity.class);
        startActivity(intent);
    }

    // Refresh list of networks
    private void updateListOfNetworks() {

        if (wifiManager.isWifiEnabled()) {

            // Get the state of the current active network
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ConfiguredNetworks.supplicantNetworkState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
            ConfiguredNetworks.supplicantSSID = wifiInfo.getSSID();

            // Reset the configured networks list
            if (wifiConfiguredNetworks != null) {
                wifiConfiguredNetworks.clear();
            }
            wifiConfiguredNetworks = wifiManager.getConfiguredNetworks();

            // Get networks list sort mode from settings
            String sort = settings.getString(getResources().getString(R.string.pref_key_sort),
                    getResources().getString(R.string.pref_def_sort));

            // Get the title display mode from settings
            String header = settings.getString(getResources().getString(R.string.pref_key_header),
                    getResources().getString(R.string.pref_def_header));

            switch (sort) {

                // Automatic
                case Constants.PREF_SORT_AUTO:
                    // Sorts according to title display mode preference
                    if (header.matches(Constants.PREF_HEADER_DESCRIPTION)) {
                        sortByDescription();
                    } else {
                        sortByName();
                    }
                    try {
                        // Sort list by decreasing order of signal level
                        Collections.sort(wifiConfiguredNetworks, new Comparator<WifiConfiguration>() {
                            @Override
                            public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
                                int rhsLevel = Constants.OUT_OF_REACH;
                                int lhsLevel = Constants.OUT_OF_REACH;

                                wifiScannedNetworks = wifiManager.getScanResults();
                                ListIterator<ScanResult> listIterator = wifiScannedNetworks.listIterator();

                                while (listIterator.hasNext()) {
                                    int index = listIterator.nextIndex();
                                    ScanResult scanResult = wifiScannedNetworks.get(index);
                                    String ssid = scanResult.SSID;
                                    if (rhs.SSID.matches(configuredNetworks.getCfgSSID(ssid))) {
                                        rhsLevel = scanResult.level;
                                    }
                                    if (lhs.SSID.matches(configuredNetworks.getCfgSSID(ssid))) {
                                        lhsLevel = scanResult.level;
                                    }
                                    listIterator.next();
                                }

                                return wifiManager.compareSignalLevel(rhsLevel, lhsLevel);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
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

        } else {
            // Get network id of the current active network
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int activeNetworkId = wifiInfo.getNetworkId();
            // Disconnect and disable the current network
            wifiManager.disconnect();
            wifiManager.disableNetwork(activeNetworkId);
            ConfiguredNetworks.lastSupplicantNetworkState = NetworkInfo.DetailedState.DISCONNECTED;
            ConfiguredNetworks.supplicantNetworkState = NetworkInfo.DetailedState.DISCONNECTED;
        }

        try {
            // Create a list adapter if one was not created yet
            if (networksListAdapter == null) {
                networksListAdapter = new ConfiguredNetworksListAdapter(this,
                        wifiConfiguredNetworks, configuredNetworks);
                networksListView.setAdapter(networksListAdapter);
            } else {
                // Refresh list
                networksListAdapter.clear();
                networksListAdapter.addAll(wifiConfiguredNetworks);
                networksListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Sort networks by their descriptions
    private void sortByDescription() {

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

    // Sort the networks by their names
    private void sortByName() {

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
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case Constants.FINE_LOCATION_PERMISSION_REQUEST: {
                // if permission is granted create list of networks
                if (grantResults.length > 0
                        && ((grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                    updateListOfNetworks();
                } else {
                    finish();
                }
                return;
            }
        }
    }


    // GoogleClientApi methods

    @Override
    public void onConnected(Bundle bundle) {
        wifiManager.startScan();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    // EDIT DESCRIPTION DIALOG

    @Override // OK
    public void onDescriptionEditDialogPositiveClick(DialogFragment dialog) {
        updateListOfNetworks();
    }

    @Override // Cancel
    public void onDescriptionEditDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }


    // PASSWORD CHANGE DIALOG

    @Override
    public void onPasswordChangeDialogPositiveClick(DialogFragment dialog) {
        updateListOfNetworks();
    }

    @Override
    public void onPasswordChangeDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
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
        dialog.getDialog().cancel();
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
        dialog.getDialog().cancel();
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
        dialog.getDialog().cancel();
    }

    @Override // Do not show again
    public void onAlertNetworkManagementPolicyDialogNeutralClick(DialogFragment dialog) {
        showNetworkManagementPolicyWarnPref.edit().putBoolean(Constants.NET_MNG_POLICY_WARN, false).commit();
    }


    // ADD NETWORK DIALOG

    @Override // Connect
    public void onAddNetworkDialogPositiveClick(DialogFragment dialog) {
        // If GoogleApiClient is connected start scanning for available networks
        if (googleApiClient.isConnected()) {
            wifiManager.startScan();
        }
    }

    @Override // Cancel
    public void onAddNetworkDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }


}