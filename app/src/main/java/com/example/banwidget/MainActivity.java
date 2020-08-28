package com.example.banwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banwidget.data.ChinaDate;
import com.example.banwidget.data.Weather_sojson;
import com.example.banwidget.tool.BanDB;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private TextView dateView;// 选日期的显示文本
    private TextView nameView;// 节日的名字
    private RecyclerView listView;
    private TextView nongliYear;
    private AppCompatSpinner nongliMonth;
    private AppCompatSpinner nongliDay;
    private AppCompatSpinner specicalMonth;
    private AppCompatSpinner specicalOrder;
    private AppCompatSpinner specicalWeek;
    private FloatingActionButton button;// 新增节日按钮
    private CollapsingToolbarLayout toolbarLayout;
    private MyDatePickerDialog datePickerDialog;
    private AlertDialog.Builder builder;
    private AlertDialog.Builder builder2;

    private BanDB db;
    private Activity activity = this;
    private View xinLiDialogView;
    private View nongLiDialogView;
    private View specificDialogView;

    private ArrayList<String> dates;

    // 设置的年月日
    private int yearN;
    private int month;
    private int day;
    private boolean anniversary;

    // 设置特殊节日的年月日
    private int smonth = 0;
    private int sorder = 1;
    private int sweek = 1;// sunday是1,monday是2

    private String selectFestivalName = "";
    private int type = 0;

    private String[] nongliYue = new String[]{"正月", "二月", "三月", "四月", "五月",
            "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"};
    private String[] nongliRi = new String[]{"初一", "初二", "初三", "初四", "初五",
            "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六",
            "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七",
            "廿八", "廿九", "三十"};
    private String[] week = {"天", "一", "二", "三", "四", "五", "六"};

    private static int TYPE_XINLI = 0;
    private static int TYPE_NONGLI = 1;
    private static int TYPE_SPECIFIC = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mayRequestPermission()) {
            setContentView(R.layout.activity_main);
            db = new BanDB(this);

            initView();
            initListener();

            loadXinLiData();
            ChinaDate.getJieQi();
            initDate();
        }
    }

    private String softVersion;//软件版本号

    /**
     * 初始化数据
     */
    private void initDate() {
        new Weather_sojson(this).getJsonFromNet();

        PackageManager pm = getApplicationContext().getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
            softVersion = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if (db != null)
            db.onDestory();
        super.onDestroy();
    }

    private OnClickListener xinLiListener;
    private OnClickListener nongLiListener;
    private OnClickListener specificListener;

    private void initListener() {
        // 新历增加按钮的侦听
        xinLiListener = (View v) -> {
            xinLiDialogView = getLayoutInflater().inflate(R.layout.dialog_add_xinli, null);
            final CheckBox checkBox = (CheckBox) xinLiDialogView.findViewById(R.id.anniversary);
            nameView = (TextView) xinLiDialogView.findViewById(R.id.festival_name);
            dateView = (TextView) xinLiDialogView.findViewById(R.id.festival_date);

            checkBox.setOnClickListener(v1 -> anniversary = checkBox.isChecked());
            xinLiDialogView.findViewById(R.id.pickDate).setOnClickListener(v12 -> datePickerDialog.show());
            builder = new AlertDialog.Builder(activity);
            builder.setView(xinLiDialogView)
                    .setTitle(R.string.main_add_solar_festival)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.confirm,
                            (dialog, which) -> {
                                if (db.insertXinLiFestival(nameView.getText().toString().trim(), month, day, yearN)) {
                                    initList();
                                    Toast.makeText(getApplicationContext(), R.string.main_add_success, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.main_add_solar_festival_fail, Toast.LENGTH_SHORT).show();
                                }
                            }).show();
        };

        // 农历增加按钮的侦听
        nongLiListener = new OnClickListener() {
            private CheckBox checkBox;

            @Override
            public void onClick(View v) {
                nongLiDialogView = getLayoutInflater().inflate(R.layout.dialog_add_nongli, null);

                checkBox = (CheckBox) nongLiDialogView.findViewById(R.id.anniversary);
                nameView = (TextView) nongLiDialogView.findViewById(R.id.festival_name);
                dateView = (TextView) nongLiDialogView.findViewById(R.id.festival_date);

                nongliYear = (TextView) nongLiDialogView.findViewById(R.id.nongli_year);
                nongliMonth = (AppCompatSpinner) nongLiDialogView.findViewById(R.id.nongli_month);
                nongliDay = (AppCompatSpinner) nongLiDialogView.findViewById(R.id.nongli_day);

                nongliYear.setOnFocusChangeListener((v13, hasFocus) -> setDateView());
                checkBox.setOnClickListener(v14 -> {
                    anniversary = checkBox.isChecked();
                    if (anniversary) {
                        nongliYear.setVisibility(View.VISIBLE);
                    } else {
                        nongliYear.setVisibility(View.GONE);
                    }
                });

                ArrayList<String> monthdata = new ArrayList<>();
                for (String s : nongliYue) {
                    monthdata.add(s);
                }

                // 农历月选择
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, monthdata);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nongliMonth.setAdapter(adapter);

                // 农历日选择
                ArrayList<String> nognliDayDatas = new ArrayList<>();
                for (String s : nongliRi) {
                    nognliDayDatas.add(s);
                }
                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                        nognliDayDatas);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nongliDay.setAdapter(adapter2);

                nongliMonth.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        month = position;
                        setDateView();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                nongliDay.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        day = position + 1;
                        setDateView();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                builder2 = new AlertDialog.Builder(activity);
                builder2.setView(nongLiDialogView)
                        .setTitle(R.string.main_add_lunar_festival)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            if (nongliYear.getVisibility() == View.VISIBLE) {
                                if (nongliYear.getText().toString().trim().equals("")) {
                                    yearN = 0;
                                } else {
                                    yearN = Integer.parseInt(nongliYear.getText().toString().trim());
                                }
                            }
                            if (nongliYear.getVisibility() == View.GONE) {
                                yearN = 0;
                            }
                            if (db.insertNongLiFestival(nameView.getText().toString().trim(), month, day, yearN)) {
                                initList();
                                showShortMessage(getString(R.string.main_add_success));
                            } else {
                                showShortMessage(getString(R.string.main_add_lunar_festival_fail));
                            }
                        }).show();
            }

            private void setDateView() {
                if (nongliYear.getText() == null || nongliYear.getText().toString().equals("")) {
                    anniversary = false;
                    checkBox.setChecked(false);
                    nongliYear.setVisibility(View.GONE);
                }
                if (anniversary) {
                    yearN = Integer.parseInt(nongliYear.getText().toString().trim());
                    dateView.setText(yearN + "年" + nongliYue[month] + nongliRi[day - 1]);
                } else {
                    dateView.setText(nongliYue[month] + nongliRi[day - 1]);
                }
            }
        };

        // 特殊节日的侦听器
        specificListener = v -> {
            specificDialogView = getLayoutInflater().inflate(R.layout.dialog_add_specific, null);
            nameView = (TextView) specificDialogView.findViewById(R.id.festival_name);
            specicalMonth = (AppCompatSpinner) specificDialogView.findViewById(R.id.specific_month);
            specicalOrder = (AppCompatSpinner) specificDialogView.findViewById(R.id.specific_order);
            specicalWeek = (AppCompatSpinner) specificDialogView.findViewById(R.id.specific_week);
            // 月
            final ArrayList<Integer> mondata = new ArrayList<>();
            for (int i = 1; i < 13; i++) {
                mondata.add(i);
            }
            ArrayAdapter<Integer> month = new ArrayAdapter<>(
                    activity, android.R.layout.simple_spinner_item, mondata);
            month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            specicalMonth.setAdapter(month);
            specicalMonth.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    smonth = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // 第几个星期X
            final ArrayList<Integer> orderdata = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                orderdata.add(i);
            }
            ArrayAdapter<Integer> order = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, orderdata);
            order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            specicalOrder.setAdapter(order);
            specicalOrder.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sorder = orderdata.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // 星期X
            final ArrayList<String> weekdata = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weekdata.add(week[i]);
            }
            ArrayAdapter<String> week = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, weekdata);
            week.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            specicalWeek.setAdapter(week);
            specicalWeek.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int position, long id) {
                    sweek = position + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            builder2 = new AlertDialog.Builder(activity);
            builder2.setView(specificDialogView)
                    .setTitle(R.string.main_add_special_festival)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        if (db.insertSpecificFestival(nameView.getText().toString().trim(),
                                smonth, sorder, sweek)) {
                            initList();
                            showShortMessage(getString(R.string.main_add_success));
                        } else {
                            showShortMessage(getString(R.string.main_add_special_festival_fail));
                        }
                    }).show();
        };
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.dateview);
        button = findViewById(R.id.addDate);
        toolbarLayout = findViewById(R.id.toolbar_layout);

        datePickerDialog = new MyDatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    month = monthOfYear;
                    day = dayOfMonth;
                    if (anniversary) {
                        yearN = year;
                        dateView.setText(yearN + "年" + (month + 1) + "月" + dayOfMonth + "日");
                    } else {
                        yearN = 0;
                        dateView.setText((month + 1) + "月" + day + "日");
                    }
                    Log.v("日期", yearN + "年" + monthOfYear + "月" + dayOfMonth + "日");
                }, 2000, 0, 1);
    }

    private void initList() {
        if (toolbarLayout.getTitle().equals(getString(R.string.main_solar))) {
            loadXinLiData();
        } else if (toolbarLayout.getTitle().equals(getString(R.string.main_lunar))) {
            loadNongLiData();
        } else if (toolbarLayout.getTitle().equals(getString(R.string.main_special))) {
            loadSpecificData();
        }
    }

    private void loadXinLiData() {
        type = TYPE_XINLI;
        reset();

        toolbarLayout.setTitle(getString(R.string.main_solar));

        dates = db.getAllXinLiFestivalInfo();
        DateRecyclerAdapter adapter = new DateRecyclerAdapter(this, dates, this);
        adapter.setItemLongClickListener((v, position) -> {
            String[] s = dates.get(position).split("\t\t");
            if (s.length > 1) {
                selectFestivalName = s[1];
            }
            System.out.println(selectFestivalName);
            return false;
        });
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        this.registerForContextMenu(listView);

        button.setOnClickListener(xinLiListener);

        listView.scheduleLayoutAnimation();
    }

    private void loadNongLiData() {
        type = TYPE_NONGLI;
        reset();

        toolbarLayout.setTitle(getString(R.string.main_lunar));

        dates = db.getAllNongLiFestivalInfo();
        ArrayList<String> nongLiDates = new ArrayList<>();
        for (String string : dates) {
            String m = string.substring(0, 2);
            String d = string.substring(2, 4);

            int month = Integer.parseInt(m);
            int day = Integer.parseInt(d) - 1;

            StringBuffer buffer = new StringBuffer();
            buffer.append(nongliYue[month] + nongliRi[day]);
            buffer.append(string.substring(4, string.length()));

            nongLiDates.add(buffer.toString());
        }
        DateRecyclerAdapter adapter = new DateRecyclerAdapter(this, nongLiDates, this);
        adapter.setItemLongClickListener((v, position) -> {
            String[] s = dates.get(position).split("\t\t");
            if (s.length > 1) {
                selectFestivalName = s[1];
            }
            System.out.println(selectFestivalName);
            return false;
        });
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        this.registerForContextMenu(listView);
        button.setOnClickListener(nongLiListener);

        listView.scheduleLayoutAnimation();
    }

    private void loadSpecificData() {
        type = TYPE_SPECIFIC;
        reset();

        toolbarLayout.setTitle(getString(R.string.main_special));

        dates = db.getAllSpecificFestivalInfo();
        ArrayList<String> specificDates = new ArrayList<>();
        for (String string : dates) {
            StringBuffer buffer = new StringBuffer();

            String[] read = string.split("\t\t");
            buffer.append((Integer.parseInt(read[0]) + 1) + "月第");
            buffer.append(read[1] + "个星期");
            buffer.append(week[Integer.parseInt(read[2]) - 1] + "\t\t");
            if (read.length > 3) {
                buffer.append(read[3]);
            }

            specificDates.add(buffer.toString());
        }

        DateRecyclerAdapter adapter = new DateRecyclerAdapter(this, specificDates, this);
        adapter.setItemLongClickListener((v, position) -> {
            String[] s = dates.get(position).split("\t\t");
            if (s.length > 3) {
                selectFestivalName = s[3];
            }
            System.out.println(selectFestivalName);
            return false;
        });
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        this.registerForContextMenu(listView);
        button.setOnClickListener(specificListener);

        listView.scheduleLayoutAnimation();
    }

    private void reset() {
        yearN = 0;
        month = 0;
        day = 1;
        anniversary = false;

        smonth = 0;
        sorder = 1;
        sweek = 1;

        selectFestivalName = "";
    }

    public void showShortMessage(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals(getString(R.string.main_solar))) {
            loadXinLiData();
        } else if (item.getTitle().toString().equals(getString(R.string.main_lunar))) {
            loadNongLiData();
        } else if (item.getTitle().toString().equals(getString(R.string.main_special))) {
            loadSpecificData();
        } else if (item.getTitle().toString().equals(getString(R.string.update_title))) {
            showUpdateLog();
        } else if (item.getItemId() == R.id.weather_city) {
            showCityInput();
        }
//        else if (item.getItemId() == R.id.test) {
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.clear();
        menu.setHeaderTitle(R.string.main_operation);
        menu.add(0, 1, Menu.NONE, R.string.main_festival_name);
        menu.add(0, 2, Menu.NONE, R.string.main_festival_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(getApplicationContext(), R.string.main_lazy, Toast.LENGTH_SHORT).show();
                break;
            case 2:
                if (type == TYPE_XINLI) {
                    if (db.deleteXinLiFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(), R.string.main_delete_success, Toast.LENGTH_SHORT).show();
                        loadXinLiData();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.main_delete_solar_fail, Toast.LENGTH_SHORT).show();
                    }
                } else if (type == TYPE_NONGLI) {
                    if (db.deleteNongLiFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(),  R.string.main_delete_success, Toast.LENGTH_SHORT).show();
                        loadNongLiData();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.main_delete_lunar_fail, Toast.LENGTH_SHORT).show();
                    }
                } else if (type == TYPE_SPECIFIC) {
                    if (db.deleteSpecificFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(),  R.string.main_delete_success, Toast.LENGTH_SHORT).show();
                        loadSpecificData();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.main_delete_special_fail, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 自定义DatePickerDialog，去掉了年
     *
     * @author Administrator
     */
    class MyDatePickerDialog extends DatePickerDialog {
        public MyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            super.onDateChanged(view, year, month, day);
            if (!anniversary) {
                this.setTitle((month + 1) + "月" + day + "日");
            }
        }
    }

    /**
     * 显示更新日志
     */
    private void showUpdateLog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_title) + " " + softVersion)
                .setMessage(R.string.update_date).create().show();
    }

    /**
     * 显示城市设置
     */
    private void showCityInput() {
        Weather_sojson sojson = new Weather_sojson(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_set_city, null);
        final EditText editText = (EditText) view.findViewById(R.id.city_name);
        editText.setText(sojson.getCity());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.city_setting).setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String value = editText.getText().toString();
                    if (TextUtils.isEmpty(value)) {
                        editText.setError("cant null");
                    } else {
                        sojson.setCity(value);
                        showShortMessage(getString(R.string.operation_complete));
                    }
                }).create().show();
    }


    /**
     * 权限的申请，非常值得参照
     *
     * @return
     */
    private boolean mayRequestPermission() {
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 0);
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {//不论结果所何，重启页面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
