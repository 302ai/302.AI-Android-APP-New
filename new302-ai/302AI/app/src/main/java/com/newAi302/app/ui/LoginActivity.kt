package com.newAi302.app.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.databinding.ActivityLoginBinding
import com.newAi302.app.databinding.ActivityMainBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.utils.ViewAnimationUtils
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.newAi302.app.ui.login.LoginOneActivity
import com.newAi302.app.ui.login.SplashActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mWebView: BridgeWebView? = null
    //private var mDialog:SettingDialog?=null
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var webViewPreloader: WebViewPreloader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        // 初始化WebView预加载（在Activity创建时触发，提前完成WebView初始化）
        webViewPreloader = WebViewPreloader(this)
        webViewPreloader.preloadWebView()
        binding.loginImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val html = "https://dash.302.ai/sso/login?app=302+AI+Studio&name=302+AI+Studio&icon=https://file.302.ai/gpt/imgs/5b36b96aaa052387fb3ccec2a063fe1e.png&weburl=https://302.ai/&redirecturl=https://dash.302.ai/dashboard/overview&lang=zh-CN"
            //showLoginPickerDialog(html)
            val intent = Intent(this@LoginActivity, LoginOneActivity::class.java)
            startActivity(intent)
        }
        binding.loginBackImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            finish()
        }

        //测试使用  sk-4ogGTjSn67pc9RREx3wOxpquTnF1vIKJN6nRVk4z8jteyfkd
        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.saveData("sk-4ogGTjSn67pc9RREx3wOxpquTnF1vIKJN6nRVk4z8jteyfkd")
            dataStoreManager.saveUserName("gga")
        }

    }



    private fun showLoginPickerDialog(url:String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_login_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // 1. 从 Application 中获取预加载的 WebView
       /* val app = application as MyApplication
        mWebView = app.preloadedWebView

        // 2. 重新设置 WebView 的父容器（关键：将预加载的 WebView 添加到当前 Dialog 的布局中）
        val webViewContainer = dialog.findViewById<ViewGroup>(R.id.webView_login) // 需在布局中添加容器

        // 关键步骤：先从原有父容器中移除 WebView
        val currentParent = mWebView?.parent
        if (currentParent is ViewGroup) {
            currentParent.removeView(mWebView) // 从原父容器移除
        }

        webViewContainer.removeAllViews() // 清空容器
        webViewContainer.addView(mWebView) // 将预加载的 WebView 加入容器*/
        // 关键：获取预加载的WebView（已从临时容器移除，无父容器冲突）
        mWebView = webViewPreloader.getPreloadedWebView() ?: run {
            // 降级处理：若预加载失败，重新创建WebView（避免崩溃）
            BridgeWebView(this).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
                settings.javaScriptEnabled = true
                // ... 复用基础配置
            }
        }

        // 将复用的WebView添加到Dialog的布局中（R.id.webView_login是Dialog布局中的容器）
        val webViewContainer = dialog.findViewById<ViewGroup>(R.id.webView_login)
        webViewContainer?.apply {
            removeAllViews() // 清空容器，避免残留视图
            addView(mWebView) // 添加预加载的WebView
        }

        //mWebView = dialog.findViewById<BridgeWebView>(R.id.webView_login)
        mWebView?.settings?.javaScriptEnabled = true
//        mWebView?.settings?.databaseEnabled = true //数据库缓存
        mWebView?.settings?.setGeolocationEnabled(true) // 允许网页定位
        mWebView?.settings?.loadsImagesAutomatically = true // 加快网页加载完成的速度，等页面完成再加载图片
        mWebView?.settings?.domStorageEnabled = true       // 开启 localStorage
        /*mWebView?.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        mWebView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 在页面加载完成后注入监听 window.postMessage 的 JavaScript 代码
                mWebView?.evaluateJavascript(
                    """
                window.addEventListener('info', function(info) {
                    AndroidInterface.receiveMessage(JSON.stringify(info.data));
                });
            """.trimIndent(), null
                )
            }
        }*/
        // 如果需要，还可以设置透明度
        mWebView?.setAlpha(1.0f)
        // 设置背景色为白色（覆盖默认的透明/黑色）
        mWebView?.setBackgroundColor(Color.WHITE)
        // 加载前显示白色背景（避免短暂黑屏）
        mWebView?.background = ContextCompat.getDrawable(this@LoginActivity, android.R.color.white)

        mWebView?.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                // 先调用父类方法获取原始响应（也可自己重新请求网络获取响应，按需选择）
                val originalResponse = super.shouldInterceptRequest(view, request)

                if (request?.url.toString().contains("apikey=")) {
                    // 解析 JSON 数据，假设返回的是 JSON 格式
                    try {
                        Log.e("ceshi","不是空${request?.url.toString()}")
                        Log.e("ceshi","截取的key：${extractApiKey(request?.url.toString())}")
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveData(extractApiKey(request?.url.toString())!!)
                            dataStoreManager.saveUserName(extractApiUserName(request?.url.toString())!!)
                        }
                        lifecycleScope.launch(Dispatchers.Main) {
                            //mDialog?.findViewById<EditText>(R.id.edit_apiKey)?.setText(extractApiKey(request?.url.toString()))
                            Toast.makeText(this@LoginActivity, "登录成功，谢谢", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return originalResponse
            }
        }



        val html = "https://dash.302.ai/sso/login?app=302+AI+Studio&name=302+AI+Studio&icon=https://file.302.ai/gpt/imgs/5b36b96aaa052387fb3ccec2a063fe1e.png&weburl=https://302.ai/&redirecturl=https://dash.302.ai/dashboard/overview&lang=zh-CN"
        val htmlTest = "https://test-dash.gpt302.com/sso/login?app=302.ai&name=302AI&icon=https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f13fb09879ecb5185e440cef6eb9.png~tplv-uwbnlip3yd-webp.webp&weburl=https://baidu.com&redirecturl=https://test-dash.gpt302.com/dashboard/overview&lang=zh-CN"
        mWebView?.apply {
            settings.javaScriptEnabled = true // 确保 JS 启用（部分网站依赖 JS 加载）
            settings.domStorageEnabled = true
            settings.useWideViewPort = true // 适配屏幕，避免加载后样式问题间接导致超时
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
            loadUrl(url)
        }
        //mWebView?.loadUrl(url)


        dialog.findViewById<Button>(R.id.cancelLoginButton).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun extractApiKey(url: String): String? {
        // 查找问号（参数起始位置）
        val queryStart = url.indexOf('?')
        if (queryStart == -1) {
            return null // 没有参数部分
        }

        // 提取所有参数（问号后面的部分）
        val queryParams = url.substring(queryStart + 1)

        // 分割参数（按 & 符号）
        val params = queryParams.split("&")

        // 遍历参数，找到 apikey
        for (param in params) {
            val keyValue = param.split("=", limit = 2) // 限制分割为2部分（防止值中包含=）
            if (keyValue.size == 2 && keyValue[0] == "apikey") {
                return keyValue[1] // 返回apikey的值
            }
        }

        return null // 未找到apikey参数
    }

    private fun extractApiUserName(url: String): String? {//ttps://dash.302.ai/dashboard/overview?apikey=sk-4ogGTjSn67pc9RREx3wOxpquTnF1vIKJN6nRVk4z8jteyfkd&uid=MjIwOTI1&username=ghh
        // 查找问号（参数起始位置）
        val queryStart = url.indexOf('?')
        if (queryStart == -1) {
            return null // 没有参数部分
        }

        // 提取所有参数（问号后面的部分）
        val queryParams = url.substring(queryStart + 1)

        // 分割参数（按 & 符号）
        val params = queryParams.split("&")

        // 遍历参数，找到 apikey
        for (param in params) {
            val keyValue = param.split("=", limit = 2) // 限制分割为2部分（防止值中包含=）
            if (keyValue.size == 2 && keyValue[0] == "username") {
                return keyValue[1] // 返回apikey的值
            }
        }

        return null // 未找到apikey参数
    }


    class WebViewPreloader(private val activity: LoginActivity) {
        // 暂存预加载的WebView（用Activity Context创建）
        private var preloadedWebView: BridgeWebView? = null
        // 临时父容器（仅用于暂存WebView，避免无父容器报错）
        private val tempContainer = FrameLayout(activity)

        // 预加载WebView（在Activity onCreate时调用）
        fun preloadWebView() {
            if (preloadedWebView != null) return // 避免重复创建

            // 1. 用Activity Context创建WebView（关键：避免Application Context）
            val webView = BridgeWebView(activity).apply {
                // 2. 初始化绘制参数（解决黑屏核心：设置白色背景+禁用异常硬件加速）
                setBackgroundColor(android.graphics.Color.WHITE) // 强制白色背景，避免默认黑色
                background = activity.getDrawable(android.R.color.white) // 双重保障
                setLayerType(WebView.LAYER_TYPE_SOFTWARE, null) // 部分设备硬件加速导致黑屏，临时禁用（可根据测试开关）

                // 3. 初始化WebSettings（复用你原有的配置）
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // 4. 绑定WebViewClient（复用你原有的SSL和错误处理逻辑）
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: android.webkit.SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed() // 仅调试用，正式环境需验证证书！
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: android.webkit.WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            activity.runOnUiThread {
                                android.widget.Toast.makeText(
                                    activity,
                                    "加载失败：${error?.description}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            // 5. 暂存WebView到临时容器（避免无父容器报错）
            tempContainer.addView(webView)
            preloadedWebView = webView
        }

        // 获取预加载的WebView（在Dialog显示前调用）
        fun getPreloadedWebView(): BridgeWebView? {
            val webView = preloadedWebView ?: return null
            // 关键：从临时容器中移除WebView，避免父容器冲突
            (webView.parent as? ViewGroup)?.removeView(webView)
            return webView
        }

        // 销毁WebView（和Activity生命周期绑定，避免内存泄漏）
        fun destroyWebView() {
            preloadedWebView?.apply {
                stopLoading()
                webViewClient = android.webkit.WebViewClient() // 解绑客户端
                removeAllViews()
                destroy()
            }
            preloadedWebView = null
            tempContainer.removeAllViews()
        }
    }

}