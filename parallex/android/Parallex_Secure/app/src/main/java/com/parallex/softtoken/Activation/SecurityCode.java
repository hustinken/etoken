/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Activation;

import java.util.Timer;
import java.util.TimerTask;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.entrust.identityGuard.mobile.sdk.Identity;
import com.entrust.identityGuard.mobile.sdk.IdentityProvider;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Settings.SettingsActivity;
import com.parallex.softtoken.Utilities.Util;
import com.entrust.identityGuard.mobile.sdk.exception.IdentityGuardMobileException;
import com.google.android.material.navigation.NavigationView;


/**
 * Activity that displays the ever changing security code (i.e., one-time password).
 *
 */
public class SecurityCode extends AppCompatActivity {
    private static final String LOG_COMPONENT = SecurityCode.class.getName();

    // The length of time (in seconds) the current security code
    // should be displayed before a new one is generated.
    private static final int OTP_VALIDITY_TIME = 30;
    public static final String IS_IDENTITY_SAVED ="IdentitySaved";


    private ProgressBar mCountdown;
    private TextView progressText;
    private TextView mOTP;
    private Timer mTimer;
    private final Handler mHandler = new Handler();
    private final Identity mIdentity = Util.getIdentity();
    private Context context;

    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView serialNo;
    Button copyBtn;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_code);
        context=this;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        serialNo = findViewById(R.id.serial_num_id);
        nav = findViewById(R.id.nav_menu);
        copyBtn = findViewById(R.id.copy_btn_id);
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        serialNo.setText(Util.getIdentity().getSerialNumber());

        TextView closeToolBar = nav.getHeaderView(0).getRootView().findViewById(R.id.close_toolbar_id);
        closeToolBar.setOnClickListener((v)->{
            drawerLayout.close();
        });

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_settings:
                        startActivity(new Intent(SecurityCode.this, SettingsActivity.class));
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        copyBtn.setOnClickListener((v)->{
            copy();
        });

        //If Identity tag exist in extras then create identity if not then save newly created Identity in Preference.
        if(!getIntent().hasExtra(IS_IDENTITY_SAVED)) {
            Util.saveIdentityInformation(context);
        }

        // Display the security code (OTP) value and a progress bar that represents
        // a countdown until a new security code needs to be generated (i.e., the
        // OTP's lifetime). Use the available IdentityProvider formatCode() helper method
        // to split the OTP into two with a separating hyphen.
        mOTP = findViewById(R.id.otp);
        String otp = "";
        for (int i = 0; i < mIdentity.getOTPLength(); i++) {
            otp += "0";
        }



        try {
            otp = mIdentity.getOTP();
        }
        catch (IdentityGuardMobileException e) {
            Log.e(LOG_COMPONENT, "Failed to get the current OTP", e);
        }
        finally {
            mOTP.setText(IdentityProvider.formatCode(otp, mIdentity.getOTPLength() / 2, '-'));
        }



        mCountdown = findViewById(R.id.countdown);
        progressText = findViewById(R.id.progress_text);
        // Set up a fixed rate timer that counts down until a new security code should be generated.

        mTimer = new Timer();
        //recyclerView = (RecyclerView) findViewById(R.id.PendingTransactionsList);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            private int ticks = OTP_VALIDITY_TIME;

            /**
             * Starting after a second (1000ms) and for every second thereafter,
             * decrement the countdown timer by one, and if we're reached zero,
             * generate a new security code and reset our countdown display.
             */
            @Override
            public void run() {
                // Since we're changing the GUI, this should happen via a worker thread
                mHandler.post(new Runnable() {

                    /**
                     * Countdown a tick, and if we've reached zero, generate a new OTP.
                     * Always update the value of the gauge to represent the current stage
                     * of our countdown.
                     */
                    @Override
                    public void run() {
                        if (--ticks == 0) {
                            ticks = OTP_VALIDITY_TIME;

                            String otp = "";
                            for (int i = 0; i < mIdentity.getOTPLength(); i++) {
                                otp += "0";
                            }

                            try {
                                otp = mIdentity.getOTP();
                            }
                            catch (IdentityGuardMobileException e) {
                                Log.e(LOG_COMPONENT, "Failed to get the current OTP", e);
                            }
                            finally {
                                mOTP.setText(IdentityProvider.formatCode(otp, mIdentity.getOTPLength() / 2, '-'));
                            }
                        }
                        mCountdown.setProgress(ticks);
                        progressText.setText(String.valueOf(ticks));
                    }
                });
            }

        }, 1000, 1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_copy:
                copy();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tool_menu, menu);
        return true;
    }

    private void copy(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Otp Code", mOTP.getText().toString().replace("-",""));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Security code has been copied", Toast.LENGTH_SHORT).show();
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer before we quit.
        mTimer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Util.stopTimerForAutoLock(SecurityCode.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Util.startTimerForAutoLock(SecurityCode.this);
    }
}
