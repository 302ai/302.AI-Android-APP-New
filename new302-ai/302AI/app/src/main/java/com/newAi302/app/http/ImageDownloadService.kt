package com.newAi302.app.http

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

// 图片下载接口
interface ImageDownloadService {
    // @Url：动态传入完整图片URL
    @GET
    fun downloadImage(@Url url: String): Call<ResponseBody> // 返回字节流
}