package com.maxleap.sample.auth.platforms;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.util.Log;

import com.maxleap.LogInCallback;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.MLWechatUtils;
import com.maxleap.SaveCallback;
import com.maxleap.WechatPlatform;
import com.maxleap.exception.MLException;
import com.maxleap.external.social.common.Platform;
import com.maxleap.sample.auth.RequestMeCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class WechatInterfaces extends SocialInterfaces {

    private static final String TAG = WechatInterfaces.class.getSimpleName();

    @Override
    public boolean isLinked(MLUser user) {
        return (user != null) && MLWechatUtils.isLinked(user);
    }

    @Override
    public void onLoginButtonClicked(final Activity activity, final int type) {
        MLWechatUtils.logInInBackground(activity,
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
                                    "User signed up and logged in through Wechat!");
                            showDetailActivity(activity, type);
                        } else {
                            Log.d(TAG,
                                    "User logged in through Wechat!");
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
    public void requestMe(Activity activity, final RequestMeCallback doneCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WechatPlatform platform = MLWechatUtils.getPlatform();
                if (platform == null) return;
                platform.requestMe(new Platform.SignRequestCallback() {
                    @Override
                    public void done(final JSONObject profile, final Exception e) {
                        MLUser user = MLUser.getCurrentUser();
                        user.put("wechatProfile", profile);

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
                });
            }
        }).start();
    }

    private void updateViewsWithProfileInfo(RequestMeCallback doneCallback) {
        MLUser currentUser = MLUser.getCurrentUser();
        if (currentUser.get("wechatProfile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("wechatProfile");
            String avatar = null;
            String name = null;
            Pair<String, String> p1 = null;
            Pair<String, String> p2 = null;
            Pair<String, String> p3 = null;
            Pair<String, String> p4 = null;
            try {
                if (userProfile.has("headimgurl")) {
                    avatar = userProfile.getString("headimgurl");
                }
                if (userProfile.has("nickname")) {
                    name = userProfile.getString("nickname");
                }
                if (userProfile.has("city")) {
                    p1 = new Pair<>("city", userProfile.getString("city"));
                }
                if (userProfile.has("sex")) {
                    p2 = new Pair<>("sex", userProfile.getInt("sex") == 1 ? "Male" : "Female");
                }
                if (userProfile.has("country")) {
                    p3 = new Pair<>("country", userProfile.getString("country"));
                }
                if (userProfile.has("language")) {
                    p4 = new Pair<>("language", userProfile.getString("language"));
                }
                doneCallback.onSuccess(avatar, name, p1, p2, p3, p4);
            } catch (JSONException e) {
                doneCallback.onError(e);
            }
        }
    }
}
