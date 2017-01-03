/*
 *  Copyright (c) 2017 mohb apps - All Rights Reserved
 *
 *  Project       : WiFiAuthority
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : SplashActivity.java
 *  Last modified : 12/28/16 4:55 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.wifiauthority;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Shows mohb logo while app is loading and calls WiFiCheckActivity,
        // which will be responsible to call MainActivity, when finished
        Intent intent = new Intent(this, WiFiCheckActivity.class);
        intent.putExtra(Constants.KEY_ACTIVITY, Constants.ACTIVITY_MAIN);
        startActivityForResult(intent, Constants.ACTIVITY_MAIN);
        finish();

    }

}
