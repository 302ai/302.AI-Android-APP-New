package com.newAi302.app.network.common_bean.callback;


import com.newAi302.app.network.common_bean.exception.NetException;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 响应回调
 * version: 1.0
 */
public abstract class ResponseCallback<T> {
    public void onStart() {
    }

    public void onProgress(long uploadSize, long fileSize) {

    }

    public void onCompleted() {
    }

    public void onSuccessImg(byte[] bytes) {

    }

    public abstract void onSuccess(T response);

    public abstract void onError(NetException e);
}
