package com.newAi302.app.network;

import com.newAi302.app.network.common_bean.callback.ResponseCallback;
import com.newAi302.app.network.converter.JsonConverterFactory;
import com.newAi302.app.network.cookie.ClearableCookieJar;
import com.newAi302.app.network.cookie.PersistentCookieJar;
import com.newAi302.app.network.cookie.cache.SetCookieCache;
import com.newAi302.app.network.cookie.persistence.SharedPrefsCookiePersistor;
import com.newAi302.app.utils.base.WearUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   :
 * version: 1.0
 */
public class NetworkCreator {

    public static NetService getNetService(ResponseCallback callback) {
        if (callback == null) {
            return NetworkServiceHolder.NET_SERVICE;
        } else {
            return NetworkHolder.getRetrofit(callback).create(NetService.class);
        }
//        return NetworkHolder.getRetrofit(callback).create(NetService.class);
    }

    /**
     * 产生一个全局的retrofit客户端
     */
    private static final class NetworkHolder {
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(WearUtil.APP_SERVER_HTTPS)
//                .baseUrl("https://test-api2.proxy302.com")
                .addConverterFactory(JsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpHolder.OK_HTTP_CLIENT)
                .build();

        private static Retrofit getRetrofit(ResponseCallback callback) {
            return new Retrofit.Builder()
                    .baseUrl(WearUtil.APP_SERVER_HTTPS)
//                    .baseUrl("https://test-api2.proxy302.com")
                    .addConverterFactory(JsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(OkHttpHolder.getOkHttpClient(callback))
                    .build();
        }
    }

    /**
     * 可以单独设置的okhttp
     */
    private static final class OkHttpHolder {
        private static final int TIME_OUT = 30;
        private static final RequestInterceptor interceptor = RequestInterceptor.getInstance();

        //设置网络缓存路径 缓存大小为10M
//        private static final Cache cache = new Cache(new File(WearUtil.getApplication().getExternalCacheDir(), "SurfeaseHttpCache"),
//                1024 * 1024 * 10);
//        private static final ClearableCookieJar cookieJar =
//                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(WearUtil.getApplication()));

        private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
//                .cache(cache)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
//                .cookieJar(cookieJar)
                .addInterceptor(interceptor)   // 添加拦截器，实现缓存以及一些请求头
                .build();

        private static OkHttpClient getOkHttpClient(ResponseCallback callback) {
            return new OkHttpClient.Builder()
//                    .cache(cache)
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
//                    .cookieJar(cookieJar)
                    .addInterceptor(new RequestInterceptor(callback))   // 添加拦截器，实现缓存以及一些请求头
                    .build();
        }
    }

    private static final class NetworkServiceHolder {
        private static final NetService NET_SERVICE = NetworkHolder.RETROFIT_CLIENT.create(NetService.class);
    }
}
