package com.newAi302.app.network;


import com.newAi302.app.network.callback.NetworkProgressListener;
import com.newAi302.app.network.common_bean.callback.ResponseCallback;
import com.newAi302.app.network.utils.GsonUtils;
import com.newAi302.app.utils.LogUtils;
import com.newAi302.app.utils.base.WearUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : Http请求拦截器
 * version: 1.0
 */
public class RequestInterceptor implements Interceptor {

    /**
     * 不需要传入签名的请求接口路径
     */
 /*   private static final List<String> noSignLinks = Collections.singletonList(
            NetConfig.URL_APP_CONFIG
    );
*/
    private static final String TAG = "RequestInterceptor";
    private static volatile RequestInterceptor mInstance;

    private static HostDispenser mHostDispenser;
    private ResponseCallback callback;

    private RequestInterceptor() {
        mHostDispenser = new HostDispenser();
    }

    public RequestInterceptor(ResponseCallback callback) {
        super();
        this.callback = callback;
    }

    public static RequestInterceptor getInstance() {
        if (mInstance == null) {
            synchronized (RequestInterceptor.class) {
                if (mInstance == null) {
                    mInstance = new RequestInterceptor();
                }
            }
        }
        return mInstance;
    }

    // 替换baseUrl
    public static String getBaseUrl(String oldHttpUrl) {
      /*  String newBaseUrl = mHostDispenser.getHost(oldHttpUrl);
        if (!TextUtils.isEmpty(newBaseUrl)) {
            return newBaseUrl;
        }*/
        return WearUtil.APP_SERVER_HTTPS;
    }

    @Override
    public Response intercept(Chain chain) throws IOException, RuntimeException {
        Response response = null;
        Request request = chain.request();
        Request.Builder builder = request
                .newBuilder();

//        String gToken = WearData.getInstance().getUserSession().getUserLoginBean().getGtoken();
//        String x = WearData.getInstance().getUserSession().getUserLoginBean().getX();
//        String okHttpVersion = Version.userAgent();
//        String newUserAgent = String.format("%s/%s (%s; Version Core:%s; Android %s;) %s", AppUtils.getAppName(), WearUtil.appVersion,
//                AppUtils.getAppPackageName(), WearUtil.appVersionCode, android.os.Build.VERSION.RELEASE, okHttpVersion);
//        String ogToken = DXDtxUtils.dtxEncrypt(WearData.getInstance().getUserSession().getUserInfoBean().getUserId() + "##" + System.currentTimeMillis());  //ogToken的 格式：userId##当前时间戳 大头虾加密
//        boolean isOgToken = ogToken.contains("/og/");  //如果是活动请求，是就加上ogToken，否则不加
        builder.addHeader("BODY-X-TYPE", "2")
                .addHeader("BODY-X-VERSION", "1.0")
//                .addHeader("gtoken", gToken)
//                .addHeader("x", x)
//                .addHeader("pf", TokenUtils.getPF())         //平台
                .addHeader("ver", WearUtil.appVersion)       //版本
//                .addHeader("timezone", getTimeZone())  //时区
//                .addHeader("langkey", LanguageUtils.getSystemLanguage().toString()) //系统语言
//                .addHeader("User-Agent", newUserAgent)
//                .addHeader("ogToken", isOgToken ? ogToken : "")    //活动ogToken
//                .addHeader("deviceId", HyttoIdUtils.getDeviceId())  //设备Id
                .build();

        RequestBody body = request.body();
        if (body != null && request.method().equals(HttpMethod.POST.toString())) {
            Buffer bufferRequest = new Buffer();
            body.writeTo(bufferRequest);
            String bodyToString = bufferRequest.readUtf8();

//            Log.i(TAG, "paramJson", bodyToString);

            // 添加签名信息到头部
//            Map<String, String> map = addSignHeader(request, bodyToString);
//            if (map.keySet().size() > 0) {
//                builder.addHeader("ts", map.get("ts"));
//                builder.addHeader("tt", map.get("tt"));
//                builder.addHeader("tprotocol", map.get("tprotocol"));
//            }
        }

        //重建新的HttpUrl，修改需要修改的url部分
        HttpUrl newFullUrl = getHttpUrl(builder, request.url());
        if (callback == null) {
            response = chain.proceed(builder.url(newFullUrl)
                    .build());
        } else {
            //进度监听
            NetworkProgressProxy progressListener = new NetworkProgressProxy(request.body(), new NetworkProgressListener() {
                @Override
                public void onProgress(long uploadSize, long fileSize) {
                    callback.onProgress(uploadSize, fileSize);
//                    LogUtils.e("onProgress___uploadSize: " + uploadSize + ", fileSize: " + fileSize);
                }
            });
            response = chain.proceed(builder.url(newFullUrl)
                    .post(progressListener)
                    .build());
        }

//        response = interceptorAction(chain, builder, "", response);
        return response;
    }

    /**
     * 获取时区（String，+/-号）
     */
//    private static String getTimeZone() {
//        double timeZoneDouble = TimeUtils.getTimezoneOffset();
//        if (timeZoneDouble >= 0) {
//            return "+" + timeZoneDouble;
//        } else {
//            return "" + timeZoneDouble;
//        }
//    }

    private HttpUrl getHttpUrl(Request.Builder builder, HttpUrl oldHttpUrl) {

   /*     if (oldHttpUrl.toString().contains(NetConfig.LOG_NEW_V2)) { // gzip形式上传
            builder.addHeader("Content-Encoding", "gzip");
        }
*/
        String baseUrl = getBaseUrl(oldHttpUrl.toString());

        HttpUrl newBaseUrl = HttpUrl.parse(baseUrl);
        //重建新的HttpUrl，修改需要修改的url部分
        return oldHttpUrl
                .newBuilder()
                .scheme(newBaseUrl.scheme())
                .host(newBaseUrl.host())
                .port(newBaseUrl.port())
                .build();
    }

    /**
     * ==========一次网络请求完成，此处可以对服务器返回的数据做处理，
     * 比如gtoken，跳到登录页
     *
     * @param chain
     * @param builder
     * @param gToken
     * @param response
     * @return 是否拦截
     */
    private synchronized Response interceptorAction(Chain chain, Request.Builder builder, String gToken, Response response) {
        try { // 从这里捕捉异常，防止全方法捕捉异常导致正常无法连接服务器异常无法捕捉
            String chainUrl = response.request().url().toString();

            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));

            try {
                RequestBody body = chain.request().body();
                if (body != null) {
                    Buffer bufferRequest = new Buffer();
                    body.writeTo(bufferRequest);
                    String bodyToString = null;
                    boolean isNotLogBody = false;
                   /* if (chainUrl.contains(NetConfig.LOG_NEW_V2)) {
                        byte[] bytes = GZIPUtils.uncompress(bufferRequest.readByteArray());
                        bodyToString = EncodeUtils.urlDecode(new String(bytes));
                    } else {
                        //部分接口不打印 log
                        isNotLogBody = isNotAddBodyString(chainUrl);
                        if (!isNotLogBody) {
                            bodyToString = bufferRequest.readUtf8();
                        }
                    }*/
                    LogUtils.i(TAG, GsonUtils.toJson(builder.build().headers()), chainUrl, bodyToString, responseBodyString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(TAG, GsonUtils.toJson(builder.build().headers()), chainUrl, "获取参数异常", responseBodyString);
            }
//                getTokenSuccessData(response,request.url().toString(), "synchronized");

            String jsStr = new String(responseBodyString);
            JSONObject jsonObj = new JSONObject(jsStr);
            String code = jsonObj.optString("code"); // 返回接口成功失败
            String message = jsonObj.optString("message"); // 返回接口成功失败

           /* String accessToken = WearData.getInstance().getUserSession().getUserLoginBean().getGtoken();
            //比较请求的token与本地存储的token   如果不一致说明已经刷新过了，直接重试
            if (!gToken.equals(accessToken) && MyApplication.userIsLogin) {
                return getResponseIsTokenExpired(chain, accessToken);
            }
            switch (code) {
                case NetworkCodeEnum.CODE_22002:       //gToken过期
                    response = refreshGToken(chain, accessToken);
                    break;
                case NetworkCodeEnum.CODE_22003:    //rToken过期
                case NetworkCodeEnum.CODE_500410:   // 客户端被冻结，gtoken失效
                case NetworkCodeEnum.CODE_22001:    //gtoken 错误
                case NetworkCodeEnum.CODE_50024:    //gtoken 无效
                    loginOut(NetworkCodeEnum.resultNetWorkErrorMessage(R.string.refresh_token_expired), code, LogManager.L0004TypeLoginOut.INVALID_RTOKEN);
                    return null;
                case NetworkCodeEnum.CODE_500409:   //gToken多端使用
                    loginOut(NetworkCodeEnum.resultNetWorkErrorMessage(R.string.refresh_token_expired), code, LogManager.L0004TypeLoginOut.XMPP_CONFLICT);
                    return null;
                case NetworkCodeEnum.CODE_500411:   //被禁
                    loginOut(NetworkCodeEnum.resultNetWorkErrorMessage(R.string.refresh_token_expired), code, LogManager.L0004TypeLoginOut.FOR_BIDDEN_BY_SERVER);
                    return null;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }


        return response;
    }

    /**
     * 部分接口不打印 log
     *
     * @param chainUrl
     * @return
     */
/*    private boolean isNotAddBodyString(String chainUrl) {
        if (!TextUtils.isEmpty(chainUrl)
                && (chainUrl.contains(NetConfig.URL_UPLOAD_CHECK_PHOTO)  //上传相关的
                || chainUrl.contains(NetConfig.URL_UPLOAD_PHOTO)
                || chainUrl.contains(NetConfig.URL_UPLOAD_VIDEO)
                || chainUrl.contains(NetConfig.URL_UPLOAD_SOUND))) {
            return true;
        }
        return false;
    }*/

    /**
     * CODE_22002 刷新GToken
     *
     * @param chain
     * @param accessToken
     * @return
     * @throws IOException
     * @throws RuntimeException
     */
   /* private Response refreshGToken(Chain chain, String accessToken) throws IOException, RuntimeException {
        if (ObjectUtils.isEmpty(accessToken)) {
            return null;
        } else {    //gToken 需要刷新
            GTokenRefreshBean gRefreshBean = getNewToken(accessToken);
            if (gRefreshBean != null) { // 判断刷新是否成功
                if (gRefreshBean.isResult() && gRefreshBean.getData() != null) {
                    // 将新的token 加密后 存入数据库和常量
                    WearData.getInstance().getUserSession().setRefreshTokenInfo(gRefreshBean.getData());
                    return getResponseIsTokenExpired(chain, gRefreshBean.getData().getGtoken());
                } else { // 刷新失败跳到登录页面
                    String msg = gRefreshBean.getMessage();
                    if (gRefreshBean.getCode().equals(NetworkCodeEnum.CODE_22003)) { // CODE_22003 需要显示特定的文本
                        msg = NetworkCodeEnum.resultNetWorkErrorMessage(R.string.refresh_token_expired);
                    }
                    loginOut(msg, gRefreshBean.getCode(), LogManager.L0004TypeLoginOut.INVALID_RTOKEN);
                    return null; // 这里返回空是因为原 response 是 旧 CODE_22002 的 response，而此处已经提示过错误信息，所以直接返回空，交给订阅器处理
                }
            } else { // 刷新失败跳到登录页面
                loginOut(NetworkCodeEnum.resultSystemErrorMessage(null), NetException.TOKEN_REFLASH_ERROR, LogManager.L0004TypeLoginOut.INVALID_RTOKEN);
                return null; // 这里返回空是因为原 response 是 旧 CODE_22002 的 response，而此处已经提示过错误信息，所以直接返回空，交给订阅器处理
            }
        }
    }*/

    /**
     * 跳到登录页面
     *
     * @param msg
     * @param code
     * @param exitType 退出登录类型
     * @throws RuntimeException
     */
 /*   private void loginOut(String msg, String code, String exitType) throws RuntimeException {
        UtilsBridge.runOnUiThread(() -> {
            Activity topActivity = UtilsBridge.getTopActivity();
//            ToastUtils.showShort(msg + " [" + code + "]");
            WearData.getInstance().onLogout();
            BzzActivityNavUtil.navToLogin(topActivity);
        });
    }*/

    /**
     * 重新用新的Token去请求
     *
     * @param chain
     * @param gToken
     * @return
     * @throws IOException
     * @throws RuntimeException
     */
    private Response getResponseIsTokenExpired(Chain chain, String gToken) throws IOException, RuntimeException {
        Request request = chain.request();
        Request.Builder builder = request
                .newBuilder();
        Response response = null;//添加固定请求头
        builder.addHeader("BODY-X-TYPE", "2")
                .addHeader("BODY-X-VERSION", "1.0")
//                .addHeader("pf", TokenUtils.getPF())
                .addHeader("ver", WearUtil.appVersion)
//                .addHeader("langkey", LanguageUtils.getSystemLanguage().toString())
                .addHeader("gtoken", gToken)
                .build();

        //重建新的HttpUrl，修改需要修改的url部分
        HttpUrl newFullUrl = getHttpUrl(builder, request.url());

        //重建这个request，通过builder.url(newFullUrl).build()；
        //然后返回一个response至此结束修改
        response = chain.proceed(builder.url(newFullUrl).build());
        return response;
    }

    /**
     * 通过rToken去刷新gToken
     *
     * @param gToken
     * @return
     */
    /*private GTokenRefreshBean getNewToken(String gToken) {
        try {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor((Interceptor) chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .addHeader("pf", TokenUtils.getPF())
                        .addHeader("ver", WearUtil.appVersion)
                        .addHeader("langkey", LanguageUtils.getSystemLanguage().toString())
                        .addHeader("gtoken", gToken)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });
            OkHttpClient client = httpClient.build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(WearUtil.APP_SERVER_HTTPS)
                    .client(client)
                    .build();

            NetService netService = retrofit.create(NetService.class);
            JSONObject body = new JSONObject();
            String rToken = WearData.getInstance().getUserSession().getUserLoginBean().getRtoken();
            body.put("rtoken", rToken);
            retrofit2.Response<ResponseBody> bodyResponse = netService.refreshToken(NetConfig.URL_GTOKEN_REFRESH, RequestBody.create(null, body.toString())).execute();
            byte[] bytes = bodyResponse.body().bytes();
            String token = new String(bytes);
            GTokenRefreshBean gTokenRefreshBean = GsonUtils.fromJson(token, GTokenRefreshBean.class);
            return gTokenRefreshBean;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     * 添加签名信息到头部
     * @param request    请求
     * @param parameters 参数
     * @return
     */
    /*private Map<String, String> addSignHeader(Request request, String parameters) {
        String url = request.url().toString();
        String urlString = url.replace(WearUtil.APP_SERVER_HTTPS, "");
        Map<String, String> headerMap = new HashMap<>();

        if (noSignLinks.contains(urlString) == false) {
            // 如果是post的才进行加sign的验证
            int signInt = signForHttpHeader(urlString, parameters);

            if (signInt > 0) {
                // 计算出有值才进行加签名
                long timestamp = System.currentTimeMillis();
                String signStr = signInt + "#" + timestamp;

                headerMap.put("ts", DXDtxUtils.dtxEncrypt(signStr));
                headerMap.put("tt", timestamp + "");
                headerMap.put("tprotocol", "n1");

//                LogUtils.d("Jin", "signStr: " + signStr);
            }
        }

        return headerMap;
    }*/

    /**
     * 获取签名
     * @param pathUrl   请求路径
     * @param parameters    参数
     * @return
     */
    /*private int signForHttpHeader(String pathUrl, String parameters) {
        String userId = WearData.getInstance().getUserSession().getUserInfoBean().getUserId();
        if (TextUtils.isEmpty(userId)) {
            userId = "^_^";
        }

        String paramStr = parameters;
        if (TextUtils.isEmpty(paramStr)) {
            paramStr = "{}";
        }

        if (paramStr.equals("null")) {
            paramStr = "{}";
        }

        String path = userId + pathUrl + paramStr;
        int sign = getSumKeyForSign(path);

        return sign;
    }*/

    /**
     * 获取字符串的排序后的codePoints
     *
     * @param str 字符串
     * @return
     */
    private static int[] stringToSortedCodePoints(String str) {
        int[] codePoints = new int[0];

        codePoints = str.codePoints().toArray();

        Arrays.sort(codePoints);
        return codePoints;
    }

    /**
     * 获取 映照 索引位置数组
     *
     * @return
     */
    /*private List<Integer> signIndexArray() {
        AppConfigBean appConfigBean = WearData.getInstance().getAppSession().getAppConfigBean();
        List<Integer> signGenHC = appConfigBean.getSignGenHC();

        if (signGenHC == null || signGenHC.size() < 2) {
            return new ArrayList<>();
        }

        Integer getSignHSCount = signGenHC.get(0);
        Integer signLocalPlusNum = signGenHC.get(1);

        List<Integer> signGenHS = appConfigBean.getSignGenHS();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return signGenHS.subList(0, getSignHSCount).stream().map(item -> item + signLocalPlusNum).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
*/
    /**
     * 获取 sign Sum
     *
     * @param str
     * @return
     */
   /* private int getSumKeyForSign(String str) {
        try {
            List<Integer> signGenHB = WearData.getInstance().getAppSession().getAppConfigBean().getSignGenHB();

            int[] asciiArray = stringToSortedCodePoints(str);
            int sum = 0;
            List<Integer> indexes = signIndexArray();

            if (signGenHB == null || signGenHB.size() < 1 || indexes.size() < 1) {
                // 这种情况表示配置文件没有拿到, 或者配置文件的数据不对
                return sum;
            }

            for (int i = 0; i < indexes.size(); i++) {
                int item = signGenHB.get(i);

                // 当signGenHS的取数索引位置大于 codePointsArray   长度导致下标越界时 , 使用对应的位置的数字进行替换.
                if (indexes.get(i) < asciiArray.length) {
                    item = asciiArray[indexes.get(i)];
                }

                if (i == indexes.size() - 1) {
                    sum *= item;
                } else {
                    sum += item;
                }
            }

            return sum;
        } catch (Exception e) {
            return -1;
        }
    }*/
}
