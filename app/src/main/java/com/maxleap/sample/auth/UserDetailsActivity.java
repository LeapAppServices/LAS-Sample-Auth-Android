package com.maxleap.sample.auth;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.maxleap.MLUser;
import com.maxleap.sample.auth.platforms.FacebookInterfaces;
import com.maxleap.sample.auth.platforms.SocialInterfaces;
import com.maxleap.sample.auth.platforms.TwitterInterfaces;
import com.maxleap.sample.auth.platforms.WechatInterfaces;
import com.maxleap.sample.auth.platforms.WeiboInterfaces;


public class UserDetailsActivity extends AppCompatActivity {

    private static final String TAG = UserDetailsActivity.class.getSimpleName();

    public static final String EXTRA_TYPE = "type";

    public static final int FACEBOOK = 0;
    public static final int TWITTER = 1;
    public static final int WEIBO = 2;
    public static final int WECHAT = 3;

    private ImageView mUserProfilePicture;
    private TextView mUsernameView;
    private TextView mLabel1;
    private TextView mLabel2;
    private TextView mLabel3;
    private TextView mLabel4;
    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private SocialInterfaces interfaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userdetails);

        mUserProfilePicture = (ImageView) findViewById(R.id.userProfilePicture);
        mUsernameView = (TextView) findViewById(R.id.userName);

        mLabel1 = (TextView) findViewById(R.id.label1);
        mLabel2 = (TextView) findViewById(R.id.label2);
        mLabel3 = (TextView) findViewById(R.id.label3);
        mLabel4 = (TextView) findViewById(R.id.label4);

        mText1 = (TextView) findViewById(R.id.text1);
        mText2 = (TextView) findViewById(R.id.text2);
        mText3 = (TextView) findViewById(R.id.text3);
        mText4 = (TextView) findViewById(R.id.text4);

        int type = getIntent().getIntExtra(EXTRA_TYPE, FACEBOOK);
        switch (type) {
            case FACEBOOK:
                interfaces = new FacebookInterfaces();
                break;
            case WEIBO:
                interfaces = new WeiboInterfaces();
                break;
            case TWITTER:
                interfaces = new TwitterInterfaces();
                break;
            case WECHAT:
                interfaces = new WechatInterfaces();
                break;
        }

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interfaces.onLogoutButtonClicked(UserDetailsActivity.this);
            }
        });

        // Fetch Social user info if the session is active
        MLUser user = MLUser.getCurrentUser();

        if (interfaces.isLinked(user)) {
            interfaces.requestMe(this, new RequestMeCallback() {
                @Override
                public void onSuccess(final String avatar,
                                      final String name,
                                      final Pair<String, String> p1,
                                      final Pair<String, String> p2,
                                      final Pair<String, String> p3,
                                      final Pair<String, String> p4) {
                    mUserProfilePicture.post(new Runnable() {
                        @Override
                        public void run() {
                            if (avatar != null) {
                                Glide.with(UserDetailsActivity.this)
                                        .load(avatar)
                                        .into(mUserProfilePicture);
                            }
                            if (name != null) {
                                mUsernameView.setText(name);
                            } else {
                                mUsernameView.setText("");
                            }
                            if (p1 != null) {
                                mLabel1.setText(p1.first);
                                mText1.setText(p1.second);
                            } else {
                                mLabel1.setVisibility(View.GONE);
                                mText1.setVisibility(View.GONE);
                            }
                            if (p2 != null) {
                                mLabel2.setText(p2.first);
                                mText2.setText(p2.second);
                            } else {
                                mLabel2.setVisibility(View.GONE);
                                mText2.setVisibility(View.GONE);
                            }
                            if (p3 != null) {
                                mLabel3.setText(p3.first);
                                mText3.setText(p3.second);
                            } else {
                                mLabel3.setVisibility(View.GONE);
                                mText3.setVisibility(View.GONE);
                            }
                            if (p4 != null) {
                                mLabel4.setText(p4.first);
                                mText4.setText(p4.second);
                            } else {
                                mLabel4.setVisibility(View.GONE);
                                mText4.setVisibility(View.GONE);
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
