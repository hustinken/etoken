package com.parallex.softtoken.Authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import com.parallex.softtoken.MainActivity;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

public class FingerprintActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_activity);

        if(!checkBiometricsSupport()){
            showPinVerification();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Log.d("Result",errorCode + " Authentication error: " + errString);
                    if(errorCode == 10){ //Cancel
                        if(Util.canAutoLock){
                            onBackPressed();
                        }
                    }else if(errorCode == 7){ //Trial exceeded
                        showPinVerification();
                    }else if(errorCode == 11){ //No fingerprints enrolled
                        Util.setFingerPrintRequired(FingerprintActivity.this, false);
                        showPinVerification();
                    }
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.d("Result","Authentication success");
                    Util.canAutoLock = true;
                    startActivity(new Intent(FingerprintActivity.this, MainActivity.class));
                    finish();
                }
            };
        }
    }

    private CancellationSignal getCancellationSignal(){
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                notifyUser("Authentication was cancelled by user");
            }
        });
        return cancellationSignal;
    }

    private boolean checkBiometricsSupport() {
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if(!keyguardManager.isKeyguardSecure()){
            notifyUser("Fingerprint authentication has not been enabled in settings");
            Util.setFingerPrintRequired(FingerprintActivity.this, false);
            return false;
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint authentication permission is not enabled");
            Util.setFingerPrintRequired(FingerprintActivity.this, false);
            return false;
        }
        getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        return true;
    }

    private void startBiometricsProcess(){
        BiometricPrompt.Builder biometricPrompt = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt = new BiometricPrompt.Builder(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt.setTitle("Fingerprint of ProvidusSoftoken");
        }
        //biometricPrompt.setDescription("Authenticate with fingerprint to access the application");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt.setNegativeButton("USE PIN", this.getMainExecutor(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d("Result","Authentication canceled");
                    startActivity(new Intent(FingerprintActivity.this, EnterPasswordActivity.class));
                    finish();
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt.build().authenticate(getCancellationSignal(),getMainExecutor(),authenticationCallback);
        }
    }

    private void showPinVerification(){
        startActivity(new Intent(FingerprintActivity.this, EnterPasswordActivity.class));
        finish();
    }

    private void notifyUser(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBiometricsProcess();
    }
}