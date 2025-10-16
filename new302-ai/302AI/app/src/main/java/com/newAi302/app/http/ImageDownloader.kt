package com.newAi302.app.http

import android.content.Context
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageDownloader(private val service: ImageDownloadService) {

    // 下载图片并保存到私有目录，返回本地路径（suspend：协程支持）
    suspend fun downloadImage(networkUrl: String, context: Context): String? {
        return try {
            // 1. 发起网络请求（同步调用，需在IO线程）
            val response: Response<ResponseBody> = service.downloadImage(networkUrl).execute()
            if (!response.isSuccessful) return null // 响应失败（如404、500）
            Log.e("ceshi","下载图片流是否成功：${response.isSuccessful}")


            val responseBody = response.body() ?: return null // 无字节流返回
            Log.e("ceshi","0下载图片流是否成功：${responseBody}")

            // 2. 定义本地存储路径（私有目录：/data/data/包名/files/images/）
            val imageDir = File(context.filesDir, "images") // files目录下创建images子目录
            if (!imageDir.exists()) imageDir.mkdirs() // 目录不存在则创建

            // 修复后：增加目录创建结果判断
            // mkdirs() 返回 Boolean：true=创建成功，false=创建失败（如权限不足、空间满）
            if (!imageDir.exists() && !imageDir.mkdirs()) {
                throw IOException("Failed to create directory: ${imageDir.absolutePath}") // 主动抛异常，便于定位
            }

            // 3. 生成唯一文件名（用网络URL的哈希值，避免重复）
            val fileName = "${networkUrl.hashCode()}.png"
            val localFile = File(imageDir, fileName)

            // 4. 字节流写入本地文件
            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(localFile)
            inputStream.copyTo(outputStream) // 拷贝字节流

            // 5. 关闭流（避免内存泄漏）
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            localFile.absolutePath // 返回本地文件路径
        } catch (e: IOException) {
            e.printStackTrace() // 处理IO异常（网络中断、文件写入失败）
            null
        }
    }
}