package com.example.myapp;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefsManager.init(getApplicationContext());
    }
}
