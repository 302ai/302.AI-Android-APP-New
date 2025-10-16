package com.newAi302.app.network.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.reactivex.subscribers.DisposableSubscriber;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public abstract class BaseSubscriber<T> extends DisposableSubscriber<T> {

    public String url = null;

    @Override
    public void onComplete() {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {
        addLog(e);
    }

    private void addLog(Throwable e) {
        /*if (e instanceof SocketTimeoutException) { // 服务器响应的超时
            addLog(NetException.API_REQUEST_TIME_OUT, e.getMessage());
            return;
        }
        if (e instanceof HttpException) { // 连接失败
            addLog(NetException.API_REQUEST_FAILED, e.getMessage());
            return;
        }

        if (e instanceof ConnectException) {  //  服务器请求超时
            addLog(NetException.SERVER_REQUEST_FAILED, e.getMessage());
            return;
        }

        if (e instanceof TimeoutException) { // "连接超时！请检查网络状况!")
            addLog(NetException.SOCKET_TIME_OUT, e.getMessage());
            return;
        }

        if (e instanceof SocketException) {// "服务器连接关闭！请检查网络状况或稍后再试!"
            addLog(NetException.SOCKET_CONNECT_ERROR, e.getMessage());
            return;
        }

        if (e instanceof NullPointerException) { // 当token失效会主动抛出这空指针，这里结合 RToken是否为空来判断
            addLog(NetException.NULL_PORINT_ERROR, e.getMessage());
            return;
        }*/
//        addLog(NetException.LOCAL_UN_DEFINE_ERROR, e.getMessage());
    }

    /**
     * 判断是否有网络连接
     *
     * @return
     */
    protected boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                boolean mIsAvailable = mNetworkInfo.isAvailable();
                if (!mIsAvailable) {
//                    addLog(NetException.NET_CONNECT_ERROR, "Network Not Connected");
                }
                return mIsAvailable;
            }
        }
//        addLog(NetException.NET_CONNECT_ERROR, "Network Not Connected");
        return false;
    }

    public void addLog(String code, String msg) { // remote 里面接口失败日志，buzzee 不知道需不需要
//        try {
//            if (ObjectUtils.isEmpty(msg)){
//                msg = WearUtils.resultNetConnectErrorMessage();
//            }
//            LogUtils.addLog(LogIdType.S0005, url + "#" + code + "#" + msg);
//        } catch (Exception e) {
//        }
    }
}
