package com.maxleap.sample.auth.platforms;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.maxleap.FacebookPlatform;
import com.maxleap.FacebookProvider;
import com.maxleap.LogInCallback;
import com.maxleap.MLFacebookUtils;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.SaveCallback;
import com.maxleap.exception.MLException;
import com.maxleap.sample.auth.RequestMeCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FacebookInterfaces extends SocialInterfaces {

    private static final String TAG = FacebookInterfaces.class.getSimpleName();

    @Override
    public boolean isLinked(MLUser user) {
        return (user != null) && MLFacebookUtils.isLinked(user);
    }

    @Override
    public void onLoginButtonClicked(final Activity activity, final int type) {
        List<String> permissions = Arrays.asList("public_profile",
                FacebookProvider.Permissions.User.ABOUT_ME,
                FacebookProvider.Permissions.User.RELATIONSHIPS,
                FacebookProvider.Permissions.User.BIRTHDAY,
                FacebookProvider.Permissions.User.LOCATION);

        MLFacebookUtils.logInInBackground(permissions, activity,
                new LogInCallback() {
                    @Override
                    public void done(MLUser user, MLException e) {
                        if (e != null) {
                            e.printStackTrace();
                            MLLog.t(e.getMessage());
                            return;
                        }

                        if (user.isNew()) {
                            Log.d(TAG,
                                    "User signed up and logged in through Facebook!");
                            showDetailActivity(activity, type);
                        } else {
                            Log.d(TAG,
                                    "User logged in through Facebook!");
                            showDetailActivity(activity, type);
                        }
                    }
                });
    }

    @Override
    public void onLogoutButtonClicked(Activity activity) {
        // Log the user out with MaxLeap SDK
        MLUser.logOut();

        // Log the user out with Facebook SDK
        LoginManager.getInstance().logOut();

        activity.finish();

        // Go to the login view
        showLoginActivity(activity);
    }

    @Override
    public void requestMe(Activity activity, RequestMeCallback doneCallback) {
        FacebookPlatform platform = MLFacebookUtils.getPlatform();
        if (platform == null) return;
        AccessToken token = new AccessToken(
                platform.getAccessToken(),
                platform.getApplicationId(),
                platform.getPlatformId(),
                null,
                null,
                null,
                new Date(Long.valueOf(platform.getExpires())),
                null
        );
        makeMeRequest(token, doneCallback);
    }

    private void makeMeRequest(AccessToken accessToken, final RequestMeCallback doneCallback) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (user != null) {
                            // Create a JSON object to hold the profile info
                            JSONObject userProfile = new JSONObject();
                            try {
                                System.out.println(user);

                                // Populate the JSON object
                                userProfile.put("facebookId", user.optString("id"));
                                userProfile.put("name", user.optString("name"));
                                if (user.optJSONObject("location") != null) {
                                    userProfile.put("location", user.optJSONObject("location").optString("name"));
                                }
                                if (user.optString("gender") != null) {
                                    userProfile.put("gender",
                                            user.optString("gender"));
                                }
                                if (user.optString("birthday") != null) {
                                    userProfile.put("birthday",
                                            user.optString("birthday"));
                                }
                                if (user.optString("relationship_status") != null) {
                                    userProfile
                                            .put("relationship_status",
                                                    user
                                                            .optString("relationship_status"));
                                }

                                // Save the user profile info in a user property
                                MLUser currentUser = MLUser
                                        .getCurrentUser();
                                currentUser.put("facebookProfile", userProfile);

                                MLUserManager.saveInBackground(currentUser,
                                        new SaveCallback() {

                                            @Override
                                            public void done(
                                                    MLException exception) {
                                                if (exception != null) {
                                                    exception.printStackTrace();
                                                } else {
                                                    MLLog.d(TAG,
                                                            "finish saving");
                                                }
                                            }
                                        });

                                // Show the user info
                                updateViewsWithProfileInfo(doneCallback);
                            } catch (JSONException e) {
                                MLLog.e(TAG,
                                        "Error parsing returned user data.");
                            }

                        } else if (response.getError() != null) {
                            MLLog.e(TAG,
                                    "Some other error: "
                                            + response.getError()
                                            .getErrorMessage());
                        }
                    }
                });
        request.executeAsync();

    }

    private void updateViewsWithProfileInfo(RequestMeCallback doneCallback) {
        MLUser currentUser = MLUser.getCurrentUser();
        if (currentUser.get("facebookProfile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("facebookProfile");
            String avatar = null;
            String name = null;
            Pair<String, String> p1 = null;
            Pair<String, String> p2 = null;
            Pair<String, String> p3 = null;
            Pair<String, String> p4 = null;
            try {
                if (userProfile.has("facebookId")) {
                    String facebookId = userProfile.get("facebookId")
                            .toString();
                    avatar = "http://graph.facebook.com/" + facebookId + "/picture";
                }
                if (userProfile.has("name")) {
                    name = userProfile.getString("name");
                }
                if (userProfile.has("location")) {
                    p1 = new Pair<>("location", userProfile.getString("location"));
                }
                if (userProfile.has("gender")) {
                    p2 = new Pair<>("gender", userProfile.getString("gender"));
                }
                if (userProfile.has("birthday")) {
                    p3 = new Pair<>("birthday", userProfile.getString("birthday"));
                }
                if (userProfile.has("relationship_status")) {
                    p4 = new Pair<>("relationship_status", userProfile.getString("relationship_status"));
                }
                doneCallback.onSuccess(avatar, name, p1, p2, p3, p4);
            } catch (JSONException e) {
                doneCallback.onError(e);
            }

        }
    }

}
