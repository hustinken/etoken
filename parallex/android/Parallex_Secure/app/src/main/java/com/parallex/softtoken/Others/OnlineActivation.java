package com.parallex.softtoken.Others;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.entrust.identityGuard.mobile.sdk.ActivationLaunchUrlParams;
import com.entrust.identityGuard.mobile.sdk.Identity;
import com.entrust.identityGuard.mobile.sdk.IdentityProvider;
import com.entrust.identityGuard.mobile.sdk.LaunchUrlParams;
import com.entrust.identityGuard.mobile.sdk.PlatformDelegate;
import com.entrust.identityGuard.mobile.sdk.TransactionProvider;
import com.parallex.softtoken.Activation.SecurityCode;
import com.parallex.softtoken.Activation.EstablishPin;
import com.parallex.softtoken.MainActivity;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;
import com.entrust.identityGuard.mobile.sdk.exception.IdentityGuardMobileException;

/**
 * Activity which handles the online activation scenario introduced as part of the 2.0 release
 * of the IdentityGuard Mobile SDK.  This activity is launched from a user clicking an
 * app-specific link.  The link contains the registration URL, registration password and the
 * serial number of the token required to activate the new soft token.
 * 
 * @since 2.0
 */
public class OnlineActivation extends Activity {

    private EditText mSerialNumber;
    private EditText mAddress;
    private String mRegistrationPassword;
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        // Did user arrive via app-specific link
        final Intent intent = getIntent();
        final String intentAction = intent.getAction();
        if(intentAction != null && intentAction.equals(Intent.ACTION_VIEW)) {
            setContentView(R.layout.online_activation);
            
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.title_addIdentity);
            
            mSerialNumber = (EditText) findViewById(R.id.serialNum);
            mAddress = (EditText) findViewById(R.id.address);
            
            parseLaunchUrlParameters(intent);
            
            establishButtons();
        } else {
            // We should only be able to get to this activity via app-specific link.  If we
            // got here some other way, launch the classic add identity activity.
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        
        finish();
    }
    
    /**
     * Parses the parameters out of the app-specific link that launched the app and displays the
     * registration URL and token serial serial numbers in the appropriate fields.
     * @param intent the intent.
     */
    private void parseLaunchUrlParameters(Intent intent) {
        
        try {
            LaunchUrlParams linkParams = PlatformDelegate.parseLaunchUrl(intent);
            if(linkParams instanceof ActivationLaunchUrlParams) {
                String address = getAddress(((ActivationLaunchUrlParams) linkParams)
                        .getRegistrationUrl());
                mSerialNumber.setText(((ActivationLaunchUrlParams) linkParams).getSerialNumber());
                mAddress.setText(address);
                mRegistrationPassword = ((ActivationLaunchUrlParams) linkParams)
                        .getRegistrationPassword();
            }
        } catch(IdentityGuardMobileException e) {
            Util.showErrorDialog(this, getString(R.string.error_badSerialNum));
        }
    }
    
    /**
     * Set the actions to take on click with the OK and cancel buttons.
     */
    private void establishButtons() {
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                String serialNumber = mSerialNumber.getText().toString().trim();
                // Validate the serial number.
                if(validateSerialNumber(serialNumber)) {
                    String address = mAddress.getText().toString().trim();
                    
                    CreateRegisterIdentity cri = new CreateRegisterIdentity(OnlineActivation.this,
                            Util.getDeviceId(), mRegistrationPassword, address, serialNumber);
                    cri.execute();
                }
            }
        });
    }
    
    /**
     * Gets the registration URL and appends the <code>https://</code> prefix if it is missing.
     * @param address The registration URL
     * @return the complete address to use as the registration URL
     */
    private String getAddress(String address) {
        
        return address.startsWith("https://") ? address : "https://" + address;
    }
    
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
    
    /**
     * Actions to take upon successful online activation and registration.
     * @param result The created identity.
     */
    private void completeOnlineActivation(Identity result) {
        
        // Store our identity so it can be accessed by other activities
        Util.setIdentity(result);
        
        //Store the address so that we can perform online tranactions
        Util.setAddress(mAddress.getText().toString().trim());
        
        // Determine whether the soft token identity requires a PIN to protect it.
        if (result.isPINRequired()) {
            // Our soft token identity requires a PIN
            Intent intent = new Intent(this, EstablishPin.class);
            intent.putExtra("online", true);
            startActivity(intent);
        } else {
            // Go directly to showing the user the security code.
            startActivity(new Intent(this, SecurityCode.class));
        }
        finish();
    }
    
    /**
     * Display an error to the user indicating the reason for the failed online activation.
     * @param e The exception.
     */
    private void failedOnlineActivation(Exception e) {
        
        Util.showErrorDialog(this, e.getMessage());
    }
    
    /**
     * An asynchronous task to perform the communication with the IdentityGuard Self-Service
     * Transaction Component to complete the online activation of a token.
     */
    private class CreateRegisterIdentity extends AsyncTask<Void, Void, Identity> {
        
        private ProgressDialog mDialog;
        private Context mContext;
        private String mDeviceId;
        private String mRegPwd;
        private String mTransactionAddress;
        private String mSerNum;
        
        private Exception activationFailException;
        
        public CreateRegisterIdentity(Context context, String deviceId, String regPwd,
                String address, String serialNum) {
            
            mContext = context;
            mDeviceId = deviceId;
            mRegPwd = regPwd;
            mTransactionAddress = address;
            mSerNum = serialNum;
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground()
         */
        @Override
        protected Identity doInBackground(Void... params) {
            
            try {
                PlatformDelegate.setApplicationId("otp.test");
                
                TransactionProvider tp = new TransactionProvider(mTransactionAddress);
                tp.setContext(mContext);
                
                // For the purpose of this sample app, we will disable notifications for
                // transaction notifications.
                boolean supportsNotification = false;
                
                // For the purpose of this sample app, we will enable transactions.
                boolean supportsTransactions = true;
                
                // For the purpose of this sample app, we will enable online transactions.
                boolean supportsOnlineTransactions = true;
                
                // For the purpose of this sample app, we will enable offline transactions.
                boolean supportsOfflineTransactions = true;
                
                // The device ID field is used to send notifications to the device.
                // For Android devices, this value corresponds to the Google Cloud Messaging
                // (GCM) registration ID.
                if(mDeviceId == null) {
                    mDeviceId = Util.getDeviceId();
                }
                
                Identity id = tp.createIdentityUsingRegPassword(
                        PlatformDelegate.getCommCallback(), mRegPwd, mSerNum, mDeviceId,
                        supportsNotification, supportsTransactions,
                        supportsOnlineTransactions, supportsOfflineTransactions);
                return id;
            } catch(IdentityGuardMobileException e) {
                PlatformDelegate.logError(OnlineActivation.class.getName(), e.getMessage(), e);
                activationFailException = e;
            } catch(Exception e) {
                PlatformDelegate.logError(OnlineActivation.class.getName(),
                        "Error registering id.", e);
                activationFailException = e;
            }
            
            // An exception occurred during activation/registration, so return a null object for
            // the identity.
            return null;
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute()
         */
        @Override
        protected void onPostExecute(Identity result) {
            
            super.onPostExecute(result);
            stopDialog();
            
            if(result != null) {
                completeOnlineActivation(result);
            } else {
                failedOnlineActivation(activationFailException);
            }
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            createDialog(mContext.getString(R.string.dialog_registering));
        }
        
        /**
         * Creates a new <code>ProgressDialog</code> object displaying the provided message.
         * @param message The message to display.
         */
        private void createDialog(String message) {
            
            if(mDialog != null) {
                mDialog = null;
            }
            
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(message);
            mDialog.show();
        }
        
        /**
         * Stop the current <code>ProgressDialog</code>.
         */
        private void stopDialog() {
            
            if(mDialog != null) {
                mDialog.dismiss();
                mDialog.cancel();
                mDialog = null;
            }
        }
    }
}
