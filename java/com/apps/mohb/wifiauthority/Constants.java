/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Constants.java
 *  Last modified : 8/7/17 4:49 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;


public class Constants {

    // App Shared Preferences
    public static final int PRIVATE_MODE = 0;
    public static final String PREF_NAME = "WiFiAuthority";
    public static final String NET_MNG_POLICY_WARN = "NET_MNG_POLICY_WARN";

    // Permissions request
    public static final int FINE_LOCATION_PERMISSION_REQUEST = 0;

    // Lists Saved Preferences
    public static final String DATA = "Data";
    public static final String JSON_DESCRIPTION = "description";
    public static final String JSON_SSID = "ssid";
    public static final String JSON_HIDDEN = "hidden";
    public static final String JSON_BSSID = "bssid";
    public static final String JSON_SECURITY = "security";
    public static final String JSON_FREQUENCY = "frequency";
    public static final String JSON_PASSWORD = "password";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";

    // Network data
    public static final String KEY_NETWORK_ID = "NETWORK_ID";
    public static final String KEY_DESCRIPTION = "DESCRIPTION";
    public static final String KEY_SSID = "SSID";
    public static final String KEY_HIDDEN = "HIDDEN";
    public static final String KEY_BSSID = "BSSID";
    public static final String KEY_SECURITY = "SECURITY";
    public static final String KEY_FREQUENCY = "FREQUENCY";
    public static final String KEY_PASSWORD = "PASSWORD";
    public static final String KEY_LATITUDE = "LATITUDE";
    public static final String KEY_LONGITUDE = "LONGITUDE";
    public static final String KEY_OLD_NAME = "OLD_SSID";
    public static final String KEY_NEW_NAME = "NEW_SSID";

    // Network security
    public static final String KEY_NONE_WEP = "{0}";
    public static final String KEY_WPA = "{1}";
    public static final String KEY_WPA2 = "{2}";
    public static final String KEY_EAP = "{3}";

    public static final String SCAN_WPA = "WPA";
    public static final String SCAN_EAP = "IEEE8021X";
    public static final String SCAN_WEP = "WEP";

    public static final int SET_OPEN = 0;
    public static final int SET_WEP = 1;
    public static final int SET_WPA = 2;
    public static final int SET_EAP = 3;

    public static final String CFG_CCMP = "CCMP";
    public static final String CFG_TKIP = "TKIP";

    public static final String DUMMY_PASSWORD = "--------";

    // Lists
    public static final int LIST_HEAD = 0;
    public static final int LIST_HEADER_POSITION = 1;

    // Feedback and question
    public static final int QUESTION_ARRAY_SIZE = 1;

    // Toasts
    public static final int TOAST_X_OFFSET = 0;
    public static final int TOAST_Y_OFFSET = 0;

    // Networks signal levels
    public static final int LEVELS = 3;
    public static final int LEVEL_HIGH = 2;
    public static final int LEVEL_LOW = 1;
    public static final int LEVEL_VERY_LOW = 0;

    public static final int OUT_OF_REACH = -1000;

    // Network frequency
    public static final int FREQ_5GHZ = 5000;
    public static final int HEIGHT_ZERO = 0;
    public static final int NO_FREQ_SET = 0;

    // FAB alpha
    public static final float FAB_OPAQUE = (float) 1.0;
    public static final float FAB_TRANSLUCENT = (float) 0.8;

    // Network updated result failed code
    public static final int NETWORK_UPDATE_FAIL = -1;

    // Map
    public static final int MAP_DETAILS_ZOOM_LEVEL = 15;
    public static final double DEFAULT_LATITUDE = 0;
    public static final double DEFAULT_LONGITUDE = 0;
    public static final double MIN_LATITUDE = -90;
    public static final double MAX_LATITUDE = 90;
    public static final double MIN_LONGITUDE = -180;
    public static final double MAX_LONGITUDE = 180;

    // Settings
    public static final String PREF_HEADER_DESCRIPTION = "1";

    public static final String PREF_SORT_AUTO = "1";
    public static final String PREF_SORT_DESCRIPTION = "2";
    public static final String PREF_SORT_NAME = "3";

    public static final String PREF_KEY_RECONFIG = "SSID_RECONFIG";
    public static final String PREF_KEY_STORE_PASSWORD = "STORE_PASSWORD";
    public static final String PREF_KEY_SHOW_ALL_APS = "SHOW_ALL_APS";

    public static final String PREF_SECURITY_WPA_EAP = "3";
    public static final String PREF_SECURITY_WEP = "2";
    public static final String PREF_SECURITY_OPEN = "1";
    ;
    public static final String PREF_MIN_SIGNAL_HIGH = "3";
    public static final String PREF_MIN_SIGNAL_LOW = "2";
    public static final String PREF_MIN_SIGNAL_VERY_LOW = "1";

}