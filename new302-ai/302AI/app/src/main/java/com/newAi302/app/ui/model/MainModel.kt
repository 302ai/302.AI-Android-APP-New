package com.newAi302.app.ui.model

import android.util.Log
import com.newAi302.app.bean.ForwardDomainBean
import com.newAi302.app.bean.ImageVerifyCodeBean
import com.newAi302.app.bean.ProxyApkBean
import com.newAi302.app.bean.ProxySecretBean
import com.newAi302.app.network.NetConfig
import com.newAi302.app.network.NetworkUtil
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.callback.AliPayData
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.callback.ResponseCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.ToastUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   :
 * version: 1.0
 */
object MainModel {

    /**
     * 获取图片验证码
     */
    fun getProxyStaticImage(
        url: String,
        callback: RequestCallback<BaseResponse<ImageVerifyCodeBean>>
    ) {
        NetworkUtil.getInstance().executeGet(
            url,
            object : ResponseCallback<BaseResponse<ImageVerifyCodeBean>>() {
                override fun onSuccess(response: BaseResponse<ImageVerifyCodeBean>?) {

                }

                override fun onError(e: NetException?) {

                }

                override fun onSuccessImg(bytes: ByteArray?) {
                    super.onSuccessImg(bytes)
                    callback.imgSuccess(bytes)
                }
            })
    }

    /**
     * 获取代理的加密密钥
     */
    fun getProxySecret(
        network: String,
        callback: RequestCallback<BaseResponse<ProxySecretBean>>
    ) {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["network"] = network

        NetworkUtil.getInstance()
            .executeGetAllRaw(
                NetConfig.URL_PROXY_SECRET,
                hashMap,
                object : ResponseCallback<BaseResponse<ProxySecretBean>>() {
                    override fun onSuccess(response: BaseResponse<ProxySecretBean>?) {
                        callback.onSuccess(response)
                    }

                    override fun onError(e: NetException?) {
                        callback.onError(e)
                    }
                })
    }

    fun getProxyApk(
        callback: RequestCallback<BaseResponse<ProxyApkBean>>
    ) {
        NetworkUtil.getInstance()
            .executeGet(
                NetConfig.URL_PROXY_GET_APK,
                object : ResponseCallback<BaseResponse<ProxyApkBean>>() {
                    override fun onSuccess(response: BaseResponse<ProxyApkBean>?) {
                        callback.onSuccess(response)
                    }

                    override fun onError(e: NetException?) {
                        callback.onError(e)
                    }
                })
    }

    /**
     * 获取中转域名的接口
     */
    fun getForwardDomain(callback: RequestCallback<BaseResponse<ForwardDomainBean>>) {
        NetworkUtil.getInstance().executeGet(NetConfig.URL_FORWARD_DOMAIN,
            object : ResponseCallback<BaseResponse<ForwardDomainBean>>() {
                override fun onSuccess(response: BaseResponse<ForwardDomainBean>?) {
                    callback.onSuccess(response)
                }

                override fun onError(e: NetException?) {
                    callback.onError(e)
                }
            })
    }

    fun getAliPayUrl(payway_id:Int,callback: LoginCallback){
        NetworkUtil.getInstance().executePostAllRaw4(NetConfig.URL_ALIPAY_GET_URL,
            payway_id,
            callback)
    }

    fun getUstdurl(payway_id:Int,callback: LoginCallback){
        NetworkUtil.getInstance().executePostAllRaw5(NetConfig.URL_USTD_GET_URL,
            payway_id,
            callback)
    }

    fun getStripeKey(payway_id: Int,callback: LoginCallback){
        NetworkUtil.getInstance().executePostAllRaw6(NetConfig.URL_USTD_GET_URL,
            payway_id,
            callback)
    }

}