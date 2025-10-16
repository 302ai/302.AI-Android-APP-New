package com.newAi302.app.network.common_bean.callback;

public interface LoginCallback {
    // 请求成功时的回调方法，参数为响应体内容（这里假设为字符串形式，可根据实际情况调整）
    void onSuccess(String responseBody);
    // 请求失败时的回调方法，参数为错误码和对应的错误信息
    void onFailure(int errorCode, String errorMessage);
}
