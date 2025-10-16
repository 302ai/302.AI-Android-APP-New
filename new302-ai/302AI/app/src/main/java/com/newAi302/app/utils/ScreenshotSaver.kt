package com.newAi302.app.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ScreenshotSaver {
    private const val TAG = "ScreenshotSaver"
    private const val IMAGE_FORMAT = "JPEG" // 保存格式：JPEG/PNG
    private const val QUALITY = 90 // 压缩质量（0-100）

    /**
     * 将 Bitmap 保存到系统相册（DCIM目录）
     * @param context 上下文
     * @param bitmap 要保存的图片
     * @return 保存成功返回图片路径，失败返回 null
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        return try {
            // 生成唯一文件名（时间戳 + 固定前缀）
            val fileName = "recycler_screenshot_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            }.jpg"

            // 根据 Android 版本选择保存方式（Android 10+ 用 MediaStore，低版本用文件）
            val imagePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(context, bitmap, fileName)
            } else {
                saveToExternalStorage(context, bitmap, fileName)
            }

            // 通知系统相册刷新（确保图片立即显示）
            if (imagePath != null) {
                refreshGallery(context, imagePath)
                Log.d(TAG, "图片保存成功：$imagePath")
            } else {
                Log.e(TAG, "图片保存失败")
            }
            imagePath
        } catch (e: Exception) {
            Log.e(TAG, "保存失败：${e.message}", e)
            null
        } finally {
            // 释放 Bitmap 资源（如果不再使用）
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Android 10+ 适配：使用 MediaStore 保存到相册（无需权限）
     */
    private fun saveToMediaStore(context: Context, bitmap: Bitmap, fileName: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Screenshots") // 保存到DCIM/Screenshots目录
            put(MediaStore.Images.Media.IS_PENDING, 1) // 标记为待处理（避免被扫描）
        }

        // 获取ContentResolver插入图片
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        // 写入图片数据
        var outputStream: OutputStream? = null
        try {
            outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                // 压缩并写入（JPEG格式）
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            }
            // 标记为已处理（允许被扫描）
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            return uri.path // 返回图片路径
        } finally {
            outputStream?.close()
        }
    }

    /**
     * Android 9及以下：直接保存到外部存储（需要WRITE_EXTERNAL_STORAGE权限）
     */
    private fun saveToExternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
        // 检查存储是否可用
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e(TAG, "外部存储不可用")
            return null
        }

        // 保存路径：/sdcard/DCIM/Screenshots/
        val saveDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots")
        if (!saveDir.exists()) {
            saveDir.mkdirs() // 创建目录（如果不存在）
        }

        val imageFile = File(saveDir, fileName)
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            return imageFile.absolutePath // 返回绝对路径
        } finally {
            outputStream?.close()
        }
    }

    /**
     * 通知系统相册刷新，确保图片立即显示
     */
    private fun refreshGallery(context: Context, imagePath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 发送广播通知相册更新
            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val file = File(imagePath)
            val uri = android.net.Uri.fromFile(file)
            mediaScanIntent.data = uri
            context.sendBroadcast(mediaScanIntent)
        } else {
            // 低版本直接扫描整个目录
            context.sendBroadcast(
                android.content.Intent(
                    android.content.Intent.ACTION_MEDIA_MOUNTED,
                    android.net.Uri.fromFile(Environment.getExternalStorageDirectory())
                )
            )
        }
    }
}