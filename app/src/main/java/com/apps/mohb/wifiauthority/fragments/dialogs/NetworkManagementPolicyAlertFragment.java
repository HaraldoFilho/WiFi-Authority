/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : NetworkManagementPolicyAlertFragment.java
 *  Last modified : 10/1/20 1:33 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.wifiauthority.R;


public class NetworkManagementPolicyAlertFragment extends DialogFragment {

    public interface NetworkManagementPolicyDialogListener {
        void onAlertNetworkManagementPolicyDialogPositiveClick(DialogFragment dialog);

        void onAlertNetworkManagementPolicyDialogNeutralClick(DialogFragment dialog);

        void onAlertNetworkManagementPolicyDialogNegativeClick(DialogFragment dialog);
    }

    private NetworkManagementPolicyDialogListener mListener;


    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.alert_title_warning).setMessage(R.string.alert_message_network_mng_policy_change)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertNetworkManagementPolicyDialogPositiveClick(NetworkManagementPolicyAlertFragment.this);
                    }
                })
                .setNeutralButton(R.string.alert_button_tell_more, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertNetworkManagementPolicyDialogNeutralClick(NetworkManagementPolicyAlertFragment.this);
                    }
                })
                .setNegativeButton(R.string.alert_button_not_warn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAlertNetworkManagementPolicyDialogNegativeClick(NetworkManagementPolicyAlertFragment.this);
                    }
                });

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NetworkManagementPolicyDialogListener so we can send events to the host
            mListener = (NetworkManagementPolicyDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NetworkManagementPolicyDialogListener");
        }
    }

}
