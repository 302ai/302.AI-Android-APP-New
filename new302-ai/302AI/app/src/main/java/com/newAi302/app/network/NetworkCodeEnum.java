package com.newAi302.app.network;


import com.newAi302.app.utils.base.WearUtil;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : 统一错误码
 * version: 1.0
 */
public class NetworkCodeEnum {

    /**
     * 服务器错误码和错误信息，错误信息等多语言翻译，暂时使用服务器直接返回的
     */
    public static final String CODE_20000 = "901";    //发送次数已超上限
    public static final String CODE_20001 = "20001";    //参数pf错误
    public static final String CODE_20002 = "20002";    //参数ver错误

    /**
     * 特殊错误返回
     */
    public static final String NET_ERROR_HTTP_413 = "HTTP 413 Request Entity Too Large";   //文件上传太大
    public static final String NET_ERROR_HTTP_429 = "Operation is too frequent, please try again later";    //请求太频繁

    public static ErrorCode maybeChangeMsg(String code, String msg) {
        switch (code) { // 不显示系统错误提示

            case CODE_20000:
            case CODE_20001:
            case CODE_20002:
//                msg = resultNetWorkErrorMessage(R.string.server_params_error);
                break;
        }
        return new ErrorCode(code, msg);
    }

    public static String resultSystemErrorMessage(Throwable e) {
        if (WearUtil.getApplication() != null) {
        }
        if (e != null && e.getMessage() != null) {
//            switch (e.getMessage()) {
//                case NET_ERROR_HTTP_413:
//                    return NET_ERROR_HTTP_413;
//            }
            return e.getMessage();
        }
//        return "Unable to connect to the server 0";
        return "";
    }

    public static String resultTimeOutErrorMessage() {
        if (WearUtil.getApplication() != null) {

        }
        return "Unable to connect to the server 1";
    }

    public static String resultNetConnectErrorMessage() {
        if (WearUtil.getApplication() != null) {

        }
        return "Unable to connect to the server 2";
    }

    public static String resultNetWorkErrorMessage(int messageCode) {
        if (WearUtil.getApplication() != null) {
//            return StoreUtil.getString(messageCode);
            return null;
        }
        return "Unable to connect to the server";
    }

    public static String resultNetDataErrorMessage(String message) {
        return "数据解析错误：" + message;
    }

    public static class ErrorCode {

        private String code;
        private String msg;

        ErrorCode(String code, String notice) {
            this.code = code;
            this.msg = notice;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
