package as.leap.sample.auth;

import com.facebook.*;

import as.leap.LASFacebookUtils;
import as.leap.LASUser;
import as.leap.LASUserManager;
import as.leap.callback.SaveCallback;
import as.leap.exception.LASException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.LoginActivity;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

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

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutButtonClicked();
            }
        });

        // Fetch Facebook user info if the session is active
        Session session = Session.getActiveSession();
        LASUser user = LASUser.getCurrentUser();

        if (session == null && LASFacebookUtils.isLinked(user)) {
            String accessToken = LASFacebookUtils.getPlatform().getAccessToken();
            String expires = LASFacebookUtils.getPlatform().getExpires();

            TokenCachingStrategy tcs = new SharedPreferencesTokenCachingStrategy(this);
            Bundle data = tcs.load();
            TokenCachingStrategy.putToken(data, accessToken);
            TokenCachingStrategy.putExpirationMilliseconds(data, Long.parseLong(expires));
            tcs.save(data);

            session = new Session.Builder(this).setApplicationId(App.FACEBOOK_APP_ID).
                    setTokenCachingStrategy(tcs).build();

            session.openForRead(new Session.OpenRequest(this).setCallback(new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    if (session.isOpened()) {
                        makeMeRequest();
                    }
                }
            }));
            Session.setActiveSession(session);
        } else if (session != null && session.isOpened()) {
            makeMeRequest();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private void makeMeRequest() {
        Request request = Request.newMeRequest(Session.getActiveSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            // Create a JSON object to hold the profile info
                            JSONObject userProfile = new JSONObject();
                            try {
                                // Populate the JSON object
                                userProfile.put("facebookId", user.getId());
                                userProfile.put("name", user.getName());
                                if (user.getLocation() != null
                                        && user.getLocation().getProperty(
                                        "name") != null) {
                                    userProfile.put("location", user
                                            .getLocation().getProperty("name"));
                                }
                                if (user.getProperty("gender") != null) {
                                    userProfile.put("gender",
                                            user.getProperty("gender"));
                                }
                                if (user.getBirthday() != null) {
                                    userProfile.put("birthday",
                                            user.getBirthday());
                                }
                                if (user.getProperty("relationship_status") != null) {
                                    userProfile
                                            .put("relationship_status",
                                                    user
                                                            .getProperty("relationship_status"));
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
                                                    Log.d(TAG,
                                                            "finish saving");
                                                }
                                            }
                                        });

                                // Show the user info
                                updateViewsWithProfileInfo();
                            } catch (JSONException e) {
                                Log.d(TAG,
                                        "Error parsing returned user data.");
                            }

                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                    || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.d(TAG,
                                        "The facebook session was invalidated.");
                                onLogoutButtonClicked();
                            } else {
                                Log.d(TAG,
                                        "Some other error: "
                                                + response.getError()
                                                .getErrorMessage());
                            }
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
                Log.d(TAG,
                        "Error parsing saved user data." + e.getMessage());
            }

        }
    }

    private void onLogoutButtonClicked() {
        // Log the user out
        LASUser.logOut();

        finish();

        // Go to the login view
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
