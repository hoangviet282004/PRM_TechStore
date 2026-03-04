package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPrefsManager {
    private static final String PREF_NAME = "SecureData";
    private static SharedPreferences sharedPreferences;

    private static final String KEY_ACCESS    = "access_token";
    private static final String KEY_REFRESH   = "refresh_token";
    private static final String KEY_ROLE      = "user_role";
    private static final String KEY_USERNAME  = "username";

    public static void init(Context context) {
        if (sharedPreferences == null) {
            try {
                MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();
                sharedPreferences = EncryptedSharedPreferences.create(
                        context.getApplicationContext(),
                        PREF_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
            }
        }
    }

    // Guard: ensures init() was called before any read/write
    private static SharedPreferences prefs() {
        if (sharedPreferences == null) {
            throw new IllegalStateException("SharedPrefsManager not initialized. Call init() in Application.onCreate().");
        }
        return sharedPreferences;
    }

    // Atomic save of all login data in a single editor commit
    public static void saveLoginData(String access, String refresh, String role, String username) {
        prefs().edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .putString(KEY_ROLE, role)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public static void saveTokens(String access, String refresh) {
        prefs().edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    public static void saveUserRole(String role) {
        prefs().edit().putString(KEY_ROLE, role).apply();
    }

    public static String getAccessToken() {
        return prefs().getString(KEY_ACCESS, null);
    }

    public static String getRefreshToken() {
        return prefs().getString(KEY_REFRESH, null);
    }

    // Returns null if no role saved — callers should handle null explicitly
    public static String getUserRole() {
        return prefs().getString(KEY_ROLE, null);
    }

    public static void saveUsername(String username) {
        prefs().edit().putString(KEY_USERNAME, username).apply();
    }

    public static String getUsername() {
        return prefs().getString(KEY_USERNAME, "");
    }

    public static void clearAll() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }
}
