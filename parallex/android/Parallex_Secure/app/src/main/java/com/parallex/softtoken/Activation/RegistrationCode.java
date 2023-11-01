/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Activation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Dialogs.ProgressDialog;
import com.parallex.softtoken.Model.ApiModel;
import com.parallex.softtoken.Utilities.Util;

import org.json.JSONObject;

/**
 * Activity that presents the registration code. The end-user must
 * enter this value back into their self-service web page or provide it
 * to an administrator (or help desk person) to complete the soft token
 * activation process.
 */
public class RegistrationCode extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     */
    TextView regCode;
    ApiModel model;
    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.establish_pin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        model = new ApiModel();
        dialog = new ProgressDialog(this);

        //  regCode = findViewById(R.id.regCode);
        // Display an explanation of what needs to be done with the registration code
        // plus the code itself.
        // TextView regCode = findViewById(R.id.regCode);
        // regCode.setText(Util.getIdentity().getRegistrationCode());

        model.setAction("3");
        model.setUserId(Util.getUserId(this));
        model.setSerialNo(model.getSerialNo());
        model.setRegistrationNo(Util.getIdentity().getRegistrationCode());
        model.setSessionId(Util.getSessionId(this));


//        Button okButton = findViewById(R.id.ok);
//        okButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                // Ask the user if they have used the registration code.
//                Util.showConfirmationDialog(RegistrationCode.this, getString(R.string.confirm_regCodeUse), new OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (which == DialogInterface.BUTTON_POSITIVE) {
//                            // If the answer is yes, clear out the registration code and move on to the security code.
//                            Util.getIdentity().clearRegistrationCode();
//                            startActivity(new Intent(RegistrationCode.this, SecurityCode.class));
//                            finish();
//                        } else {
//                            // Return back to the Activity screen
//                            dialog.cancel();
//                        }
//                    }
//                });
//            }
//        });
        activateToken();
    }

    private void activateToken() {
        dialog.show();
        dialog.setProgressLabel("Activating your token, Please wait", true);
        model.activateToken(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        displayError("No Internet Connection", "Dismiss");
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);

                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    String responseDesc = json.getString("ps_resp_desc");
                    if (responseCode.equals("0")) {
                        dialog.setProgressLabel("Activation was successful", true, true);
                        dialog.setButtonClicked("Proceed", new ProgressDialog.ButtonClicked() {
                            @Override
                            public void onClicked(View v) {
                                Util.setFirstInstallation(RegistrationCode.this, false);
                                Util.getIdentity().clearRegistrationCode();
                                startActivity(new Intent(RegistrationCode.this, SecurityCode.class));
                                finish();
                            }
                        });

                    } else if (responseCode.equals("1")) {
                        dialog.setProgressLabel(responseDesc, true, true);
                        dialog.setButtonClicked("Ok", new ProgressDialog.ButtonClicked() {
                            @Override
                            public void onClicked(View v) {
                                onBackPressed();
                            }
                        });
                    } else {
                        displayError("Connection failed", "Dismiss");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    displayError("Server Error", "Dismiss");
                }
            }
        });
    }

    private void displayError(String message, String buttonText) {
        dialog.setProgressLabel(message, true, true);
        dialog.setButtonClicked(buttonText, new ProgressDialog.ButtonClicked() {
            @Override
            public void onClicked(View v) {
                onBackPressed();
            }
        });
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.nav_copy:
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("Registration Code", regCode.getText().toString().replace("-",""));
//                clipboard.setPrimaryClip(clip);
//                Toast.makeText(this, "Registration code has been copied", Toast.LENGTH_SHORT).show();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.tool_menu, menu);
//        return true;
//    }
}
