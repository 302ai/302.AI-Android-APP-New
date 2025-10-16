package com.newAi302.app.widget.html

import android.content.Context
import android.content.Intent
import android.net.Uri


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/15
 * desc   : html管理类
 * version: 1.0
 */
object HtmlManager {

    /**
     * 跳转指定浏览器
     *
     * @param context
     * @param url 跳转url
     */
    fun jumpHtml(context: Context, url: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val uri = Uri.parse(url)
        intent.setData(uri)

        if (isApplicationAvilible(context, "com.android.browser")) {
            //Chrome 浏览器
            intent.setPackage("com.android.browser");
        } else if (isApplicationAvilible(context, "com.hicloud.browser")) {
            // 华为浏览器
            intent.setPackage("com.hicloud.browser");
        } else if (isApplicationAvilible(context, "com.android.chrome")) {
            //默认浏览器 （测试了一下可以启动荣耀8和小米10的系统自带的默认浏览器）
            intent.setPackage("com.android.chrome")
        }
        context.startActivity(intent)
    }

    /**
     *判断手机是否安装了此浏览器
     * @param context
     * @param appPackageName 应用包名
     * @return true：安装，false：未安装
     */
    private fun isApplicationAvilible(context: Context, appPackageName: String): Boolean {
        val packageManager = context.packageManager // 获取packagemanager
        val list = packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息
        for (i in list.indices) {
            val packageName = list[i].packageName;
            if (appPackageName == packageName) {
                return true
            }
        }
        return false
    }
}