package com.newAi302.app.network.common_bean.bean;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public class BaseResponse<T> {
    private boolean result;
    //    private String message;
    private String msg;
    private String code;
    private T data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

/*    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }*/

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
