/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : PrivacyPolicyDialogFragment.java
 *  Last modified : 7/4/17 12:56 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apps.mohb.wifiauthority.R;


public class PrivacyPolicyDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_about_dialog, null);

        TextView textViewTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView textView = (TextView) view.findViewById(R.id.txtText);

        textViewTitle.setText(getText(R.string.action_privacy_policy));
        textView.setText(getText(R.string.html_privacy_policy));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

}