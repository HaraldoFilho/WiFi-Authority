/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Toasts.java
 *  Last modified : 10/1/20 9:31 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


/*
    This class manages all the toasts in the application
*/

public class Toasts {

    private static Toast unableAddNetwork;
    private static Toast unableRemoveNetwork;
    private static Toast unableToChangePassword;
    private static Toast noDetailedInformation;
    private static Toast networkIsConfigured;
    private static Toast networkConnectionError;
    private static Toast noNetworkFound;

    /*
         Toast to notify that there is no location information
    */

    public static void showNoDetailedInformation(Context context, int textId) {
        noDetailedInformation = Toast.makeText((context), textId, Toast.LENGTH_SHORT);
        noDetailedInformation.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        noDetailedInformation.show();
    }

    public static void cancelNoDetailedInformation() {
        if (noDetailedInformation != null) {
            noDetailedInformation.cancel();
        }
    }


    /*
         Toast to notify that it is unable to add network
    */

    public static void showUnableAddNetwork(Context context) {
        unableAddNetwork = Toast.makeText((context), R.string.toast_unable_add_network, Toast.LENGTH_LONG);
        unableAddNetwork.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        unableAddNetwork.show();
    }

    public static void cancelUnableAddNetwork() {
        if (unableAddNetwork != null) {
            unableAddNetwork.cancel();
        }
    }

    /*
         Toast to notify that it is unable to remove network
    */

    public static void showUnableRemoveNetwork(Context context) {
        unableRemoveNetwork = Toast.makeText((context), R.string.toast_unable_remove_network, Toast.LENGTH_SHORT);
        unableRemoveNetwork.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        unableRemoveNetwork.show();
    }

    public static void cancelUnableRemoveNetwork() {
        if (unableRemoveNetwork != null) {
            unableRemoveNetwork.cancel();
        }
    }


    /*
         Toast to notify that it is unable to change password
    */

    public static void showUnableToChangePassword(Context context) {
        unableToChangePassword = Toast.makeText((context), R.string.toast_unable_to_change_password, Toast.LENGTH_SHORT);
        unableToChangePassword.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        unableToChangePassword.show();
    }

    public static void cancelUnableToChangePassword() {
        if (unableToChangePassword != null) {
            unableToChangePassword.cancel();
        }
    }


    /*
         Toast to notify that there is no network found or to display
    */

    public static void showNoNetworkFound(Context context, int textId) {
        noNetworkFound = Toast.makeText((context), textId, Toast.LENGTH_SHORT);
        noNetworkFound.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        noNetworkFound.show();
    }

    public static void cancelNoNetworkFound() {
        if (noNetworkFound != null) {
            noNetworkFound.cancel();
        }
    }


    /*
         Toast to notify that network is already configured
    */

    public static void showNetworkIsConfigured(Context context) {
        networkIsConfigured = Toast.makeText((context), R.string.toast_network_is_configured, Toast.LENGTH_SHORT);
        networkIsConfigured.setGravity(Gravity.BOTTOM, Constants.TOAST_X_OFFSET, Constants.TOAST_Y_OFFSET);
        networkIsConfigured.show();
    }

    public static void cancelNetworkIsConfigured() {
        if (networkIsConfigured != null) {
            networkIsConfigured.cancel();
        }
    }


    /*
         Toast to notify a network connection error
    */

    public static void showNetworkConnectionError(Context context, int textId) {
        networkConnectionError = Toast.makeText((context), textId, Toast.LENGTH_SHORT);
        networkConnectionError.show();
    }

    public static void cancelNetworkConnectionError() {
        if (networkConnectionError != null) {
            networkConnectionError.cancel();
        }
    }


    /*
         Cancel all toasts
    */

    public static void cancelAllToasts() {
        cancelUnableAddNetwork();
        cancelUnableRemoveNetwork();
        cancelUnableToChangePassword();
        cancelNetworkIsConfigured();
        cancelNetworkConnectionError();
        cancelNoNetworkFound();
        cancelNoDetailedInformation();
    }

}
