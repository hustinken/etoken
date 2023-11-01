/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Settings;
import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.parallex.softtoken.Authentication.ChangePin;
import com.parallex.softtoken.Authentication.CreatePIN;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

import java.util.Objects;


/**
 * Activity that displays the ever changing security code (i.e., one-time password).
 *
 */
public class SettingsActivity extends AppCompatActivity {

    /** Called when the activity is first created. */
    CheckBox fingerPrintCheckbox, pinLockCheckbox;
    LinearLayout fingerPrintBtn, pinLockBtn, changePin;
    Spinner spinnerItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fingerPrintCheckbox = findViewById(R.id.finger_print);
        pinLockCheckbox = findViewById(R.id.pin_lock);
        fingerPrintBtn = findViewById(R.id.finger_print_btn);
        pinLockBtn = findViewById(R.id.pin_lock_btn);
        spinnerItem = findViewById(R.id.timer_spinner);

        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Util.setTimerForLockScreen(SettingsActivity.this, spinnerItem.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fingerPrintBtn.setOnClickListener(buttonClicked);
        pinLockBtn.setOnClickListener(buttonClicked);

        loadConfig();
    }


    void loadConfig(){
        pinLockCheckbox.setChecked(false);
        fingerPrintCheckbox.setChecked(false);

        //Set pin checkbox
        if(Util.isPinRequired(SettingsActivity.this)){
            pinLockCheckbox.setChecked(true);
        }

        //Set fingerprint checkbox
        if(Util.isFingerPrintRequired(SettingsActivity.this)){
            if(pinLockCheckbox.isChecked()){
                fingerPrintCheckbox.setChecked(true);
            }
        }

        //Set spinner item
        for(int i=0; i<spinnerItem.getAdapter().getCount(); i++){
            String item = spinnerItem.getAdapter().getItem(i).toString();
            String storedItem = Util.getTimerForLockScreen(SettingsActivity.this);
            if(item.equals(storedItem)){
                spinnerItem.setSelection(i);
                break;
            }
        }
    }

    View.OnClickListener buttonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        if(Util.getPin(SettingsActivity.this).equals("")){
            startActivity(new Intent(SettingsActivity.this, CreatePIN.class));
            return;
        }
        switch (view.getId()){
            case R.id.finger_print_btn:
                if(!checkBiometricsSupport()) return;
                if(!pinLockCheckbox.isChecked()) return;

                if(fingerPrintCheckbox.isChecked()){
                    Util.setFingerPrintRequired(SettingsActivity.this, false);
                    fingerPrintCheckbox.setChecked(false);
                }else{
                    Util.setFingerPrintRequired(SettingsActivity.this, true);
                    fingerPrintCheckbox.setChecked(true);
                }
                break;
            case R.id.pin_lock_btn:
                if(pinLockCheckbox.isChecked()){
                    Util.setPinRequired(SettingsActivity.this, false);
                    Util.setFingerPrintRequired(SettingsActivity.this, false);
                    pinLockCheckbox.setChecked(false);
                    fingerPrintCheckbox.setChecked(false);
                }else{
                    Util.setPinRequired(SettingsActivity.this, true);
                    pinLockCheckbox.setChecked(true);
                }
                break;
        }
        }
    };

    public void onChangePinBtnClicked(View view){
        if(!Util.getPin(SettingsActivity.this).equals("")){
            Intent intent = new Intent(SettingsActivity.this, ChangePin.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean checkBiometricsSupport() {
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if(!keyguardManager.isKeyguardSecure()){
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint authentication permission is not enabled");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        }
        return true;
    }

    private void notifyUser(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
