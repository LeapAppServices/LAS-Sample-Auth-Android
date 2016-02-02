package com.maxleap.sample.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.maxleap.MLUser;
import com.maxleap.MaxLeap;
import com.maxleap.sample.auth.platforms.FacebookInterfaces;
import com.maxleap.sample.auth.platforms.SocialInterfaces;
import com.maxleap.sample.auth.platforms.TwitterInterfaces;
import com.maxleap.sample.auth.platforms.WechatInterfaces;
import com.maxleap.sample.auth.platforms.WeiboInterfaces;

public class LoginActivity extends AppCompatActivity {

    private SocialInterfaces currentInterfaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button facebookLoginButton = (Button) findViewById(R.id.facebookLoginButton);
        Button twitterLoginButton = (Button) findViewById(R.id.twitterLoginButton);
        Button weiboLoginButton = (Button) findViewById(R.id.weiboLoginButton);
        Button wechatLoginButton = (Button) findViewById(R.id.wechatLoginButton);

        if (App.REGION.endsWith(MaxLeap.REGION_CN)) {
            facebookLoginButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);
        } else {
            weiboLoginButton.setVisibility(View.GONE);
            wechatLoginButton.setVisibility(View.GONE);
        }

        facebookLoginButton.setOnClickListener(onLoginListener(new FacebookInterfaces(), UserDetailsActivity.FACEBOOK));
        twitterLoginButton.setOnClickListener(onLoginListener(new TwitterInterfaces(), UserDetailsActivity.TWITTER));
        weiboLoginButton.setOnClickListener(onLoginListener(new WeiboInterfaces(), UserDetailsActivity.WEIBO));
        wechatLoginButton.setOnClickListener(onLoginListener(new WechatInterfaces(), UserDetailsActivity.WECHAT));
    }

    private View.OnClickListener onLoginListener(final SocialInterfaces interfaces, final int type) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentInterfaces = interfaces;
                MLUser currentUser = MLUser.getCurrentUser();
                if (interfaces.isLinked(currentUser)) {
                    interfaces.showDetailActivity(LoginActivity.this, type);
                    return;
                }
                interfaces.onLoginButtonClicked(LoginActivity.this, type);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentInterfaces != null) {
            currentInterfaces.onActivityResult(requestCode, resultCode, data);
        }
    }
}
