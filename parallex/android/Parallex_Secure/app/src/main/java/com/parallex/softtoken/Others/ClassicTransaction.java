package com.parallex.softtoken.Others;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.entrust.identityGuard.mobile.sdk.PlatformDelegate;
import com.entrust.identityGuard.mobile.sdk.Transaction;
import com.entrust.identityGuard.mobile.sdk.TransactionProvider;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

public class ClassicTransaction extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.classic_transaction);
        
        Intent intent = getIntent();
        Transaction transaction = (Transaction) intent.getExtras().get("txn");
        
        TextView confirmCode = (TextView) findViewById(R.id.confirm_code);
        try {
            confirmCode.setText(TransactionProvider.getConfirmationCode(Util.getIdentity(), transaction));
        } catch(Exception e) {
            PlatformDelegate.logError(getClass().getName(), "Crypto error.", e);
        }
        
        Button done = (Button) findViewById(R.id.ok);
        done.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        
        finish();
    }
}
