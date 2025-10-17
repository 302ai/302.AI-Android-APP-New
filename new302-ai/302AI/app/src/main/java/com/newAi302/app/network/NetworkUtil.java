package com.newAi302.app.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.newAi302.app.network.common_bean.callback.LoginCallback;
import com.newAi302.app.network.common_bean.callback.ResponseCallback;
import com.newAi302.app.network.common_bean.exception.NetException;
import com.newAi302.app.network.disposable.NetAllSubscriber;
import com.newAi302.app.network.disposable.NetNoRuleSubscriber;
import com.newAi302.app.network.utils.GsonUtils;
import com.newAi302.app.utils.EncodeUtils;
import com.newAi302.app.utils.GZIPUtils;
import com.newAi302.app.utils.LogUtils;
import com.newAi302.app.utils.base.WearData;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   :
 * version: 1.0
 */
public class NetworkUtil {

    public static String TAG = "NetworkNewUtils";

    private static volatile NetworkUtil mInstance;

    public static NetworkUtil getInstance() {
        if (mInstance == null) {
            synchronized (NetworkUtil.class) {
                if (mInstance == null) {
                    mInstance = new NetworkUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * post - Map请求
     */
    public <T> Disposable executePostAllRaw(final String url, Map<String, Object> parame, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }

        NetworkClient build = NetworkClient.create()
                .raw(GsonUtils.toJson(parame))
                .url(url)
                .build();
        return build.postraw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * 表单方式上传参数的 POST 请求（区别于 JSON 原始请求）
     * @param url 请求地址
     * @param parame 表单参数（键值对）
     * @param callback 请求结果回调
     * @param <T> 回调数据的泛型类型
     * @return Disposable 用于取消请求
     */
    public <T> Disposable executePostAllRaw1(final String url, Map<String, Object> parame, final ResponseCallback<T> callback) {
        // 1. 解析回调的泛型类型（与原方法逻辑一致）
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }

        // 2. 处理参数空指针：避免传入 null 导致后续表单参数异常
        if (parame == null) {
            parame = new HashMap<>();
        }

        // 3. 构建 NetworkClient：使用表单参数（params）替代 JSON 原始请求（raw）
        NetworkClient build = NetworkClient.create()
                .url(url)                  // 设置请求地址
                .params(parame)            // 传入表单参数（键值对），对应 NetworkClient 的 PARAMS 字段
                .build();                  // 构建客户端实例

        // 4. 发起表单 POST 请求：调用 post()（对应 POST 类型），而非原方法的 postraw()（POST_RAW 类型）
        return build.post()
                .compose(getSchedulersTransformerMain())  // 线程切换（与原方法一致）
                .subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));  // 订阅回调（与原方法一致）
    }

    public <T> Disposable executePostAllRaw1(final String url, String parame, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }

        Log.e("ceshi","token++++>"+parame);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                test(parame);
            }
        });
        thread.start();
        FormBody formBody = new FormBody.Builder()
                //.add("id_token","eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlNTIxYmY1ZjdhNDAwOGMzYmQ3MjFmMzk2OTcwOWI1MzY0MzA5NjEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiemV4aW9uZyBob25nIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FDZzhvY0xVRlJjWVQyQlBGTl9naE1lUUFvUlpXM0hfcG9iOEZSb1Z1TGpjU1lNaTdGQT1zOTYtYyIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9wcm94eTMwMi00MTYzMjEiLCJhdWQiOiJwcm94eTMwMi00MTYzMjEiLCJhdXRoX3RpbWUiOjE3MzIxNjk0NzAsInVzZXJfaWQiOiJGeE1NT1dhcXlET1RxalBWWUdLcDBMcWRZSmgxIiwic3ViIjoiRnhNTU9XYXF5RE9UcWpQVllHS3AwTHFkWUpoMSIsImlhdCI6MTczMjE2OTQ3MCwiZXhwIjoxNzMyMTczMDcwLCJlbWFpbCI6InpleGlvbmcyNEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjEwNDcyNDM3Njc3MDE5NTE0MzQ4MSJdLCJlbWFpbCI6WyJ6ZXhpb25nMjRAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.O4YoOAqqhYEvSqu2kV1Aod2esE5xK2xJXtzhrfIC-zp2PI7Ouz7w28FdZpIIjOupM6atuThdqcTE7gZ9pR6BRaRz8QBhCE33M3oBWpDyo0UdyUmXLqCyLLDKMDv6fkZhS4qm0sutPxpoeMavyjDgxRRgv88kv2_Nybfr2uoowGy57HcpnhaOnuFgctueU3puvrGpntHvdeSuleMT5cvvioCLMDAPUsaUPeqcqkrg9sxt1cqVH5L0SUkcD5_9R8XrkKmQvg4HAEBH2-GpMxWJ2m9iX2I1l_elo-twJVnne0uVNgcK9kN8Gof5uC8EvlTeew6sYNr1KixGHN1GrClt2Q")
                //.add("id_token","eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlNTIxYmY1ZjdhNDAwOGMzYmQ3MjFmMzk2OTcwOWI1MzY0MzA5NjEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoi6YKx6L6JIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FDZzhvY0s4cUZQZ1RuZVlJTmVzTVNLMGdxeFBFaHl0MWJaeWpFdVhmNnp6UzRmSTBJWW13Zz1zOTYtYyIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9wcm94eTMwMi00MTYzMjEiLCJhdWQiOiJwcm94eTMwMi00MTYzMjEiLCJhdXRoX3RpbWUiOjE3MzIxNTM2NzgsInVzZXJfaWQiOiJGeUo2V2ppSUVxVGVDV21hNENEaDdtbE1IYzQzIiwic3ViIjoiRnlKNldqaUlFcVRlQ1dtYTRDRGg3bWxNSGM0MyIsImlhdCI6MTczMjE1MzY3OCwiZXhwIjoxNzMyMTU3Mjc4LCJlbWFpbCI6Imh1aXFpdTEwOEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjExNDExNzUzNTgxMDc1Njc3NDMzOCJdLCJlbWFpbCI6WyJodWlxaXUxMDhAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.XDtt7F164lrqrhzCd6R-nihHUiXlsvXGMeYxDbhcBxMZk-MoacYMbj80FUXGoJyDKAkNRtH0quTD7JV07fDWMVG5hkSyL-qLCXwTcrXwmIUFK3TjfRyFGas-5Ly-0uQafCwFFfL00zIxWidj-fH1KzwkpmVIC0woXjFQ2oPKPrLmt6R1kXNw-wRSAkfafZ9JypvgLTCCStac3yaesSnhWaRnSNWZhIntsPzFSwrAFTz0Ar64KFxIl23XCUxIZjnwVgQ7kODU9qLkQf0CPGfer1cO8fszzpTZ0qR8kBVTu1Iw4MXwGLvfZyfLEIkRK-WrpaNS76Niv-D5Ex-QJXw62w")
                .add("id_token",parame)
                .build();


        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("id_token", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlNTIxYmY1ZjdhNDAwOGMzYmQ3MjFmMzk2OTcwOWI1MzY0MzA5NjEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiemV4aW9uZyBob25nIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FDZzhvY0xVRlJjWVQyQlBGTl9naE1lUUFvUlpXM0hfcG9iOEZSb1Z1TGpjU1lNaTdGQT1zOTYtYyIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9wcm94eTMwMi00MTYzMjEiLCJhdWQiOiJwcm94eTMwMi00MTYzMjEiLCJhdXRoX3RpbWUiOjE3MzIxNjk0NzAsInVzZXJfaWQiOiJGeE1NT1dhcXlET1RxalBWWUdLcDBMcWRZSmgxIiwic3ViIjoiRnhNTU9XYXF5RE9UcWpQVllHS3AwTHFkWUpoMSIsImlhdCI6MTczMjE2OTQ3MCwiZXhwIjoxNzMyMTczMDcwLCJlbWFpbCI6InpleGlvbmcyNEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjEwNDcyNDM3Njc3MDE5NTE0MzQ4MSJdLCJlbWFpbCI6WyJ6ZXhpb25nMjRAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.O4YoOAqqhYEvSqu2kV1Aod2esE5xK2xJXtzhrfIC-zp2PI7Ouz7w28FdZpIIjOupM6atuThdqcTE7gZ9pR6BRaRz8QBhCE33M3oBWpDyo0UdyUmXLqCyLLDKMDv6fkZhS4qm0sutPxpoeMavyjDgxRRgv88kv2_Nybfr2uoowGy57HcpnhaOnuFgctueU3puvrGpntHvdeSuleMT5cvvioCLMDAPUsaUPeqcqkrg9sxt1cqVH5L0SUkcD5_9R8XrkKmQvg4HAEBH2-GpMxWJ2m9iX2I1l_elo-twJVnne0uVNgcK9kN8Gof5uC8EvlTeew6sYNr1KixGHN1GrClt2Q");
        // 创建请求头的 Map，用于存放请求头信息
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        NetworkClient build = NetworkClient.create()
                .raw(GsonUtils.toJson(hashMap))
                .postGzip(formBody)
                .url(url)
                .addHeaders(headers)
                .build();
        return build.postraw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));

    }


    public <T> Disposable executePostAllRaw2(final String url, Map<String, String> parame, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        /*if (finalNeedType == null) {
            return null;
        }*/

        LogUtils.e("ceshi  google登录========：parame====="+parame);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                test2(parame.get("user_id"),parame.get("email"));
            }
        });
        thread.start();

        FormBody formBody = new FormBody.Builder()
                .add("user_id", Objects.requireNonNull(parame.get("user_id")))
                .add("email", Objects.requireNonNull(parame.get("email")))
                .build();
        // 创建请求头的 Map，用于存放请求头信息
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        NetworkClient build = NetworkClient.create()
                //.raw(GsonUtils.toJson(parame))
                .postGzip(formBody)
                .url(url)
                .addHeaders(headers)
                .build();
        return build.postraw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }


    public void executePostAllRaw3(final String url, Map<String, String> parame, LoginCallback callback) {

        LogUtils.e("ceshi  google登录========：parame====="+parame);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                // 构建表单数据，这里按照要求添加id_token字段及其对应的值
                FormBody formBody = new FormBody.Builder()
                        .add("user_id",parame.get("user_id"))
                        .add("email",parame.get("email"))
                        .build();

                // 设置请求头信息，与curl命令中保持一致
                String acceptHeader = "application/json";
                String contentTypeHeader = "application/x-www-form-urlencoded";

                Request request = new Request.Builder()
                        .url("https://dash-api.proxy302.com/user/login/google")
                        .post(formBody)
                        .header("accept", acceptHeader)
                        .header("Content-Type", contentTypeHeader)
                        .build();

                try {
                    // 发送请求并获取响应
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.e("ceshi","666666");
                        // 请求成功，调用回调方法的onSuccess，传递响应体内容
                        callback.onSuccess(response.body().string());
                    } else {
                        Log.e("ceshi","111"+response.code());
                        // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                        callback.onFailure(response.code(), "读取响应体内容出错: " + response.body().string());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                    callback.onFailure(-1, "读取响应体内容出错: " + e.getMessage());
                }
            }
        });
        thread.start();

    }

    /**
     * post - bean请求（自动过滤null字段）
     */
    public <T> Disposable executePostAllRaw(final String url, Object bean, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null || bean == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .raw(GsonUtils.toJson(bean))
                .url(url)
                .build();
        return build.postraw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * post - 同步处理
     */
    public <T> Disposable executePostAllRawSync(final String url, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
                .raw(GsonUtils.toJson(parames))
                .url(url)
                .build();
        return build.postraw().compose(getSchedulersTransformerSync()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    public <T> Disposable executePostAllRawSync(final String url, Object bean, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null || bean == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .raw(JSONObject.toJSONString(bean))
                .url(url)
                .build();
        return build.postraw().compose(getSchedulersTransformerSync()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    public <T> Disposable executeGetAllRaw(final String url, Map<String, Object> parame, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .raw(GsonUtils.toJson(parame))
                .url(url)
                .build();

        return build.getRaw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }


    /**
     * 用于没用 result：true ；code = 0；data：{} 的数据返回解析
     */
    public <T> Disposable executePost(final String url, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
//                .raw(GsonUtils.toJson(parames))
                .url(url)
                .build();

        return build.post().compose(getSchedulersTransformerMain()).subscribeWith(new NetNoRuleSubscriber<>(url, finalNeedType, callback));
    }

    public <T> Disposable executePostAll(final String url, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
//                .raw(GsonUtils.toJson(parames))
                .url(url)
                .build();

        return build.post().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    public <T> Disposable executePut(final String url, Map<String, Object> params, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        Log.e("ceshi","0邮箱修改密码："+params.toString());
        // 将 Map 转为 JSON 字符串（作为请求体）
        String jsonBody = GsonUtils.toJson(params);
        Log.e("ceshi","1邮箱修改密码："+jsonBody.toString());
        NetworkClient build = NetworkClient.create()
                .raw(jsonBody) // 设置「原始 JSON 字符串」为请求体
                .url(url)
                .build();

        // 使用 PUT 方法发起请求
        return build.putJson()
                .compose(getSchedulersTransformerMain()) // 线程调度（保持原有逻辑）
                .subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }


    public <T> Disposable executePostAsync(final String url, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
                .url(url)
                .build();

        return build.post().compose(getSchedulersTransformerSync()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    public <T> Disposable executePutAll(final String url, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
//                .raw(GsonUtils.toJson(parames))
                .url(url)
                .build();
        return build.put().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * 单文件上传，可设置加密上传头
     */
    public <T> Disposable executeUpload(final String url, File file, HashMap<String, String> addHeadersMap, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }

//        Log.e("executeUpload: " + FileUtils.getSize(file));
        NetworkClient build = NetworkClient.create()
                .file(file)
                .callbback(callback)
                .url(url)
                .addHeaders(addHeadersMap)
                .build();

        return build.upload().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * 单文件上传（同步）
     */
   /* public <T> Disposable executeUploadSync(final String url, File file, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
//        LogUtils.e("executeUpload: " + FileUtils.getSize(file));
        NetworkClient build = NetworkClient.create()
//                .params(parames)
                .file(file)
                .callback(callback)
                .url(url)
                .build();

        return build.upload().compose(getSchedulersTransformerSync()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }*/

    /**
     * 多文件上传（少用这个接口发，尽量用单个的处理）
     */
   /* public <T> Disposable executePostUpLoad(final String url, List<String> pathList, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        List<File> fileList = new ArrayList<>();
        for (String path : pathList) {
            if (!FileUtils.isFileExists(path)) {
                callback.onError(new NetException(NetException.LOCAL_UN_DEFINE_ERROR, "file is not exist"));
                return null;
            }
            fileList.add(new File(path));
        }
        NetworkClient build = NetworkClient.create()
                .fileList(fileList)
                .callback(callback)
                .url(url)
                .build();
        return build.uploadMulti().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }*/


    /**
     * 文件上传(参数和文件)
     */
    /*public <T> Disposable executePostUpLoadFileAndParames(String url, File file, Map<String, Object> parames, final ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .params(parames)
                .file(file)
                .callback(callback)
                .url(url)
                .build();
        return build.postUpload().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }*/


    /**
     * 上传gzip
     * <p>
     * //     * @param url
     * //     * @param callback
     *
     * @return
     */
    public <T> Disposable executePostZip(final String url, String params, final ResponseCallback<T> callback) {

        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        RequestBody requestBody = null;//json就是要拼的json上传文件
        try {
            requestBody = RequestBody.create(MediaType.parse("text/plain; charset=UTF-8"), GZIPUtils.compress(EncodeUtils.urlEncode(params)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (requestBody == null) {
            callback.onError(new NetException("", "gzip post error"));
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .postGzip(requestBody)
                .url(url)
                .build();

        return build.postGzip().compose(getSchedulersTransformerMain()).subscribeWith(new NetNoRuleSubscriber<>(url, finalNeedType, callback));

    }

    public static <T> FlowableTransformer<T, T> getSchedulersTransformerMain() {
        return getSchedulersTransformer(AndroidSchedulers.mainThread());
    }

    public static <T> FlowableTransformer<T, T> getSchedulersTransformerSync() {
        return getSchedulersTransformer(Schedulers.io());
    }

    public static <T> FlowableTransformer<T, T> getSchedulersTransformer(Scheduler observeScheduler) {
        FlowableTransformer<T, T> flowableTransformer = new FlowableTransformer<T, T>() {
            @NonNull
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(observeScheduler);
            }
        };
        return flowableTransformer;
    }

    /**
     * 执行get请求 无参
     * 不执行过滤
     * <p>
     * 已用gson解析
     * <p>
     * 返回的是 Subscription
     * 三个地方调用，其中一个地方不用回调错误消息，其他两个如果有问题会报解析错误
     *
     * @param url
     * @param callback
     * @param <T>
     * @return
     */
    //
    public <T> Disposable executeGet(final String url, ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .url(url)
                .build();

        return build.get().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * 返回json 中， 没外层包装
     *
     * @param url
     * @param callback
     * @param <T>
     * @return
     */
    public <T> Disposable executeGetNoRule(final String url, ResponseCallback<T> callback) {
        Type finalNeedType = GenericsUtils.getSuperClassGenricType(callback.getClass());
        if (finalNeedType == null) {
            return null;
        }
        NetworkClient build = NetworkClient.create()
                .url(url)
                .build();

        return build.get().compose(getSchedulersTransformerMain()).subscribeWith(new NetNoRuleSubscriber<>(url, finalNeedType, callback));
    }

    /**
     * MethodHandler
     */
    private List<Type> MethodHandler(Type mType) {
        List<Type> needtypes = new ArrayList<>();
        if (mType instanceof ParameterizedType) {
            Type[] parentypes = ((ParameterizedType) mType).getActualTypeArguments();
            for (Type childtype : parentypes) {
                Log.d(TAG, "===========childtype:=======" + childtype);
                needtypes.add(childtype);
                //needParentType = childtype;
                if (childtype instanceof ParameterizedType) {
                    Type[] childtypes = ((ParameterizedType) childtype).getActualTypeArguments();
                    for (Type type : childtypes) {
                        needtypes.add(type);
                        //needChildType = type;
                        Log.d(TAG, "=========type:=======" + childtype);
                    }
                }
            }
        }
        return needtypes;
    }


    private void test(String token){
        OkHttpClient client = new OkHttpClient();

        // 构建表单数据，这里按照要求添加id_token字段及其对应的值
        FormBody formBody = new FormBody.Builder()
                //.add("id_token", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlNTIxYmY1ZjdhNDAwOGMzYmQ3MjFmMzk2OTcwOWI1MzY0MzA5NjEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiemV4aW9uZyBob25nIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FDZzhvY0xVRlJjWVQyQlBGTl9naE1lUUFvUlpXM0h_fc96-tcIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9wcm94eTMwMi00MTYzMjEiLCJhdWQiOiJwcm94eTMwMi00MTYzMjEiLCJhdXRoX3RpbWUiOjE3MzIxNjk0NzAsInVzZXJfaWQiOiJGeE1NT1dhcXlET1RxalBWWUdLcDBMcWRZSmgxIiwic3ViIjoiRnhNTU9XYXF5RE9UcWpQVllHS3AwTHFkWUpoMSIsImlhdCI6MTczMjE2OTQ3MCwiZXhwIjoxNzMyMTczMDcwLCJlbWFpbCI6InpleGlvbmcyNEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjEwNDcyNDM3Njc3MDE5NTE0MzQ4MSJdLCJlbWFpbCI6WyJ6ZXhpb25nMjRAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.O4YoOAqqhYEvSqu2kV1Aod2esE5xK2xJXtzhrfIC-zp2PI7Ouz7w28FdZpIIjOupM6atuThdqcTE7gZ9pR6BRaRz8QBhCE33M3oBWpDyo0UdyUmXLqCyLLDKMDv6fkZhS4qm0sutPxpoeMavyjDgxRRgv88kv2_Nybfr2uoowGy57HcpnhaOnuFgctueU3puvrGpntHvdeSuleMT5cvvioCLMDAPUsaUPeqcqkrg9sxt1cqVH5L0SUkcD5_9R8XrkKmQvg4HAEBH2-GpMxWJ2m9iX2I1l_elo-twJVnne0uVNgcK9kN8Gof5uC8EvlTeew6sYNr1KixGHN1GrClt2Q")
                .add("id_token",token)
                .build();

        // 设置请求头信息，与curl命令中保持一致
        String acceptHeader = "application/json";
        String contentTypeHeader = "application/x-www-form-urlencoded";

        Request request = new Request.Builder()
                .url("https://dash-api.proxy302.com/user/register/google")
                .post(formBody)
                .header("accept", acceptHeader)
                .header("Content-Type", contentTypeHeader)
                .build();

        try {
            // 发送请求并获取响应
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("ceshi","666"+response.body().string());
            } else {
                Log.e("ceshi","111"+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void test2(String user_id,String email){
        OkHttpClient client = new OkHttpClient();

        // 构建表单数据，这里按照要求添加id_token字段及其对应的值
        FormBody formBody = new FormBody.Builder()
                .add("user_id",user_id)
                .add("email",email)
                .build();

        // 设置请求头信息，与curl命令中保持一致
        String acceptHeader = "application/json";
        String contentTypeHeader = "application/x-www-form-urlencoded";

        Request request = new Request.Builder()
                .url("https://dash-api.proxy302.com/user/login/google")
                .post(formBody)
                .header("accept", acceptHeader)
                .header("Content-Type", contentTypeHeader)
                .build();

        try {
            // 发送请求并获取响应
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("ceshi","666"+response.body().string());
            } else {
                Log.e("ceshi","111"+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executePostAllRaw4(final String url, int parame, final LoginCallback callback) {

        Log.e("ceshi","pay++++>"+parame);

        /*FormBody formBody = new FormBody.Builder()
                .add("payway_id",String.valueOf(parame))
                .build();



        // 创建请求头的 Map，用于存放请求头信息
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", addBasicPrefix(WearData.getInstance().getToken()));

        NetworkClient build = NetworkClient.create()
                //.raw(GsonUtils.toJson(hashMap))
                .postGzip(formBody)
                .url(url)
                .addHeaders(headers)
                .build();
        return build.postraw().compose(getSchedulersTransformerMain()).subscribeWith(new NetAllSubscriber<>(url, finalNeedType, callback));*/
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                // 构建表单数据，这里按照要求添加id_token字段及其对应的值
                FormBody formBody = new FormBody.Builder()
                        .add("payway_id",String.valueOf(parame))
                        .build();

                // 设置请求头信息，与curl命令中保持一致
                String acceptHeader = "application/json";
                String contentTypeHeader = "application/x-www-form-urlencoded";

                Request request = new Request.Builder()
                        .url("https://dash-api.302.ai/proxy/charges/alipay")//https://dash-api.302.ai/proxy/charges/stripe_alipay
                        .post(formBody)
                        .header("accept", acceptHeader)
                        .header("Content-Type", contentTypeHeader)
                        .header("Authorization",addBasicPrefix(WearData.getInstance().getToken()))
                        .build();

                try {
                    // 发送请求并获取响应
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.e("ceshi","666666");
                        // 请求成功，调用回调方法的onSuccess，传递响应体内容
                        callback.onSuccess(response.body().string());
                    } else {
                        Log.e("ceshi","111"+response.code());
                        // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                        callback.onFailure(response.code(), "读取响应体内容出错: " + response.body().string());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                    callback.onFailure(-1, "读取响应体内容出错: " + e.getMessage());
                }
            }
        });
        thread.start();

    }

    public static String addBasicPrefix(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        return "Basic " + input;
    }


    public void executePostAllRaw5(final String url, int parame, final LoginCallback callback) {

        Log.e("ceshi","Epay++++>"+parame);

          Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                // 构建表单数据，这里按照要求添加id_token字段及其对应的值
                FormBody formBody = new FormBody.Builder()
                        .add("payway_id",String.valueOf(parame))
                        .build();

                // 设置请求头信息，与curl命令中保持一致
                String acceptHeader = "application/json";
                String contentTypeHeader = "application/x-www-form-urlencoded";

                Request request = new Request.Builder()
                        .url("https://dash-api.302.ai"+url+parame)
                        .post(formBody)
                        .header("accept", acceptHeader)
                        .header("Content-Type", contentTypeHeader)
                        .header("Authorization",addBasicPrefix(WearData.getInstance().getToken()))
                        .build();

                try {
                    // 发送请求并获取响应
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.e("ceshi","666666");
                        // 请求成功，调用回调方法的onSuccess，传递响应体内容
                        callback.onSuccess(response.body().string());
                    } else {
                        Log.e("ceshi","111"+response.code());
                        // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                        callback.onFailure(response.code(), "读取响应体内容出错: " + response.body().string());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                    callback.onFailure(-1, "读取响应体内容出错: " + e.getMessage());
                }
            }
        });
        thread.start();

    }

    public void executePostAllRaw6(final String url, int parame, final LoginCallback callback) {

        Log.e("ceshi","Stripe--pay++++>"+parame);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                // 构建表单数据，这里按照要求添加id_token字段及其对应的值
                FormBody formBody = new FormBody.Builder()
                        .add("payway_id",String.valueOf(parame))
                        .add("device_type","Android")
                        .build();

                // 设置请求头信息，与curl命令中保持一致
                String acceptHeader = "application/json";
                String contentTypeHeader = "application/x-www-form-urlencoded";

                Request request = new Request.Builder()
                        .url("https://dash-api.302.ai/proxy/charges/stripe")
                        .post(formBody)
                        .header("accept", acceptHeader)
                        .header("Content-Type", contentTypeHeader)
                        .header("Authorization",addBasicPrefix(WearData.getInstance().getToken()))
                        .build();

                try {
                    // 发送请求并获取响应
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.e("ceshi","666666");
                        // 请求成功，调用回调方法的onSuccess，传递响应体内容
                        callback.onSuccess(response.body().string());
                    } else {
                        Log.e("ceshi","111"+response.code());
                        // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                        callback.onFailure(response.code(), "读取响应体内容出错: " + response.body().string());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 如果读取响应体内容出现异常，视为请求失败，调用onFailure并传递相应错误信息
                    callback.onFailure(-1, "读取响应体内容出错: " + e.getMessage());
                }
            }
        });
        thread.start();

    }

}
