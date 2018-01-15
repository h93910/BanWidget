package com.example.banwidget.tool;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Q on 2016/6/14.
 */
public class FileHelper {
    private static String sdcardPath = "";
    /**
     * Constructs a new instance of {@code Object}.
     */
    private Context mContext;
    private File pathDir;

    private FileHelper(Context context, String path) {
        String pathStr = "";
        mContext = context;
        pathStr = getExternalSdCardPath() + path;
        pathDir = new File(pathStr);
        if (!pathDir.exists())
            pathDir.mkdirs();
//                pathDir = mContext.getCacheDir();
    }

    public static FileHelper getInstance(Context context, String pathName) {
        return new FileHelper(context, pathName);
    }


    public String getAbsoluteFilePath(String fileName) {
        if (null != pathDir)
            return pathDir.getAbsolutePath() + "/" + fileName;
        return "";
    }

    /**
     * 遍历 "system/etc/vold.fstab" 文件，获取全部的Android的挂载点信息
     *
     * @return
     */
    private static ArrayList<String> getDevMountList() {
        File file = new File("/system/etc/vold.fstab");
        StringBuffer buffer = new StringBuffer();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String lineText;
            while ((lineText = reader.readLine()) != null) {
                buffer.append(lineText);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] toSearch = buffer.toString().split(" ");
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }

    /**
     * 返回内置SD卡目录
     *
     * @return
     */
    private static String getInternalSdCardPath() {
        String path = null;
        File sdCardFile = null;
        ArrayList<String> devMountList = getDevMountList();
        for (String devMount : devMountList) {
            File file = new File(devMount);
            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);
                if (testWritable.mkdirs()) {
                    testWritable.delete();
                } else {
                    path = null;
                }
            }
        }
        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * 获取扩展SD卡存储目录
     * <p>
     * 如果有外接的SD卡，并且已挂载，则返回这个外置SD卡目录
     * 否则：返回内置SD卡目录
     *
     * 2017.02.07 Ban
     * @return
     */
    public static String getExternalSdCardPath() {
        if (TextUtils.isEmpty(sdcardPath)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.i("FileHelper", "SD卡已挂载");
                sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                Log.i("FileHelper", "SD卡未为挂载状态");
                sdcardPath = getInternalSdCardPath();
                Log.i("FileHelper", "读取可用内置空间路径为：" + sdcardPath);
            }
        }
        return sdcardPath;
    }

}
