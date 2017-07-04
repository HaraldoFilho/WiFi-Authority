/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : WifiDisabledAlertFragment.java
 *  Last modified : 7/1/17 1:59 AM
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


public class WifiDisabledAlertFragment extends DialogFragment {

    public interface WifiDisabledDialogListener {
        void onAlertWifiDialogPositiveClick(DialogFragment dialog);

        void onAlertWifiDialogNegativeClick(DialogFragment dialog);
    }

    private WifiDisabledDialogListener mListener;


    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.alert_title_wifi_disabled).setMessage(R.string.alert_message_wifi_disabled)
                .setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertWifiDialogPositiveClick(WifiDisabledAlertFragment.this);
                    }
                })
                .setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertWifiDialogNegativeClick(WifiDisabledAlertFragment.this);
                    }
                });

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the WifiDisabledDialogListener so we can send events to the host
            mListener = (WifiDisabledDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement WifiDisabledDialogListener");
        }
    }

}
