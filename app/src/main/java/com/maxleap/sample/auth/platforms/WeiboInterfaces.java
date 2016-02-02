package com.maxleap.sample.auth.platforms;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.util.Log;

import com.maxleap.LogInCallback;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.MLWeiboUtils;
import com.maxleap.SaveCallback;
import com.maxleap.WeiboPlatform;
import com.maxleap.exception.MLException;
import com.maxleap.sample.auth.App;
import com.maxleap.sample.auth.RequestMeCallback;
import com.maxleap.utils.JSONBuilder;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeiboInterfaces extends SocialInterfaces {

    private static final String TAG = WeiboInterfaces.class.getSimpleName();

    @Override
    public boolean isLinked(MLUser user) {
        return (user != null) && MLWeiboUtils.isLinked(user);
    }

    @Override
    public void onLoginButtonClicked(final Activity activity, final int type) {
        MLWeiboUtils.logInInBackground(activity,
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
                                    "User signed up and logged in through Weibo!");
                            showDetailActivity(activity, type);
                        } else {
                            Log.d(TAG,
                                    "User logged in through Weibo!");
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
    public void requestMe(Activity activity, RequestMeCallback doneCallback) {
        final WeiboPlatform platform = MLWeiboUtils.getPlatform();
        if (platform == null) return;

        makeMeRequest(activity, platform, doneCallback);
    }

    private void makeMeRequest(Activity activity, final WeiboPlatform platform, final RequestMeCallback doneCallback) {
        WeiboParameters params = new WeiboParameters(App.WEIBO_APP_KEY);
        params.put("access_token", platform.getAccessToken());
        params.put("uid", platform.getPlatformId());
        AsyncWeiboRunner runner = new AsyncWeiboRunner(activity);
        runner.requestAsync("https://api.weibo.com/2/users/show.json", params, "GET", new RequestListener() {
            @Override
            public void onComplete(final String s) {
                JSONObject profile = JSONBuilder.wrap(s).build();
                MLUser user = MLUser.getCurrentUser();
                user.put("weiboProfile", profile);

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
            }

            @Override
            public void onWeiboException(final WeiboException e) {
                doneCallback.onError(e);
            }
        });
    }

    private void updateViewsWithProfileInfo(RequestMeCallback doneCallback) {
        MLUser currentUser = MLUser.getCurrentUser();
        if (currentUser.get("weiboProfile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("weiboProfile");
            try {
                String avatar = null;
                String name = null;
                Pair<String, String> p1 = null;
                Pair<String, String> p2 = null;
                Pair<String, String> p3 = null;
                Pair<String, String> p4 = null;

                if (userProfile.has("avatar_large")) {
                    avatar = userProfile.getString("avatar_large");
                }
                if (userProfile.has("name")) {
                    name = userProfile.getString("name");
                }
                if (userProfile.has("location")) {
                    p1 = new Pair<>("location", userProfile.getString("location"));
                }
                if (userProfile.has("city")) {
                    p2 = new Pair<>("city", userProfile.getString("city"));
                }
                if (userProfile.has("gender")) {
                    p3 = new Pair<>("gender", userProfile.getString("gender").equals("m") ? "Male" : "Female");
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
