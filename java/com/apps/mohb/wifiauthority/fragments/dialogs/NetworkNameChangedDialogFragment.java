/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : NetworkNameChangedDialogFragment.java
 *  Last modified : 7/4/17 12:56 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.Toasts;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;


public class NetworkNameChangedDialogFragment extends DialogFragment {

    public interface NetworkNameChangedDialogListener {
        void onNetworkNameChangedDialogPositiveClick(DialogFragment dialog);

        void onNetworkNameChangedDialogNegativeClick(DialogFragment dialog);
    }

    private NetworkNameChangedDialogListener mListener;

    private TextView networkDescription;
    private TextView networkOldName;
    private TextView networkNewName;
    private EditText networkPasswd;
    private CheckBox checkPasswdVisible;
    private CheckBox checkConnect;


    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private ConfiguredNetworks configuredNetworks;

    private String mac;
    private String oldSSID;
    private String newSSID;
    private String description;
    private String security;

    private boolean connect;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_ssid_changed, null);

        networkDescription = (TextView) view.findViewById(R.id.txtNetSubtitle);
        networkOldName = (TextView) view.findViewById(R.id.txtOldName);
        networkNewName = (TextView) view.findViewById(R.id.txtNewName);
        networkPasswd = (EditText) view.findViewById(R.id.txtNewPassword);
        checkPasswdVisible = (CheckBox) view.findViewById(R.id.checkNewPasswd);
        checkConnect = (CheckBox) view.findViewById(R.id.checkConnect);

        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        configuredNetworks = new ConfiguredNetworks(getContext());
        connect = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final Bundle bundle = this.getArguments();

        if (bundle != null) {
            networkDescription.setText(bundle.getString(Constants.KEY_DESCRIPTION));
            networkOldName.setText(":   " + bundle.getString(Constants.KEY_OLD_NAME));
            networkNewName.setText(":   " + bundle.getString(Constants.KEY_NEW_NAME));

            mac = bundle.getString(Constants.KEY_BSSID);
            oldSSID = configuredNetworks.getDataSSID(bundle.getString(Constants.KEY_OLD_NAME));
            newSSID = configuredNetworks.getDataSSID(bundle.getString(Constants.KEY_NEW_NAME));
            description = bundle.getString(Constants.KEY_DESCRIPTION);
            security = bundle.getString(Constants.KEY_SECURITY);

            wifiConfiguration = configuredNetworks.setNetworkCiphers(wifiConfiguration, security);

            builder.setTitle(R.string.dialog_network_name_changed_title);

        }

        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onNetworkNameChangedDialogPositiveClick(NetworkNameChangedDialogFragment.this);

                if (!configuredNetworks.isConfiguredBySSID(wifiManager.getConfiguredNetworks(), newSSID)) {

                    wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
                    wifiConfiguration.SSID = configuredNetworks.getCfgSSID(newSSID);
                    wifiConfiguration.priority = 40;

                    wifiConfiguration = configuredNetworks.setNetworkSecurity(wifiConfiguration,
                            configuredNetworks.getNetworkSecurity(security),
                            "\"" + networkPasswd.getText().toString() + "\"");

                    wifiManager.disconnect();

                    int netId = wifiManager.addNetwork(wifiConfiguration);

                    if (connect) {
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
                    } else {
                        wifiManager.disableNetwork(netId);
                    }

                    wifiManager.saveConfiguration();

                    configuredNetworks.removeNetworkData(oldSSID);
                    configuredNetworks.addNetworkData(description, newSSID, mac, security, "",
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                    configuredNetworks.saveDataState();
                } else {
                    Toasts.showNetworkIsConfigured(getContext());
                }

            }
        }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onNetworkNameChangedDialogNegativeClick(NetworkNameChangedDialogFragment.this);
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

        checkConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnect.isChecked()) {
                    connect = true;
                } else {
                    connect = false;
                }
            }
        });

        return builder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NetworkNameChangedDialogListener so we can send events to the host
            mListener = (NetworkNameChangedDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NetworkNameChangedDialogListener");
        }
    }

}
