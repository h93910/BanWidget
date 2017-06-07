package com.example.banwidget;

import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.banwidget.R;

public class MainActivity extends Activity {
    private TextView textView;// 标题
    private TextView dateView;// 选日期的显示文本
    private TextView nameView;// 节日的名字
    private ListView listView;
    private TextView nongliYear;
    private Spinner nongliMonth;
    private Spinner nongliDay;
    private Spinner specicalMonth;
    private Spinner specicalOrder;
    private Spinner specicalWeek;
    private Button button;// 新增节日按钮
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
        setContentView(R.layout.activity_main);
        db = new BanDB(this);

        initView();
        initListener();

        loadXinLiData();
        ChinaDate.getJieQi();
    }

    private OnClickListener xinLiListener;
    private OnClickListener nongLiListener;
    private OnClickListener specificListener;

    private void initListener() {
        // 新历增加按钮的侦听
        xinLiListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                xinLiDialogView = getLayoutInflater().inflate(
                        R.layout.add_xinli_dialog, null);
                final CheckBox checkBox = (CheckBox) xinLiDialogView
                        .findViewById(R.id.anniversary);
                nameView = (TextView) xinLiDialogView
                        .findViewById(R.id.festival_name);
                dateView = (TextView) xinLiDialogView
                        .findViewById(R.id.festival_date);

                checkBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        anniversary = checkBox.isChecked();
                    }
                });
                xinLiDialogView.findViewById(R.id.pickDate).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                datePickerDialog.show();
                            }
                        });
                builder = new AlertDialog.Builder(activity);
                builder.setView(xinLiDialogView)
                        .setTitle("新增新历节日")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (db.insertXinLiFestival(nameView
                                                        .getText().toString().trim(),
                                                month, day, yearN)) {
                                            initList();
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "添加成功", Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "添加新历节日失败",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).show();
            }
        };

        // 农历增加按钮的侦听
        nongLiListener = new OnClickListener() {
            private CheckBox checkBox;

            @Override
            public void onClick(View v) {
                nongLiDialogView = getLayoutInflater().inflate(
                        R.layout.add_nongli_dialog, null);

                checkBox = (CheckBox) nongLiDialogView
                        .findViewById(R.id.anniversary);
                nameView = (TextView) nongLiDialogView
                        .findViewById(R.id.festival_name);
                dateView = (TextView) nongLiDialogView
                        .findViewById(R.id.festival_date);

                nongliYear = (TextView) nongLiDialogView
                        .findViewById(R.id.nongli_year);
                nongliMonth = (Spinner) nongLiDialogView
                        .findViewById(R.id.nongli_month);
                nongliDay = (Spinner) nongLiDialogView
                        .findViewById(R.id.nongli_day);

                nongliYear
                        .setOnFocusChangeListener(new OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                setDateView();
                            }
                        });

                checkBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        anniversary = checkBox.isChecked();
                        if (anniversary) {
                            nongliYear.setVisibility(View.VISIBLE);
                        } else {
                            nongliYear.setVisibility(View.GONE);
                        }
                    }
                });

                ArrayList<String> monthdata = new ArrayList<String>();
                for (String s : nongliYue) {
                    monthdata.add(s);
                }

                // 农历月选择
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        activity, android.R.layout.simple_spinner_item,
                        monthdata);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nongliMonth.setAdapter(adapter);

                // 农历日选择
                ArrayList<String> nognliDayDatas = new ArrayList<String>();
                for (String s : nongliRi) {
                    nognliDayDatas.add(s);
                }
                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                        activity, android.R.layout.simple_spinner_item,
                        nognliDayDatas);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nongliDay.setAdapter(adapter2);

                nongliMonth
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                month = position;

                                setDateView();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                nongliDay
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                day = position + 1;
                                setDateView();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                builder2 = new AlertDialog.Builder(activity);
                builder2.setView(nongLiDialogView)
                        .setTitle("新增农历节日")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (nongliYear.getVisibility() == View.VISIBLE) {
                                            if (nongliYear.getText().toString()
                                                    .trim().equals("")) {
                                                yearN = 0;
                                            } else {
                                                yearN = Integer
                                                        .parseInt(nongliYear
                                                                .getText()
                                                                .toString()
                                                                .trim());
                                            }
                                        }
                                        if (nongliYear.getVisibility() == View.GONE) {
                                            yearN = 0;
                                        }

                                        if (db.insertNongLiFestival(nameView
                                                        .getText().toString().trim(),
                                                month, day, yearN)) {
                                            initList();
                                            showShortMessage("添加成功");
                                        } else {
                                            showShortMessage("添加农历节日失败");
                                        }
                                    }
                                }).show();
            }

            private void setDateView() {
                if (nongliYear.getText() == null
                        || nongliYear.getText().toString().equals("")) {
                    anniversary = false;
                    checkBox.setChecked(false);
                    nongliYear.setVisibility(View.GONE);
                }
                if (anniversary) {
                    yearN = Integer.parseInt(nongliYear.getText().toString()
                            .trim());
                    dateView.setText(yearN + "年" + nongliYue[month]
                            + nongliRi[day - 1]);
                } else {
                    dateView.setText(nongliYue[month] + nongliRi[day - 1]);
                }
            }
        };

        // 特殊节日的侦听器
        specificListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                specificDialogView = getLayoutInflater().inflate(
                        R.layout.add_specific_dialog, null);
                nameView = (TextView) specificDialogView
                        .findViewById(R.id.festival_name);
                specicalMonth = (Spinner) specificDialogView
                        .findViewById(R.id.specific_month);
                specicalOrder = (Spinner) specificDialogView
                        .findViewById(R.id.specific_order);
                specicalWeek = (Spinner) specificDialogView
                        .findViewById(R.id.specific_week);

                // 月
                final ArrayList<Integer> mondata = new ArrayList<Integer>();
                for (int i = 1; i < 13; i++) {
                    mondata.add(i);
                }
                ArrayAdapter<Integer> month = new ArrayAdapter<Integer>(
                        activity, android.R.layout.simple_spinner_item, mondata);
                month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                specicalMonth.setAdapter(month);
                specicalMonth
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                smonth = position;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                // 第几个星期X
                final ArrayList<Integer> orderdata = new ArrayList<Integer>();
                for (int i = 1; i < 6; i++) {
                    orderdata.add(i);
                }
                ArrayAdapter<Integer> order = new ArrayAdapter<Integer>(
                        activity, android.R.layout.simple_spinner_item,
                        orderdata);
                order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                specicalOrder.setAdapter(order);
                specicalOrder
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                sorder = orderdata.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                // 星期X
                final ArrayList<String> weekdata = new ArrayList<String>();
                for (int i = 0; i < 7; i++) {
                    weekdata.add(week[i]);
                }
                ArrayAdapter<String> week = new ArrayAdapter<String>(activity,
                        android.R.layout.simple_spinner_item, weekdata);
                week.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                specicalWeek.setAdapter(week);
                specicalWeek
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
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
                        .setTitle("新增特殊节日")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (db.insertSpecificFestival(nameView
                                                        .getText().toString().trim(),
                                                smonth, sorder, sweek)) {
                                            initList();
                                            showShortMessage("添加成功");
                                        } else {
                                            showShortMessage("添加特殊节日失败");
                                        }
                                    }
                                }).show();
            }
        };
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.text);
        listView = (ListView) findViewById(R.id.dateview);
        button = (Button) findViewById(R.id.addDate);

        datePickerDialog = new MyDatePickerDialog(this,
                new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        month = monthOfYear;
                        day = dayOfMonth;
                        if (anniversary) {
                            yearN = year;
                            dateView.setText(yearN + "年" + (month + 1) + "月"
                                    + dayOfMonth + "日");
                        } else {
                            yearN = 0;
                            dateView.setText((month + 1) + "月" + day + "日");
                        }
                        Log.v("日期", yearN + "年" + monthOfYear + "月"
                                + dayOfMonth + "日");
                    }
                }, 2000, 0, 1);
    }

    private void initList() {
        if (textView.getText().toString().equals("新历")) {
            loadXinLiData();
        } else if (textView.getText().toString().equals("新历")) {
            loadNongLiData();
        } else if (textView.getText().toString().equals("特殊")) {
            loadSpecificData();
        }
    }

    private void loadXinLiData() {
        type = TYPE_XINLI;
        reset();

        textView.setText("新历");
        button.setText("添加新历节日");

        dates = db.getAllXinLiFestivalInfo();
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, dates));
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                String[] s = dates.get(position).split("\t\t");
                if (s.length > 1) {
                    selectFestivalName = s[1];
                }
                System.out.println(selectFestivalName);

                return false;
            }
        });
        this.registerForContextMenu(listView);

        button.setOnClickListener(xinLiListener);
    }

    private void loadNongLiData() {
        type = TYPE_NONGLI;
        reset();

        textView.setText("农历");
        button.setText("添加农历节日");

        dates = db.getAllNongLiFestivalInfo();
        ArrayList<String> nongLiDates = new ArrayList<String>();
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
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, nongLiDates));
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                String[] s = dates.get(position).split("\t\t");
                if (s.length > 1) {
                    selectFestivalName = s[1];
                }
                System.out.println(selectFestivalName);

                return false;
            }
        });
        this.registerForContextMenu(listView);
        button.setOnClickListener(nongLiListener);
    }

    private void loadSpecificData() {
        type = TYPE_SPECIFIC;
        reset();

        textView.setText("特殊");
        button.setText("添加特殊节日");

        dates = db.getAllSpecificFestivalInfo();
        ArrayList<String> specificDates = new ArrayList<String>();
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
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, specificDates));
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                String[] s = dates.get(position).split("\t\t");
                if (s.length > 3) {
                    selectFestivalName = s[3];
                }
                System.out.println(selectFestivalName);

                return false;
            }
        });
        this.registerForContextMenu(listView);
        button.setOnClickListener(specificListener);
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
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("新历")) {
            loadXinLiData();
        } else if (item.getTitle().toString().equals("农历")) {
            loadNongLiData();
        } else if (item.getTitle().toString().equals("特殊")) {
            loadSpecificData();
        } else if (item.getTitle().toString().equals("更新日志")) {
            showUpdateLog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.clear();
        menu.setHeaderTitle("操作");
        menu.add(0, 1, Menu.NONE, "修改节日名");
        menu.add(0, 2, Menu.NONE, "删除此节日");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(getApplicationContext(), "懒得搞，删了再加吧",
                        Toast.LENGTH_SHORT).show();

                break;
            case 2:
                if (type == TYPE_XINLI) {
                    if (db.deleteXinLiFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(), "删除成功",
                                Toast.LENGTH_SHORT).show();
                        loadXinLiData();
                    } else {
                        Toast.makeText(getApplicationContext(), "删除新历节日时出错！",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (type == TYPE_NONGLI) {
                    if (db.deleteNongLiFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(), "删除成功",
                                Toast.LENGTH_SHORT).show();
                        loadNongLiData();
                    } else {
                        Toast.makeText(getApplicationContext(), "删除农历节日时出错！",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (type == TYPE_SPECIFIC) {
                    if (db.deleteSpecificFestival(selectFestivalName)) {
                        Toast.makeText(getApplicationContext(), "删除成功",
                                Toast.LENGTH_SHORT).show();
                        loadSpecificData();
                    } else {
                        Toast.makeText(getApplicationContext(), "删除特殊 节日时出错！",
                                Toast.LENGTH_SHORT).show();
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

        public MyDatePickerDialog(Context context, OnDateSetListener callBack,
                                  int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            // TODO Auto-generated constructor stub
        }

        // public MyDatePickerDialog(Context context, OnDateSetListener
        // callBack,
        // int year, int monthOfYear, int dayOfMonth) {
        // super(context, callBack, year, monthOfYear, dayOfMonth);
        //
        // this.setTitle((monthOfYear + 1) + "月" + dayOfMonth + "日");
        //
        // ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
        // .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
        // }
        //
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
        builder.setTitle(R.string.update_title).setMessage(R.string.update_date).create().show();
    }
}
