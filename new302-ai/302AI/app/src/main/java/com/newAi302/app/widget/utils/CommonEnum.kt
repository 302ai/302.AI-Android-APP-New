package com.newAi302.app.widget.utils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/6/3
 * desc   : 全局枚举
 * version: 1.0
 */
object CommonEnum {


    interface PassWordType {
        companion object {
            const val EMAIL: Int = 1  //邮箱
            const val PHONE: Int = 2  //手机号码
        }
    }

    //加载html路径
    interface LoadHtmlType {
        companion object {
            const val NEED_SPLICED_PARA: Int = 1      //需拼接参数  如：汇总
            const val NOT_NEED_SPLICED_PARA: Int = 2  //不需要拼接参数
            const val DIRECT_LINK: Int = 3            //直接链接
        }
    }

    //proxyType
    interface ProxyType {
        companion object {
            const val PROXY_TYPE_SOCKS: String = "socks5"
            const val PROXY_TYPE_HTTP: String = "http"
        }
    }
}