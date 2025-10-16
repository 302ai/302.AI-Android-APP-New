package com.newAi302.app.network.httpconfig

import com.newAi302.app.module.IConfigProvide
import com.newAi302.app.utils.base.WearUtil

/**
 * author : lzh
 * e-mail :
 * time   : 2024/5/7
 * desc   : 各环境 host 初始化
 * version: 1.0
 */
class HostConfigProvide : IConfigProvide {

    //测试环境
    private val TestHost = "https://test-api2.proxy302.com"
    private val TestHostHtml = "https://test-dashboard2.proxy302.com"

    //正式环境
    private val ProductHost = "https://dash-api.302.ai"//https://dash-api.302.ai//https://dash-api.proxy302.com
    private val ProductHostHtml = "https://dash.proxy302.com"//https://dash.proxy302.com


    //测试环境
    override fun initTest() {
        WearUtil.APP_SERVER_HTTPS = TestHost
    }

    //测试环境 Html
    override fun initTestHtml() {
        WearUtil.APP_SERVER_HTTPS_HTML = TestHostHtml
    }


    /**
     * 预生产环境 暂时不用
     */
    override fun initPreProduct() {

    }

    /**
     * 生产环境（正式环境）
     */
    override fun initProduct() {
        WearUtil.APP_SERVER_HTTPS = ProductHost
    }

    /**
     * 生产环境Html（正式环境）
     */
    override fun initProductHtml() {
        WearUtil.APP_SERVER_HTTPS_HTML = ProductHostHtml
    }
}