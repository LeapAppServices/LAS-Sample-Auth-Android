package com.maxleap.sample.auth;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.maxleap.MLFacebookUtils;
import com.maxleap.MLTwitterUtils;
import com.maxleap.MLWechatUtils;
import com.maxleap.MLWeiboUtils;
import com.maxleap.MaxLeap;


public class App extends Application {

    //  MaxLeap
    public static final String APP_ID = "Replace this with your App Id";
    public static final String API_KEY = "Replace this with your Rest Key";

    //  Facebook
    public static final String FACEBOOK_APP_ID = "Replace this with your Facebook app id";
    public static final String FACEBOOK_SECRET_KEY = "Replace this with your Facebook secret key";

    //  Twitter
    public static final String TWITTER_CONSUMER_KEY = "Replace this with your Twitter consumer key";
    public static final String TWITTER_CONSUMER_SECRET = "Replace this with your Twitter consumer secret";

    //  Weibo
    public static final String WEIBO_APP_KEY = "Replace this with your Weibo app key";
    public static final String WEIBO_APP_SECRET = "Replace this with your Weibo app secret";

    //  Wechat
    public static final String WECHAT_APP_ID = "Replace this with your Wechat app id";
    public static final String WECHAT_APP_SECRET = "Replace this with your Wechat app secret";

    //  Region
    public static final String REGION = MaxLeap.REGION_CN;

    @Override
    public void onCreate() {
        super.onCreate();

        if (APP_ID.startsWith("Replace") || API_KEY.startsWith("Replace")) {
            throw new IllegalArgumentException("Please replace with your app id and api key first before" +
                    "using MaxLeap SDK.");
        }

        MaxLeap.setLogLevel(MaxLeap.LOG_LEVEL_DEBUG);
        MaxLeap.initialize(this, APP_ID, API_KEY, REGION);

        if (REGION.equals(MaxLeap.REGION_CN)) {
            //  Weibo
            MLWeiboUtils.initialize(WEIBO_APP_KEY, WEIBO_APP_SECRET);

            //  Wechat
            MLWechatUtils.initialize(WECHAT_APP_ID, WECHAT_APP_SECRET);
        } else {
            //  Facebook
            MLFacebookUtils.initialize(FACEBOOK_APP_ID, FACEBOOK_SECRET_KEY);

            //  Using Facebook SDK
            FacebookSdk.sdkInitialize(this);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);

            //  Twitter
            MLTwitterUtils.initialize(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        }


    }

}
