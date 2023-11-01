package com.parallex.softtoken.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parallex.softtoken.MainActivity;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

public class EnterPasswordActivity extends AppCompatActivity {

    EditText pin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.enter_password_activity);
        pin = findViewById(R.id.pin);
        pin.addTextChangedListener(textWatcher);
        pin.requestFocus();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String enteredPin = pin.getText().toString();
            String applicationPin = Util.getPin(EnterPasswordActivity.this);
            if(enteredPin.length() > 3){
                if(enteredPin.equals(applicationPin)){
                    Util.canAutoLock = true;
                    startActivity(new Intent(EnterPasswordActivity.this, MainActivity.class));
                    finish();
                }else{
                    pin.setText("");
                    Toast.makeText(EnterPasswordActivity.this, "Wrong Pin", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
