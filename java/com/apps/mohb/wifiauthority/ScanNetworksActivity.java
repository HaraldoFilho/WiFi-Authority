/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ScanNetworksActivity.java
 *  Last modified : 3/21/17 11:04 PM
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
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apps.mohb.wifiauthority.adapters.ScannedNetworksListAdapter;
import com.apps.mohb.wifiauthority.fragments.dialogs.AddNetworkDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.LocationPermissionsAlertFragment;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;


public class ScanNetworksActivity extends AppCompatActivity implements
        LocationPermissionsAlertFragment.LocationPermissionsDialogListener,
        AddNetworkDialogFragment.AddNetworkDialogListener {

    private WifiManager wifiManager;
    private List<ScanResult> wifiScannedNetworks;
    private ListView networksListView;
    private View listHeader;
    private View listFooter;
    private ScannedNetworksListAdapter networksListAdapter;
    private WiFiScanReceiver wiFiScanReceiver;
    private ConfiguredNetworks configuredNetworks;
    private ProgressDialog progressDialog;


    // Inner class to receive WiFi scan results
    private class WiFiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            progressDialog.cancel();

            wifiScannedNetworks = wifiManager.getScanResults();

            // remove hidden networks from list
            ListIterator<ScanResult> listIteratorHidden = wifiScannedNetworks.listIterator();
            while (listIteratorHidden.hasNext()) {
                int indexHidden = listIteratorHidden.nextIndex();
                String ssid = wifiScannedNetworks.get(indexHidden).SSID;
                if (ssid.isEmpty()) {
                    listIteratorHidden.next();
                    listIteratorHidden.remove();
                } else {
                    listIteratorHidden.next();
                }
            }

            // sort list by decreasing order of signal level
            Collections.sort(wifiScannedNetworks, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return wifiManager.compareSignalLevel(rhs.level, lhs.level);
                }
            });


            // remove duplicated networks from list
            String uniques = "";
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

            if (networksListAdapter == null) {
                networksListAdapter = new ScannedNetworksListAdapter(context, wifiScannedNetworks);
                networksListView.setAdapter(networksListAdapter);
            } else {
                // Refresh list
                networksListAdapter.clear();
                networksListAdapter.addAll(wifiScannedNetworks);
                networksListAdapter.notifyDataSetChanged();
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_networks);

        // Create list header and footer, that will insert spaces on top and bottom of the
        // list to make material design effect elevation and shadow
        listHeader = getLayoutInflater().inflate(R.layout.list_header, networksListView);
        listFooter = getLayoutInflater().inflate(R.layout.list_footer, networksListView);

        networksListView = (ListView) findViewById(R.id.scanList);

        // Insert header and footer if version is Lollipop (5.x) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networksListView.addHeaderView(listHeader);
            networksListView.addFooterView(listFooter);
            listHeader.setClickable(false);
            listFooter.setClickable(false);
        }

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        configuredNetworks = new ConfiguredNetworks(this);

        // handle clicks on list items
        networksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // This check is necessary due to app is incapable of get list of
                // scanned networks if WiFi is disabled, generating null pointer exceptions.
                if (wifiManager.isWifiEnabled()) {

                    // Set default values for these variables
                    String ssid = "";
                    String bssid = "";
                    String capabilities = "";

                    // Get the correct position of clicked item according to Android version
                    // due to use of header and footer
                    int correctPosition = getCorrectPosition(position);

                    // Check if position is inside networks list size (not header or footer)
                    if ((correctPosition >= 0) && (correctPosition < wifiScannedNetworks.size())) {
                        ssid = wifiScannedNetworks.get(correctPosition).SSID;
                        bssid = wifiScannedNetworks.get(correctPosition).BSSID;
                        capabilities = wifiScannedNetworks.get(correctPosition).capabilities;
                    }

                    // Check if network is not configured yet
                    if (!configuredNetworks.isConfiguredBySSID(wifiManager.getConfiguredNetworks(), ssid)) {
                        DialogFragment dialog = new AddNetworkDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.KEY_SSID, ssid);
                        bundle.putString(Constants.KEY_BSSID, bssid);
                        bundle.putString(Constants.KEY_SECURITY, capabilities);
                        dialog.setArguments(bundle);
                        dialog.show(getSupportFragmentManager(), "AddNetworkDialogFragment");
                    } else { // If network is already configured show dialog informing this
                        Toasts.showNetworkIsConfigured(getApplicationContext());
                    }
                } else {
                    Toasts.showWiFiDisabled(getApplicationContext());
                    wifiManager.setWifiEnabled(true);
                }
            }
        });

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            // Add Hidden Network
            case R.id.action_add_network:
                DialogFragment dialog = new AddNetworkDialogFragment();
                dialog.show(getSupportFragmentManager(), "AddHiddenNetworkDialogFragment");
                break;

            // Help
            case R.id.action_help_scan:
                Intent intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", getString(R.string.url_help_scan));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        Toasts.cancelAllToasts();
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void scanForAvailableNetworks() {

        // Shows a dialog window with a spin wheel informing that data is being fetched
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_scan_networks));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        wiFiScanReceiver = new WiFiScanReceiver();
        registerReceiver(wiFiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    // List item position correction due to header
    private int getCorrectPosition(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            position = position - Constants.LIST_HEADER_POSITION;
        }
        return position;
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
        dialog.getDialog().cancel();
        finish();
    }


    // ADD NETWORK DIALOG

    @Override // Connect
    public void onAddNetworkDialogPositiveClick(DialogFragment dialog) {
        finish();
    }

    @Override // Cancel
    public void onAddNetworkDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }


    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case Constants.FINE_LOCATION_PERMISSION_REQUEST: {
                // if permission is granted reset
                if (grantResults.length > 0
                        && ((grantResults[0] != PackageManager.PERMISSION_GRANTED))) {
                    scanForAvailableNetworks();
                } else {
                    finish();
                }
                return;
            }
        }
    }


}
