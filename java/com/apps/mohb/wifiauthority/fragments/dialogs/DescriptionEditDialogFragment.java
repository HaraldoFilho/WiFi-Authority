/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : DescriptionEditDialogFragment.java
 *  Last modified : 6/26/17 11:15 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.apps.mohb.wifiauthority.Constants;
import com.apps.mohb.wifiauthority.R;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;


public class DescriptionEditDialogFragment extends DialogFragment {

    public interface DescriptionEditDialogListener {
        void onDescriptionEditDialogPositiveClick(DialogFragment dialog);

        void onDescriptionEditDialogNegativeClick(DialogFragment dialog);
    }

    private DescriptionEditDialogListener mListener;
    private ConfiguredNetworks configuredNetworks;
    private EditText text;
    private String networkSSID;
    private LayoutInflater inflater;
    private Bundle bundle;
    View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_description_edit_dialog, null);

        bundle = this.getArguments();
        networkSSID = bundle.getString(Constants.KEY_SSID);

        configuredNetworks = new ConfiguredNetworks(getContext());

        text = (EditText) view.findViewById(R.id.txtEdit);
        if (configuredNetworks.hasNetworkAdditionalData(networkSSID)) {
            text.setText(configuredNetworks.getDescriptionBySSID(networkSSID));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.dialog_edit_description);

        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!configuredNetworks.hasNetworkAdditionalData(networkSSID)) {
                    configuredNetworks.addNetworkData(text.getText().toString(), networkSSID, "", "", "",
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                }
                configuredNetworks.updateNetworkDescription(networkSSID, text.getText().toString());
                configuredNetworks.saveDataState();
                mListener.onDescriptionEditDialogPositiveClick(DescriptionEditDialogFragment.this);
            }
        })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDescriptionEditDialogNegativeClick(DescriptionEditDialogFragment.this);
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DescriptionEditDialogListener so we can send events to the host
            mListener = (DescriptionEditDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DescriptionEditDialogListener");
        }
    }

}
