/**
 * Copyright (c) 2011 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.entrust.identityGuard.mobile.sdk.Identity;
import com.entrust.identityGuard.mobile.sdk.PlatformDelegate;
import com.parallex.softtoken.Authentication.EnterPasswordActivity;
import com.parallex.softtoken.Activation.EstablishPin;
import com.parallex.softtoken.Authentication.FingerprintActivity;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.entrust.identityGuard.mobile.sdk.exception.EncryptionRequiredException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Utility class that provides methods required by each Activity
 *
 */
public class Util {

    private static final String IDENTITY="Identity";
    private static final String REGISTRATION_URL ="IdentityRegistrationURL";

    private static Identity identity;
    private static String address;


    /**
     * Generate and display error dialog with the given message and an OK button
     * @param msg The message to display.
     * @return The dialog that was created.
     */
    public static Dialog showErrorDialog(Context context, String msg) {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context,
                R.style.Theme_MaterialComponents_Dialog_Alert)
                .setTitle("Error")
                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setBackground(context.getResources().getDrawable(R.drawable.splash_background))
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
        return dialog;
    }

    /**
     * Generate and display info dialog with the given message and an OK button
     * @param context context in which to display the dialog
     * @param msg The message to display.
     * @return The dialog that was created.
     */
    public static Dialog showInfoDialog(Context context, String msg, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setTitle(R.string.title_success)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.label_OK), listener);
        return builder.show();
    }

    /**
     * Generate and display a confirmation dialog with the given message and Yes/No buttons.
     * @param context Context in which to display the dialog
     * @param msg The message to display
     * @param listener The Listener to call when either of the Yes/No buttons is invoked.
     * @return The dialog that was created
     */
    public static Dialog showConfirmationDialog(Context context, String msg, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setTitle(R.string.title_confirm)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(context.getString(R.string.label_yes), listener)
                .setNegativeButton(context.getString(R.string.label_no), listener)
                .setCancelable(false);

        return builder.show();
    }

    /**
     * Store the Identity created so it can be shared across multiple Activities.
     * Normally, this would go into some form of persistent storage, but with this
     * simple example, we just record the identity in memory.
     * @param identity The Identity to store
     */
    public static void setIdentity(Identity identity) {
        Util.identity = identity;
    }

    /**
     * Return the identity we stored
     * @return The identity we stored
     */
    public static Identity getIdentity() {
        return identity;
    }

    /**
     * Save the Encrypted Identity, RegistrationURL and DeviceAPIVersion in saved preferences.
     */
    public static void saveIdentityInformation(Context context){
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(IDENTITY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            JSONObject jsonIdentity = getIdentity().toJSON();
            byte[] encryptedIdentity = PlatformDelegate.encryptData(context,jsonIdentity.toString().getBytes());
            editor.putString(IDENTITY, Base64.encodeToString(encryptedIdentity,Base64.DEFAULT));
            editor.putString(REGISTRATION_URL, Util.getAddress());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // < 3.5.3
   /*public static boolean extractIdentityInformation(Context context){
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(IDENTITY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String identityString = sharedPreferences.getString(IDENTITY, "");
            String identityAddress = sharedPreferences.getString(REGISTRATION_URL, "");
            if (!identityString.equals("")) {
                byte[] decodeFromBase64 = Base64.decode(identityString, Base64.DEFAULT);
                byte[] decryptedIdentity = PlatformDelegate.decryptData(context, decodeFromBase64);
                Identity identity = new Identity(new JSONObject(new String(decryptedIdentity)));
                setIdentity(identity);
                setAddress(identityAddress);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }*/

    public byte[] decryptIdentity(Context context,byte[] encryptedIdentity) {
        byte[] decrypted = null;
        try {
            try {
                return PlatformDelegate.decryptData(context, encryptedIdentity);
            } catch (EncryptionRequiredException e) {
                /* This could be because this is being run in OS >= Android M (23) device and
                the encryption key has not been //changed to use the new keys.*/
                byte[] reEncryptedData = PlatformDelegate.reEncryptDataUsingNewKeys(context, encryptedIdentity);
                // Save reEncryptedData data
                return PlatformDelegate.decryptData(context, reEncryptedData);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return decrypted;
    }


    // >=3.5.3
    /**
     * Extract the saved Encrypted Identity, RegistrationURL and DeviceAPIVersion from saved preferences.
     */
    public static boolean extractIdentityInformation(Context context){
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(IDENTITY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String identityString = sharedPreferences.getString(IDENTITY, "");
            Log.i("Util","Old string:"+identityString);
            String identityAddress = sharedPreferences.getString(REGISTRATION_URL, "");
            if (!identityString.equals("")) {
                byte[] base64EncryptData = Base64.decode(identityString, Base64.DEFAULT);
                byte[] decryptedIdentity;
                try {
                    decryptedIdentity = PlatformDelegate.decryptData(context, base64EncryptData);
                } catch(EncryptionRequiredException e){
                    Log.i("Util","Decryption Exception!! Re-Encrypt data using new keys");
                    // New Keys migration is required. This could be because this is being run in
                    // a >= Android M (23) device and the encryption key has not been changed
                    // to use the new keys.
                    byte[] encryptedIdentity = PlatformDelegate.reEncryptDataUsingNewKeys(context,base64EncryptData);
                    editor.putString(IDENTITY, Base64.encodeToString(encryptedIdentity,Base64.DEFAULT));
                    editor.apply();
                    identityString = sharedPreferences.getString(IDENTITY, "");
                    Log.i("Util","ReEncrypted new string:"+identityString);
                    byte[] newEncodedDataBase64 = Base64.decode(identityString, Base64.DEFAULT);
                    decryptedIdentity = PlatformDelegate.decryptData(context, newEncodedDataBase64);
                }
                Identity identity = new Identity(new JSONObject(new String(decryptedIdentity)));
                setIdentity(identity);
                setAddress(identityAddress);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void setAddress(String address) {
        Util.address = address;
    }

    public static String getAddress() {
        return address;
    }

    /**
     * Return a unique identifier for the device (see comments inline below).
     * It is the combination of an Identity's instance ID and device ID that
     * has to be unique to register for transaction verification, and the
     * instance ID already has 64 bits of random.
     * @return An identifier for the device.
     */
    public static String getDeviceId() {
        // To register with the transaction component just
        // requires a non-null identifier. A real identifier -
        // such as a device PIN on BlackBerry or the registration_id
        // returned from registering an app for notifications on
        // Android - is required for the advanced notification
        // functionality. The random identifier used here is okay
        // for applications not using notifications.
        int randomVal = new Random().nextInt();
        // Make sure the value is positive.
        randomVal &= 0x7FFFFFFF;
        return String.valueOf(randomVal);
    }

    public static boolean isPinRequired(Activity activity){
        String fingerPrintRequired = SharedPref.get(activity.getApplicationContext(),"PIN_REQUIRED","false");
        return Boolean.parseBoolean(fingerPrintRequired);
    }

    public static void setPinRequired(Activity activity, boolean setEnabled){
        SharedPref.set(activity.getApplicationContext(),"PIN_REQUIRED",String.valueOf(setEnabled));
    }

    public static String getPin(Activity activity){
        return SharedPref.get(activity.getApplicationContext(), EstablishPin.PINKEY,"");
    }

    public static void setPin(Activity activity, String text){
        SharedPref.set(activity.getApplicationContext(), EstablishPin.PINKEY,text);
        if(text.length()>3){
            setPinRequired(activity, true);
        }
    }

    public static boolean isFingerPrintRequired(Activity activity){
        String fingerPrintRequired = SharedPref.get(activity.getApplicationContext(),"FINGER_PRINT","false");
        return Boolean.parseBoolean(fingerPrintRequired);
    }

    public static void setFingerPrintRequired(Activity activity, boolean setEnabled){
        SharedPref.set(activity.getApplicationContext(),"FINGER_PRINT",String.valueOf(setEnabled));
    }

    public static boolean isFirstInstallation(Activity activity){
        String fingerPrintRequired = SharedPref.get(activity.getApplicationContext(),"FIRST_INSTALLED","true");
        return Boolean.parseBoolean(fingerPrintRequired);
    }

    public static void setFirstInstallation(Activity activity, boolean setEnabled){
        SharedPref.set(activity.getApplicationContext(),"FIRST_INSTALLED",String.valueOf(setEnabled));
    }

    public static String getUserId(Activity activity){
        return SharedPref.get(activity.getApplicationContext(),"USER_ID","");
    }

    public static void setUserId(Activity activity, String userId){
        SharedPref.set(activity.getApplicationContext(),"USER_ID",userId);
    }

    public static String getSessionId(Activity activity){
        return SharedPref.get(activity.getApplicationContext(),"SESSION_ID","");
    }

    public static void setSessionId(Activity activity, String sessionId){
        SharedPref.set(activity.getApplicationContext(),"SESSION_ID",sessionId);
    }

    public static String readStream(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            return total.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setTimerForLockScreen(Activity activity, String timeValue){
        SharedPref.set(activity.getApplicationContext(), "LOCK_TIME", timeValue);
    }

    public static String getTimerForLockScreen(Activity activity){
        return SharedPref.get(activity.getApplicationContext(),"LOCK_TIME", "1 min");
    }

    private static Handler handler = null;
    private static Runnable runnable = null;
    public static boolean canAutoLock = true;
    public static void startTimerForAutoLock(final Activity activity){
        String timeVal = getTimerForLockScreen(activity).split(" ")[0];
        final int TIME = Integer.parseInt(timeVal) * 60000;
        runnable = new Runnable() {
            @Override
            public void run() {
                canAutoLock = false;
            }
        };
        if(canAutoLock){
            handler = new Handler(activity.getMainLooper());
            handler.postDelayed(runnable,TIME);
        }
    }

    public static void stopTimerForAutoLock(Activity activity){
        if(handler != null){
            removeCallBacks();
            if(!canAutoLock){
                if (Util.isFingerPrintRequired(activity)) {
                    Intent intent = new Intent(activity, FingerprintActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                } else if (Util.isPinRequired(activity)) {
                    Intent intent = new Intent(activity, EnterPasswordActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        }
    }

    private static void removeCallBacks(){
        handler.removeCallbacks(runnable);
        handler = null;
        runnable = null;
    }
}
