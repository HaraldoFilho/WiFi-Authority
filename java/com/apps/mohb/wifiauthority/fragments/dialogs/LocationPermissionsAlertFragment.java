/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : LocationPermissionsAlertFragment.java
 *  Last modified : 7/4/17 12:56 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.apps.mohb.wifiauthority.R;


public class LocationPermissionsAlertFragment extends DialogFragment {

    public interface LocationPermissionsDialogListener {
        void onAlertLocationPermDialogPositiveClick(DialogFragment dialog);
        void onAlertLocationPermDialogNegativeClick(DialogFragment dialog);
    }

    private LocationPermissionsDialogListener mListener;


    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.alert_title_warning).setMessage(R.string.alert_message_loc_permission_needed);
        alertDialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onAlertLocationPermDialogPositiveClick(LocationPermissionsAlertFragment.this);
            }
        })
                .setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertLocationPermDialogNegativeClick(LocationPermissionsAlertFragment.this);
                    }
                });

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the LocationPermissionsDialogListener so we can send events to the host
            mListener = (LocationPermissionsDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement LocationPermissionsDialogListener");
        }
    }

}
