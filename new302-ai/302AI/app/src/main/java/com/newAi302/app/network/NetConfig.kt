package com.newAi302.app.network;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : 网络请求URL常量类
 * version: 1.0
 */
class NetConfig {

//    val URL_APP_CONFIG = "/dpg/v2/common/app-config.json" //APP配置信息

    companion object {
        @JvmField
        val URL_GOOGLE_LOGIN: String = "/user/register/google"

        @JvmField
        var URL_USER_LOGIN: String = "/user/login"   //登录接口
        @JvmField
        var URL_USER_LOGIN_ALL: String = "/user/login/all"   //登录接口

        @JvmField
        val URL_USER_INFO: String = "/user/info"     //获取用户信息

        @JvmField
        val url_USER_REGISTER_EMAIL_CODE = "/user/register/email/code" //获取邮箱code - 注册时需要用到 注：针对邮箱

        @JvmField
        val url_USER_REGISTER_EMAIL_CODE_NEW = "/user/reset_pw/email/code" //获取邮箱code - 注册时需要用到 注：针对邮箱

        @JvmField
        val URL_USER_REGISTER_EMAIL: String = "/user/v1/register" //注册接口，生产发送邮箱的激活链接

        @JvmField
        val URL_PROXY_STATIC_IMAGE: String = "/proxy/static/image"  //获取图片验证码

        @JvmField
        val URL_USER_RESET_PW: String = "/user/reset_pw"  //手机号码重置密码

        @JvmField
        val URL_USER_PHONE_REGISTER: String = "/user/register/phone"  //手机号码注册

        @JvmField
        val URL_USER_SMS_RNY: String = "/user/sms/rny"   //获取容联云手机验证码

        @JvmField
        val URL_USER_LOGIN_PHONE: String = "/user/login/phone" //手机号登录

        @JvmField
        val URL_USER_LOGIN_PHONE_SMS: String = "/user/sms/phone" //手机号验证码登录

        @JvmField
        val URL_USER_RESET_PW_EMAIL: String = "/user/reset_pw" //重置密码，发送邮箱

        @JvmField
        val URL_USER_RESET_PW_EMAIL_NEW_EMAIL: String = "/user/reset_pw_with_code" //重置密码，发送邮箱,新邮箱

        @JvmField
        val URL_USER_RESET_PW_PHONE: String = "/user/reset_pw" //手机号重置密码

        @JvmField
        val URL_PROXY_SECRET: String = "/proxy/secret" //获取代理加密密钥

        @JvmField
        val URL_FORWARD_DOMAIN = "/api/forward/domain" //获取中转域名的接口

        @JvmField
        val URL_ALIPAY_GET_URL = "/proxy/charges/alipay"

        @JvmField
        val URL_USTD_GET_URL = "/api/epay/"

        @JvmField
        val URL_PROXY_GET_APK: String = "/api/v1/app/version"
    }
}
