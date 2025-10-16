package com.newAi302.app.network;


import com.newAi302.app.network.common_bean.callback.ResponseCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : 网络请求工具
 * version: 1.0
 */
public class NetworkClient {

    private final Map<String, Object> PARAMS;
    private final String URL;
    private final RequestBody BODY;
    //上传下载
    private final File FILE;
    private final List<File> FILE_LIST;
    private final ResponseCallback CALLBACK;
    private HashMap<String, String> ADDHEADERS;

    public <T> NetworkClient(Map<String, Object> params,
                             String url,
                             RequestBody body,
                             File file,
                             List<File> file_list,
                             HashMap<String, String> addHeaders,
                             ResponseCallback<T> callback) {
        this.PARAMS = params;
        this.URL = url;
        this.BODY = body;
        this.FILE = file;
        this.FILE_LIST = file_list;
        this.ADDHEADERS = addHeaders;
        this.CALLBACK = callback;
    }

    public static NetworkClientBuilder create() {
        return new NetworkClientBuilder();
    }

    //真实的网络操作
    private Flowable<ResponseBody> request(HttpMethod method) {
        final NetService service = NetworkCreator.getNetService(CALLBACK);

        Flowable<ResponseBody> flowable = null;
        switch (method) {
            case GET:
                flowable = service.getRequest(URL, "", "");
                break;
            case GET_RAW:
                flowable = service.getJson(URL);
                break;
            case POST:
                flowable = service.postRequest(URL, PARAMS);
                break;
            case POST_RAW:
                flowable = service.postJson(URL, BODY);
                break;
            case PUT:
                flowable = service.put(URL, PARAMS);
                break;
            case DELETE:
                flowable = service.delete(URL, PARAMS);
                break;
            case UPLOAD:
                final RequestBody requestBody = RequestBody.create(MultipartBody.FORM, FILE);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                HashMap<String, String> addHeaders = new HashMap<>();

                if (ADDHEADERS != null && ADDHEADERS.size() > 0) {
                    addHeaders = ADDHEADERS;
                }

                flowable = service.upload(URL, body, addHeaders);
                break;
            case UPLOAD_MULTI:
                List<MultipartBody.Part> partList = new ArrayList<>(FILE_LIST.size());
                for (File file : FILE_LIST) {
                    RequestBody bodyFile = RequestBody.create(MultipartBody.FORM, file);
                    final MultipartBody.Part partFile = MultipartBody.Part.createFormData(file.getName(), file.getName(), bodyFile);
                    partList.add(partFile);
                }
                flowable = service.uploadMultiFile(URL, partList);
                break;
            case POST_UPLOAD_FILE_AND_PARAMS:
                final RequestBody reqPostUploadBody = RequestBody.create(MultipartBody.FORM, FILE);
                final MultipartBody.Part postUploadBody = MultipartBody.Part.createFormData("file", FILE.getName(), reqPostUploadBody);

                HashMap<String, RequestBody> postUploadBodyHashMap = new HashMap<>();
                if (this.PARAMS != null) {
                    Set<String> set = PARAMS.keySet();
                    Iterator<String> iterator = set.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String values = (String) PARAMS.get(key);
                        if (values != null) {
                            postUploadBodyHashMap.put(key, RequestBody.create(MediaType.parse("text/plain; charset=UTF-8"), values));
                        }
                    }
                }
                flowable = service.postUpload(URL, postUploadBodyHashMap, postUploadBody);
                break;
            case POST_GZIP:
                flowable = service.postGzip(URL, this.BODY);
                break;
            default:
                break;
        }
        return flowable;
    }

    //各种请求
    public final Flowable<ResponseBody> get() {
        return request(HttpMethod.GET);
    }

    public final Flowable<ResponseBody> getRaw() {
        return request(HttpMethod.GET_RAW);
    }

    public final Flowable<ResponseBody> post() {
        return request(HttpMethod.POST);
    }

    public final Flowable<ResponseBody> postraw() {
        return request(HttpMethod.POST_RAW);
    }

    public final Flowable<ResponseBody> put() {
        return request(HttpMethod.PUT);
    }

    public final Flowable<ResponseBody> delete() {
        return request(HttpMethod.DELETE);
    }

    public final Flowable<ResponseBody> upload() {
        return request(HttpMethod.UPLOAD);
    }

    public final Flowable<ResponseBody> uploadMulti() {
        return request(HttpMethod.UPLOAD_MULTI);
    }

    public final Flowable<ResponseBody> postUpload() {
        return request(HttpMethod.POST_UPLOAD_FILE_AND_PARAMS);
    }

    public final Flowable<ResponseBody> postGzip() {
        return request(HttpMethod.POST_GZIP);
    }

    public final Flowable<ResponseBody> download() {
        return NetworkCreator.getNetService(CALLBACK).download(URL, PARAMS);
    }
}