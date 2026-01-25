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
            // Tạo hoặc lấy Master Key để mã hóa
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm lưu Token
    public static void saveToken(String token) {
        sharedPreferences.edit().putString("auth_token", token).apply();
    }

    // Hàm lấy Token ra dùng
    public static String getToken() {
        return sharedPreferences.getString("auth_token", null);
    }

    // Hàm xóa Token (khi Đăng xuất)
    public static void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
