package com.newAi302.app.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : Retrofit网络服务类
 * version: 1.0
 */

public interface NetService {

    @POST()
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Flowable<ResponseBody> postRequest(@Url() String url, @FieldMap Map<String, Object> params);

    @POST()
    @FormUrlEncoded
    @Headers("Content-Type:multipart/form-data; charset=utf-8")
    Flowable<ResponseBody> postRequestbak(@Url() String url, @FieldMap Map<String, Object> params);

    @POST()
    @Headers("Content-Type:application/json; charset=utf-8")
    Call<ResponseBody> refreshToken(@Url() String url, @Body RequestBody params);


    @POST()
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Call<ResponseBody> messagePush(@Url() String url, @Header("gtoken") String token, @Header("ver") String ver, @FieldMap Map<String, String> params);

    //    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @GET()
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Flowable<ResponseBody> getRequest(@Url String url, @Header("gtoken") String token, @Header("x") String x);


    @GET()
    @Headers("Content-Type:application/json; charset=utf-8")
    Flowable<ResponseBody> getJson(@Url String url);

    @POST()
    @Headers("Content-Type:application/json; charset=utf-8")
    Flowable<ResponseBody> postJson(@Url() String url, @Body RequestBody requestBody);

    @POST()
    Flowable<ResponseBody> postGzip(@Url() String url, @Body RequestBody requestBody);

//
//    @POST()
//    Observable<ResponseBody> upload(@Url() String url, @Body RequestBody Body);

    @FormUrlEncoded
    @PUT
    Flowable<ResponseBody> put(@Url String url, @FieldMap Map<String, Object> params);

    @PUT
    Flowable<ResponseBody> putJson(@Url String url, @Body RequestBody body);

    @DELETE
    Flowable<ResponseBody> delete(@Url String url, @QueryMap Map<String, Object> params);

    //下载是直接到内存,所以需要 @Streaming
    @Streaming
    @GET
    Flowable<ResponseBody> download(@Url String url, @QueryMap Map<String, Object> params);

    //上传
    @Multipart
    @POST
    Flowable<ResponseBody> upload(@Url String url, @Part MultipartBody.Part file, @HeaderMap HashMap<String, String> addHeaders);

    //多文件上传
    @Multipart
    @POST()
    Flowable<ResponseBody> uploadMultiFile(@Url String url, @Part List<MultipartBody.Part> parts);

    //文件加参数
    @Multipart
    @POST()
    Flowable<ResponseBody> postUpload(@Url() String url, @PartMap Map<String, RequestBody> params, @Part MultipartBody.Part filePart);
}