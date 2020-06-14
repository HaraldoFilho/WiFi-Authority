/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : AboutActivity.java
 *  Last modified : 6/14/20 2:01 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.apps.mohb.wifiauthority.fragments.dialogs.MaterialIconsDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.PrivacyPolicyDialogFragment;
import com.apps.mohb.wifiauthority.fragments.dialogs.TermsOfUseDialogFragment;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // displays app version number
        TextView version = (TextView) findViewById(R.id.textAppVersion);
        version.setText(getString(R.string.version_name) + " " + getString(R.string.version_number));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        DialogFragment dialog;
        Intent intent;
        Bundle bundle;

        switch (id) {

            // Feedback
            case R.id.action_feedback:
                String[] feedback_address = new String[Constants.QUESTION_ARRAY_SIZE];
                feedback_address[Constants.LIST_HEAD] = getString(R.string.info_feedback_email);
                composeEmail(feedback_address, getString(R.string.action_feedback)
                        + " " + getString(R.string.info_app_name));
                break;

            // Bug report
            case R.id.action_bug_report:
                String[] bug_address = new String[Constants.QUESTION_ARRAY_SIZE];
                bug_address[Constants.LIST_HEAD] = getString(R.string.info_bug_email);
                composeEmail(bug_address, getString(R.string.info_bug_email)
                        + " " + getString(R.string.info_app_name));
                break;

            // Terms of use
            case R.id.action_terms_of_use:
                dialog = new TermsOfUseDialogFragment();
                dialog.show(getSupportFragmentManager(), "TermsOfUseDialogFragment");
                break;

            // Privacy policy
            case R.id.action_privacy_policy:
                dialog = new PrivacyPolicyDialogFragment();
                dialog.show(getSupportFragmentManager(), "PrivacyPolicyDialogFragment");
                break;

            /* ##### DEPRECATED #####
            // Legal notices
            case R.id.action_legal_notices:
                new GetLegalNotices().execute();
                break;
            */

            // Icons attribution
            case R.id.action_material_icons:
                dialog = new MaterialIconsDialogFragment();
                dialog.show(getSupportFragmentManager(), "MaterialIconsDialogFragment");
                break;

        }

        return super.onOptionsItemSelected(item);

    }


    // CLASS METHOD

    /*
         Compose a e-mail to send a question
    */
    private void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(Constants.KEY_EMAIL)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
