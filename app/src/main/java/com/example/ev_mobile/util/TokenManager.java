package com.example.ev_mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_AUTH_TOKEN = "token";
    private static SharedPreferences prefs;

    public TokenManager(Context context) {
        if (prefs == null) {  // Ensure initialized only once
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    // Save token (e.g. after login)
    public void saveToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    // Get token
    public static String getToken() {
        if (prefs == null) {
            throw new IllegalStateException("TokenManager not initialized. Call constructor with Context first.");
        }
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    // Clear token (logout)
    public void clearToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply();
    }

    public boolean hasToken() {
        return getToken() != null;
    }
}