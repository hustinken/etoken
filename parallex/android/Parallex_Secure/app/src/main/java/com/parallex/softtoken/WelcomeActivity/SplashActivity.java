/**
 * Copyright (c) 2014 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.WelcomeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.parallex.softtoken.Authentication.EnterPasswordActivity;
import com.parallex.softtoken.Authentication.FingerprintActivity;
import com.parallex.softtoken.MainActivity;
import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Utilities.Util;

public class SplashActivity extends AppCompatActivity {

    Animation topAnimation;
    ImageView welcomeImg;
    Handler handler = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        welcomeImg = findViewById(R.id.anim_logo);
        welcomeImg.setAnimation(topAnimation);

        handler = new Handler(getMainLooper());
        handler.postDelayed(runnable, 3000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
        if(Util.isFirstInstallation(SplashActivity.this)){
            Intent intent = new Intent(SplashActivity.this, SelectionMenu.class);
            startActivity(intent);
        } else if (Util.isFingerPrintRequired(SplashActivity.this)) {
            Intent intent = new Intent(SplashActivity.this, FingerprintActivity.class);
            startActivity(intent);
        } else if (Util.isPinRequired(SplashActivity.this)) {
            Intent intent = new Intent(SplashActivity.this, EnterPasswordActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(handler != null){
            handler.removeCallbacks(runnable);
            handler = null;
        }
        finish();
    }
}