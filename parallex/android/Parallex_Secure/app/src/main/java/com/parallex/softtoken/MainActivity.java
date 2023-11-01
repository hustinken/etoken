/**
 * Copyright (c) 2014 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.entrust.identityGuard.mobile.sdk.Identity;
import com.entrust.identityGuard.mobile.sdk.IdentityProvider;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.google.android.material.navigation.NavigationView;
import com.parallex.softtoken.Activation.EstablishPin;
import com.parallex.softtoken.Activation.RegistrationCode;
import com.parallex.softtoken.Activation.SecurityCode;
import com.parallex.softtoken.Model.ApiModel;
import com.parallex.softtoken.Settings.SettingsActivity;
import com.parallex.softtoken.Utilities.Util;

/**
 * Activity used to add a new soft token identity.
 * Serial number and activation code are collected from the user.
 */
public class MainActivity extends AppCompatActivity {

    // Used when registering for transaction verification.  This can be null
    // if transaction verification is not required.
    private static final String DEVICE_ID = Util.getDeviceId();

    private EditText mSerialNumber, mActivationCode;

    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ApiModel model;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new ApiModel();

        toolbar = findViewById(R.id.toolbar);
        nav = findViewById(R.id.nav_menu);
        drawerLayout = findViewById(R.id.drawer);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        TextView closeToolBar = nav.getHeaderView(0).getRootView().findViewById(R.id.close_toolbar_id);
        closeToolBar.setOnClickListener((v)->{
            drawerLayout.close();
        });

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mSerialNumber = findViewById(R.id.serialNum);
        mActivationCode = findViewById(R.id.actCode);
        Button okButton = findViewById(R.id.ok);

        if(checkIdentityExistOrNot(MainActivity.this)){
            Intent intent = new Intent(MainActivity.this, SecurityCode.class);
            intent.putExtra(SecurityCode.IS_IDENTITY_SAVED,true);
            startActivity(intent);
            finish();
        }

//        mSerialNumber.setText(model.getSerialNo());
//        mActivationCode.setText(model.getActivationNo());

        mSerialNumber.addTextChangedListener(textWatcher);
        mActivationCode.addTextChangedListener(textWatcher);
        okButton.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View view) {
                // Validate both the serial number and the activation code.
                String serialNumber = mSerialNumber.getText().toString();
                String activationCode = mActivationCode.getText().toString();
                if (validateSerialNumber(serialNumber) && validateActivationCode(activationCode)) {
                    // Serial number and activation code are good: Generate an Identity from the IdentityProvider factory class
                    try {
                        Identity identity = new IdentityProvider().generate(DEVICE_ID,
                                serialNumber, activationCode);
                        // Store our identity so it can be accessed by other activities
                        Util.setIdentity(identity);
                        model.setActivationNo(activationCode);
                        model.setSerialNo(serialNumber);
                        // Determine whether the soft token identity requires a PIN to protect it.
                        if (identity.isPINRequired()) {
                            // Our soft token identity requires a PIN
                            startActivity(new Intent(MainActivity.this, EstablishPin.class));
                        } else {
                            // Go directly to showing the user the registration code.
                            startActivity(new Intent(MainActivity.this, RegistrationCode.class));
                        }
                        finish();
                    } catch (Exception e) {
                        // Since we have validated beforehand, this should not happen.
                        Util.showErrorDialog(MainActivity.this, getString(R.string.error_createFailure, e.getMessage()));
                    }
                }
            }

        });
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(mSerialNumber.hasFocus()){
                if(mSerialNumber.getText().toString().length() > 9){
                    validateSerialNumber(mSerialNumber.getText().toString());
                }
            }else{
                if(mActivationCode.getText().toString().length() > 15){
                    validateActivationCode(mActivationCode.getText().toString());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    /**
     * Validate the provided soft token serial number.
     * @param sernum The serial number that needs validating.
     * @return true if the serial number is valid, false otherwise.
     */
    private boolean validateSerialNumber(String sernum) {
        try {
            IdentityProvider.validateSerialNumber(sernum);
            return true;
        } catch (Exception e) {
            // The serial number was not correct; set focus back to the
            // field where it was entered and show an alert dialog.
            Util.showErrorDialog(this, getString(R.string.error_badSerialNum));
            mSerialNumber.requestFocus();
            return false;
        }
    }

    private boolean checkIdentityExistOrNot(Context context){
        return Util.extractIdentityInformation(context);
    }

    /**
     * Validate the provided soft token activation code.
     * @param actcode The activation code that needs validating.
     * @return true if the activation code is valid, false otherwise.
     */
    private boolean validateActivationCode (String actcode) {
        try {
            IdentityProvider.validateActivationCode(actcode);
            return true;
        } catch (Exception e) {
            // The activation code was not correct; set focus back to the
            // field where it was entered and show an alert dialog.
            Util.showErrorDialog(this, getString(R.string.error_badActCode));
            mActivationCode.requestFocus();
            return false;
        }
    }
}