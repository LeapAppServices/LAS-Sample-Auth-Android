package com.maxleap.sample.auth.platforms;

import android.app.Activity;
import android.content.Intent;

import com.maxleap.MLUser;
import com.maxleap.sample.auth.LoginActivity;
import com.maxleap.sample.auth.RequestMeCallback;
import com.maxleap.sample.auth.UserDetailsActivity;

public abstract class SocialInterfaces {

    public abstract boolean isLinked(MLUser user);

    public abstract void onLoginButtonClicked(Activity activity, int type);

    public abstract void onLogoutButtonClicked(Activity activity);

    public void showDetailActivity(Activity activity, int type) {
        Intent intent = new Intent(activity, UserDetailsActivity.class);
        intent.putExtra(UserDetailsActivity.EXTRA_TYPE, type);
        activity.startActivity(intent);
        activity.finish();
    }

    public void showLoginActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public abstract void requestMe(Activity activity, RequestMeCallback doneCallback);

    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
    }
}
