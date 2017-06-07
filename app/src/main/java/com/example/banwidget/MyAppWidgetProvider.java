package com.example.banwidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract.Contacts.Data;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.example.banwidget.R;

public class MyAppWidgetProvider extends AppWidgetProvider {
    private String temp = "";
    private BanDB banDB;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("onEnabled");

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        System.out.println(c.getTimeInMillis());

        Intent intent = new Intent("com.stone.action.start");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0);

        AlarmManager am = (AlarmManager) context
                .getSystemService(context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        System.out.println("onUpdate");
        if (banDB == null) {
            banDB = new BanDB(context);
        }

        temp = ChinaDate.today();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setOnClickPendingIntent(R.id.open, pendingIntent);
        remoteViews
                .setTextViewText(R.id.dongli_text, "农历:" + ChinaDate.today());
        // ComponentName componentName = new ComponentName(context,
        // MyAppWidgetProvider.class);
        remoteViews.setTextViewText(R.id.jieri_text, "无节日");// 没节日先设为空
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        checkFestival(appWidgetManager, remoteViews, appWidgetIds);
        // Timer timer = new Timer();
        // timer.schedule(new MyTime(appWidgetManager, remoteViews,
        // appWidgetIds),
        // 1000, 1000);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals("com.stone.action.start")) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);

            ComponentName componentName = new ComponentName(context,
                    MyAppWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager
                    .getAppWidgetIds(componentName);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    class MyTime extends TimerTask {
        private AppWidgetManager appWidgetManager;
        private RemoteViews remoteViews;
        private int[] appWidgetIds;

        public MyTime(AppWidgetManager appWidgetManager,
                      RemoteViews remoteViews, int[] appWidgetIds) {
            this.appWidgetManager = appWidgetManager;
            this.remoteViews = remoteViews;
            this.appWidgetIds = appWidgetIds;
        }

        @Override
        public void run() {
            remoteViews.setTextViewText(R.id.dongli_text,
                    System.currentTimeMillis() + "");

            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }

    private void checkFestival(AppWidgetManager appWidgetManager,
                               RemoteViews remoteViews, int[] appWidgetIds) {
        StringBuffer buffer = new StringBuffer();
        ArrayList<String> dongLiJie = banDB.getNongLiFestivalName(ChinaDate.todayNumberString());
        for (String s : dongLiJie) {
            if (!s.equals("")) {
                buffer.append(s + "和");
            }
        }
        ArrayList<String> xinLiJie = banDB.getXinLiFestivalName(ChinaDate.getXinLiTodayString());
        for (String s : xinLiJie) {
            if (!s.equals("")) {
                buffer.append(s + "和");
            }
        }

        int[] data = ChinaDate.todaySpecific();
        String specificJie = banDB.getSpecificFestivalName(data[0], data[1],
                data[2]);
        if (!specificJie.equals("")) {
            buffer.append(specificJie + "和");
        }

        String jieQi = ChinaDate.getJieQi();
        if (!TextUtils.isEmpty(jieQi)) {
            buffer.append(jieQi + "和");
        }

        if (buffer.length() != 0) {
            String showString = buffer.substring(0, buffer.length() - 1);
            remoteViews.setTextViewText(R.id.jieri_text, "今天是:" + showString);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }
}
