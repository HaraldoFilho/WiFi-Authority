/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : LegalNoticesDialogFragment.java
 *  Last modified : 6/29/17 11:53 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apps.mohb.wifiauthority.R;


public class LegalNoticesDialogFragment extends DialogFragment {

    public static LegalNoticesDialogFragment newInstance(String notices) {

        LegalNoticesDialogFragment legalNoticesDialogFragment = new LegalNoticesDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("notices", notices);
        legalNoticesDialogFragment.setArguments(bundle);

        return legalNoticesDialogFragment;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String legalNotices = getArguments().getString("notices");

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_about_dialog, null);

        TextView textViewTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView textView = (TextView) view.findViewById(R.id.txtText);

        textViewTitle.setText(getText(R.string.action_legal_notices));
        textView.setText(legalNotices);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}