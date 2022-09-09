package com.example.banwidget

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceActivity
import androidx.preference.Preference

class SettingActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = "setting";
        addPreferencesFromResource(R.xml.setting);
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent("com.stone.action.start")
        intent.component = ComponentName(this, MyAppWidgetProvider::class.java) //api 8.0以上发给自己的必须写
        sendBroadcast(intent)
    }
}