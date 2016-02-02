package com.maxleap.sample.auth;

import android.support.v4.util.Pair;

public interface RequestMeCallback {
    void onSuccess(String avatar,
                   String username,
                   Pair<String, String> p1,
                   Pair<String, String> p2,
                   Pair<String, String> p3,
                   Pair<String, String> p4);

    void onError(Exception e);
}
