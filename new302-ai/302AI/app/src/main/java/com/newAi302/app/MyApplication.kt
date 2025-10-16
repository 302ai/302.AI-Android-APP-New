package com.newAi302.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.databinding.library.BuildConfig
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.newAi302.app.network.httpconfig.HostConfigProvide
import com.newAi302.app.utils.LanguageUtil
import com.newAi302.app.utils.Utils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.utils.base.WearUtil
import com.tencent.mmkv.MMKV

//import leakcanary.LeakCanary

/**
 * author :
 * e-mail :
 * time   : 2025/4/16
 * desc   :
 * version: 1.0
 */
class MyApplication:Application() {
    // 预加载的 WebView 实例（使用 lazy 延迟初始化，避免启动时阻塞）
    val preloadedWebView by lazy {
        createPreloadedWebView(this)
    }

    /**
     * 创建并配置预加载的 WebView
     */
    private fun createPreloadedWebView(context: Context): BridgeWebView {
        return BridgeWebView(context).apply {
            // 基础配置（复用你代码中的设置）
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setGeolocationEnabled(true)


            // 缓存策略优化（预加载时提前缓存资源）
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            settings.databaseEnabled = true

            setAlpha(1.0f)
            // 背景设置（避免黑屏）
            setBackgroundColor(Color.WHITE)
            background = ContextCompat.getDrawable(context, android.R.color.white)

            // TLS 和混合内容配置（复用你代码中的逻辑）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // 预加载时暂时设置基础 WebViewClient（使用时可覆盖）
            webViewClient = object : WebViewClient() {
                // 预加载阶段不需要拦截逻辑，留空即可
            }

            val html = "https://dash.302.ai/sso/login?app=302+AI+Studio&name=302+AI+Studio&icon=https://file.302.ai/gpt/imgs/5b36b96aaa052387fb3ccec2a063fe1e.png&weburl=https://302.ai/&redirecturl=https://dash.302.ai/dashboard/overview&lang=zh-CN"
            // 可选：预加载一个轻量页面（如空白页），避免首次使用时白屏
            loadUrl(html)//"about:blank"
        }
    }

    override fun attachBaseContext(base: Context) {
        // 初始化时应用保存的语言设置
        val language = LanguageUtil.getSavedLanguage(base)
        super.attachBaseContext(LanguageUtil.applyLanguage(base, language))
    }

    override fun onTerminate() {
        super.onTerminate()
        // 应用退出时销毁 WebView，避免内存泄漏
        preloadedWebView.destroy()
    }

    companion object {
        lateinit var myApplicationContext: Context
        lateinit var sharedPreferences: SharedPreferences
        var isFirstLaunch: Boolean = true
    }


    override fun onCreate() {
        super.onCreate()
        myApplicationContext = this
        // LeakCanary 会自动初始化并开始监控
        // 初始化 LeakCanary（无需检查分析进程）
        MMKV.initialize(this)
        Utils.init(this)

        WearUtil.init(this)
        WearUtil.initEvn(HostConfigProvide())
        WearData.getInstance()
        if (BuildConfig.DEBUG) {
//            LeakCanary.config = LeakCanary.config.copy(
//                dumpHeap = true // 确保在调试版本中启用堆转储
//            )
        }
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // 读取首次启动标记
        isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        // 如果是首次启动，在读取后就更新标记（防止多次判断）
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }


    }

}