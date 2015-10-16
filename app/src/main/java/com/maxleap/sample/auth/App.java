package com.maxleap.sample.auth;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.maxleap.MLFacebookUtils;
import com.maxleap.MaxLeap;


public class App extends Application {

    public static final String APP_ID = "Replace this with your App Id";
    public static final String API_KEY = "Replace this with your Rest Key";
    public static final String FACEBOOK_APP_ID = "Replace this with your Facebook app id";
    public static final String FACEBOOK_SECRET_KEY = "Replace this with your Facebook secret key";

    @Override
    public void onCreate() {
        super.onCreate();

        if (APP_ID.startsWith("Replace") || API_KEY.startsWith("Replace")) {
            throw new IllegalArgumentException("Please replace with your app id and api key first before" +
                    "using MaxLeap SDK.");
        }

        MaxLeap.setLogLevel(MaxLeap.LOG_LEVEL_DEBUG);
        MaxLeap.initialize(this, APP_ID, API_KEY);

        // Set your Facebook App Id in strings.xml
        MLFacebookUtils.initialize(FACEBOOK_APP_ID, FACEBOOK_SECRET_KEY);

        //  Using Facebook SDK
        FacebookSdk.sdkInitialize(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
    }

}
