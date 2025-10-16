package com.newAi302.app.http

import android.R
import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.utils.ImageToGalleryUtil
import com.newAi302.app.utils.StringObjectUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.File

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/8/20
 * desc   :
 * version: 1.0
 */
object ResourceUtils {


    fun saveResource(context: Context,url:String){
        val urlMapper = ImageUrlMapper(context)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://file.302.ai/") // 占位（因用@Url动态URL）
            .build()
        val downloadService = retrofit.create(ImageDownloadService::class.java)
        val imageDownloader = ImageDownloader(downloadService)
        //  lifecycleScope：与Activity生命周期绑定，避免内存泄漏
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 下载图片（IO线程）
                val localUrl = withContext(Dispatchers.IO) {
                    imageDownloader.downloadImage(url, context)
                }
                Log.e("ceshi","下载的图片地址:$localUrl")

                if (localUrl.isNullOrEmpty()) {
                    Log.e("ceshi","存储本地url失败")
                    return@launch
                }else{
                    Log.e("ceshi","下载成功的图片地址:$localUrl")
                    CoroutineScope(Dispatchers.IO).launch {
                        urlMapper.saveUrlMapping(url, StringObjectUtils.extractImageId(localUrl))
                    }
                }

                // 2. 下载成功，插入Room数据库（IO线程）
                withContext(Dispatchers.IO) {

                }


            } catch (e: Exception) {

                Log.e("ceshi","下载图片异常${e.toString()}")
            }
        }
    }
}