package com.newAi302.app.network;


import com.newAi302.app.network.common_bean.callback.ResponseCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   :
 * version: 1.0
 */
public class NetworkClientBuilder {
    private Map<String, Object> mParams;
    private String mUrl;
    private RequestBody mBody;
    //上传下载
    private File mFile;
    private List<File> mFileList;
    private ResponseCallback mCallback;
    private HashMap<String, String> mAddHeaders;


    public NetworkClientBuilder(){

    }

    public final NetworkClientBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final NetworkClientBuilder params(Map<String, Object> params) {
        this.mParams = params;
        return this;
    }

    public final NetworkClientBuilder raw(String raw) {
        this.mBody = RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"), raw);
        return this;
    }


    public final NetworkClientBuilder postGzip(RequestBody body) {
        this.mBody = body;
        return this;
    }

    //上传
    public final NetworkClientBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final NetworkClientBuilder file(String file) {
        this.mFile = new File(file);
        return this;
    }

    public final NetworkClientBuilder fileList(List<File> fileList) {
        this.mFileList = fileList;
        return this;
    }

    public final NetworkClientBuilder addHeaders(HashMap<String, String> addHeaders) {
        this.mAddHeaders = addHeaders;
        return this;
    }

    public <T> NetworkClientBuilder callbback(ResponseCallback<T> callback) {
        this.mCallback = callback;
        return this;
    }

    public final NetworkClient build() {
        return new NetworkClient(mParams, mUrl, mBody, mFile, mFileList, mAddHeaders, mCallback);
    }
}
