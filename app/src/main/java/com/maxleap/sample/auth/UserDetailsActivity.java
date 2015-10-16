package com.maxleap.sample.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.maxleap.MLFacebookUtils;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.SaveCallback;
import com.maxleap.exception.MLException;
import com.maxleap.social.facebook.FacebookPlatform;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class UserDetailsActivity extends AppCompatActivity {

    private static final String TAG = UserDetailsActivity.class.getSimpleName();

    private ProfilePictureView mUserProfilePictureView;
    private TextView mUserNameView;
    private TextView mUserLocationView;
    private TextView mUserGenderView;
    private TextView mUserDateOfBirthView;
    private TextView mUserRelationshipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userdetails);

        mUserProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
        mUserNameView = (TextView) findViewById(R.id.userName);
        mUserLocationView = (TextView) findViewById(R.id.userLocation);
        mUserGenderView = (TextView) findViewById(R.id.userGender);
        mUserDateOfBirthView = (TextView) findViewById(R.id.userDateOfBirth);
        mUserRelationshipView = (TextView) findViewById(R.id.userRelationship);

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutButtonClicked();
            }
        });

        // Fetch Facebook user info if the session is active
        MLUser user = MLUser.getCurrentUser();

        if (MLFacebookUtils.isLinked(user)) {
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
            makeMeRequest(token);
        }
    }

    private void makeMeRequest(AccessToken accessToken) {
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
                                currentUser.put("profile", userProfile);

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
                                updateViewsWithProfileInfo();
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

    private void updateViewsWithProfileInfo() {
        MLUser currentUser = MLUser.getCurrentUser();
        if (currentUser.get("profile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {
                if (userProfile.has("facebookId")) {
                    String facebookId = userProfile.get("facebookId")
                            .toString();
                    mUserProfilePictureView.setProfileId(facebookId);
                } else {
                    // Show the default, blank user profile picture
                    mUserProfilePictureView.setProfileId(null);
                }
                if (userProfile.has("name")) {
                    mUserNameView.setText(userProfile.getString("name"));
                } else {
                    mUserNameView.setText("");
                }
                if (userProfile.has("location")) {
                    mUserLocationView.setText(userProfile.getString("location"));
                } else {
                    mUserLocationView.setText("");
                }
                if (userProfile.has("gender")) {
                    mUserGenderView.setText(userProfile.getString("gender"));
                } else {
                    mUserGenderView.setText("");
                }
                if (userProfile.has("birthday")) {
                    mUserDateOfBirthView.setText(userProfile
                            .getString("birthday"));
                } else {
                    mUserDateOfBirthView.setText("");
                }
                if (userProfile.has("relationship_status")) {
                    mUserRelationshipView.setText(userProfile
                            .getString("relationship_status"));
                } else {
                    mUserRelationshipView.setText("");
                }
            } catch (JSONException e) {
                MLLog.d(TAG,
                        "Error parsing saved user data." + e.getMessage());
            }

        }
    }

    private void onLogoutButtonClicked() {
        // Log the user out with LAS SDK
        MLUser.logOut();

        // Log the user out with Facebook SDK
        LoginManager.getInstance().logOut();

        finish();

        // Go to the login view
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
