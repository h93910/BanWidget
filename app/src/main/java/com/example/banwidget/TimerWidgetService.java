package com.example.banwidget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

public class TimerWidgetService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
         * do some thing
		 */
        // 发送广播通知 widget 更新 状态
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            sendBroadcast(new Intent(intent.getAction()).putExtra("time", System.currentTimeMillis()));
            Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_LONG).show();
        }
        return Service.START_STICKY;
    }
}
