package com.newAi302.app.network.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public class JsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private static final String TAG = "JsonConverter";

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    JsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {

        try {
            String body = value.string();
            JSONObject jsonObj = new JSONObject(body);
            int status = jsonObj.optInt("status");
            String msg = jsonObj.optString("msg", "服务器开小差了~~");

            if (status == 1) {
                if (jsonObj.has("data")) {
                    Object data = jsonObj.get("data");
                    body = data.toString();
                    return adapter.fromJson(body);
                } else {
                    return (T) msg;
                }
            } else {
                throw new RuntimeException(msg);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            value.close();
        }
    }
}
