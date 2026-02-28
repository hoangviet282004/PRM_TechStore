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

    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_ROLE = "user_role"; // FIX: Thêm key để lưu Role

    private static final String KEY_USERNAME = "username";

    public static void init(Context context) {
        if (sharedPreferences == null) {
            try {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                sharedPreferences = EncryptedSharedPreferences.create(
                        PREF_NAME, masterKeyAlias, context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) { e.printStackTrace(); }
        }
    }

    public static void saveTokens(String access, String refresh) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    // THÊM: Hàm lưu Role khi Login thành công
    public static void saveUserRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }

    public static String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS, null);
    }

    public static String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH, null);
    }

    // THÊM: Hàm lấy Role để phân luồng Chat
    public static String getUserRole() {
        return sharedPreferences.getString(KEY_ROLE, "Customer"); // Mặc định là Customer
    }


    public static void saveUsername(String username) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public static String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public static void clearAll() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }
}