/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Authentication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

/**
 * Activity used to establish a PIN for the soft token identity.
 *
 */
public class CreatePIN extends Activity {

    // All soft token PINs are the following length.
    private static final int PIN_LENGTH = 4;

    private EditText mPin, mPinConfirm;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_pin);
        
        String intro = getString(R.string.intro_PIN, PIN_LENGTH);
        ((TextView) findViewById(R.id.pinIntro)).setText(intro);
        mPin = findViewById(R.id.pin);
        mPinConfirm = findViewById(R.id.pinConfirm);
        Button okButton = findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Get the values the user entered for the PIN and its confirmation
                String pinValue = mPin.getText().toString();
                String pinValueConfirm = mPinConfirm.getText().toString();
                // In order to move to the next step, both the PIN and Confirm PIN fields must be non-empty
                // and they must have the same value.
                if (pinValue.length() > 3 &&  pinValueConfirm.length() > 3 && pinValue.equals(pinValueConfirm)) {
                    Toast.makeText(CreatePIN.this, "Your token has been protected", Toast.LENGTH_SHORT).show();
                    Util.setPin(CreatePIN.this, pinValue);
                    onBackPressed();
                } else {
                    // One or both of the fields are empty or their values don't match.
                    // Clear out both fields, set focus back to the PIN entry field, and
                    // announce the problem with an error dialog.
                    mPin.setText("");
                    mPinConfirm.setText("");
                    mPin.requestFocus();
                    Util.showErrorDialog(CreatePIN.this, getString(R.string.error_mismatchPIN));
                }
            }

        });
    }
}
