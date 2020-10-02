/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : AddNetworkDialogFragment.java
 *  Last modified : 10/1/20 9:43 PM
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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.Toasts;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;

import java.util.Objects;


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

    private String ssid;
    private String bssid;
    private boolean hidden = false;
    private String security = Constants.EMPTY;
    private int securityOption;
    private String password = Constants.EMPTY;
    private double lastLatitude;
    private double lastLongitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_network_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);
        final Bundle bundle = this.getArguments();

        wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        configuredNetworks = new ConfiguredNetworks(getContext());

        networkName = view.findViewById(R.id.txtSSID);
        networkSecurity = view.findViewById(R.id.spinSecurity);
        networkPasswd = view.findViewById(R.id.txtPassword);
        checkPasswdVisible = view.findViewById(R.id.checkPasswd);
        networkDescription = view.findViewById(R.id.txtDescription);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.security_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSecurity.setAdapter(adapter);

        if (bundle != null) {
            ssid = bundle.getString(Constants.KEY_SSID);
            bssid = bundle.getString(Constants.KEY_BSSID);
            security = bundle.getString(Constants.KEY_SECURITY);
            lastLatitude = bundle.getDouble(Constants.KEY_LATITUDE);
            lastLongitude = bundle.getDouble(Constants.KEY_LONGITUDE);
            networkName.setText(ssid);
            networkName.setEnabled(false);

            if (configuredNetworks.getNetworkSecurity(security) == Constants.SET_OPEN) {
                networkPasswd.setEnabled(false);
                checkPasswdVisible.setEnabled(false);
            } else {
                networkSecurity.setSelection(configuredNetworks.getNetworkSecurity(security));
            }

            networkSecurity.setEnabled(false);

            wifiConfiguration = configuredNetworks.setNetworkCiphers(wifiConfiguration, security);

            builder.setTitle(R.string.dialog_add_network_title);

        } else {
            bssid = Constants.EMPTY;
            lastLongitude = Constants.DEFAULT_LATITUDE;
            lastLongitude = Constants.DEFAULT_LONGITUDE;
            builder.setTitle(R.string.dialog_add_hidden_network_title);
        }

        networkSecurity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (configuredNetworks.isValidPassword(networkSecurity.getSelectedItemPosition(),
                        networkPasswd.getText().toString())) {
                    ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                } else {
                    ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                if (networkSecurity.getLastVisiblePosition() == Constants.SET_OPEN) {
                    networkPasswd.setEnabled(false);
                    checkPasswdVisible.setEnabled(false);
                } else {
                    networkPasswd.setEnabled(true);
                    checkPasswdVisible.setEnabled(true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Required empty method
            }
        });

        networkPasswd.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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

                if (configuredNetworks.isValidPassword(networkSecurity.getSelectedItemPosition(),
                        networkPasswd.getText().toString())) {
                    ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                } else {
                    ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

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


        builder.setPositiveButton(R.string.dialog_button_connect, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onAddNetworkDialogPositiveClick(AddNetworkDialogFragment.this);

                ssid = networkName.getText().toString();
                password = networkPasswd.getText().toString();
                securityOption = networkSecurity.getSelectedItemPosition();

                boolean networkIsConfiguredBySSID = false;
                try {
                    networkIsConfiguredBySSID = configuredNetworks
                            .isConfiguredBySSID(wifiManager.getConfiguredNetworks(), ssid);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                if (!networkIsConfiguredBySSID) {

                    wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
                    wifiConfiguration.SSID = configuredNetworks.getCfgSSID(ssid);
                    wifiConfiguration.priority = Constants.CFG_PRIORITY;

                    wifiConfiguration = configuredNetworks.setNetworkSecurity(wifiConfiguration,
                            securityOption, configuredNetworks.getCfgPassword(password));

                    // Set network as hidden if configured ssid is not found
                    if (!configuredNetworks.isAvailableBySSID(wifiManager.getScanResults(), ssid)) {
                        hidden = true;
                        wifiConfiguration.hiddenSSID = true;
                    } else {
                        hidden = false;
                        wifiConfiguration.hiddenSSID = false;
                    }

                    wifiManager.disconnect();

                    int netId = wifiManager.addNetwork(wifiConfiguration);

                    if (netId != Constants.NETWORK_UPDATE_FAIL) {

                        String description = networkDescription.getText().toString();

                        // If adding a hidden network, there is no bundle to get security info from it,
                        // so get capabilities string from configuration
                        if (bundle == null) {
                            security = configuredNetworks.getCapabilities(wifiConfiguration);
                        }

                        if (!configuredNetworks.hasNetworkAdditionalData(ssid)) {
                            configuredNetworks.addNetworkData(description, ssid, hidden, bssid, security, password,
                                    lastLatitude, lastLongitude);
                        } else {
                            configuredNetworks.updateNetworkDescription(ssid, description);
                        }
                        configuredNetworks.saveDataState();

                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();

                    } else {
                        Toasts.showUnableAddNetwork(getContext());
                    }
                } else {
                    Toasts.showNetworkIsConfigured(getContext());
                }

            }

        }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onAddNetworkDialogNegativeClick(AddNetworkDialogFragment.this);
            }
        });

        return builder.create();

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AddNetworkDialogListener so we can send events to the host
            mListener = (AddNetworkDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AddNetworkDialogListener");
        }
    }

}

