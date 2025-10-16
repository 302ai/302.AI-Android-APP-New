package com.newAi302.app.ui

import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityAnnouncementBinding
import com.newAi302.app.databinding.ActivityVersionInformationBinding

class VersionInformationActivity : BaseActivity() {
    private lateinit var binding: ActivityVersionInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVersionInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }

        /*binding.webViewUrl.settings.javaScriptEnabled = true// 启用JavaScript（可选，根据HTML内容需求）
        binding.webViewUrl.settings.domStorageEnabled = true// 启用DOM存储（可选）

        binding.webViewUrl.loadUrl(
            "https://302.ai/downloads/"
        )*/
        // 在加载 URL 前添加以下配置
        binding.webViewUrl.apply {
            // 设置背景色为白色（覆盖默认的透明/黑色）
            setBackgroundColor(Color.WHITE)
            // 加载前显示白色背景（避免短暂黑屏）
            background = ContextCompat.getDrawable(context, android.R.color.white)

            // 其他已有配置
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            // 新增：屏幕适配核心设置
            settings.apply {
                // 1. 让WebView支持<meta name="viewport">标签
                useWideViewPort = true  // 允许网页的viewport设置生效
                //loadWithOverviewMode = true  // 缩放至屏幕大小

                // 2. 设置初始缩放比例（0表示不缩放，自动适配）
                //setInitialScale(0)

                // 3. 可选：允许用户手动缩放（根据需求开启）
                builtInZoomControls = false  // 关闭内置缩放控件
                supportZoom() // 禁用缩放功能（避免用户手动缩放导致布局错乱）

                // 4. 适配不同屏幕密度
                displayZoomControls = false  // 隐藏缩放按钮
            }

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

            // 可选：如果网页本身是黑色背景，强制注入 CSS 修改
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 注入 JS 强制网页背景为白色（针对网页自身样式问题）
                    view?.loadUrl("javascript:(function() { " +
                            "document.body.style.backgroundColor = '#ffffff'; " +
                            "document.body.style.color = '#000000'; " +  // 同时确保文字颜色为黑色
                            "})()")
                }
            }

            loadUrl("https://302.ai/downloads/")
        }

    }
}