package com.newAi302.app.widget.utils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/6/4
 * desc   : 相关Html路径工具类
 * version: 1.0
 */
class CommonHtmlUtil {

    companion object {
        val handlerName = "routerHandler"  //统一的配置名称 与 H5交换 跳转链接使用
        val handlerNamePara = "testiOSCallback" //统一配置名称 与 H5数据交换 截获data数据

        //跳转页面链接
        val totalHtmlPath = "/webapp/authentication/"   //汇总
        val dynamicTrafficHtmlPath = "/webapp/proxy/dynamic/traffic"  //动态IP - 按流量计费
        val dynamicIpHtmlPath = "/webapp/proxy/dynamic/ip"  //动态IP - 按IP计费
        val staticTrafficHtmlPath = "/webapp/proxy/static/traffic"  //静态IP  - 按流量计费
        val staticIpHtmlPath = "/webapp/proxy/static/ip" //静态IP  - 按IP计费
        val quickAccessHtmlPath = "/webapp/quick-access"   //快捷访问
        val chargeHtmlPath = "/webapp/charge"  //充值
        val userCenterHtmlPath = "/webapp/user-center"  //个人中心
        val helpCenterHtmlPath = "https://proxy302.helplook.com/"  //帮助中心

        //json数据格式
        val htmlType = "type"
        val htmlData = "data"

        //交互事件
        val logoutHtml = "logout" //登出
        val proxyHtml = "proxy"  //代理
        val goLinkHtml = "goLink" //链接
        val quickAccessHtml = "quick-access" //快速访问
        val chargeHtml = "charge" //去充值

        val changeStatusHtml = "change-status"//开关某一代理
        val deleteStatusHtml = "delete-proxy"
        val payBillHtml = "chargeBill"

    }
}