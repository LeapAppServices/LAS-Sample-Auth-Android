package as.leap.sample.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import as.leap.LASFacebookUtils;
import as.leap.LASUser;
import as.leap.callback.LogInCallback;
import as.leap.exception.LASException;
import as.leap.external.social.facebook.FacebookProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        // Check if there is a currently logged in user
        // and they are linked to a Facebook account.
        LASUser currentUser = LASUser.getCurrentUser();
        if ((currentUser != null) && LASFacebookUtils.isLinked(currentUser)) {
            // Go to the user info activity
            showUserDetailsActivity();
        }
    }

    private void onLoginButtonClicked() {

        List<String> permissions = Arrays.asList("public_profile",
                FacebookProvider.Permissions.User.ABOUT_ME,
                FacebookProvider.Permissions.User.RELATIONSHIPS,
                FacebookProvider.Permissions.User.BIRTHDAY,
                FacebookProvider.Permissions.User.LOCATION);

        LASFacebookUtils.logInInBackground(permissions, this,
                new LogInCallback<LASUser>() {
                    @Override
                    public void done(LASUser user, LASException err) {
                        // LoginActivity.this.progressDialog.dismiss();
                        if (err != null) {
                            err.printStackTrace();
                            return;
                        }
                        if (user == null) {
                            Log.d(TAG,
                                    "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {// is new
                            Log.d(TAG,
                                    "User signed up and logged in through Facebook!");
                            showUserDetailsActivity();
                        } else {
                            Log.d(TAG,
                                    "User logged in through Facebook!");
                            showUserDetailsActivity();
                        }
                    }
                });
    }

    private void showUserDetailsActivity() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        startActivity(intent);
    }
}
