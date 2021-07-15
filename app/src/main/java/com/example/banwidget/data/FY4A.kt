package com.example.banwidget.data

import android.app.WallpaperManager
import android.content.Context
import android.content.Context.STORAGE_SERVICE
import android.content.Context.WALLPAPER_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.banwidget.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.math.sqrt


/**
 *
 * @Author: Ban
 * @Date: 2021/7/2
 */
class FY4A constructor(conText: Context) {
    private var PATH: String
    private var context: Context = conText
    private val bitmapSet = arrayOfNulls<Bitmap>(4 * 4)
    private var glide = Glide.with(context).asBitmap().skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)

    init {
        PATH = getSecondaryStoragePath() + "/Ban/" + conText.getString(R.string.app_name)
        val f = File(PATH)
        if (!f.exists()) {
            f.mkdirs()
        }
    }

    fun execute() {
        downloadPicSet()?.let {
            combineThePicAndSave(it)
            SystemClock.sleep(2000)
            setWallpaper(it)
        }
    }

    /**
     * 返回要生成的文件路径
     */
    private fun downloadPicSet(): String? {
        val timePoints = arrayOf(
            0, 15, 100, 200, 245,
            300, 315, 400, 500, 545,
            600, 615, 700, 800, 845,
            900, 915, 1000, 1100, 1145,
            1200, 1215, 1300, 1400, 1445,
            1500, 1515, 1600, 1700, 1745,
            1800, 1815, 1900, 2000, 2045,
            2100, 2115, 2200, 2300, 2345
        )
        val dataFormat = SimpleDateFormat("yyyyMMdd")

        val nowUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        var dateString = dataFormat.format(nowUTC.time);
        val nowTime = nowUTC.get(Calendar.HOUR_OF_DAY) * 100 + nowUTC.get(Calendar.MINUTE)
        //二分取所需值
        var effectiveIndex = 0
        var e = timePoints.size - 1
        while (effectiveIndex < e) {
            val mid = (effectiveIndex + e) / 2
            if (nowTime > timePoints[mid]) {
                effectiveIndex = mid + 1
            } else {
                e = mid
            }
        }
        effectiveIndex = if (effectiveIndex == 0) 0 else effectiveIndex - 1

        //资源格式
        val urlFormat =
            "https://satellite.nsmc.org.cn/mongoTile_DSS/NOM/TileServer.php?layer=PRODUCT&PRODUCT=FY4A-_AGRI--_N_DISK_1047E_L1C_MTCC_MULT_NOM_YYYYMMDDhhmmss_YYYYMMDDhhmmss_4000M_V0001.JPG&DATE=%s&TIME=%s&&ENDTIME=%s&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image%%2Fjpeg&TRANSPARENT=true&LAYERS=satellite&NOTILE=BLACK&TILED=true&WIDTH=256&HEIGHT=256&SRS=EPSG%%3A11111&STYLES=&BBOX=%d%%2C%d%%2C%d%%2C%d"
        var out = 0;//重试数
        while (true) {
            if (effectiveIndex < 0) {
                effectiveIndex += 40;//倒数
                nowUTC.add(Calendar.DAY_OF_MONTH, -1)//减一天
                dateString = dataFormat.format(nowUTC.time);
            }
            val timeString = String.format("%04d", timePoints[effectiveIndex])
            //是否已经有下载
            if (checkFileExists(dateString, timeString)) {
                val path = "$PATH/${dateString}_$timeString.jpg"
                val msg = "存在:$path";
                showToast(msg)
                return path
            }
            val testResult = downloadPic(
                String.format(
                    urlFormat, dateString, timeString, timeString,
                    0, 0, 2750, 2750
                ), 0
            )
            if (testResult == 0) {
                val msg = "开始下载:$dateString $timeString";
                showToast(msg)
                SystemClock.sleep(3000)
                for (i in 0..3) {
                    for (j in 0..3) {
                        val x1 = -5500 + 2750 * i
                        val y1 = -5500 + 2750 * j
                        val x2 = x1 + 2750
                        val y2 = y1 + 2750
                        downloadPic(
                            String.format(
                                urlFormat, dateString, timeString, timeString,
                                x1, y1, x2, y2
                            ), i * 4 + j
                        )
                    }
                }
                return "$PATH/${dateString}_$timeString.jpg"
            } else if (testResult == 3) {//白图
                val msg = "$dateString $timeString 没有"
                showToast(msg)
                effectiveIndex--;
                out++;
                if (out >= 5) {
                    return null;
                }
            } else {
                val msg = "放弃刷新:result $testResult";
                showToast(msg)
                return null;
            }
        }
    }

    private fun downloadPic(url: String, id: Int, retry: Int = 0): Int? {
        try {
            val b = glide.load(url).submit().get()
            if (b.width == 512) {
                bitmapSet[id]?.let {
                    it.recycle()
                }
                bitmapSet[id] = b
                val msg = "download ok:$id";
                showToast(msg)
            } else {
                b.recycle()
                return 3
            }
            return 0;
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (retry == 5) {
            return 1;
        }
        SystemClock.sleep(1000)
        return downloadPic(url, id, retry + 1);
    }

    /**
     * 拼接图片
     */
    private fun combineThePicAndSave(filePath: String) {
        if (bitmapSet.last() == null) {//无图，为已经有文件
            return
        }
        val n = Bitmap.createBitmap(512 * 4, 512 * 4, Bitmap.Config.ARGB_8888);
        val c = Canvas(n)
        //放图
        for (i in 0..3) {
            for (j in 0..3) {
                bitmapSet[i * 4 + j]?.let {
                    c.drawBitmap(it, i * 512f, (3 - j) * 512f, null)
                }
            }
        }
        //写字
        val text = "UTC:${File(filePath).name.substring(0, 13)}"
        val p = Paint()
        p.textSize = 30f
        p.color = context.getColor(android.R.color.holo_red_dark);
        c.drawText(text, 512 * 4f - 350, 512 * 4f - 20, p)

        saveBitmap(n, filePath)

        n.recycle()
    }

    private fun saveBitmap(b: Bitmap, filePath: String) {
        val f = File(filePath)
        try {
            val fo = FileOutputStream(f)
            b.compress(Bitmap.CompressFormat.JPEG, 100, fo)
            fo.flush()
            fo.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置背景
     */
    private fun setWallpaper(path: String) {
        val ratio = (sqrt(5f) - 1) / 2
        val b = BitmapFactory.decodeFile(path)
        val n = Bitmap.createBitmap(b.width, (b.height / ratio).toInt(), Bitmap.Config.ARGB_8888);
        val c = Canvas(n)
        c.drawBitmap(b, 0f, (n.height - b.height) / 2f, null)//居中

        val wallpaperManager = context.getSystemService(WALLPAPER_SERVICE) as WallpaperManager
        wallpaperManager.apply {
            setBitmap(n)
            b.recycle()
            n.recycle()
        }
    }

    /**
     * 查背景图是否存在
     */
    private fun checkFileExists(date: String, time: String): Boolean {
        val path = "$PATH/${date}_$time.jpg"
        val file = File(path)
        return file.exists()
    }

    // 获取次存储卡路径,一般就是外置 TF 卡了. 不过也有可能是 USB OTG 设备...
    // 其实只要判断第二章卡在挂载状态,就可以用了.
    private fun getSecondaryStoragePath(): String? {
        try {
            val sm = context.getSystemService(STORAGE_SERVICE) as StorageManager?
            val getVolumePathsMethod: Method =
                StorageManager::class.java.getMethod("getVolumePaths")
            val paths = getVolumePathsMethod.invoke(sm) as Array<String>
            // second element in paths[] is secondary storage path
            return if (paths.size <= 1) paths[0] else paths[1]
        } catch (e: Exception) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e)
        }
        return null
    }

    companion object {
        private const val TAG = "FY4A"
    }

    private fun showToast(msg: String) {
        Log.w(TAG, msg)
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun onDestroy() {
        bitmapSet.mapIndexed { index, bitmap ->
            bitmap?.recycle()
            bitmapSet[index] = null
        }
        SystemClock.sleep(3000)
        Runtime.getRuntime().gc()
        Log.d(TAG, "onDestroy finish")
    }
}