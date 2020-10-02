/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ScanNetworksActivity.java
 *  Last modified : 10/1/20 9:43 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.apps.mohb.wifiauthority.adapters.ScannedNetworksListAdapter;
import com.apps.mohb.wifiauthority.fragments.dialogs.AddNetworkDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.LocationPermissionsAlertFragment;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;


public class ScanNetworksActivity extends AppCompatActivity implements
        LocationPermissionsAlertFragment.LocationPermissionsDialogListener,
        AddNetworkDialogFragment.AddNetworkDialogListener {

    private WifiManager wifiManager;
    private List<ScanResult> wifiScannedNetworks;
    private ConfiguredNetworks configuredNetworks;
    private ScannedNetworksListAdapter networksListAdapter;
    private ListView networksListView;

    private ProgressDialog progressDialog;

    private SharedPreferences settings;
    private String minSecurityToShow;
    private String minSignalLevelToShow;

    private FusedLocationProviderClient fusedLocationClient;
    private double lastLatitude;
    private double lastLongitude;

    /*
         Inner class to receive WiFi scan results
    */
    private class WiFiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            progressDialog.cancel();

            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            try {
                wifiScannedNetworks = wifiManager.getScanResults();

                if (wifiScannedNetworks.isEmpty()) {
                    Toasts.showNoNetworkFound(getApplicationContext(), R.string.toast_no_network_found);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                // sort list by decreasing order of signal level
                Collections.sort(wifiScannedNetworks, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult lhs, ScanResult rhs) {
                        return WifiManager.compareSignalLevel(rhs.level, lhs.level);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {

                // remove duplicated networks from list if the show all aps setting is off
                if (!settings.getBoolean(Constants.PREF_KEY_SHOW_ALL_APS, false)) {

                    String uniques = Constants.EMPTY;
                    ListIterator<ScanResult> listIteratorDuplicate = wifiScannedNetworks.listIterator();

                    while (listIteratorDuplicate.hasNext()) {

                        int indexDuplicate = listIteratorDuplicate.nextIndex();
                        String ssid = wifiScannedNetworks.get(indexDuplicate).SSID;

                        if (uniques.contains(ssid)) {
                            listIteratorDuplicate.next();
                            listIteratorDuplicate.remove();
                        } else {
                            uniques = uniques.concat("[" + ssid + "]");
                            listIteratorDuplicate.next();
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                // Remove security levels according to settings
                ListIterator<ScanResult> scanResultListIterator = wifiScannedNetworks.listIterator();

                while (scanResultListIterator.hasNext()) {

                    int index = scanResultListIterator.nextIndex();
                    String ssid = wifiScannedNetworks.get(index).SSID;
                    String capabilities = wifiScannedNetworks.get(index).capabilities;

                    // Get security levels to remove
                    minSecurityToShow = settings.getString(getResources().getString(R.string.pref_key_security),
                            getResources().getString(R.string.pref_def_security));

                    // Set if security is WEP or open
                    boolean isWep = false;
                    boolean isOpen = false;

                    if (capabilities.contains(Constants.SCAN_WEP)) {
                        isWep = true;
                    } else {
                        if (!capabilities.contains(Constants.SCAN_EAP)
                                && !capabilities.contains(Constants.SCAN_WEP)
                                && !capabilities.contains(Constants.SCAN_WPA)) {
                            isOpen = true;
                        }

                    }

                    // Get signal levels to show according to settings
                    minSignalLevelToShow = settings.getString(getResources().getString(R.string.pref_key_signal),
                            getResources().getString(R.string.pref_def_signal));

                    int minSignalLevel;

                    assert minSignalLevelToShow != null;
                    switch (minSignalLevelToShow) {

                        case Constants.PREF_MIN_SIGNAL_HIGH:
                            minSignalLevel = Constants.LEVEL_HIGH;
                            break;

                        case Constants.PREF_MIN_SIGNAL_LOW:
                            minSignalLevel = Constants.LEVEL_LOW;
                            break;

                        default:
                            minSignalLevel = Constants.LEVEL_VERY_LOW;
                            break;

                    }

                    int signalLevel = WifiManager.calculateSignalLevel(
                            wifiScannedNetworks.get(index).level, Constants.LEVELS);

                    // Remove insecure (if option is on), low signal levels and hidden networks from list
                    if ((minSecurityToShow.matches(Constants.PREF_SECURITY_WPA_EAP) && (isWep || isOpen)
                            || (minSecurityToShow.matches(Constants.PREF_SECURITY_WEP) && (isOpen)))
                            || (signalLevel < minSignalLevel)
                            || ssid.isEmpty()) {
                        scanResultListIterator.next();
                        scanResultListIterator.remove();
                    } else {
                        scanResultListIterator.next();
                    }
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }

            // If there is no network to show according to settings, inform this
            if ((wifiScannedNetworks != null) && (wifiScannedNetworks.isEmpty())) {
                if (minSecurityToShow.matches(Constants.PREF_SECURITY_OPEN)
                        || (minSignalLevelToShow.matches(Constants.PREF_MIN_SIGNAL_VERY_LOW))) {
                    Toasts.showNoNetworkFound(getApplicationContext(), R.string.toast_no_network_to_display);
                }
            }
            updateListOfNetworks(context);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_networks);

        // Create list header and footer, that will insert spaces on top and bottom of the
        // list to make material design effect elevation and shadow
        View listHeader = getLayoutInflater().inflate(R.layout.list_header, networksListView);
        View listFooter = getLayoutInflater().inflate(R.layout.list_footer, networksListView);

        networksListView = findViewById(R.id.scanList);

        // Insert header and footer if version is Lollipop (5.x) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networksListView.addHeaderView(listHeader);
            networksListView.addFooterView(listFooter);
            listHeader.setClickable(false);
            listFooter.setClickable(false);
        }

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        configuredNetworks = new ConfiguredNetworks(this);

        // handle clicks on list items
        networksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // This check is necessary due to app is incapable of get list of
                // scanned networks if WiFi is disabled, generating null pointer exceptions.
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                // Set default values for these variables
                String ssid = Constants.EMPTY;
                String bssid = Constants.EMPTY;
                String capabilities = Constants.EMPTY;

                // Get the correct position of clicked item according to Android version
                // due to use of header and footer
                int correctPosition = getCorrectPosition(position);

                // Check if position is inside networks list size (not header or footer)
                if ((correctPosition >= Constants.LIST_HEAD) && (correctPosition < wifiScannedNetworks.size())) {
                    ssid = wifiScannedNetworks.get(correctPosition).SSID;
                    bssid = wifiScannedNetworks.get(correctPosition).BSSID;
                    capabilities = wifiScannedNetworks.get(correctPosition).capabilities;
                }

                try {
                    // Check if network is not configured yet
                    if (!configuredNetworks.isConfiguredBySSID(wifiManager.getConfiguredNetworks(), ssid)) {
                        DialogFragment dialog = new AddNetworkDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.KEY_SSID, ssid);
                        bundle.putString(Constants.KEY_BSSID, bssid);
                        bundle.putString(Constants.KEY_SECURITY, capabilities);
                        bundle.putDouble(Constants.KEY_LATITUDE, lastLatitude);
                        bundle.putDouble(Constants.KEY_LONGITUDE, lastLongitude);
                        dialog.setArguments(bundle);
                        dialog.show(getSupportFragmentManager(), "AddNetworkDialogFragment");
                    } else { // If network is already configured show dialog informing this
                        Toasts.showNetworkIsConfigured(getApplicationContext());
                    }
                } catch (NullPointerException | SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create an instance of GoogleAPIClient to load maps
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // check permissions to access fine location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            scanForAvailableNetworks();
        } else {

            // check if user already denied permission request
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))) {
                DialogFragment dialog = new LocationPermissionsAlertFragment();
                dialog.show(getSupportFragmentManager(), "LocationPermissionsAlertFragment");
            } else {
                // request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.FINE_LOCATION_PERMISSION_REQUEST);
            }

        }

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
        getMenuInflater().inflate(R.menu.options_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent intent;

        switch (id) {

            // Add Network
            case R.id.action_add_network:
                DialogFragment dialog = new AddNetworkDialogFragment();
                dialog.show(getSupportFragmentManager(), "AddNetworkDialogFragment");
                break;

            // Settings
            case R.id.action_scan_settings:
                settings.edit().putBoolean(Constants.PREF_KEY_SCAN_ACTIVITY, true).apply();
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            // Help
            case R.id.action_help_scan:
                intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_URL, getString(R.string.url_help_scan));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        Toasts.cancelAllToasts();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Finished activity so wifi will not be
        // turned on if application is running
        // on background
        finish();
    }


    // CLASS METHODS

    /*
         Scan for networks on reach
    */
    private void scanForAvailableNetworks() {
        // Shows a dialog window with a spin wheel informing that data is being fetched
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_scan_networks));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        WiFiScanReceiver wiFiScanReceiver = new WiFiScanReceiver();
        registerReceiver(wiFiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

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
         Update the list of available networks
    */
    private void updateListOfNetworks(Context context) {
        try {
            if (networksListAdapter == null) {
                networksListAdapter = new ScannedNetworksListAdapter(context, wifiScannedNetworks);
                networksListView.setAdapter(networksListAdapter);
            } else {
                // Refresh list
                networksListAdapter.clear();
                networksListAdapter.addAll(wifiScannedNetworks);
                networksListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // REQUEST PERMISSION DIALOG

    @Override  // Yes
    public void onAlertLocationPermDialogPositiveClick(DialogFragment dialog) {
        // request permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.FINE_LOCATION_PERMISSION_REQUEST);

    }

    @Override // No
    public void onAlertLocationPermDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
        finish();
    }


    // ADD NETWORK DIALOG

    @Override // Connect
    public void onAddNetworkDialogPositiveClick(DialogFragment dialog) {
        finish();
    }

    @Override // Cancel
    public void onAddNetworkDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST) {// if permission is granted reset
            if (grantResults.length > Constants.RESULTS_EMPTY
                    && ((grantResults[Constants.RESULT_INDEX] != PackageManager.PERMISSION_GRANTED))) {
                scanForAvailableNetworks();
            } else {
                finish();
            }
        }
    }


}
