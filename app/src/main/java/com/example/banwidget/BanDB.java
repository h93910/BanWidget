package com.example.banwidget;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class BanDB extends SQLiteOpenHelper {
    SQLiteDatabase database;

    public BanDB(Context context) {
        super(context, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ban/ban.db", null, 1);
        database = getReadableDatabase();
        createTable();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 取得全部农历节日信息
     *
     * @param NongLiDate
     * @return
     */
    public ArrayList<String> getAllNongLiFestivalInfo() {
        ArrayList<String> nongLiFestivalInfo = new ArrayList<String>();

        Cursor cursor = database.query("nongli_info", null, null, null, null,
                null, "festival_date asc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor
                        .getColumnIndex("festival_date"));
                String name = cursor.getString(cursor
                        .getColumnIndex("festival_name"));
                int start = cursor.getInt(cursor
                        .getColumnIndex("festival_start"));
                if (start == 0) {
                    nongLiFestivalInfo.add(date + "\t\t" + name);
                } else {
                    nongLiFestivalInfo.add(date + "\t\t" + name + "\t\t"
                            + start + "年");
                }
                Log.v("读取到农历节目", date + "\t" + name);
            }
        }
        return nongLiFestivalInfo;
    }

    /**
     * 取得全部新历节日信息
     *
     * @param NongLiDate
     * @return
     */
    public ArrayList<String> getAllXinLiFestivalInfo() {
        ArrayList<String> xinLiFestivalInfo = new ArrayList<String>();

        Cursor cursor = database.query("xinli_info", null, null, null, null,
                null, "festival_date asc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor
                        .getColumnIndex("festival_date"));
                String name = cursor.getString(cursor
                        .getColumnIndex("festival_name"));
                int start = cursor.getInt(cursor
                        .getColumnIndex("festival_start"));
                try {
                    Date date2 = ChinaDate.mysdf.parse(date);
                    date = (date2.getMonth() + 1) + "月" + date2.getDate() + "日";
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (start == 0) {
                    xinLiFestivalInfo.add(date + "\t\t" + name);
                } else {
                    xinLiFestivalInfo.add(date + "\t\t" + name + "\t\t" + start
                            + "年");
                }
                Log.v("读取到新历节目", date + "\t" + name);
            }
        }
        return xinLiFestivalInfo;
    }

    /**
     * 取得全部特殊节日信息
     *
     * @return
     */
    public ArrayList<String> getAllSpecificFestivalInfo() {
        ArrayList<String> specificFestivalInfo = new ArrayList<String>();

        Cursor cursor = database
                .query("specific_info", null, null, null, null, null,
                        "festival_month asc,festival_order asc,festival_week asc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor
                        .getColumnIndex("festival_name"));
                int month = cursor.getInt(cursor
                        .getColumnIndex("festival_month"));
                int order = cursor.getInt(cursor
                        .getColumnIndex("festival_order"));
                int week = cursor
                        .getInt(cursor.getColumnIndex("festival_week"));

                String data = month + "\t\t" + order + "\t\t" + week + "\t\t"
                        + name;

                specificFestivalInfo.add(data);
                Log.v("读取到特殊节目", data);
            }
        }
        return specificFestivalInfo;
    }

    /**
     * 通过指定农历日期取得农历节的名字
     *
     * @return
     */
    public String getNongLiFestivalName(String NongLiDate) {
        String name = "";
        System.out.println("今天:" + NongLiDate);

        Cursor cursor = database.query("nongli_info",
                new String[]{"festival_name"}, "festival_date=?",
                new String[]{NongLiDate}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex("festival_name"));
                Log.v("读取到农历节目", name);
            }
        } else {
            Log.v("查询到农历节目", "无");
        }
        return name;
    }

    /**
     * 通过指定新历日期取得新历节的名字
     *
     * @return
     */
    public String getXinLiFestivalName(String xinLiDate) {
        String name = "";
        System.out.println("今天:" + xinLiDate);
        Cursor cursor = database.query("xinli_info", new String[]{
                        "festival_name", "festival_start"}, "festival_date=?",
                new String[]{xinLiDate}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex("festival_name"));
                int start = cursor.getInt(cursor
                        .getColumnIndex("festival_start"));

                if (start != 0) {
                    int disparity = new Date().getYear() + 1900 - start;
                    Log.e("start", start + "");
                    Log.e("disparity", disparity + "");
                    if (disparity != 0) {
                        name += disparity + "周年";
                    }
                }

                Log.v("单独读取到新历节目", name);
            }
        }
        return name;
    }

    /**
     * 通过日期获取特殊节日
     *
     * @param month 0开始的月
     * @param order 第几个星期
     * @param week  星期几
     * @return
     */
    public String getSpecificFestivalName(int month, int order, int week) {
        System.out.println("今天是" + (month + 1) + "月的第" + order + "个星期" + (week - 1));

        String name = "";

        Cursor cursor = database.query("specific_info",
                new String[]{"festival_name"},
                "festival_month=? and festival_order=? and festival_week=?",
                new String[]{String.valueOf(month), String.valueOf(order),
                        String.valueOf(week)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex("festival_name"));

                Log.v("单独读取特殊节目", name);
            }
        }
        return name;
    }

    /**
     * 插入农历节日
     *
     * @param festivalName 节日名
     * @param month        月
     * @param day          日
     * @param start        0周年的那一年，如值为0刚不记周年
     * @return
     */
    public boolean insertNongLiFestival(String festivalName, int month,
                                        int day, int start) {
        StringBuffer date = new StringBuffer();
        if (month < 10) {
            date.append("0" + month);
        } else {
            date.append(month);
        }
        if (day < 10) {
            date.append("0" + day);
        } else {
            date.append(day);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("festival_name", festivalName);
        contentValues.put("festival_date", date.toString());
        contentValues.put("festival_start", start);

        if (database.insert("nongli_info", null, contentValues) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功插入新的新历节日");
        return true;
    }

    /**
     * 插入新历节日
     *
     * @param festivalName 节日名
     * @param month        月
     * @param day          日
     * @param start        0周年的那一年，如值为0刚不记周年
     * @return
     */

    public boolean insertXinLiFestival(String festivalName, int month, int day,
                                       int start) {
        String date = "";
        Date date2 = new Date();
        date2.setMonth(month);
        date2.setDate(day);
        date = ChinaDate.mysdf.format(date2);

        ContentValues contentValues = new ContentValues();
        contentValues.put("festival_name", festivalName);
        contentValues.put("festival_date", date);
        contentValues.put("festival_start", start);

        if (database.insert("xinli_info", null, contentValues) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功插入新的新历节日");
        return true;
    }

    public boolean insertSpecificFestival(String name, int month, int order,
                                          int week) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("festival_name", name);
        contentValues.put("festival_month", month);
        contentValues.put("festival_order", order);
        contentValues.put("festival_week", week);

        if (database.insert("specific_info", null, contentValues) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功插入新的特殊节日");
        return true;
    }

    public boolean deleteNongLiFestival(String festivalName) {
        if (database.delete("nongli_info", "festival_name=?",
                new String[]{festivalName}) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功删除一条指定的新历节日");
        return true;
    }

    public boolean deleteXinLiFestival(String festivalName) {
        if (database.delete("xinli_info", "festival_name=?",
                new String[]{festivalName}) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功删除一条指定的新历节日");
        return true;
    }

    public boolean deleteSpecificFestival(String festivalName) {
        if (database.delete("specific_info", "festival_name=?",
                new String[]{festivalName}) == -1) {
            // 没有表才失败的，所以要新建表
            createTable();
            return false;
        }
        Log.i("", "成功删除一条指定的特别节日");
        return true;
    }

    /**
     * 新建一张表
     */
    public void createTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS nongli_info(festival_date nvarchar(14),festival_name nvarchar(30),festival_start INTEGER)");
        database.execSQL("CREATE TABLE IF NOT EXISTS xinli_info(festival_date nvarchar(14),festival_name nvarchar(30),festival_start INTEGER)");
        database.execSQL("CREATE TABLE IF NOT EXISTS specific_info(festival_name nvarchar(30),festival_month INTEGER,festival_order INTEGER,festival_week INTEGER)");
    }

}
