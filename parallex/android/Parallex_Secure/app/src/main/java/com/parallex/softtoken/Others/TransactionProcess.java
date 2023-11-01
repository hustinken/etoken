package com.parallex.softtoken.Others;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.entrust.identityGuard.mobile.sdk.PlatformDelegate;
import com.entrust.identityGuard.mobile.sdk.Transaction;
import com.entrust.identityGuard.mobile.sdk.TransactionDetail;
import com.entrust.identityGuard.mobile.sdk.TransactionMode;
import com.entrust.identityGuard.mobile.sdk.TransactionProvider;
import com.entrust.identityGuard.mobile.sdk.TransactionResponse;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;
import com.entrust.identityGuard.mobile.sdk.exception.IdentityGuardMobileException;

public class TransactionProcess extends Activity {
    
    private Transaction mTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction);
        
        Intent intent = getIntent();
        mTransaction = (Transaction) intent.getExtras().get("txn");
        
        // Display transaction summary if one is provided.
        TextView summary = (TextView) findViewById(R.id.summary);
        if(mTransaction.getSummary() != null) {
            summary.setText(mTransaction.getSummary());
        } else {
            summary.setVisibility(View.GONE);
        }
        
        // Display the transaction details if any are provided.
        TableLayout table = (TableLayout) findViewById(R.id.details_table);
        if(mTransaction.getDetails() != null) {
            addTransactionDetails(table, mTransaction);
        } else {
            table.setVisibility(View.GONE);
        }
        
        Button concern = (Button) findViewById(R.id.concern);
        concern.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                try {
                    mTransaction.setTransactionResponse(TransactionResponse.CONCERN);
                    
                    if(mTransaction.getTransactionMode().toString().equals(TransactionMode.ONLINE.toString())) {
                        OnlineTransactionResponse response =
                                new OnlineTransactionResponse(TransactionProcess.this,
                                TransactionProvider.getConfirmationCode(Util.getIdentity(), mTransaction));
                        response.execute();
                    } else {
                        finish();
                    }
                } catch(Exception e) {
                    PlatformDelegate.logError(TransactionProcess.class.getName(),
                            "An error occurred while sending a transaction response.", e);
                    Util.showErrorDialog(TransactionProcess.this,
                            getString(R.string.error_generating_confirmation_code));
                }
            }
        });
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                try {
                    mTransaction.setTransactionResponse(TransactionResponse.CANCEL);
                    
                    if(mTransaction.getTransactionMode().toString().equals(TransactionMode.ONLINE.toString())) {
                        OnlineTransactionResponse response =
                                new OnlineTransactionResponse(TransactionProcess.this,
                                TransactionProvider.getConfirmationCode(Util.getIdentity(), mTransaction));
                        response.execute();
                    } else {
                        finish();
                    }
                } catch(Exception e) {
                    PlatformDelegate.logError(TransactionProcess.class.getName(),
                            "An error occurred while sending a transaction response.", e);
                    Util.showErrorDialog(TransactionProcess.this,
                            getString(R.string.error_generating_confirmation_code));
                }
            }
        });
        
        Button confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                try {
                    mTransaction.setTransactionResponse(TransactionResponse.CONFIRM);
                    
                    if(mTransaction.getTransactionMode().toString().equals(TransactionMode.ONLINE.toString())) {
                        OnlineTransactionResponse response =
                                new OnlineTransactionResponse(TransactionProcess.this,
                                TransactionProvider.getConfirmationCode(Util.getIdentity(), mTransaction));
                        response.execute();
                    } else {
                        Intent intent = new Intent(TransactionProcess.this, ClassicTransaction.class);
                        intent.putExtra("txn", mTransaction);
                        startActivity(intent);
                        finish();
                    }
                } catch(Exception e) {
                    PlatformDelegate.logError(TransactionProcess.class.getName(),
                            "An error occurred while sending a transaction response.", e);
                    Util.showErrorDialog(TransactionProcess.this,
                            getString(R.string.error_generating_confirmation_code));
                }
            }
        });
    }
    
    private void addTransactionDetails(TableLayout table, Transaction transaction) {
        
        TransactionDetail[] details = transaction.getDetails();
        for(int i = 0; i < details.length; i++) {
            addDetailRow(table, details[i]);
        }
    }
    
    private void addDetailRow(TableLayout table, TransactionDetail detail) {
        
        LayoutInflater inflater = getLayoutInflater();
        
        TableRow row = (TableRow) inflater.inflate(R.layout.transaction_details_row, table, false);
        
        TextView nameView = (TextView) row.findViewById(R.id.detail);
        nameView.setText(detail.getDetail());
        
        TextView valueView = (TextView) row.findViewById(R.id.value);
        valueView.setText(detail.getValue());
        
        table.addView(row);
    }
    
    private void completeOnlineTransaction(boolean result, Transaction transaction) {
        
        if(result) {
            Util.showInfoDialog(this, getString(R.string.transaction_success,
                    mTransaction.getTransactionResponse().toString()),
                    new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            dialog.cancel();
                            finish();
                        }
                    });
        } else {
            Util.showErrorDialog(this, getString(R.string.transaction_failed));
        }
    }
    
    private void onlineTransactionFailed(Exception e) {
        
        if(e != null) {
            Util.showErrorDialog(this, e.getMessage());
        } else {
            Util.showErrorDialog(this, getString(R.string.transaction_failed));
        }
    }
    
    private class OnlineTransactionResponse extends AsyncTask<Void, Void, Boolean> {
        
        public OnlineTransactionResponse(Context context, String response) {
            
            mContext = context;
            mResponse = response;
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            
            try {
                TransactionProvider tp = new TransactionProvider(Util.getAddress());
                Boolean success = tp.authenticateTransaction(PlatformDelegate.getCommCallback(),
                        Util.getIdentity(), mTransaction, mResponse);
                return success;
            } catch(IdentityGuardMobileException e) {
                PlatformDelegate.logError(TransactionProcess.class.getName(), e.getMessage(), e);
                mTransactionFailedException = e;
            } catch (Exception e) {
                PlatformDelegate.logError(TransactionProcess.class.getName(),
                        "Error authenticating transaction.", e);
                mTransactionFailedException = e;
            }
    
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            stopDialog();
    
            // Give the result a value if null
            result = result == null ? false : result;
            
            if(mTransactionFailedException != null) {
                onlineTransactionFailed(mTransactionFailedException);
            } else {
                completeOnlineTransaction(result, mTransaction);
            }
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            createDialog(mContext.getString(R.string.dialog_responding));
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

        private Context mContext;
        private String mResponse;
        private Exception mTransactionFailedException;
        
        private ProgressDialog mDialog;
    }
}
