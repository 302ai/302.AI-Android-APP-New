package com.newAi302.app.widget.dialog

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.newAi302.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialog : BottomSheetDialogFragment() {
    var type:String = "UTs"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载自定义布局文件，这里假设布局文件为R.layout.bottom_sheet_layout
        val view = inflater.inflate(R.layout.bottom_one_sheet_layout, container, false)
        // 可以在这里对视图进行初始化操作，例如设置按钮点击事件等
        val webView = view.findViewById<WebView>(R.id.webViewMessage)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        webView?.apply {
            settings.javaScriptEnabled = true // 确保 JS 启用（部分网站依赖 JS 加载）
            settings.domStorageEnabled = true
            //settings.useWideViewPort = true // 适配屏幕，避免加载后样式问题间接导致超时
            settings.loadWithOverviewMode = true

            // 优先使用缓存，无缓存时再请求网络
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            // 启用 HTML5 缓存（如 localStorage、IndexedDB）
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            // 1. 支持 TLS 1.0~1.3（适配低版本 Android）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许混合内容（HTTPS 加载 HTTP 资源）
                webViewClient = object : WebViewClient() {
                    // 解决 TLS 协议支持问题
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        // 注意：仅调试时使用！正式环境需验证证书，避免安全风险
                        handler?.proceed() // 忽略 SSL 证书错误（临时排查用）
                    }

                    // 捕获加载错误，打印日志定位问题
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        // 打印错误信息（在 Logcat 中搜索 "WebViewError"）
                        Log.e("WebViewError", "错误码：${error?.errorCode}，描述：${error?.description}")
                        when (error?.errorCode) {
                            -8 -> { // ERR_CONNECTION_TIMED_OUT
                                Toast.makeText(context, "连接超时，请检查网络或稍后重试", Toast.LENGTH_SHORT).show()
                            }
                            -2 -> { // 无网络
                                Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, "加载失败：${error?.description}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            if (type == "UTs"){
                loadUrl("https://302.ai/legal/terms/")//https://www.proxy302.com/en/terms_content/
            }else{
                loadUrl("https://302.ai/legal/privacy/")//https://www.proxy302.com/en/privacy_content/
            }
        }


        /*if (type == "UTs"){
            webView.loadUrl("https://www.proxy302.com/en/terms_content/")
        }else if (type == "PAs"){
            webView.loadUrl("https://www.proxy302.com/en/privacy_content/")
        }*/


        return view
    }


    fun setMessageType(type:String){
        this.type = type
    }
}