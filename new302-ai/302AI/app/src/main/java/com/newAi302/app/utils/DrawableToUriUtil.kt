package com.newAi302.app.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.OutputStream

object DrawableToUriUtil {

    /**
     * 将drawable中的PNG资源转换为content URI
     * @param context 上下文
     * @param drawableResId drawable资源ID
     * @param displayName 显示名称
     * @return 生成的content URI
     */
    fun getDrawableUri(context: Context, drawableResId: Int, displayName: String): Uri? {
        // 从drawable获取Bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
            ?: return null

        // 准备插入MediaStore的参数
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp") // 保存路径
        }

        // 获取ContentResolver
        val resolver: ContentResolver = context.contentResolver

        // 插入内容并获取URI
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        // 将Bitmap写入到URI对应的输出流
        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    uri
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果出错，删除已创建的条目
            resolver.delete(uri, null, null)
            null
        } finally {
            // 回收Bitmap
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 删除通过getDrawableUri生成的图片文件
     * @param context 上下文
     * @param uri 要删除的图片URI
     * @return 是否删除成功
     */
    fun deleteImageUri(context: Context, uri: Uri): Boolean {
        return try {
            val resolver: ContentResolver = context.contentResolver
            // 通过ContentResolver删除URI对应的文件
            val rowsDeleted = resolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
