package com.example.banwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.banwidget.data.ChinaDate;
import com.example.banwidget.data.Weather_sojson;
import com.example.banwidget.tool.BanDB;
import com.example.banwidget.data.FY4A;
import com.example.banwidget.tool.MySampleDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * 变量使用需要注意，有分分钟被清掉数据的时候
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MyAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "MyAppWidgetProvider";
    private volatile boolean fy = false;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("onEnabled");
        // sendAlarmBroadcast(context);
        super.onEnabled(context);
    }

    private void sendAlarmBroadcast(Context context) {
//        Calendar c = Calendar.getInstance();
//        System.out.println(c.getTimeInMillis());

        //每天更新发送的通知
        Intent intent = new Intent("on.enable.action");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);// 表示包含未启动的App
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
        long nextTime = System.currentTimeMillis() + 60000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setExactAndAllowWhileIdle(ALARM_TYPE, nextTime, pendingIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            am.setExact(ALARM_TYPE, nextTime, pendingIntent);
        else
            am.set(ALARM_TYPE, nextTime, pendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        System.out.println("onUpdate");
        BanDB banDB = new BanDB(context);

        Weather_sojson weather = new Weather_sojson(context);
        MySampleDate.getInstance(context, "set");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        //打个主页面
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setData(Uri.parse("id:" + R.id.open));
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //每天更新发送的通知
        Intent intent = new Intent("on.enable.action");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);// 表示包含未启动的App
        intent.setComponent(new ComponentName(context, MyAppWidgetProvider.class));//api 8.0以上发给自己的必须写
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.open, pendingIntent);
        remoteViews.setTextViewText(R.id.dongli_text, "农历:" + ChinaDate.today());

        String nowTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        remoteViews.setTextViewText(R.id.click_time_text, "上次更新:" + nowTimeString);

        //主页checkbox功能
        // remoteViews.setViewVisibility(R.id.check, MySampleDate.getBooleanValue("check") ? View.GONE : View.VISIBLE);
//        Intent intent2 = new Intent(context, TimerWidgetService.class);
//        intent2.setAction("com.ban.click");
//        intent2.setData(Uri.parse("id:" + R.id.check));
//        PendingIntent pendingIntent2 = PendingIntent.getService(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.check, pendingIntent2);

        String weatherInfo = weather.getWeatherAndTemperatureString();
        if (!TextUtils.isEmpty(weatherInfo)) {
            remoteViews.setTextViewText(R.id.weather_text, weatherInfo);
        }
        Bitmap weatherIcon = weather.getWeatherIcon();
        remoteViews.setImageViewBitmap(R.id.weather_icon, weatherIcon);
        remoteViews.setViewVisibility(R.id.weather_icon_bg, weatherIcon == null ? View.GONE : View.VISIBLE);

        remoteViews.setTextViewText(R.id.jieri_text, "无节日");// 没节日先设为空
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        checkFestival(banDB, appWidgetManager, remoteViews, appWidgetIds);

        banDB.onDestory();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        Log.i(TAG, action);
        //  MySampleDate.getInstance(context, "set");
        if ("com.ban.click".equals(action)) {
            //      MySampleDate.saveInfo("check", true);
            MySampleDate.saveInfo("checkTime", System.currentTimeMillis());
            Toast.makeText(context.getApplicationContext(), "checked", Toast.LENGTH_LONG).show();
        } else if ("on.enable.action".equals(action)) {
            //因为setWindow只执行一次，所以要重新定义闹钟实现循环。
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                sendAlarmBroadcast(context);
//            }
            Toast.makeText(context.getApplicationContext(), "on.enable.action", Toast.LENGTH_LONG).show();
        } else if ("com.stone.action.start".equals(action)) {
            Toast.makeText(context.getApplicationContext(), "on.enable.action.start", Toast.LENGTH_LONG).show();
            //            Long last = MySampleDate.getLongValue("checkTime");
//            if (last != 0) {
//                Calendar calendarLast = Calendar.getInstance();
//                calendarLast.setTime(new Date(last));
//                Calendar calendarNow = Calendar.getInstance();
//                calendarNow.setTime(new Date());
//                if (calendarLast.get(Calendar.DAY_OF_MONTH) != calendarNow.get(Calendar.DAY_OF_MONTH)) {
//             //       MySampleDate.deleteListInfo("check");
//                }
//            }
        } else if ("ban.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            new Weather_sojson(context).getJsonFromNet();
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, MyAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        onUpdate(context, appWidgetManager, appWidgetIds);

        if (checkNetAvailable(context)) {
            if (!fy) {
                fy = true;
                MyApplication.ThreadExecutor.execute(() -> {
                    SystemClock.sleep(10 * 1000);
                    FY4A fy = new FY4A(context);
                    fy.execute();
                    fy.onDestroy();
                    SystemClock.sleep(2 * 60 * 1000);
                    this.fy = false;
                });
            }
        }
    }

    private void checkFestival(BanDB banDB, AppWidgetManager appWidgetManager,
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

    private boolean checkNetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }
}
