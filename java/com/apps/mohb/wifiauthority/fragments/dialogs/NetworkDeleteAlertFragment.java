/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : NetworkDeleteAlertFragment.java
 *  Last modified : 6/29/17 11:53 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.apps.mohb.wifiauthority.R;


public class NetworkDeleteAlertFragment extends DialogFragment {

    public interface NetworkDeleteDialogListener {
        void onNetworkDeleteDialogPositiveClick(DialogFragment dialog);

        void onNetworkDeleteDialogNegativeClick(DialogFragment dialog);
    }

    private NetworkDeleteDialogListener mListener;


    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alert_title_are_you_sure).setMessage(R.string.alert_message_delete_network)
                .setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNetworkDeleteDialogPositiveClick(NetworkDeleteAlertFragment.this);
                    }
                })
                .setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNetworkDeleteDialogNegativeClick(NetworkDeleteAlertFragment.this);
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NetworkDeleteDialogListener so we can send events to the host
            mListener = (NetworkDeleteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NetworkDeleteDialogListener");
        }
    }

}