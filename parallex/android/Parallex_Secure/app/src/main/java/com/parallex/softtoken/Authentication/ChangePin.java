package com.parallex.softtoken.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

public class ChangePin extends AppCompatActivity {

    EditText prevPin, newPin, confirmPin;
    LinearLayout passLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_pin);
        prevPin = findViewById(R.id.prev_pin);
        newPin = findViewById(R.id.new_pin);
        confirmPin = findViewById(R.id.confirm_pin);
        passLayout = findViewById(R.id.change_pin_layout);

        passLayout.setVisibility(View.GONE);

        prevPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passLayout.setVisibility(View.GONE);
                if(prevPin.getText().toString().length() > 3){
                    if(!prevPin.getText().toString().equals(Util.getPin(ChangePin.this))){
                        Toast.makeText(ChangePin.this, "Please enter the correct pin", Toast.LENGTH_LONG).show();
                        prevPin.setText("");
                    }else{
                        passLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void onProceedButtonClicked(View view){
        if(!newPin.getText().toString().equals(confirmPin.getText().toString())){
            Toast.makeText(ChangePin.this, "The PIN values do not match. Re-enter your PIN.", Toast.LENGTH_LONG).show();
            newPin.setText("");
            confirmPin.setText("");
        }else if(newPin.getText().toString().length() > 3 && confirmPin.getText().toString().length() > 3){
            Util.setPin(ChangePin.this, newPin.getText().toString());
            Toast.makeText(ChangePin.this, "Your pin has been changed successfully", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }
}