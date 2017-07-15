/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : PasswordChangeDialogFragment.java
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

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;


public class PasswordChangeDialogFragment extends DialogFragment {

    public interface PasswordChangeDialogListener {
        void onPasswordChangeDialogPositiveClick(DialogFragment dialog);

        void onPasswordChangeDialogNegativeClick(DialogFragment dialog);
    }

    private PasswordChangeDialogListener mListener;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private ConfiguredNetworks configuredNetworks;
    private EditText networkPasswd;
    private int networkId;
    private String networkSSID;
    private String networkSecurity;
    private LayoutInflater inflater;
    private CheckBox checkPasswdVisible;
    private Bundle bundle;
    View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_password_change_dialog, null);

        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(getContext().WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        configuredNetworks = new ConfiguredNetworks(getContext());
        checkPasswdVisible = (CheckBox) view.findViewById(R.id.checkPasswdChange);

        bundle = this.getArguments();
        networkId = bundle.getInt(Constants.KEY_NETWORK_ID);
        networkSSID = bundle.getString(Constants.KEY_SSID);
        networkSecurity = bundle.getString(Constants.KEY_SECURITY);

        networkPasswd = (EditText) view.findViewById(R.id.txtChangePasswd);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.dialog_change_password);

        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!configuredNetworks.hasNetworkAdditionalData(networkSSID)) {
                    configuredNetworks.addNetworkData("", networkSSID, networkPasswd.getText().toString(), "", "",
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                }
                wifiConfiguration.networkId = networkId;

                String password = "\"" + networkPasswd.getText().toString() + "\"";

                if (networkSecurity.contains(Constants.KEY_NONE_WEP)) {
                    wifiConfiguration.wepKeys[0] = password;
                } else {
                    wifiConfiguration.preSharedKey = password;
                }

                wifiManager.disconnect();
                wifiManager.updateNetwork(wifiConfiguration);
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();

                configuredNetworks.setPassword(networkSSID, networkPasswd.getText().toString());
                configuredNetworks.saveDataState();
                mListener.onPasswordChangeDialogPositiveClick(PasswordChangeDialogFragment.this);
            }
        })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPasswordChangeDialogNegativeClick(PasswordChangeDialogFragment.this);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the PasswordChangeDialogListener so we can send events to the host
            mListener = (PasswordChangeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement PasswordChangeDialogListener");
        }
    }

}
