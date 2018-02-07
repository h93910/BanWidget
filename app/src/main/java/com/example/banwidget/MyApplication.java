package com.example.banwidget;

import android.app.Application;

import com.example.banwidget.tool.CrashHandler;

/**
 * Created by Ban on 2018/1/23.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
