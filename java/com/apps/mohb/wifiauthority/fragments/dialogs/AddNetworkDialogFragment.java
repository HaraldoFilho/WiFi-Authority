/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : AddNetworkDialogFragment.java
 *  Last modified : 12/22/16 8:31 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.Toasts;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;


public class AddNetworkDialogFragment extends DialogFragment {

    public interface AddNetworkDialogListener {
        void onAddNetworkDialogPositiveClick(DialogFragment dialog);

        void onAddNetworkDialogNegativeClick(DialogFragment dialog);
    }

    private AddNetworkDialogListener mListener;
    private EditText networkName;
    private Spinner networkSecurity;
    private EditText networkPasswd;
    private CheckBox checkPasswdVisible;
    private EditText networkDescription;


    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private ConfiguredNetworks configuredNetworks;

    String ssid;
    String security = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_network_dialog, null);

        networkName = (EditText) view.findViewById(R.id.txtSSID);
        networkSecurity = (Spinner) view.findViewById(R.id.spinSecurity);
        networkPasswd = (EditText) view.findViewById(R.id.txtPassword);
        checkPasswdVisible = (CheckBox) view.findViewById(R.id.checkPasswd);
        networkDescription = (EditText) view.findViewById(R.id.txtDescription);

        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        configuredNetworks = new ConfiguredNetworks(getContext());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.security_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSecurity.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);


        final Bundle bundle = this.getArguments();

        if (bundle != null) {
            ssid = bundle.getString(Constants.KEY_SSID);
            security = bundle.getString(Constants.KEY_SECURITY);
            networkName.setText(ssid);
            networkName.setEnabled(false);

            if (configuredNetworks.getNetworkSecurity(security) != Constants.SET_OPEN) {
                networkSecurity.setSelection(configuredNetworks.getNetworkSecurity(security));
            } else {
                networkPasswd.setEnabled(false);
                checkPasswdVisible.setEnabled(false);
            }
            networkSecurity.setEnabled(false);

            wifiConfiguration = configuredNetworks.setNetworkCiphers(wifiConfiguration, security);

            builder.setTitle(R.string.dialog_add_network_title);
        } else {
            builder.setTitle(R.string.dialog_add_hidden_network_title);
            wifiConfiguration.hiddenSSID = true;
        }


        builder.setPositiveButton(R.string.dialog_button_connect, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onAddNetworkDialogPositiveClick(AddNetworkDialogFragment.this);

                ssid = networkName.getText().toString();

                if (!configuredNetworks.isConfiguredBySSID(wifiManager.getConfiguredNetworks(), ssid)) {

                    wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
                    wifiConfiguration.SSID = configuredNetworks.getCfgSSID(ssid);
                    wifiConfiguration.priority = 40;

                    wifiConfiguration = configuredNetworks.setNetworkSecurity(wifiConfiguration,
                            networkSecurity.getSelectedItemPosition(),
                            "\"" + networkPasswd.getText().toString() + "\"");

                    wifiManager.disconnect();
                    int netId = wifiManager.addNetwork(wifiConfiguration);
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.saveConfiguration();

                    wifiManager.reconnect();

                    String bssid = "";
                    if (bundle != null) {
                        bssid = bundle.getString(Constants.KEY_BSSID);
                    }
                    if (!configuredNetworks.hasNetworkAdditionalData(ssid)) {
                        configuredNetworks.addNetworkData(ssid, bssid, networkDescription.getText().toString(),
                                Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                    } else {
                        configuredNetworks.updateNetworkDescription(ssid, networkDescription.getText().toString());
                    }
                    configuredNetworks.saveDataState();
                } else {
                    Toasts.showNetworkIsConfigured(getContext());
                }

            }
        })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAddNetworkDialogNegativeClick(AddNetworkDialogFragment.this);
                    }
                });


        checkPasswdVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPasswdVisible.isChecked()) {
                    networkPasswd.setTransformationMethod(null);
                    networkPasswd.setSelection(networkPasswd.getText().length());
                } else {
                    networkPasswd.setTransformationMethod(new PasswordTransformationMethod());
                    networkPasswd.setSelection(networkPasswd.getText().length());
                }
            }
        });

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AddNetworkDialogListener so we can send events to the host
            mListener = (AddNetworkDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddNetworkDialogListener");
        }
    }

}
