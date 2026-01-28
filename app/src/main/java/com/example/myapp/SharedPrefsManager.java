package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPrefsManager {
    private static final String PREF_NAME = "SecureData";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME, masterKeyAlias, context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) { e.printStackTrace(); }
    }

    // Lưu cặp đôi token
    public static void saveTokens(String access, String refresh) {
        sharedPreferences.edit().putString("access_token", access).apply();
        sharedPreferences.edit().putString("refresh_token", refresh).apply();
    }

    public static String getAccessToken() {
        return sharedPreferences.getString("access_token", null);
    }

    public static String getRefreshToken() {
        return sharedPreferences.getString("refresh_token", null);
    }

    public static void clear() {
        sharedPreferences.edit().clear().apply();
    }
}