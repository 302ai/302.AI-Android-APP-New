package com.newAi302.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/29
 * desc   :
 * version: 1.0
 */
object SystemUtils {

    /**
     * 将文本复制到系统剪贴板
     * @param context 上下文（如 Activity/Fragment 的 this）
     * @param text 要复制的文本内容
     * @param label 剪贴板条目标签（可选，用于描述内容，可传 null）
     */
    fun copyTextToClipboard(context: Context, text: String, label: String? = "Copied Text") {
        // 获取剪贴板管理器
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 创建包含文本的剪贴数据
        val clip = ClipData.newPlainText(label, text)

        // 将数据设置到剪贴板
        clipboard.setPrimaryClip(clip)
    }


    /**
     * 生成符合UUIDv4标准的用户ID
     * @return 36位带连字符的UUID字符串（如：550e8400-e29b-41d4-a716-446655440000）
     */
    fun generateUserId(): String {
        // 直接调用Java标准库的UUID生成方法，默认生成版本4的UUID
        return UUID.randomUUID().toString()
    }

    // 将Uri转换为临时文件
    fun uriToTempFile(context: Context, uri: Uri): File {
        // 获取文件名（用于临时文件命名）
        val fileName = getFileNameFromUri(context, uri)
        val tempFile = File(context.cacheDir, fileName)

        // 创建临时文件并写入内容
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4KB缓冲区
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }

        return tempFile
    }

    // 从Uri获取文件名
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result = "unknown_file"
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        } else if (uri.scheme == "file") {
            result = File(uri.path).name
        }
        return result
    }

    fun encodeUrlPath(path: String): String {
        return URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
            .replace("+", "%20") // 替换空格（如有）
            .replace("%7E", "~") // 保留波浪线（可选）
    }

    fun replaceUrlString(path: String):String{
        return path.replace("\"imgs\"","%22imgs%22")
    }


    /**
     * 获取系统语言
     */
    fun getSystemLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android N及以上版本
            context.resources.configuration.locales[0].displayName
        } else {
            // Android N以下版本
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.displayName
        }
    }

    /**
     * 获取系统主题模式
     */
    fun getSystemTheme(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q及以上版本可以直接获取深色模式设置
            when (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
                android.content.res.Configuration.UI_MODE_NIGHT_YES -> "深色模式"
                android.content.res.Configuration.UI_MODE_NIGHT_NO -> "浅色模式"
                else -> "未知模式"
            }
        } else {
            // Android Q以下版本无法直接获取，这里做兼容处理
            "无法确定（Android Q以下版本）"
        }
    }

    /**
     * 打开系统语言设置页面
     */
    fun openLanguageSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        context.startActivity(intent)
    }

    /**
     * 打开系统主题设置页面
     */
    fun openThemeSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android P及以上版本可以直接打开显示设置
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        } else {
            // 低版本系统打开设置主页面
            Intent(Settings.ACTION_SETTINGS)
        }
        context.startActivity(intent)
    }

    /**
     * 分享文本内容到其他应用
     */
    fun shareContent(context: Context,text: String) {
        //val content = shareEditText.text.toString().trim()

        // 检查内容是否为空
        if (text.isEmpty()) {
            Toast.makeText(context, "请输入要分享的内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建分享意图
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain" // 文本类型
        }

        // 启动分享活动
        context.startActivity(Intent.createChooser(shareIntent, "分享到..."))
    }

    /**
     * 分享图片的示例方法（需要时可以使用）
     * @param imageUri 图片的Uri
     */
    fun shareImage(context: Context,imageUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*" // 图片类型
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 授予读取权限
        }

        context.startActivity(Intent.createChooser(shareIntent, "分享图片到..."))
    }


}