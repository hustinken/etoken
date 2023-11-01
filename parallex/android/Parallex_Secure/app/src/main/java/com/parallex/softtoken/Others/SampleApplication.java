package com.parallex.softtoken.Others;

import android.app.Application;

import com.entrust.identityGuard.mobile.sdk.PlatformDelegate;
import com.entrust.identityGuard.mobile.sdk.tokenproviders.ThirdPartyTokenManagerFactory;

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PlatformDelegate.setLogLevel(PlatformDelegate.LOG_LEVEL_DEBUG);
        PlatformDelegate.initialize(this);
        PlatformDelegate.setApplicationScheme("sampleapp");
        ThirdPartyTokenManagerFactory.setContext(getApplicationContext());
    }
}
