/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MaterialIconsDialogFragment.java
 *  Last modified : 10/1/20 1:33 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.wifiauthority.R;


public class MaterialIconsDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_about_dialog, null);

        TextView textViewTitle = view.findViewById(R.id.txtTitle);
        TextView textView = view.findViewById(R.id.txtText);

        textViewTitle.setText(getText(R.string.action_material_icons));
        textView.setText(getText(R.string.html_icons_attribution));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}