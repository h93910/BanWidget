package com.example.banwidget.data;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.crossbow.volley.toolbox.Crossbow;
import com.example.banwidget.R;
import com.example.banwidget.tool.DataTool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;

/**
 * Created by Ban on 2017/12/28.
 */

public class Weather_sojson {
    private static final String TAG = "Weather_sojson";

    private Context context;
    private DataTool dataTool;
    private String city = "深圳市";
    private JsonArray weatherInfos;

    public Weather_sojson(Context context) {
        this.context = context;
        dataTool = new DataTool(context, "date_weather");
    }

    public String getCity() {
        getInfo();
        return city;
    }

    private void getInfo() {
        String info = dataTool.getText();
        if (TextUtils.isEmpty(info)) return;
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(info, JsonObject.class);
        if (object.get("city") != null)
            city = object.get("city").getAsString();
        if (object.get("forecast") != null)
            weatherInfos = object.get("forecast").getAsJsonArray();
    }

    public String getWeatherAndTemperatureString() {
        getInfo();
        if (weatherInfos == null) {
            getJsonFromNet();
        } else {
            return getInfoFromLocal();
        }
        return null;
    }

    public void getJsonFromNet() {
        String url = "http://www.sojson.com/open/api/weather/json.shtml?city=" + getCity();
        Log.d(TAG, url);
        StringRequest request = new StringRequest(url, this::getInfoFromNet
                , error -> {
            //Error handling
        });

        Crossbow.get(context).async(request);
    }

    private String getInfoFromLocal() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer dayKey = new StringBuffer();
        dayKey.append(String.format("%02d日", calendar.get(Calendar.DAY_OF_MONTH)));
        dayKey.append(context.getResources().getStringArray(R.array.week)[calendar.get(Calendar.DAY_OF_WEEK) - 1]);

        String key = dayKey.toString();
        for (JsonElement weatherInfo : weatherInfos) {
            JsonObject object = weatherInfo.getAsJsonObject();
            if (object.get("date").getAsString().equals(key)) {
                String low = object.get("low").getAsString().split(" ")[1];
                String high = object.get("high").getAsString().split(" ")[1];

                StringBuffer infoString = new StringBuffer();
                infoString.append(city + " " + object.get("type").getAsString() + " ");
                infoString.append(low + "至" + high);
                return infoString.toString();
            }
        }
        setCity(city);
        return "";
    }

    private void getInfoFromNet(String text) {
        Log.d(TAG, text);

        Gson gson = new Gson();
        try {
            JsonObject object = gson.fromJson(text, JsonObject.class);
            if (object.getAsJsonObject("data") == null) return;
            weatherInfos = object.getAsJsonObject("data").getAsJsonArray("forecast");

            JsonObject saveDate = new JsonObject();
            saveDate.addProperty("city", city);
            saveDate.add("forecast", weatherInfos);

            dataTool.setText(gson.toJson(saveDate));
            context.sendBroadcast(new Intent("com.stone.action.start"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setCity(String city) {
        this.city = city;

        Gson gson = new Gson();
        JsonObject saveDate = new JsonObject();
        saveDate.addProperty("city", city);
        dataTool.setText(gson.toJson(saveDate));

        context.sendBroadcast(new Intent("com.stone.action.start"));
    }
}
