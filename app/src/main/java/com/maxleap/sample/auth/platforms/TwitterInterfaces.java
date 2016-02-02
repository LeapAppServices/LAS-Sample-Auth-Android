package com.maxleap.sample.auth.platforms;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.util.Log;

import com.maxleap.LogInCallback;
import com.maxleap.MLLog;
import com.maxleap.MLTwitterUtils;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.SaveCallback;
import com.maxleap.TwitterPlatform;
import com.maxleap.exception.MLException;
import com.maxleap.sample.auth.App;
import com.maxleap.sample.auth.RequestMeCallback;
import com.maxleap.social.twitter.signpost.OAuthConsumer;
import com.maxleap.social.twitter.signpost.basic.DefaultOAuthConsumer;
import com.maxleap.utils.JSONBuilder;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitterInterfaces extends SocialInterfaces {

    private static final String TAG = TwitterInterfaces.class.getSimpleName();

    @Override
    public boolean isLinked(MLUser user) {
        return (user != null) && MLTwitterUtils.isLinked(user);
    }

    @Override
    public void onLoginButtonClicked(final Activity activity, final int type) {
        MLTwitterUtils.logInInBackground(activity,
                new LogInCallback() {
                    @Override
                    public void done(MLUser user, MLException e) {
                        if (e != null) {
                            e.printStackTrace();
                            MLLog.t(e.getMessage());
                            return;
                        }

                        MLLog.d(TAG, user.toString());

                        if (user.isNew()) {
                            Log.d(TAG,
                                    "User signed up and logged in through Twitter!");
                            showDetailActivity(activity, type);
                        } else {
                            Log.d(TAG,
                                    "User logged in through Twitter!");
                            showDetailActivity(activity, type);
                        }
                    }
                });
    }

    @Override
    public void onLogoutButtonClicked(Activity activity) {
        // Log the user out with MaxLeap SDK
        MLUser.logOut();

        activity.finish();

        // Go to the login view
        showLoginActivity(activity);
    }

    @Override
    public void requestMe(final Activity activity, final RequestMeCallback doneCallback) {
        final TwitterPlatform platform = MLTwitterUtils.getPlatform();
        if (platform == null) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                makeMeRequest(activity, platform, doneCallback);
            }
        }).start();
    }

    private void makeMeRequest(Activity activity, TwitterPlatform platform, RequestMeCallback doneCallback) {
        OAuthConsumer consumer = new DefaultOAuthConsumer(App.TWITTER_CONSUMER_KEY, App.TWITTER_CONSUMER_SECRET);
        consumer.setTokenWithSecret(platform.getAccessToken(), platform.getAccessSecret());
        try {
            URL url = new URL("https://api.twitter.com/1.1/account/verify_credentials.json");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            consumer.sign(request);
            request.connect();
            boolean isError = false;
            InputStream in = request.getInputStream();
            if (in == null) {
                in = request.getErrorStream();
                isError = true;
            }
            StringBuilder data = new StringBuilder();
            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                data.append(new String(buff, 0, len));
            }
            if (isError) {
                throw new MLException(request.getResponseCode(), data.toString());
            }
            JSONObject profile = JSONBuilder.wrap(data.toString()).build();
            MLUser user = MLUser.getCurrentUser();
            user.put("twitterProfile", profile);

            MLUserManager.saveInBackground(user, new SaveCallback() {
                @Override
                public void done(final MLException e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        MLLog.d(TAG,
                                "finish saving");
                    }
                }
            });

            updateViewsWithProfileInfo(doneCallback);
        } catch (Exception e) {
            doneCallback.onError(e);
        }
    }

    private void updateViewsWithProfileInfo(RequestMeCallback doneCallback) {
        MLUser currentUser = MLUser.getCurrentUser();
        if (currentUser.get("twitterProfile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("twitterProfile");
            try {
                String avatar = null;
                String name = null;
                Pair<String, String> p1 = null;
                Pair<String, String> p2 = null;
                Pair<String, String> p3 = null;
                Pair<String, String> p4 = null;

                if (userProfile.has("profile_image_url")) {
                    avatar = userProfile.getString("profile_image_url");
                }
                if (userProfile.has("name")) {
                    name = userProfile.getString("name");
                }
                if (userProfile.has("location")) {
                    p1 = new Pair<>("location", userProfile.getString("location"));
                }
                if (userProfile.has("description")) {
                    p2 = new Pair<>("description", userProfile.getString("description"));
                }
                if (userProfile.has("friends_count")) {
                    p3 = new Pair<>("friends_count", "" + userProfile.getInt("friends_count"));
                }
                if (userProfile.has("created_at")) {
                    SimpleDateFormat dateFormatBefore = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
                    Date date = dateFormatBefore.parse(userProfile
                            .getString("created_at"));
                    SimpleDateFormat dateFormatAfter = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                    p4 = new Pair<>("created_at", dateFormatAfter.format(date));
                }
                doneCallback.onSuccess(avatar, name, p1, p2, p3, p4);
            } catch (Exception e) {
                doneCallback.onError(e);
            }

        }
    }
}
