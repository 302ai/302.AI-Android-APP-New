package com.newAi302.app.network.callback;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 网络请求 - 进度监听
 * version: 1.0
 */
public interface NetworkProgressListener {
    void onProgress(long uploadSize, long fileSize);
}
