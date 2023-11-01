/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Activation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Dialogs.ProgressDialog;
import com.parallex.softtoken.MainActivity;
import com.parallex.softtoken.Registration.RegisterCustomer;

/**
 * Activity used to establish a PIN for the soft token identity.
 */
public class ActivateToken extends Activity {

    // All soft token PINs are the following length.
    private static final int PIN_LENGTH = 4;
    public static final String PINKEY = "001002#";


    private boolean onlineActivation;
    Button button, button2;
    ProgressDialog dialog;
    RegisterCustomer registerCustomer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_token);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        dialog = new ProgressDialog(this);

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                buttonClicked.onClicked(view);
//                dismiss();
//            }
//        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivateToken.this, MainActivity.class));
                finish();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.show();
                dialog.setProgressLabel("Resending SMS", true);
                dialog.setButtonClicked2("Resend SMS", new ProgressDialog.ButtonClicked() {
                    @Override
                    public void onClicked(View v) {
                        registerCustomer.sendSmsToUser();
                        dialog.dismiss();
                    }
                });

            }
        });


    }

    public void setButtonClicked(String buttonText, ProgressDialog.ButtonClicked buttonClicked) {
        button.setVisibility(View.VISIBLE);
        button.setText(buttonText);
        this.buttonClicked = buttonClicked;
    }

    public void setButtonClicked2(String buttonText, ProgressDialog.ButtonClicked buttonClicked) {
        button2.setVisibility(View.VISIBLE);
        button2.setText(buttonText);
        this.buttonClicked2 = buttonClicked;
    }

//    @Override
//    public void onBackPressed() {
//        if (!lock) {
//            dismiss();
//            super.onBackPressed();
//        }
//    }

    private ProgressDialog.ButtonClicked buttonClicked;
    private ProgressDialog.ButtonClicked buttonClicked2;

    public interface ButtonClicked {
        void onClicked(View v);
    }

}
