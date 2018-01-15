package com.example.banwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.banwidget.data.ChinaDate;
import com.example.banwidget.data.Weather_sojson;
import com.example.banwidget.tool.BanDB;
import com.example.banwidget.tool.MySampleDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * 变量使用需要注意，有分分钟被清掉数据的时候
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MyAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "MyAppWidgetProvider";
    private String temp = "";
    private BanDB banDB;
    private Weather_sojson weather;
    private MySampleDate mySampleDate;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (banDB != null) {
            banDB.onDestory();
            banDB = null;
        }
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

        //每天更新发送的通知
        Intent intent = new Intent("com.stone.action.start");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        System.out.println("onUpdate");
        if (banDB == null) {
            try {
                banDB = new BanDB(context);
            } catch (Exception e) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                return;
            }
        }
        if (weather == null) {
            weather = new Weather_sojson(context);
        }
        MySampleDate.getInstance(context, "set");

        temp = ChinaDate.today();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(Uri.parse("id:" + R.id.open));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.open, pendingIntent);
        remoteViews.setTextViewText(R.id.dongli_text, "农历:" + ChinaDate.today());

        remoteViews.setViewVisibility(R.id.check, MySampleDate.getBooleanValue("check") ? View.GONE : View.VISIBLE);
        Intent intent2 = new Intent(context, TimerWidgetService.class);
        intent2.setAction("com.ban.click");
        intent2.setData(Uri.parse("id:" + R.id.check));
        PendingIntent pendingIntent2 = PendingIntent.getService(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.check, pendingIntent2);

        String weatherInfo = weather.getWeatherAndTemperatureString();
        if (!TextUtils.isEmpty(weatherInfo)) {
            remoteViews.setTextViewText(R.id.weather_text, weatherInfo);
        }


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

        String action = intent.getAction();
        Log.i(TAG, action);
        MySampleDate.getInstance(context, "set");
        if ("com.ban.click".equals(action)) {
            MySampleDate.saveInfo("check", true);
            MySampleDate.saveInfo("checkTime", System.currentTimeMillis());
            Toast.makeText(context.getApplicationContext(), "checked", Toast.LENGTH_LONG).show();
        } else if ("com.stone.action.start".equals(action)) {
            Long last = MySampleDate.getLongValue("checkTime");
            if (last != 0) {
                Calendar calendarLast = Calendar.getInstance();
                calendarLast.setTime(new Date(last));
                Calendar calendarNow = Calendar.getInstance();
                calendarNow.setTime(new Date());
                if (calendarLast.get(Calendar.DAY_OF_MONTH) != calendarNow.get(Calendar.DAY_OF_MONTH)) {
                    MySampleDate.deleteListInfo("check");
                }
            }
        } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            new Weather_sojson(context).getJsonFromNet();
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, MyAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void checkFestival(AppWidgetManager appWidgetManager,
                               RemoteViews remoteViews, int[] appWidgetIds) {
        StringBuffer buffer = new StringBuffer();
        //农历节
        ArrayList<String> dongLiJie = banDB.getNongLiFestivalName(ChinaDate.todayNumberString());
        for (String s : dongLiJie) {
            if (!s.equals("")) {
                buffer.append(s + "和");
            }
        }
        //新历节
        ArrayList<String> xinLiJie = banDB.getXinLiFestivalName(ChinaDate.getXinLiTodayString());
        for (String s : xinLiJie) {
            if (!s.equals("")) {
                buffer.append(s + "和");
            }
        }
        //特殊节，如几月的第几个星期几
        int[] data = ChinaDate.todaySpecific();
        String specificJie = banDB.getSpecificFestivalName(data[0], data[1],
                data[2]);
        if (!specificJie.equals("")) {
            buffer.append(specificJie + "和");
        }
        //节气
        String jieQi = ChinaDate.getJieQi();
        if (!TextUtils.isEmpty(jieQi)) {
            buffer.append(jieQi + "和");
        }
        //总显示
        if (buffer.length() != 0) {
            String showString = buffer.substring(0, buffer.length() - 1);
            remoteViews.setTextViewText(R.id.jieri_text, "今天是:" + showString);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }
}
