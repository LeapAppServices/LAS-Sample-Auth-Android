package com.maxleap.sample.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.maxleap.LogInCallback;
import com.maxleap.MLFacebookUtils;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.exception.MLException;
import com.maxleap.social.facebook.FacebookProvider;

import java.util.Arrays;
import java.util.List;

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
        MLUser currentUser = MLUser.getCurrentUser();
        if ((currentUser != null) && MLFacebookUtils.isLinked(currentUser)) {
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

        MLFacebookUtils.logInInBackground(permissions, this,
                new LogInCallback<MLUser>() {
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
        finish();
    }
}
