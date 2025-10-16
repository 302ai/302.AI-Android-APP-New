package com.newAi302.app.network.common_bean.callback;


import  com.newAi302.app.network.common_bean.exception.NetException;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 请求Callback
 * version: 1.0
 */
public abstract class RequestCallback<T> {

    //请求前
    public void onStart() {
    }

    //请求成功
    public abstract void onSuccess(T data);


    //请求完成
    public void onCompleted() {
    }

    //请求失败
    public abstract void onError(NetException e);

    //进度
    public void onProgress(long downSize, long fileSize) {
    }

    public void dowloadSuccess(String path, String fileName, long fileSize) {
    }

    public void imgSuccess(byte[] bytes){

    }
}
