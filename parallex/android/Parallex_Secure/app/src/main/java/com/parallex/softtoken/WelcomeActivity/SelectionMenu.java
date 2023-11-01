package com.parallex.softtoken.WelcomeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.MainActivity;
import com.parallex.softtoken.Registration.RegisterCustomer;
import com.parallex.softtoken.Utilities.Util;

public class SelectionMenu extends AppCompatActivity implements View.OnClickListener {


    Button activateToken, registerToken;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        activateToken = findViewById(R.id.activate_token);
        registerToken = findViewById(R.id.register_token);

        setSupportActionBar(toolbar);

        activateToken.setOnClickListener(this);
        registerToken.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!Util.isFirstInstallation(this)){
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == registerToken){
            Intent intent = new Intent(SelectionMenu.this, RegisterCustomer.class);
            startActivity(intent);
        }else if(view == activateToken){
            Intent intent = new Intent(SelectionMenu.this, MainActivity.class);
            startActivity(intent);
        }
    }
}