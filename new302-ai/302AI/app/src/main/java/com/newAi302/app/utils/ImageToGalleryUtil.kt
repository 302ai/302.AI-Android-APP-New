package com.newAi302.app.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageToGalleryUtil {
    // 相册中图片的保存目录（DCIM下的子目录，便于用户查找）
    private const val GALLERY_DIR = "DCIM/AI302Images"
    // 日期格式化：用于生成唯一文件名（避免重复）
    private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)

    private val REQUEST_WRITE_STORAGE = 1001

    /**
     * 将私有目录的图片保存到相册
     * @param context 上下文（Activity/Fragment）
     * @param privateImagePath 私有目录图片路径（如 /data/user/0/com.newAi302.app/files/images/1670914306.png）
     * @return 成功返回相册中图片的ContentUri，失败返回null
     */
    suspend fun savePrivateImageToGallery(
        context: Context,
        privateImagePath: String
    ): String? = withContext(Dispatchers.IO) { // 耗时操作在IO线程
        try {
            // 1. 校验私有文件是否存在
            val privateFile = File(privateImagePath)
            if (!privateFile.exists() || !privateFile.isFile) {
                throw IOException("私有图片不存在：${privateFile.absolutePath}")
            }

            // 2. 检查权限（仅Android 9及以下需要）
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("缺少写入外部存储权限，请先申请")
            }

            // 3. 生成相册中图片的文件名（如 20240820_153045_1670914306.png）
            val timeStamp = DATE_FORMAT.format(Date())
            val originalFileName = privateFile.name // 原文件名（1670914306.png）
            val galleryFileName = "${timeStamp}_$originalFileName"

            // 4. 配置MediaStore参数（决定图片在相册中的存储位置和信息）
            val contentValues = ContentValues().apply {
                // 图片类型（必须为image/png，与文件格式匹配）
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                // 文件名
                put(MediaStore.Images.Media.DISPLAY_NAME, galleryFileName)
                // 文件大小
                put(MediaStore.Images.Media.SIZE, privateFile.length())

                // 区分Android版本，设置存储路径
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+：使用RELATIVE_PATH指定相对路径（DCIM/AI302Images）
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        GALLERY_DIR
                    )
                    // 标记为“可见”，相册能直接识别
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                } else {
                    // Android 9-：直接指定绝对路径（/storage/emulated/0/DCIM/AI302Images/xxx.png）
                    val galleryDir = File(
                        Environment.getExternalStorageDirectory(),
                        GALLERY_DIR
                    )
                    if (!galleryDir.exists()) galleryDir.mkdirs() // 创建目录
                    val galleryFile = File(galleryDir, galleryFileName)
                    put(MediaStore.Images.Media.DATA, galleryFile.absolutePath)
                }
            }

            // 5. 向MediaStore插入记录，获取ContentResolver的写入流
            val contentResolver = context.contentResolver
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val insertUri = contentResolver.insert(contentUri, contentValues)
                ?: throw IOException("MediaStore插入失败，无法获取写入URI")

            // 6. 复制私有文件到相册（通过ContentResolver的输出流）
            contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                FileInputStream(privateFile).use { inputStream ->
                    inputStream.copyTo(outputStream, bufferSize = 8192) // 高效拷贝
                    outputStream.flush()
                }
            } ?: throw IOException("无法打开相册文件写入流")

            // 7. 通知相册刷新（部分机型需手动触发，确保图片立即显示）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mediaScanIntent = android.content.Intent(
                    android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    insertUri
                )
                context.sendBroadcast(mediaScanIntent)
            } else {
                // Android 4.4以下：扫描整个目录
                context.sendBroadcast(
                    android.content.Intent(
                        android.content.Intent.ACTION_MEDIA_MOUNTED,
                        android.net.Uri.fromFile(
                            File(Environment.getExternalStorageDirectory(), GALLERY_DIR)
                        )
                    )
                )
            }

            // 返回相册图片的ContentUri字符串（如 content://media/external/images/media/12345）
            return@withContext insertUri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null // 失败返回null
        }
    }


    // 触发“保存到相册”逻辑（含权限申请）
    fun saveToGalleryAction(privatePath: String,context: Context,lifecycleScope:LifecycleCoroutineScope) {
        // 1. Android 9及以下：检查并申请动态权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 申请权限（用户首次拒绝后，下次会显示“不再询问”）
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_STORAGE
                )
                return
            }
        }

        // 2. 权限通过，调用工具类保存图片
        lifecycleScope.launch {
            val galleryUri = ImageToGalleryUtil.savePrivateImageToGallery(context, privatePath)
            // 主线程更新UI提示
            withContext(Dispatchers.Main) {
                if (galleryUri != null) {
                    Toast.makeText(context, "已保存到相册（DCIM/AI302Images）", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "保存失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}