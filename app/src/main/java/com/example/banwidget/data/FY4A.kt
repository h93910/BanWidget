package com.example.banwidget.data

import android.annotation.SuppressLint
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
import kotlinx.coroutines.*
import okhttp3.*
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
    private val HOST = "https://3xacxxx.de"

    init {
        PATH = getSecondaryStoragePath() + "/Ban/" + conText.getString(R.string.app_name)
        val f = File(PATH)
        if (!f.exists()) {
            f.mkdirs()
        }
    }

    fun execute() {
        val preferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        if (!preferences.getBoolean("fengyu", true)) {//没开就不下
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            delay(10 * 1000)
            start()
            delay(30 * 1000)
            bitmapSet.mapIndexed { index, bitmap ->
                bitmap?.recycle()
                bitmapSet[index] = null
            }
            delay(3000)
            Runtime.getRuntime().gc()
            Log.d(TAG, "onDestroy finish")
        }
    }

    private fun start() {
        val client = OkHttpClient()
        val r = Request.Builder().url("$HOST/hentai/fengyu/last/info").build()
        client.newCall(r).enqueue(responseCallback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast(context.getString(R.string.fengyu_fail_net))
            }

            override fun onResponse(call: Call, response: Response) {
                val filePath = "$PATH/${response.body?.string()}"
                downloadAndSet(filePath)
            }
        })
    }

    /**
     * 2022年6月24日 官网已经不再提供一片一片的图，现在走自己的服务器下载已经处理好图片
     */
    private fun downloadAndSet(path: String) {
        //是否已经有下载
        val file = File(path)
        if (file.exists()) {//有图就不设置了，默认前一次是设置成功的
            return
        }
        try {
            val b = glide.load("$HOST/hentai/fengyu/last").submit().get()
            val n = Bitmap.createBitmap(b.width, b.height, Bitmap.Config.ARGB_8888);
            val c = Canvas(n)
            c.drawBitmap(b, 0f, 0f, null)
            saveBitmap(n, path)
            n.recycle()

            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                try {
                    setWallpaper(path)
                } catch (e: Exception) {
                    showToast("设置屏纸出错:${e.message}")
                }
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun saveBitmap(b: Bitmap, filePath: String) {
        val f = File(filePath)
        try {
            val fo = FileOutputStream(f)
            b.compress(Bitmap.CompressFormat.JPEG, 100, fo)
            fo.flush()
            fo.close()
        } catch (e: IOException) {
            showToast(e.message.toString())
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
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }
}