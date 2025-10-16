package com.newAi302.app.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.google.gson.Gson
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.bean.HtmlChargeJsonBean
import com.newAi302.app.bean.HtmlQuickAccessBean
import com.newAi302.app.constant.AppConstant
import com.newAi302.app.databinding.ActivityPayBinding
import com.newAi302.app.databinding.ActivityVideoPlayerBinding
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.callback.ResponseAliData
import com.newAi302.app.network.common_bean.callback.ResponseStripeData
import com.newAi302.app.network.common_bean.callback.ResponseUsdtData
import com.newAi302.app.ui.model.MainModel
import com.newAi302.app.utils.ActivityUtils
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.utils.base.WearUtil
import com.newAi302.app.widget.dialog.PayDetailsDialog
import com.newAi302.app.widget.utils.CommonEnum
import com.newAi302.app.widget.utils.CommonHtmlUtil
import kotlinx.coroutines.delay
import org.json.JSONObject

class PayActivity : BaseActivity() {
    private lateinit var binding: ActivityPayBinding
    private var mWebView: BridgeWebView? = null
    private var isUSDT = false
    private var mPayDetailDialog: PayDetailsDialog? = null
    private var mQuickAccessBean: HtmlQuickAccessBean? = null
    private var payType = ""


    var uploadMessageAboveL: ValueCallback<Array<Uri?>?>? = null
    val FILECHOOSER_RESULTCODE_FOR_ANDROID_5:Int = 5174
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            it.data?.dataString?.apply {
                uploadMessageAboveL?.onReceiveValue(arrayOf(Uri.parse(this)))
                uploadMessageAboveL = null
            }
        }else if(it.resultCode == RESULT_CANCELED){
            if(uploadMessageAboveL != null){
                uploadMessageAboveL?.onReceiveValue(null)
                uploadMessageAboveL = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backImage.setOnClickListener {
            finish()
        }

        loadWebHandler(
            CommonEnum.LoadHtmlType.NEED_SPLICED_PARA,
            CommonHtmlUtil.totalHtmlPath
        )

//        loadWebHandler(
//            CommonEnum.LoadHtmlType.NOT_NEED_SPLICED_PARA,
//            CommonHtmlUtil.chargeHtmlPath
//        )

    }


    /**
     * 加载webView链接
     * @param type       加载类型
     * @param htmlUrl    加载路径
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun loadWebHandler(type: Int, htmlUrl: String) {

        mWebView = binding?.webView
        mWebView?.settings?.javaScriptEnabled = true
//        mWebView?.settings?.databaseEnabled = true //数据库缓存
        mWebView?.settings?.setGeolocationEnabled(true) // 允许网页定位
        mWebView?.settings?.loadsImagesAutomatically = true // 加快网页加载完成的速度，等页面完成再加载图片
        mWebView?.settings?.domStorageEnabled = true       // 开启 localStorage
        mWebView?.setDefaultHandler(DefaultHandler())
        mWebView?.webChromeClient = WebChromeClient()
        // 如果需要，还可以设置透明度
        mWebView?.setAlpha(1.0f)
        // 设置背景色为白色（覆盖默认的透明/黑色）
        mWebView?.setBackgroundColor(Color.WHITE)
        // 加载前显示白色背景（避免短暂黑屏）
        mWebView?.background = ContextCompat.getDrawable(this@PayActivity, android.R.color.white)
        /*mWebView?.apply {
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
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            }*/
        }*/

        mWebView?.webChromeClient = object : WebChromeClient() {
            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri?>?>,
                fileChooserParams: FileChooserParams
            ): Boolean {

                try {
                    val intent = fileChooserParams?.createIntent()?:return super.onShowFileChooser(webView,filePathCallback,fileChooserParams)
                    uploadMessageAboveL = filePathCallback
                    launcher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    uploadMessageAboveL = null
                    return false
                }
                return true
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.e("ceshi","onReceivedTitle$title${view?.url}")
                if (view?.url == "https://dash.proxy302.com/charge") {//https://dash.proxy302.com/charge
                    // 在这里可以进行你想要的操作，比如记录日志、更新UI等
                    isUSDT = false
                    Log.d("ceshi", "监听到跳转到https://dash.proxy302.com/webapp/charge")
                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,"https://dash.proxy302.com/webapp/charge")
                }
            }

        }



        var url = ""
        when (type) {
            CommonEnum.LoadHtmlType.NEED_SPLICED_PARA -> {
                url =
                    WearUtil.APP_SERVER_HTTPS_HTML + htmlUrl + WearData.getInstance().token + "/?device=" + AppConstant.device
            }

            CommonEnum.LoadHtmlType.NOT_NEED_SPLICED_PARA -> {
                url = WearUtil.APP_SERVER_HTTPS_HTML + htmlUrl
            }

            CommonEnum.LoadHtmlType.DIRECT_LINK -> {
                url = htmlUrl
            }
        }

        LogUtils.e("ceshi  url是什么=================：", url)
        //url = "https://302.ai/charge"
        //url = "https://dash.proxy302.com/webapp/authentication/MTQ1MzE2NzI4N0BxcS5jb206U3ExVXZtZ3A6ZDI=/?device=android"
        mWebView?.loadUrl(url)

        mWebView?.registerHandler(CommonHtmlUtil.handlerNamePara, object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.e("测试","ffffffffffff")
                LogUtils.e("ceshi 是否有数据过来============:", data.toString())

                showHtmlAnalysisData(data)
            }
        })


    }

    /**
     * 数据处理
     */
    fun showHtmlAnalysisData(jsonStr: String?) {
        var chargeId = 0
        if (jsonStr != null) {
            val jsonObj = JSONObject(jsonStr)

            val type = jsonObj.optString(CommonHtmlUtil.htmlType)
            val data = jsonObj.optString(CommonHtmlUtil.htmlData)
            LogUtils.e("ceshi 解析的数据是什么==============：", type)

            when (type) {
                CommonHtmlUtil.chargeHtml -> {

                    mPayDetailDialog = PayDetailsDialog(this@PayActivity, mQuickAccessBean)
                    mPayDetailDialog?.show()




                    val chargeJsonBean = Gson().fromJson(data, HtmlChargeJsonBean::class.java)
                    if (chargeJsonBean != null) {
                        LogUtils.e(
                            "ceshi 输出的数据类型是什么===========：",
                            chargeJsonBean.note,
                            chargeJsonBean.id
                        )
                        chargeId = chargeJsonBean.id
                        mPayDetailDialog?.setOnButtonClickListener(object : PayDetailsDialog.OnButtonClickListener {
                            override fun onButtonClick(buttonText: String) {
                                Log.e("ceshi","onButtonClick$buttonText")
                                when (buttonText) {
                                    "aliPay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        MainModel.getAliPayUrl(chargeId, object : LoginCallback {
                                            override fun onSuccess(responseBody: String?) {
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                                                Log.e("ceshi","ali+++++${response.data.to}")
                                                runOnUiThread {
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to)
                                                    val intent = Intent(Intent.ACTION_VIEW)
                                                    intent.setData(Uri.parse(response.data.to))
                                                    ActivityUtils.startActivity(intent)
                                                }

                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","ali----")
                                            }

                                        })


                                    }
                                    "usdtPay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        MainModel.getUstdurl(chargeId, object : LoginCallback {
                                            override fun onSuccess(responseBody: String?) {
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                                                Log.e("ceshi","ali+++++${response.data.url}")
                                                runOnUiThread {
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url)
                                                    //mPayDetailDialog?.setUrlListener(response.data.url)
                                                    Log.e("ceshi","++url${response.data.url}")
                                                    //mBinding?.incMain?.webViewPay?.evaluateJavascript("document.body.innerHTML = \"\";", null)

                                                    /*mBinding?.incMain?.webViewPay?.loadUrl(response.data.url)
                                                    lifecycleScope.launch(Dispatchers.Main) {
                                                        // 延迟1秒（1000毫秒）
                                                        delay(1300)
                                                        mBinding?.incMain?.webView?.visibility = View.GONE
                                                        mBinding?.incMain?.webViewPay?.visibility = View.VISIBLE
                                                    }*/
                                                }
                                                isUSDT = true

                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","ali----")
                                            }

                                        })
                                    }
                                    "stripePay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        payType = "stripePay"
                                        MainModel.getStripeKey(chargeId, object : LoginCallback {
                                            override fun onSuccess(responseBody: String?) {
                                                Log.e("ceshi","stripe++++")
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseStripeData::class.java)
                                                var publishableKey = ""
                                                publishableKey = response.data.jk+response.data.hk+response.data.pk+response.data.a
                                                Log.e("ceshi","ali+++++${publishableKey}")
                                                //sendDataToJavaScript(publishableKey)
                                                var url = "https://dash.proxy302.com/stripe?pk=$publishableKey&sid=${response.data.session_id}"
                                                Log.e("ceshi","url++++${url}")

                                                runOnUiThread {
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,url)
                                                    //mPayDetailDialog?.setUrlListener(url)
                                                    Log.e("ceshi","--url${url}")
                                                    //loadWebHandler1(url)
                                                }
                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","stripe----")
                                            }

                                        })
                                    }
                                }
                            }

                        })

                    }
                }

                CommonHtmlUtil.payBillHtml -> {
                    //HtmlManager.jumpHtml(this@HomeActivity, data)
                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,data)
                }
            }
        }
    }
    //


}