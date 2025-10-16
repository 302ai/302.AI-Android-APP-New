package com.newAi302.app.ui.model

import com.newAi302.app.bean.EmailCodeBeanRes
import com.newAi302.app.bean.LoginBean
import com.newAi302.app.bean.PhoneSmsRnyCodeBean
import com.newAi302.app.bean.RegisterBeanRes
import com.newAi302.app.network.NetConfig
import com.newAi302.app.network.NetworkUtil
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.callback.ResponseCallback
import com.newAi302.app.network.common_bean.exception.NetException


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/9
 * desc   : 登录 model
 * version: 1.0
 */
class LoginModel {

    companion object {

        /**
         * 登录 -邮箱登录
         */
        fun goLogin(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw(
                    NetConfig.URL_USER_LOGIN,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {

                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        fun goLoginAll(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw(
                    NetConfig.URL_USER_LOGIN_ALL,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {

                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }


        fun goLoginGoogle(
            hashMap: String,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw1(
                    NetConfig.URL_GOOGLE_LOGIN,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {

                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        fun goLoginGoogle1(
            hashMap: MutableMap<String, String>,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw2(
                    NetConfig.URL_GOOGLE_LOGIN,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {

                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        fun goLoginGoogle2(
            hashMap: MutableMap<String, String>,
            callback: LoginCallback
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw3(
                    NetConfig.URL_GOOGLE_LOGIN,
                    hashMap,
                    callback)
        }

        /**
         * 获取注册邮箱 email_code executePost
         */
        fun registerEmailCode(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<EmailCodeBeanRes>>
        ) {
            NetworkUtil.getInstance()
                .executePostAll(NetConfig.url_USER_REGISTER_EMAIL_CODE,
                    hashMap, object : ResponseCallback<BaseResponse<EmailCodeBeanRes>>() {
                        override fun onSuccess(response: BaseResponse<EmailCodeBeanRes>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        /**
         * 邮箱注册
         */
        fun registerEmail(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<RegisterBeanRes>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw(NetConfig.URL_USER_REGISTER_EMAIL,
                    hashMap,
                    object : ResponseCallback<BaseResponse<RegisterBeanRes>>() {
                        override fun onSuccess(response: BaseResponse<RegisterBeanRes>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        /**
         *获取容联云手机验证码
         */
        fun getUserSmsRnyCode(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<PhoneSmsRnyCodeBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw(
                    NetConfig.URL_USER_SMS_RNY,
                    hashMap,
                    object : ResponseCallback<BaseResponse<PhoneSmsRnyCodeBean>>() {
                        override fun onSuccess(response: BaseResponse<PhoneSmsRnyCodeBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        /**
         * 手机号码注册
         */
        fun registerPhone(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<RegisterBeanRes>>
        ) {
            NetworkUtil.getInstance()
                .executePostAll(
                    NetConfig.URL_USER_PHONE_REGISTER,
                    hashMap,
                    object : ResponseCallback<BaseResponse<RegisterBeanRes>>() {
                        override fun onSuccess(response: BaseResponse<RegisterBeanRes>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        /**
         *手机号码登录
         */
        fun goLoginPhone(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw(NetConfig.URL_USER_LOGIN_PHONE,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {
                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        fun goLoginSmsPhone(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<LoginBean>>
        ) {
            NetworkUtil.getInstance()
                .executePostAllRaw1(NetConfig.URL_USER_LOGIN_PHONE_SMS,
                    hashMap,
                    object : ResponseCallback<BaseResponse<LoginBean>>() {
                        override fun onSuccess(response: BaseResponse<LoginBean>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }
                    })
        }

        /**
         * 重置密码 发送邮箱  - 邮箱
         */
        fun resetLoginPassWord(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<Any>>
        ) {
            NetworkUtil.getInstance()
                .executePostAll(NetConfig.URL_USER_RESET_PW_EMAIL,
                    hashMap,
                    object : ResponseCallback<BaseResponse<Any>>() {
                        override fun onSuccess(response: BaseResponse<Any>?) {
                            callback.onSuccess(response)
                        }

                        override fun onError(e: NetException?) {
                            callback.onError(e)
                        }

                    })
        }

        /**
         * 手机号重置密码 - 手机号
         */
        fun resetLoginPassWordPhone(
            hashMap: MutableMap<String, Any>,
            callback: RequestCallback<BaseResponse<Any>>
        ) {
            NetworkUtil.getInstance().executePutAll(NetConfig.URL_USER_RESET_PW_PHONE,
                hashMap, object : ResponseCallback<BaseResponse<Any>>() {
                    override fun onSuccess(response: BaseResponse<Any>?) {
                        callback.onSuccess(response)
                    }

                    override fun onError(e: NetException?) {
                        callback.onError(e)
                    }
                })
        }
    }
}