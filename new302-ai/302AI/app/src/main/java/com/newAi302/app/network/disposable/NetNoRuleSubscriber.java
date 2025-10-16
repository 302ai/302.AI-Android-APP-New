package com.newAi302.app.network.disposable;

import com.google.gson.Gson;
import com.newAi302.app.network.NetworkCodeEnum;
import com.newAi302.app.network.base.BaseSubscriber;
import com.newAi302.app.network.common_bean.callback.ResponseCallback;
import com.newAi302.app.network.common_bean.exception.NetException;
import com.newAi302.app.utils.base.WearUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 这个Subscriber 用于没用 result：true ；code = 0；data：{} 的数据返回解析
 * version: 1.0
 */
public class NetNoRuleSubscriber<T> extends BaseSubscriber<ResponseBody> implements Serializable {

    private static final String TAG = "NetworkNewUtils";
    // 传入的 ResponseCallback 实例确定的泛型
    private Type finalNeedType;
    private ResponseCallback<T> callback;

    public NetNoRuleSubscriber(String url, Type finalNeedType, ResponseCallback<T> callback) {
        this.url = url;
        this.finalNeedType = finalNeedType;
        this.callback = callback;
    }

    /**
     * 回调具体的 model 层中传入的 ResponseCallback 接口函数
     */
    @Override
    public void onStart() {
        super.onStart();
        // todo some common as show loadding  and check netWork is NetworkAvailable
        //检查网络连接
        if (!isNetworkConnected(WearUtil.getApplication())) {
//            callback.onError(new NetException(NetException.NET_CONNECT_ERROR, NetworkCodeEnum.resultNetConnectErrorMessage()));
            // 一定主动调用下面这一句,取消本次Subscriber订阅
            if (!isDisposed()) {
                dispose();
            }
            return;
        }

        if (callback != null) {
            callback.onStart();
        }
    }


    @Override
    public void onComplete() {
        // todo some common as  dismiss loadding
        if (callback != null) {
            callback.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        if (callback != null) {

            e.printStackTrace();

            if (e instanceof SocketTimeoutException) { // 服务器响应的超时
//                callback.onError(new NetException(NetException.API_REQUEST_TIME_OUT, NetworkCodeEnum.resultTimeOutErrorMessage()));
                return;
            }
            if (e instanceof HttpException) { // 连接失败
//                callback.onError(new NetException(NetException.API_REQUEST_FAILED, NetworkCodeEnum.resultSystemErrorMessage(e)));
                return;
            }

            if (e instanceof ConnectException) {  //  服务器请求超时
//                callback.onError(new NetException(NetException.SERVER_REQUEST_FAILED, NetworkCodeEnum.resultSystemErrorMessage(e)));
                return;
            }

            if (e instanceof TimeoutException) { // "连接超时！请检查网络状况!")
//                callback.onError(new NetException(NetException.SOCKET_TIME_OUT, NetworkCodeEnum.resultSystemErrorMessage(e)));
                return;
            }

            if (e instanceof SocketException) {// "服务器连接关闭！请检查网络状况或稍后再试!"
//                callback.onError(new NetException(NetException.SOCKET_CONNECT_ERROR, NetworkCodeEnum.resultSystemErrorMessage(e)));
                return;
            }

            if (e instanceof NullPointerException) { // 当token失效会主动抛出这空指针，这里结合 RToken是否为空来判断
//                callback.onError(new NetException(NetException.SOCKET_CONNECT_ERROR, NetworkCodeEnum.resultSystemErrorMessage(e)));
                return;
            }

            callback.onError(new NetException(NetException.LOCAL_UN_DEFINE_ERROR, NetworkCodeEnum.resultSystemErrorMessage(e)));
        }
    }

    /**
     * 处理服务器返回的数据，服务器返回的数据有：status:状态  msg:相应的信息  data:相应的数据
     * 我们只需要将data部分回调回去即可
     *
     * @param responseBody
     */
    @Override
    public void onNext(ResponseBody responseBody) {
        try {
            byte[] bytes = responseBody.bytes();
            String jsStr = new String(bytes);

//            LogUtils.d(TAG, "====ResponseBody:====urlL: " + url + ", jsStr: " + jsStr);
            /*if (ObjectUtils.isEmpty(jsStr)) {
                throw new NetException(NetException.SERVICE_DATA_ERROR, NetworkCodeEnum.resultNetDataErrorMessage(" ResponseBody is Empty !"));
            }
            if (JsonUtil.parseObject(jsStr, finalNeedType) == null) {
                throw new NetException(NetException.SERVICE_DATA_ERROR, NetworkCodeEnum.resultNetDataErrorMessage(" Json translate Type failed  Json = " + jsStr));
            }*/
            // 解析直接得到数组
            if (jsStr.startsWith("[")) {
                T bean = jsStr.getClass() != finalNeedType ? new Gson().fromJson(jsStr, finalNeedType) : (T) jsStr;
                if (bean != null) {
                    if (callback != null) {
                        callback.onSuccess(bean);
                    }
                    return;
                }
            } else if (jsStr.startsWith("{")) {
                JSONObject jsonObj = new JSONObject(jsStr);
                boolean hasResultKey;
                try {
                    jsonObj.get("result");
                    hasResultKey = true;
                } catch (JSONException e) {
                    hasResultKey = false;
                }

                boolean result = jsonObj.optBoolean("result", false); // 返回接口成功失败

                String msg = jsonObj.optString("message", NetworkCodeEnum.resultNetDataErrorMessage("Json Response Body has no key message"));

                String code = jsonObj.optString("code", NetException.SERVICE_DATA_ERROR);
                if (!hasResultKey || result) {
                    //如果是String类型，直接返回
                    try {
                        T bean = jsStr.getClass() != finalNeedType ? new Gson().fromJson(jsStr, finalNeedType) : (T) jsStr;
                        if (bean != null) {
                            if (callback != null) {
                                callback.onSuccess(bean);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        if (result) {
                            if (callback != null) {
                                callback.onSuccess(null);
                            }
//                            LogUtils.e(TAG, " request is success ,but translate 'data' is failed .");
                            return;
                        }
                    }
                }
                // 请求失败
                NetworkCodeEnum.ErrorCode errorCode = NetworkCodeEnum.maybeChangeMsg(code, msg);    //去枚举获取错误信息和错误码
//                addLog(code, msg);
                if (callback != null) {
                    callback.onError(new NetException(errorCode.getCode(), errorCode.getMsg())); // 暂时返回服务返回的错误信息，等之后多语言翻译可以替换成本地
                }
            }
        } catch (Exception e) { // json 解析出错
//            LogUtils.d(TAG, "====ResponseBody:====Exception====url: " + url);
            e.printStackTrace();
            if (callback != null) {
                callback.onError(new NetException(NetException.SERVICE_DATA_ERROR, NetworkCodeEnum.resultNetDataErrorMessage(e.getMessage())));
            }
        }
    }
}
