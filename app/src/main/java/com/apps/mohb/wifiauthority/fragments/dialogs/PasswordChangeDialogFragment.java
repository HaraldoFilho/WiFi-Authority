/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : PasswordChangeDialogFragment.java
 *  Last modified : 8/19/17 11:21 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.Toasts;
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
    private boolean networkIsHidden;
    private String networkSecurity;
    private CheckBox checkPasswdVisible;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_password_change_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle bundle = this.getArguments();

        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(getContext().WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        configuredNetworks = new ConfiguredNetworks(getContext());

        networkPasswd = (EditText) view.findViewById(R.id.txtChangePasswd);
        checkPasswdVisible = (CheckBox) view.findViewById(R.id.checkPasswdChange);

        networkId = bundle.getInt(Constants.KEY_NETWORK_ID);
        networkSSID = bundle.getString(Constants.KEY_SSID);
        networkIsHidden = bundle.getBoolean(Constants.KEY_HIDDEN);
        networkSecurity = bundle.getString(Constants.KEY_SECURITY);

        builder.setView(view);
        builder.setTitle(R.string.dialog_change_password);

        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String password = configuredNetworks.getCfgPassword(networkPasswd.getText().toString());

                if (!configuredNetworks.hasNetworkAdditionalData(networkSSID)) {
                    configuredNetworks.addNetworkData(Constants.EMPTY, networkSSID, networkIsHidden,
                            networkPasswd.getText().toString(), networkSecurity, Constants.EMPTY,
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                }

                wifiConfiguration.networkId = networkId;

                if (configuredNetworks.getNetworkSecurity(networkSecurity) == Constants.SET_WEP) {
                    wifiConfiguration.wepKeys[Constants.WEP_PASSWORD_KEY_INDEX] = password;
                } else {
                    wifiConfiguration.preSharedKey = password;
                }

                wifiManager.disconnect();

                int netId = wifiManager.updateNetwork(wifiConfiguration);

                if (netId != Constants.NETWORK_UPDATE_FAIL) {

                    wifiManager.enableNetwork(networkId, true);
                    wifiManager.reconnect();

                    configuredNetworks.setPassword(networkSSID, networkPasswd.getText().toString());
                    configuredNetworks.saveDataState();

                } else {

                    // Shared preferences variable to control if Network Management Policy Dialog for Marshmallow (version 6.x)
                    // or higher must be shown
                    SharedPreferences showNetworkManagementPolicyWarnPref = getContext()
                            .getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

                    // If version is Marshmallow (6.x) or higher show dialog explaining new networks manage,ent policy
                    if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M))
                            && (showNetworkManagementPolicyWarnPref.getBoolean(Constants.NET_MNG_POLICY_WARN, true))) {
                        DialogFragment dialogPolicy = new NetworkManagementPolicyAlertFragment();
                        dialogPolicy.show(getFragmentManager(), "NetworkManagementPolicyAlertFragment");
                    } else {
                        Toasts.showUnableToChangePassword(getContext());
                    }

                }

                mListener.onPasswordChangeDialogPositiveClick(PasswordChangeDialogFragment.this);

            }
        }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
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

        networkPasswd.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // Required empty method
            }
        });

        networkPasswd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Required empty method
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Required empty method
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (configuredNetworks.isValidPassword(configuredNetworks.getNetworkSecurity(networkSecurity),
                        networkPasswd.getText().toString())) {
                    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                } else {
                    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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
