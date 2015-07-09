package as.leap.sample.auth;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import as.leap.LASFacebookUtils;
import as.leap.LASLog;
import as.leap.LASUser;
import as.leap.LASUserManager;
import as.leap.callback.SaveCallback;
import as.leap.exception.LASException;
import as.leap.external.social.facebook.FacebookPlatform;

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
        LASUser user = LASUser.getCurrentUser();

        if (LASFacebookUtils.isLinked(user)) {
            FacebookPlatform platform = LASFacebookUtils.getPlatform();
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
                                LASUser currentUser = LASUser
                                        .getCurrentUser();
                                currentUser.put("profile", userProfile);

                                LASUserManager.saveInBackground(currentUser,
                                        new SaveCallback() {

                                            @Override
                                            public void done(
                                                    LASException exception) {
                                                if (exception != null) {
                                                    exception.printStackTrace();
                                                } else {
                                                    LASLog.d(TAG,
                                                            "finish saving");
                                                }
                                            }
                                        });

                                // Show the user info
                                updateViewsWithProfileInfo();
                            } catch (JSONException e) {
                                LASLog.e(TAG,
                                        "Error parsing returned user data.");
                            }

                        } else if (response.getError() != null) {
                            LASLog.e(TAG,
                                    "Some other error: "
                                            + response.getError()
                                            .getErrorMessage());
                        }
                    }
                });
        request.executeAsync();

    }

    private void updateViewsWithProfileInfo() {
        LASUser currentUser = LASUser.getCurrentUser();
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
                LASLog.d(TAG,
                        "Error parsing saved user data." + e.getMessage());
            }

        }
    }

    private void onLogoutButtonClicked() {
        // Log the user out with LAS SDK
        LASUser.logOut();

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
