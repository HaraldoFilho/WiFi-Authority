/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : SettingsActivity.java
 *  Last modified : 10/1/20 1:33 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.apps.mohb.wifiauthority.fragments.dialogs.PreferencesResetAlertFragment;
import com.apps.mohb.wifiauthority.networks.ConfiguredNetworks;

import java.util.Objects;


/*
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity implements
        PreferencesResetAlertFragment.PreferencesResetDialogListener {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= Constants.LIST_HEAD
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;

        }

    };


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), Constants.EMPTY));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        // Create settings fragment which actually contain the settings screen
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackPressed();
                break;

            // Reset to defaults
            case R.id.action_defaults:
                DialogFragment alertDialog = new PreferencesResetAlertFragment();
                alertDialog.show(getSupportFragmentManager(), "PreferencesResetAlertFragment");
                break;

            // Help
            case R.id.action_help_settings:
                Intent intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_URL, getString(R.string.url_help_settings));
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.pref_key_header))));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.pref_key_sort))));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.pref_key_security))));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.pref_key_signal))));

            setPreferenceClickListener(Objects.requireNonNull(findPreference(Constants.PREF_KEY_STORE_PASSWORD)));

        }

        // Listen to changes on store passwords setting
        private Preference.OnPreferenceClickListener preferenceClickListener
                = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // If store password settings option was disabled clear all passwords
                if (!preference.getSharedPreferences().getBoolean(Constants.PREF_KEY_STORE_PASSWORD, false)) {
                    ConfiguredNetworks configuredNetworks = new ConfiguredNetworks(requireActivity().getApplicationContext());
                    configuredNetworks.clearAllPasswords();
                }
                return true;
            }
        };

        private void setPreferenceClickListener(Preference preference) {
            preference.setOnPreferenceClickListener(preferenceClickListener);
        }

    }


    // RESET TO DEFAULTS DIALOG

    @Override // Yes
    public void onAlertDialogPositiveClick(DialogFragment dialog) {
        // Clear settings on memory
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        // Set defaults on memory
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Update settings screen with the default values
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    @Override // No
    public void onAlertDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }

}
