package com.newAi302.app.network.common_bean.exception;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 错误码
 * version: 1.0
 */
public class NetException extends RuntimeException {
    // 通用错误
    public static String VERIFICATION_CODE_ERROR = "900";  //验证码错误
    public static String LOGIN_WRONG_PASSWORD = "604";  //登录密码错误
    public static String RESPONSE_SUCCESS = "success";  //接口响应成功
    public static String RESPONSE_FAIL = "fail";        //接口响应失败
    public static String NO_REGISTER = "2000";        //该邮箱账号未注册
    public static String ALREADY_REGISTERED = "605"; //该邮箱已经注册

    public static String SERVICE_DATA_ERROR = ""; //服务器数据错误
    public static String LOCAL_UN_DEFINE_ERROR = "A000";  // 未归类的错误 localUnDefineError

    public static String NULL_PORINT_ERROR = "A012";// 网络空指针异常
    public static String A013 = "A013";  // 批量文件上传，发现文件不存在的情况

    //    public static String SERVER_UN_DEFINE_ERROR = "S000";  // 服务器未定义的错误类型  serverUnDefineError
    public static String SERVER_UN_DEFINE_ERROR = "0";  // 服务器未定义的错误类型  serverUnDefineError

    public static String OVER_RATE_LIMIT_ERROR = "429"; // http状态码为429了（超出了请求频次）

    public String code; // -101 服务器异常（返回数据为空）
    public String message;

    public NetException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getDetailMessage() {
        return message + " [" + code + "]";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "NetException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
