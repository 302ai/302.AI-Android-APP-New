package com.newAi302.app.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object UriToFileUtils {

    /**
     * 将content://格式的URI转换为File对象
     * @param context 上下文
     * @param uri 要转换的URI
     * @return 转换后的File对象，转换失败返回null
     */
    fun convertUriToFile(context: Context, uri: Uri): File? {
        // 检查URI是否是content://格式
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            // 处理文档类型的URI
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // 处理不同提供者的URI
                return when {
                    isExternalStorageDocument(uri) -> {
                        handleExternalStorageDocument(uri)
                    }
                    isDownloadsDocument(uri) -> {
                        handleDownloadsDocument(context, uri)
                    }
                    isMediaDocument(uri) -> {
                        handleMediaDocument(context, uri)
                    }
                    else -> {
                        // 其他类型的文档，尝试通过复制文件到缓存目录来处理
                        copyUriToCacheFile(context, uri)
                    }
                }
            } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                // 普通内容URI，如媒体库
                val path = getMediaDatabasePath(context, uri, null)
                return if (path != null) File(path) else copyUriToCacheFile(context, uri)
            }
        }
        // 如果是file://格式的URI，直接转换
        else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            return File(uri.path ?: return null)
        }
        return null
    }

    /**
     * 处理外部存储文档
     */
    private fun handleExternalStorageDocument(uri: Uri): File? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]

        if ("primary".equals(type, ignoreCase = true)) {
            return File("${Environment.getExternalStorageDirectory()}/${split[1]}")
        }
        return null
    }

    /**
     * 处理下载文档
     */
    private fun handleDownloadsDocument(context: Context, uri: Uri): File? {
        val id = DocumentsContract.getDocumentId(uri)
        val contentUri = Uri.parse("content://downloads/public_downloads")
        val newUri = ContentUris.withAppendedId(contentUri, id.toLong())
        val path = getMediaDatabasePath(context, newUri, null)
        return if (path != null) File(path) else copyUriToCacheFile(context, uri)
    }

    /**
     * 处理媒体文档
     */
    private fun handleMediaDocument(context: Context, uri: Uri): File? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]

        var contentUri: Uri? = null
        when (type) {
            "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])

        val path = getMediaDatabasePath(context, contentUri, selection, *selectionArgs)
        return if (path != null) File(path) else copyUriToCacheFile(context, uri)
    }

    /**
     * 从媒体数据库获取文件路径
     */
    private fun getMediaDatabasePath(
        context: Context,
        uri: Uri?,
        selection: String?,
        vararg selectionArgs: String
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(
                uri ?: return null,
                projection,
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * 将URI指向的内容复制到缓存文件
     */
    private fun copyUriToCacheFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return null
            }

            // 创建缓存文件
            val cacheDir = context.cacheDir
            val fileName = "temp_${System.currentTimeMillis()}"
            val cacheFile = File(cacheDir, fileName)

            // 复制内容
            val outputStream = FileOutputStream(cacheFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 检查是否是外部存储文档
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * 检查是否是下载文档
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * 检查是否是媒体文档
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}

// 用于ContentUris.withAppendedId的工具类
object ContentUris {
    fun withAppendedId(baseUri: Uri, id: Long): Uri {
        return Uri.withAppendedPath(baseUri, id.toString())
    }
}
